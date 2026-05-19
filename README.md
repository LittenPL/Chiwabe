# 🤖 Chiwabe Chatbot

Um chatbot inteligente e versátil com suporte a múltiplas interfaces (CLI e Discord), integração com LLM via OpenRouter, e um sistema avançado de memória com compressão automática e resumos inteligentes.

## 📋 Visão Geral

Chiwabe é um assistente de IA que pode ser usado tanto via linha de comando (CLI) quanto como um bot no Discord. O sistema mantém histórico de conversas por usuário, comprime automaticamente conversas antigas usando IA, e oferece suporte a múltiplos modelos de linguagem.

### Características Principais

- ✅ **Interface CLI** - Interação via linha de comando com modo debug
- ✅ **Bot Discord** - Integração completa com Discord (JDA 6.4.1)
- ✅ **Sistema de Memória Inteligente** - Histórico persistente com compressão automática
- ✅ **Resumos Automáticos** - Conversas antigas são resumidas via IA quando limite é atingido
- ✅ **Múltiplos Usuários** - Históricos separados por usuário (CLI, Discord individual ou por servidor)
- ✅ **Múltiplos Modelos** - Suporte a diferentes modelos de IA via OpenRouter
- ✅ **Modo Debug** - Informações detalhadas de execução para desenvolvimento

## 🔧 Requisitos

- **Java 21** ou superior
- **Maven 3.6+**
- Chave de API do [OpenRouter](https://openrouter.ai/)
- Token do Discord (para usar o bot Discord)

## 📁 Estrutura do Projeto

```
Chiwabe/
├── pom.xml                              # Configuração Maven
├── README.md                            # Este arquivo
├── .gitignore                           # Padrões Git
├── cmd.bat                              # Script de execução (Windows)
├── discord.bat                          # Script para iniciar bot Discord (Windows)
├── DISCORD_HELP.md                      # Documentação do bot Discord
│
├── ChiwabeChatbot/
│   └── .env                             # Variáveis de ambiente (não versionado)
│
├── src/main/java/com/chiwabe/
│   ├── Celebro.java                     # Interface CLI (linha de comando)
│   ├── ChiwabeLLM.java                  # Integração com OpenRouter API
│   ├── Memoria.java                     # Gerenciamento de histórico e memória
│   └── ChiwabeDiscord.java              # Bot Discord (JDA)
│
└── data/
    └── usuarios/                        # Históricos por usuário
        ├── CLI/                         # Histórico do usuário CLI
        │   ├── memoria_ativa.json       # Últimas 40 mensagens
        │   ├── memoria_resumida.json    # Resumos de conversas antigas
        │   └── memoria.json             # Histórico completo
        │
        └── [DISCORD_USER_ID]/           # Histórico por usuário Discord
            ├── memoria_ativa.json
            ├── memoria_resumida.json
            └── memoria.json
```

## ⚙️ Configuração

### 1. Clonar o Repositório

```bash
git clone <seu-repositorio>
cd Chiwabe
```

### 2. Configurar Variáveis de Ambiente

Crie um arquivo `.env` na pasta `ChiwabeChatbot/`:

```env
OPENROUTER_KEY=sua_chave_openrouter_aqui
DISCORD_TOKEN=seu_token_discord_aqui
DISCORD_CLIENT_ID=seu_client_id_aqui
```

**Onde obter as credenciais:**
- **OpenRouter Key**: [https://openrouter.ai/keys](https://openrouter.ai/keys)
- **Discord Token**: [Discord Developer Portal](https://discord.com/developers/applications)
- **Discord Client ID**: Também no Discord Developer Portal

## 🏗️ Build

### Compilar o Projeto

```bash
mvn clean compile
```

### Criar JAR Executável

```bash
mvn clean package
```

Isso gera dois JARs em `target/`:
- `chiwabe-chatbot.jar` - JAR simples
- `chiwabe-chatbot-jar-with-dependencies.jar` - JAR com todas as dependências (recomendado)

## 🚀 Execução

### Via Maven (CLI)

```bash
mvn exec:java -Dexec.mainClass="com.chiwabe.Celebro"
```

### Via JAR (CLI)

```bash
java -jar target/chiwabe-chatbot-jar-with-dependencies.jar
```

### Via Script Windows (CLI)

```bash
cmd.bat
```

### Bot Discord

```bash
mvn exec:java -Dexec.mainClass="com.chiwabe.ChiwabeDiscord"
```

Ou via script Windows:

```bash
discord.bat
```

## 📚 Funcionalidades Detalhadas

### 🖥️ Celebro (Interface CLI)

A interface de linha de comando permite interagir com Chiwabe de forma direta.

**Recursos:**
- Modo debug ativável na inicialização
- Alternância entre modelos de IA com comandos:
  - `seja burra` - Muda para Nemotron 3 Nano (modelo mais rápido)
  - `seja inteligente` - Muda para Nemotron 3 Super (modelo mais poderoso)
- Histórico persistente por usuário
- Suporte a múltiplas conversas simultâneas

**Exemplo de uso:**

```
DepureMode y/n: n

U: Olá, como você está?

Chiwabe: [resposta da IA]

Tokens: 245

U: seja inteligente
Alterado para Nemotron 3 Super

U: Explique a teoria da relatividade
```

### 🤖 ChiwabeLLM

Módulo responsável pela integração com a API OpenRouter.

**Funcionalidades:**
- Chamadas HTTP para OpenRouter API
- Suporte a múltiplos modelos de IA
- Processamento de histórico de conversa
- Extração de tokens utilizados
- Tratamento de erros (rate limiting, erros de conexão, etc.)
- Escape seguro de caracteres especiais em JSON

**Modelos Disponíveis:**
- `nvidia/nemotron-3-nano-30b-a3b:free` - Rápido e leve
- `nvidia/nemotron-3-super-120b-a12b:free` - Mais poderoso e preciso
- Qualquer outro modelo disponível no OpenRouter

### 💾 Memoria

Sistema avançado de gerenciamento de histórico com compressão automática.

**Características:**

1. **Histórico Ativo** (`memoria_ativa.json`)
   - Armazena as últimas 40 mensagens
   - Carregado em cada conversa para contexto
   - Atualizado após cada interação

2. **Histórico Completo** (`memoria.json`)
   - Cópia completa de todas as mensagens
   - Salvo para backup e auditoria

3. **Resumos Antigos** (`memoria_resumida.json`)
   - Armazena resumos de conversas antigas
   - Gerados automaticamente quando histórico > 70 mensagens
   - Nunca são resumidos novamente (apenas acumulados)
   - Incluídos no contexto da IA para manter continuidade

**Fluxo de Compressão:**

```
Histórico cresce → 70+ mensagens
    ↓
Primeiras 50 mensagens são resumidas via IA
    ↓
Resumo é salvo em memoria_resumida.json
    ↓
Últimas 40 mensagens são mantidas em memoria_ativa.json
    ↓
Próximas conversas usam: [resumos antigos] + [últimas 40 mensagens]
```

**Suporte a Múltiplos Usuários:**
- Cada usuário tem seu próprio diretório em `data/usuarios/`
- CLI usa ID "CLI"
- Discord usa ID do usuário ou ID do servidor + ID do usuário (configurável)
- Históricos completamente isolados

### 🎮 ChiwabeDiscord

Bot Discord totalmente funcional com suporte a servidor e contexto específico.

**Recursos:**
- Responde a menções no Discord
- Modo livre (responde a todas as mensagens)
- Modo memória por servidor (histórico separado por servidor)
- Modo debug para desenvolvimento
- Divide mensagens longas (limite Discord: 2000 caracteres)
- Indicador de digitação ("está digitando...")
- Suporte a contexto específico (ex: projeto CABO 21)

**Configuração:**
- Edite as variáveis no início de `ChiwabeDiscord.java`:
  - `dev_mode` - Ativa logs detalhados
  - `livre` - Se true, responde a todas as mensagens; se false, apenas a menções
  - `memoriaServidor` - Se true, histórico por servidor; se false, por usuário global

**Exemplo de Uso no Discord:**

```
@Chiwabe Olá, como você está?
→ Chiwabe: [resposta]

@Chiwabe Explique o projeto CABO 21
→ Chiwabe: [resposta baseada no contexto do projeto]
```

## 📊 Dependências

```xml
<!-- JDA 6.4.1 - Discord API para Java -->
<dependency>
    <groupId>net.dv8tion</groupId>
    <artifactId>JDA</artifactId>
    <version>6.4.1</version>
</dependency>

<!-- Gson 2.10.1 - Processamento de JSON -->
<dependency>
    <groupId>com.google.code.gson</groupId>
    <artifactId>gson</artifactId>
    <version>2.10.1</version>
</dependency>
```

## 🔍 Modo Debug

Ative o modo debug para ver informações detalhadas de execução:

```
DepureMode y/n: y
```

**Informações exibidas:**
- Chave de API carregada (primeiros 10 caracteres)
- Número de mensagens na memória ativa
- Número de resumos carregados
- Status HTTP da resposta
- Corpo completo da resposta da API
- Número de tokens utilizados
- Mensagens salvas na memória

## 📝 Notas de Desenvolvimento

### Sistema de Memória

- O histórico é automaticamente comprimido quando atinge **70 mensagens**
- As últimas **40 mensagens** são mantidas na memória ativa
- Os resumos nunca são resumidos novamente (apenas acumulados)
- Cada usuário tem seu próprio histórico isolado
- A compressão usa um modelo de IA mais rápido (`openrouter/owl-alpha`)

### Tratamento de Erros

- **429 (Rate Limit)**: Exibe aviso de limite atingido
- **400 (Bad Request)**: Exibe o JSON enviado para debug
- **Outros erros**: Mensagem genérica de erro

### Performance

- Histórico em JSON para fácil manipulação
- Compressão automática evita crescimento infinito de memória
- Resumos reutilizáveis para contexto histórico
- Suporte a múltiplos usuários sem conflitos

### Segurança

- Escape de caracteres especiais em JSON
- Validação de entrada (mensagens vazias são ignoradas)
- Tokens de API não são exibidos completamente em logs
- Históricos isolados por usuário

## 🐛 Troubleshooting

### "Erro ao ler arquivo .env"
- Verifique se o arquivo `.env` existe em `ChiwabeChatbot/`
- Verifique se tem permissão de leitura

### "Limite atingido!" (429)
- Você atingiu o rate limit do OpenRouter
- Aguarde alguns minutos antes de fazer novas requisições

### "Erro ao conectar"
- Verifique sua conexão com a internet
- Verifique se a chave de API é válida
- Verifique se o modelo especificado existe no OpenRouter

### Bot Discord não responde
- Verifique se o token do Discord é válido
- Verifique se o bot tem permissão para enviar mensagens no canal
- Ative o modo debug para ver logs detalhados

## 📄 Licença

Projeto pessoal - Chiwabe Chatbot

## 👤 Autor

Desenvolvido por [LittenPL]

---

**Última atualização:** Maio de 2026
