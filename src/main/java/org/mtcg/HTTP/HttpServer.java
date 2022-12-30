package org.mtcg.HTTP;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.mtcg.cards.Package;
import org.mtcg.cards.SimpleCard;
import org.mtcg.cards.SimpleCardMapper;
import org.mtcg.config.DataSource;
import org.mtcg.repository.InMemoryUserRepository;
import org.mtcg.repository.PostgresCardRepository;
import org.mtcg.repository.PostgresUserRepository;
import org.mtcg.user.User;
import org.mtcg.controller.StoreController;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;


public class HttpServer {
    //replace later with DB connection to User-Database
    //HashMap<User, String> users = new HashMap<>();
    //new implementation with Respository
    private InMemoryUserRepository userRepository = new InMemoryUserRepository();
    private BlockingQueue<User> blockingQueue = new LinkedBlockingDeque<>();
    private PostgresUserRepository postgressUserRepository = new PostgresUserRepository(DataSource.getInstance());
    private PostgresCardRepository postgresCardRepository = new PostgresCardRepository(DataSource.getInstance());
    private StoreController storeController = new StoreController();
    public void start() {
        //load users from DB
        loadUsersFromDB();
        try (ServerSocket serverSocket = new ServerSocket(10001)) {
            while (true) {
                    final Socket socket = serverSocket.accept();
                    //System.out.println("New client Connected" + socket.getInetAddress());
                    ClientHandler clientHandler= new ClientHandler(socket,userRepository, postgressUserRepository, postgresCardRepository ,storeController, blockingQueue);
                    Thread t = new Thread(clientHandler);
                    t.start();
                    //System.out.println("Client "+ socket.getInetAddress() +" was served");
            }
        } catch (IOException ex1) {
            System.err.println(ex1);
        }
    }
    void loadUsersFromDB(){
        userRepository.setUsers(postgressUserRepository.getAllUsers());
        userRepository.initializeUserStack();
    }
}
