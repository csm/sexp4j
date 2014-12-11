package org.metastatic.sexp4j;

import com.google.common.base.Optional;

/**
 * A callback for parsing s-expressions.
 */
public interface StreamingParserCallback
{
    /**
     * Called on the beginning of a list.
     *
     * @throws org.metastatic.sexp4j.ParseException If a list is not appropriate given the parser's current state.
     */
    void beginList() throws ParseException;

    /**
     * Called on the end of a list.
     *
     * @throws org.metastatic.sexp4j.ParseException If ending a list is not appropriate given the current state.
     */
    void endList() throws ParseException;

    /**
     * Called when parsing an atom.
     *
     * @param atom The atom.
     * @param displayHint The display hint, if present.
     * @throws org.metastatic.sexp4j.ParseException If an atom is not appropriate given the current parser state.
     */
    void onAtom(byte[] atom, Optional<byte[]> displayHint) throws ParseException;
}
