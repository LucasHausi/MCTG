package org.mtcg.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.json.JSONArray;
import org.json.JSONObject;
import org.mtcg.HTTP.HttpStatus;
import org.mtcg.HTTP.Response;
import org.mtcg.cards.Card;
import org.mtcg.cards.Package;
import org.mtcg.game.Store;
import org.mtcg.game.TradingDeal;
import org.mtcg.repository.PostgresCardRepository;
import org.mtcg.user.User;
import org.mtcg.util.Pair;

import java.util.List;
import java.util.UUID;

public class StoreController {
    private Store store = new Store();

    public Response sellPackage(User u) {
        Response response = new Response();
        //check if there are packages to buy
        if (store.notEmpty()) {
            //check if user has enough money
            if (u.hasEnoughMoney()) {
                //get and remove Package and let the user acquire the package
                Package p = store.getRandPackage();
                u.acquirePackage(p);
                PostgresCardRepository.addOwnerToPackage(p,u);
                ObjectMapper objectMapper = new ObjectMapper();
                try {
                    String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(p.getCards());
                    response.setHttpStatus(HttpStatus.OK);
                    response.setBody(json);
                } catch(Exception e) {
                    System.err.println("Error when converting Package to JSON string");
                }
            } else {
                System.out.println("The user " + u.getUsername() + " has not enough money!");
                response.setHttpStatus(HttpStatus.FORBIDDEN);
                response.setBody("Not enough money for buying a card package");
                return response;
            }
        } else {
            System.out.println("The store is empty!");
            response.setHttpStatus(HttpStatus.NOT_FOUND);
            response.setBody("No card package available for buying");
            return response;
        }
        return response;
    }
    public Response checkTradingDeals(){
        store.printTradingDeals();
        Response response = new Response();
        List<TradingDeal> deals= store.getDeals();
        if(!deals.isEmpty()){
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                JSONArray resArray = new JSONArray();
                for (TradingDeal d : deals){
                    JSONObject jsonDeal = new JSONObject();
                    jsonDeal.put("Id", d.getId().toString());
                    jsonDeal.put("CardToTrade", d.getOfferedCardID().toString());
                    jsonDeal.put("Type", d.getRequirement().getType().toString());
                    jsonDeal.put("MinimumDamage", d.getRequirement().getMinDamage());
                    resArray.put(jsonDeal);
                }
                response.setHttpStatus(HttpStatus.OK);
                response.setBody(resArray.toString());
            } catch(Exception e) {
                System.err.println("Error when converting dealList to JSON string" + e);
                response.setHttpStatus(HttpStatus.INTERNAL_SERVER_ERROR);
                response.setBody("Internal error");
            }
        }else{
            response.setHttpStatus(HttpStatus.OK);
            response.setBody("The request was fine, but there are no trading deals available");
        }
        return response;
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
