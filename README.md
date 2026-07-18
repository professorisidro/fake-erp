# FakeERP

API REST em **Spring Boot** que simula um mini ERP: autenticação via **JWT** e um relatório de pedidos (`tbl_orders`) filtrado por ano e mês. Usa banco **H2** em arquivo, com schema e dados de exemplo carregados automaticamente na inicialização.

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

| login | senha | role       |
|-------|-------|------------|
| admin | admin | ROLE_ADMIN |
| user  | user  | ROLE_USER  |

## Autenticação

A API é stateless. Todos os endpoints exigem um token JWT no header `Authorization: Bearer <token>`, exceto o login e os endpoints do Swagger/H2 console, que são públicos.

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
  "expiresInMs": 3600000
}
```

**Response `401 Unauthorized`** — login ou senha inválidos.

---

### `GET /report/{year}/{month}`

🔒 Requer JWT. Retorna os pedidos de `tbl_orders` filtrados por ano e mês, com os totais consolidados.

**Path params**

| Parâmetro | Tipo | Descrição                  |
|-----------|------|-----------------------------|
| `year`    | int  | Ano do pedido (ex.: `2026`) |
| `month`   | int  | Mês do pedido, de `1` a `12`|

**Response `200 OK`**

```json
{
  "year": 2026,
  "month": 1,
  "count": 4,
  "totalValue": 3050.50,
  "totalDiscount": 195.50,
  "totalAmount": 2855.00,
  "orders": [
    {
      "orderId": 1001,
      "orderDateTime": "2026-01-05T09:30:00",
      "value": 1000.00,
      "discount": 50.00,
      "total": 950.00,
      "status": "PAID"
    }
  ]
}
```

**Response `400 Bad Request`** — mês fora do intervalo `1..12`.

**Response `401 Unauthorized`** — token ausente ou inválido.

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
