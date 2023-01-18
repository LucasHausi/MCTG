package org.mtcg.HTTP;

import org.mtcg.config.DataSource;
import org.mtcg.game.QueueHandler;
import org.mtcg.repository.InMemoryUserRepository;
import org.mtcg.repository.PostgresUserRepository;
import org.mtcg.service.CardService;
import org.mtcg.service.UserService;
import org.mtcg.user.User;
import org.mtcg.util.Pair;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;


public class HttpServer {
    private final BlockingQueue<User> bQPlayers = new LinkedBlockingDeque<>();
    private final BlockingQueue<Pair<User,User>> bQGameResults = new LinkedBlockingDeque<>();
    public void start() {
        QueueHandler gameHandler = new QueueHandler(bQPlayers,bQGameResults);
        Thread gameHandling = new Thread(gameHandler);
        gameHandling.start();

        UserService userService =
            new UserService(new InMemoryUserRepository(), new PostgresUserRepository(DataSource.getInstance()));
        CardService cardService = new CardService();

        try (ServerSocket serverSocket = new ServerSocket(10001)) {
            while (true) {
                    final Socket socket = serverSocket.accept();
                    //System.out.println("New client Connected" + socket.getInetAddress());
                    ClientHandler clientHandler= new ClientHandler(socket, bQPlayers,bQGameResults, userService, cardService);
                    Thread t = new Thread(clientHandler);
                    t.start();
                    //System.out.println("Client "+ socket.getInetAddress() +" was served");
            }
        } catch (IOException ex1) {
            System.err.println(ex1);
        }
    }
}
