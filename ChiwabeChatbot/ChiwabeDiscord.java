import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class ChiwabeDiscord {
    public static void main(String[] args) throws Exception{

        List<String> linhas = Files.readAllLines(Paths.get(".env"));
        String token = linhas.get(1).split("=")[1];
        String clientId = linhas.get(2).split("=")[1];

    }
}