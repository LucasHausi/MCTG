package org.mtcg.controller;

import org.mtcg.user.User;

import java.util.UUID;

public interface IDBControler {
    public void createUser(User u);
    public void checkIfUserExists(User u);
    public void getCard(UUID id);
}
