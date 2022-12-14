package org.mtcg.Cards;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public abstract class Card implements Attackable {
    protected final float damage;
    protected final UUID id;
    protected Elements element;

    public Card(float damage, int element) {
        this.id = UUID.randomUUID();
        this.damage = damage;
        switch(element){
            case(0):
                this.element = Elements.Fire;
                break;
            case(1):
                this.element = Elements.Water;
                break;
            default:
                this.element = Elements.Normal;
        }
    }
    //Constructor when a admin creates Cards
    public Card(UUID id, float damage, Elements element){
        this.id = id;
        this.damage = damage;
        this.element = element;
    }
    public float getDamage() {
        return damage;
    }
    Effectiveness calcElementFactor(Card opponent)
    {
        //three cases
        /*
        water -> fire
        fire -> normal
        normal -> water
         */
        switch (this.element){
            case Fire:
            {
                if(opponent.element == Elements.Water){
                    return Effectiveness.not_effective;
                }
                else if(opponent.element == Elements.Normal)
                {
                    return Effectiveness.effective;
                }
                break;
            }
            case Water:
            {
                if(opponent.element == Elements.Normal){
                    return Effectiveness.not_effective;
                }
                else if(opponent.element == Elements.Fire)
                {
                    return Effectiveness.effective;
                }
                break;
            }
            default:
                if(opponent.element == Elements.Fire){
                    return Effectiveness.not_effective;
                }
                else if(opponent.element == Elements.Water)
                {
                    return Effectiveness.effective;
                }
        }
        return Effectiveness.no_effect;
    }
    boolean winsBattle(float selfDamage, float opponentDamage){
        if(selfDamage > opponentDamage){
            return true;
        }
        else{
            return false;
        }
    }

    private boolean checkSpecialMonsterCases(Card opponent) {
        /* Cases to consider for no Attack:
        Goblin -> Dragon
        Ork -> Wizzard
        Dragon -> FireElve
         */
        Monstercard tempA = (Monstercard) this;
        Monstercard tempB = (Monstercard) opponent;
        switch (tempA.getType()){
            case Goblin:
                if(tempB.getType()==Monsters.Dragon){
                    System.out.println("Goblin is to afraid to attack...");
                    return false;
                }
                break;
            case Ork:
                if(tempB.getType()==Monsters.Wizzard){
                    System.out.println("The Ork is controlled by Wizzard and has to abort the attack..");
                    return false;
                }
                break;
            case Dragon:
                if(tempB.getType()==Monsters.Elve && tempB.element == Elements.Fire){
                    System.out.println("The Dragon recognizes his former childhood friend and decides to spare him..");
                    return false;
                }
                break;
        }
        return true;
    }

    //could you use an override annotation here?
    public Card attack(Card opponent){
        //three cases pure monster fight, pure spell fight, mixed fight
        boolean selfIsMonster = this instanceof Monstercard;
        boolean opponentIsMonster = opponent instanceof Monstercard;

        System.out.print(this.toString().concat(" ("));
        System.out.print(this.damage+" Damage) vs "+ opponent.toString()+" ("+ opponent.damage+" Damage) => ");
        if(selfIsMonster && opponentIsMonster) {
            if(checkSpecialMonsterCases(opponent))
            {
                //compare no effects
                if(winsBattle(this.damage, opponent.damage)){
                    return this;
                }
                else {
                    return opponent;
                }
            }
            else {
                return opponent;
            }
        }
        // pure Spellfights and mixed fights
        else {
            Effectiveness e = this.calcElementFactor(opponent);
            boolean wins;
            System.out.print(this.damage+" VS "+opponent.damage+" -> ");
            switch (e){
                case effective:
                    wins = winsBattle(this.damage*2, (opponent.damage/2));
                    System.out.print(this.damage*2+" VS "+ opponent.damage/2+" => ");
                    break;
                case not_effective:
                    wins = winsBattle(this.damage/2, (opponent.damage*2));
                    System.out.print(this.damage/2+" VS "+ opponent.damage*2+" => ");
                    break;
                default:
                    wins = winsBattle(this.damage, opponent.damage);
            }
            if(wins){
                return this;
            }
            else {
                return opponent;
            }
        }
    }
}
