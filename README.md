# Process Scheduling Simulator

Simulador de escalonamento de processos com interface web e API REST.

## 📋 Pré-requisitos

Antes de executar o projeto, certifique-se de ter instalado:

- [Docker](https://docs.docker.com/get-docker/) (versão 20.10 ou superior)
- [Docker Compose](https://docs.docker.com/compose/install/) (versão 2.0 ou superior)

Para verificar se estão instalados corretamente, execute:

```bash
docker --version
docker compose version
```

## 🚀 Como Executar

### 1. Clone o repositório (se ainda não tiver)

```bash
git clone <url-do-repositorio>
cd process-scheduling-sim
```

### 2. Inicie os serviços com Docker Compose

```bash
docker compose up --build
```

Este comando irá:
- Construir as imagens Docker para a API (Spring Boot) e o frontend (Next.js)
- Iniciar os containers
- Configurar a rede entre os serviços

### 3. Acesse a aplicação

Após a inicialização (pode levar alguns minutos na primeira vez):

- **Frontend (Web)**: http://localhost:3000
- **API (Backend)**: http://localhost:8080

## 🛠️ Comandos Úteis

### Executar em segundo plano (modo detached)

```bash
docker compose up -d
```

### Ver logs dos serviços

```bash
# Todos os serviços
docker compose logs -f

# Apenas a API
docker compose logs -f api

# Apenas o frontend
docker compose logs -f web
```

### Parar os serviços

```bash
docker compose down
```

### Parar e remover volumes

```bash
docker compose down -v
```

### Reconstruir as imagens

```bash
docker compose build --no-cache
docker compose up
```

### Reiniciar um serviço específico

```bash
# Reiniciar apenas a API
docker compose restart api

# Reiniciar apenas o frontend
docker compose restart web
```

## 🏗️ Arquitetura

O projeto é composto por dois serviços:

- **API (Backend)**: Aplicação Spring Boot rodando na porta 8080
- **Web (Frontend)**: Aplicação Next.js rodando na porta 3000

Os serviços se comunicam através de uma rede Docker interna (`app-network`).

## 🔍 Troubleshooting

### Porta já em uso

Se as portas 3000 ou 8080 já estiverem em uso, você pode:

1. Parar o serviço que está usando a porta
2. Ou modificar as portas no arquivo `docker-compose.yml`

### Problemas de build

Se houver erros durante o build:

```bash
# Limpe os containers e imagens antigas
docker compose down
docker system prune -a

# Reconstrua do zero
docker compose build --no-cache
docker compose up
```

### API não responde

Verifique o status do healthcheck:

```bash
docker compose ps
```

A API possui um healthcheck que verifica se está saudável antes do frontend iniciar.

## 📝 Desenvolvimento

Para desenvolvimento local sem Docker, consulte os READMEs específicos:

- API: `./api/README.md` (ou `./api/HELP.md`)
- Web: Verifique o `package.json` na pasta `./web`