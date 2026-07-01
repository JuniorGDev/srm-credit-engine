CREATE TABLE settlement (
    id                  BIGSERIAL PRIMARY KEY,
    receivable_id       BIGINT         NOT NULL,
    payment_currency_id BIGINT         NOT NULL,
    exchange_rate_value DECIMAL(19, 6) NOT NULL CHECK ( exchange_rate_value > 0 ),
    present_value       DECIMAL(19, 2) NOT NULL CHECK ( present_value > 0 ),
    discount_value      DECIMAL(19, 2) NOT NULL CHECK ( discount_value > 0 ),
    settled_amount      DECIMAL(19, 2) NOT NULL CHECK ( settled_amount > 0 ),
    created_at          TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP,

    CONSTRAINT fk_settlement_receivable
        FOREIGN KEY (receivable_id)
            REFERENCES receivable (id),
    CONSTRAINT fk_settlement_payment_currency
        FOREIGN KEY (payment_currency_id)
            REFERENCES currency (id)
);

CREATE INDEX idx_settlement_receivable
    ON settlement (receivable_id);

CREATE INDEX idx_settlement_payment_currency
    ON settlement (payment_currency_id);
