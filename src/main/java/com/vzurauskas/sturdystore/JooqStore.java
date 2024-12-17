package com.vzurauskas.sturdystore;

import com.vzurauskas.nereides.jackson.Json;
import com.vzurauskas.nereides.jackson.MutableJson;
import com.vzurauskas.nereides.jackson.SmartJson;
import org.jooq.*;
import org.jooq.impl.DSL;

import java.util.*;
import java.util.stream.Collectors;

public final class JooqStore implements Store<Store.Entry> {
    private final String table;
    private final Cached<DSLContext> db;
    private final Map<String, Map.Entry<String, DataType<?>>> fieldToColumn;

    public JooqStore(
        String table,
        DSLContext db,
        Map<String, Map.Entry<String, DataType<?>>> fieldToColumn,
        List<ConstraintEnforcementStep> constraints,
        List<CreateIndexIncludeStep> indexes
    ) {
        this.table = table;
        this.db = new Cached<>(() -> {
            init(db, constraints, indexes);
            return db;
        });
        this.fieldToColumn = new HashMap<>(fieldToColumn);
    }

    @Override
    public void save(Entry entry) {
        SmartJson se = new SmartJson(entry);
        db.value().insertInto(DSL.table(table))
            .columns(
                fieldToColumn.values().stream().map(
                    col -> DSL.field(col.getKey())
                ).collect(Collectors.toList())
            )
            .values(
                fieldToColumn.keySet().stream()
                    .map(se::leaf)
                    .collect(Collectors.toList())
            )
            .execute();
    }

    @Override
    public Set<Entry> find(Condition... conditions) {
        return db.value()
            .select()
            .from(table)
            .where(aggFieldEquals(conditions))
            .fetch().stream()
            .map(
                record -> {
                    MutableJson json = new MutableJson();
                    Arrays.stream(record.fields()).forEach(
                        field -> json.with(
                            field.getName().toLowerCase(),
                            field.getValue(record).toString())
                    );
                    return new JooqEntry(record.get("ID").toString(), json);
                }
            ).collect(Collectors.toSet());
    }

    @Override
    public int size() {
        return db.value().selectCount().from(table).fetchOne(0, int.class);
    }

    private void init(
        DSLContext database,
        List<ConstraintEnforcementStep> constraints,
        List<CreateIndexIncludeStep> indexes
    ) {
        CreateTableColumnStep step = database.createTableIfNotExists(table);
        fieldToColumn.values().forEach(
            column -> step.column(column.getKey(), column.getValue())
        );
        step.constraints(constraints).execute();
        indexes.forEach(Query::execute);
    }

    private org.jooq.Condition aggFieldEquals(Condition... conditions) {
        return aggFieldEquals(
            fieldEquals(conditions[0]),
            Arrays.copyOfRange(conditions, 1, conditions.length)
        );
    }

    private org.jooq.Condition aggFieldEquals(
        org.jooq.Condition agg, Condition... tail
    ) {
        return tail.length == 0
            ? agg
            : aggFieldEquals(
                agg.and(fieldEquals(tail[0])),
                Arrays.copyOfRange(tail, 1, tail.length)
            );
    }

    private org.jooq.Condition fieldEquals(Condition condition) {
        return DSL.field(
            table + '.' + condition.field().toUpperCase()
        ).eq(condition.value());
    }

    private static class JooqEntry extends Entry {
        protected JooqEntry(String id, Json origin) {
            super(id, origin);
        }
    }
}
