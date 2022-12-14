package org.mtcg.Cards;

import java.util.ArrayList;

public class Package {
    private ArrayList<Card> cards;

    public ArrayList<Card> getCards() {
        return cards;
    }

    public void setCards(ArrayList<Card> cards) {
        this.cards = cards;
    }

    //DEV Function
    public void print(){
        for(Card c : cards){
            System.out.println("ID:  " + c.id);
        }
    }
}
