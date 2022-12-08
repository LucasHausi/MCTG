package org.mtcg.game;

import org.mtcg.cards.Card;
import org.mtcg.cards.Monstercard;
import org.mtcg.cards.Spellcard;

import java.util.ArrayList;
import java.util.Random;

public class Stack {

    private ArrayList<Card> cards;
    public Stack() {
        Random rand = new Random();
        this.cards = new ArrayList<Card>();
        for(int i=0; i < 10; i++)
        {
            int randDamage = rand.nextInt(1, 30);
            int randElement = rand.nextInt(0,3);
            int randMonster = rand.nextInt(0,7);
            int spellOrMonster = rand.nextInt(0,2);

            Card c;
            switch (spellOrMonster){
                case(1):
                    c = new Spellcard(randDamage,randElement);
                    break;
                default:
                    c = new Monstercard(randDamage,randElement, randMonster);
                    break;
            }
            this.addCard(c);
            // set to Null for GarbageCollector
            c = null;
        }
    }

    public void addCard(Card c){
        this.cards.add(c);
    }

    public ArrayList<Card> getCards() {
        return cards;
    }

    public void setCards(ArrayList<Card> cards) {
        this.cards = cards;
    }
    public Card getCard(int index)
    {
        return this.cards.get(index);
    }

    // DEV Functions
    public void printStack(){
        for(int i=0; i < this.cards.size(); i++){
            StringBuilder sb = new StringBuilder();
            sb.append(i);
            sb.append(": ");
            sb.append(this.cards.get(i).toString());
            sb.append(" with damage ");
            sb.append(this.cards.get(i).getDamage());
            System.out.println(sb);
        }
    }
    public void removeCard(Card c)
    {
        if(this.cards.size()>1)
        {
            this.cards.remove(c);
        }
    }
    public int getStackSize()
    {
        return this.cards.size();
    }
}
