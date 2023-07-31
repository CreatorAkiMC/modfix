package com.aki.modfix.util.cache;

@FunctionalInterface
public interface IntInt2ObjFunction<R> {

    R apply(int x, int y);

}
