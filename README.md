# Cultural Events API

API RESTful para gerenciamento de **eventos culturais**, **locais (venues)**, **sessões** e **reservas**. O projeto implementa **autenticação JWT**, **controle de acesso por perfil (RBAC)**, **cache**, **testes unitários e de integração**.

---

## 📌 Tema e Justificativa

**Tema:** Plataforma de Eventos Culturais

**Justificativa:** A aplicação facilita o cadastro, a divulgação e o gerenciamento de eventos culturais, permitindo organizar sessões por local e controlar reservas de forma segura. O projeto aplica conceitos essenciais de sistemas corporativos, como:
* Arquitetura em camadas
* Regras de negócio centralizadas
* Validação de dados
* Segurança (Spring Security)
* Testes automatizados
* Dockerização
* Documentação (Swagger)

---

## 🚀 Tecnologias Utilizadas

* **Java 21**
* **Spring Boot 3** (Web, Data JPA, Validation, Security, Cache)
* **PostgreSQL**
* **JWT** (JSON Web Token)
* **Swagger / OpenAPI** (Springdoc)
* **Docker** & **Docker Compose**
* **Testes:** JUnit 5, Mockito, Spring Boot Test, MockMvc
* **JaCoCo** (Cobertura de testes)

---

## 🏗️ Arquitetura

O projeto segue uma arquitetura em camadas bem definida:

* `model` – Entidades JPA (Domínio)
* `repository` – Persistência de dados (Spring Data JPA)
* `service` – Regras de negócio
* `controller` – Endpoints REST (Camada de apresentação)
* `security` – Configurações de Autenticação e Autorização JWT

---

## ⚙️ Regras de Negócio Principais

1.  **Reservas:** Só podem ser criadas se houver assentos disponíveis na sessão.
2.  **Capacidade:** É definida estritamente pelo *venue* (local) onde a sessão ocorre.
3.  **Cancelamento:** O cancelamento de uma reserva altera seu status para `CANCELED` e libera o assento.
4.  **Tickets:** Podem ser consultados via código único (UUID).
5.  **Segurança:** Operações de escrita (criar/editar/deletar eventos) exigem perfil administrativo.

---

## 🔒 Segurança (JWT)

A autenticação é baseada em Tokens JWT.

### Perfis de Acesso
* `ADMIN`
* `ORGANIZER`
* `USER`

### Regras de Acesso
* **Público:** Login, Registro, Listagem de Eventos/Sessões.
* **Admin/Organizer:** CRUD de Eventos, Venues e Sessões.
* **Autenticado (User):** Criar e gerenciar suas próprias reservas.

---

## ⚡ Cache

O cache (Spring Cache) é aplicado em rotas de leitura frequente (`GET`) para otimizar a performance e reduzir a carga no banco de dados.

* **Entidades cacheadas:** Events, Venues, Sessions.
* **Invalidação:** O cache é limpo automaticamente (evict) quando ocorre uma operação de escrita (POST, PUT, DELETE) na respectiva entidade.

---

## 📦 Como Executar o Projeto

### Pré-requisitos
* Java 21
* Maven
* Docker (Opcional, mas recomendado para o banco de dados)

### 1. Subir o Banco de Dados (Docker)

Utilize o arquivo `docker-compose.yml` para subir o PostgreSQL:

```bash
docker compose up -d

```

> **Nota:** O container rodará na porta `5432`. Certifique-se de que ela está livre.

### 2. Configuração (`application.yml`)

Verifique se as configurações de ambiente batem com o seu banco local:

```yaml
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
    secret: ${SECURITY_JWT_SECRET:sua_chave_secreta_aqui_com_pelo_menos_256_bits}
    expiration-minutes: 120

```

### 3. Rodar a Aplicação

Na raiz do projeto, execute:

```bash
mvn spring-boot:run

```

---

## 📚 Documentação da API (Swagger)

Com a aplicação rodando, acesse:

* **Swagger UI:** [http://localhost:8080/swagger](https://www.google.com/search?q=http://localhost:8080/swagger)
* **OpenAPI JSON:** [http://localhost:8080/v3/api-docs](https://www.google.com/search?q=http://localhost:8080/v3/api-docs)

---

## 🧪 Testes

### Executar todos os testes

```bash
mvn test

```

### Tipos de Testes

* **Unitários (`src/test/java/.../service`):** Utilizam Mockito para isolar a camada de serviço.
* **Integração (`src/test/java/.../integration`):** Utilizam `MockMvc` e sobem o contexto do Spring para testar os endpoints e o fluxo completo.

### Cobertura de Código (JaCoCo)

Após rodar os testes, o relatório é gerado em:
`target/site/jacoco/index.html`

Para abrir no Linux:

```bash
xdg-open target/site/jacoco/index.html

```

---

## 📡 Endpoints Principais

### Auth

| Método | Rota | Auth | Descrição |
| --- | --- | --- | --- |
| `POST` | `/api/v1/auth/register` | Não | Cadastro de usuário |
| `POST` | `/api/v1/auth/login` | Não | Login e retorno de Token |

### Events

| Método | Rota | Auth | Perfil | Descrição |
| --- | --- | --- | --- | --- |
| `POST` | `/api/v1/events` | Sim | ADMIN/ORG | Cria evento |
| `GET` | `/api/v1/events` | Não | Público | Lista eventos |
| `GET` | `/api/v1/events/{id}` | Não | Público | Detalhes do evento |
| `PUT` | `/api/v1/events/{id}` | Sim | ADMIN/ORG | Atualiza evento |
| `DELETE` | `/api/v1/events/{id}` | Sim | ADMIN/ORG | Remove evento |

### Venues (Locais)

| Método | Rota | Auth | Perfil | Descrição |
| --- | --- | --- | --- | --- |
| `POST` | `/api/v1/venues` | Sim | ADMIN/ORG | Cria venue |
| `GET` | `/api/v1/venues` | Não | Público | Lista venues |
| `PUT` | `/api/v1/venues/{id}` | Sim | ADMIN/ORG | Atualiza venue |

### Sessions

| Método | Rota | Auth | Perfil | Descrição |
| --- | --- | --- | --- | --- |
| `POST` | `/api/v1/sessions` | Sim | ADMIN/ORG | Cria sessão |
| `GET` | `/api/v1/sessions` | Não | Público | Lista sessões |

### Reservations

| Método | Rota | Auth | Descrição |
| --- | --- | --- | --- |
| `POST` | `/api/v1/reservations` | Sim | Cria reserva |
| `GET` | `/api/v1/reservations` | Sim | Minhas reservas |
| `POST` | `/api/v1/reservations/{id}/cancel` | Sim | Cancela reserva |
| `GET` | `/api/v1/reservations/ticket/{code}` | Sim | Consulta ticket por UUID |
| `GET` | `/api/v1/reservations/availability/{sessionId}` | Sim | Vagas disponíveis |

---

## 📂 Estrutura de Pastas

```text
src
├── main
│   └── java
│       └── com.seuprojeto
│           ├── controller
│           ├── model
│           ├── repository
│           ├── security
│           └── service
└── test
    ├── java
    │   └── com.seuprojeto
    │       ├── integration  # Testes de Integração
    │       └── service      # Testes Unitários
    └── resources
        └── application-test.yml

```

---

## 🐳 Docker Compose (Referência)

Conteúdo do arquivo `docker-compose.yml`:

```yaml
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

## 🤖 CI/CD (GitHub Actions)

Este projeto possui uma pipeline de Integração Contínua (CI) com **GitHub Actions** para garantir que o código compile e que os testes passem a cada push/PR.

### O que a pipeline faz

- Checkout do repositório
- Setup do Java 21
- Sobe um PostgreSQL (service container)
- Executa `mvn clean test` (inclui unitários + integração)
- Gera relatório de cobertura **JaCoCo**
- Faz upload do relatório como artifact no GitHub



