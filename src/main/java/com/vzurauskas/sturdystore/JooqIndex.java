package com.vzurauskas.sturdystore;

import org.jooq.DSLContext;

public final class JooqIndex {
    private final String name;
    private final String table;
    private final String[] columns;

    public JooqIndex(String name, String table, String... columns) {
        this.name = name;
        this.table = table;
        this.columns = columns;
    }

    @SuppressWarnings("resource")
    void create(DSLContext dsl) {
        dsl
            .createIndexIfNotExists(name)
            .on(table, columns)
            .execute();
    }
}
