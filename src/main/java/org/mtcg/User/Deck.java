package org.mtcg.User;

import org.mtcg.Cards.Card;

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
    public void printDeck(){
        for(Card c : cards)
        {
            System.out.println(c);
        }
    }
    //DEV Functions
}
