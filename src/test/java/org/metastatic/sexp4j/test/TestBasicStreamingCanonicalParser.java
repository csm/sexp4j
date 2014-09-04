package org.metastatic.sexp4j.test;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;
import org.metastatic.sexp4j.CanonicalStreamingParser;
import org.metastatic.sexp4j.ParseException;

public class TestBasicStreamingCanonicalParser
{
    @Test
    public void testInvalidAtomLength()
    {
        String input = "2147483648:";
        CanonicalStreamingParser parser = new CanonicalStreamingParser(new ByteArrayInputStream(input.getBytes()));
        try
        {
            parser.parse();
            Assert.fail("this should have failed");
        }
        catch (ParseException pe)
        {
            // pass
        }
        catch (IOException ioe)
        {
            Assert.fail("expected a ParseException");
        }
    }
}
