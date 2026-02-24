# PicPaySimplified

API REST para gerenciamento de contas bancárias, usuários e transações. Desenvolvida com Spring Boot e autenticação via JWT.

Tecnologias utilizadas nesse projeto:
Spring Boot, Spring Security, JWT, Hibernate / JPA, Postgres, Gradle, Swagger, Validation, Docker

Requisitos e orientações para rodar a aplicação:
 Ter em sua máquina o Docker instalado. Clone o repositório em uma pasta com git clone https://github.com/HenriqueFerC/PicPaySimplified . Abra o terminal e rode : "docker-compose up --build". Após isso, o projeto estará rodando na sua máquina na porta 8080.

Documentação do SWAGGER/OpenAPI:
http://localhost:8080/swagger-ui/index.html#/
http://localhost:8080/v3/api-docs

Funcionalidades:
- Cadastro e autenticação de usuários
- Criação de contas bancárias
- Registro de transações entre usuários
- Consulta de transações
- Proteção de endpoints com JWT
- Documentação Swagger

## Principais Requisições

### Cadastro de Usuário 

`/user/register`.

JSON:
```json
{
  "fullName": "John Does",
  "cpfCnpj": "12345678900",
  "email": "johndoes@email.com",
  "password": "password123",
  "typeUser": "user"
}
```
______________________________________
### Login de Usuário

`/auth/login`

JSON:
```json
{
  "fullName": "John Does",
  "password": "password123",
}
```

______________________________________
### Busca de todos os Usuários

`/user/list`.

______________________________________
### Busca dos dados do usuário autenticado

`/user/myProfile`

______________________________________
### Atualização de Usuário autenticado

`/user/update`.

* **Requer Token.**

JSON:
```json
{
  "fullName": "John Doe",
  "cpfCnpj": "123.456.789-00 or 12.345.678/0001-00",
  "email": "johndoe@email.com",
  "password": "password123",
  "typeUser": "user"
}
```

______________________________________
### Cadastro de Conta Bancária para o Usuário autenticado

`/bankAccount/register`.

* **Requer Token.**

JSON:
```json
{
  "agency": 1234,
  "accountNumber": 567890,
  "balance": 1000
}
```
______________________________________
### Depósito de saldo para Conta Bancária

`/bankAccount/deposit`.

* **Requer Token.**
* **RequiredParam.**
______________________________________
### Saque de saldo para Conta Bancária

`/bankAccount/withdraw`.

* **Requer Token.**
* **RequestParam.**

--------------------------------------
### Sacar valor bancário

`/conta/valor-sacado?valor-sacado=?`

* **Requer Token.**
--------------------------------------
### Depositar valor bancário

`/conta/valor-depositado?valor-depositado=?`

* **Requer Token.**
______________________________________
### Realizar transação do usuário autenticado entre Contas.

`/transaction`.

* **Requer Token.**

JSON:
```json
{
  "value": 100,
  "idPayee": 1
}
```
______________________________________
### Busca de todas as transações do Usuário autenticado.

`/transaction/myTransactions`.

* **Requer Token.**

______________________________________
### Busca de todas as transações dentro de um período do Usuário autenticado.

`/transaction/myTransactions`.

* **Requer Token.**
* **RequestParam**

______________________________________
### Reverter Transação por ID da transação do Usuário autenticado.

`/transaction/revert/{id}`.

* **Requer Token.**

______________________________________

