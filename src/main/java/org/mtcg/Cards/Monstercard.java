package org.mtcg.Cards;
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

        public String toString() {
        StringBuilder sb = new StringBuilder();
        if(this.element != Elements.Normal)
        {
            sb.append(this.element);
        }
        sb.append(this.type);
        return sb.toString();
    }

}