package org.metastatic.sexp4j.test;

import org.junit.Test;
import org.metastatic.sexp4j.AdvancedWriter;
import org.metastatic.sexp4j.Expression;
import org.metastatic.sexp4j.ExpressionBuilder;
import org.metastatic.sexp4j.Primitives;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Random;

/**
 * Created by cmarshall on 12/4/14.
 */
public class TestAdvancedWriter {

    @Test
    public void test1() throws IOException {
        Random r = new Random(31337);
        ExpressionBuilder builder = ExpressionBuilder.create();

        builder.beginList();
        builder.atom("items");

        builder.beginList();
        builder.atom("foo");
        byte[] b = new byte[32];
        r.nextBytes(b);
        builder.atom(b);
        builder.endList();

        builder.beginList();
        builder.atom("bar");
        builder.atom("this is a simple sentence");
        builder.endList();

        builder.beginList();
        builder.atom("baz");
        builder.atom(Primitives.bytes(31337));
        builder.endList();

        builder.endList();

        Expression expr = builder.build();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        AdvancedWriter writer = AdvancedWriter.create().indentAmount(4).lineLength(40).outputStream(out).build();
        writer.writeExpression(expr);
        System.out.println(new String(out.toByteArray()));
    }
}
