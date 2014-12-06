package org.metastatic.sexp4j.test;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.junit.Test;
import org.junit.Assert;
import org.metastatic.sexp4j.*;

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

    @Test
    public void testAtom2() throws IOException {
        String input = "11:hello world";
        Expression expr = new CanonicalParser(new ByteArrayInputStream(input.getBytes())).parse();
        Assert.assertNotNull(expr);
        Assert.assertTrue(expr instanceof Atom);
        Assert.assertEquals(11, ((Atom) expr).length());
        Assert.assertArrayEquals("hello world".getBytes("UTF-8"), ((Atom) expr).bytes());
    }

    @Test
    public void testBasicExpr() throws IOException {
        String input = "((5:value3:foo)(4:hash40:0beec7b5ea3f0fdbc95d0dd47f3c5bc275da8a33))";
        Expression expr = new CanonicalParser(new ByteArrayInputStream(input.getBytes())).parse();
        Assert.assertNotNull(expr);
        Assert.assertTrue(expr instanceof ExpressionList);
        Assert.assertEquals(2, ((ExpressionList) expr).size());

        Expression expr1 = ((ExpressionList) expr).get(0);
        Assert.assertTrue(expr1 instanceof ExpressionList);
        Assert.assertEquals(2, ((ExpressionList) expr1).size());

        Expression expr1_1 = ((ExpressionList) expr1).get(0);
        Assert.assertTrue(expr1_1 instanceof Atom);
        Assert.assertArrayEquals("value".getBytes(), ((Atom) expr1_1).bytes());
        Expression expr1_2 = ((ExpressionList) expr1).get(1);
        Assert.assertTrue(expr1_2 instanceof Atom);
        Assert.assertArrayEquals("foo".getBytes(), ((Atom) expr1_2).bytes());

        Expression expr2 = ((ExpressionList) expr).get(1);
        Assert.assertTrue(expr2 instanceof ExpressionList);
        Assert.assertEquals(2, ((ExpressionList) expr2).size());

        Expression expr2_1 = ((ExpressionList) expr2).get(0);
        Assert.assertTrue(expr2_1 instanceof Atom);
        Assert.assertArrayEquals("hash".getBytes(), ((Atom) expr2_1).bytes());
        Expression expr2_2 = ((ExpressionList) expr2).get(1);
        Assert.assertTrue(expr2_2 instanceof Atom);
        Assert.assertArrayEquals("0beec7b5ea3f0fdbc95d0dd47f3c5bc275da8a33".getBytes(), ((Atom) expr2_2).bytes());
    }
}
