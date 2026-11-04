CREATE TABLE orders(
    id UUID PRIMARY KEY ,
    customer_id VARCHAR(100) NOT NULL,
    amount NUMERIC(19, 2) NOT NULL CHECK (amount > 0),
    currency VARCHAR(3) NOT NULL,
    status VARCHAR(30) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_orders_customer_id
  ON orders(customer_id);

CREATE INDEX idx_orders_status
  ON orders(status);

CREATE INDEX idx_orders_created_at
   ON orders(created_at);