-- Products table
CREATE TABLE IF NOT EXISTS product (
  id BIGSERIAL PRIMARY KEY,
  description VARCHAR(255) NOT NULL
);

-- Order ↔ Product (many-to-many) join table
CREATE TABLE IF NOT EXISTS order_product (
  order_id   BIGINT NOT NULL,
  product_id BIGINT NOT NULL,
  PRIMARY KEY (order_id, product_id),
  CONSTRAINT fk_op_order
    FOREIGN KEY (order_id) REFERENCES "order"(id) ON DELETE CASCADE,
  CONSTRAINT fk_op_product
    FOREIGN KEY (product_id) REFERENCES product(id) ON DELETE CASCADE
);

-- Resetting the customer_id_seq sequence.
-- Since data.sql has inserted Customer records with IDs up to 100,
-- we restart the sequence at 101. This ensures that when a new Customer
-- is created via JPA/Hibernate, the next value will be 101, avoiding
-- "duplicate key value violates unique constraint" errors.
ALTER SEQUENCE customer_id_seq RESTART WITH 101;

-- Resetting the order_id_seq sequence.
-- Since data.sql has inserted Order records with IDs up to 10000,
-- we restart the sequence at 10001. This ensures that when a new Order
-- is created, the next value will continue from 10001 and not collide
-- with preloaded IDs, preventing duplicate key constraint violations.
ALTER SEQUENCE order_id_seq RESTART WITH 10001;

-- Indexes to optimize join lookups
CREATE INDEX IF NOT EXISTS idx_order_product_order_id
  ON order_product(order_id);

CREATE INDEX IF NOT EXISTS idx_order_product_product_id
  ON order_product(product_id);

-- Helpful indexes for latency-sensitive reads
-- (functional index for case-insensitive search)
CREATE INDEX IF NOT EXISTS idx_order_customer_id
    ON "order"(customer_id);

--CREATE INDEX IF NOT EXISTS idx_customer_name_lower_expr
--  ON customer ((LOWER(name)));

CREATE EXTENSION IF NOT EXISTS pg_trgm;

CREATE INDEX IF NOT EXISTS idx_customer_name_trgm
  ON customer USING GIN (LOWER(name) gin_trgm_ops);
