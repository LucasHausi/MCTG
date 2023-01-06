package org.mtcg.repository;

import org.mtcg.config.DBConnector;
import org.mtcg.config.DataSource;
import org.mtcg.controller.TokenController;
import org.mtcg.user.User;

import java.util.*;

public class InMemoryUserRepository implements UserRepository{
    private HashMap<User, String> users = new HashMap<>();
    public void setUsers(List<User> users) {
        for(User u : users){
            this.users.put(u,null);
        }
    }
    public HashMap<String, Integer> getAllElos(){
        HashMap<String, Integer> tempMap = new HashMap<>();
        HashMap<String, Integer> sortedMap = new HashMap<>();
        ArrayList<Integer> list = new ArrayList<>();

        this.users.forEach((user,token)->{
            tempMap.put(user.getUsername(), user.getElo());
        });
        tempMap.forEach((user,elo)->{
            list.add(elo);
        });
        for (int tempElo : list) {
            for (Map.Entry<String, Integer> entry : tempMap.entrySet()) {
                if (entry.getValue().equals(tempElo)) {
                    sortedMap.put(entry.getKey(), tempElo);
                }
            }
        }
        return sortedMap;
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
    public boolean checkAdminToken(String sentToken){
        User u1 = this.getUserByUsername("admin");
        return sentToken.equals(this.users.get(u1));
    }
    public void initializeUserStack(){
        //for each user in memory grab the corr. cards and add them to the stack
        this.users.forEach((user,token) -> {
            //create one instance of CardResp because the main one of the card service is not created yet
            PostgresCardRepository temp = new PostgresCardRepository(DataSource.getInstance());
            user.setCardStack(temp.getCardsByUsername(user));
            //set null for garbage collector
            temp=null;
        });
    }
    //DEV Functions
    public void printIMUserRepository(){
        this.users.entrySet().forEach(entry -> {
            System.out.println(entry.getKey().getUsername() + " " + entry.getValue());
        });
    }
}
