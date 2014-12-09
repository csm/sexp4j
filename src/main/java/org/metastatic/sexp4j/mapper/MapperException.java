package org.metastatic.sexp4j.mapper;

import java.io.IOException;

/**
 * Created by cmarshall on 12/8/14.
 */
public class MapperException extends IOException {
    public MapperException() {
    }

    public MapperException(String message) {
        super(message);
    }

    public MapperException(String message, Throwable cause) {
        super(message, cause);
    }

    public MapperException(Throwable cause) {
        super(cause);
    }

    public MapperException(String format, Object... args) {
        this(String.format(format, args));
    }
}
