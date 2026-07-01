CREATE INDEX idx_settlement_created_at
    ON settlement(created_at);

CREATE INDEX idx_receivable_seller_name
    ON receivable(seller_name);

CREATE INDEX idx_receivable_currency
    ON receivable(currency_id);