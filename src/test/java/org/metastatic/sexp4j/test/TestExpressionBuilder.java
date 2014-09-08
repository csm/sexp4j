package org.metastatic.sexp4j.test;

import org.junit.Assert;
import org.junit.Test;
import org.metastatic.sexp4j.Atom;
import org.metastatic.sexp4j.Expression;
import org.metastatic.sexp4j.ExpressionBuilder;
import org.metastatic.sexp4j.ExpressionList;

public class TestExpressionBuilder {
    @Test
    public void testBuilder() {
        ExpressionBuilder builder = new ExpressionBuilder();
        builder.beginList().beginList().atom("value").atom("foo").endList()
                .beginList().atom("hash").atom("0beec7b5ea3f0fdbc95d0dd47f3c5bc275da8a33");
        Expression expr = builder.build();
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
