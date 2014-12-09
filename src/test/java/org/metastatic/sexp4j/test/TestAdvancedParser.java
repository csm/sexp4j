package org.metastatic.sexp4j.test;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;
import org.metastatic.sexp4j.*;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Created by cmarshall on 12/5/14.
 */
public class TestAdvancedParser {

    private int parseOctal(String octal) {
        int e1 = octal.charAt(0);
        int e2 = octal.charAt(1);
        int e3 = octal.charAt(2);
        return ((e1 - '0') << 6) | ((e2 - '0') << 3) | (e3 - '0');
    }

    @Test
    public void testOctalParsing() throws IOException {
        for (int i = 0; i < 256; i++) {
            String s = String.format("\"\\%03o\"", i);
            Atom atom = (Atom) new AdvancedParser(new ByteArrayInputStream(s.getBytes())).parse();
            Assert.assertEquals(i, atom.bytes()[0] & 0xff);
        }
    }

    @Test
    public void testParse1() throws IOException {
        AdvancedParser parser = new AdvancedParser(new ByteArrayInputStream("()".getBytes()));
        Expression e = parser.parse();
        Assert.assertTrue(e instanceof ExpressionList);
        Assert.assertEquals(0, ((ExpressionList) e).size());
    }

    @Test
    public void testParse2() throws IOException {
        String expr = "(this 2:is #61# |c2FtcGxl| \"s-expression in \\\n\\x61dvanced \\146ormat\")";
        AdvancedParser parser = new AdvancedParser(new ByteArrayInputStream(expr.getBytes()));
        Expression e = parser.parse();
        Assert.assertTrue(e instanceof ExpressionList);
        Assert.assertEquals(5, ((ExpressionList) e).size());
        Assert.assertEquals(Atom.atom("this"), ((ExpressionList) e).get(0));
        Assert.assertEquals(Atom.atom("is"), ((ExpressionList) e).get(1));
        Assert.assertEquals(Atom.atom("a"), ((ExpressionList) e).get(2));
        Assert.assertEquals(Atom.atom("sample"), ((ExpressionList) e).get(3));
        Assert.assertEquals(String.format("actual: %s", new String(((Atom) ((ExpressionList) e).get(4)).bytes())), Atom.atom("s-expression in advanced format"), ((ExpressionList) e).get(4));
    }

    @Test
    public void testHints1() throws IOException {
        String input = "([hint] atom)";
        Expression expr = new AdvancedParser(new ByteArrayInputStream(input.getBytes())).parse();
        assertThat(expr, instanceOf(ExpressionList.class));
        assertThat(((ExpressionList) expr).size(), is(1));
        assertThat(((ExpressionList) expr).get(0), instanceOf(Atom.class));
        assertTrue(((Atom) ((ExpressionList) expr).get(0)).displayHint().isPresent());
        assertThat(((Atom) ((ExpressionList) expr).get(0)).displayHint().get().atom().stringValue(),
                is("hint"));
        assertThat(((Atom) ((ExpressionList) expr).get(0)).stringValue(), is("atom"));
    }
}
