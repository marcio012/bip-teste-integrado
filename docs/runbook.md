# Guia de Execução e Operação (Runbook Consolidado)

Este documento consolida requisitos, instruções de execução (dev/test), integração com container EJB via JNDI e troubleshooting.

## Requisitos
- JDK 17
- Maven 3.9+
- Postgres (para dev) ou Docker (opcional)
- Node 18+ e npm (para frontend, se aplicável)

## Banco (dev)
- Configure o Postgres local:
  - URL: jdbc:postgresql://localhost:5432/bip
  - Usuário: bip
  - Senha: bip
- Scripts:
  - db/schema.sql
  - db/seed.sql

## EJB (módulo)
- Compilar e rodar testes:
  - mvn -f ejb-module/pom.xml clean test
- Build artefato:
  - mvn -f ejb-module/pom.xml clean install
  - Saída: ejb-module/target/ejb-module-0.0.1-SNAPSHOT.jar

## Backend (Spring Boot)
- Swagger (springdoc) habilitado.
- Perfis:
  - dev: Postgres (application-dev.properties)
  - test: H2 em memória (application-test.properties)
- Rodar:
  - mvn -f backend-module/pom.xml spring-boot:run
  - Com perfil dev: mvn -f backend-module/pom.xml spring-boot:run -Dspring-boot.run.profiles=dev
- Endpoints base: /api/v1/beneficios
  - GET /{id}
  - GET /
  - POST /           (criar benefício)
  - PUT /{id}        (atualizar benefício)
  - DELETE /{id}     (excluir benefício)
  - POST /{id}/creditos?valor=100.00
  - POST /{id}/debitos?valor=50.00
  - POST /transferencias?origemId=1&destinoId=2&valor=10.00
- Swagger UI:
  - http://localhost:8080/swagger-ui/index.html
  - OpenAPI JSON: /v3/api-docs

## Integração com EJB (JNDI)
- Lookup usado pelo backend:
  - java:global/ejb-module/BeneficioEjbService
- Execução integrada (container EJB):
  1) Build do EJB (ver seção EJB).
  2) Implantar o JAR no container (TomEE/Payara/WildFly).
  3) Configurar DataSource no container, alinhado ao Postgres do backend.
  4) Validar nome JNDI publicado. Se diferente, ajustar o lookup no backend.
  5) Subir o backend no perfil dev, garantindo acesso ao container EJB.

### Passos por container
- WildFly
  - Deploy: copiar o JAR para standalone/deployments/
  - Configurar DataSource (mesma URL/usuário)
  - Validar JNDI publicado para BeneficioEjbService
- Payara/GlassFish
  - Admin Console > Applications > Deploy (selecionar JAR)
  - Criar JDBC Connection Pool + JDBC Resource
  - Validar JNDI
- TomEE
  - Empacotar em EAR/WAR reconhecido pelo TomEE e deploy em webapps/
  - Garantir JNDI estável

## Testes
- EJB:
  - Funcionais de crédito, débito, transferência e validações.
  - Concorrência (lock pessimista e @Version).
- Backend:
  - Testes de controller (CRUD e operações) com MockMvc e @ControllerAdvice.
- Comandos:
  - EJB: mvn -f ejb-module/pom.xml clean test
  - Backend: mvn -f backend-module/pom.xml clean test

## Decisões técnicas
- Jakarta EE 10 (jakarta.*) no EJB.
- Lock pessimista em operações críticas; entidade com @Version para suportar otimista.
- Exceções com @ApplicationException(rollback = true).
- Spring Boot 3 na camada REST com Swagger (springdoc).
- H2 para testes; Postgres para dev.

## CI (GitHub Actions)
- Build e testes de ejb-module e backend-module.
- Job opcional para frontend (lint/test/build) se scripts existirem.

## Troubleshooting
- 500 no backend ao chamar EJB:
  - Container EJB está no ar? JAR implantado?
  - Nome JNDI confere com o lookup?
  - DataSource configurado e acessível?
- Conexão ao banco:
  - Verifique URLs/credenciais nos dois lados (EJB e backend).
- Concorrência/Deadlock:
  - Ordem de lock por ID implementada; revisar logs SQL se necessário.
- Testes locais:
  - EJB: usa PU de teste com H2; não interfere no dev/prod.

## Frontend (opcional)
- Requisitos: Node 18+, npm
- Dentro de frontend/:
  - npm ci
  - npm run lint (se existir)
  - npm test -- --watch=false (se existir)
  - npm run build