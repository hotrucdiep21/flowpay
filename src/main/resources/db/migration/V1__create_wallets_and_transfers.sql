CREATE TABLE wallets
(
    id       VARCHAR(100) PRIMARY KEY,
    owner_id VARCHAR(100)   NOT NULL,
    balance  NUMERIC(19, 2) NOT NULL,
    status   VARCHAR(20)    NOT NULL,
    version  BIGINT         NOT NULL DEFAULT 0,

    CONSTRAINT chk_wallet_balance_non_negative
        CHECK ( balance >= 0 ),
    CONSTRAINT chk_wallet_status
        CHECK ( status IN ('ACTIVE', 'BLOCKED', 'CLOSED') )
);

CREATE TABLE transfers
(
    id                 VARCHAR(100) PRIMARY KEY,
    request_id         VARCHAR(100)             NOT NULL,
    sender_wallet_id   VARCHAR(100)             NOT NULL,
    receiver_wallet_id VARCHAR(100)             NOT NULL,
    amount             NUMERIC(19, 2)           NOT NULL,
    status             VARCHAR(20)              NOT NULL,
    created_at         TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_transfer_request_id
        UNIQUE (request_id),

    CONSTRAINT chk_transfer_amount_positive
        CHECK ( amount > 0 ),

    CONSTRAINT chk_transfer_different_wallets
        CHECK ( sender_wallet_id <> receiver_wallet_id ),

    CONSTRAINT chk_transfer_status
        CHECK ( status IN ('PENDING', 'SUCCEEDED', 'FAILED') )
);

CREATE INDEX idx_transfers_sender_wallet_id
    ON transfers (sender_wallet_id);

CREATE INDEX idx_transfers_receiver_wallet_id
    ON transfers (receiver_wallet_id);