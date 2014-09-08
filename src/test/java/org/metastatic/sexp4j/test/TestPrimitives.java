package org.metastatic.sexp4j.test;

import org.junit.Assert;
import org.junit.Test;
import org.metastatic.sexp4j.Primitives;

public class TestPrimitives {
    @Test
    public void testInt() {
        int value = 0xcafebabe;
        byte[] bytes = Primitives.bytes(value);
        Assert.assertArrayEquals(new byte[] {(byte) 0xca, (byte) 0xfe, (byte) 0xba, (byte) 0xbe}, bytes);
        Assert.assertEquals(value, Primitives.toInt(bytes));
    }

    @Test
    public void testLong() {
        long value = 0xdeadcafebabebeefL;
        byte[] bytes = Primitives.bytes(value);
        Assert.assertArrayEquals(new byte[] {(byte) 0xde, (byte) 0xad, (byte) 0xca, (byte) 0xfe, (byte) 0xba, (byte) 0xbe, (byte) 0xbe, (byte) 0xef}, bytes);
        Assert.assertEquals(value, Primitives.toLong(bytes));
    }
}
