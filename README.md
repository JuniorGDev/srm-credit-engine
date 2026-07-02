# SRM Credit Engine

## 🚀 Sobre o Projeto

O SRM Credit Engine é uma plataforma financeira especializada no cálculo de deságio e operações multimoedas (BRL/USD) para títulos de crédito. O sistema permite a antecipação de recebíveis através de cálculos precisos de valor presente, aplicando taxas de spread diferenciadas por tipo de título (Duplicatas e Cheques Pré-Datados), com suporte a conversão cambial automática.

## 🛠️ Tecnologias Utilizadas

### Backend
- **Java 21** - Linguagem principal
- **Spring Boot 3.5.16** - Framework de aplicação
- **Spring Data JPA** - ORM e acesso a dados
- **Spring Validation** - Validação de requisições
- **Flyway** - Migrações de banco de dados
- **PostgreSQL** - Banco de dados relacional (produção)
- **H2 Database** - Banco de dados em memória (testes)
- **SpringDoc OpenAPI 2.8.16** - Documentação de API (Swagger)
- **Lombok** - Redução de boilerplate
- **JUnit 5** - Framework de testes
- **AssertJ** - Asserções fluentes para testes

### Frontend
- **Angular 20.3.0** - Framework SPA
- **TypeScript 5.9.2** - Tipagem estática
- **PrimeNG 20.4.0** - Biblioteca de componentes UI
- **PrimeIcons 7.0.0** - Ícones
- **Angular CDK 20.2.14** - Componentes de design
- **RxJS 7.8.0** - Programação reativa
- **Karma + Jasmine** - Framework de testes

### DevOps & Infraestrutura
- **Docker** - Containerização
- **Docker Compose** - Orquestração de containers
- **Maven** - Gerenciamento de dependências (backend)
- **npm** - Gerenciamento de dependências (frontend)

## 🏗️ Arquitetura & Padrões

### Justificativa da Stack

**Backend (Java + Spring Boot):**
- Escolha pela robustez e maturidade do ecossistema Spring em aplicações financeiras
- Suporte nativo a transações, validações e injeção de dependência
- Excelente integração com bancos relacionais via JPA
- Tipagem estática do Java garante maior segurança em cálculos monetários

**Frontend (Angular + PrimeNG):**
- Angular oferece estrutura consistente e escalável para aplicações empresariais
- PrimeNG fornece componentes prontos e estilizados, acelerando o desenvolvimento
- TypeScript previne erros em tempo de compilação
- RxJS facilita o gerenciamento de estado assíncrono

### Strategy Pattern para Regras de Risco

O sistema utiliza o **Strategy Pattern** para isolar as regras de cálculo de spread por tipo de recebível:

- **Interface `ReceivableStrategy`**: Define o contrato com métodos `getType()` e `getSpread()`
- **Implementações Concretas**:
  - `DuplicataMercantilStrategy`: Spread de 1.5% (0.015)
  - `ChequePreDatadoStrategy`: Spread de 2.5% (0.025)
- **StrategyFactory**: Factory que injeta todas as estratégias e retorna a adequada baseada no tipo

**Benefícios:**
- Novos tipos de recebíveis podem ser adicionados sem modificar o código existente (Open/Closed Principle)
- Testes isolados por estratégia
- Manutenção simplificada das regras de negócio

### Estrutura em Camadas (Backend)

1. **Controllers**: Camada de exposição REST, valida requisições e retorna respostas
2. **Services**: Camada de negócio, orquestra cálculos e transações
3. **Repositories**: Camada de acesso a dados via JPA
4. **Calculators**: Componentes especializados em cálculos financeiros
5. **Strategies**: Implementações de regras variáveis por tipo
6. **DTOs**: Objetos de transferência de dados (Request/Response)
7. **Entities**: Mapeamento ORM das tabelas do banco

### Global Exception Handler

O `GlobalExceptionHandler` (anotado com `@RestControllerAdvice`) centraliza o tratamento de exceções:

- Trata exceções de negócio customizadas (`ResourceNotFoundException`, `InvalidSettlementException`, etc.)
- Trata exceções de validação (`MethodArgumentNotValidException`, `ConstraintViolationException`)
- Retorna respostas HTTP apropriadas com `ProblemDetail` (RFC 7807)
- Padroniza o formato de erro em toda a API

## 💾 Modelagem de Dados

```sql
-- Tabela de Moedas
CREATE TABLE currency (
    id   BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(3)   NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Tabela de Taxas de Câmbio
CREATE TABLE exchange_rate (
    id               BIGSERIAL PRIMARY KEY,
    from_currency_id BIGINT         NOT NULL,
    to_currency_id   BIGINT         NOT NULL,
    rate             DECIMAL(19, 8) NOT NULL,
    created_at       TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_exchange_rate_from_currency
        FOREIGN KEY (from_currency_id) REFERENCES currency (id),
    CONSTRAINT fk_exchange_rate_to_currency
        FOREIGN KEY (to_currency_id) REFERENCES currency (id),
    CONSTRAINT uk_exchange_rate_currency
        UNIQUE (from_currency_id, to_currency_id)
);

CREATE INDEX idx_exchange_rate_currency
    ON exchange_rate (from_currency_id, to_currency_id);

-- Tabela de Recebíveis
CREATE TABLE receivable (
    id              BIGSERIAL PRIMARY KEY,
    seller_name     VARCHAR(150)   NOT NULL,
    face_value      DECIMAL(19, 2) NOT NULL CHECK (face_value > 0),
    due_date        DATE           NOT NULL,
    currency_id     BIGINT         NOT NULL,
    receivable_type VARCHAR(60)    NOT NULL,
    created_at      TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_receivable_currency
        FOREIGN KEY (currency_id) REFERENCES currency (id)
);

CREATE INDEX idx_receivable_due_date ON receivable (due_date);

-- Tabela de Liquidações
CREATE TABLE settlement (
    id                  BIGSERIAL PRIMARY KEY,
    receivable_id       BIGINT         NOT NULL,
    payment_currency_id BIGINT         NOT NULL,
    exchange_rate_value DECIMAL(19, 6) NOT NULL CHECK (exchange_rate_value > 0),
    present_value       DECIMAL(19, 2) NOT NULL CHECK (present_value > 0),
    discount_value      DECIMAL(19, 2) NOT NULL CHECK (discount_value > 0),
    settled_amount      DECIMAL(19, 2) NOT NULL CHECK (settled_amount > 0),
    created_at          TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP,
    CONSTRAINT fk_settlement_receivable
        FOREIGN KEY (receivable_id) REFERENCES receivable (id),
    CONSTRAINT fk_settlement_payment_currency
        FOREIGN KEY (payment_currency_id) REFERENCES currency (id)
);

CREATE INDEX idx_settlement_receivable ON settlement (receivable_id);
CREATE INDEX idx_settlement_payment_currency ON settlement (payment_currency_id);
```

## 🐋 Como Executar

### Pré-requisitos
- Docker e Docker Compose instalados
- Variáveis de ambiente configuradas no arquivo `.env` na raiz do projeto

### Subindo a Aplicação com Docker Compose

```bash
# Clone o repositório
git clone https://github.com/JuniorGDev/srm-credit-engine.git
cd srm-credit-engine

# Configure as variáveis de ambiente (exemplo no arquivo .env)
cp .env.example .env
# Edite o .env com suas configurações

# Suba todos os serviços (PostgreSQL, Backend, Frontend)
docker-compose up -d

# Verifique os logs
docker-compose logs -f
```

**Endpoints após execução:**
- API Backend: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html
- Frontend: http://localhost:4200
- PostgreSQL: localhost:5432

### Executando Testes Unitários

**Backend (Maven):**
```bash
cd credit-engine-api
mvn test
```

**Frontend (npm):**
```bash
cd credit-engine-web
npm test
```

### Executando Localmente (Sem Docker)

**Backend:**
```bash
cd credit-engine-api
mvn spring-boot:run
```

**Frontend:**
```bash
cd credit-engine-web
npm install
npm start
```