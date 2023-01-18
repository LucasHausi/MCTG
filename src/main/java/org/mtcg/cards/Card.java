package org.mtcg.cards;

import java.util.UUID;

public abstract class Card implements Attackable {
    protected final float damage;

    protected final UUID id;
    protected Elements element;
    protected boolean lock;

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
    //Constructor when an admin creates Cards
    public Card(UUID id, float damage, Elements element, boolean lock){
        this.id = id;
        this.damage = damage;
        this.element = element;
        this.lock = lock;
    }
    public float getDamage() {
        return damage;
    }

    public boolean isLock() {
        return lock;
    }

    public void lock() {
        this.lock = true;
    }
    public void unlock(){this.lock = false;};

    public Elements getElement() {
        return element;
    }

    public UUID getId() {
        return id;
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
    //0->self wins, 1->draw , 2->opponent wins
    int winsBattle(float selfDamage, float opponentDamage){
        if(selfDamage > opponentDamage){
            return 0;
        }
        else if (selfDamage == opponentDamage){
            return 1;
        }else {
            return 2;
        }
    }
    public boolean isMonster(){
        return this instanceof Monstercard;
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
    // return Values 0 -> abbort attack, 1->instant death for opponent, 3-> instant death for self, 2->normal attack
    private int checkSpecialSpellCases(Card opponent){
        Monstercard tempMonster;
        Spellcard tempSpell;
        if(this.isMonster()){
            tempMonster = (Monstercard) this;
            tempSpell = (Spellcard) opponent;

        }else if(opponent.isMonster()){
            tempMonster = (Monstercard) opponent;
            tempSpell = (Spellcard) this;
        }else{
            return 2;
        }
        //only two cases important here
        if(tempMonster.getType() == Monsters.Kraken){
            System.out.println("A Kranken is immune against spells");
            return 0;
        } else if (tempMonster.getType()==Monsters.Knight && tempSpell.getElement()==Elements.Water) {
            System.out.println("The Knight drowns instantly");
            if(this == tempMonster){
                return 3;
            }else{
                return 1;
            }

        }
        return 2;
    }
    public abstract String toPlainString();
    public abstract String toFancyString();
    //could you use an override annotation here?
    public Card attack(Card opponent){
        //three cases pure monster fight, pure spell fight, mixed fight
        boolean selfIsMonster = this instanceof Monstercard;
        boolean opponentIsMonster = opponent instanceof Monstercard;
        int wins;

        System.out.print(this.toString().concat(" ("));
        System.out.print(this.damage+" Damage) vs "+ opponent.toString()+" ("+ opponent.damage+" Damage) => ");
        // pure monster fight
        if(selfIsMonster && opponentIsMonster) {
            //if false is returned here then there is no attack
            if(checkSpecialMonsterCases(opponent))
            {
                //compare no effects
                wins = winsBattle(this.damage, opponent.damage);
            }
            else {
                return null;
            }
        }
        // pure Spellfights and mixed fights
        else {
                int caseResult = this.checkSpecialSpellCases(opponent);
                if(caseResult == 2){
                Effectiveness e = this.calcElementFactor(opponent);
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
                }else if(caseResult==1){
                    wins = 0;
                } else if (caseResult==3) {
                    wins = 2;
                } else{
                    return null;
                }

            }
        switch (wins){
            case 0:
                return this;
            case 2:
                return opponent;
            default:
                return null;
        }
    }
}
