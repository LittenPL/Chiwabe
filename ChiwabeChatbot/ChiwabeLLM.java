import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChiwabeLLM{
    public static String Chiwabe(String key, String LLM, String system, String pergunta, boolean dev_mode) throws Exception{

        String resposta = null;

        //Iniciando cliente
        HttpClient client = HttpClient.newHttpClient();
        
        //======================Carregando histórico======================
        StringBuilder historico = Memoria.carregarHistorico();
        if(dev_mode && historico.length() > 0){
            System.out.println("Memória ativa carregada: " + Memoria.contarMensagens(historico) + " mensagens");
        }

        //======================Carregando resumos======================
        StringBuilder resumos = Memoria.carregarResumosAntigos();
        if(dev_mode && resumos.length() > 0){
            System.out.println("Resumos de memória carregados: " + Memoria.contarMensagens(resumos) + " resumos");
        }

        //======Fechar o programa=====
            if(pergunta.equalsIgnoreCase("tchau")){
                Memoria.salvarNaMemoria(historico);
                Memoria.processarHistoricoAoEncerrar(historico, client, key, dev_mode);
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
                if(historico.length() > 0) historico.append(",");
                historico.append("{\"role\":\"user\",\"content\":\"").append(perguntaSafe).append("\"}");


                //====================== Montando corpo da LLM ======================
                String jsonBody = """
                    {
                      "model": "%s",
                      "messages": [
                        {"role": "system", "content": "%s"},
                        %s,
                        %s
                      ],
                      "max_tokens": 2048,
                      "include_reasoning": true,
                      "temperature": 0.8
                    }
                    """.formatted(LLM, system, resumos.toString(), historico.toString());

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

            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Erro ao conectar");
            }
            return resposta;
    }
}