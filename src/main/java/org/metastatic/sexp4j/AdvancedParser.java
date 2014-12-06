package org.metastatic.sexp4j;

import java.io.InputStream;

/**
 * Created by cmarshall on 12/5/14.
 */
public class AdvancedParser extends ParserBase {
    public AdvancedParser(InputStream in) {
        super(new AdvancedStreamingParser(in));
    }
}
