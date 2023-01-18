package org.mtcg.cards;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CardTest {

    //tests for specialties
    @Test
    void goblinAttacksDragon() {
        //Arrange
        Card tempGoblin = new Monstercard(UUID.randomUUID(), 12 ,Elements.Normal, Monsters.Goblin, false);
        Card tempDragon = new Monstercard(UUID.randomUUID(), 12 ,Elements.Normal, Monsters.Dragon, false);
        //ACT
        //ASSERT
        assertNull(tempGoblin.attack(tempDragon));

    }
    @Test
    void orkAttacksWizzard() {
        //Arrange
        Card tempOrk = new Monstercard(UUID.randomUUID(), 25 ,Elements.Normal, Monsters.Ork, false);
        Card tempWizzard = new Monstercard(UUID.randomUUID(), 12 ,Elements.Normal, Monsters.Wizzard, false);
        //ACT
        //ASSERT
        assertNull(tempOrk.attack(tempWizzard));
        assertEquals(tempOrk,tempWizzard.attack(tempOrk));

    }
    @Test
    void knightAgainstWaterspell() {
        //Arrange
        Card tempKnight = new Monstercard(UUID.randomUUID(), 12 ,Elements.Normal, Monsters.Knight, false);
        Card tempSpell = new Spellcard(UUID.randomUUID(), 12 ,Elements.Water, false);
        //ACT
        //ASSERT
        assertEquals(tempSpell,tempSpell.attack(tempKnight));
        assertEquals(tempSpell,tempKnight.attack(tempSpell));

    }
    @Test
    void spellAgainstKraken() {
        //Arrange
        Card tempKraken = new Monstercard(UUID.randomUUID(), 12 ,Elements.Normal, Monsters.Kraken, false);
        Card tempSpell = new Spellcard(UUID.randomUUID(), 12 ,Elements.Water, false);
        //ACT
        //ASSERT
        assertEquals(null, tempSpell.attack(tempKraken));
        assertEquals(null, tempKraken.attack(tempSpell));

    }
    @Test
    void fireElvesAgaisntDragons() {
        //Arrange
        Card tempElve = new Monstercard(UUID.randomUUID(), 25 ,Elements.Fire, Monsters.Elve, false);
        Card tempDragon = new Monstercard(UUID.randomUUID(), 12 ,Elements.Normal, Monsters.Dragon, false);
        //ACT
        //ASSERT
        assertNull(tempDragon.attack(tempElve));
        assertEquals(tempElve,tempElve.attack(tempDragon));
    }

    //test assess element damage behavior
    @Test
    void fireAgainstNormal() {
        //Arrange
        Card tempFire = new Monstercard(UUID.randomUUID(), 12 ,Elements.Fire, Monsters.Elve, false);
        Card tempNormal = new Spellcard(UUID.randomUUID(), 12 ,Elements.Normal, false);
        //ACT
        //ASSERT
        assertEquals(tempFire,tempFire.attack(tempNormal));
        assertEquals(tempFire,tempNormal.attack(tempFire));
    }
    @Test
    void waterAgainstFire() {
        //Arrange
        Card tempFire = new Monstercard(UUID.randomUUID(), 12 ,Elements.Fire, Monsters.Elve, false);
        Card tempWater = new Spellcard(UUID.randomUUID(), 12 ,Elements.Water, false);
        //ACT
        //ASSERT
        assertEquals(tempWater,tempFire.attack(tempWater));
        assertEquals(tempWater,tempWater.attack(tempFire));
    }
    @Test
    void normalAgainstWater() {
        //Arrange
        Card tempWater = new Monstercard(UUID.randomUUID(), 12 ,Elements.Water, Monsters.Elve, false);
        Card tempNormal = new Spellcard(UUID.randomUUID(), 12 ,Elements.Normal, false);
        //ACT
        //ASSERT
        assertEquals(tempNormal,tempNormal.attack(tempWater));
        assertEquals(tempNormal,tempWater.attack(tempNormal));
    }
    @Test
    void draw() {
        //Arrange
        Card tempWater = new Monstercard(UUID.randomUUID(), 24 ,Elements.Water, Monsters.Elve, false);
        Card tempNormal = new Spellcard(UUID.randomUUID(), 6 ,Elements.Normal, false);
        //ACT
        //ASSERT
        assertNull(tempNormal.attack(tempWater));
        assertNull(tempWater.attack(tempNormal));
    }
    @Test
    void monstersNoElementEffect() {
        //Arrange
        Card tempWater = new Monstercard(UUID.randomUUID(), 12 ,Elements.Water, Monsters.Elve, false);
        Card tempNormal = new Monstercard(UUID.randomUUID(), 6 ,Elements.Normal, Monsters.Elve, false);
        //ACT
        //ASSERT
        assertEquals(tempWater,tempNormal.attack(tempWater));
        assertEquals(tempWater,tempWater.attack(tempNormal));
    }
}