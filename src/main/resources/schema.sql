-- ==========================================================================
-- Esquema do FakeERP (H2). Idempotente para preservar os dados em disco.
-- ==========================================================================

CREATE TABLE IF NOT EXISTS tbl_users (
    id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role     VARCHAR(50)  NOT NULL DEFAULT 'ROLE_USER'
);

CREATE TABLE IF NOT EXISTS tbl_orders (
    order_id        BIGINT PRIMARY KEY,
    order_date_time TIMESTAMP      NOT NULL,
    value           DECIMAL(15, 2) NOT NULL,
    discount        DECIMAL(15, 2) NOT NULL,
    total           DECIMAL(15, 2) NOT NULL,
    status          VARCHAR(50)    NOT NULL
);
