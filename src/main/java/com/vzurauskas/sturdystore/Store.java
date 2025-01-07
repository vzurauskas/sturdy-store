package com.vzurauskas.sturdystore;

import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.vzurauskas.nereides.jackson.Json;
import com.vzurauskas.nereides.jackson.MutableJson;
import com.vzurauskas.nereides.jackson.SmartJson;

public interface Store<T extends Store.Entry> {
    void save(T entry);
    Collection<Entry> find(Condition... conditions);
    Map<? super String, Entry> findMap(String keyField, Condition... conditions);
    int size();

    abstract class Entry implements Json {
        private final Json origin;

        protected Entry(String id, Json origin) {
            this.origin = new MutableJson(origin).with("id", id);
        }

        @Override
        public final InputStream bytes() {
            return origin.bytes();
        }

        @Override
        public final String toString() {
            return origin.toString();
        }
    }

    final class Condition {
        private final String field;
        private final String value;

        public Condition(String field, String value) {
            this.field = field;
            this.value = value;
        }

        public boolean matches(Entry entry) {
            return value.equals(new SmartJson(entry).leaf(field));
        }

        public String field() {
            return field;
        }

        public String value() {
            return value;
        }
    }

    final class FakeStore implements Store<Entry> {
        private final Set<Entry> entries;

        public FakeStore() {
            this.entries = new HashSet<>();
        }

        @Override
        public void save(Entry entry) {
            entries.add(entry);
        }

        @Override
        public Set<Entry> find(Condition... conditions) {
            return resultStream(conditions).collect(Collectors.toSet());
        }

        @Override
        public Map<? super String, Entry> findMap(
            String keyField, Condition... conditions
        ) {
            return resultStream(conditions).collect(
                Collectors.toMap(
                    entry -> new SmartJson(entry).leaf(keyField),
                    entry -> entry
                )
            );
        }

        private Stream<Entry> resultStream(Condition[] conditions) {
            return entries.stream().filter(entry ->
                Arrays.stream(conditions).allMatch(
                    condition -> condition.matches(entry)
                )
            );
        }

        @Override
        public int size() {
            return entries.size();
        }

        public Stream<Entry> stream() {
            return entries.stream();
        }
    }
}
