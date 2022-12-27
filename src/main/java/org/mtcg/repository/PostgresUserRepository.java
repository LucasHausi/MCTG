package org.mtcg.repository;

import org.mtcg.config.DBConnector;
import org.mtcg.user.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PostgresUserRepository implements UserRepository{

    private final DBConnector dataSource;

    public PostgresUserRepository(DBConnector dataSource){
        this.dataSource = dataSource;
        try (PreparedStatement ps = dataSource.getConnection()
                .prepareStatement(SETUP_TABLE)){
            ps.execute();
        } catch (SQLException ex1) {
            throw new IllegalStateException("Failed to setup up table" + ex1);
        }
    }
    private static final String SETUP_TABLE = """
                CREATE TABLE IF NOT EXISTS users(
                    username varchar(500)primary key,
                    password varchar(500),
                    coins int,
                    nickname varchar(500),
                    bio varchar(500),
                    image varchar(500)
                );
            """;

    private static final String GET_USER_BY_USERNAME = """
        SELECT * FROM USERS WHERE username = ?;
        """;

    private static final String ADD_User = """
            INSERT INTO users (username, password, coins, nickname, bio, image) VALUES (?, ?, ?, ?, ?, ?)
            """;

    public void addUser(User u){
        try (Connection c = dataSource.getConnection()) {
            try (PreparedStatement ps = c.prepareStatement(ADD_User)) {
                ps.setString(1, u.getUsername());
                ps.setString(2, u.getPassword());
                ps.setInt(3, u.getCoins());
                ps.setString(4, u.getNickname());
                ps.setString(5, u.getBio());
                ps.setString(6, u.getImage());
                ps.execute();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("DB query failed", e);
        }
    }
    @Override
    public User getUserByUsername(String username) {
        try (Connection c = dataSource.getConnection()) {
            try (PreparedStatement ps = c.prepareStatement(GET_USER_BY_USERNAME)) {
                ps.setString(1, username);
                ps.execute();
                final ResultSet resultSet = ps.getResultSet();
                if(resultSet.next()==false){
                    return null;
                }else {
                    return new User(
                            resultSet.getString("username"),
                            resultSet.getString("password"),
                            resultSet.getInt("coins"),
                            resultSet.getString("nickname"),
                            resultSet.getString("bio"),
                            resultSet.getString("image")
                    );
                }

            }
        } catch (SQLException e) {
            throw new IllegalStateException("DB query failed", e);
        }
    }
    //DEV Function
    public void PRINT_ALL_USERS(){
        try (Connection c = dataSource.getConnection()) {
            try (PreparedStatement ps = c.prepareStatement("SELECT * FROM users")) {
                ps.execute();
                final ResultSet resultSet = ps.getResultSet();
                while (resultSet.next()) {
                    System.out.println(resultSet.getString(1));
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("DB query failed", e);
        }
    }



}
