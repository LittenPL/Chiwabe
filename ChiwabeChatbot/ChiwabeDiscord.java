import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class ChiwabeDiscord {
    public static void main(String[] args) throws Exception{

        List<String> linhas = Files.readAllLines(Paths.get(".env"));
        String token = linhas.get(0).split("=")[1];
        System.out.println("Token carregado: " + token);

    }
}