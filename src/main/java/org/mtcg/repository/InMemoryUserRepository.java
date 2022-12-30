package org.mtcg.repository;

import org.mtcg.controller.TokenController;
import org.mtcg.user.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryUserRepository implements UserRepository{
    private HashMap<User, String> users = new HashMap<>();

    public void setUsers(List<User> users) {
        for(User u : users){
            this.users.put(u,null);
        }
    }
    public void addAuthToken(User u){
        users.put(u, TokenController.generateNewAuthToken());
    }
    public void addUser(User u){
        boolean exists = false;
        for(User tempUser : users.keySet()){
            if(tempUser.getUsername().equals(u.getUsername())){
                exists = true;
            }
        }
        if(!exists){
            users.put(u, null);
        }
    }
    @Override
    public User getUserByUsername(String username) {
        return users.entrySet().stream()
                .filter(tempUser -> username.equals(tempUser.getKey().getUsername()))
                .map(Map.Entry::getKey).findFirst()
                .orElse(null);
    }

    public User getUserByAuthToken(String sentToken) {
        return this.users.entrySet().stream()
                .filter(tempUser -> sentToken.equals(tempUser.getValue()))
                .map(Map.Entry::getKey).findFirst()
                .orElse(null);
    }

    public boolean checkAuthToken(String sentToken) {
        return this.users.containsValue(sentToken);
    }
    public void initializeUserStack(){
        //for each user in memory grab the corr. cards and add them to the stack
        this.users.forEach((user,token) -> {
            user.setCardStack(PostgresCardRepository.getCardsByUsername(user));
            //user.printStack();
        });
    }
    //DEV Functions
    public void printIMUserRepository(){
        this.users.entrySet().forEach(entry -> {
            System.out.println(entry.getKey().getUsername() + " " + entry.getValue());
        });
    }
}
