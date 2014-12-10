package org.metastatic.sexp4j.mapper.test;

import org.hamcrest.core.IsInstanceOf;
import org.junit.Test;
import org.metastatic.sexp4j.AdvancedWriter;
import org.metastatic.sexp4j.Expression;
import org.metastatic.sexp4j.mapper.MapperException;
import org.metastatic.sexp4j.mapper.SimpleMapper;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Map;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.*;
import static org.metastatic.sexp4j.CollectionDSL.*;

/**
 * Created by cmarshall on 12/8/14.
 */
public class TestSimpleMapper {
    SimpleMapper mapper = new SimpleMapper();

    @Test
    public void testMap() throws IOException {
        Map m = map("aBool", Boolean.TRUE,
                    "aNull", null,
                    "aByte", (byte) 0x1f,
                    "aShort", (short) 123,
                    "anInt", 123456,
                    "aLong", 12345678900L,
                    "aFloat", (float) 3.14,
                    "aDouble", 3.141,
                    "bytes", "just some bytes".getBytes(),
                    "string", "just a string",
                    "bigint", BigInteger.TEN.pow(100),
                    "bigdec", BigDecimal.valueOf(355, 10).divide(BigDecimal.valueOf(113, 10), BigDecimal.ROUND_FLOOR),
                    "list", list("just", "some", "items", "in", "a", "list"),
                    "set", set("more", "items", "in", "a", "set", "but", "no", "items", "repeated"),
                    "map", map("submaps", Boolean.TRUE));
        Expression e = mapper.encode(m);
        AdvancedWriter.create().outputStream(System.out).lineLength(20).indentAmount(2).build().writeExpression(e);
        Object o = mapper.decode(e);
        assertThat(o, instanceOf(Map.class));
        Map m2 = (Map) o;
        assertEquals(Boolean.TRUE, m2.get("aBool"));
        assertTrue(m2.containsKey("aNull"));
        assertNull(m2.get("aNull"));
        assertEquals((byte) 0x1f, m2.get("aByte"));
        assertEquals((short) 123, m2.get("aShort"));
        assertEquals(123456, m2.get("anInt"));
        assertEquals(12345678900L, m2.get("aLong"));
    }
}
