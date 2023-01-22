package org.mtcg.service;

import org.mtcg.HTTP.HttpStatus;
import org.mtcg.HTTP.Response;
import org.mtcg.cards.Card;
import org.mtcg.cards.Package;
import org.mtcg.config.DataSource;
import org.mtcg.controller.StoreController;
import org.mtcg.game.TradingDeal;
import org.mtcg.repository.PostgresCardRepository;
import org.mtcg.repository.PostgresTradeRepository;
import org.mtcg.user.User;
import org.mtcg.util.Pair;

import java.util.UUID;

public class CardService {
    private final PostgresCardRepository postgresCardRepository;
    private final StoreController storeController;
    private final PostgresTradeRepository tradeRepository;


    public CardService() {
        this.postgresCardRepository = new PostgresCardRepository(DataSource.getInstance());
        this.tradeRepository = new PostgresTradeRepository(DataSource.getInstance());
        this.storeController =  new StoreController();


    }

    public synchronized Response addPackage(Package p) {
        Response response = new Response();
        //when the package is successfully  added to DB
        if(postgresCardRepository.addPackage(p)){
            storeController.addPackage(p);
            response.setHttpStatus(HttpStatus.CREATED);
            response.setBody("Package and cards successfully created");
            return response;
        }else{
            response.setHttpStatus(HttpStatus.CONFLICT);
            response.setBody("At least one card in the packages already exists");
            return response;
        }
    }
    public synchronized Response sellPackage(User u1){
        return storeController.sellPackage(u1);
    }
    public synchronized Response tryToTradeCard(UUID id, String offeredCard, User buyer){
        Response response = new Response();
        //get the wanted card + the seller
        Pair<User, Card> seller_n_Card = storeController.tryToTradeCard(id, offeredCard, buyer);
        //eg if the trade can be done
        if(seller_n_Card!=null){
            User seller = seller_n_Card.left();
            Card wanted = seller_n_Card.right();
            Card offered = buyer.getCardFromStack(offeredCard);

            seller.removeCardFromStack(wanted);
            buyer.removeCardFromStack(offered);

            wanted.unlock();
            postgresCardRepository.setLock(false, wanted.getId().toString());

            seller.addCardToStack(offered);
            buyer.addCardToStack(wanted);
            postgresCardRepository.setOwner(offered,seller);
            postgresCardRepository.setOwner(wanted,buyer);
            System.out.println("The trade was done");
            response.setHttpStatus(HttpStatus.OK);
            response.setBody("Trading deal successfully executed");
        }else{
            System.out.println("The trade could not be done!");
            response.setHttpStatus(HttpStatus.CONFLICT);
            response.setBody("The provided deal ID was not found or the offered card is not owned by the user, or the requirements are not me");
        }
        return response;

    }
    public synchronized Response deleteTradingDeal(UUID tradeID){
        Response response = new Response();
        Card offered = storeController.deleteTradingDeal(tradeID);
        if (offered != null){
            tradeRepository.deleteTrade(tradeID);
            postgresCardRepository.setLock(false, offered.getId().toString());
            System.out.println("The trading deal was deleted");
            response.setHttpStatus(HttpStatus.OK);
            response.setBody("Trading deal successfully deleted");
        }else{
            System.out.println("The trading deal was not found!");
            response.setHttpStatus(HttpStatus.NOT_FOUND);
            response.setBody("The provided deal ID was not found.");
        }
        return response;
    }
    public synchronized Response addTradingDeal(TradingDeal td){
        Response response = new Response();
        User seller = td.getSeller();
        if(seller.lockCard(td.getOfferedCardID())){
            if(storeController.addTradingDeal(td)){
                tradeRepository.addTrade(td);
                postgresCardRepository.setLock(true, td.getOfferedCardID());
                System.out.println("Trading Deal was added!");
                response.setHttpStatus(HttpStatus.CREATED);
                response.setBody("Trading deal successfully created");
                return response;
            }else{
                System.out.println("The deal exists already");
                response.setHttpStatus(HttpStatus.CONFLICT);
                response.setBody("A deal with this deal ID already exists.");
                return response;
            }
        }
        else {
            System.out.println("The card to trade could not be locked");
            response.setHttpStatus(HttpStatus.FORBIDDEN);
            response.setBody("The deal contains a card that is not owned by the user or locked in the deck.");
            return response;
        }
    }
    public Response checkTradingDeals(){
        return storeController.checkTradingDeals();
    }
}
