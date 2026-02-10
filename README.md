````md
# Cultural Events API

API RESTful para gerenciamento de **eventos culturais**, **locais (venues)**, **sessões** e **reservas**, com **autenticação JWT**, **controle de acesso por perfil**, **cache**, **testes unitários e de integração**.

---

## Tema e justificativa

**Tema:** Plataforma de Eventos Culturais

**Justificativa:**  
A aplicação facilita o cadastro, a divulgação e o gerenciamento de eventos culturais, permitindo organizar sessões por local e controlar reservas de forma segura. O projeto aplica conceitos de sistemas corporativos como arquitetura em camadas, regras de negócio centralizadas, validação de dados, segurança, testes automatizados, dockerização e documentação.

---

## Tecnologias utilizadas

- Java 21
- Spring Boot 3
- Spring Web
- Spring Data JPA
- Bean Validation
- Spring Security + JWT
- PostgreSQL
- Swagger / OpenAPI (Springdoc)
- Cache (Spring Cache)
- Testes:
  - JUnit 5
  - Mockito
  - Spring Boot Test
  - MockMvc
- JaCoCo (cobertura de testes)
- Docker

---

## Arquitetura em camadas

- **model** – Entidades JPA
- **repository** – Persistência com Spring Data JPA
- **service** – Regras de negócio
- **controller** – Endpoints REST
- **security** – Autenticação e autorização JWT

---

## Regras de negócio principais

- Reservas só podem ser criadas se houver assentos disponíveis.
- Capacidade é definida pelo venue da sessão.
- Cancelamento de reserva altera o status para `CANCELED`.
- Tickets podem ser consultados via código UUID.
- Operações administrativas exigem autenticação e perfil adequado.

---

## Segurança (JWT)

- Autenticação baseada em **JWT**
- Token retornado no login e registro
- Perfis disponíveis:
  - `ADMIN`
  - `ORGANIZER`
  - `USER`

### Regras de acesso (resumo)

- Rotas públicas:
  - `/api/v1/auth/**`
  - GET de eventos, venues e sessions
- Rotas protegidas:
  - CRUD de eventos, venues e sessions → `ADMIN` ou `ORGANIZER`
  - Reservas → usuário autenticado

---

## Cache

Cache aplicado apenas em **GETs importantes**, evitando impacto em escrita:

- Events:
  - `GET /api/v1/events`
  - `GET /api/v1/events/{id}`
- Venues:
  - `GET /api/v1/venues`
  - `GET /api/v1/venues/{id}`
- Sessions:
  - `GET /api/v1/sessions`
  - `GET /api/v1/sessions/{id}`
  - `GET /api/v1/sessions?eventId=`

Caches são invalidados automaticamente em operações de escrita (POST, PUT, DELETE).

---

## Como executar o projeto (local)

### Pré-requisitos

- Java 21
- Maven
- Docker (opcional, para banco)

### Subir o banco com Docker

```bash
docker compose up -d
````

> Caso a porta 5432 esteja ocupada, ajuste no `docker-compose.yml`.

---

### Configuração (`application.yml`)

```yml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/culturalevents
    username: postgres
    password: postgres
    driver-class-name: org.postgresql.Driver
  jpa:
    hibernate:
      ddl-auto: update
    open-in-view: false

security:
  jwt:
    secret: ${SECURITY_JWT_SECRET:kdkkefnknsrftudmiDWNLSOSIUBEMVSYKFMIIUD}
    expiration-minutes: ${SECURITY_JWT_EXPIRATION_MINUTES:120}

springdoc:
  swagger-ui:
    path: /swagger
```

---

### Rodar a aplicação

```bash
mvn spring-boot:run
```

---

## Swagger

* UI: `http://localhost:8080/swagger`
* OpenAPI JSON: `http://localhost:8080/v3/api-docs`

---

## Testes

### Executar testes

```bash
mvn test
```

### Testes de integração

* Localizados em: `src/test/java/.../integration`
* Executados automaticamente com `mvn test`
* Utilizam **MockMvc** e contexto real da aplicação

### Testes unitários

* Localizados em: `src/test/java/.../service`
* Utilizam **Mockito** para isolamento da camada de serviço

---

## Cobertura de testes (JaCoCo)

Após rodar:

```bash
mvn test
```

Relatório HTML:

```text
target/site/jacoco/index.html
```

Abrir no Linux:

```bash
xdg-open target/site/jacoco/index.html
```

---

## Endpoints

### Auth

| Método | Rota                    | Auth | Descrição                   |
| ------ | ----------------------- | ---- | --------------------------- |
| POST   | `/api/v1/auth/register` | Não  | Cadastro e retorno de token |
| POST   | `/api/v1/auth/login`    | Não  | Login e retorno de token    |

### Events

| Método | Rota                  | Auth | Perfil          | Descrição       |
| ------ | --------------------- | ---- | --------------- | --------------- |
| POST   | `/api/v1/events`      | Sim  | ADMIN/ORGANIZER | Cria evento     |
| GET    | `/api/v1/events`      | Não  | Público         | Lista eventos   |
| GET    | `/api/v1/events/{id}` | Não  | Público         | Detalha evento  |
| PUT    | `/api/v1/events/{id}` | Sim  | ADMIN/ORGANIZER | Atualiza evento |
| DELETE | `/api/v1/events/{id}` | Sim  | ADMIN/ORGANIZER | Remove evento   |

### Venues

| Método | Rota                  | Auth | Perfil          | Descrição      |
| ------ | --------------------- | ---- | --------------- | -------------- |
| POST   | `/api/v1/venues`      | Sim  | ADMIN/ORGANIZER | Cria venue     |
| GET    | `/api/v1/venues`      | Não  | Público         | Lista venues   |
| GET    | `/api/v1/venues/{id}` | Não  | Público         | Detalha venue  |
| PUT    | `/api/v1/venues/{id}` | Sim  | ADMIN/ORGANIZER | Atualiza venue |
| DELETE | `/api/v1/venues/{id}` | Sim  | ADMIN/ORGANIZER | Remove venue   |

### Sessions

| Método | Rota                    | Auth | Perfil          | Descrição       |
| ------ | ----------------------- | ---- | --------------- | --------------- |
| POST   | `/api/v1/sessions`      | Sim  | ADMIN/ORGANIZER | Cria sessão     |
| GET    | `/api/v1/sessions`      | Não  | Público         | Lista sessões   |
| GET    | `/api/v1/sessions/{id}` | Não  | Público         | Detalha sessão  |
| PUT    | `/api/v1/sessions/{id}` | Sim  | ADMIN/ORGANIZER | Atualiza sessão |
| DELETE | `/api/v1/sessions/{id}` | Sim  | ADMIN/ORGANIZER | Remove sessão   |

### Reservations

| Método | Rota                                            | Auth | Perfil      | Descrição                   |
| ------ | ----------------------------------------------- | ---- | ----------- | --------------------------- |
| POST   | `/api/v1/reservations`                          | Sim  | Autenticado | Cria reserva                |
| GET    | `/api/v1/reservations`                          | Sim  | Autenticado | Lista reservas              |
| GET    | `/api/v1/reservations/{id}`                     | Sim  | Autenticado | Detalha reserva             |
| POST   | `/api/v1/reservations/{id}/cancel`              | Sim  | Autenticado | Cancela reserva             |
| GET    | `/api/v1/reservations/ticket/{code}`            | Sim  | Autenticado | Consulta ticket por UUID    |
| GET    | `/api/v1/reservations/availability/{sessionId}` | Sim  | Autenticado | Disponibilidade de assentos |

---

## Estrutura de pastas

```
src/main/java
 ├── model
 ├── repository
 ├── service
 ├── controller
 └── security

src/test/java
 ├── service        (testes unitários)
 └── integration    (testes de integração)

src/test/resources
 └── application-test.yml
```

---

## Docker (PostgreSQL)

```yml
services:
  db:
    image: postgres:16
    container_name: culturalevents-db
    environment:
      POSTGRES_DB: culturalevents
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
    ports:
      - "5432:5432"
    volumes:
      - pgdata:/var/lib/postgresql/data

volumes:
  pgdata:
```

---

## CI/CD

Pipeline prevista com GitHub Actions para:

* Build do projeto
* Execução dos testes
* Relatório de cobertura (JaCoCo)

```
```
