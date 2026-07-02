# AI_USAGE.md — Uso de Inteligência Artificial como Copiloto

Este documento detalha como a Inteligência Artificial (LLM) foi utilizada como ferramenta de suporte no desenvolvimento do **SRM Credit Engine**, em conformidade com as diretrizes da Política de Uso de IA do desafio técnico.

---

# 1. Abordagem Estratégica e Filosofia de Uso

O uso da IA neste projeto foi pautado no princípio da **Autoria Intelectual**.

A ferramenta foi utilizada essencialmente para:

- acelerar tarefas repetitivas, como a estrutura inicial de testes unitários;
- validar lógicas de negócio complexas;
- discutir alternativas de implementação;
- simular cenários de borda;
- revisar decisões arquiteturais.

Toda a arquitetura final, as decisões de design de código e a palavra final sobre a simplicidade da solução foram tomadas por mim, garantindo domínio completo sobre todo o código entregue.

---

# 2. Prompts Estratégicos Utilizados

Abaixo estão registrados os principais prompts utilizados durante o desenvolvimento.

## 2.1 Testes do Calculador de Valor Presente

```text
Analyze the entire project before generating tests.

Understand how PresentValueCalculator is used by SettlementService.

Generate a complete unit test class using JUnit 5 and AssertJ.

Do not use Mockito.

Cover:
- Correct present value calculation
- Different receivable types
- Different spreads
- Same day due date
- Future due dates
- Large monetary values
- Decimal precision
- Rounding
- Zero values
- Invalid values
- Boundary conditions

Generate clean, production-ready tests.
```

**Objetivo**

Garantir o comportamento matemático e a precisão decimal do motor de cálculo sem acoplamento com mocks.

---

## 2.2 Testes do StrategyFactory

```text
Analyze the project before generating tests.

Generate unit tests for StrategyFactory.

Verify:
- Correct spread value
- Supports the expected ReceivableType
- No unnecessary mocks

Use JUnit 5 and AssertJ.
```

**Objetivo**

Validar o desacoplamento do padrão Strategy e o correto roteamento das estratégias.

---

## 2.3 Testes das Estratégias Específicas

```text
Analyze the project before generating tests.

Generate unit tests for (ChequePreDatadoStrategy ou DuplicataMercantil).

Verify:
- Correct spread value
- Supports the expected ReceivableType
- No unnecessary mocks

Use JUnit 5 and AssertJ.
```

**Objetivo**

Garantir o isolamento das regras de spread para cada tipo de recebível.

---

## 2.4 Testes da Camada de Controllers

```text
First, inspect the workspace and understand:

- project architecture
- package organization
- controller layer
- service layer
- DTOs
- custom exceptions
- GlobalExceptionHandler
- Bean Validation annotations
- Swagger annotations
- existing coding conventions

Do not assume any implementation.

After analyzing the project, generate a production-ready unit test class for
(CurrencyController ou ExchangeRateController ou SettlementController).

Requirements:

- Use @WebMvcTest
- Use MockMvc
- Use @MockBean for all service dependencies
- Do NOT use @SpringBootTest
- Do NOT access the database
- Do NOT mock Spring MVC components
- Follow Arrange-Act-Assert
- Use descriptive test method names
- Keep tests clean and readable
- Use Jackson ObjectMapper to serialize request bodies
- Verify HTTP status codes
- Verify JSON response bodies using jsonPath()
- Verify service interactions with Mockito
- Cover every public endpoint
- Validate error responses asserting the complete ProblemDetail payload
  (type, title, status, detail, instance)

Before generating the tests, create a complete test plan describing every endpoint and scenario that will be covered.
```

**Objetivo**

Garantir cobertura completa da camada HTTP, validações e tratamento de erros conforme RFC 9457.

---

## 2.5 Testes da Camada de Services

```text
First, inspect the entire workspace.

Understand the project architecture before generating any tests.

Analyze:

- package organization
- entities
- DTOs
- repositories
- services
- calculators
- strategies
- custom exceptions
- validation rules
- business rules
- coding style

Use only the existing project structure.

After analyzing the codebase, generate a complete unit test class for
(ExchangeRateService ou CurrencyService ou SettlementService)
following the existing project conventions.

Requirements:

- Use JUnit 5
- Mockito
- AssertJ
- @ExtendWith(MockitoExtension.class)
- @InjectMocks
- @Mock

Do not use @SpringBootTest.

Mock only external dependencies.

Follow Arrange-Act-Assert.

Cover:
- happy path
- validation
- business rules
- exceptions
- repository interactions

Before generating the tests, provide a test plan listing every scenario.
```

**Objetivo**

Garantir cobertura das regras de negócio, validações e exceções customizadas.

---

# 3. Evolução de Negócio e Refatorações Discutidas

Além da geração de testes, a IA foi utilizada como ferramenta de brainstorming técnico para validar decisões durante o desenvolvimento.

Os principais tópicos discutidos foram:

## Cálculo do Valor Presente

Discussão sobre o uso do prazo em meses versus dias para refinamento da fórmula financeira.

## Liquidação na Mesma Moeda

Refinamento da regra para que a conversão cambial ocorra apenas em operações cross-currency.

## Persistência da Taxa de Câmbio

Decisão de armazenar `exchange_rate_value` como `NULL` quando não houver conversão de moeda.

## Arquitetura do Settlement

Refinamento da separação de responsabilidades entre:

- SettlementService
- StrategyFactory
- PresentValueCalculator
- SettlementContext

## Consulta Analítica

Migração da consulta do extrato de liquidação de JPA para SQL Nativo visando maior eficiência.

## Simplificação da Query do Extrato

A busca passou a exigir obrigatoriamente um período, reduzindo a complexidade da consulta.

## Endpoint de Exchange Rate

Evolução para uma abordagem REST utilizando parâmetros de consulta com `@GetMapping(params = ...)`.

## Docker

Discussão da orquestração da aplicação para execução completa utilizando apenas:

```bash
docker compose up
```

## Frontend

Utilização da IA para:

- interpretar requisitos do desafio;
- validar fluxos da aplicação;
- auxiliar no desenho estrutural dos componentes da interface.

---

# 4. Análise Crítica

## Onde a IA Economizou Tempo

A IA proporcionou ganhos significativos de produtividade principalmente em:

- geração inicial de testes unitários;
- scaffolding de Controllers;
- criação de DTOs;
- configuração de MockMvc;
- validação de cenários;
- revisão de regras de negócio;
- apoio na estruturação inicial do frontend.

Seu uso foi especialmente útil para acelerar tarefas repetitivas, permitindo maior foco nas decisões arquiteturais.

---

## Onde a IA Atrapalhou

Durante o desenvolvimento ocorreram situações em que a IA apresentou limitações.

Os principais problemas encontrados foram:

- geração de arquiteturas excessivamente complexas para um projeto de pequeno porte;
- sugestões incompatíveis com o contexto existente da aplicação;
- contradições entre respostas ao longo das interações;
- propostas de implementação que violavam o princípio KISS.

---

## Decisão do Desenvolvedor

Sempre que as sugestões produzidas pela IA aumentavam desnecessariamente a complexidade da solução ou apresentavam inconsistências, elas foram descartadas.

Nesses casos, as decisões finais foram tomadas com base na análise do código, consulta à documentação oficial e implementação manual, preservando a simplicidade, a coesão e os requisitos do desafio técnico.