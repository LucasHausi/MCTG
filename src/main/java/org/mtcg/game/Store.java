package org.mtcg.game;

import org.mtcg.cards.Card;
import org.mtcg.cards.Package;
import org.mtcg.repository.PostgresCardRepository;
import org.mtcg.repository.PostgresTradeRepository;
import org.mtcg.user.User;
import org.mtcg.util.Pair;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class Store {
    private List<Package> packages;
    private List<TradingDeal> deals;

    public Store() {
        this.packages = PostgresCardRepository.getAllPackages();
        this.deals = PostgresTradeRepository.getAllTrades();
    }

    public void addPackage(Package p) {
        this.packages.add(p);
    }
    public boolean notEmpty() {
        return !packages.isEmpty();
    }

    public Package getRandPackage() {
        //threadsafe version to get a rand number
        int randomElementIndex = ThreadLocalRandom.current().nextInt(this.packages.size());
        Package p = this.packages.get(randomElementIndex);
        this.packages.remove(p);
        return p;
    }

    public void printTradingDeals() {
        for (TradingDeal td : deals) {
            System.out.println(td);
        }
    }
    boolean dealExists(UUID id){
        return this.deals.stream()
                .filter(tempDeal -> id.equals(tempDeal.getId()))
                .findFirst()
                .isPresent();
    }
    public boolean addTradingDeal(TradingDeal td) {
        if(!dealExists(td.getId())){
            this.deals.add(td);
            return true;
        }
        else{
            return false;
        }
    }

    TradingDeal getTradingDeal(UUID tradeID) {
        return this.deals.stream()
                .filter(tradingDeal -> tradeID.equals(tradingDeal.getId()))
                .findFirst()
                .orElse(null);
    }

    public Card deleteTradingDeal(UUID tradeID) {
        TradingDeal temp = getTradingDeal(tradeID);
        if (temp != null) {
            this.deals.remove(temp);
            return temp.getSeller().getCardFromStack(temp.getOfferedCardID());
        } else {
            return null;
        }
    }

    public Pair<User, Card> tryToTradeCard(UUID id, String offeredCard, User buyer) {
        TradingDeal temp = getTradingDeal(id);
        if (temp != null) {
            User seller = temp.getSeller();
            if (seller != buyer) {
                Card wanted = seller.getCardFromStack(temp.getOfferedCardID());
                Card offered = buyer.getCardFromStack(offeredCard);
                if (wanted != null && offered != null) {
                    Requirement rq = temp.getRequirement();
                    if (offered.getDamage() >= rq.getMinDamage()) {
                        switch (rq.getType()) {
                            case ("monster"):
                                if (offered.isMonster()) {
                                    return new Pair<>(seller, wanted);
                                }
                                break;
                            default:
                                if (!offered.isMonster()) {
                                    return new Pair<>(seller, wanted);
                                }
                        }
                    }else {
                        System.out.println("The damage was to little");
                        return null;
                    }
                }else {
                    System.out.println("One cards was not valid");
                    return null;
                }
            }else{
                System.out.println("You cannot trade wth yourself!");
                return null;
            }
        }else{
            System.out.println("The trade was not found");
        }
        return null;
    }
}
