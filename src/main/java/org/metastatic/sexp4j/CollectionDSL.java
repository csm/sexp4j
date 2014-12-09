package org.metastatic.sexp4j;

import com.google.common.base.Preconditions;

import java.util.*;

/**
 * Created by cmarshall on 12/8/14.
 */
public class CollectionDSL {
    public static Map map(Object... keyValues) {
        Preconditions.checkArgument(keyValues.length % 2 == 0);
        Map ret = new LinkedHashMap(keyValues.length / 2);
        for (int i = 0; i < keyValues.length; i += 2) {
            ret.put(keyValues[i], keyValues[i + 1]);
        }
        return ret;
    }

    public static Set set(Object... values) {
        Map ret = new LinkedHashMap(values.length);
        for (Object value : values)
            ret.put(value, null);
        return ret.keySet();
    }

    public static List list(Object... values) {
        return Arrays.asList(values);
    }
}
