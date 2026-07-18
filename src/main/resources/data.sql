-- ==========================================================================
-- Carga inicial do FakeERP (H2). MERGE torna o script re-executável a cada
-- inicialização sem duplicar registros (os dados ficam em disco).
-- ==========================================================================

-- --------------------------------------------------------------------------
-- Usuários de login (senha no formato do DelegatingPasswordEncoder: {bcrypt})
--   admin / admin   -> ROLE_ADMIN
--   user  / user    -> ROLE_USER
-- --------------------------------------------------------------------------
MERGE INTO tbl_users (id, username, password, role) KEY (username) VALUES
    (1, 'admin', '{bcrypt}$2a$10$pxgPjE.T2ot/h6JINKxrSeNbyYfKijPuCnd9f3AFZD/gH9glcx8jC', 'ROLE_ADMIN'),
    (2, 'user',  '{bcrypt}$2a$10$pmegM4Aech0QsMAdaTBlWeAXvTKo6msBcGimcQE81fDDXmzYrGme2', 'ROLE_USER');

-- --------------------------------------------------------------------------
-- Pedidos (tbl_orders) - dados de exemplo em meses distintos
-- --------------------------------------------------------------------------
MERGE INTO tbl_orders (order_id, order_date_time, value, discount, total, status) KEY (order_id) VALUES
    -- Janeiro/2026
    (1001, TIMESTAMP '2026-01-05 09:30:00', 1000.00,  50.00,  950.00, 'PAID'),
    (1002, TIMESTAMP '2026-01-12 14:10:00',  480.00,   0.00,  480.00, 'PAID'),
    (1003, TIMESTAMP '2026-01-20 18:45:00',  250.00,  25.00,  225.00, 'CANCELLED'),
    (1004, TIMESTAMP '2026-01-28 11:00:00', 1320.50, 120.50, 1200.00, 'PENDING'),
    -- Fevereiro/2026
    (1005, TIMESTAMP '2026-02-03 08:15:00',  760.00,  60.00,  700.00, 'PAID'),
    (1006, TIMESTAMP '2026-02-15 16:20:00',  199.90,   0.00,  199.90, 'PAID'),
    (1007, TIMESTAMP '2026-02-27 13:05:00', 2500.00, 250.00, 2250.00, 'PAID'),
    -- Março/2026
    (1008, TIMESTAMP '2026-03-08 10:40:00',  540.00,  40.00,  500.00, 'PENDING'),
    (1009, TIMESTAMP '2026-03-19 19:55:00',  880.00,  80.00,  800.00, 'PAID'),
    (1010, TIMESTAMP '2026-03-30 12:25:00', 1500.00,   0.00, 1500.00, 'CANCELLED'),
    -- Julho/2026
    (1011, TIMESTAMP '2026-07-04 09:00:00',  320.00,  20.00,  300.00, 'PAID'),
    (1012, TIMESTAMP '2026-07-16 15:30:00', 4100.00, 100.00, 4000.00, 'PAID');
