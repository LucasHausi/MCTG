package org.mtcg;

import org.mtcg.HTTP.HttpServer;
import org.mtcg.config.DataSource;
import org.mtcg.repository.PostgresCardRepository;
import org.mtcg.repository.PostgresUserRepository;
import org.mtcg.user.User;

public class Main {
    public static void main(String[] args) {

        HttpServer server = new HttpServer();
        server.start();
    }
}