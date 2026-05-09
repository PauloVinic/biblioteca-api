# Biblioteca API

API REST para gestao de biblioteca desenvolvida para o Tech Challenge Fase 2. A aplicacao permite controlar livros, usuarios, emprestimos, devolucoes e relatorios operacionais, com persistencia em PostgreSQL, documentacao OpenAPI/Swagger e execucao via Docker.

## Tecnologias utilizadas

- Java 21
- Spring Boot 3.5
- Spring Web
- Spring Data JPA
- Jakarta Validation
- PostgreSQL
- H2 para testes de integracao
- Springdoc OpenAPI / Swagger UI
- Maven Wrapper
- JaCoCo
- Docker e Docker Compose

## Funcionalidades

- Cadastro, consulta paginada, atualizacao e exclusao de livros.
- Filtros de livros por titulo, autor e ISBN.
- Cadastro, consulta paginada, atualizacao e exclusao de usuarios.
- Registro de emprestimos com calculo da data prevista de devolucao.
- Registro de devolucoes com atualizacao do status do emprestimo e disponibilidade do livro.
- Consulta paginada de emprestimos por status.
- Relatorio dos 20 livros mais emprestados.
- Relatorio dos livros emprestados no momento com previsao de devolucao.
- Tratamento padronizado de erros de validacao, negocio e recursos nao encontrados.

## Requisitos para execucao

- Java 21 para execucao local sem Docker.
- Docker e Docker Compose para subir a API com PostgreSQL.
- Porta `8080` disponivel para a API.
- Porta `5432` disponivel para o PostgreSQL quando usar `docker compose`.

## Como rodar com Docker

```powershell
docker compose up --build
```

A API ficara disponivel em:

```text
http://localhost:8080
```

Para encerrar o ambiente:

```powershell
docker compose down
```

Para remover tambem o volume do PostgreSQL:

```powershell
docker compose down -v
```

## Swagger

Com a aplicacao em execucao, acesse:

```text
http://localhost:8080/swagger-ui.html
```

O documento OpenAPI tambem fica disponivel em:

```text
http://localhost:8080/api-docs
```

No perfil `prod`, a documentacao interativa e desabilitada.

## Como rodar os testes

No Windows:

```powershell
.\mvnw.cmd clean verify
```

Em Linux/macOS:

```bash
./mvnw clean verify
```

O comando executa a suite automatizada e a validacao de cobertura do JaCoCo.

## Cobertura de testes

O build possui regra de cobertura minima de 90% para o escopo monitorado pelo JaCoCo. No estado final validado do projeto, a cobertura de instrucoes ficou em 94,92%, com 61 testes aprovados.

O relatorio HTML gerado pelo JaCoCo fica em:

```text
target/site/jacoco/index.html
```

## Estrutura do projeto

```text
biblioteca-api/
├── src/
│   ├── main/
│   │   ├── java/com/techchallenge/biblioteca/
│   │   └── resources/
│   └── test/
├── docs/
│   ├── RELATORIO_TECNICO.pdf
│   └── RELATÓRIO TÉCNICO.docx
├── Dockerfile
├── docker-compose.yml
├── .dockerignore
├── .gitignore
├── pom.xml
└── README.md
```

## Relatorio tecnico

O relatorio tecnico principal da entrega esta em:

```text
docs/RELATORIO_TECNICO.pdf
```

O arquivo `docs/RELATÓRIO TÉCNICO.docx` foi mantido como backup editavel.

## Observacoes

Esta entrega foi preparada para Java 21, PostgreSQL e Docker. A execucao com Docker usa o perfil `docker`, configurado para conectar a API ao servico `postgres` definido no `docker-compose.yml`.

## Identificacao da entrega

Aluno: Paulo Vinicius de Souza Martinez

RM: RM360057

Repositorio: https://github.com/PauloVinic/biblioteca-api
