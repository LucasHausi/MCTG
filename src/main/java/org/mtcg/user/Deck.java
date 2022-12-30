package org.mtcg.user;

import org.mtcg.cards.Card;

import java.util.ArrayList;

public class Deck {
    private ArrayList<Card> cards;

    public Deck() {
        this.cards = new ArrayList<Card>();
    }

    public void addCard(Card c)
    {
        this.cards.add(c);
    }

    public Card getCard(int index){
        return this.cards.get(index);
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
