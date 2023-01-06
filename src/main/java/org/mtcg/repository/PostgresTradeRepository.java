package org.mtcg.repository;

import org.mtcg.config.DBConnector;
import org.mtcg.config.DataSource;
import org.mtcg.game.Requirement;
import org.mtcg.game.TradingDeal;
import org.mtcg.service.UserService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PostgresTradeRepository {
    private static DBConnector dataSource;

    public PostgresTradeRepository(DBConnector dataSource) {
        this.dataSource = dataSource;
        try (PreparedStatement ps = dataSource.getConnection()
                .prepareStatement(SETUP_TABLE)){
            ps.execute();
        } catch (SQLException ex1) {
            throw new IllegalStateException("Failed to setup up table" + ex1);
        }
    }
    public void addTrade(TradingDeal td){
        try (Connection c = dataSource.getConnection()) {
            try (PreparedStatement ps = c.prepareStatement(ADD_TradingDeal)) {
                ps.setString(1, td.getId().toString());
                ps.setString(2, td.getSeller().getUsername());
                ps.setString(3, td.getOfferedCardID());
                ps.setString(4, td.getRequirement().getType());
                ps.setFloat(5, td.getRequirement().getMinDamage());
                ps.execute();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("DB query failed", e);
        }
    }
    public static List<TradingDeal> getAllTrades(){
        List<TradingDeal> result = new ArrayList<>();
        try (Connection c = dataSource.getConnection()) {
            try (PreparedStatement ps = c.prepareStatement("SELECT * FROM TRADES")) {
                ps.execute();
                final ResultSet resultSet = ps.getResultSet();
                UserRepository imur = UserService.getIMUR();
                while (resultSet.next()) {
                    result.add(convertResultSetToTrade(resultSet, imur));
                }
                //set null for garbage collector
                imur = null;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("DB query failed", e);
        }
        return result;
    }
    public void deleteTrade(UUID tradeID){
        try (Connection c = dataSource.getConnection()) {
            try (PreparedStatement ps = c.prepareStatement(DELETE_TradingDeal)) {
                ps.setString(1, tradeID.toString());
                ps.execute();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("DB query failed", e);
        }
    }
    static TradingDeal convertResultSetToTrade(ResultSet resultSet,UserRepository imur) throws SQLException{
        return new TradingDeal(
                UUID.fromString(resultSet.getString("id")),
                resultSet.getString("offered_cardID"),
                imur.getUserByUsername(resultSet.getString("seller")),
                new Requirement(
                    resultSet.getString("typ"),
                    resultSet.getFloat("min_Damage"))
        );
    }
    private final String ADD_TradingDeal = """
            INSERT INTO trades (id, seller, offered_cardID, typ, min_Damage) VALUES (?, ?, ?, ?, ?)
            """;
    private final String DELETE_TradingDeal = """
            DELETE FROM TRADES WHERE ID = ?
            """;
    private final String SETUP_TABLE = """
                CREATE TABLE IF NOT EXISTS trades(
                   id varchar(500)primary key,
                   seller varchar(500),
                   offered_cardID varchar(500),
                   typ varchar(500),
                   min_Damage float,
                   CONSTRAINT fk_user
                      FOREIGN KEY(seller)
                      REFERENCES users(username)
                      ON DELETE CASCADE,
                   CONSTRAINT fk_card
                      FOREIGN KEY(offered_cardID)
                      REFERENCES cards(id)
                      ON DELETE CASCADE
                );
            """;
}
