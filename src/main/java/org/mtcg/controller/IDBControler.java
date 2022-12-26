package org.mtcg.controller;

import org.mtcg.User.User;

import java.util.UUID;

public interface IDBControler {
    public void createUser(User u);
    public void checkIfUserExists(User u);
    public void getCard(UUID id);
}
