import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Gerenciador de histórico de conversa com persistência em arquivo
 * e resumo automático de conversas antigas
 */
public class Memoria {

    /**
     * Carrega a memoria ativa do arquivo memoria_ativa.json
     */
    public static StringBuilder carregarHistorico(){
        try {
            String conteudo = new String(Files.readAllBytes(Paths.get("memoria_ativa.json")));
            // Remove os colchetes do array JSON e retorna apenas o conteúdo
            conteudo = conteudo.trim();
            if(conteudo.startsWith("[") && conteudo.endsWith("]")){
                conteudo = conteudo.substring(1, conteudo.length() - 1);
            }
            return new StringBuilder(conteudo);
        } catch (Exception e) {
            // Arquivo não existe ou erro ao ler - começa vazio
            return new StringBuilder();
        }
    }

    /**
     * Salva o histórico completo em memoria.json
     */
    public static void salvarNaMemoria(StringBuilder historico){
        try {
            String conteudo = "[" + historico.toString() + "]";
            Files.write(Paths.get("memoria.json"), conteudo.getBytes());
            System.out.println("Memória salva");
        } catch (Exception e) {
            System.out.println("Erro ao salvar na memoria: " + e.getMessage());
        }
    }

    /**
     * Conta quantas mensagens há na memoria ativa
     */
    public static int contarMensagens(StringBuilder historico){
        int count = 0;
        String str = historico.toString();
        for(int i = 0; i < str.length(); i++){
            if(str.charAt(i) == '{') count++;
        }
        return count;
    }

    /**
     * Processa o histórico ao encerrar:
     * - Se > 100 mensagens: resume as antigas e mantém as recentes
     * - Resumos são salvos em memoria_resumida.json (nunca são resumidos novamente)
     * - Memória ativa contém apenas últimas mensagens
     */
    public static void processarHistoricoAoEncerrar(StringBuilder historico, HttpClient client, String key, boolean dev_mode){
        try {
            int totalMensagens = contarMensagens(historico);
            
            if(totalMensagens > 100){
                System.out.println("Limite da memória ativa atingido (" + totalMensagens + "/100 mensagens)");
                System.out.println("Comprimindo conversas antigas...");
                
                // Extrair primeiras 50 mensagens para resumir
                String primeiras50 = extrairPrimeirasNMensagens(historico, 50);
                
                // Chamar IA para resumir
                String resumo = resumirComIA(primeiras50, client, key, dev_mode);
                
                // Salvar resumo em memoria_resumida.json (histórico de resumos)
                if(resumo != null && !resumo.isEmpty()){
                    adicionarResumoAoHistorico(resumo);
                }
                
                // Extrair últimas 40 mensagens (SEM resumos anteriores)
                String ultimas40 = extrairUltimasNMensagens(historico, 40);
                
                // Montar nova memoria ativa: APENAS últimas 40 (sem resumos)
                String conteudo = "[" + ultimas40 + "]";
                Files.write(Paths.get("memoria_ativa.json"), conteudo.getBytes());
                System.out.println("Memória ativa comprimida (" + contarMensagens(new StringBuilder(ultimas40)) + " mensagens recentes)");
                
            } else {
                // Histórico pequeno - copia tudo
                String conteudo = "[" + historico.toString() + "]";
                Files.write(Paths.get("memoria_ativa.json"), conteudo.getBytes());
                System.out.println("Memória ativa salva (" + totalMensagens + " mensagens)");
            }
            
        } catch (Exception e) {
            System.out.println("Erro ao processar histórico: " + e.getMessage());
        }
    }

    /**
     * Adiciona um resumo ao arquivo de histórico de resumos
     * Resumos nunca são resumidos novamente - apenas acumulados
     */
    public static void adicionarResumoAoHistorico(String resumo){
        try {
            String conteudoAtual = "";
            try {
                conteudoAtual = new String(Files.readAllBytes(Paths.get("memoria_resumida.json")));
                // Remove colchetes
                if(conteudoAtual.startsWith("[") && conteudoAtual.endsWith("]")){
                    conteudoAtual = conteudoAtual.substring(1, conteudoAtual.length() - 1);
                }
            } catch (Exception e) {
                // Arquivo não existe - começa vazio
                conteudoAtual = "";
            }
            
            // Adicionar novo resumo
            StringBuilder novoConteudo = new StringBuilder();
            if(conteudoAtual.length() > 0){
                novoConteudo.append(conteudoAtual).append(",");
            }
            novoConteudo.append(resumo);
            
            // Salvar
            String conteudo = "[" + novoConteudo.toString() + "]";
            Files.write(Paths.get("memoria_resumida.json"), conteudo.getBytes());
            
        } catch (Exception e) {
            System.out.println("Erro ao adicionar resumo ao histórico: " + e.getMessage());
        }
    }

    /**
     * Carrega todos os resumos do arquivo memoria_resumida.json
     * Estes resumos serão incluídos no contexto da IA
     */
    public static StringBuilder carregarResumosAntigos(){
        try {
            String conteudo = new String(Files.readAllBytes(Paths.get("memoria_resumida.json")));
            // Remove os colchetes do array JSON e retorna apenas o conteúdo
            conteudo = conteudo.trim();
            if(conteudo.startsWith("[") && conteudo.endsWith("]")){
                conteudo = conteudo.substring(1, conteudo.length() - 1);
            }
            return new StringBuilder(conteudo);
        } catch (Exception e) {
            // Arquivo não existe ou erro ao ler - começa vazio
            return new StringBuilder();
        }
    }

    /**
     * Extrai as primeiras N mensagens do histórico
     */
    public static String extrairPrimeirasNMensagens(StringBuilder historico, int n){
        String str = historico.toString();
        int count = 0;
        int inicio = 0;
        int fim = 0;
        
        for(int i = 0; i < str.length(); i++){
            if(str.charAt(i) == '{'){
                if(count == 0) inicio = i;
                count++;
                if(count == n){
                    // Encontrar o fechamento da n-ésima mensagem
                    int braces = 1;
                    for(int j = i + 1; j < str.length(); j++){
                        if(str.charAt(j) == '{') braces++;
                        if(str.charAt(j) == '}') braces--;
                        if(braces == 0){
                            fim = j + 1;
                            break;
                        }
                    }
                    break;
                }
            }
        }
        
        if(fim > inicio){
            String resultado = str.substring(inicio, fim);
            // Remove vírgula final se existir
            if(resultado.endsWith(",")){
                resultado = resultado.substring(0, resultado.length() - 1);
            }
            return resultado;
        }
        return "";
    }

    /**
     * Extrai as últimas N mensagens do histórico
     */
    public static String extrairUltimasNMensagens(StringBuilder historico, int n){
        String str = historico.toString();
        int count = 0;
        int inicio = 0;
        
        // Contar total de mensagens
        int total = 0;
        for(int i = 0; i < str.length(); i++){
            if(str.charAt(i) == '{') total++;
        }
        
        int primeiraAExtrair = total - n;
        if(primeiraAExtrair < 0) primeiraAExtrair = 0;
        
        count = 0;
        for(int i = 0; i < str.length(); i++){
            if(str.charAt(i) == '{'){
                if(count == primeiraAExtrair){
                    inicio = i;
                    break;
                }
                count++;
            }
        }
        
        if(inicio > 0){
            String resultado = str.substring(inicio);
            // Remove vírgula inicial se existir
            if(resultado.startsWith(",")){
                resultado = resultado.substring(1);
            }
            return resultado;
        }
        return str;
    }

    /**
     * Chama a IA para resumir as mensagens antigas
     */
    public static String resumirComIA(String mensagensAntiga, HttpClient client, String key, boolean dev_mode){
        try {
            // Montar prompt para resumir
            String prompt = "Resuma BREVEMENTE (2-8 frases) esta conversa passada para uma IA:\\n" + mensagensAntiga;
            String promptSafe = prompt
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");

            String jsonBody = """
                {
                  "model": "openrouter/owl-alpha",
                  "messages": [
                    {"role": "user", "content": "%s"}
                  ],
                  "temperature": 0.4
                }
                """.formatted(promptSafe);

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://openrouter.ai/api/v1/chat/completions"))
                .header("Authorization", "Bearer " + key)
                .header("Content-Type", "application/json")
                .header("HTTP-Referer", "http://localhost")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            
            if(response.statusCode() == 200){
                String bruto = response.body();
                Pattern pattern = Pattern.compile("\"content\"\\s*:\\s*\"(.*?)\"\\s*,\\s*\"refusal\"", Pattern.DOTALL);
                Matcher matcher = pattern.matcher(bruto);
                
                if(matcher.find()){
                    String resumo = matcher.group(1)
                        .replace("\\n", "\n")
                        .replace("\\\"", "\"")
                        .replace("\\\\", "\\")
                        .trim();
                    
                    // Escapar para JSON
                    String resumoSafe = resumo
                        .replace("\\", "\\\\")
                        .replace("\"", "\\\"")
                        .replace("\n", "\\n")
                        .replace("\r", "\\r")
                        .replace("\t", "\\t");
                    
                    System.out.println("Comprimido para: " + resumo);
                    
                    // Retornar como objeto JSON
                    return "{\"role\":\"system\",\"content\":\"[RESUMO DE CONVERSA ANTERIOR] " + resumoSafe + "\"}";
                }
            }
        } catch (Exception e) {
            System.out.println("Erro ao comprimir: " + e.getMessage());
        }
        return null;
    }
}
