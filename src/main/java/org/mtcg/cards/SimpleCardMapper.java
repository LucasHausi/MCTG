package org.mtcg.cards;

import java.util.ArrayList;
import java.util.UUID;

public class SimpleCardMapper {

    Elements getElementFromName(String name){
        if(name.contains("Water")){
            return Elements.Water;
        } else if (name.contains("Fire")) {
            return Elements.Fire;
        } else if (name.contains("Regular")) {
            return Elements.Normal;
        }else{
            return Elements.Normal;
        }
    }
    Monsters getMonstertypeFromName(String name){
        if(name.contains("Ork")){
            return Monsters.Ork;
        } else if (name.contains("Dragon")) {
            return Monsters.Goblin;
        } else if (name.contains("Goblin")) {
            return Monsters.Goblin;
        } else if (name.contains("Wizzard")) {
            return Monsters.Wizzard;
        }else if (name.contains("Knight")) {
            return Monsters.Knight;
        } else if (name.contains("Kraken")) {
            return Monsters.Kraken;
        }else if (name.contains("Elve")) {
            return Monsters.Elve;
        }else{
            return null;
        }
    }
    public Package mapSimpleCardsToCards(SimpleCard[] simpleCards){

        Package p = new Package();
        ArrayList<Card> tempCards= new ArrayList<>();

        for(SimpleCard sc : simpleCards)
        {
            Elements element = getElementFromName(sc.getName());
            Monsters monsterType = getMonstertypeFromName(sc.getName());
            UUID id = sc.getId();
            float damage = sc.getDamage();
            if(monsterType == null){
                tempCards.add(new Spellcard(id, damage , element));
            }else{
                tempCards.add(new Monstercard(id, damage , element, monsterType));
            }
        }
        p.setCards(tempCards);
        return p;
    }
}
