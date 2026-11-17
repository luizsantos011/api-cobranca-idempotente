# API de Cobrança Idempotente

![Java](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![Spring Data JPA](https://img.shields.io/badge/Spring%20Data-JPA-007396?style=for-the-badge&logo=hibernate&logoColor=white)
![H2 Database](https://img.shields.io/badge/Database-H2%20in--memory-003B57?style=for-the-badge)
![Status](https://img.shields.io/badge/Status-Concluído-blue?style=for-the-badge)

API REST desenvolvida em Java 21 com Spring Boot focada na resolução do Problema 1 (Cobranças Duplicadas) da trilha de engenharia de software para raciocínio técnico. O projeto aborda a prevenção de cobranças duplicadas diante de instabilidades de rede, retentativas automáticas e cenários de alta concorrência (race condition), garantindo a integridade financeira sem bloquear requisições legítimas de mesmo valor.

## O Problema Investigado

> Cenário: Uma API recebe solicitações de cobrança. O cliente sofre um timeout de rede e reenvia a requisição. Como impedir uma segunda cobrança indesejada sem bloquear duas compras legítimas consecutivas de mesmo valor?

Abordagens baseadas em janelas de tempo ou checagens por valor falham ao bloquear pagamentos legítimos em sequência (ex: compra repetida de passagens ou itens unitários). A solução adotada consiste no uso de chaves de idempotência combinadas com travas atômicas no banco de dados.

## Decisões Técnicas e Arquitetura

O sistema adota uma arquitetura em camadas orientada a serviços e separação estrita de responsabilidades:

```text
HTTP Request (POST /api/v1/cobranca)
       |
       v
CobrancaController ---- [ @Valid Bean Validation ] ----> CobrancaRequestDTO
       |
       v
CobrancaService
       |-- 1. Leitura Prévia (SELECT via findByIdempotencyKey)
       |       +-- Se já existe: retorna registro existente imediatamente
       |
       +-- 2. Tentativa de Escrita (INSERT com UNIQUE constraint)
               |-- Sucesso: salva e retorna com status PROCESSANDO
               +-- Concorrência (Race Condition):
                       captura DataIntegrityViolationException
                       recupera e devolve a cobrança persistida pela outra thread
```

### Desacoplamento via Chave de Idempotência (idempotencyKey)
A chave única enviada pelo cliente desassocia a identificação da operação de fatores temporais ou monetários. Isso permite que uma segunda requisição com o mesmo valor e mesmo cliente seja processada perfeitamente se possuir uma chave inédita, enquanto retentativas da mesma intenção são consolidadas em uma única transação.

### Barreira de Integridade no Banco (UNIQUE CONSTRAINT)
Validações apenas em nível de aplicação (ex: if (!repository.exists(...))) são vulneráveis a condições de corrida (Time-of-check to time-of-use - TOCTOU). Duas requisições simultâneas podem passar pela verificação antes de qualquer uma gravar no banco. A definição @Column(unique = true, nullable = false) na entidade Cobranca transfere a garantia final de atomicidade para a engine do banco de dados relacional.

### Estratégia Híbrida: Leitura Prévia e Fallback de Exceção
- Retentativas por timeout (caso comum): a leitura prévia findByIdempotencyKey localiza o registro via índice, evitando o custo computacional de disparar erros e rollbacks desnecessários no banco de dados.
- Concorrência simultânea (caso extremo): se múltiplas threads passarem pela checagem no mesmo milissegundo, a DataIntegrityViolationException disparada pelo banco é interceptada com segurança pelo CobrancaService, recuperando a instância salva e retornando-a de forma transparente ao cliente.

### Precisão Financeira
Uso de BigDecimal com escala definida para representação de valores monetários, evitando anomalias de arredondamento de ponto flutuante inerentes aos tipos primitivos double ou float.

## Tecnologias Utilizadas

| Categoria | Tecnologia / Recurso |
| :--- | :--- |
| **Linguagem & Runtime** | Java 21, Records, Streams |
| **Framework Base** | Spring Boot, Injeção de Dependências |
| **Persistência** | Spring Data JPA, Hibernate, H2 Database (em memória) |
| **Validação** | Jakarta Bean Validation (@NotBlank, @NotNull, @Positive) |
| **Testes & Concorrência** | JUnit 5, Spring Boot Test, CountDownLatch, ExecutorService |
| **Produtividade** | Lombok (@Getter, @Setter) |

## Estrutura do Projeto

```text
src/
├── main/
│   ├── java/
│   │   └── com/
│   │       └── example/
│   │           └── api_cobranca_idempotente/
│   │               ├── controller/
│   │               │   └── CobrancaController.java
│   │               ├── dto/
│   │               │   ├── CobrancaRequestDTO.java
│   │               │   └── CobrancaResponseDTO.java
│   │               ├── enums/
│   │               │   └── Status.java
│   │               ├── model/
│   │               │   └── Cobranca.java
│   │               ├── repository/
│   │               │   └── CobrancaRepository.java
│   │               ├── service/
│   │               │   └── CobrancaService.java
│   │               └── ApiCobrancaIdempotenteApplication.java
│   └── resources/
│       └── application.properties
└── test/
    └── java/
        └── com/
            └── example/
                └── api_cobranca_idempotente/
                    └── CobrancaIntegrationTest.java
```

## Evidência de Testes e Concorrência

O comportamento resiliente foi validado através de testes de integração reais com @SpringBootTest e banco em memória:

1. Idempotência Sequencial: duas requisições enviadas em sequência com a mesma idempotencyKey resultam no mesmo identificador de cobrança e em exatamente 1 registro físico no banco.
2. Concorrência Simultânea (CountDownLatch): 10 threads concorrentes disputam a inserção da mesma chave no mesmo instante através de um mecanismo de largada sincronizada. O teste comprova a captura das violações de índice único e a garantia de que apenas 1 registro é persistido, sem falhar as requisições dos clientes.

Para executar a suite de testes:

```sh
./mvnw test
```

## Como Executar a Aplicação

### Pré-requisitos
- JDK 21 ou superior instalado
- Git

### Execução local
1. Clone o repositório:
```sh
git clone <url-do-repositorio>
cd api-cobranca-idempotente
```

2. Execute a aplicação via Maven Wrapper:
```sh
./mvnw spring-boot:run
```

3. A API estará acessível em:
```text
http://localhost:8080/api/v1/cobranca
```

### Exemplo de Requisição (cURL)
```sh
curl -X POST http://localhost:8080/api/v1/cobranca \
  -H "Content-Type: application/json" \
  -d '{
    "idempotencyKey": "a93f77df-98bd-4d04-bf24-2c67ba59ecbb",
    "clienteId": "cli_98745",
    "valor": 150.00
  }'
```
