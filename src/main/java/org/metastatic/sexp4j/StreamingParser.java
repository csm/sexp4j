package org.metastatic.sexp4j;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Base class for a streaming parser.
 *
 * <p>To use a streaming parser, you register one or more
 * {@link org.metastatic.sexp4j.StreamingParserCallback} instances
 * with this object, which will be called as expressions are read
 * from the underlying input stream.</p>
 */
public abstract class StreamingParser
{
    private final List<StreamingParserCallback> callbacks;
    protected final InputStream input;

    /**
     * Construct a new parser.
     *
     * @param input The input stream to parse.
     * @throws java.lang.NullPointerException If the argument is null.
     */
    protected StreamingParser(InputStream input)
    {
        Preconditions.checkNotNull(input);
        this.input = input;
        this.callbacks = new ArrayList<>();
    }

    /**
     * Parse the input, calling the callbacks as expressions are read.
     *
     * @throws org.metastatic.sexp4j.ParseException If the input is invalid.
     * @throws IOException If an IO exception occurs.
     */
    public abstract void parse() throws IOException;

    /**
     * Add a callback to this parser.
     *
     * @param callback The callback to add.
     * @throws java.lang.NullPointerException If the argument is null.
     */
    public final void addCallback(StreamingParserCallback callback)
    {
        Preconditions.checkNotNull(callback);
        if (!callbacks.contains(callback))
            callbacks.add(callback);
    }

    /**
     * Remove a callback from this parser.
     *
     * @param callback The callback to remove.
     */
    public final void removeCallback(StreamingParserCallback callback) {
        callbacks.remove(callback);
    }

    /**
     * Called when a list begins.
     *
     * @throws ParseException If a list is not appropriate given the current parser state.
     */
    protected final void onListBegin() throws ParseException
    {
        for (StreamingParserCallback callback : callbacks)
            callback.beginList();
    }

    /**
     * Called when a list ends.
     *
     * @throws ParseException If a list end is not appropriate given the current parser state.
     */
    protected final void onListEnd() throws ParseException
    {
        for (StreamingParserCallback callback : callbacks)
            callback.endList();
    }

    /**
     * Called when an atom is read.
     *
     * @param atom The atom bytes.
     * @param displayHint The display hint bytes, if any were present.
     * @throws ParseException If an atom is not appropriate given the current parser state.
     */
    protected final void onAtom(byte[] atom, Optional<byte[]> displayHint) throws ParseException
    {
        for (StreamingParserCallback callback : callbacks)
            callback.onAtom(atom, displayHint);
    }
}
