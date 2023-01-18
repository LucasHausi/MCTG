package org.mtcg.user;

import org.mtcg.cards.Card;

import java.util.ArrayList;
import java.util.Random;

public class Deck {
    private ArrayList<Card> cards;

    public Deck() {
        this.cards = new ArrayList<>();
    }

    public void addCard(Card c)
    {
        this.cards.add(c);
    }

    public Card getCardToAttack(){
        Random rand = new Random();
        int randIndex = rand.nextInt(0, this.cards.size());
        return this.cards.get(randIndex);
    }
    public boolean removeCardFromDeck(Card c) {
        return this.removeCard(c);
    }
    public int getDeckSize()
    {
        return this.cards.size();
    }
    public boolean removeCard(Card c)
    {
        if(this.cards.size()>1)
        {
            this.cards.remove(c);
            return true;
        }
        return false;
    }
    public void clearDeck(){
        this.cards.clear();
    }
    public boolean isEmpty(){
        return this.cards.isEmpty();
    }
    public void printDeck(String format){
        if(this.cards.isEmpty()){
            System.out.println("The deck is currently empty");
        }else{
            for(Card c : cards)
            {
                switch (format){
                    case ("plain"):
                        System.out.println(c.toPlainString());
                        break;
                    default:
                        System.out.println(c.toFancyString());
                }

            }
        }

    }
    //DEV Functions
}
