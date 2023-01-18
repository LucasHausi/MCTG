package org.mtcg.service;

import org.mtcg.HTTP.HttpStatus;
import org.mtcg.HTTP.Response;
import org.mtcg.config.DataSource;
import org.mtcg.repository.InMemoryUserRepository;
import org.mtcg.repository.PostgresCardRepository;
import org.mtcg.repository.PostgresUserRepository;
import org.mtcg.repository.UserRepository;
import org.mtcg.user.User;

import java.util.HashMap;

public class UserService {
    private static InMemoryUserRepository inMemoryUserRepository;
    private PostgresUserRepository postgressUserRepository;

    public UserService(InMemoryUserRepository inMemoryUserRepository, PostgresUserRepository postgresUserRepository) {
        this.inMemoryUserRepository = inMemoryUserRepository;
        this.postgressUserRepository = postgresUserRepository;
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

    public synchronized void loginUser(User u1) {
        User u2;
        u2 = inMemoryUserRepository.getUserByUsername(u1.getUsername());
        if (u2 == null) {
            System.out.println("This user does not exist");
        } else {
            if (u2.getPassword().equals(u1.getPassword())) {
                System.out.println("Login was successful");
                inMemoryUserRepository.addAuthToken(u2);
            } else {
                System.out.println("Wrong password");
            }
        }
        //inMemoryUserRepository.printIMUserRepository();
    }

    public synchronized boolean checkAdminToken(String sentToken) {
        return inMemoryUserRepository.checkAdminToken(sentToken);
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

    public void scoreBoard() {
        HashMap<String, Integer> sortedMap = this.inMemoryUserRepository.getAllElos();
        sortedMap.forEach((username, elo) -> {
            System.out.println(username + " " + elo);
        });
    }
}
