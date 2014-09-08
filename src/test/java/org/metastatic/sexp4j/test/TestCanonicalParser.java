package org.metastatic.sexp4j.test;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.junit.Test;
import org.junit.Assert;
import org.metastatic.sexp4j.Atom;
import org.metastatic.sexp4j.CanonicalParser;
import org.metastatic.sexp4j.Expression;
import org.metastatic.sexp4j.ExpressionList;

public class TestCanonicalParser {
    @Test
    public void testEmptyList() throws IOException {
        String input = "()";
        Expression expr = new CanonicalParser(new ByteArrayInputStream(input.getBytes())).parse();
        Assert.assertNotNull(expr);
        Assert.assertTrue(expr instanceof ExpressionList);
        Assert.assertEquals(0, ((ExpressionList) expr).size());
    }

    @Test
    public void testAtom() throws IOException {
        String input = "0:";
        Expression expr = new CanonicalParser(new ByteArrayInputStream(input.getBytes())).parse();
        Assert.assertNotNull(expr);
        Assert.assertTrue(expr instanceof Atom);
        Assert.assertEquals(0, ((Atom) expr).length());
    }
}
