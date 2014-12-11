package org.metastatic.sexp4j;

import java.io.InputStream;

/**
 * A parser for the "advanced" encoding.
 */
public class AdvancedParser extends ParserBase {
    public AdvancedParser(InputStream in) {
        super(new AdvancedStreamingParser(in));
    }
}
