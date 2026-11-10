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
- Next.js 15 App Router frontend with responsive editorial design, reservation search, live availability calendar, stay comparison, guest inquiry, and private reservation management.
- The frontend talks directly to `/api/v1` through a single typed API client. It does not provide fake inventory or confirmation data.

Inventory assignment locks eligible unit rows in a transaction, then rechecks overlapping reservations before creating a hold. This serializes competing assignments for the same accommodation type without trusting an earlier availability search. Reservation creation requires an `Idempotency-Key`; payment simulation is also idempotent by key and confirmed reservations cannot be confirmed twice.

Guest reservation management uses a cryptographically random token. Only a SHA-256 digest is persisted; the raw token is returned at reservation creation and is not logged. Public endpoints return structured API errors and validate bounded request data. CORS origins and database credentials are environment-configurable.
Inquiry submissions, reservation holds/creation, and simulated-payment requests also have a lightweight in-memory limit of 10 requests per minute per remote address and endpoint group. This demo protection is per application instance; a production deployment should enforce shared rate limits at its gateway.

## Run the entire app locally

You need **Docker Desktop** with its engine running, **Java 21**, **Maven 3.9+**, and **Node.js 20.9+ with npm**. Clone the repository, open a terminal in the repository root, and follow these steps. The commands below are for Windows PowerShell; for macOS/Linux, use `export NAME=value` instead of `$env:NAME = "value"`.

### 1. Start the database

```powershell
docker compose up -d postgres
docker compose ps postgres
```

Wait until the container reports `healthy`. Compose starts PostgreSQL 16, creates the `veloura_cove` database, and publishes it on `localhost:5433` to avoid conflicting with a local PostgreSQL installation. The local demo credentials are `veloura` / `veloura`.

### 2. Start the backend

Open a **second terminal** in the repository root:

```powershell
$env:DB_URL = "jdbc:postgresql://localhost:5433/veloura_cove"
$env:DB_USERNAME = "veloura"
$env:DB_PASSWORD = "veloura"
mvn spring-boot:run
```

Wait for Spring Boot to finish starting. Flyway will create and seed the schema automatically. The API is at `http://localhost:8080/api/v1`; check it by opening `http://localhost:8080/api/v1/accommodation-types`. Swagger UI is at `http://localhost:8080/swagger-ui.html`.

If port `8080` is already in use, start the backend with `$env:SERVER_PORT = "8081"` before `mvn spring-boot:run`; use that same port in the frontend API URL in step 3. If you have previously changed the Compose database credentials, use those same values for `DB_USERNAME` and `DB_PASSWORD`. PostgreSQL keeps its data in a named volume, so changing Compose environment values does not change credentials for a database that has already been initialized.

### 3. Start the frontend

Open a **third terminal** in the repository root:

```powershell
npm install
$env:NEXT_PUBLIC_API_BASE_URL = "http://localhost:8080/api/v1"
npm run dev
```

If you started the backend on port `8081`, set the URL to `http://localhost:8081/api/v1` instead. Open **http://localhost:3000**. Keep all three terminals running while using the app. The frontend reads room, availability, pricing, and reservation data from the backend; it displays an error if the API is unavailable rather than inventing booking data.

To stop the app, press `Ctrl+C` in the frontend and backend terminals, then stop PostgreSQL from any terminal in the repository root:

```powershell
docker compose down
```

This stops the container but keeps its database volume and data for the next run. Optional `NEXT_PUBLIC_SITE_URL` configures the public metadata base URL. `CORS_ALLOWED_ORIGINS` configures allowed browser origins and defaults to the local frontend origins.

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

The main backend endpoints are:

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

## Frontend routes

`/` · `/stay` · `/stay/[slug]` · `/compare` · `/book` · `/manage` · `/experiences` · `/experiences/[slug]` · `/dining` · `/wellness` · `/offers` · `/offers/[slug]` · `/the-cove` · `/private-events` · `/privacy` · `/terms`

The booking UI uses a temporary inventory hold, stores its private management token in the current browser session only, and calls the backend payment-simulation endpoint. The token is not included in the website URL. Booking and management pages are marked non-cacheable, and private routes use a no-referrer policy.

Dining, wellness, and experience pages are informational; the API does not provide dining or experience reservations. Offers are browsable but are not yet applied to reservation pricing. An abandoned hold remains unavailable until its 15-minute expiry because there is no hold-release endpoint. If reservation creation is retried after its response is lost, an idempotent replay does not return the management token again; the guest should preserve the original successful response securely.

## Validation

```powershell
npm run typecheck
npm run lint
npm run build
npm test
mvn test
```

## Demo data and scope

Flyway seeds six accommodation categories, physical units, amenities, offers, seasonal prices, experiences, and resort settings. Seed imagery uses replaceable remote Unsplash URLs.

This backend intentionally does not implement real payments, production identity/authentication, or production-grade distributed rate limiting. Reservation-management tokens are bearer credentials and should be handled as secrets by clients. The fictional resort has no real inventory or bookable stays.
