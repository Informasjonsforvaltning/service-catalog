CREATE TABLE services (
    id            VARCHAR(255) PRIMARY KEY,
    catalog_id    VARCHAR(255) NOT NULL,
    title_nb      TEXT,
    title_nn      TEXT,
    title_en      TEXT,
    description_nb TEXT,
    description_nn TEXT,
    description_en TEXT,
    published     BOOLEAN NOT NULL DEFAULT FALSE,
    produces      JSONB,
    contact_points JSONB,
    homepage      TEXT,
    status        TEXT,
    spatial       JSONB,
    subject       JSONB
);

CREATE INDEX idx_services_catalog_id ON services (catalog_id);
CREATE INDEX idx_services_catalog_id_published ON services (catalog_id, published);

CREATE TABLE public_services (
    id            VARCHAR(255) PRIMARY KEY,
    catalog_id    VARCHAR(255) NOT NULL,
    title_nb      TEXT,
    title_nn      TEXT,
    title_en      TEXT,
    description_nb TEXT,
    description_nn TEXT,
    description_en TEXT,
    dct_type      JSONB,
    published     BOOLEAN NOT NULL DEFAULT FALSE,
    produces      JSONB,
    contact_points JSONB,
    homepage      TEXT,
    status        TEXT,
    spatial       JSONB,
    subject       JSONB
);

CREATE INDEX idx_public_services_catalog_id ON public_services (catalog_id);
CREATE INDEX idx_public_services_catalog_id_published ON public_services (catalog_id, published);
