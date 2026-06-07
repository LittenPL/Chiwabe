package com.chiwabe;

import java.nio.file.Files;
import java.nio.file.Paths;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class EnvInsert extends Application{

    TextArea insert;
    Label aviso;

    @Override
    public void start(Stage primaryStage) {
        try {
            
            // Criar interface
            BorderPane root = criarInterface();
            
            // Criar cena
            Scene scene = new Scene(root, 500, 150);
            scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
            
            // Configurar stage
            primaryStage.setTitle("Criador de Chave");
            primaryStage.setScene(scene);
            primaryStage.setOnCloseRequest(e -> System.exit(0));
            primaryStage.show();
            
        } catch (Exception e) {}
    }

    private BorderPane criarInterface(){
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #bbbbbb;");

        VBox interfaceEnv = new VBox(0);
        interfaceEnv.setPadding(new Insets(20));
        root.setCenter(interfaceEnv);

        // TEXTO INICIAL PARA ORIENTAÇÃO
        //.
        Label instruction = new Label();
        instruction.setText("INSIRA A SUA CHAVE API AQUI:");
        instruction.setStyle("-fx-text-fill: #1c1c1c; -fx-font-size: 20; -fx-font-weight: bold;");
        instruction.setAlignment(Pos.CENTER_LEFT);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        // ÁREA PARA INSERIR A CHAVE
        //.
        insert = new TextArea();
        insert.setWrapText(true);
        insert.setPrefRowCount(1);
        insert.setStyle("-fx-control-inner-background: #cdcdcd; -fx-text-fill: #181818; -fx-font-family: 'Consolas'; -fx-font-size: 16;");
        insert.setPromptText("Digite aqui...");
        insert.addEventFilter(javafx.scene.input.KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == javafx.scene.input.KeyCode.ENTER) {
                    definirEnv();
                    e.consume();}});

        // AVISO DO ESTADO DA CHAVE
        //.
        aviso = new Label();
        aviso.setText("A chave já foi configurada.");
        aviso.setStyle("-fx-text-fill: #cc0000; -fx-font-size: 10; -fx-font-weight: bold;");
        aviso.setVisible(false);

        interfaceEnv.getChildren().addAll(instruction, spacer, insert, aviso);

        return root;
    }

    private void definirEnv(){
        try{
        java.nio.file.Path env = Paths.get("ChiwabeChatbot/.env");
        if(!Files.exists(env)){
            Files.write(Paths.get("ChiwabeChatbot/.env"), ("API_KEY=" + insert.getText()).getBytes());
            atualizarAviso();
            insert.clear();
        }else{
            aviso.setVisible(true);
        }
        }catch (Exception e){}
    }

    private void atualizarAviso(){
        aviso.setText("A chave foi configurada com sucesso!");
        aviso.setStyle("-fx-text-fill: #44cd00; -fx-font-size: 10; -fx-font-weight: bold;");
        aviso.setVisible(true);
    }

    public static void main(String[] args) {
        launch(args);
    }
}