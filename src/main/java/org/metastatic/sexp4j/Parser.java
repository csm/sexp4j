package org.metastatic.sexp4j;

import java.io.IOException;

/**
 * Created by cmarshall on 12/5/14.
 */
public interface Parser {
    public Expression parse() throws IOException;
}
