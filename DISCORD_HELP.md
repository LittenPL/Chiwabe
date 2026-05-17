# 🤖 Guia de Integração Discord - Chiwabe Bot

## ✅ Status da Implementação

A integração do Chiwabe com Discord foi **100% concluída**! Aqui está o que foi feito:

### Fase 1: Preparação ✅
- ✅ Arquivo `.env` configurado com:
  - `API_KEY` - Chave da OpenRouter
  - `DISCORD_BOT_TOKEN` - Token do bot Discord
  - `DISCORD_CLIENT_ID` - ID da aplicação Discord

### Fase 2: Refatoração ✅
- ✅ `Memoria.java` refatorado para suportar múltiplos usuários
  - Cada usuário tem seu próprio histórico em `data/usuarios/{userId}/`
  - CLI usa `userId = "CLI"`
  - Discord usa `userId = ID do usuário Discord`

### Fase 3: Integração Discord ✅
- ✅ `ChiwabeDiscord.java` implementado com:
  - Listener de mensagens do Discord
  - Integração com `ChiwabeLLM`
  - Divisão automática de mensagens (limite 2000 caracteres)
  - Histórico separado por usuário
  - Limite de 4200 tokens por resposta

### Fase 4: Compatibilidade ✅
- ✅ CLI (`Celebro.java`) continua funcionando normalmente
- ✅ Ambos usam o mesmo sistema de memória
- ✅ Históricos separados (CLI vs Discord)

---

## 🚀 Como Usar

### 1. **Executar o Bot Discord**

```bash
cd c:/Users/pedro/Chiwabe
mvn clean compile
mvn exec:java -Dexec.mainClass="com.chiwabe.ChiwabeDiscord"
```

Ou criar um script `run_discord.bat`:
```batch
@echo off
cd c:\Users\pedro\Chiwabe
mvn exec:java -Dexec.mainClass="com.chiwabe.ChiwabeDiscord"
pause
```

### 2. **Executar a CLI**

```bash
cd c:/Users/pedro/Chiwabe
mvn clean compile
mvn exec:java -Dexec.mainClass="com.chiwabe.Celebro"
```

Ou usar o `cmd.bat` existente.

---

## 📁 Estrutura de Diretórios

```
data/
├── usuarios/
│   ├── CLI/
│   │   ├── memoria_ativa.json
│   │   ├── memoria_resumida.json
│   │   └── memoria.json
│   ├── 123456789/          (ID do usuário Discord)
│   │   ├── memoria_ativa.json
│   │   ├── memoria_resumida.json
│   │   └── memoria.json
│   └── 987654321/
│       ├── memoria_ativa.json
│       ├── memoria_resumida.json
│       └── memoria.json
```

---

## 🔧 Configuração do Discord Developer Portal

Se ainda não fez, siga estes passos:

### 1. Criar Aplicação
- Acesse [Discord Developer Portal](https://discord.com/developers/applications)
- Clique em "New Application"
- Dê um nome (ex: "Chiwabe")

### 2. Gerar Token
- Vá para "Bot" → "Add Bot"
- Copie o token e adicione ao `.env` como `DISCORD_BOT_TOKEN`

### 3. Configurar Permissões
- Em "Bot" → "Scopes", selecione:
  - ✅ `bot`
  - ✅ `applications.commands`

- Em "Bot" → "Permissions", selecione:
  - ✅ `Send Messages`
  - ✅ `Read Messages/View Channels`
  - ✅ `Read Message History`

### 4. Gerar URL de Convite
- Copie a URL gerada em "Scopes"
- Abra em um navegador e adicione o bot ao seu servidor

### 5. Obter Client ID
- Em "General Information", copie o "Application ID"
- Adicione ao `.env` como `DISCORD_CLIENT_ID`

---

## 💾 Sistema de Memória

### Como Funciona
1. **Memória Ativa** (`memoria_ativa.json`)
   - Armazena até 70 mensagens recentes
   - Carregada a cada interação

2. **Memória Resumida** (`memoria_resumida.json`)
   - Armazena resumos de conversas antigas
   - Nunca é resumida novamente
   - Incluída no contexto da IA

3. **Memória Completa** (`memoria.json`)
   - Backup de todo o histórico
   - Salvo ao final de cada conversa

### Limite de Tokens
- **Máximo**: 4200 tokens por resposta
- **Modelo padrão**: `nvidia/nemotron-3-nano-30b-a3b:free`
- **Modelo alternativo**: `nvidia/nemotron-3-super-120b-a12b:free`

---

## 🎯 Funcionalidades

### CLI (Celebro.java)
```
U: Olá!
Chiwabe: [resposta]

U: seja inteligente
Alterado para Nemotron 3 Super

U: seja burra
Alterado para Nemotron 3 Nano

U: tchau
[Salva memória e encerra]
```

### Discord (ChiwabeDiscord.java)
- Responde a todas as mensagens no servidor
- Mantém histórico separado por usuário
- Divide respostas grandes automaticamente
- Mostra indicador de digitação

---

## 🐛 Troubleshooting

### Erro: "Arquivo .env não encontrado"
- Certifique-se de que o arquivo está em `ChiwabeChatbot/.env`
- Verifique se tem as 3 linhas:
  ```
  API_KEY=...
  DISCORD_BOT_TOKEN=...
  DISCORD_CLIENT_ID=...
  ```

### Erro: "Bot não responde no Discord"
- Verifique se o bot está online (status verde)
- Confirme que tem permissão de "Send Messages" no canal
- Verifique os logs para mensagens de erro

### Erro: "Limite de tokens atingido"
- Aguarde alguns minutos
- Ou use um modelo diferente com `seja inteligente` (CLI)

---

## 📊 Próximas Melhorias (Opcional)

- [ ] Adicionar comando `/help` no Discord
- [ ] Implementar rate limiting por usuário
- [ ] Adicionar reações (👍, 👎) para feedback
- [ ] Suporte a embeds do Discord
- [ ] Logging em arquivo
- [ ] Dashboard web para gerenciar memória

---

## 📝 Notas Importantes

1. **Segurança**: Nunca compartilhe seu `.env` ou tokens
2. **Privacidade**: Cada usuário tem seu próprio histórico
3. **Compatibilidade**: CLI e Discord funcionam simultaneamente
4. **Memória**: Históricos são persistidos em JSON

---

## ✨ Pronto para Usar!

Seu bot Chiwabe está 100% funcional e pronto para:
- ✅ Responder no Discord
- ✅ Manter histórico por usuário
- ✅ Funcionar via CLI
- ✅ Gerenciar memória automaticamente

Divirta-se! 🎉
