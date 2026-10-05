# FakeERP

API REST em **Spring Boot** que simula um mini ERP: autenticação via **JWT com escopos**, um relatório de pedidos (`tbl_orders`) filtrado por ano e mês e os endpoints do **squad de agentes de crédito PJ** (cadastro de empresas, política de crédito e decisões com aprovação humana). Usa banco **H2** em arquivo, com schema e dados de exemplo carregados automaticamente na inicialização.

## Stack

- Java 25
- Spring Boot 4.1.0 (Web MVC, Security, Data JPA)
- H2 Database (arquivo, `./data/fakeerp.mv.db`)
- JWT (JJWT 0.13.0)
- springdoc-openapi (Swagger UI)

## Como executar

```bash
./mvnw spring-boot:run
```

A aplicação sobe em `http://localhost:8080`.

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
- Console H2: `http://localhost:8080/h2-console`
  JDBC URL: `jdbc:h2:file:./data/fakeerp` — usuário: `admin` — senha: `admin`

### Usuários de teste

Carregados por `data.sql`:

| login               | senha         | role       | escopos                                                  |
|---------------------|---------------|------------|----------------------------------------------------------|
| `admin`             | `admin`       | ROLE_ADMIN | `report:read policy:read credit:write credit:approve`    |
| `user`              | `user`        | ROLE_USER  | `report:read`                                            |
| `agent-analista`    | `analista`    | ROLE_AGENT | `report:read`                                            |
| `agent-compliance`  | `compliance`  | ROLE_AGENT | `policy:read`                                            |
| `agent-coordenador` | `coordenador` | ROLE_AGENT | `credit:write`                                           |

## Autenticação e escopos

A API é stateless. Todos os endpoints exigem um token JWT no header `Authorization: Bearer <token>`, exceto o login e os endpoints do Swagger/H2 console, que são públicos.

O token carrega a claim `scope` com os escopos do usuário (coluna `scopes` de `tbl_users`). Cada endpoint de negócio exige um escopo específico — controle de acesso **por operação**, na própria API, independente do que o prompt do agente diga:

| Escopo           | Endpoints                                    | Quem recebe          |
|------------------|----------------------------------------------|----------------------|
| `report:read`    | `GET /report/{year}/{month}`, `GET /company/{cnpj}` | Agente Analista |
| `policy:read`    | `GET /credit-policy`                          | Agente Compliance    |
| `credit:write`   | `POST /credit-decision`                       | Agente Coordenador   |
| `credit:approve` | `PATCH /credit-decision/{id}/approve`         | Só humano (`admin`)  |

- Token ausente, inválido ou expirado → **`401 Unauthorized`**
- Token válido sem o escopo exigido → **`403 Forbidden`**

Todos os erros seguem o formato ProblemDetail (RFC 9457):

```json
{
  "type": "about:blank",
  "title": "Forbidden",
  "status": 403,
  "detail": "O token não possui o escopo necessário para esta operação",
  "instance": "/company/11111111000191"
}
```

## Endpoints

### `POST /auth/login`

Autentica o usuário e retorna um token JWT.

**Request body**

```json
{
  "login": "admin",
  "password": "admin"
}
```

**Response `200 OK`**

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "type": "Bearer",
  "expiresInMs": 3600000,
  "scope": "report:read policy:read credit:write credit:approve"
}
```

**Response `401 Unauthorized`** — login ou senha inválidos.

---

### `GET /report/{year}/{month}`

🔒 Requer o escopo `report:read`. Retorna os pedidos de `tbl_orders` filtrados por ano e mês — e, opcionalmente, pelo CNPJ da empresa que faturou — com os totais consolidados.

**Path params**

| Parâmetro | Tipo | Descrição                  |
|-----------|------|-----------------------------|
| `year`    | int  | Ano do pedido (ex.: `2026`) |
| `month`   | int  | Mês do pedido, de `1` a `12`|

**Query params**

| Parâmetro | Tipo   | Descrição                                                                 |
|-----------|--------|---------------------------------------------------------------------------|
| `cnpj`    | string | Opcional. CNPJ (14 dígitos, sem máscara) da empresa que faturou. Sem ele, o relatório inclui todas as empresas |

**Response `200 OK`**

```json
{
  "year": 2026,
  "month": 1,
  "cnpj": "11111111000191",
  "count": 4,
  "totalValue": 3050.50,
  "totalDiscount": 195.50,
  "totalAmount": 2855.00,
  "orders": [
    {
      "orderId": 1001,
      "cnpj": "11111111000191",
      "orderDateTime": "2026-01-05T09:30:00",
      "value": 1000.00,
      "discount": 50.00,
      "total": 950.00,
      "status": "PAID"
    }
  ]
}
```

`cnpj` vem `null` quando o relatório não é filtrado.

**Response `400 Bad Request`** — mês fora do intervalo `1..12` ou CNPJ fora do formato.

**Response `404 Not Found`** — CNPJ não cadastrado.

**Response `401 Unauthorized`** — token ausente ou inválido. **`403 Forbidden`** — token sem `report:read`.

---

## Squad de crédito PJ

Três agentes, cada um com um escopo: o **Analista** levanta dados (`/company` + `/report`), o **Compliance** valida contra a política (`/credit-policy`) e o **Coordenador** grava a decisão (`POST /credit-decision`). A aprovação definitiva é feita por um humano (`PATCH /credit-decision/{id}/approve`).

### Empresas de exemplo (`tbl_company`)

| CNPJ             | Razão social       | Segmento  | Faturamento declarado | Cenário         |
|------------------|--------------------|-----------|-----------------------|-----------------|
| `11111111000191` | Comercio Fake Ltda | varejo    | R$ 180.000/mês        | Saudável        |
| `22222222000172` | Servicos Fake ME   | servicos  | R$ 60.000/mês         | No limite       |
| `33333333000153` | Industria Fake SA  | industria | R$ 120.000/mês        | Divergente      |

Pedidos de mai–set/2026 de cada empresa (`GET /report/{year}/{month}?cnpj=...`), desenhados contra a política `2026.1` (mín. R$ 50.000/mês, mín. 10 pedidos em 3 meses, desconto máx. 15%):

| CNPJ             | Faturado/mês (total)      | Pedidos/mês | Desconto  | O que o squad deve perceber                          |
|------------------|---------------------------|-------------|-----------|------------------------------------------------------|
| `11111111000191` | R$ 176–185 mil            | 6           | 2–7%      | Bate com o declarado; dentro da política             |
| `22222222000172` | R$ 52–56 mil (jul: 48,7 mil) | 4        | 11–14,5%  | No limite: o resultado depende do mês e do critério  |
| `33333333000153` | R$ 34–40 mil              | 3 (1 cancelado) | 16–22% | Declara 3x o que fatura; desconto e volume fora da política |

Os pedidos originais (1001–1012, jan–jul/2026) pertencem à `11111111000191`.

---

### `GET /company/{cnpj}`

🔒 Requer o escopo `report:read`. Tag **Cadastro**. Retorna os dados cadastrais da empresa. `declaredMonthlyRevenue` é o faturamento *declarado* — para o agente confrontar com o relatório de pedidos.

| Parâmetro | Tipo   | Descrição                         |
|-----------|--------|-----------------------------------|
| `cnpj`    | string | 14 dígitos, sem máscara           |

**Response `200 OK`**

```json
{
  "cnpj": "11111111000191",
  "corporateName": "Comercio Fake Ltda",
  "tradeName": "Fake Comercio",
  "segment": "varejo",
  "foundedAt": "2015-03-10",
  "declaredMonthlyRevenue": 180000.00
}
```

**`400`** — CNPJ fora do formato. **`404`** — CNPJ não cadastrado.

---

### `GET /credit-policy`

🔒 Requer o escopo `policy:read`. Tag **Compliance**. Retorna a política de crédito vigente. Somente leitura (a política é carga fixa do `data.sql`).

| Query param | Tipo   | Descrição                                                                 |
|-------------|--------|---------------------------------------------------------------------------|
| `segment`   | string | Opcional. Sem política específica para o segmento, retorna a política `geral` |

**Response `200 OK`**

```json
{
  "policyVersion": "2026.1",
  "segment": "geral",
  "minMonthsActive": 12,
  "minMonthlyRevenue": 50000.00,
  "maxDiscountRateAllowed": 0.15,
  "minOrdersCountLast3Months": 10,
  "maxRequestedAmount": 300000.00,
  "updatedAt": "2026-01-05T00:00:00Z"
}
```

`policyVersion` deve ser informado na decisão gravada — para a auditoria saber contra qual regra a proposta foi avaliada.

---

### `POST /credit-decision`

🔒 Requer o escopo `credit:write`. Tag **Compliance**. Grava a decisão do squad. É o único endpoint de escrita dos agentes.

**Request body**

```json
{
  "cnpj": "11111111000191",
  "referenceYear": 2026,
  "referenceMonth": 7,
  "requestedAmount": 120000.00,
  "proposedScore": 0.78,
  "decision": "PENDING_REVIEW",
  "policyVersion": "2026.1",
  "analystAgentId": "agent-analista-v1",
  "complianceAgentId": "agent-compliance-v1",
  "justification": "Faturamento consistente, dentro da política; requer revisão por valor acima de R$100k.",
  "supersedesId": null
}
```

**Response `201 Created`**

```json
{
  "id": 1001,
  "cnpj": "11111111000191",
  "referenceYear": 2026,
  "referenceMonth": 7,
  "revision": 0,
  "supersedesId": null,
  "requestedAmount": 120000.00,
  "proposedScore": 0.78,
  "policyVersion": "2026.1",
  "decision": "PENDING_REVIEW",
  "status": "PENDING_REVIEW",
  "createdBy": "agent-coordenador",
  "createdAt": "2026-09-11T14:32:00Z",
  "approvedBy": null,
  "approvedAt": null
}
```

**Guardrails no servidor**

- `decision = APPROVED` → **`422`**. O squad só grava `PENDING_REVIEW` ou `REJECTED`; aprovar exige `credit:approve`.
- Já existe decisão para o mesmo CNPJ/ano/mês → **`409`** (agente reprocessando a mesma proposta). A mensagem indica o `supersedesId` a usar para corrigir.
- Correção = novo registro com `supersedesId` (nova `revision`); a decisão anterior nunca é apagada. Só a revisão mais recente pode ser substituída.
- CNPJ não cadastrado ou `policyVersion` inexistente → **`422`**. Campos ausentes/fora do intervalo → **`400`** com a lista dos campos.
- `createdBy` é sempre o usuário do token. Não existe `DELETE`.

---

### `PATCH /credit-decision/{id}/approve`

🔒 Requer o escopo `credit:approve` — **nenhum agente recebe**, só o humano (`admin`). Human-in-the-loop como controle de acesso real, não como instrução de prompt.

**Request body**

```json
{
  "approvedBy": "isidro",
  "finalDecision": "APPROVED"
}
```

`finalDecision`: `APPROVED` ou `REJECTED`. Para auditoria, `approvedBy` é gravado com o **usuário autenticado** (subject do JWT); o valor do body é apenas informativo. `decision` mantém o que o squad gravou e `status` passa a ser a decisão final.

**`404`** — decisão não encontrada. **`409`** — decisão não está `PENDING_REVIEW` ou já foi substituída por outra revisão. **`422`** — `finalDecision` inválido.

## Exemplo de uso (curl)

```bash
# 1. Login
TOKEN=$(curl -s -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"login":"admin","password":"admin"}' | jq -r .token)

# 2. Relatório de janeiro/2026
curl -s http://localhost:8080/report/2026/1 \
  -H "Authorization: Bearer $TOKEN"
```

### Fluxo do squad de crédito

```bash
login() { curl -s -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d "{\"login\":\"$1\",\"password\":\"$2\"}" | jq -r .token; }

ANALISTA=$(login agent-analista analista)
COMPLIANCE=$(login agent-compliance compliance)
COORDENADOR=$(login agent-coordenador coordenador)
HUMANO=$(login admin admin)

# Analista: cadastro + relatório
curl -s http://localhost:8080/company/33333333000153 -H "Authorization: Bearer $ANALISTA"
curl -s "http://localhost:8080/report/2026/7?cnpj=33333333000153" -H "Authorization: Bearer $ANALISTA"

# Compliance: política vigente
curl -s "http://localhost:8080/credit-policy?segment=industria" -H "Authorization: Bearer $COMPLIANCE"

# Coordenador: grava a decisão como pendente de revisão humana
curl -s -X POST http://localhost:8080/credit-decision \
  -H "Authorization: Bearer $COORDENADOR" -H "Content-Type: application/json" \
  -d '{"cnpj":"33333333000153","referenceYear":2026,"referenceMonth":7,
       "requestedAmount":80000,"proposedScore":0.41,"decision":"PENDING_REVIEW",
       "policyVersion":"2026.1","analystAgentId":"agent-analista-v1",
       "complianceAgentId":"agent-compliance-v1",
       "justification":"Faturamento declarado divergente do relatório de pedidos."}'

# Humano: decisão final (os agentes recebem 403 aqui)
curl -s -X PATCH http://localhost:8080/credit-decision/1001/approve \
  -H "Authorization: Bearer $HUMANO" -H "Content-Type: application/json" \
  -d '{"approvedBy":"isidro","finalDecision":"REJECTED"}'
```

## Testes

```bash
./mvnw test
```

Os testes de integração (`CreditSquadApiTests`) usam H2 em memória (profile `test`) e não alteram `./data/fakeerp.mv.db`.
