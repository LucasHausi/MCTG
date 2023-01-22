package org.mtcg.game;

public class Requirement {
    private String type;
    private float minDamage;

    public Requirement(String type, float minDamage) {
        this.type = type;
        this.minDamage = minDamage;
    }

    public String getType() {
        return type;
    }
    @Override
    public String toString() {
        return "Requirement: "+this.type+" minDamage "+this.minDamage;
    }

    public float getMinDamage() {
        return minDamage;
    }

}
