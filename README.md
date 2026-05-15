# Chiwabe Chatbot

Chatbot inteligente com integração Discord e LLM via OpenRouter.

## Estrutura do Projeto

```
ChiwabeChatbot/
├── pom.xml                          # Configuração Maven
├── README.md                        # Este arquivo
├── .gitignore                       # Padrões Git
├── .env                             # Variáveis de ambiente (não versionado)
├── cmd.bat                          # Script de execução
├── src/
│   └── main/
│       ├── java/
│       │   └── com/chiwabe/
│       │       ├── Celebro.java           # Classe principal (CLI)
│       │       ├── ChiwabeLLM.java        # Integração com LLM
│       │       ├── Memoria.java           # Gerenciamento de memória
│       │       └── ChiwabeDiscord.java    # Integração Discord (em desenvolvimento)
│       └── resources/
└── data/
    ├── memoria_ativa.json           # Histórico ativo de conversa
    ├── memoria_resumida.json        # Resumos de conversas antigas
    └── memoria.json                 # Histórico completo
```

## Requisitos

- Java 11 ou superior
- Maven 3.6+

## Configuração

1. Crie um arquivo `.env` na raiz do projeto com as seguintes variáveis:
```
OPENROUTER_KEY=sua_chave_aqui
DISCORD_TOKEN=seu_token_discord
DISCORD_CLIENT_ID=seu_client_id
```

## Build

Para compilar o projeto:
```bash
mvn clean compile
```

Para criar um JAR executável com todas as dependências:
```bash
mvn clean package
```

Isso gerará dois JARs em `target/`:
- `chiwabe-chatbot.jar` - JAR simples
- `chiwabe-chatbot-jar-with-dependencies.jar` - JAR com todas as dependências incluídas

## Execução

### Via Maven
```bash
mvn exec:java -Dexec.mainClass="com.chiwabe.Celebro"
```

### Via JAR
```bash
java -jar target/chiwabe-chatbot-jar-with-dependencies.jar
```

### Via Script (Windows)
```bash
cmd.bat
```

## Dependências

- **JDA 6.4.1** - Discord API para Java
- **Gson 2.10.1** - Processamento de JSON

## Funcionalidades

### Celebro (CLI)
- Interface de linha de comando para interagir com o chatbot
- Suporte a modo debug
- Alternância entre modelos de IA

### ChiwabeLLM
- Integração com OpenRouter API
- Suporte a múltiplos modelos de IA
- Processamento de histórico de conversa

### Memoria
- Persistência de histórico em JSON
- Compressão automática de conversas antigas
- Resumo inteligente via IA

### ChiwabeDiscord
- Integração com Discord (em desenvolvimento)

## Notas de Desenvolvimento

- Os arquivos de memória são armazenados em `data/`
- O histórico é automaticamente comprimido quando atinge 70 mensagens
- As últimas 40 mensagens são mantidas na memória ativa
- Os resumos nunca são resumidos novamente (apenas acumulados)

## Licença

Projeto pessoal - Chiwabe Chatbot
