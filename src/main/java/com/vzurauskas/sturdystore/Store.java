package com.vzurauskas.sturdystore;

import com.vzurauskas.nereides.jackson.Json;
import com.vzurauskas.nereides.jackson.MutableJson;
import com.vzurauskas.nereides.jackson.SmartJson;

import java.io.InputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface Store<T extends Store.Entry> {
    void save(T entry);
    Collection<Entry> find(Condition... conditions);
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
            Stream<Entry> stream = entries.stream();
            for (Condition condition : conditions) {
                stream = stream.filter(
                    e -> condition.value().equals(
                        new SmartJson(e).leaf(condition.field())
                    )
                );
            }
            return stream.collect(Collectors.toSet());
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
