package org.mtcg.config;

import java.sql.Connection;

public interface DBConnector {
    Connection getConnection();
}
