package org.metastatic.sexp4j.test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;
import org.metastatic.sexp4j.*;

public class TestWriteParse {
    @Test
    public void testBasic() throws IOException {
        ExpressionList list = new ExpressionList();
        ExpressionList list1 = new ExpressionList();
        list1.add(new Atom("value".getBytes()));
        list1.add(new Atom("foo".getBytes()));
        list.add(list1);
        ExpressionList list2 = new ExpressionList();
        list2.add(new Atom("hash".getBytes()));
        list2.add(new Atom("0beec7b5ea3f0fdbc95d0dd47f3c5bc275da8a33".getBytes()));
        list.add(list2);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Writer writer = new CanonicalWriter(out);
        writer.writeExpression(list);
        byte[] bytes = out.toByteArray();
        System.out.println("wrote bytes: " + new String(bytes));
        Expression expr = new CanonicalParser(new ByteArrayInputStream(bytes)).parse();
        Assert.assertNotNull(expr);
        Assert.assertEquals(list, expr);
    }
}
