package org.mtcg.repository;

import org.mtcg.config.DBConnector;
import org.mtcg.config.DataSource;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class PostgresCardRepository {

    private final DBConnector dataSource;

    public PostgresCardRepository(DBConnector dataSource){
        this.dataSource=dataSource;
        try (PreparedStatement ps = dataSource.getConnection()
                .prepareStatement(SETUP_TABLE)){
            ps.execute();
        } catch (SQLException ex1) {
            throw new IllegalStateException("Failed to setup up table" + ex1);
        }
    }

    private static final String SETUP_TABLE = """
                CREATE TABLE IF NOT EXISTS cards(
                    id varchar(500)primary key,
                    element varchar(500),
                    damage float,
                    type varchar(500),
                    owner varchar(500),
                    CONSTRAINT fk_user
                          FOREIGN KEY(owner)
                    	  REFERENCES users(username)
                    	  ON DELETE CASCADE
                );
            """;

}
