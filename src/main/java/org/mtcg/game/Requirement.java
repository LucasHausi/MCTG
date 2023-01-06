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
    public void setType(String type) {
        this.type = type;
    }
    @Override
    public String toString() {
        return "Requirement: "+this.type+" minDamage "+this.minDamage;
    }

    public float getMinDamage() {
        return minDamage;
    }

    public void setMinDamage(float minDamage) {
        this.minDamage = minDamage;
    }
}
