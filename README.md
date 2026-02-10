Perfeito — o README já está **bem acima do nível esperado** 👏
Vou te devolver **ajustado**, com:

* linguagem um pouco mais acadêmica/corporativa (DSC curte isso)
* pequenos acertos de consistência (cache, perfis, CI/CD)
* sem mudar nada técnico do que você já fez

Pode **copiar e colar direto** no `README.md`.

---

````md
# Cultural Events API

API RESTful para gerenciamento de **eventos culturais**, **locais (venues)**, **sessões** e **reservas**, com **autenticação JWT**, **controle de acesso por perfil**, **testes automatizados**, **cache** e **pipeline CI**.

---

## Tema e justificativa

**Tema:** Plataforma de Eventos Culturais  

**Justificativa:**  
A aplicação facilita o cadastro, divulgação e organização de eventos culturais, permitindo a criação de sessões por local e o gerenciamento de reservas/ingressos.  
O projeto aplica conceitos de **sistemas corporativos**, como arquitetura em camadas, regras de negócio, validação de dados, segurança, testes automatizados, dockerização e documentação.

---

## Tecnologias

- Java 21
- Spring Boot 3.3.5
- Spring Web, Spring Data JPA, Bean Validation
- Spring Security + JWT
- PostgreSQL
- Swagger / OpenAPI (springdoc)
- Testes: JUnit 5, Mockito, Spring Boot Test, MockMvc
- JaCoCo (cobertura de testes)
- Cache: Spring Cache
- Docker
- GitHub Actions (CI)

---

## Arquitetura em camadas

- **model**: entidades JPA (Event, Venue, Session, Reservation, User)
- **repository**: acesso a dados via Spring Data JPA
- **service**: regras de negócio e validações
- **controller**: endpoints REST utilizando DTOs

---

## Regras de negócio principais

- Uma **reserva** só pode ser criada se houver assentos disponíveis  
  (capacidade do venue − reservas ativas).
- Cancelamento de reserva altera o status para **CANCELED**.
- Um ticket pode ser consultado por **UUID (code)**.
- Exclusões respeitam integridade referencial.

---

## Segurança (JWT) e perfis

Perfis (`Role`):
- `ADMIN`
- `ORGANIZER`
- `USER`

Regras principais:
- Endpoints de autenticação são públicos
- Swagger é público
- Endpoints GET de eventos, venues e sessões são públicos
- Criação de reservas exige autenticação
- Operações de criação, atualização e exclusão exigem `ADMIN` ou `ORGANIZER`

---

## Cache

Cache aplicado **apenas em endpoints GET estratégicos**, evitando complexidade desnecessária:

- Events: `list`, `get`
- Venues: `list`, `get`
- Sessions: `list`, `get`, `listByEvent`

Operações de escrita (`POST`, `PUT`, `DELETE`) realizam **evict** do cache relacionado.

---

## Como executar (Local)

### Pré-requisitos
- Java 21
- Maven
- Docker (opcional, para o banco)

### Subir PostgreSQL com Docker

```bash
docker compose up -d
````

> Caso a porta `5432` esteja ocupada, altere a porta no `docker-compose.yml`.

### Configuração (`application.yml`)

```yml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/culturalevents
    username: postgres
    password: postgres
  jpa:
    hibernate:
      ddl-auto: update
    open-in-view: false

security:
  jwt:
    secret: troque-isto-por-uma-string-bem-grande-aleatoria
    expiration-minutes: 120

springdoc:
  swagger-ui:
    path: /swagger
```

### Executar a aplicação

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

### Testes de Integração

Os testes de integração utilizam **MockMvc** e são executados junto com os testes unitários.

Eles podem ser identificados no log por classes com sufixo `*IT`.

---

## Cobertura de Testes (JaCoCo)

Após executar os testes:

```bash
mvn test
```

O relatório HTML fica disponível em:

```
target/site/jacoco/index.html
```

Para abrir no Linux:

```bash
xdg-open target/site/jacoco/index.html
```

---

## Endpoints

### Auth

| Método | Rota                    | Auth | Perfil | Descrição                    |
| ------ | ----------------------- | ---- | ------ | ---------------------------- |
| POST   | `/api/v1/auth/register` | Não  | -      | Cadastro de usuário          |
| POST   | `/api/v1/auth/login`    | Não  | -      | Login e geração de token JWT |

### Events

| Método | Rota                  | Auth | Perfil          | Descrição       |
| ------ | --------------------- | ---- | --------------- | --------------- |
| POST   | `/api/v1/events`      | Sim  | ADMIN/ORGANIZER | Criar evento    |
| GET    | `/api/v1/events`      | Não  | -               | Listar eventos  |
| GET    | `/api/v1/events/{id}` | Não  | -               | Detalhar evento |
| PUT    | `/api/v1/events/{id}` | Sim  | ADMIN/ORGANIZER | Atualizar       |
| DELETE | `/api/v1/events/{id}` | Sim  | ADMIN/ORGANIZER | Remover         |

### Venues

| Método | Rota                  | Auth | Perfil          | Descrição      |
| ------ | --------------------- | ---- | --------------- | -------------- |
| POST   | `/api/v1/venues`      | Sim  | ADMIN/ORGANIZER | Criar venue    |
| GET    | `/api/v1/venues`      | Não  | -               | Listar venues  |
| GET    | `/api/v1/venues/{id}` | Não  | -               | Detalhar venue |
| PUT    | `/api/v1/venues/{id}` | Sim  | ADMIN/ORGANIZER | Atualizar      |
| DELETE | `/api/v1/venues/{id}` | Sim  | ADMIN/ORGANIZER | Remover        |

### Sessions

| Método | Rota                    | Auth | Perfil          | Descrição |
| ------ | ----------------------- | ---- | --------------- | --------- |
| POST   | `/api/v1/sessions`      | Sim  | ADMIN/ORGANIZER | Criar     |
| GET    | `/api/v1/sessions`      | Não  | -               | Listar    |
| GET    | `/api/v1/sessions/{id}` | Não  | -               | Detalhar  |
| PUT    | `/api/v1/sessions/{id}` | Sim  | ADMIN/ORGANIZER | Atualizar |
| DELETE | `/api/v1/sessions/{id}` | Sim  | ADMIN/ORGANIZER | Remover   |

### Reservations

| Método | Rota                                            | Auth | Perfil      | Descrição       |
| ------ | ----------------------------------------------- | ---- | ----------- | --------------- |
| POST   | `/api/v1/reservations`                          | Sim  | Autenticado | Criar reserva   |
| GET    | `/api/v1/reservations`                          | Sim  | Autenticado | Listar reservas |
| GET    | `/api/v1/reservations/{id}`                     | Sim  | Autenticado | Detalhar        |
| POST   | `/api/v1/reservations/{id}/cancel`              | Sim  | Autenticado | Cancelar        |
| GET    | `/api/v1/reservations/ticket/{code}`            | Sim  | Autenticado | Ticket          |
| GET    | `/api/v1/reservations/availability/{sessionId}` | Sim  | Autenticado | Disponibilidade |

---

## Estrutura do projeto

```
src/main/java
 └── model
 └── repository
 └── service
 └── controller
 └── security

src/test/java
 └── service
 └── integration
```

---

## Docker

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

* Pipeline configurada com **GitHub Actions**
* Executa:

    * build
    * testes unitários e de integração
    * geração de relatório JaCoCo


