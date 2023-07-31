package com.aki.modfix.util.gl;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.IntFunction;
import java.util.stream.Collectors;

public class MapCreateHelper {
    public static <K, V> HashMap<K, V> CreateHashMap(K[] Keys, IntFunction<V> ValueFunc) {
        HashMap<K, V> map = new HashMap<>();

        for(int i = 0; i < Keys.length; i++) {
            map.put(Keys[i], ValueFunc.apply(i));
        }

        return map;
    }

    public static <K, V> LinkedHashMap<K, V> CreateLinkedHashMap(K[] Keys, IntFunction<V> ValueFunc) {
        return new LinkedHashMap<>(CreateHashMap(Keys, ValueFunc));
    }
}
