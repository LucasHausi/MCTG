package org.mtcg.cards;

public abstract class Card implements Attackable {
    protected final float damage;

    protected Elements element;

    public Card(float damage, int element) {
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
    //could you use an override annotation here?
    public Card attack(Card opponent){
        //three cases pure monster fight, pure spell fight, mixed fight
        boolean selfIsMonster = this instanceof Monstercard;
        boolean opponentIsMonster = opponent instanceof Monstercard;

        System.out.print("PlayerA: "+this.toString().concat(" ("));
        System.out.print(this.damage+" Damage) vs PlayerB: "+ opponent.toString()+" ("+ opponent.damage+" Damage) => ");
        if(selfIsMonster && opponentIsMonster) {
            //compare no effects
            if(winsBattle(this.damage, opponent.damage)){
                return this;
            }
            else {
                return opponent;
            }
        } /*else if (selfIsMonster || opponentIsMonster) {
            Effectiveness e = this.calcElementFactor(opponent);
            boolean wins=false;
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
                    System.out.print(this.damage+" Damage) vs "+ opponent.toString()+" ("+ opponent.damage+") => ");
            }
            if(wins){
                return this;
            }
            else {
                return opponent;
            }
        }*/
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
