package org.mtcg.controller;

import org.mtcg.cards.Package;
import org.mtcg.game.Store;
import org.mtcg.repository.PostgresCardRepository;
import org.mtcg.user.User;

public class StoreController {
    public static Store store = new Store();

    public void sellPackage(User u) {
        //check if there are packages to buy
        if (store.notEmpty()) {
            //check if user has enough money
            if (u.hasEnoughMoney()) {
                //get and remove Package and let the user acquire the package
                Package p = store.getRandPackage();
                u.acquirePackage(p);
                PostgresCardRepository.addOWNER(p,u);
            } else {
                System.out.println("The user " + u.getUsername() + " has not enough money!");
            }
        } else {
            System.out.println("The store is empty!");
        }
    }
}
