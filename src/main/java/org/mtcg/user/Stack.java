package org.mtcg.user;

import org.mtcg.cards.Card;

import java.util.ArrayList;
import java.util.UUID;

public class Stack {

    private ArrayList<Card> cards;
    public Stack() {
        cards = new ArrayList<>();
        /*Random rand = new Random();
        this.cards = new ArrayList<Card>();
        for(int i=0; i < 10; i++)
        {
            int randDamage = rand.nextInt(1, 30);
            int randElement = rand.nextInt(0,3);
            int randMonster = rand.nextInt(0,2);
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
        }*/
    }

    public boolean cardExists(UUID id){
        boolean exists = false;
        for(Card c : this.cards){
            if(c.getId().equals(id)){
                exists = true;
            }
        }
        return exists;
    }
    public boolean addCard(Card c){
        //check if the id of the card is not taken already
        if(cardExists(c.getId()) == false){
            this.cards.add(c);
            return true;
        }else{
            //Cant add card because the id already exists
            return false;
        }

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

    public  Card getCard(UUID id){
        return this.cards.stream()
                .filter(tempCard -> id.equals(tempCard.getId()))
                .findFirst()
                .orElse(null);
    }
    // DEV Functions
    public void printStack(){
        for(int i=0; i < this.cards.size(); i++){
            StringBuilder sb = new StringBuilder();
            sb.append("ID: ");
            sb.append(this.cards.get(i).getId());
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
