package org.mtcg.cards;

import org.junit.jupiter.api.Test;
import org.mtcg.game.BattleLog;

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
        assertNull(tempGoblin.attack(tempDragon,new BattleLog()));

    }
    @Test
    void orkAttacksWizzard() {
        //Arrange
        Card tempOrk = new Monstercard(UUID.randomUUID(), 25 ,Elements.Normal, Monsters.Ork, false);
        Card tempWizzard = new Monstercard(UUID.randomUUID(), 12 ,Elements.Normal, Monsters.Wizzard, false);
        //ACT
        //ASSERT
        assertNull(tempOrk.attack(tempWizzard,new BattleLog()));
        assertEquals(tempOrk,tempWizzard.attack(tempOrk,new BattleLog()));

    }
    @Test
    void knightAgainstWaterspell() {
        //Arrange
        Card tempKnight = new Monstercard(UUID.randomUUID(), 12 ,Elements.Normal, Monsters.Knight, false);
        Card tempSpell = new Spellcard(UUID.randomUUID(), 12 ,Elements.Water, false);
        //ACT
        //ASSERT
        assertEquals(tempSpell,tempSpell.attack(tempKnight,new BattleLog()));
        assertEquals(tempSpell,tempKnight.attack(tempSpell,new BattleLog()));

    }
    @Test
    void spellAgainstKraken() {
        //Arrange
        Card tempKraken = new Monstercard(UUID.randomUUID(), 12 ,Elements.Normal, Monsters.Kraken, false);
        Card tempSpell = new Spellcard(UUID.randomUUID(), 12 ,Elements.Water, false);
        //ACT
        //ASSERT
        assertEquals(null, tempSpell.attack(tempKraken,new BattleLog()));
        assertEquals(null, tempKraken.attack(tempSpell,new BattleLog()));

    }
    @Test
    void fireElvesAgaisntDragons() {
        //Arrange
        Card tempElve = new Monstercard(UUID.randomUUID(), 25 ,Elements.Fire, Monsters.Elve, false);
        Card tempDragon = new Monstercard(UUID.randomUUID(), 12 ,Elements.Normal, Monsters.Dragon, false);
        //ACT
        //ASSERT
        assertNull(tempDragon.attack(tempElve,new BattleLog()));
        assertEquals(tempElve,tempElve.attack(tempDragon,new BattleLog()));
    }

    //test assess element damage behavior
    @Test
    void fireAgainstNormal() {
        //Arrange
        Card tempFire = new Monstercard(UUID.randomUUID(), 12 ,Elements.Fire, Monsters.Elve, false);
        Card tempNormal = new Spellcard(UUID.randomUUID(), 12 ,Elements.Normal, false);
        //ACT
        //ASSERT
        assertEquals(tempFire,tempFire.attack(tempNormal,new BattleLog()));
        assertEquals(tempFire,tempNormal.attack(tempFire,new BattleLog()));
    }
    @Test
    void waterAgainstFire() {
        //Arrange
        Card tempFire = new Monstercard(UUID.randomUUID(), 12 ,Elements.Fire, Monsters.Elve, false);
        Card tempWater = new Spellcard(UUID.randomUUID(), 12 ,Elements.Water, false);
        //ACT
        //ASSERT
        assertEquals(tempWater,tempFire.attack(tempWater,new BattleLog()));
        assertEquals(tempWater,tempWater.attack(tempFire,new BattleLog()));
    }
    @Test
    void normalAgainstWater() {
        //Arrange
        Card tempWater = new Monstercard(UUID.randomUUID(), 12 ,Elements.Water, Monsters.Elve, false);
        Card tempNormal = new Spellcard(UUID.randomUUID(), 12 ,Elements.Normal, false);
        //ACT
        //ASSERT
        assertEquals(tempNormal,tempNormal.attack(tempWater,new BattleLog()));
        assertEquals(tempNormal,tempWater.attack(tempNormal,new BattleLog()));
    }
    @Test
    void draw() {
        //Arrange
        Card tempWater = new Monstercard(UUID.randomUUID(), 24 ,Elements.Water, Monsters.Elve, false);
        Card tempNormal = new Spellcard(UUID.randomUUID(), 6 ,Elements.Normal, false);
        //ACT
        //ASSERT
        assertNull(tempNormal.attack(tempWater,new BattleLog()));
        assertNull(tempWater.attack(tempNormal,new BattleLog()));
    }
    @Test
    void monstersNoElementEffect() {
        //Arrange
        Card tempWater = new Monstercard(UUID.randomUUID(), 12 ,Elements.Water, Monsters.Elve, false);
        Card tempNormal = new Monstercard(UUID.randomUUID(), 6 ,Elements.Normal, Monsters.Elve, false);
        //ACT
        //ASSERT
        assertEquals(tempWater,tempNormal.attack(tempWater,new BattleLog()));
        assertEquals(tempWater,tempWater.attack(tempNormal,new BattleLog()));
    }
}