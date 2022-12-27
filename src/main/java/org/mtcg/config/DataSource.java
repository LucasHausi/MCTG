package org.mtcg.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public class DataSource implements DBConnector{
    private final HikariDataSource ds;

    public DataSource(){
        HikariConfig config = new HikariConfig("src/main/resources/hikari.properties");
        ds = new HikariDataSource(config);
    }

    private static DataSource dataSource;

    public static DataSource getInstance() {
        if (dataSource == null) {
            dataSource = new DataSource();
        }
        return dataSource;
    }
    @Override
    public Connection getConnection() {
        try{
            return this.ds.getConnection();
        }catch(SQLException ex1){
            throw new IllegalStateException("The requested database is not available!\n" + ex1);
        }
    }
}
