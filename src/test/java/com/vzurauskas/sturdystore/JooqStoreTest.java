package com.vzurauskas.sturdystore;

import com.vzurauskas.nereides.jackson.SmartJson;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JooqStoreTest {

    @Test
    void findsOne() {
        CashTransactionStore store = storeWithFiveEntries();
        Set<Store.Entry> entries = store.find(
            new Store.Condition("date", "2023-12-31")
        );
        assertEquals(1, entries.size());
        assertEquals("Squirrels", singletonField(entries, "category"));
    }

    @Test
    void findsMultiple() {
        CashTransactionStore store = storeWithFiveEntries();
        Set<Store.Entry> entries = store.find(
            new Store.Condition("category", "Books")
        );
        assertEquals(2, entries.size());
    }

    @Test
    void findsByMultipleConditions() {
        CashTransactionStore store = storeWithFiveEntries();
        Set<Store.Entry> entries = store.find(
            new Store.Condition("category", "Books"),
            new Store.Condition("date", "2024-10-18")
        );
        assertEquals(1, entries.size());
        assertEquals("9.99", singletonField(entries, "amount"));
    }

    private static String singletonField(Set<Store.Entry> entries, String amount) {
        return entries.stream().findFirst()
            .map(SmartJson::new)
            .map(json -> json.leaf(amount))
            .orElseThrow();
    }

    private CashTransactionStore storeWithFiveEntries() {
        CashTransactionStore store = new CashTransactionStore(new Store.FakeStore());
        store.save(entry("2024-10-16", "10.00", "D", "Books"));
        store.save(entry("2024-10-17", "5.00", "D", "Clothes"));
        store.save(entry("2024-10-18", "9.99", "D", "Books"));
        store.save(entry("2020-10-01", "107.00", "C", "Salary"));
        store.save(entry("2023-12-31", "10.55", "D", "Squirrels"));
        assertEquals(5, store.size());
        return store;
    }

    private static CashTransactionStore.CashEntry entry(
        String date, String amount, String dc, String category
    ) {
        return new CashTransactionStore.CashEntry(
            new CashTransactionStore.Transaction(date, amount, dc, category)
        );
    }
}
