package org.metastatic.sexp4j.mapper;

import org.metastatic.sexp4j.Writer;

/**
 * Created by cmarshall on 12/4/14.
 */
public class ObjectMapper {
    public void writeObject(Writer writer, Object o) {
        Class clazz = o.getClass();
        if (clazz.isPrimitive()) {

        }
    }
}
