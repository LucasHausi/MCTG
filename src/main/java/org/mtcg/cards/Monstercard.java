package org.mtcg.cards;
import java.lang.*;
import java.util.UUID;

public class Monstercard extends Card{

    public Monsters getType() {
        return type;
    }

    private Monsters type;
    public Monstercard(float damage, int element, int type)
    {
        super(damage, element);
        switch (type){
            case(0):
                this.type = Monsters.Dragon;
                break;
            case(1):
                this.type = Monsters.Goblin;
                break;
            case(2):
                this.type = Monsters.Knight;
                break;
            case(3):
                this.type = Monsters.Ork;
                break;
            case(4):
                this.type = Monsters.Wizzard;
                break;
            case(5):
                this.type = Monsters.Kraken;
                break;
            default:
                this.type = Monsters.Elve;
                break;
        }
    }

    //Constructor when a admin creates Cards
    public Monstercard(UUID id, float damage, Elements element, Monsters type){
        super(id, damage, element);
        this.type = type;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("/* ");
        if(this.element != Elements.Normal)
        {
            sb.append(this.element);
        }
        sb.append(this.type);
        sb.append(" - Damage ");
        sb.append(this.damage);
        sb.append("*/ ");
        return sb.toString();
    }
    public String toPlainString(){
        return this.id+" "+this.type+" "+this.element+" "+this.damage;
    }

}
