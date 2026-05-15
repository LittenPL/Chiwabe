package com.chiwabe;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;

public class ChiwabeDiscord {
    public static void main(String[] args) throws Exception{

        //======================Token e clientId======================
        List<String> linhas = Files.readAllLines(Paths.get(".env"));
        String token = linhas.get(1).split("=")[1];
        String clientId = linhas.get(2).split("=")[1];

        //======================Iniciando bot======================
        JDA jda = JDABuilder.createDefault(token).build();
        jda.awaitReady();
        jda.getPresence().setActivity(Activity.playing("com.chiwabe.Celebro"));


    }
}
