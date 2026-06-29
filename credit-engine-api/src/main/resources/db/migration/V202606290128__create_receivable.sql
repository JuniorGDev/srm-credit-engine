CREATE TABLE receivable (
    id              BIGSERIAL PRIMARY KEY,
    seller_name     VARCHAR(150)   NOT NULL,
    face_value      DECIMAL(19, 2) NOT NULL CHECK (face_value > 0),
    due_date        DATE           NOT NULL,
    currency_id     BIGINT         NOT NULL,
    receivable_type VARCHAR(60)    NOT NULL,
    created_at      TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP,

    CONSTRAINT fk_receivable_currency
        FOREIGN KEY (currency_id)
            REFERENCES currency (id)
);

CREATE INDEX idx_receivable_due_date
    ON receivable (due_date);