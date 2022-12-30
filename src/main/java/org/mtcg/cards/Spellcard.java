package org.mtcg.cards;

import java.util.UUID;

public class Spellcard extends Card {

    public Spellcard(float damage, int element) {
        super(damage, element);
    }

    //Constructor when a admin creates Cards
    public Spellcard(UUID id, float damage, Elements element) {
        super(id, damage, element);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (this.element != Elements.Normal) {
            sb.append(this.element);
        }

        sb.append("Spell");
        return sb.toString();
    }

    @Override
    public String toFancyString() {
        StringBuilder sb = new StringBuilder();
        sb.append("/* ");
        if (this.element != Elements.Normal) {
            sb.append(this.element);
        }
        sb.append("Spell - Damage ");
        sb.append(this.damage);
        sb.append(" */");
        return sb.toString();
    }

    @Override
    public String toPlainString() {
        return this.id + " Spell " + this.element + " " + this.damage;
    }
}
