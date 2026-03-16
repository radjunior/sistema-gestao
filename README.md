# Sistema de Gestao

Aplicacao web de gestao desenvolvida com Spring Boot, Thymeleaf e PostgreSQL.

O projeto possui autenticacao com Spring Security e modulos de cadastro para:

- Produto
- Categoria
- Grupo e Subgrupo
- Marca

## Stack

- Java 21
- Spring Boot 4
- Spring Web MVC
- Spring Data JPA
- Spring Security
- Thymeleaf
- PostgreSQL
- Maven

## Funcionalidades

- Login com autenticacao baseada em usuario salvo no banco
- Tela inicial protegida por autenticacao
- Cadastro, edicao e exclusao de produtos
- Cadastro, edicao e exclusao de categorias
- Cadastro, edicao e exclusao de grupos e subgrupos
- Cadastro, edicao e exclusao de marcas
- Controle de estoque direto no cadastro de produto
- Geracao automatica de SKU quando nao informado

## Estrutura do projeto

```text
src/main/java/br/com/gestao
|- config/         Configuracoes de seguranca e Thymeleaf
|- controller/     Controllers MVC e tratamento global de erros
|- entity/         Entidades JPA
|- repository/     Repositorios Spring Data
|- service/        Regras de negocio
|- util/           Utilitarios

src/main/resources
|- static/         CSS, JS e assets
|- templates/      Paginas Thymeleaf
|- application.properties
|- application-dev.properties
|- application-prd.properties
```

## Entidades principais

- `Produto`: dados cadastrais, dados comerciais, SKU e estoque
- `Categoria`
- `Grupo`
- `Subgrupo`
- `Marca`
- `Usuario`
- `Perfil`
- `Estoque`

## Perfis e configuracao

O projeto inicia por padrao com o perfil `dev`.

Arquivo base:

```properties
spring.application.name=sistema-gestao
spring.profiles.active=dev
```

Os perfis `dev` e `prd` usam variaveis de ambiente para a conexao com banco:

```properties
spring.datasource.url=${DB_URL}
spring.datasource.username=${DB_USER}
spring.datasource.password=${DB_PASS}
spring.datasource.driver-class-name=${DB_DRIVER}
spring.jpa.hibernate.ddl-auto=validate
```

### Variaveis de ambiente necessarias

Defina estas variaveis antes de subir a aplicacao:

```bash
DB_URL=jdbc:postgresql://localhost:5432/seu_banco
DB_USER=seu_usuario
DB_PASS=sua_senha
DB_DRIVER=org.postgresql.Driver
```

## Como executar

### 1. Compilar

```bash
mvn clean compile
```

### 2. Rodar a aplicacao

```bash
mvn spring-boot:run
```

Ou, apos empacotar:

```bash
mvn clean package
java -jar target/sistema-gestao.jar
```

## Acesso

- Login: `/login`
- Home: `/home`
- Cadastro de produto: `/cadastro/produto`
- Cadastro de categoria: `/cadastro/categoria`
- Cadastro de grupo: `/cadastro/grupo`
- Cadastro de marca: `/cadastro/marca`

## Seguranca

- Rotas estaticas liberadas: `/css/**`, `/js/**`, `/assets/**`
- Demais rotas exigem autenticacao
- Logout via `/logout`
- Passwords com `BCryptPasswordEncoder`

## Banco de dados

O projeto esta configurado com `spring.jpa.hibernate.ddl-auto=validate`, entao o schema precisa existir e estar alinhado com as entidades JPA antes da inicializacao.

## Testes

Para compilar os testes:

```bash
mvn -DskipTests test-compile
```

Para executar os testes:

```bash
mvn test
```

Observacao: como a aplicacao depende das variaveis `DB_URL`, `DB_USER`, `DB_PASS` e `DB_DRIVER`, os testes de contexto tambem exigem um ambiente configurado ou um perfil de teste dedicado.

## Observacoes de manutencao

- O cadastro de produto esta unificado: os dados comerciais e de estoque ficam diretamente em `Produto`
- O frontend do modulo de produto usa Thymeleaf, CSS proprio e JavaScript simples para filtro e calculo de preco
- A aplicacao segue arquitetura MVC tradicional com renderizacao server-side
