package org.metastatic.sexp4j;

import java.io.IOException;
import java.io.InputStream;

import com.google.common.io.ByteStreams;

public class CanonicalStreamingParser extends StreamingParser
{
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
            if (ch == '(')
                onListBegin();
            else if (ch == ')')
                onListEnd();
            else if (Character.isDigit(ch))
            {
                int len = readLength(ch);
                byte[] buffer = new byte[len];
                ByteStreams.readFully(input, buffer);
            }
            else
                throw new ParseException("invalid character in stream: %c", ch);
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
