package com.aki.modfix.util.cache;

@FunctionalInterface
public interface IntIntInt2ObjFunction<R> {

    R apply(int x, int y, int z);

}
