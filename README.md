# Veloura Cove

Veloura Cove is a fictional luxury boutique resort and a software-engineering portfolio project. It is not a real hotel: reservations and inventory are demonstrations, and payment outcomes are simulated. No real payment provider is connected and no card information is collected.

## Architecture

- Java 21 and Spring Boot 3 REST API, rooted at `/api/v1`
- PostgreSQL persistence managed by Flyway; Hibernate validates, but does not create, the schema
- A modular `accommodation`, `availability`, `pricing`, `reservation`, `offer`, `experience`, `inquiry`, and `common` code structure
- Availability comes from physical accommodation units and overlapping reservations. Holds and confirmed stays block inventory; expired holds do not.
- The pricing service computes per-night seasonal rates, resort fees, and tax using `BigDecimal`.
- Reservation lifecycle: Search → Availability → 15-minute Hold → Guest Details → Simulated Payment → Confirmation
- OpenAPI documentation is available at `/swagger-ui.html`.

Inventory assignment locks eligible unit rows in a transaction, then rechecks overlapping reservations before creating a hold. This serializes competing assignments for the same accommodation type without trusting an earlier availability search. Reservation creation requires an `Idempotency-Key`; payment simulation is also idempotent by key and confirmed reservations cannot be confirmed twice.

Guest reservation management uses a cryptographically random token. Only a SHA-256 digest is persisted; the raw token is returned at reservation creation and is not logged. Public endpoints return structured API errors and validate bounded request data. CORS origins and database credentials are environment-configurable.
Inquiry submissions, reservation holds/creation, and simulated-payment requests also have a lightweight in-memory limit of 10 requests per minute per remote address and endpoint group. This demo protection is per application instance; a production deployment should enforce shared rate limits at its gateway.

## Local development

Requirements: Java 21, Maven, and Docker Compose.

```powershell
docker compose up -d postgres
$env:DB_USERNAME = "veloura"
$env:DB_PASSWORD = "veloura"
mvn spring-boot:run
```

The application listens on port `8080`. PostgreSQL defaults to `localhost:5432` and database `veloura_cove`; override with `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD`. `CORS_ALLOWED_ORIGINS` is a comma-separated origin list (defaults to localhost development origins). Flyway creates and seeds the schema on startup.

Example requests:

```http
GET /api/v1/accommodation-types
GET /api/v1/availability?checkIn=2027-02-10&checkOut=2027-02-13&adults=2&children=0
POST /api/v1/reservation-holds
Content-Type: application/json

{"accommodationTypeId":"<uuid>","checkIn":"2027-02-10","checkOut":"2027-02-13","adults":2,"children":0}
```

Guest details are submitted to `POST /api/v1/reservations` with the returned `holdId` and an `Idempotency-Key` header.

See the OpenAPI UI for the complete request and response contract, including validation and endpoint descriptions. Simulated payment accepts only `SUCCESS` or `DECLINED`; it never contacts a payment provider. Example reservation requests use `holdId` plus guest fields; `POST /api/v1/reservations` and `POST /api/v1/reservations/{id}/payment-simulation` both require an `Idempotency-Key` header.

The main endpoints are:

| Endpoint | Purpose |
|---|---|
| `GET /api/v1/accommodation-types`, `GET /api/v1/accommodation-types/{slug}` | Browse rooms and room details |
| `GET /api/v1/accommodation-types/compare?ids=...` | Compare two or three rooms |
| `GET /api/v1/availability` | Search inventory and estimated itemized prices |
| `GET /api/v1/accommodation-types/{slug}/availability-calendar` | Inventory-backed nightly calendar |
| `POST /api/v1/reservation-holds`, `POST /api/v1/reservations` | Hold inventory and attach guest details |
| `POST /api/v1/reservations/{id}/payment-simulation` | Demo-only payment outcome |
| `GET /api/v1/reservations/manage/{token}`, `POST /api/v1/reservations/manage/{token}/cancel` | Private reservation management |
| `GET /api/v1/offers`, `GET /api/v1/experiences` | Browse resort offers and experiences |
| `POST /api/v1/inquiries` | Submit an inquiry (not a booking) |
| `GET /api/v1/resort-settings` | Public resort contact and policy settings |

Validation errors use a consistent JSON shape with `timestamp`, `status`, `code`, `message`, and `fieldErrors`.

## Demo data and scope

Flyway seeds six accommodation categories, physical units, amenities, offers, seasonal prices, experiences, and resort settings. Seed imagery uses replaceable remote Unsplash URLs.

This backend intentionally does not implement real payments, production identity/authentication, or production-grade distributed rate limiting. Reservation-management tokens are bearer credentials and should be handled as secrets by clients. The fictional resort has no real inventory or bookable stays.
