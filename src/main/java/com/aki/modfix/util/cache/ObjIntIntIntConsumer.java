package com.aki.modfix.util.cache;

@FunctionalInterface
public interface ObjIntIntIntConsumer<T> {
    void accept(T t, int x, int y, int z);

}
