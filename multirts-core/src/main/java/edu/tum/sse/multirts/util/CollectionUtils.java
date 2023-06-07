package edu.tum.sse.multirts.util;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public final class CollectionUtils {
    @SafeVarargs
    public static <T> Set<T> newSet(T... objs) {
        return Arrays.stream(objs).collect(Collectors.toSet());
    }

    @SafeVarargs
    public static <T> List<T> newList(T... objs) {
        return Arrays.stream(objs).collect(Collectors.toList());
    }

    @SafeVarargs
    public static <K, V> Map<K, V> newMap(Map.Entry<K, V>... entries) {
        return Arrays.stream(entries).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
