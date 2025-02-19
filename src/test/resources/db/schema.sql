CREATE TABLE accounts (
    id BIGSERIAL PRIMARY KEY,
    account_number VARCHAR(255) NOT NULL UNIQUE,
    balance NUMERIC(15, 2)
);

CREATE TABLE transaction_history (
    id BIGSERIAL PRIMARY KEY,                      -- Unique transaction ID
    account_id_from BIGINT,                   -- Sender's account (NULL for deposits)
    account_id_to BIGINT,                     -- Receiver's account (NULL for withdrawals)
    amount NUMERIC(15, 2) NOT NULL,                -- Transaction amount with two decimal places
    transaction_type VARCHAR(20) NOT NULL,         -- Type: DEPOSIT, WITHDRAW, TRANSFER
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP -- Timestamp of the transaction
);