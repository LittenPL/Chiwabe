import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Celebro{
    public static void main(String[] args) throws Exception{
        Scanner insert = new Scanner(System.in, "UTF-8");
        boolean in_dev = true;
        boolean dev_mode = false;
        String key = null;

        //======================Entering DevMode======================
        if(in_dev){
            System.out.print("DepureMode y/n: ");
            String veri = insert.nextLine();
            if(veri.equalsIgnoreCase("y")){dev_mode = true;}
            if(veri.equalsIgnoreCase("n")){dev_mode = false;}}

        //======================Verificando a chave======================
        try {
            List<String> linhas = Files.readAllLines(Paths.get(".env"));
            key = linhas.get(0).split("=")[1];
        } catch (Exception e) {
            System.out.println("Erro ao ler arquivo .env");
        }
        if(dev_mode){
            System.out.println("Loaded: " + key);
        }

        //Iniciando cliente
        HttpClient client = HttpClient.newHttpClient();

        //======================Carregando histórico======================
        StringBuilder historico = Memoria.carregarHistorico();
        if(dev_mode && historico.length() > 0){
            System.out.println("Histórico recente carregado: " + Memoria.contarMensagens(historico) + " mensagens");
        }

        //Carregando resumos antigos
        StringBuilder resumosAntigos = Memoria.carregarResumosAntigos();
        if(dev_mode && resumosAntigos.length() > 0){
            System.out.println("Resumos antigos carregados: " + Memoria.contarMensagens(resumosAntigos) + " resumos");
        }

        while(true){
            //======================Perguntando======================
            System.out.print("U: ");
            String pergunta = insert.nextLine();
            //Fechar o programa
            if(pergunta.equalsIgnoreCase("tchau")){
                Memoria.salvarNaMemoria(historico);
                Memoria.processarHistoricoAoEncerrar(historico, client, key, dev_mode);
                break;
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

                // Montar mensagens: resumos antigos + histórico recente
                String mensagensCompletas = "";
                if(resumosAntigos.length() > 0){
                    mensagensCompletas = resumosAntigos.toString() + "," + historico.toString();
                } else {
                    mensagensCompletas = historico.toString();
                }

                String jsonBody = """
                    {
                      "model": "nvidia/nemotron-3-nano-30b-a3b:free",
                      "messages": [
                        {"role": "system", "content": "Seu nome é Chiwabe. Você é direta, proativa, e não usa muita formatação"},
                        %s
                      ],
                      "include_reasoning": false,
                      "temperature": 0.7
                    }
                    """.formatted(mensagensCompletas);

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
                    System.out.println("Erro ao conectar");
                    continue;}
                if(response.statusCode() == 400){
                    System.out.println("Erro no JSON, verifique: ");
                    System.out.println(jsonBody);}

                //======================Resposta======================
                String bruto = response.body();
                Pattern pattern = Pattern.compile("\"content\"\\s*:\\s*\"(.*?)\"\\s*,\\s*\"refusal\"", Pattern.DOTALL);
                Matcher matcher = pattern.matcher(bruto);
                if (matcher.find()) {
                    // Filtrando a resposta
                    String resposta = matcher.group(1)
                                        .replace("\\n", "\n")
                                        .replace("\\\"", "\"")
                                        .replace("\\\\", "\\")
                                        .replaceAll("[^\\p{L}\\p{N}\\p{P}\\p{Z}\\n]", "")
                                        .trim();
                    System.out.println("""
                    
                    Chiwabe: """);
                    System.out.println(resposta);

                    //Adicionar resposta da IA ao histórico
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
        }
        insert.close();
    }
}
