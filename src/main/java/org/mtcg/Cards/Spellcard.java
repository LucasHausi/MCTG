package org.mtcg.Cards;

public class Spellcard extends Card{

    public Spellcard(float damage, int element)
    {
        super(damage, element);
    }
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if(this.element != Elements.Normal)
        {
            sb.append(this.element);
        }
        sb.append("Spell");
        return sb.toString();
    }
}
