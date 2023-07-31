package com.aki.modfix.util.cache;

public interface ObjObjObjObjConsumer<T, U, V, W> {
    void accept(T t, U u, V v, W w);
}
