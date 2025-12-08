-- Resource Service Database Schema

CREATE TABLE IF NOT EXISTS resources (
    id BIGSERIAL PRIMARY KEY,
    data BYTEA
);