--V006 create service_categories table

CREATE TABLE service_categories (
    id   UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL UNIQUE,
    slug VARCHAR(100) NOT NULL UNIQUE
);
