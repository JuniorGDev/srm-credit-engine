CREATE TABLE exchange_rate (
    id               BIGSERIAL PRIMARY KEY,

    from_currency_id BIGINT         NOT NULL,
    to_currency_id   BIGINT         NOT NULL,

    rate             DECIMAL(19, 8) NOT NULL,

    created_at       TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_exchange_rate_from_currency
        FOREIGN KEY (from_currency_id)
            REFERENCES currency (id),

    CONSTRAINT fk_exchange_rate_to_currency
        FOREIGN KEY (to_currency_id)
            REFERENCES currency (id),

    CONSTRAINT uk_exchange_rate_currency
        UNIQUE (from_currency_id, to_currency_id)
);

CREATE INDEX idx_exchange_rate_currency
    ON exchange_rate (from_currency_id, to_currency_id);