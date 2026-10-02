-- Drop the old free-text location column from event
ALTER TABLE event
    DROP COLUMN location;

-- Add a FK to the location table
ALTER TABLE event
    ADD COLUMN location_id UUID;

ALTER TABLE event
    ADD CONSTRAINT FK_EVENT_ON_LOCATION
        FOREIGN KEY (location_id) REFERENCES location (id);

CREATE INDEX idx_event_location ON event (location_id);
