package com.aki.modfix.util.cache;

@FunctionalInterface
public interface IntIntObj2ObjFunction<T, R> {

    R apply(int x, int y, T t);

}
