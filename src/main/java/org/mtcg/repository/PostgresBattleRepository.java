package org.mtcg.repository;

import org.mtcg.config.DBConnector;
import org.mtcg.game.BattleLog;
import org.mtcg.user.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class PostgresBattleRepository {
    private DBConnector dataSource;

    public PostgresBattleRepository(DBConnector dataSource) {
        this.dataSource = dataSource;
        try (PreparedStatement ps = dataSource.getConnection()
                .prepareStatement(SETUP_TABLE)){
            ps.execute();
        } catch (SQLException ex1) {
            throw new IllegalStateException("Failed to setup up table" + ex1);
        }
    }
    public void addBattle(BattleLog battleLog, User winner){
        try (Connection c = dataSource.getConnection()) {
            try (PreparedStatement ps = c.prepareStatement(ADD_BATTLE)) {
                if(winner!=null){
                    ps.setString(1, winner.getUsername());
                }else{
                    ps.setString(1, "Draw");
                }

                ps.setString(2, battleLog.getLog());
                ps.execute();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("DB query failed", e);
        }
    }
    private final String ADD_BATTLE = """
            INSERT INTO BATTLES (winner, battleLog) VALUES (?,?);
            """;
    private final String SETUP_TABLE = """
            CREATE TABLE IF NOT EXISTS battles(
                id    serial primary key,
                winner varchar(500),
                battleLog text
                );
            """;
}
