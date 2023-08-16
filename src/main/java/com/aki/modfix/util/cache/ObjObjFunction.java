package com.aki.modfix.util.cache;

@FunctionalInterface
public interface ObjObjFunction<T, U> {
    void apply(T t, U u);
}
