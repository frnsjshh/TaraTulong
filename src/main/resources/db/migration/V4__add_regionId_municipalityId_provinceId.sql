ALTER TABLE location
    ADD municipality_id INTEGER;

ALTER TABLE location
    ADD province_id INTEGER;

ALTER TABLE location
    ADD region_id INTEGER;

ALTER TABLE location
    ALTER COLUMN municipality_id SET NOT NULL;

ALTER TABLE location
    ALTER COLUMN province_id SET NOT NULL;

ALTER TABLE location
    ALTER COLUMN region_id SET NOT NULL;