package com.aki.modfix.util.cache;

@FunctionalInterface
public interface ObjInt2IntFunction<T> {

    int apply(T t, int v);

}
