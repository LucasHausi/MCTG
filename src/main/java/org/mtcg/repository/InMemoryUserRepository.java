package org.mtcg.repository;

import org.mtcg.user.User;

import java.util.HashMap;
import java.util.Map;

public class InMemoryUserRepository implements UserRepository{
    private HashMap<User, String> users = new HashMap<>();

    public void addUser(User u, String token){
        users.put(u, token);
        System.out.println(u.getUsername() + " was created");
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
    //DEV Functions
    public void printIMUserRepository(){
        this.users.entrySet().forEach(entry -> {
            System.out.println(entry.getKey().getUsername() + " " + entry.getValue());
        });
    }
}
