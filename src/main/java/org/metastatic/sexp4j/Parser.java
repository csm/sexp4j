package org.metastatic.sexp4j;

import java.io.IOException;

/**
 * An expression parser.
 */
public interface Parser {
    /**
     * Parse an expression, and return it.
     *
     * @return The parsed expression.
     * @throws org.metastatic.sexp4j.ParseException If the input being parsed is invalid.
     * @throws IOException If an IO exception occurs.
     */
    public Expression parse() throws IOException;
}
