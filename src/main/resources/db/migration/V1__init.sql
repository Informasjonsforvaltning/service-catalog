CREATE TABLE services (
    id            VARCHAR(255) PRIMARY KEY,
    catalog_id    VARCHAR(255) NOT NULL,
    published     BOOLEAN NOT NULL DEFAULT FALSE,
    service_type  VARCHAR(50) NOT NULL,
    data          JSONB
);

CREATE INDEX idx_services_catalog_id ON services (catalog_id);
CREATE INDEX idx_services_catalog_id_published ON services (catalog_id, published);
CREATE INDEX idx_services_catalog_id_service_type ON services (catalog_id, service_type);

