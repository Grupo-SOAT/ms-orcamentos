# 📋 Microsserviço de Orçamentos

Microsserviço responsável pelo processamento de **orçamentos**, envio de notificações por e-mail e registro da **decisão do cliente** sobre um orçamento.

O serviço faz parte da arquitetura distribuída do sistema **Oficina Mecânica** e foi extraído do monólito para tratar de forma independente os fluxos relacionados a orçamento e comunicação com o cliente.

Desenvolvido em **Java 21**, **Spring Boot 4** e seguindo os princípios de **DDD** e **Arquitetura Hexagonal**.
---

## 📋 Sumário

* [Sobre o projeto](#-sobre-o-projeto)
* [Responsabilidades](#-responsabilidades)
* [Arquitetura](#-arquitetura)
* [Fluxo principal](#-fluxo-principal)
* [Tecnologias](#-tecnologias)
* [Estrutura do projeto](#-estrutura-do-projeto)
* [Casos de uso](#-casos-de-uso)
* [Kafka](#-kafka)
* [E-mail](#-e-mail)
* [Banco de dados](#-banco-de-dados)
* [API](#-api)
* [Observabilidade](#-observabilidade)
* [Testes](#-testes)
* [Qualidade de código](#-qualidade-de-código)
* [Docker](#-docker)
* [Execução local](#-execução-local)
* [CI/CD](#-cicd)
* [Deploy na AWS](#-deploy-na-aws)
* [Integração com o monólito](#-integração-com-o-monólito)
* [Licença](#-licença)

---

# 📖 Sobre o projeto

O `ms-orcamentos` é o microsserviço responsável pelos fluxos relacionados à comunicação e decisão sobre **orçamentos da oficina mecânica**.

A descrição oficial do projeto define o serviço como:

> Microsserviço de envio de e-mail de orçamento e registro de decisão do cliente.

O serviço utiliza comunicação assíncrona através do **Apache Kafka** e possui integração com e-mail para envio das informações relacionadas ao orçamento.

A aplicação possui também endpoints HTTP para os fluxos que precisam de comunicação síncrona.

---

# 🎯 Responsabilidades

O microsserviço possui duas responsabilidades principais.

## 📧 Envio de e-mail de orçamento

Quando um orçamento precisa ser apresentado ao cliente, o microsserviço é responsável pelo envio da comunicação por e-mail.

O conteúdo da mensagem utiliza templates **Thymeleaf**, permitindo separar o conteúdo visual do código Java.

## ✅ Registro da decisão do cliente

O microsserviço também registra a decisão tomada pelo cliente em relação ao orçamento.

O domínio possui um caso de uso específico:

```text
RegisterBudgetDecisionUseCase
```

responsável por processar esse fluxo.

---

# 🏗️ Arquitetura

O projeto segue **Arquitetura Hexagonal (Ports and Adapters)**.

```text
                         ┌─────────────────────┐
                         │   HTTP / Kafka      │
                         │    Input Adapters   │
                         └──────────┬──────────┘
                                    │
                                    ▼
                         ┌─────────────────────┐
                         │        Ports        │
                         │      Input          │
                         └──────────┬──────────┘
                                    │
                                    ▼
                   ┌────────────────────────────────┐
                   │             DOMAIN              │
                   │                                │
                   │            Budget              │
                   │                                │
                   │  Register Decision             │
                   │  Send Approval Email           │
                   └───────────────┬────────────────┘
                                   │
                                   ▼
                         ┌─────────────────────┐
                         │   Output Adapters   │
                         │                     │
                         │ PostgreSQL          │
                         │ SMTP / Mail         │
                         │ Kafka               │
                         └─────────────────────┘
```

A estrutura do código separa:

```text
adapter
config
domain
port
```

mantendo as regras de negócio independentes das implementações de infraestrutura.

---

# 🔄 Fluxo principal

Um dos fluxos principais da solução pode ser representado por:

```text
                 Monólito
                    │
                    │ evento
                    ▼
                 Kafka
                    │
                    ▼
             MS Orçamentos
                    │
             ┌──────┴──────┐
             │             │
             ▼             ▼
          PostgreSQL      SMTP
             │             │
             │             ▼
             │        E-mail cliente
             │
             ▼
       Decisão do cliente
```

O uso do Kafka permite desacoplar o processamento do orçamento do fluxo principal do monólito.

---

# 🧠 Domínio de orçamento

O domínio está concentrado em:

```text
domain/budget
```

e é dividido em:

```text
budget/
├── exception/
├── model/
└── usecase/
```

Os principais casos de uso são:

```text
RegisterBudgetDecisionUseCase
SendBudgetApprovalEmailUseCase
```

---

# 📨 Kafka

O microsserviço utiliza **Spring Kafka** para integração com o sistema de mensageria.

A dependência está explicitamente configurada no projeto:

```xml
<artifactId>spring-kafka</artifactId>
```

O Kafka permite que eventos relacionados aos orçamentos sejam processados de forma assíncrona.

### Arquitetura

```text
Monólito
   │
   │ evento de orçamento
   ▼
 Kafka
   │
   ▼
MS Orçamentos
   │
   ├── Processa orçamento
   ├── Envia e-mail
   └── Persiste decisão
```

Essa abordagem reduz o acoplamento entre o processamento principal da oficina e o fluxo de comunicação com o cliente.

---

# 📧 E-mail

O serviço utiliza:

```text
Spring Boot Starter Mail
```

para comunicação SMTP.

Também utiliza:

```text
Thymeleaf
```

para construção dos templates de e-mail.

Os templates ficam em:

```text
src/main/resources/templates/
```

Isso permite que o conteúdo dos e-mails seja alterado sem misturar HTML diretamente com as regras de negócio.

---

# 🌐 API

O serviço possui uma camada HTTP organizada em:

```text
adapter/input/budget/controller
```

e:

```text
adapter/input/budget/message
```

Essa separação permite tratar diferentes formas de entrada da aplicação, mantendo os casos de uso isolados da infraestrutura.

A API pode ser utilizada para os fluxos síncronos relacionados ao orçamento, enquanto eventos Kafka são utilizados nos fluxos assíncronos.

---

# 🗄️ Banco de dados

A persistência utiliza:

```text
PostgreSQL
```

através de:

```text
Spring Data JPA
```

O `pom.xml` também possui o driver PostgreSQL configurado.

A persistência é abstraída através da camada de portas:

```text
port/persistence
```

mantendo a regra de negócio independente do mecanismo utilizado para armazenar os dados.

---

# 🛠️ Tecnologias

| Tecnologia            | Utilização           |
| --------------------- | -------------------- |
| **Java 21**           | Linguagem            |
| **Spring Boot 4.0.5** | Framework            |
| **Spring Web**        | API HTTP             |
| **Spring Data JPA**   | Persistência         |
| **PostgreSQL**        | Banco de dados       |
| **Spring Kafka**      | Mensageria           |
| **Spring Mail**       | Envio de e-mails     |
| **Thymeleaf**         | Templates de e-mail  |
| **Spring Actuator**   | Health checks        |
| **JUnit**             | Testes               |
| **Mockito**           | Testes               |
| **Testcontainers**    | Testes de integração |
| **Awaitility**        | Testes assíncronos   |
| **JaCoCo**            | Cobertura            |
| **Maven**             | Build                |
| **Docker**            | Containerização      |

As principais dependências estão declaradas no `pom.xml`.

---

# 📁 Estrutura do projeto

```text
ms-orcamentos/
│
├── .github/
│   ├── workflows/
│   │   └── deploy.yaml
│   └── CODEOWNERS
│
├── docker-local/
│
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── br/com/fiap/postech/msorcamentos/
│   │   │       ├── adapter/
│   │   │       │   ├── input/
│   │   │       │   │   └── budget/
│   │   │       │   │       ├── controller/
│   │   │       │   │       └── message/
│   │   │       │   │
│   │   │       │   └── output/
│   │   │       │
│   │   │       ├── config/
│   │   │       │
│   │   │       ├── domain/
│   │   │       │   └── budget/
│   │   │       │       ├── exception/
│   │   │       │       ├── model/
│   │   │       │       └── usecase/
│   │   │       │
│   │   │       └── port/
│   │   │           ├── email/
│   │   │           ├── message/
│   │   │           └── persistence/
│   │   │
│   │   └── resources/
│   │       ├── templates/
│   │       ├── application.properties
│   │       └── application-local.properties
│   │
│   └── test/
│
├── Dockerfile
├── pom.xml
├── mvnw
├── mvnw.cmd
└── README.md
```

A organização do código confirma a separação entre domínio, portas e adapters.

---

# 📊 Observabilidade

O microsserviço utiliza:

```text
Spring Boot Actuator
```

para health checks e informações operacionais.

A dependência está declarada diretamente no projeto.

Isso permite que o serviço seja monitorado pelo ambiente Kubernetes e integrado à stack de observabilidade da infraestrutura.

---

# 🧪 Testes

O projeto possui suporte para:

* testes unitários;
* testes de integração;
* testes de fluxos assíncronos.

As principais ferramentas são:

```text
JUnit
Mockito
Testcontainers
Awaitility
JaCoCo
```

O `pom.xml` também configura exclusões de cobertura para camadas técnicas como:

```text
config
port
```

Executar:

```bash
./mvnw test
```

No Windows:

```cmd
mvnw.cmd test
```

---

# 📈 Cobertura

O projeto utiliza:

```text
JaCoCo
```

para geração do relatório de cobertura.

Executar:

```bash
./mvnw clean verify
```

O relatório é gerado dentro de:

```text
target/site/jacoco/
```

---

# 🐳 Docker

O projeto possui um Dockerfile multi-stage.

### Build

O primeiro estágio utiliza:

```text
maven:3.9-eclipse-temurin-21
```

para compilar o projeto.

### Runtime

A aplicação utiliza:

```text
eclipse-temurin:21-jdk-alpine
```

como imagem de execução.

A porta exposta é:

```text
8081
```

e a aplicação é iniciada através de:

```bash
java -jar /app/app.jar
```

---

# 💻 Execução local

## Pré-requisitos

* Java 21;
* Docker;
* Maven ou Maven Wrapper;
* PostgreSQL;
* Kafka, caso os fluxos assíncronos sejam executados localmente;
* servidor SMTP, caso seja necessário testar o envio de e-mails.

---

## Clonar

```bash
git clone https://github.com/Grupo-SOAT/ms-orcamentos.git
cd ms-orcamentos
```

---

## Compilar

```bash
./mvnw clean package
```

Windows:

```cmd
mvnw.cmd clean package
```

---

## Executar

```bash
./mvnw spring-boot:run
```

Ou:

```bash
java -jar target/*.jar
```

A aplicação utiliza a porta:

```text
8081
```

---

# 🐳 Executar com Docker

Construir:

```bash
docker build -t ms-orcamentos .
```

Executar:

```bash
docker run -p 8081:8081 ms-orcamentos
```

---

# 🔄 CI/CD

O projeto possui GitHub Actions:

```text
.github/workflows/
└── deploy.yaml
```

O pipeline é responsável por automatizar o processo de entrega da aplicação.

No fluxo AWS, a imagem Docker é construída, publicada no ECR e posteriormente implantada no Kubernetes através do fluxo GitOps.

---

# ☁️ Deploy na AWS

O microsserviço é executado como container no:

```text
Amazon EKS
```

Sua imagem é armazenada no:

```text
Amazon ECR
```

Repository:

```text
registry-oficina-mecanica-ms-orcamentos
```

O deployment Kubernetes é mantido no repositório:

```text
Grupo-SOAT/k8s-infra-oficina-mecanica
```

Fluxo:

```text
Código
   │
   ▼
GitHub Actions
   │
   ├── Testes
   ├── Maven Build
   └── Docker Build
          │
          ▼
        ECR
          │
          ▼
k8s-infra-oficina-mecanica
          │
          ▼
       Argo CD
          │
          ▼
         EKS
```

---

# 🔗 Integração com o monólito

O microsserviço não substitui o monólito.

Ele funciona como um componente especializado dentro da arquitetura distribuída.

```text
┌──────────────────────────────┐
│          Monólito            │
│                              │
│ Oficina Mecânica             │
│ Ordens de Serviço            │
└───────────────┬──────────────┘
                │
                │ evento
                ▼
        ┌───────────────┐
        │     Kafka     │
        └───────┬───────┘
                │
                ▼
┌──────────────────────────────┐
│       MS Orçamentos          │
│                              │
│ • Processa orçamento         │
│ • Envia e-mail               │
│ • Registra decisão           │
└───────────────┬──────────────┘
                │
          ┌─────┴─────┐
          ▼           ▼
      PostgreSQL     SMTP
                        │
                        ▼
                    Cliente
```

Essa separação permite que o fluxo de orçamento evolua independentemente do restante da aplicação.

---

# 🔐 Princípios arquiteturais

O projeto segue alguns princípios importantes:

### Separação de responsabilidades

A regra de negócio permanece no domínio.

### Ports and Adapters

As dependências externas são acessadas através de portas.

### Baixo acoplamento

Kafka, banco de dados, SMTP e HTTP são tratados como detalhes externos ao domínio.

### Processamento assíncrono

Eventos Kafka permitem que determinadas operações sejam executadas sem bloquear o fluxo principal.

### Independência do monólito

O microsserviço possui ciclo de build, testes e deploy independente.

---

# 📚 Projeto

Este microsserviço faz parte da solução **Oficina Mecânica – Tech Challenge Fase 3**, desenvolvida pelo **Grupo-SOAT**.

---

# 📄 Licença

Este projeto está licenciado sob a licença **MIT**.

Consulte o arquivo [`LICENSE`](./LICENSE).
