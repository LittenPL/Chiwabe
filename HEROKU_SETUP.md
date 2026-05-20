# 🚀 Guia de Deploy no Heroku - Chiwabe Discord Bot

## ✅ Alterações Realizadas

As seguintes correções foram implementadas para compatibilidade com Heroku:

### 1. **system.properties** (Novo)
- Especifica Java 21 como versão obrigatória
- Heroku usará essa configuração durante o build

### 2. **Procfile** (Atualizado)
- Alterado de: `worker: java -cp target/classes:target/dependency/* com.chiwabe.ChiwabeDiscord`
- Para: `worker: java -jar target/chiwabe-chatbot-jar-with-dependencies.jar`
- Agora usa o JAR com todas as dependências incluídas

### 3. **ChiwabeDiscord.java** (Atualizado)
- Removida leitura do arquivo `.env` (não funciona no Heroku)
- Agora lê variáveis de ambiente usando `System.getenv()`
- Validação de variáveis obrigatórias com mensagens de erro claras
- Imports desnecessários removidos automaticamente

### 4. **data/usuarios/.gitkeep** (Novo)
- Garante que a pasta seja versionada no Git
- Históricos dos usuários serão criados automaticamente em runtime

---

## 🔧 Passos para Deploy no Heroku

### 1. **Fazer Commit das Alterações**
```bash
git add .
git commit -m "Configurar projeto para Heroku"
git push heroku main
```

### 2. **Configurar Variáveis de Ambiente no Heroku**

Acesse o dashboard do Heroku ou use a CLI:

```bash
heroku config:set API_KEY=sua_chave_openrouter_aqui
heroku config:set DISCORD_TOKEN=seu_token_discord_aqui
heroku config:set DISCORD_CLIENT_ID=seu_client_id_aqui
```

**Onde obter as credenciais:**
- **API_KEY (OpenRouter)**: https://openrouter.ai/keys
- **DISCORD_TOKEN**: https://discord.com/developers/applications
- **DISCORD_CLIENT_ID**: Também no Discord Developer Portal

### 3. **Verificar as Variáveis Configuradas**
```bash
heroku config
```

### 4. **Iniciar o Worker Dyno**
```bash
heroku ps:scale worker=1
```

### 5. **Monitorar os Logs**
```bash
heroku logs --tail
```

---

## 🐛 Troubleshooting

### "ERRO: Variável de ambiente API_KEY não configurada!"
- Verifique se você configurou a variável com `heroku config:set API_KEY=...`
- Use `heroku config` para confirmar

### "ERRO: Variável de ambiente DISCORD_TOKEN não configurada!"
- Verifique se você configurou a variável com `heroku config:set DISCORD_TOKEN=...`

### "ERRO: Variável de ambiente DISCORD_CLIENT_ID não configurada!"
- Verifique se você configurou a variável com `heroku config:set DISCORD_CLIENT_ID=...`

### Bot não responde no Discord
- Verifique se o worker dyno está rodando: `heroku ps`
- Verifique os logs: `heroku logs --tail`
- Confirme que o bot tem permissão para enviar mensagens no servidor

### Build falha com erro de Java
- Verifique se o `system.properties` está na raiz do projeto
- Confirme que está com conteúdo: `java.runtime.version=21`

---

## 📝 Notas Importantes

1. **Filesystem Efêmero**: Heroku apaga todos os arquivos quando o dyno reinicia. Os históricos dos usuários (`data/usuarios/`) serão perdidos. Para persistência, considere usar um banco de dados.

2. **Variáveis de Ambiente**: Todas as credenciais devem ser configuradas via `heroku config:set`, nunca commitar `.env` no repositório.

3. **Worker Dyno**: Este é um dyno sem web interface. Ele roda em background e não responde a requisições HTTP. É perfeito para um bot Discord.

4. **Custos**: Dynos gratuitos no Heroku foram descontinuados. Verifique os planos pagos disponíveis.

---

## ✨ Próximos Passos (Opcional)

Para melhorar a persistência de dados:
- Integrar com PostgreSQL (banco de dados do Heroku)
- Salvar históricos em um serviço de armazenamento em nuvem (AWS S3, etc.)
- Usar Redis para cache de conversas recentes

---

**Última atualização:** Maio de 2026
