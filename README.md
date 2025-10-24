# Process Scheduling Simulator

Simulador de escalonamento de processos com interface web (Next.js) e API
(Spring Boot) conectados via websocket. O objetivo é demonstrar e visualizar o comportamento de
diferentes algoritmos de escalonamento de processos.

# Links Úteis
- Repositorio: https://github.com/erikbayerlein/process-scheduling-sim
- Live Demo: https://process-scheduling-simulator.flemis.cloud/
> Observação:
Caso encontre problemas para se conectar à API no Live Demo, teste a execução local utilizando o Docker Compose.

## Como executar (apenas Docker)

Pré-requisitos:

- Docker
- Docker Compose

No diretório raiz do projeto, execute:

```bash
docker compose up -d
```

Após a inicialização os serviços estarão acessíveis em:

- Frontend (Web): http://localhost:3000
- API (Backend): http://localhost:8080

Para encerrar os serviços:

```bash
docker compose down
```

> Observação: este README foi simplificado para mostrar apenas como executar o

## Execução manual (sem Docker)

Se preferir rodar a aplicação localmente sem Docker, siga os passos abaixo.

Pré-requisitos mínimos:

- Java 21 (o projeto da API usa Java 21 conforme `api/pom.xml`)
- Maven (ou use o wrapper incluído em `./api/mvnw`)
- Node.js (recomenda-se >= 18) e npm ou pnpm

Recomenda-se abrir dois terminais: um para a API (backend) e outro para o
frontend (web).

### Rodando a API (Spring Boot)

1. Entre na pasta da API:

```bash
cd api
```

2. Usando o wrapper Maven (Linux/macOS):

```bash
./mvnw spring-boot:run
```

Ou, se preferir compilar e executar o JAR:

```bash
./mvnw clean package -DskipTests
java -jar target/*.jar
```

A API por padrão escuta na porta 8080.

### Rodando o frontend (Next.js)

1. Abra outro terminal e entre na pasta do frontend:

```bash
cd web
```

2. Instale dependências (ex.: com pnpm ou npm):

```bash
# com pnpm
pnpm install

# ou com npm
npm install
```

3. Inicie em modo de desenvolvimento:

```bash
# com pnpm
pnpm dev

# ou com npm
npm run dev
```

O frontend padrão estará disponível em http://localhost:3000 e, por padrão,
conectará à API rodando em http://localhost:8080 (verifique a URL de websocket
se necessário nas configurações do frontend).

### Observações

- Execute backend e frontend em terminais separados para desenvolvimento.
- Se precisar mudar portas ou URLs, verifique as configurações no `application.properties` da API e no código/config do frontend.
