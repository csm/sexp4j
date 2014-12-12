package org.metastatic.sexp4j;

import com.google.common.base.Preconditions;

import java.util.*;

/**
 * DSL for creating collections.
 */
public class CollectionDSL {
    /**
     * Create a linked hash map with the given key/values.
     *
     * @param keyValues A sequence of pairs of keys and values.
     * @return The new map.
     */
    public static Map map(Object... keyValues) {
        Preconditions.checkArgument(keyValues.length % 2 == 0);
        Map ret = new LinkedHashMap(keyValues.length / 2);
        for (int i = 0; i < keyValues.length; i += 2) {
            ret.put(keyValues[i], keyValues[i + 1]);
        }
        return ret;
    }

    /**
     * Return a set (hashed, but preserving order) of the given values.
     *
     * @param values The values.
     * @return The set.
     */
    public static Set set(Object... values) {
        Map ret = new LinkedHashMap(values.length);
        for (Object value : values)
            ret.put(value, null);
        return ret.keySet();
    }

    /**
     * Return a list of objects.
     *
     * @param values The values.
     * @return The list.
     */
    public static List list(Object... values) {
        return Arrays.asList(values);
    }
}
