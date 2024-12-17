package com.vzurauskas.sturdystore;

import java.util.function.Supplier;

final class Cached<T> {
    private final Supplier<T> func;
    private T value;

    public Cached(Supplier<T> func) {
        this.func = func;
    }

    public T value() {
        if (value == null) {
            value = func.get();
        }
        return value;
    }
}
