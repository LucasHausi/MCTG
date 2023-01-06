package org.mtcg.service;

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

    public synchronized void addPackage(Package p) {
        storeController.addPackage(p);
        postgresCardRepository.addPackage(p);
    }
    public synchronized void sellPackage(User u1){
        storeController.sellPackage(u1);
    }
    public synchronized void tryToTradeCard(UUID id, String offeredCard, User buyer){
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
        }else{
            System.out.println("The trade could not be done!");
        }


    }
    public synchronized void deleteTradingDeal(UUID tradeID){
        Card offered = storeController.deleteTradingDeal(tradeID);
        if (offered != null){
            tradeRepository.deleteTrade(tradeID);
            postgresCardRepository.setLock(false, offered.getId().toString());
            System.out.println("The trading deal was deleted");
        }else{
            System.out.println("The trading deal was not found!");
        }
    }
    public synchronized void addTradingDeal(TradingDeal td){
        User seller = td.getSeller();
        if(seller.lockCard(td.getOfferedCardID())){
            if(storeController.addTradingDeal(td)){
                tradeRepository.addTrade(td);
                postgresCardRepository.setLock(true, td.getOfferedCardID());
                System.out.println("Trading Deal was added!");
            }else{
                System.out.println("The deal exists already");
            }
        }
        else {
            System.out.println("The card to trade could not be locked");
        }


    }
    public void checkTradingDeals(){
        storeController.checkTradingDeals();
    }
}
