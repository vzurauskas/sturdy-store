package com.vzurauskas.sturdystore;

import com.jolbox.bonecp.BoneCPDataSource;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import java.sql.Connection;
import java.sql.SQLException;

public final class Database implements AutoCloseable {
    private final BoneCPDataSource source;
    private final String file;
    private Connection connection;

    public Database(String source) {
        this.file = source;
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

    private BoneCPDataSource dataSource() {
        final BoneCPDataSource src = new BoneCPDataSource();
        src.setDriverClass("org.h2.Driver");
        src.setJdbcUrl("jdbc:h2:" + file);
        src.setUser("");
        src.setPassword("");
        return src;
    }

    @Override
    public void close() throws SQLException {
        connection.close();
        source.close();
    }
}
