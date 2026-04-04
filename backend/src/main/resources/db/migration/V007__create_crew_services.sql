--V007 create crew_services join table

CREATE TABLE crew_services (
    crew_profile_id UUID NOT NULL REFERENCES crew_profiles(id) ON DELETE CASCADE,
    category_id     UUID NOT NULL REFERENCES service_categories(id),
    PRIMARY KEY (crew_profile_id, category_id)
);
