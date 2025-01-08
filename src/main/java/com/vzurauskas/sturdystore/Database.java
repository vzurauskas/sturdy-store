package com.vzurauskas.sturdystore;

import java.io.Closeable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import com.zaxxer.hikari.HikariDataSource;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

public final class Database implements Closeable {
    private final HikariDataSource source;

    public Database(String h2File, String username, String password) {
        this.source = dataSource(h2File, username, password);
    }

    public void execVoid(VoidTransaction<DSLContext> transaction) {
        try (Connection connection = source.getConnection();
             DSLContext dsl = DSL.using(connection, SQLDialect.H2)) {
            transaction.exec(dsl);
        } catch (SQLException e) {
            throw new RuntimeException("Error during database operation", e);
        }
    }

    public <T> List<T> execFetchList(ListTransaction<DSLContext, T> transaction) {
        try (Connection connection = source.getConnection();
             DSLContext dsl = DSL.using(connection, SQLDialect.H2)) {
            return transaction.exec(dsl);
        } catch (SQLException e) {
            throw new RuntimeException("Error during database operation", e);
        }
    }

    public <K, V> Map<? super K, V> execFetchMap(MapTransaction<DSLContext, K, V> transaction) {
        try (Connection connection = source.getConnection();
             DSLContext dsl = DSL.using(connection, SQLDialect.H2)) {
            return transaction.exec(dsl);
        } catch (SQLException e) {
            throw new RuntimeException("Error during database operation", e);
        }
    }

    private HikariDataSource dataSource(
        String h2File, String username, String password
    ) {
        HikariDataSource ds = new HikariDataSource();
        ds.setJdbcUrl("jdbc:h2:" + h2File);
        ds.setUsername(username);
        ds.setPassword(password);
        return ds;
    }

    @Override
    public void close() {
        source.close();
    }

    @FunctionalInterface
    public interface VoidTransaction<C> {
        void exec(C connection) throws SQLException;
    }

    @FunctionalInterface
    public interface ListTransaction<C, T> {
        List<T> exec(C connection) throws SQLException;
    }

    @FunctionalInterface
    public interface MapTransaction<C, K, V> {
        Map<? super K, V> exec(C connection) throws SQLException;
    }
}
