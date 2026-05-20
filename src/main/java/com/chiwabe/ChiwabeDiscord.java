package com.chiwabe;

import java.util.Scanner;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class ChiwabeDiscord extends ListenerAdapter {
    
    private static String apiKey;
    private static boolean dev_mode = false;
    private static boolean livre = false;
    private static boolean memoriaServidor = true;
    private static final String LLM = "nvidia/nemotron-3-nano-30b-a3b:free";
    private static final String SYSTEM_PROMPT = Memoria.lerArquivo("data/config/systemDiscord.txt");
    private static final String CABO_21_CONTEUDO = Memoria.lerArquivo("data/config/conteudoCABO21.txt");
    private static final int MAX_MESSAGE_LENGTH = 2000;
    private static final int MAX_TOKENS = 4200;

    //====================== Abrir manualmente ========================
    private static boolean manual = false;

    public static void main(String[] args) throws Exception {

        //======================Entering DevMode======================
        if(manual){
        Scanner insert = new Scanner(System.in, "UTF-8");
        System.out.print("DepureMode y/n: ");
        String veri = insert.nextLine();
        if(veri.equalsIgnoreCase("y")){dev_mode = true;}}

        //====================Modo livre: responde a todas as mensagens=======================
        if(manual){
        Scanner insert = new Scanner(System.in, "UTF-8");
        System.out.print("Modo livre y/n: ");
        String veri = insert.nextLine();
        if(veri.equalsIgnoreCase("y")){livre = true;}}

        //====================Modo memória de server====================
        if(manual){
        Scanner insert = new Scanner(System.in, "UTF-8");
        System.out.print("Modo memória de servidor y/n: ");
        String veri = insert.nextLine();
        if(veri.equalsIgnoreCase("n")){memoriaServidor = false;}}

        //======================Lendo credenciais das variáveis de ambiente======================
        apiKey = System.getenv("API_KEY");
        String discordToken = System.getenv("DISCORD_TOKEN");
        String clientId = System.getenv("DISCORD_CLIENT_ID");

        // Validar se as variáveis foram carregadas
        if (apiKey == null || apiKey.isEmpty()) {
            System.err.println("ERRO: Variável de ambiente API_KEY não configurada!");
            System.exit(1);
        }
        if (discordToken == null || discordToken.isEmpty()) {
            System.err.println("ERRO: Variável de ambiente DISCORD_TOKEN não configurada!");
            System.exit(1);
        }
        if (clientId == null || clientId.isEmpty()) {
            System.err.println("ERRO: Variável de ambiente DISCORD_CLIENT_ID não configurada!");
            System.exit(1);
        }

        if(dev_mode){
            System.out.println("API Key carregada: " + apiKey.substring(0, Math.min(10, apiKey.length())) + "...");
            System.out.println("Discord Token carregado: " + discordToken.substring(0, Math.min(10, discordToken.length())) + "...");
            System.out.println("Client ID: " + clientId);
        }

        //======================Iniciando bot Discord======================
        JDA jda = JDABuilder.createDefault(discordToken)
            .addEventListeners(new ChiwabeDiscord())
            .build();
        
        jda.awaitReady();
        System.out.println("Bot Chiwabe conectado ao Discord!");
        jda.getPresence().setActivity(Activity.playing("Pronta pra ser consultada!"));
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        // Ignorar mensagens do próprio bot
        if (event.getAuthor().isBot()) return;

        // Ignorar mensagens em DM (apenas responder em servidores)
        if (!event.isFromGuild()) return;

        // Ignorar mensagens que não mencionam o bot
        if(!livre){
        if (!event.getMessage().getContentRaw().contains("<@!" + event.getJDA().getSelfUser().getId() + ">") &&
            !event.getMessage().getContentRaw().contains("<@" + event.getJDA().getSelfUser().getId() + ">")) {
            return;}}

        String mensagem = event.getMessage().getContentRaw().replace("<@!" + event.getJDA().getSelfUser().getId() + ">", "")
                                        .replace("<@" + event.getJDA().getSelfUser().getId() + ">", "")
                                        .trim();
        String userId = event.getAuthor().getId();
        String userName = event.getAuthor().getName();

        if(dev_mode){
            System.out.println("[" + userName + " (" + userId + ")]: (" + mensagem + ")");
        }
        
        //Memória por servidor=============================
        if(memoriaServidor){
            userId = event.getGuild().getId();} // Adicionar ID do servidor para criar memória separada por servidor

        //Colocando o nome na mensagem
        StringBuilder mensagemComNome = new StringBuilder();
        String prefixo = userName + ": ";
        mensagemComNome.append(prefixo).append(mensagem);

        // Mostrar que o bot está digitando
        event.getChannel().sendTyping().queue();

        try {
            // Chamar a IA com o ID do usuário como userId
            String resposta = ChiwabeLLM.Chiwabe(apiKey, userId, LLM, SYSTEM_PROMPT, mensagemComNome.toString(), dev_mode, CABO_21_CONTEUDO);

            if (resposta == null || resposta.isEmpty()) {
                event.getChannel().sendMessage("ERRO. Não entendi direito. Por favor repita a pergunta").queue();
                return;
            }

            // Dividir resposta se for muito grande (limite do Discord é 2000 caracteres)
            if (resposta.length() > MAX_MESSAGE_LENGTH) {
                String[] partes = dividirMensagem(resposta, MAX_MESSAGE_LENGTH);
                for (String parte : partes) {
                    event.getChannel().sendMessage(parte).queue();
                }
            } else {
                event.getChannel().sendMessage(resposta).queue();
            }

        } catch (Exception e) {
            e.printStackTrace();
            event.getChannel().sendMessage("Erro ao processar mensagem: " + e.getMessage()).queue();
        }
    }

    /**
     * Divide uma mensagem em partes menores para respeitar o limite do Discord
     */
    private static String[] dividirMensagem(String mensagem, int tamanhoMaximo) {
        if (mensagem.length() <= tamanhoMaximo) {
            return new String[]{mensagem};
        }

        java.util.List<String> partes = new java.util.ArrayList<>();
        String[] palavras = mensagem.split(" ");
        StringBuilder parte = new StringBuilder();

        for (String palavra : palavras) {
            if ((parte.length() + palavra.length() + 1) > tamanhoMaximo) {
                if (parte.length() > 0) {
                    partes.add(parte.toString());
                    parte = new StringBuilder();
                }
            }
            if (parte.length() > 0) {
                parte.append(" ");
            }
            parte.append(palavra);
        }

        if (parte.length() > 0) {
            partes.add(parte.toString());
        }

        return partes.toArray(new String[0]);
    }
}
