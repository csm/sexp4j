package org.metastatic.sexp4j;

/**
 * A callback for parsing s-expressions.
 */
public interface StreamingParserCallback
{
    /**
     * Called on the beginning of a list.
     */
    default void beginList() throws ParseException
    {
    }

    /**
     * Called on the end of a list.
     */
    default void endList() throws ParseException
    {
    }

    /**
     * Called when parsing an atom.
     *
     * @param atom The atom.
     */
    default void onAtom(byte[] atom) throws ParseException
    {
    }
}
