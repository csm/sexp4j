package org.metastatic.sexp4j;

import java.io.InputStream;

/**
 * Created by cmarshall on 12/5/14.
 */
public class CanonicalParser extends ParserBase {
    public CanonicalParser(InputStream in) {
        super(new CanonicalStreamingParser(in));
    }
}
