# Execução com Docker Compose e Endpoints (com exemplos cURL)

Este projeto já inclui um arquivo **`.env`** na raiz com o token estático do desafio para facilitar a execução.

---
## 🧰 1) Pré-requisitos
- Docker Desktop (Compose v2)
- Java (21)
- Docker e Docker Compose
- Maven 3.9+
- Portas livres: 8081, 8082, 8083, 5433, 5434, 5435

---
## ⚙️ 2) Arquivo `.env` (já presente na raiz)
```dotenv
APP_STATIC_TOKEN=authenticate-key
SERVICE_A_URL=http://service-a:8081
SERVICE_B_URL=http://service-b:8082
```

> ⚠️ Não coloque segredos reais aqui. Este `.env` é apenas para o desafio.

---
## 🚀 3) Subir os serviços
```bash
docker compose up -d --build
```

## 📖 4) Swagger
- A: http://localhost:8081/swagger-ui.html
- B: http://localhost:8082/swagger-ui.html
- C: http://localhost:8083/swagger-ui.html

Clique em **Authorize** e informe `Bearer authenticate-key`.

---
## 🧩 5) Endpoints e exemplos cURL

### 🔹 Service A (http://localhost:8081)
Responsável por criar e replicar dados para o Service B.

#### 🧑‍💼 Usuários
```bash
# Criar
curl -X POST http://localhost:8081/usuarios   -H "Authorization: Bearer authenticate-key"   -H "Content-Type: application/json"   -d '{"name":"Ana","email":"ana@example.com"}'

# Listar
curl -H "Authorization: Bearer authenticate-key" http://localhost:8081/usuarios

# Atualizar
curl -X PUT http://localhost:8081/usuarios   -H "Authorization: Bearer authenticate-key"   -H "Content-Type: application/json"   -d '{"id":1,"name":"Ana Maria","email":"ana.maria@example.com"}'

# Deletar
curl -X DELETE http://localhost:8081/usuarios/1   -H "Authorization: Bearer authenticate-key"
```

#### 🚚 Entregadores
```bash
# Criar
curl -X POST http://localhost:8081/entregadores   -H "Authorization: Bearer authenticate-key"   -H "Content-Type: application/json"   -d '{"name":"Carlos","phone":"+55 71 99999-0000"}'

# Listar
curl -H "Authorization: Bearer authenticate-key" http://localhost:8081/entregadores

# Atualizar
curl -X PUT http://localhost:8081/entregadores   -H "Authorization: Bearer authenticate-key"   -H "Content-Type: application/json"   -d '{"id":1,"name":"Carlos Souza","phone":"+55 71 98888-0000"}'

# Deletar
curl -X DELETE http://localhost:8081/entregadores/1   -H "Authorization: Bearer authenticate-key"
```

#### 📦 Pedidos
```bash
# Criar
curl -X POST http://localhost:8081/pedidos   -H "Authorization: Bearer authenticate-key"   -H "Content-Type: application/json"   -d '{"description":"Pedido 1","value":123.45,"externalUserId":1,"idDelivery":1}'

# Listar
curl -H "Authorization: Bearer authenticate-key" http://localhost:8081/pedidos

# Atualizar
curl -X PUT http://localhost:8081/pedidos   -H "Authorization: Bearer authenticate-key"   -H "Content-Type: application/json"   -d '{"id":1,"description":"Pedido 1 atualizado","value":150.00,"externalUserId":1,"idDelivery":1}'

# Deletar
curl -X DELETE http://localhost:8081/pedidos/1   -H "Authorization: Bearer authenticate-key"
```



---
### 🔹 Service C (http://localhost:8083)
Exporta pedidos do Service B em formato CSV.

```bash
curl -H "Authorization: Bearer authenticate-key"   -OJ http://localhost:8083/export/pedidos
# Gera: pedidos-YYYYMMDD-HHmmss.csv
```

---
## 🧪 6) Caso de Teste 5.1 – Exceção 422 após 3 POSTs
Após três criações bem-sucedidas em Service A, a próxima tentativa deve retornar **HTTP 422**.

**Objetivo:** validar o tratamento de erro entre A ↔ B.  
**Pré-condições:** stack ativa e token configurado.

```bash
# 1
curl -X POST http://localhost:8081/usuarios   -H "Authorization: Bearer authenticate-key"   -H "Content-Type: application/json"   -d '{"name":"u1","email":"u1@example.com"}'
# 2
curl -X POST http://localhost:8081/usuarios   -H "Authorization: Bearer authenticate-key"   -H "Content-Type: application/json"   -d '{"name":"u2","email":"u2@example.com"}'
# 3
curl -X POST http://localhost:8081/usuarios   -H "Authorization: Bearer authenticate-key"   -H "Content-Type: application/json"   -d '{"name":"u3","email":"u3@example.com"}'
# 4 → deve retornar 422
curl -i -X POST http://localhost:8081/usuarios   -H "Authorization: Bearer authenticate-key"   -H "Content-Type: application/json"   -d '{"name":"u4","email":"u4@example.com"}'
```
