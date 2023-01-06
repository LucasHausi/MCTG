package org.mtcg.controller;

import org.mtcg.cards.Card;
import org.mtcg.cards.Package;
import org.mtcg.game.Store;
import org.mtcg.game.TradingDeal;
import org.mtcg.repository.PostgresCardRepository;
import org.mtcg.user.User;
import org.mtcg.util.Pair;

import java.util.UUID;

public class StoreController {
    private Store store = new Store();

    public void sellPackage(User u) {
        //check if there are packages to buy
        if (store.notEmpty()) {
            //check if user has enough money
            if (u.hasEnoughMoney()) {
                //get and remove Package and let the user acquire the package
                Package p = store.getRandPackage();
                u.acquirePackage(p);
                PostgresCardRepository.addOwnerToPackage(p,u);
            } else {
                System.out.println("The user " + u.getUsername() + " has not enough money!");
            }
        } else {
            System.out.println("The store is empty!");
        }
    }
    public void checkTradingDeals(){
        store.printTradingDeals();
    }
    public boolean addTradingDeal(TradingDeal td){
       return store.addTradingDeal(td);
    }
    public Card deleteTradingDeal(UUID tradeID){
        return store.deleteTradingDeal(tradeID);
    }
    public Pair<User, Card> tryToTradeCard(UUID id, String offeredCard, User buyer){
        return store.tryToTradeCard(id,offeredCard, buyer);
    }
    public void addPackage(Package p){
        store.addPackage(p);
    }
}
