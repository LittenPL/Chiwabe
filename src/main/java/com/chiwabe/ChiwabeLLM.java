package com.chiwabe;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChiwabeLLM{
    
    /**
     * Interface para processar chunks de stream em tempo real
     */
    public interface StreamCallback {
        /**
         * Chamado quando um novo chunk é recebido
         * @param chunk Conteúdo do chunk a ser exibido
         */
        void onChunk(String chunk);
        
        /**
         * Chamado quando o stream é completado com sucesso
         * @param fullResponse Resposta completa acumulada
         */
        void onComplete(String fullResponse);
        
        /**
         * Chamado quando ocorre um erro durante o stream
         * @param e Exceção que ocorreu
         */
        void onError(Exception e);
    }
    public static String Chiwabe(String key, String userId, String LLM, String system, String pergunta, boolean dev_mode, String contexto) throws Exception{

        String resposta = null;
        //Pergunta vazia, retornar resposta vazia sem chamar a API
        if(pergunta == null || pergunta.trim().isEmpty()){
            return "O que foi? Fala alguma coisa aí!";
        }

        //Iniciando cliente
        HttpClient client = HttpClient.newHttpClient();
        
        //======================Carregando histórico======================
        StringBuilder historico = Memoria.carregarHistorico(userId);
        if(dev_mode && historico.length() > 0){
            System.out.println("Memória ativa carregada: " + Memoria.contarMensagens(historico) + " mensagens");
        }

        //======================Carregando resumos======================
        StringBuilder resumos = Memoria.carregarResumosAntigos(userId);
        if(dev_mode && resumos.length() > 0){
            System.out.println("Resumos de memória carregados: " + Memoria.contarMensagens(resumos) + " resumos");
        }

            //======================Conectando======================
            try{
                //Escapar caracteres especiais para não quebrar o JSON
                String perguntaSafe = pergunta
                    .replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r")
                    .replace("\t", "\\t");

                //Adicionar pergunta ao histórico
                if(historico.length() > 0){ historico.append(",");
                historico.append("{\"role\":\"user\",\"content\":\"").append(perguntaSafe).append("\"}");
                }


                //====================== Montando corpo da LLM ======================
                String jsonBody = """
                    {
                      "model": "%s",
                      "messages": [
                        {"role": "system", "content": "%s"},
                        {"role": "user", "content": "%s"},
                        %s,
                        %s
                      ],
                      "max_tokens": 2048,
                      "include_reasoning": true,
                      "temperature": 0.8
                    }
                    """.formatted(LLM, system, contexto, resumos.toString(), historico.toString());

                HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://openrouter.ai/api/v1/chat/completions"))
                    .header("Authorization", "Bearer " + key)
                    .header("Content-Type", "application/json")
                    .header("HTTP-Referer", "http://localhost")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                if(dev_mode){
                    System.out.println("Status: " + response.statusCode());
                    System.out.println("Response: " + response.body());
                }

                //======================Erros======================
                if (response.statusCode() == 429) {
                    System.out.println("⚠️ Limite atingido!");}
                if(response.statusCode() != 200){
                    System.out.println("Erro ao conectar");}
                if(response.statusCode() == 400){
                    System.out.println("Erro no JSON, verifique: ");
                    System.out.println(jsonBody);}

                //======================Resposta======================
                String bruto = response.body();
                Pattern pattern = Pattern.compile("\"content\"\\s*:\\s*\"(.*?)\"\\s*,\\s*\"refusal\"", Pattern.DOTALL);
                Matcher matcher = pattern.matcher(bruto);
                if (matcher.find()) {
                    // Filtrando a resposta
                    resposta = matcher.group(1)
                                        .replace("\\n", "\n")
                                        .replace("\\\"", "\"")
                                        .replace("\\\\", "\\")
                                        .replaceAll("[^\\p{L}\\p{N}\\p{P}\\p{Z}\\n]", "")
                                        .trim();

                    //Adicionar resposta ao histórico
                    String respostaSafe = matcher.group(1)
                                        .replace("\\", "\\\\")
                                        .replace("\"", "\\\"")
                                        .replace("\n", "\\n")
                                        .replace("\r", "\\r")
                                        .replace("\t", "\\t");
                    historico.append(",{\"role\":\"assistant\",\"content\":\"").append(respostaSafe).append("\"}");
                } else {
                    System.out.println("Não foi possível localizar a resposta");
                }

                //======================Tokens======================
                Pattern tokensPattern = Pattern.compile("\"total_tokens\"\\s*:\\s*(\\d+)");
                Matcher tokensMatcher = tokensPattern.matcher(bruto);
                if(tokensMatcher.find()){
                    String tokens = tokensMatcher.group(1);
                    System.out.println("""

                    Tokens: """ + tokens);
                } else {
                    System.out.println("Não foi possível identificar os tokens");
                }

                //=============Salvando memória======================
                Memoria.salvarNaMemoria(userId, historico, dev_mode);
                Memoria.processarHistoricoAoEncerrar(userId, historico, client, key, dev_mode);

            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Erro ao conectar");
            }
            return resposta;
    }

    /**
     * Versão com stream da função Chiwabe
     * Exibe a resposta em tempo real conforme os chunks chegam
     * @param key Chave de autenticação da API
     * @param userId ID do usuário para persistência de memória
     * @param LLM Modelo de LLM a usar
     * @param system Prompt do sistema
     * @param pergunta Pergunta do usuário
     * @param dev_mode Se true, exibe informações de debug
     * @param contexto Contexto adicional para a IA
     * @param callback Interface para processar chunks em tempo real
     */
    public static void ChiwabeStream(String key, String userId, String LLM, String system, String pergunta, boolean dev_mode, String contexto, StreamCallback callback) throws Exception {
        
        //Pergunta vazia, retornar sem chamar a API
        if(pergunta == null || pergunta.trim().isEmpty()){
            callback.onChunk("O que foi? Fala alguma coisa aí!");
            callback.onComplete("O que foi? Fala alguma coisa aí!");
            return;
        }

        //Iniciando cliente
        HttpClient client = HttpClient.newHttpClient();
        
        //======================Carregando histórico======================
        StringBuilder historico = Memoria.carregarHistorico(userId);
        if(dev_mode && historico.length() > 0){
            System.out.println("Memória ativa carregada: " + Memoria.contarMensagens(historico) + " mensagens");
        }

        //======================Carregando resumos======================
        StringBuilder resumos = Memoria.carregarResumosAntigos(userId);
        if(dev_mode && resumos.length() > 0){
            System.out.println("Resumos de memória carregados: " + Memoria.contarMensagens(resumos) + " resumos");
        }

        try {
            //Escapar caracteres especiais para não quebrar o JSON
            String perguntaSafe = pergunta
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");

            //Adicionar pergunta ao histórico
            if(historico.length() > 0){ 
                historico.append(",");
            }
            historico.append("{\"role\":\"user\",\"content\":\"").append(perguntaSafe).append("\"}");

            //====================== Montando corpo da LLM ======================
            String jsonBody = """
                {
                  "model": "%s",
                  "messages": [
                    {"role": "system", "content": "%s"},
                    {"role": "user", "content": "%s"},
                    %s,
                    %s
                  ],
                  "max_tokens": 2048,
                  "include_reasoning": true,
                  "temperature": 0.8,
                  "stream": true
                }
                """.formatted(LLM, system, contexto, resumos.toString(), historico.toString());

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://openrouter.ai/api/v1/chat/completions"))
                .header("Authorization", "Bearer " + key)
                .header("Content-Type", "application/json")
                .header("HTTP-Referer", "http://localhost")
                .header("Accept", "text/event-stream")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

            if(dev_mode){
                System.out.println("Iniciando stream...");
            }

            //======================Recebendo stream======================
            HttpResponse<java.io.InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
            
            if(dev_mode){
                System.out.println("Status: " + response.statusCode());
            }

            //======================Erros======================
            if (response.statusCode() == 429) {
                callback.onError(new Exception("⚠️ Limite atingido!"));
                return;
            }
            if(response.statusCode() != 200){
                callback.onError(new Exception("Erro ao conectar. Status: " + response.statusCode()));
                return;
            }

            //======================Processando stream======================
            StringBuilder respostaCompleta = new StringBuilder();
            processarStreamSSE(response.body(), callback, respostaCompleta, dev_mode);

            //Adicionar resposta ao histórico
            String respostaSafe = respostaCompleta.toString()
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
            historico.append(",{\"role\":\"assistant\",\"content\":\"").append(respostaSafe).append("\"}");

            //=============Salvando memória======================
            Memoria.salvarNaMemoria(userId, historico, dev_mode);
            Memoria.processarHistoricoAoEncerrar(userId, historico, client, key, dev_mode);

            callback.onComplete(respostaCompleta.toString());

        } catch (Exception e) {
            e.printStackTrace();
            callback.onError(e);
        }
    }

    /**
     * Processa a resposta em formato Server-Sent Events (SSE)
     * Extrai chunks de conteúdo e chama o callback para cada um
     * @param inputStream Stream de entrada da resposta
     * @param callback Callback para processar chunks
     * @param respostaCompleta StringBuilder para acumular a resposta completa
     * @param dev_mode Se true, exibe informações de debug
     */
    private static void processarStreamSSE(java.io.InputStream inputStream, StreamCallback callback, StringBuilder respostaCompleta, boolean dev_mode) {
        try (java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(inputStream))) {
            String linha;
            while ((linha = reader.readLine()) != null) {
                if (linha.startsWith("data: ")) {
                    String jsonData = linha.substring(6); // Remove "data: "
                    
                    // Ignorar linhas de keep-alive
                    if (jsonData.equals("[DONE]")) {
                        break;
                    }
                    
                    try {
                        // Extrair conteúdo do JSON usando regex
                        String conteudo = extrairConteudoDoChunk(jsonData);
                        if (conteudo != null && !conteudo.isEmpty()) {
                            // Decodificar escape sequences
                            conteudo = conteudo
                                .replace("\\n", "\n")
                                .replace("\\\"", "\"")
                                .replace("\\\\", "\\");
                            
                            callback.onChunk(conteudo);
                            respostaCompleta.append(conteudo);
                            
                            if(dev_mode){
                                System.out.println("[DEBUG] Chunk: " + conteudo);
                            }
                        }
                    } catch (Exception e) {
                        if(dev_mode){
                            System.out.println("[DEBUG] Erro ao processar chunk: " + e.getMessage());
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Erro ao processar stream: " + e.getMessage(), e);
        }
    }

    /**
     * Extrai o campo "content" de um chunk JSON do stream
     * @param jsonData String JSON do chunk
     * @return Conteúdo extraído ou null se não encontrado
     */
    private static String extrairConteudoDoChunk(String jsonData) {
        try {
            // Padrão para extrair o campo "content" do JSON
            Pattern pattern = Pattern.compile("\"content\"\\s*:\\s*\"(.*?)\"(?=,|\\})");
            Matcher matcher = pattern.matcher(jsonData);
            
            if (matcher.find()) {
                return matcher.group(1);
            }
        } catch (Exception e) {
            // Ignorar erros de parsing
        }
        return null;
    }
}
