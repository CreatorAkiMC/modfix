package com.aki.modfix.util.cache;

@FunctionalInterface
public interface IntObjConsumer<T> {

    void accept(int i, T t);

}
