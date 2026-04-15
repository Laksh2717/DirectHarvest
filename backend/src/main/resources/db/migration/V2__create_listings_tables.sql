CREATE TABLE listings (
    id BIGSERIAL PRIMARY KEY,
    farmer_id BIGINT NOT NULL,
    title VARCHAR(150) NOT NULL,
    description VARCHAR(1000),
    crop_name VARCHAR(120) NOT NULL,
    quantity NUMERIC(12, 2) NOT NULL,
    price_per_kg NUMERIC(12, 2) NOT NULL,
    available_from DATE,
    location VARCHAR(255),
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT fk_listings_farmer FOREIGN KEY (farmer_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE INDEX idx_listings_farmer_id ON listings (farmer_id);
CREATE INDEX idx_listings_status ON listings (status);

CREATE TABLE listing_images (
    id BIGSERIAL PRIMARY KEY,
    listing_id BIGINT NOT NULL,
    cloudinary_public_id VARCHAR(255) NOT NULL,
    cloudinary_secure_url VARCHAR(500) NOT NULL,
    format VARCHAR(20),
    width INTEGER,
    height INTEGER,
    bytes BIGINT,
    is_primary BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_listing_images_listing FOREIGN KEY (listing_id) REFERENCES listings (id) ON DELETE CASCADE
);

CREATE INDEX idx_listing_images_listing_id ON listing_images (listing_id);

