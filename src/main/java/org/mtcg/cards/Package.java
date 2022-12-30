package org.mtcg.cards;

import java.util.ArrayList;

public class Package {
    private ArrayList<Card> cards;

    public Package() {
        this.cards = new ArrayList<>();
    }

    public ArrayList<Card> getCards() {
        return cards;
    }
    public void addCard(Card c){
        this.cards.add(c);
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
