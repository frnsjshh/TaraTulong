CREATE TABLE location
(
    id        BIGINT       NOT NULL,
    code      VARCHAR(255) NOT NULL,
    name      VARCHAR(255) NOT NULL,
    type      VARCHAR(255) NOT NULL,
    parent_id BIGINT,
    CONSTRAINT pk_location PRIMARY KEY (id)
);

ALTER TABLE location
    ADD CONSTRAINT uc_location_code UNIQUE (code);

ALTER TABLE location
    ADD CONSTRAINT FK_LOCATION_ON_PARENT FOREIGN KEY (parent_id) REFERENCES location (id);