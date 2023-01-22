package org.mtcg.repository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mtcg.cards.*;
import org.mtcg.cards.Package;
import org.mtcg.config.TestDataSource;
import org.mtcg.user.Stack;
import org.mtcg.user.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PostgresCardRepositoryTest {
    //need to create UserRepo first because of fk constraint
    private PostgresCardRepository postgresCardRepository
            = new PostgresCardRepository(TestDataSource.getInstance());

    @AfterEach
    void cleanUpCardTable() throws SQLException {
        try (Connection c = TestDataSource.getInstance().getConnection()) {
            c.prepareStatement("""
                        delete from cards
                    """).execute();
            c.prepareStatement("""
                        delete from users
                    """).execute();
        }
    }
    @Test
    void testAddPackage() {
        //Arrange
        Package p = new Package();
        for(int i=0; i<5;i++){
            p.addCard(new Spellcard(15,5));
        }
        //Act
        //Assert
        assertTrue(postgresCardRepository.addPackage(p));
    }
    @Test
    void testAddPackageTwice() {
        //Arrange
        Package p = new Package();
        for(int i=0; i<5;i++){
            p.addCard(new Spellcard(15,5));
        }
        //Act
        //Assert first should go through and second should fail because of unique constraint
        assertTrue(postgresCardRepository.addPackage(p));
        assertFalse(postgresCardRepository.addPackage(p));
    }

    @Test
    void testgetAllPackagesToFewCards() throws SQLException{
        //Arrange
        setUpTestUser();
        setUpTestData(new Spellcard(15,5));
        //Act
        ArrayList<Package> result = postgresCardRepository.getAllPackages();
        //Assert
        assertEquals(0,result.size());
    }
    @Test
    void testgetAllPackagesOnePackage() throws SQLException{
        //Arrange
        setUpTestUser();
        for(int i=0; i<5;i++){
            setUpTestData(new Spellcard(15,5));
        }
        //Act
        ArrayList<Package> result = postgresCardRepository.getAllPackages();
        //Assert
        assertEquals(1,result.size());
    }
    @Test
    void testSetLock()throws SQLException{
        //Arrange
        setUpTestUser();
        UUID id = UUID.fromString("dfdd758f-649c-40f9-ba3a-8657f4b3439f");
        Monstercard m = new Monstercard(id, 25, Elements.Normal, Monsters.Elve, false);
        setUpTestData(m);
        //Act
        //Assert
        assertTrue(postgresCardRepository.setLock(false, id.toString()));

    }

    @Test
    void testSetOwner() throws SQLException{
        //Arrange
        setUpTestUser();
        User testKienboec = new User("kienboec","testPW");
        UUID id = UUID.fromString("dfdd758f-649c-40f9-ba3a-8657f4b3439f");
        Monstercard m = new Monstercard(id, 25, Elements.Normal, Monsters.Elve, false);
        setUpTestData(m);
        //Act
        postgresCardRepository.setOwner(m,testKienboec);
        //Assert
        Stack s = postgresCardRepository.getCardsByUsername(testKienboec);
        assertEquals(1,s.getStackSize());
        assertEquals(id,s.getCard(id).getId());

    }
    @Test
    void testGetCardsByUsername() throws SQLException{
        //Arrange
        setUpTestUser();
        User testKienboec = new User("kienboec","testPW");
        UUID id = UUID.fromString("dfdd758f-649c-40f9-ba3a-8657f4b3439f");
        for(int i=0; i<5;i++){
            setUpTestData(new Spellcard(15,5),testKienboec);
        }
        //Act
        Stack s = postgresCardRepository.getCardsByUsername(testKienboec);
        //Assert
        assertEquals(5,s.getStackSize());
    }
    private void setUpTestUser() throws SQLException {
        try (Connection c = TestDataSource.getInstance().getConnection()) {
            try (PreparedStatement ps = c.prepareStatement("""
                INSERT INTO users (username, password) VALUES (?, ?)
            """
            )) {
                ps.setString(1, "kienboec" +
                                "");
                ps.setString(2, "testPW");
                ps.execute();
            }
        }
    }
    private void setUpTestData(Card card) throws SQLException {
        try (Connection c = TestDataSource.getInstance().getConnection()) {
            final var insert = c.prepareStatement("""
                INSERT INTO CARDS VALUES (?, ?, ? ,?, ?, ?);
            """);
            insert.setString(1, card.getId().toString());
            insert.setString(2, card.getElement().toString());
            insert.setFloat(3, card.getDamage());
            try {
                Monstercard temp = (Monstercard) card;
                insert.setString(4, temp.getType().toString());
            }catch (Exception ex1){
                //when error = spellcard -> type = null
                insert.setString(4,null);
            }
            insert.setString(5,null);
            insert.setBoolean(6,card.isLock());
            insert.execute();
        }
    }
    private void setUpTestData(Card card,User u) throws SQLException {
        try (Connection c = TestDataSource.getInstance().getConnection()) {
            final var insert = c.prepareStatement("""
                INSERT INTO CARDS VALUES (?, ?, ? ,?, ?, ?);
            """);
            insert.setString(1, card.getId().toString());
            insert.setString(2, card.getElement().toString());
            insert.setFloat(3, card.getDamage());
            try {
                Monstercard temp = (Monstercard) card;
                insert.setString(4, temp.getType().toString());
            }catch (Exception ex1){
                //when error = spellcard -> type = null
                insert.setString(4,null);
            }
            insert.setString(5,u.getUsername());
            insert.setBoolean(6,card.isLock());
            insert.execute();
        }
    }
}