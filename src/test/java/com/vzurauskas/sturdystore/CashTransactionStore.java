package com.vzurauskas.sturdystore;

import java.io.InputStream;
import java.util.*;

import com.vzurauskas.nereides.jackson.Json;
import com.vzurauskas.nereides.jackson.MutableJson;
import com.vzurauskas.nereides.jackson.SmartJson;
import org.jooq.DataType;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import static org.jooq.impl.SQLDataType.DECIMAL;
import static org.jooq.impl.SQLDataType.VARCHAR;

final class CashTransactionStore implements Store<CashTransactionStore.CashEntry> {

    public static final class CashEntry extends Store.Entry {
        CashEntry(Transaction transaction) {
            super(
                UUID.randomUUID().toString(),
                transaction
            );
        }
    }

    static final class Transaction implements Json {
        private final SmartJson origin;

        Transaction(String date, String amount, String dc, String category) {
            this(new SmartJson(
                new MutableJson()
                    .with("date", date)
                    .with("amount", amount)
                    .with("dc", dc)
                    .with("category", category)
            ));
        }

        Transaction(SmartJson origin) {
            this.origin = origin;
        }

        @Override
        public InputStream bytes() {
            return origin.bytes();
        }

        @Override
        public String toString() {
            return origin.pretty();
        }
    }

    private final Store<Entry> origin;

    public CashTransactionStore(Database db) {
        this(new JooqStore(
            "CASH_TRANSACTION",
            db,
            Map.of(
                "id",       mapping("ID",       SQLDataType.UUID.nullable(false)),
                "date",     mapping("DATE",     VARCHAR(16).nullable(false)),
                "amount",   mapping("AMOUNT",   DECIMAL(16, 2).nullable(false)),
                "dc",       mapping("DC",       VARCHAR(4).nullable(false)),
                "category", mapping("CATEGORY", VARCHAR(32).nullable(false))
            ),
            List.of(
                DSL.constraint("PK_CASH_TRANSACTION").primaryKey("ID"),
                DSL.constraint("IP_CASH_TRANSACTION").unique("DATE", "CATEGORY")
            ),
            List.of(
                new JooqIndex(
                    "IDX_CT_DC_CATEGORY", "CASH_TRANSACTION", "DC", "CATEGORY"
                )
            )
        ));
    }

    private static Map.Entry<String, DataType<?>> mapping(
        String column, DataType<?> datatype
    ) {
        return new AbstractMap.SimpleEntry<>(column, datatype);
    }

    CashTransactionStore(Store<Entry> origin) {
        this.origin = origin;
    }

    @Override
    public void save(CashEntry entry) {
        origin.save(entry);
    }

    @Override
    public Collection<Entry> find(Condition... conditions) {
        return origin.find(conditions);
    }

    @Override
    public int size() {
        return origin.size();
    }
}
