package org.mtcg.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.mtcg.HTTP.HttpStatus;
import org.mtcg.HTTP.Response;
import org.mtcg.config.DataSource;
import org.mtcg.game.BattleLog;
import org.mtcg.repository.*;
import org.mtcg.user.User;

import java.util.HashMap;

public class UserService {
    private static InMemoryUserRepository inMemoryUserRepository;
    private PostgresUserRepository postgressUserRepository;
    private PostgresBattleRepository postgresBattleRepository;

    public UserService() {
        this.inMemoryUserRepository = new InMemoryUserRepository();
        this.postgressUserRepository = new PostgresUserRepository(DataSource.getInstance());
        this.postgresBattleRepository = new PostgresBattleRepository(DataSource.getInstance());
        //load users from DB
        inMemoryUserRepository.setUsers(postgressUserRepository.getAllUsers());
        inMemoryUserRepository.initializeUserStack();
    }

    public static UserRepository getIMUR() {
        return inMemoryUserRepository;
    }

    public synchronized Response addUser(User u1) {
        Response response = new Response();
        User u2 = inMemoryUserRepository.getUserByUsername(u1.getUsername());
        if (u2 == null) {
            inMemoryUserRepository.addUser(u1);
            postgressUserRepository.addUser(u1);
            System.out.println("The user " +
                               u1.getUsername() + " was created");

            response.setBody("User successfully created");
            response.setHttpStatus(HttpStatus.CREATED);
            return response;
        } else {
            System.out.println("This user does already exist!");
            response.setBody("User with same username already registered");
            response.setHttpStatus(HttpStatus.CONFLICT);
            return response;
        }
    }

    public synchronized Response loginUser(User u1) {
        User u2;
        Response response = new Response();
        u2 = inMemoryUserRepository.getUserByUsername(u1.getUsername());
        if (u2 == null) {
            System.out.println("This user does not exist");
        } else {
            if (u2.getPassword().equals(u1.getPassword())) {
                System.out.println("Login was successful");
                String token = inMemoryUserRepository.addAuthToken(u2);
                response.setBody("User login successful\n"+token);
                response.setHttpStatus(HttpStatus.OK);
                return response;
            } else {
                System.out.println("Wrong password");
            }
        }
        response.setBody("Invalid username/password provided");
        response.setHttpStatus(HttpStatus.UNAUTHORIZED);
        return response;
        //inMemoryUserRepository.printIMUserRepository();
    }

    public synchronized boolean checkAdminToken(String sentToken) {
        return inMemoryUserRepository.checkAdminToken(sentToken);
    }

    public synchronized void persistElo(User u1){
        postgressUserRepository.updateElo(u1);
    }
    public User getUserByUsername(String username) {
        return inMemoryUserRepository.getUserByUsername(username);
    }

    public User authenticateUser(String sentToken) {
        if (sentToken != null) {
            if (inMemoryUserRepository.checkAuthToken(sentToken)) {
                return inMemoryUserRepository.getUserByAuthToken(sentToken);
            } else {
                System.out.println("Wrong authentication token");
                return null;
            }
        } else {
            System.out.println("No authentication token");
            return null;
        }
    }
    public Response scoreBoard() {
        Response response = new Response();
        HashMap<String, Integer> sortedMap = this.inMemoryUserRepository.getAllElos();
        sortedMap.forEach((username, elo) -> {
            System.out.println(username + " " + elo);
        });
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(sortedMap);
            response.setHttpStatus(HttpStatus.OK);
            response.setBody(json);
        } catch(Exception e) {
            System.err.println("Error when converting Stack to JSON string");
            response.setHttpStatus(HttpStatus.INTERNAL_SERVER_ERROR);
            response.setBody("Error when converting Stack to JSON string");
        }
        return response;
    }
    public void persistBattle(BattleLog battleLog, User winner){
        postgresBattleRepository.addBattle(battleLog,winner);
    }
}
