package org.mtcg.game;

import org.mtcg.user.User;

import java.util.UUID;

public class TradingDeal {
    private UUID id ;
    private String offeredCardID;
    private User seller;
    private Requirement requirement;

    public TradingDeal(UUID id,String offeredCardID, User seller, Requirement requirement) {
        this.id = id;
        this.offeredCardID = offeredCardID;
        this.seller = seller;
        this.requirement = requirement;
    }

    public User getSeller() {
        return seller;
    }

    public String getOfferedCardID() {
        return offeredCardID;
    }

    public Requirement getRequirement() {
        return requirement;
    }

    public UUID getId() {
        return id;
    }

    @Override
    public String toString() {
        return "ID: "+this.id + "\nOffered Card: "+this.offeredCardID+"\nSeller: "+ this.seller.getNickname()+"\n"+requirement.toString();
    }
}
