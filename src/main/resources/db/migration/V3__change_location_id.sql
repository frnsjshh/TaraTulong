ALTER TABLE location
    DROP CONSTRAINT IF EXISTS fk_location_on_parent;

ALTER TABLE location
    DROP COLUMN parent_id;

ALTER TABLE location
    DROP COLUMN id;


ALTER TABLE location
    ADD COLUMN id UUID DEFAULT gen_random_uuid() NOT NULL PRIMARY KEY;


ALTER TABLE location
    ADD parent_id UUID;

ALTER TABLE location
    ADD CONSTRAINT FK_LOCATION_ON_PARENT
        FOREIGN KEY (parent_id) REFERENCES location (id);