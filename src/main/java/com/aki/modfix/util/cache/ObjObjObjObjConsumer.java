package com.aki.modfix.util.cache;

@FunctionalInterface
public interface ObjObjObjObjConsumer<T, U, V, W> {
    void accept(T t, U u, V v, W w);
}
