package org.metastatic.sexp4j;

import com.google.common.base.Optional;

/**
 * A callback for parsing s-expressions.
 */
public interface StreamingParserCallback
{
    /**
     * Called on the beginning of a list.
     */
    void beginList() throws ParseException;

    /**
     * Called on the end of a list.
     */
    void endList() throws ParseException;

    /**
     * Called when parsing an atom.
     *
     * @param atom The atom.
     */
    void onAtom(byte[] atom, Optional<byte[]> displayHint) throws ParseException;
}
