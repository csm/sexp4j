package org.metastatic.sexp4j.mapper.test;

import org.junit.Test;
import org.metastatic.sexp4j.AdvancedWriter;
import org.metastatic.sexp4j.Atom;
import org.metastatic.sexp4j.Expression;
import org.metastatic.sexp4j.mapper.ObjectMapper;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;

import static org.junit.Assert.*;

/**
 * Created by cmarshall on 12/8/14.
 */
public class TestObjectMapper {

    private ObjectMapper mapper = new ObjectMapper();

    @Test
    public void testByte() throws Exception {
        Expression e = mapper.writeObject((byte) 0xab);
        assertTrue(e instanceof Atom);
        assertEquals(1, ((Atom) e).length());
        byte b = mapper.readObject(e, Byte.class);
        assertEquals((byte) 0xab, b);
    }

    @Test
    public void testChar() throws Exception {
        Expression e = mapper.writeObject('A');
        assertTrue(e instanceof Atom);
        assertEquals(2, ((Atom) e).length());
        char c = mapper.readObject(e, Character.class);
        assertEquals('A', c);
    }

    @Test
    public void testShort() throws Exception {
        Expression e = mapper.writeObject((short) 1234);
        assertTrue(e instanceof Atom);
        assertEquals(2, ((Atom) e).length());
        short s = mapper.readObject(e, Short.class);
        assertEquals(1234, s);
    }

    @Test
    public void testInt() throws Exception {
        Expression e = mapper.writeObject(0xcafebabe);
        assertTrue(e instanceof Atom);
        assertEquals(4, ((Atom) e).length());
        int i = mapper.readObject(e, Integer.class);
        assertEquals(0xcafebabe, i);
    }

    @Test
    public void testLong() throws Exception {
        Expression e = mapper.writeObject(5000000000L);
        assertTrue(e instanceof Atom);
        assertEquals(8, ((Atom) e).length());
        long l = mapper.readObject(e, Long.class);
        assertEquals(5000000000L, l);
    }

    @Test
    public void testFloat() throws Exception {
        Expression e = mapper.writeObject((float) 3.14);
        assertTrue(e instanceof Atom);
        assertEquals(4, ((Atom) e).length());
        float f = mapper.readObject(e, Float.class);
        assertEquals((float) 3.14, f, (float) 0.001);
    }

    @Test
    public void testDouble() throws Exception {
        Expression e = mapper.writeObject(3.14);
        assertTrue(e instanceof Atom);
        assertEquals(8, ((Atom) e).length());
        double d = mapper.readObject(e, Double.class);
        assertEquals(3.14, d, 0.001);
    }

    @Test
    public void testString() throws Exception {
        String expect = "this is a string";
        Expression e = mapper.writeObject(expect);
        assertTrue(e instanceof Atom);
        assertEquals(expect.length(), ((Atom) e).length());  // should be the same in UTF-8
        String s = mapper.readObject(e, String.class);
        assertEquals(expect, s);
    }

    @Test
    public void testBigInteger() throws Exception {
        BigInteger expect = BigInteger.valueOf(2).pow(128).subtract(BigInteger.ONE);
        Expression e = mapper.writeObject(expect);
        assertTrue(e instanceof Atom);
        BigInteger bi = mapper.readObject(e, BigInteger.class);
        assertEquals(expect, bi);
    }

    @Test
    public void testBigDecimal() throws Exception {
        BigDecimal expect = BigDecimal.valueOf(355).divide(BigDecimal.valueOf(113), BigDecimal.ROUND_DOWN);
        Expression e = mapper.writeObject(expect);
        assertTrue(e instanceof Atom);
        BigDecimal bd = mapper.readObject(e, BigDecimal.class);
        assertEquals(expect, bd);
    }

    public static class PojoExample {
        public byte byteValue;
        public short shortValue;
        public char charValue;
        public int intValue;

        private String stringValue;
        private double doubleValue;
        private byte[] bytesValue;

        public PojoExample() {

        }

        public String getStringValue() {
            return stringValue;
        }

        public void setStringValue(String stringValue) {
            this.stringValue = stringValue;
        }

        public double getDoubleValue() {
            return doubleValue;
        }

        public void setDoubleValue(double doubleValue) {
            this.doubleValue = doubleValue;
        }

        public byte[] getBytesValue() {
            return bytesValue;
        }

        public void setBytesValue(byte[] bytesValue) {
            this.bytesValue = bytesValue;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            PojoExample that = (PojoExample) o;

            if (byteValue != that.byteValue) return false;
            if (charValue != that.charValue) return false;
            if (Double.compare(that.doubleValue, doubleValue) != 0) return false;
            if (intValue != that.intValue) return false;
            if (shortValue != that.shortValue) return false;
            if (!Arrays.equals(bytesValue, that.bytesValue)) return false;
            if (stringValue != null ? !stringValue.equals(that.stringValue) : that.stringValue != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result;
            long temp;
            result = (int) byteValue;
            result = 31 * result + (int) shortValue;
            result = 31 * result + (int) charValue;
            result = 31 * result + intValue;
            result = 31 * result + (stringValue != null ? stringValue.hashCode() : 0);
            temp = Double.doubleToLongBits(doubleValue);
            result = 31 * result + (int) (temp ^ (temp >>> 32));
            result = 31 * result + (bytesValue != null ? Arrays.hashCode(bytesValue) : 0);
            return result;
        }

        @Override
        public String toString() {
            return "PojoExample{" +
                    "byteValue=" + byteValue +
                    ", shortValue=" + shortValue +
                    ", charValue=" + charValue +
                    ", intValue=" + intValue +
                    ", stringValue='" + stringValue + '\'' +
                    ", doubleValue=" + doubleValue +
                    ", bytesValue=" + Arrays.toString(bytesValue) +
                    '}';
        }
    }

    @Test
    public void testPojo() throws Exception {
        PojoExample expect = new PojoExample();
        expect.byteValue = (byte) 0x1f;
        expect.charValue = 'x';
        expect.shortValue = 8080;
        expect.intValue = 1234567;
        expect.setBytesValue("some bytes".getBytes());
        expect.setStringValue("a string");
        expect.setDoubleValue(3.14);
        Expression e = mapper.writeObject(expect);
        AdvancedWriter.create().outputStream(System.out).build().writeExpression(e);
        PojoExample pe = mapper.readObject(e, PojoExample.class);
        assertEquals(expect, pe);
    }
}
