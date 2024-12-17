package com.vzurauskas.sturdystore;

import java.sql.Connection;
import java.sql.SQLException;

import com.zaxxer.hikari.HikariDataSource;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

public final class Database implements AutoCloseable {
    private final HikariDataSource source;
    private final String file;
    private final String username;
    private final String password;
    private Connection connection;

    public Database(String h2File, String username, String password) {
        this.file = h2File;
        this.username = username;
        this.password = password;
        this.source = dataSource();
    }

    public DSLContext connect() {
        try {
            this.connection = source.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return DSL.using(connection, SQLDialect.H2);
    }

    private HikariDataSource dataSource() {
        HikariDataSource ds = new HikariDataSource();
        ds.setJdbcUrl("jdbc:h2:" + file);
        ds.setUsername(username);
        ds.setPassword(password);
        return ds;
    }

    @Override
    public void close() throws SQLException {
        connection.close();
        source.close();
    }
}
