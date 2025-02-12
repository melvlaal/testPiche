CREATE TABLE account (
    id BIGSERIAL PRIMARY KEY,
    account_number VARCHAR(255) NOT NULL UNIQUE,
    balance NUMERIC(15, 2)
);