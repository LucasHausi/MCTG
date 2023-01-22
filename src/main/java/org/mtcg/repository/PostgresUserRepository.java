package org.mtcg.repository;

import org.mtcg.config.DBConnector;
import org.mtcg.config.DataSource;
import org.mtcg.user.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PostgresUserRepository implements UserRepository{

    private static DBConnector dataSource;

    //depracated ----
    public PostgresUserRepository(DBConnector dataSource){
        this.dataSource = dataSource;
        try (PreparedStatement ps = dataSource.getConnection()
                .prepareStatement(SETUP_TABLE)){
            ps.execute();
        } catch (SQLException ex1) {
            throw new IllegalStateException("Failed to setup up table" + ex1);
        }
    }
    private final String UPDATE_ELO = """
            UPDATE USERS
                    SET ELO = ?
                    WHERE
                        USERNAME = ?;
            """;
    private static final String SETUP_TABLE = """
                CREATE TABLE IF NOT EXISTS users(
                    username varchar(500)primary key,
                    password varchar(500),
                    coins int,
                    nickname varchar(500),
                    bio varchar(500),
                    image varchar(500),
                    elo int
                );
            """;

    private static final String GET_USER_BY_USERNAME = """
        SELECT * FROM USERS WHERE username = ?;
        """;

    private static final String ADD_USER = """
            INSERT INTO users (username, password, coins, nickname, bio, image, elo) VALUES (?, ?, ?, ?, ?, ?, ?)
            """;

    private static final String ALL_USERS = """
            SELECT * FROM USERS;
            """;
    private static final String UPDATE_USERDATA = """
                UPDATE USERS
                    SET NICKNAME = ?,
                        BIO = ?,
                        IMAGE = ?
                    WHERE
                        USERNAME = ?;
            """;
    public static void updateUserdata(String name, String bio, String image, String username){
        try (Connection c = dataSource.getConnection()) {
            try (PreparedStatement ps = c.prepareStatement(UPDATE_USERDATA)) {
                ps.setString(1, name);
                ps.setString(2, bio);
                ps.setString(3, image);
                ps.setString(4, username);
                ps.execute();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("DB query failed", e);
        }
    }
    public void updateElo(User u1){
        try (Connection c = dataSource.getConnection()) {
            try (PreparedStatement ps = c.prepareStatement(UPDATE_ELO)) {
                System.out.println(u1.getElo()+" "+ u1.getUsername());
                ps.setInt(1, u1.getElo());
                ps.setString(2, u1.getUsername());
                ps.execute();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("DB query failed", e);
        }
    }
    public void addUser(User u){
        try (Connection c = dataSource.getConnection()) {
            try (PreparedStatement ps = c.prepareStatement(ADD_USER)) {
                ps.setString(1, u.getUsername());
                ps.setString(2, u.getPassword());
                ps.setInt(3, u.getCoins());
                ps.setString(4, u.getNickname());
                ps.setString(5, u.getBio());
                ps.setString(6, u.getImage());
                ps.setInt(7, u.getElo());
                ps.execute();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("DB query failed", e);
        }
    }
    public static List<User> getAllUsers(){
        List<User> users = new ArrayList<>();
        try (Connection c = dataSource.getConnection()) {
            try (PreparedStatement ps = c.prepareStatement(ALL_USERS)) {
                ps.execute();
                final ResultSet resultSet = ps.getResultSet();
                while (resultSet.next()) {
                    users.add(convertResultSetToUser(resultSet));
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("DB query failed", e);
        }
        return users;
    };
    private static User convertResultSetToUser(ResultSet resultSet)  throws SQLException {
        return new User(
                resultSet.getString(1),
                resultSet.getString(2),
                resultSet.getInt(3),
                resultSet.getString(4),
                resultSet.getString(5),
                resultSet.getString(6),
                resultSet.getInt(7));
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
                            resultSet.getString("image"),
                            resultSet.getInt("image"));
                }

            }
        } catch (SQLException e) {
            throw new IllegalStateException("DB query failed", e);
        }
    }
    //DEV Function
    /*
    public void PRINT_ALL_USERS(){
        try (Connection c = dataSource.getConnection()) {
            try (PreparedStatement ps = c.prepareStatement(ALL_USERS)) {
                ps.execute();
                final ResultSet resultSet = ps.getResultSet();
                while (resultSet.next()) {
                    System.out.println(resultSet.getString(1));
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("DB query failed", e);
        }
    }*/



}
