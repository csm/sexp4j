package org.metastatic.sexp4j;

import java.io.IOException;

public class ParseException extends IOException
{
    public ParseException() {
    }

    public ParseException(String message) {
        super(message);
    }

    public ParseException(String message, Throwable cause) {
        super(message, cause);
    }

    public ParseException(Throwable cause) {
        super(cause);
    }

    public ParseException(String format, Object... args) {
        super(String.format(format, args));
    }
}
