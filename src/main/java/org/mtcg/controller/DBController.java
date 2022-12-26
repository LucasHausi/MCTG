package org.mtcg.controller;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class DBController {
    HikariConfig config = new HikariConfig("src/main/resources/hikari.properties");
    HikariDataSource ds = new HikariDataSource(config);
}
