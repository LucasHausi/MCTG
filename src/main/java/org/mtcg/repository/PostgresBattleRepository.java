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
    public void addBattle(BattleLog battleLog, String result, User user){
        try (Connection c = dataSource.getConnection()) {
            try (PreparedStatement ps = c.prepareStatement(ADD_BATTLE)) {
                ps.setString(1, result);
                ps.setString(2, user.getUsername());
                ps.setString(3, battleLog.getLog());
                ps.execute();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("DB query failed", e);
        }
    }
    private final String ADD_BATTLE = """
            INSERT INTO BATTLES (result, username, battleLog) VALUES (?,?,?);
            """;
    private final String SETUP_TABLE = """
            CREATE TABLE IF NOT EXISTS battles(
                id    serial primary key,
                result varchar(500),
                username varchar(500),
                battleLog text,
                CONSTRAINT fk_user
                    FOREIGN KEY(username)
                    REFERENCES users(username)
                    ON DELETE CASCADE
                );
            """;
}
