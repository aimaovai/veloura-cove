CREATE TABLE accommodation_types (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    slug VARCHAR(100) NOT NULL UNIQUE,
    name VARCHAR(150) NOT NULL,
    short_description VARCHAR(500) NOT NULL,
    long_description TEXT NOT NULL,
    category VARCHAR(20) NOT NULL CHECK (category IN ('ROOM', 'SUITE', 'VILLA')),
    max_guests INTEGER NOT NULL CHECK (max_guests > 0),
    max_adults INTEGER NOT NULL CHECK (max_adults > 0),
    max_children INTEGER NOT NULL CHECK (max_children >= 0),
    bed_configuration VARCHAR(150) NOT NULL,
    room_size_sq_ft INTEGER NOT NULL CHECK (room_size_sq_ft > 0),
    base_nightly_rate NUMERIC(12,2) NOT NULL CHECK (base_nightly_rate >= 0),
    currency VARCHAR(3) NOT NULL,
    view_type VARCHAR(100) NOT NULL,
    featured BOOLEAN NOT NULL DEFAULT FALSE,
    sort_order INTEGER NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    check_in_time TIME NOT NULL,
    check_out_time TIME NOT NULL,
    hero_image VARCHAR(1000) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE amenities (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL UNIQUE,
    slug VARCHAR(120) NOT NULL UNIQUE
);
CREATE TABLE accommodation_amenities (
    accommodation_type_id UUID NOT NULL REFERENCES accommodation_types(id) ON DELETE CASCADE,
    amenity_id UUID NOT NULL REFERENCES amenities(id) ON DELETE CASCADE,
    PRIMARY KEY (accommodation_type_id, amenity_id)
);
CREATE TABLE accommodation_features (
    accommodation_type_id UUID NOT NULL REFERENCES accommodation_types(id) ON DELETE CASCADE,
    display_order INTEGER NOT NULL,
    feature VARCHAR(180) NOT NULL,
    PRIMARY KEY (accommodation_type_id, display_order)
);
CREATE TABLE accommodation_images (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    accommodation_type_id UUID NOT NULL REFERENCES accommodation_types(id) ON DELETE CASCADE,
    image_url VARCHAR(1000) NOT NULL,
    alt_text VARCHAR(250) NOT NULL,
    image_type VARCHAR(30) NOT NULL,
    display_order INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE accommodation_units (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    accommodation_type_id UUID NOT NULL REFERENCES accommodation_types(id),
    unit_code VARCHAR(30) NOT NULL UNIQUE,
    floor_location VARCHAR(100) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    maintenance_status VARCHAR(20) NOT NULL DEFAULT 'READY'
        CHECK (maintenance_status IN ('READY', 'MAINTENANCE'))
);
CREATE INDEX idx_accommodation_units_type_active ON accommodation_units(accommodation_type_id, active, maintenance_status);

CREATE TABLE seasonal_rates (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    accommodation_type_id UUID REFERENCES accommodation_types(id) ON DELETE CASCADE,
    name VARCHAR(150) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    nightly_rate NUMERIC(12,2),
    percentage_adjustment NUMERIC(7,4),
    priority INTEGER NOT NULL DEFAULT 0,
    CHECK (start_date < end_date),
    CHECK ((nightly_rate IS NOT NULL AND percentage_adjustment IS NULL)
        OR (nightly_rate IS NULL AND percentage_adjustment IS NOT NULL))
);
CREATE INDEX idx_seasonal_rates_dates ON seasonal_rates(start_date, end_date);

CREATE TABLE resort_settings (
    id SMALLINT PRIMARY KEY CHECK (id = 1),
    resort_name VARCHAR(150) NOT NULL,
    phone VARCHAR(50) NOT NULL,
    email VARCHAR(254) NOT NULL,
    address_text VARCHAR(500) NOT NULL,
    check_in_time TIME NOT NULL,
    check_out_time TIME NOT NULL,
    default_currency VARCHAR(3) NOT NULL,
    resort_fee_per_night NUMERIC(12,2) NOT NULL CHECK (resort_fee_per_night >= 0),
    tax_percentage NUMERIC(7,4) NOT NULL CHECK (tax_percentage >= 0),
    cancellation_window_hours INTEGER NOT NULL CHECK (cancellation_window_hours >= 0),
    hold_duration_minutes INTEGER NOT NULL CHECK (hold_duration_minutes > 0),
    max_stay_nights INTEGER NOT NULL CHECK (max_stay_nights > 0)
);

CREATE TABLE offers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    slug VARCHAR(100) NOT NULL UNIQUE,
    name VARCHAR(150) NOT NULL,
    description TEXT NOT NULL,
    hero_image VARCHAR(1000) NOT NULL,
    discount_type VARCHAR(20) NOT NULL CHECK (discount_type IN ('PERCENTAGE', 'FIXED', 'PACKAGE')),
    discount_value NUMERIC(12,2) NOT NULL CHECK (discount_value >= 0),
    minimum_nights INTEGER NOT NULL DEFAULT 1 CHECK (minimum_nights > 0),
    valid_from DATE,
    valid_until DATE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    terms TEXT NOT NULL,
    CHECK (valid_from IS NULL OR valid_until IS NULL OR valid_from <= valid_until)
);
CREATE TABLE offer_accommodation_types (
    offer_id UUID NOT NULL REFERENCES offers(id) ON DELETE CASCADE,
    accommodation_type_id UUID NOT NULL REFERENCES accommodation_types(id) ON DELETE CASCADE,
    PRIMARY KEY (offer_id, accommodation_type_id)
);

CREATE TABLE experiences (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    slug VARCHAR(100) NOT NULL UNIQUE,
    name VARCHAR(150) NOT NULL,
    category VARCHAR(30) NOT NULL CHECK (category IN ('WELLNESS', 'DINING', 'ADVENTURE', 'ROMANCE', 'WATER', 'CULTURE')),
    short_description VARCHAR(500) NOT NULL,
    long_description TEXT NOT NULL,
    duration_minutes INTEGER NOT NULL CHECK (duration_minutes > 0),
    starting_price NUMERIC(12,2) NOT NULL CHECK (starting_price >= 0),
    currency VARCHAR(3) NOT NULL,
    hero_image VARCHAR(1000) NOT NULL,
    featured BOOLEAN NOT NULL DEFAULT FALSE,
    active BOOLEAN NOT NULL DEFAULT TRUE
);
CREATE TABLE experience_images (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    experience_id UUID NOT NULL REFERENCES experiences(id) ON DELETE CASCADE,
    image_url VARCHAR(1000) NOT NULL,
    alt_text VARCHAR(250) NOT NULL,
    display_order INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE guests (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(254) NOT NULL,
    phone VARCHAR(50) NOT NULL,
    country VARCHAR(100) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE TABLE reservations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    accommodation_unit_id UUID NOT NULL REFERENCES accommodation_units(id),
    guest_id UUID REFERENCES guests(id),
    check_in DATE NOT NULL,
    check_out DATE NOT NULL,
    adults INTEGER NOT NULL CHECK (adults > 0),
    children INTEGER NOT NULL CHECK (children >= 0),
    status VARCHAR(20) NOT NULL CHECK (status IN ('HELD', 'CONFIRMED', 'CANCELLED', 'EXPIRED', 'CHECKED_IN', 'CHECKED_OUT')),
    expires_at TIMESTAMPTZ,
    arrival_notes VARCHAR(1000),
    special_requests VARCHAR(2000),
    estimated_arrival_time VARCHAR(40),
    celebration_type VARCHAR(40),
    confirmation_number VARCHAR(20) UNIQUE,
    management_token_hash VARCHAR(64) UNIQUE,
    reservation_creation_key VARCHAR(120) UNIQUE,
    last_payment_key VARCHAR(120),
    last_payment_result VARCHAR(20),
    simulated_payment_reference VARCHAR(40),
    currency VARCHAR(3) NOT NULL,
    room_subtotal NUMERIC(12,2) NOT NULL,
    resort_fee NUMERIC(12,2) NOT NULL,
    tax_amount NUMERIC(12,2) NOT NULL,
    total_amount NUMERIC(12,2) NOT NULL,
    price_breakdown TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CHECK (check_in < check_out)
);
CREATE INDEX idx_reservations_unit_dates ON reservations(accommodation_unit_id, check_in, check_out);
CREATE INDEX idx_reservations_status_expiry ON reservations(status, expires_at);
CREATE INDEX idx_reservations_guest ON reservations(guest_id);

CREATE TABLE inquiries (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(150) NOT NULL,
    email VARCHAR(254) NOT NULL,
    phone VARCHAR(50),
    event_date DATE,
    party_size INTEGER CHECK (party_size IS NULL OR party_size > 0),
    inquiry_type VARCHAR(30) NOT NULL CHECK (inquiry_type IN ('WEDDING', 'PRIVATE_EVENT', 'CORPORATE_RETREAT', 'GENERAL', 'TRANSPORTATION')),
    message VARCHAR(3000) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_inquiries_created_at ON inquiries(created_at);
