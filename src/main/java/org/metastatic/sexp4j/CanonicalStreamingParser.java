package org.metastatic.sexp4j;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import com.google.common.io.ByteStreams;

public class CanonicalStreamingParser extends StreamingParser
{
    private byte[] displayHint = null;

    public CanonicalStreamingParser(InputStream input)
    {
        super(input);
    }

    @Override
    public void parse() throws IOException
    {
        int ch;
        while ((ch = input.read()) != -1)
        {
            if (ch == '(') {
                onListBegin();
            }
            else if (ch == ')') {
                onListEnd();
            }
            else if (ch == '[') {
                int len = readLength(input.read());
                displayHint = new byte[len];
                ByteStreams.readFully(input, displayHint);
                if (input.read() != ']') {
                    throw new ParseException("missing display hint terminator");
                }
            }
            else if (Character.isDigit(ch))
            {
                int len = readLength(ch);
                byte[] buffer = new byte[len];
                ByteStreams.readFully(input, buffer);
                onAtom(buffer, Optional.ofNullable(displayHint));
                displayHint = null;
            }
            else if (Character.isWhitespace(ch)) {
                continue;
            }
            else {
                throw new ParseException("invalid character in stream: %c", ch);
            }
        }
    }

    private int readLength(int char1) throws IOException
    {
        int result = (char1 - '0');
        int ch;
        while ((ch = input.read()) != -1)
        {
            if (ch == ':')
                break;
            else if (Character.isDigit(ch))
            {
                int r = result * 10;
                int next = (ch - '0');
                if (r < result || r + next < result)
                    throw new ParseException("integer overflow: atom length is greater than 2^31-1");
                result = r + next;
            }
            else
                throw new ParseException("invalid character in stream: %c", ch);
        }
        return result;
    }
}
