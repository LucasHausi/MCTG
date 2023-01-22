package org.mtcg.cards;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

//helper class to map the curl input
public class SimpleCard {
    private UUID id;
    private String name;
    private float damage;
    @JsonCreator
    public SimpleCard(@JsonProperty("Id") UUID id, @JsonProperty("Name") String name, @JsonProperty("Damage") float damage){
        this.id = id;
        this.damage = damage;
        this.name = name;
    }

    @Override
    public String toString() {
        return this.id+" "+this.name+" "+this.damage;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public float getDamage() {
        return damage;
    }

}
