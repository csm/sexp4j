package org.metastatic.sexp4j;

import java.io.InputStream;

/**
 * A parser for the canonical encoding.
 */
public class CanonicalParser extends ParserBase {
    /**
     * Create a new canonical parser.
     * @param in The input stream.
     */
    public CanonicalParser(InputStream in) {
        super(new CanonicalStreamingParser(in));
    }
}
