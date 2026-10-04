-- ==========================================================================
-- Carga inicial do FakeERP (H2). MERGE torna o script re-executável a cada
-- inicialização sem duplicar registros (os dados ficam em disco).
-- ==========================================================================

-- --------------------------------------------------------------------------
-- Usuários de login (senha no formato do DelegatingPasswordEncoder: {bcrypt})
--   admin             / admin       -> ROLE_ADMIN, todos os escopos (único com credit:approve)
--   user              / user        -> ROLE_USER,  report:read
--   agent-analista    / analista    -> ROLE_AGENT, report:read
--   agent-compliance  / compliance  -> ROLE_AGENT, policy:read
--   agent-coordenador / coordenador -> ROLE_AGENT, credit:write
-- --------------------------------------------------------------------------
MERGE INTO tbl_users (id, username, password, role, scopes) KEY (username) VALUES
    (1, 'admin', '{bcrypt}$2a$10$pxgPjE.T2ot/h6JINKxrSeNbyYfKijPuCnd9f3AFZD/gH9glcx8jC', 'ROLE_ADMIN', 'report:read policy:read credit:write credit:approve'),
    (2, 'user',  '{bcrypt}$2a$10$pmegM4Aech0QsMAdaTBlWeAXvTKo6msBcGimcQE81fDDXmzYrGme2', 'ROLE_USER',  'report:read'),
    (3, 'agent-analista',    '{bcrypt}$2a$10$G0VQLEE6bFbuqIJDXCVQse/gp4h405gSoSo3j2vDMxkCNpmmXOT9C', 'ROLE_AGENT', 'report:read'),
    (4, 'agent-compliance',  '{bcrypt}$2a$10$CrqcWPmuh/ZE5HA5ybE01eZPExR/7hqFARn3ANtWFzjFvMYo5vdwa', 'ROLE_AGENT', 'policy:read'),
    (5, 'agent-coordenador', '{bcrypt}$2a$10$TruVXJvLhkf0Yj5Mvs4R3O6mIk.JFYhhfgOy.ysuoDVW9ANFxa9Q.', 'ROLE_AGENT', 'credit:write');

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

-- --------------------------------------------------------------------------
-- Empresas (tbl_company) - 3 cenários propositais do squad de crédito:
--   11111111000191 -> saudável
--   22222222000172 -> no limite da política
--   33333333000153 -> divergente (declara alto; deve ser criticado)
-- --------------------------------------------------------------------------
MERGE INTO tbl_company (cnpj, corporate_name, trade_name, segment, founded_at, declared_monthly_revenue) KEY (cnpj) VALUES
    ('11111111000191', 'Comercio Fake Ltda', 'Fake Comercio',  'varejo',    DATE '2015-03-10', 180000.00),
    ('22222222000172', 'Servicos Fake ME',   'Fake Servicos',  'servicos',  DATE '2021-06-01',  60000.00),
    ('33333333000153', 'Industria Fake SA',  'Fake Industria', 'industria', DATE '2018-11-20', 120000.00);

-- --------------------------------------------------------------------------
-- Política de crédito (tbl_credit_policy) - versão vigente, segmento geral
-- --------------------------------------------------------------------------
MERGE INTO tbl_credit_policy (policy_version, segment, min_months_active, min_monthly_revenue,
                              max_discount_rate_allowed, min_orders_count_last_3_months,
                              max_requested_amount, updated_at) KEY (policy_version, segment) VALUES
    ('2026.1', 'geral', 12, 50000.00, 0.1500, 10, 300000.00, TIMESTAMP WITH TIME ZONE '2026-01-05 00:00:00+00:00');
