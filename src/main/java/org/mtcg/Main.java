package org.mtcg;

import org.mtcg.Game.Game;
import org.mtcg.HTTP.HttpServer;

public class Main {
    public static void main(String[] args) {

        HttpServer server = new HttpServer();
        server.start();
        //Game round = new Game();
        //round.startGame();
    }
}