package org.mtcg.repository;

import org.mtcg.cards.*;
import org.mtcg.cards.Package;
import org.mtcg.config.DBConnector;
import org.mtcg.user.Stack;
import org.mtcg.user.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.UUID;

public class PostgresCardRepository {

    private static DBConnector dataSource;

    public PostgresCardRepository(DBConnector dataSource){
        this.dataSource=dataSource;
        try (PreparedStatement ps = dataSource.getConnection()
                .prepareStatement(SETUP_TABLE)){
            ps.execute();
        } catch (SQLException ex1) {
            throw new IllegalStateException("Failed to setup up table" + ex1);
        }
    }
    private static final String ADD_OWNER = """
            UPDATE CARDS 
                SET OWNER = ?
                WHERE ID = ?;
            """;
    private final String SET_LOCK = """
            UPDATE CARDS 
                SET LOCK = ?
                WHERE ID = ?;
            """;

    private final String ADD_CARD = """
            INSERT INTO CARDS VALUES (?, ?, ? ,?, ?, ?);
            """;
    public boolean addPackage(Package p){
        try (Connection c = dataSource.getConnection()) {
            for(Card card : p.getCards()){
                try (PreparedStatement ps = c.prepareStatement(ADD_CARD)) {
                    ps.setString(1, card.getId().toString());
                    ps.setString(2, card.getElement().toString());
                    ps.setFloat(3, card.getDamage());
                    try {
                        Monstercard temp = (Monstercard) card;
                        ps.setString(4, temp.getType().toString());
                    }catch (Exception ex1){
                        //when error = spellcard -> type = null
                        ps.setString(4,null);
                    }
                    ps.setString(5,null);
                    ps.setBoolean(6,card.isLock());
                    ps.execute();
                }
            }

        } catch (SQLException e) {
            //should fail because of duplicate entry
            return false;
        }
        return true;
    }
    //gets all ownerless cards and adds them to the store
    public static ArrayList<Package> getAllPackages(){
        ArrayList<Card> allCards = new ArrayList<>();
        try (Connection c = dataSource.getConnection()) {
                try (PreparedStatement ps = c.prepareStatement(ALL_OWNERLESS_CARDS)) {

                    ps.execute();
                    final ResultSet resultSet = ps.getResultSet();
                    while (resultSet.next()) {
                        allCards.add(convertResultSetToCard(resultSet));
                    }
                }
        } catch (SQLException e) {
            throw new IllegalStateException("DB query failed", e);
        }
        Package temp = new Package();
        ArrayList<Package> resultPackages = new ArrayList<>();
        //save the size because it declines with remove function
        int tempSize = allCards.size();
        //collect the cards in packages by 5
        for(int i=0; i<tempSize;i++){
            temp.addCard(allCards.remove(0));
            if((i%4==0) && (i!=0)){
                resultPackages.add(temp);
                temp = new Package();
            }
        }
        return resultPackages;
    }
    private static Card convertResultSetToCard(ResultSet resultSet) throws SQLException{
        String stringType = resultSet.getString("type");
        if(stringType == null){
            return new Spellcard(
                    UUID.fromString(resultSet.getString("id")),
                    resultSet.getFloat("damage"),
                    Elements.valueOf(resultSet.getString("element")),
                    resultSet.getBoolean("lock")
            );
        }else{
            return new Monstercard(
                    UUID.fromString(resultSet.getString("id")),
                    resultSet.getFloat("damage"),
                    Elements.valueOf(resultSet.getString("element")),
                    Monsters.valueOf(stringType),
                    resultSet.getBoolean("lock")
            );
        }
    }
    public Stack getCardsByUsername(User u){
        Stack s = new Stack();
        try (Connection c = dataSource.getConnection()) {

                try (PreparedStatement ps = c.prepareStatement(GET_CARDS_BY_USERNAME)) {
                    ps.setString(1, u.getUsername());
                    ps.execute();
                    final ResultSet resultSet = ps.getResultSet();
                    while (resultSet.next()){
                        s.addCard(convertResultSetToCard(resultSet));
                    }
                }
        } catch (SQLException e) {
            throw new IllegalStateException("DB query failed", e);
        }
        return s;
    }
    public boolean setLock(Boolean lock, String id){
        try (Connection c = dataSource.getConnection()) {
            try (PreparedStatement ps = c.prepareStatement(SET_LOCK)) {
                ps.setBoolean(1, lock);
                ps.setString(2, id);
                ps.execute();
                return true;
            }
        } catch (SQLException e) {
            System.err.println("DB query failed"+ e);
            return false;
        }
    }
    public void setOwner(Card card, User u){
        try (Connection c = dataSource.getConnection()) {
            try (PreparedStatement ps = c.prepareStatement(ADD_OWNER)) {
                ps.setString(1, u.getUsername());
                ps.setString(2, card.getId().toString());
                ps.execute();
            }
        } catch (SQLException e) {
            System.err.println("Error when setting owner");
        }
    }
    public static void addOwnerToPackage(Package p, User u){
        try (Connection c = dataSource.getConnection()) {
            for(Card card : p.getCards()){
                try (PreparedStatement ps = c.prepareStatement(ADD_OWNER)) {
                    ps.setString(1, u.getUsername());
                    ps.setString(2, card.getId().toString());
                    ps.execute();
                }
            }

        } catch (SQLException e) {
            throw new IllegalStateException("DB query failed", e);
        }
    }
    private final String SETUP_TABLE = """
                CREATE TABLE IF NOT EXISTS cards(
                    id varchar(500)primary key,
                    element varchar(500),
                    damage float,
                    type varchar(500),
                    owner varchar(500),
                    lock BOOL,
                    CONSTRAINT fk_user
                          FOREIGN KEY(owner)
                    	  REFERENCES users(username)
                    	  ON DELETE CASCADE
                );
            """;
    private static final String ALL_OWNERLESS_CARDS = """
                SELECT * FROM CARDS WHERE OWNER IS NULL;
            """;
    private final String GET_CARDS_BY_USERNAME = """
                SELECT * FROM CARDS WHERE OWNER = ?;   
            """;
}
