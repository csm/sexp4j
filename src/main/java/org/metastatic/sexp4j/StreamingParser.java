package org.metastatic.sexp4j;

import com.google.common.base.Optional;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public abstract class StreamingParser
{
    private final List<StreamingParserCallback> callbacks;
    protected final InputStream input;

    protected StreamingParser(InputStream input)
    {
        this.input = input;
        this.callbacks = new ArrayList<>();
    }

    public abstract void parse() throws IOException;

    public final void addCallback(StreamingParserCallback callback)
    {
        if (!callbacks.contains(callback))
            callbacks.add(callback);
    }

    public final void removeCallback(StreamingParserCallback callback)
    {
        callbacks.remove(callback);
    }

    protected final void onListBegin() throws ParseException
    {
        for (StreamingParserCallback callback : callbacks)
            callback.beginList();
    }

    protected final void onListEnd() throws ParseException
    {
        for (StreamingParserCallback callback : callbacks)
            callback.endList();
    }

    protected final void onAtom(byte[] atom, Optional<byte[]> displayHint) throws ParseException
    {
        for (StreamingParserCallback callback : callbacks)
            callback.onAtom(atom, displayHint);
    }
}
