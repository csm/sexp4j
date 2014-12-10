package org.metastatic.sexp4j.test;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.junit.Test;
import org.junit.Assert;
import org.metastatic.sexp4j.*;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class TestCanonicalParser {
    @Test
    public void testEmptyList() throws IOException {
        String input = "()";
        Expression expr = new CanonicalParser(new ByteArrayInputStream(input.getBytes())).parse();
        Assert.assertNotNull(expr);
        assertTrue(expr instanceof ExpressionList);
        Assert.assertEquals(0, ((ExpressionList) expr).size());
    }

    @Test
    public void testAtom() throws IOException {
        String input = "0:";
        Expression expr = new CanonicalParser(new ByteArrayInputStream(input.getBytes())).parse();
        Assert.assertNotNull(expr);
        assertTrue(expr instanceof Atom);
        Assert.assertEquals(0, ((Atom) expr).length());
    }

    @Test
    public void testAtom2() throws IOException {
        String input = "11:hello world";
        Expression expr = new CanonicalParser(new ByteArrayInputStream(input.getBytes())).parse();
        Assert.assertNotNull(expr);
        assertTrue(expr instanceof Atom);
        Assert.assertEquals(11, ((Atom) expr).length());
        Assert.assertArrayEquals("hello world".getBytes("UTF-8"), ((Atom) expr).bytes());
    }

    @Test
    public void testBasicExpr() throws IOException {
        String input = "((5:value3:foo)(4:hash40:0beec7b5ea3f0fdbc95d0dd47f3c5bc275da8a33))";
        Expression expr = new CanonicalParser(new ByteArrayInputStream(input.getBytes())).parse();
        Assert.assertNotNull(expr);
        assertTrue(expr instanceof ExpressionList);
        Assert.assertEquals(2, ((ExpressionList) expr).size());

        Expression expr1 = ((ExpressionList) expr).get(0);
        assertTrue(expr1 instanceof ExpressionList);
        Assert.assertEquals(2, ((ExpressionList) expr1).size());

        Expression expr1_1 = ((ExpressionList) expr1).get(0);
        assertTrue(expr1_1 instanceof Atom);
        Assert.assertArrayEquals("value".getBytes(), ((Atom) expr1_1).bytes());
        Expression expr1_2 = ((ExpressionList) expr1).get(1);
        assertTrue(expr1_2 instanceof Atom);
        Assert.assertArrayEquals("foo".getBytes(), ((Atom) expr1_2).bytes());

        Expression expr2 = ((ExpressionList) expr).get(1);
        assertTrue(expr2 instanceof ExpressionList);
        Assert.assertEquals(2, ((ExpressionList) expr2).size());

        Expression expr2_1 = ((ExpressionList) expr2).get(0);
        assertTrue(expr2_1 instanceof Atom);
        Assert.assertArrayEquals("hash".getBytes(), ((Atom) expr2_1).bytes());
        Expression expr2_2 = ((ExpressionList) expr2).get(1);
        assertTrue(expr2_2 instanceof Atom);
        Assert.assertArrayEquals("0beec7b5ea3f0fdbc95d0dd47f3c5bc275da8a33".getBytes(), ((Atom) expr2_2).bytes());
    }

    @Test
    public void testHints1() throws IOException {
        String input = "([4:hint]4:atom5:atom2)";
        Expression expr = new CanonicalParser(new ByteArrayInputStream(input.getBytes())).parse();
        assertThat(expr, instanceOf(ExpressionList.class));
        assertThat(((ExpressionList) expr).size(), is(2));
        assertThat(((ExpressionList) expr).get(0), instanceOf(Atom.class));
        assertTrue(((Atom) ((ExpressionList) expr).get(0)).displayHint().isPresent());
        assertThat(((Atom) ((ExpressionList) expr).get(0)).displayHint().get().atom().stringValue(),
                is("hint"));
        assertThat(((Atom) ((ExpressionList) expr).get(0)).stringValue(), is("atom"));
        assertThat(((ExpressionList) expr).get(1), instanceOf(Atom.class));
        assertFalse(((Atom) ((ExpressionList) expr).get(1)).displayHint().isPresent());
        assertThat(((Atom) ((ExpressionList) expr).get(1)).stringValue(), is("atom2"));
    }
}
