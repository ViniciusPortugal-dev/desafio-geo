# Execução com Docker Compose e Endpoints (cURLs + bodies para Swagger)

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

---
## 🧪 4) Fluxo de Teste e Execução

1. **Crie usuários** no Service A.
2. **Crie entregadores** no Service A.
3. **Crie pedidos**, utilizando:
    - `idDelivery` com o **ID do entregador** criado;
    - `externalUserId` com o **externalId** retornado ao criar o usuário.
4. Após criar os pedidos, você pode:
    - Adicionar novos registros;
    - Atualizar, apagar e listar dados;
    - Verificar a **replicação automática** desses dados no Service B;
    - **Exportar** os pedidos em CSV pelo Service C.

---
## 📖 5) Swagger
- A: <http://localhost:8081/swagger-ui.html>
- B: <http://localhost:8082/swagger-ui.html>
- C: <http://localhost:8083/swagger-ui.html>

Clique em **Authorize** e informe `Bearer authenticate-key`.

> 💡 **No Swagger**, os endpoints de **atualização (PUT)** e **exclusão (DELETE)** de **Usuários** e **Pedidos** agora utilizam **`externalId` como *path variable*** no **Service A**.  
> 💡 Cole os **bodies prontos** (abaixo) diretamente no campo “Request body” do Swagger.

---
## 🧾 6) Bodies prontos para Swagger (copiar e colar)

### 🔹 Service A (`http://localhost:8081`)

#### 🧑‍💼 Usuários
- **POST /usuarios**
```json
{
  "name": "Ana",
  "email": "ana@example.com"
}
```
- **PUT /usuarios/{externalId}**
```json
{
  "name": "Ana Maria",
  "email": "ana.maria@example.com"
}
```

#### 🚚 Entregadores
- **POST /entregadores**
```json
{
  "name": "Carlos",
  "phone": "+55 71 99999-0000"
}
```
- **PUT /entregadores/{id}** *(se aplicável no seu contrato; caso utilize externalId, ajuste o path no Swagger)*
```json
{
  "name": "Carlos Souza",
  "phone": "+55 71 98888-0000"
}
```

#### 📦 Pedidos
- **POST /pedidos**
```json
{
  "description": "Pedido 1",
  "value": 123.45,
  "externalUserId": 1,
  "idDelivery": 1
}
```
- **PUT /pedidos/{externalId}**
```json
{
  "description": "Pedido 1 atualizado",
  "value": 150.0,
  "externalUserId": 1,
  "idDelivery": 1
}
```



```

```

---
## 🧩 7) cURLs (Bash e PowerShell)

> **Headers obrigatórios:** `-H "Authorization: Bearer authenticate-key"` e, quando houver body, `-H "Content-Type: application/json"`  
> **Windows (PowerShell):** use **`curl.exe`** e **aspas simples** no `-d '...'`

### 🔹 Service A (`http://localhost:8081`)

#### 🧑‍💼 Usuários
**Criar (POST /usuarios)**  
**Bash**
```bash
curl -X POST "http://localhost:8081/usuarios" \
  -H "Authorization: Bearer authenticate-key" \
  -H "Content-Type: application/json" \
  -d '{ "name":"Ana", "email":"ana@example.com" }'
```
**PowerShell**
```powershell
curl.exe -X POST "http://localhost:8081/usuarios" ^
  -H "Authorization: Bearer authenticate-key" ^
  -H "Content-Type: application/json" ^
  -d '{ "name":"Ana", "email":"ana@example.com" }'
```

**Listar (GET /usuarios)**  
**Bash**
```bash
curl "http://localhost:8081/usuarios" -H "Authorization: Bearer authenticate-key"
```
**PowerShell**
```powershell
curl.exe "http://localhost:8081/usuarios" ^
  -H "Authorization: Bearer authenticate-key"
```

**Atualizar (PUT /usuarios/{externalId})**  
**Bash**
```bash
curl -X PUT "http://localhost:8081/usuarios/<EXTERNAL_ID>" \
  -H "Authorization: Bearer authenticate-key" \
  -H "Content-Type: application/json" \
  -d '{ "name":"Ana Maria", "email":"ana.maria@example.com" }'
```
**PowerShell**
```powershell
curl.exe -X PUT "http://localhost:8081/usuarios/<EXTERNAL_ID>" ^
  -H "Authorization: Bearer authenticate-key" ^
  -H "Content-Type: application/json" ^
  -d '{ "name":"Ana Maria", "email":"ana.maria@example.com" }'
```

**Deletar (DELETE /usuarios/{externalId})**  
**Bash**
```bash
curl -X DELETE "http://localhost:8081/usuarios/<EXTERNAL_ID>" \
  -H "Authorization: Bearer authenticate-key"
```
**PowerShell**
```powershell
curl.exe -X DELETE "http://localhost:8081/usuarios/<EXTERNAL_ID>" ^
  -H "Authorization: Bearer authenticate-key"
```

---

#### 🚚 Entregadores
**Criar (POST /entregadores)**  
**Bash**
```bash
curl -X POST "http://localhost:8081/entregadores" \
  -H "Authorization: Bearer authenticate-key" \
  -H "Content-Type: application/json" \
  -d '{ "name":"Carlos", "phone":"+55 71 99999-0000" }'
```
**PowerShell**
```powershell
curl.exe -X POST "http://localhost:8081/entregadores" ^
  -H "Authorization: Bearer authenticate-key" ^
  -H "Content-Type: application/json" ^
  -d '{ "name":"Carlos", "phone":"+55 71 99999-0000" }'
```

**Atualizar (PUT /entregadores/{id ou externalId})** *(ajuste conforme seu contrato)*  
**Bash**
```bash
curl -X PUT "http://localhost:8081/entregadores/1" \
  -H "Authorization: Bearer authenticate-key" \
  -H "Content-Type: application/json" \
  -d '{ "name":"Carlos Souza", "phone":"+55 71 98888-0000" }'
```
**PowerShell**
```powershell
curl.exe -X PUT "http://localhost:8081/entregadores/1" ^
  -H "Authorization: Bearer authenticate-key" ^
  -H "Content-Type: application/json" ^
  -d '{ "name":"Carlos Souza", "phone":"+55 71 98888-0000" }'
```

**Listar (GET /entregadores)**  
**Bash**
```bash
curl "http://localhost:8081/entregadores" -H "Authorization: Bearer authenticate-key"
```
**PowerShell**
```powershell
curl.exe "http://localhost:8081/entregadores" ^
  -H "Authorization: Bearer authenticate-key"
```

**Deletar (DELETE /entregadores/{id ou externalId})** *(se aplicável)*  
**Bash**
```bash
curl -X DELETE "http://localhost:8081/entregadores/1" \
  -H "Authorization: Bearer authenticate-key"
```
**PowerShell**
```powershell
curl.exe -X DELETE "http://localhost:8081/entregadores/1" ^
  -H "Authorization: Bearer authenticate-key"
```

---

#### 📦 Pedidos
**Criar (POST /pedidos)**  
**Bash**
```bash
curl -X POST "http://localhost:8081/pedidos" \
  -H "Authorization: Bearer authenticate-key" \
  -H "Content-Type: application/json" \
  -d '{ "description":"Pedido 1", "value":123.45, "externalUserId":1, "idDelivery":1 }'
```
**PowerShell**
```powershell
curl.exe -X POST "http://localhost:8081/pedidos" ^
  -H "Authorization: Bearer authenticate-key" ^
  -H "Content-Type: application/json" ^
  -d '{ "description":"Pedido 1", "value":123.45, "externalUserId":1, "idDelivery":1 }'
```

**Listar (GET /pedidos)**  
**Bash**
```bash
curl "http://localhost:8081/pedidos" -H "Authorization: Bearer authenticate-key"
```
**PowerShell**
```powershell
curl.exe "http://localhost:8081/pedidos" ^
  -H "Authorization: Bearer authenticate-key"
```

**Atualizar (PUT /pedidos/{externalId})**  
**Bash**
```bash
curl -X PUT "http://localhost:8081/pedidos/<EXTERNAL_ID>" \
  -H "Authorization: Bearer authenticate-key" \
  -H "Content-Type: application/json" \
  -d '{ "description":"Pedido 1 atualizado", "value":150.0, "externalUserId":1, "idDelivery":1 }'
```
**PowerShell**
```powershell
curl.exe -X PUT "http://localhost:8081/pedidos/<EXTERNAL_ID>" ^
  -H "Authorization: Bearer authenticate-key" ^
  -H "Content-Type: application/json" ^
  -d '{ "description":"Pedido 1 atualizado", "value":150.0, "externalUserId":1, "idDelivery":1 }'
```

**Deletar (DELETE /pedidos/{externalId})**  
**Bash**
```bash
curl -X DELETE "http://localhost:8081/pedidos/<EXTERNAL_ID>" \
  -H "Authorization: Bearer authenticate-key"
```
**PowerShell**
```powershell
curl.exe -X DELETE "http://localhost:8081/pedidos/<EXTERNAL_ID>" ^
  -H "Authorization: Bearer authenticate-key"
```

---
### 🔹 Service B (`http://localhost:8082`)

**Listar replicados (GET)**  
**Bash**
```bash
curl "http://localhost:8082/usuarios" -H "Authorization: Bearer authenticate-key"
curl "http://localhost:8082/pedidos"  -H "Authorization: Bearer authenticate-key"
```
**PowerShell**
```powershell
curl.exe "http://localhost:8082/usuarios" ^
  -H "Authorization: Bearer authenticate-key"
curl.exe "http://localhost:8082/pedidos" ^
  -H "Authorization: Bearer authenticate-key"
```

**Atualizar replicados (PUT /{externalId})**  
**Bash**
```bash
curl -X PUT "http://localhost:8082/usuarios/<EXTERNAL_ID>" \
  -H "Authorization: Bearer authenticate-key" \
  -H "Content-Type: application/json" \
  -d '{ "name":"Ana Maria", "email":"ana.maria@example.com" }'

curl -X PUT "http://localhost:8082/pedidos/<EXTERNAL_ID>" \
  -H "Authorization: Bearer authenticate-key" \
  -H "Content-Type: application/json" \
  -d '{ "description":"Pedido 1 atualizado", "value":150.0, "externalUserId":1, "idDelivery":1 }'
```
**PowerShell**
```powershell
curl.exe -X PUT "http://localhost:8082/usuarios/<EXTERNAL_ID>" ^
  -H "Authorization: Bearer authenticate-key" ^
  -H "Content-Type: application/json" ^
  -d '{ "name":"Ana Maria", "email":"ana.maria@example.com" }'

curl.exe -X PUT "http://localhost:8082/pedidos/<EXTERNAL_ID>" ^
  -H "Authorization: Bearer authenticate-key" ^
  -H "Content-Type: application/json" ^
  -d '{ "description":"Pedido 1 atualizado", "value":150.0, "externalUserId":1, "idDelivery":1 }'
```

**Deletar replicados (DELETE /{externalId})**  
**Bash**
```bash
curl -X DELETE "http://localhost:8082/usuarios/<EXTERNAL_ID>" -H "Authorization: Bearer authenticate-key"
curl -X DELETE "http://localhost:8082/pedidos/<EXTERNAL_ID>"  -H "Authorization: Bearer authenticate-key"
```
**PowerShell**
```powershell
curl.exe -X DELETE "http://localhost:8082/usuarios/<EXTERNAL_ID>" ^
  -H "Authorization: Bearer authenticate-key"
curl.exe -X DELETE "http://localhost:8082/pedidos/<EXTERNAL_ID>" ^
  -H "Authorization: Bearer authenticate-key"
```

---
## 🧪 8) Caso de Teste 5.1 – Exceção 422 após 3 POSTs
Após **três criações bem-sucedidas** em Service A, a próxima tentativa deve retornar **HTTP 422**.

**Objetivo:** validar o tratamento de erro entre A ↔ B.  
**Pré-condições:** stack ativa e token configurado.

**Bash**
```bash
curl -X POST "http://localhost:8081/usuarios" -H "Authorization: Bearer authenticate-key" -H "Content-Type: application/json" -d '{ "name":"u1", "email":"u1@example.com" }'
curl -X POST "http://localhost:8081/usuarios" -H "Authorization: Bearer authenticate-key" -H "Content-Type: application/json" -d '{ "name":"u2", "email":"u2@example.com" }'
curl -X POST "http://localhost:8081/usuarios" -H "Authorization: Bearer authenticate-key" -H "Content-Type: application/json" -d '{ "name":"u3", "email":"u3@example.com" }'
curl -i -X POST "http://localhost:8081/usuarios" -H "Authorization: Bearer authenticate-key" -H "Content-Type: application/json" -d '{ "name":"u4", "email":"u4@example.com" }'
```
**PowerShell**
```powershell
curl.exe -X POST "http://localhost:8081/usuarios" ^ -H "Authorization: Bearer authenticate-key" ^ -H "Content-Type: application/json" ^ -d '{ "name":"u1", "email":"u1@example.com" }'
curl.exe -X POST "http://localhost:8081/usuarios" ^ -H "Authorization: Bearer authenticate-key" ^ -H "Content-Type: application/json" ^ -d '{ "name":"u2", "email":"u2@example.com" }'
curl.exe -X POST "http://localhost:8081/usuarios" ^ -H "Authorization: Bearer authenticate-key" ^ -H "Content-Type: application/json" ^ -d '{ "name":"u3", "email":"u3@example.com" }'
curl.exe -i -X POST "http://localhost:8081/usuarios" ^ -H "Authorization: Bearer authenticate-key" ^ -H "Content-Type: application/json" ^ -d '{ "name":"u4", "email":"u4@example.com" }'
```

---
## 🔐 Nota sobre autenticação
- **Header de autenticação:** `Authorization: Bearer authenticate-key`
- **Chave do desafio:** `authenticate-key` (já definida em `APP_STATIC_TOKEN` no `.env`)
- Ao usar **PowerShell (Windows)**, prefira **`curl.exe`** e mantenha o corpo JSON em **aspas simples** no `-d`.
