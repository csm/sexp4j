package org.metastatic.sexp4j.mapper;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

import com.google.common.annotations.Beta;
import org.metastatic.sexp4j.Atom;
import org.metastatic.sexp4j.Expression;
import org.metastatic.sexp4j.ExpressionList;

/**
 * A "simple" mapper between Java objects and expressions.
 *
 * <p>The definition of a "simple" object is:</p>
 *
 * <ul>
 *     <li>Any "primitive" type. These will be encoded as atoms, with one-character display hints, as follows:
 *     <ul>
 *         <li>boolean, a byte 0 or 1, with display hint "z".</li>
 *         <li>byte, one byte, with display hint "b".</li>
 *         <li>short, two bytes (multi-byte primitives are in big endian order), display hint "s".</li>
 *         <li>char, two bytes, display hint "c".</li>
 *         <li>int, four bytes, display hint "i".</li>
 *         <li>long, eight bytes, display hint "l".</li>
 *         <li>float, four bytes, display hint "f".</li>
 *         <li>double, eight bytes, display hint "d".</li>
 *         <li>nulls, zero bytes, display hint "n".</li>
 *         <li>Strings, utf-8 bytes, display hint "S".</li>
 *         <li>byte arrays, as-is, display hint "B".</li>
 *         <li>BigInteger, result of toByteArray, display hint "I".</li>
 *         <li>BigDecimal, result of toString then encoded as UTF-8, display hint "D".</li>
 *     </ul></li>
 *     <li>A list of simpler objects. This is encoded as a list: a type code atom "l", then the sequence of simpler expressions.</li>
 *     <li>A set of simpler objects. This is encoded as a list: a type code atom "s", then the sequence of simpler expressions.</li>
 *     <li>A map of strings to any simpler type. This is encoded as a list: a type code atom "m", then a sequence of key/value expressions.
 *     The key expression is always a "string", that is, an atom, without any display-hint; the value can be any expression.</li>
 * </ul>
 *
 * <p>An example encoding:</p>
 *
 * <pre>
(m aBool [z] #01#
   aNull [n] 0:
   aByte [b] #1f#
   aShort [s] #007b#
   anInt [i] #0001e240#
   aLong [l] #00000002dfdc1c34#
   aFloat [f] #4048f5c3#
   aDouble [d] #400920c49ba5e354#
   bytes [B] "just some bytes"
   string [S] "just a string"
   bigint [I] |EkmtJZTDfOsLJ4TEzgvzis5AjiEafKqyQwioLo8QAAAAAAAAAAAAAAAA|
   bigdec [D] "3.1415929203"
   list (l [S] just [S] some [S] items [S] in [S] a [S] list)
   set (s [S] more [S] items [S] in [S] a [S] set [S] but [S] no [S] repeated)
   map (m submaps [z] #01#))
 * </pre>
 */
@Beta
public class SimpleMapper {
    public static enum Type {
        Null((byte) 'n'),
        Bool((byte) 'z'),
        Byte((byte) 'b'),
        Short((byte) 's'),
        Char((byte) 'c'),
        Int((byte) 'i'),
        Long((byte) 'l'),
        Float((byte) 'f'),
        Double((byte) 'd'),
        String((byte) 'S'),
        BigInt((byte) 'I'),
        BigDecimal((byte) 'D'),
        Bytes((byte) 'B'),
        List((byte) 'l'),
        Set((byte) 's'),
        Map((byte) 'm');

        private Type(byte code) {
            this.code = code;
        }

        private final byte code;
    }

    private Atom atom(byte code, byte[] bytes) {
        return new Atom(code, bytes);
    }

    public Expression encode(Object o) throws MapperException {
        if (o == null)
            return new Atom(new byte[0]).withHint(Type.Null.code);
        Class clazz = o.getClass();
        if (clazz.equals(Boolean.class))
            return new Atom(new byte[] { (boolean) o ? (byte) 1 : (byte) 0 }).withHint(Type.Bool.code);
        if (clazz.equals(Byte.class))
            return new Atom(new byte[] { (byte) o }).withHint(Type.Byte.code);
        if (clazz.equals(Short.class))
            return Atom.atom((short) o).withHint(Type.Short.code);
        if (clazz.equals(Character.class))
            return Atom.atom((char) o).withHint(Type.Char.code);
        if (clazz.equals(Integer.class))
            return Atom.atom((int) o).withHint(Type.Int.code);
        if (clazz.equals(Long.class))
            return Atom.atom((long) o).withHint(Type.Long.code);
        if (clazz.equals(Float.class))
            return Atom.atom((float) o).withHint(Type.Float.code);
        if (clazz.equals(Double.class))
            return Atom.atom((double) o).withHint(Type.Double.code);
        if (clazz.equals(String.class))
            return Atom.atom((String) o).withHint(Type.String.code);
        if (clazz.equals(BigInteger.class))
            return new Atom(((BigInteger) o).toByteArray()).withHint(Type.BigInt.code);
        if (clazz.equals(BigDecimal.class))
            return Atom.atom(o.toString()).withHint(Type.BigDecimal.code);
        if (clazz.isArray() && clazz.getComponentType().equals(Byte.TYPE))
            return new Atom((byte[]) o).withHint(Type.Bytes.code);
        if (List.class.isAssignableFrom(clazz)) {
            ExpressionList list = new ExpressionList(((List) o).size() + 1);
            list.add(Atom.atom(Type.List.code));
            for (Object e : ((List) o))
                list.add(encode(e));
            return list;
        }
        if (Set.class.isAssignableFrom(clazz)) {
            ExpressionList list = new ExpressionList(((Set) o).size() + 1);
            list.add(Atom.atom(Type.Set.code));
            for (Object e : (Set) o)
                list.add(encode(e));
            return list;
        }
        if (Map.class.isAssignableFrom(clazz)) {
            ExpressionList list = new ExpressionList(((Map) o).size() * 2 + 1);
            list.add(Atom.atom(Type.Map.code));
            for (Object e : ((Map) o).entrySet()) {
                Map.Entry entry = (Map.Entry) e;
                if (!(entry.getKey() instanceof String) || ((String) entry.getKey()).isEmpty())
                    throw new MapperException("map keys must be nonempty strings");
                list.add(Atom.atom((String) entry.getKey()));
                list.add(encode(((Map.Entry) e).getValue()));
            }
            return list;
        }
        throw new MapperException("don't know how to encode a " + clazz);
    }

    public Object decode(Expression e) throws MapperException {
        if (e instanceof Atom) {
            byte code = '['; // if no explicit display hint, default to plain byte arrays.
            if (((Atom) e).displayHint().isPresent()) {
                code = ((Atom) e).displayHint().get().atom().typeCode();
            }
            switch (code) {
                case 'z': return ((Atom) e).byteValue() != 0;
                case 'n': return null;
                case 'b': return ((Atom) e).byteValue();
                case 's': return ((Atom) e).shortValue();
                case 'c': return ((Atom) e).charValue();
                case 'i': return ((Atom) e).intValue();
                case 'l': return ((Atom) e).longValue();
                case 'f': return ((Atom) e).floatValue();
                case 'd': return ((Atom) e).doubleValue();
                case 'S': return ((Atom) e).stringValue();
                case 'B': return ((Atom) e).bytes();
                case 'I': return ((Atom) e).bigIntegerValue();
                case 'D': return ((Atom) e).bigDecimalValue();
                default: throw new MapperException("invalid atom type code: %02x", ((Atom) e).typeCode());
            }
        }
        else {
            ExpressionList list = (ExpressionList) e;
            Expression code = list.get(0);
            if (!(code instanceof Atom))
                throw new MapperException("expecting type code byte prefixing list");
            switch (((Atom) code).typeCode()) {
                case 'l': {
                    List l = new ArrayList<>(list.size() - 1);
                    for (Expression ex : list.subList(1, list.size()))
                        l.add(decode(ex));
                    return l;
                }
                case 's': {
                    LinkedHashMap m = new LinkedHashMap(list.size() - 1);
                    for (Expression ex : list.subList(1, list.size())) {
                        Object v = decode(ex);
                        m.put(v, v);
                    }
                    return m.keySet();
                }
                case 'm': {
                    if (list.size() % 2 != 1) {
                        throw new MapperException("maps must have an even number of items");
                    }
                    LinkedHashMap m = new LinkedHashMap(list.size() / 2 + 1);
                    for (int i = 1; i + 1 < list.size(); i += 2) {
                        Object key = list.get(i);
                        if (!(key instanceof Atom) || ((Atom) key).length() == 0)
                            throw new MapperException("map keys must be nonempty atoms");
                        Object value = decode(list.get(i + 1));
                        m.put(((Atom) key).stringValue(), value);
                    }
                    return m;
                }
                default:
                    throw new MapperException("invalid type code: 0x%02x", ((Atom) code).typeCode());
            }
        }
    }
}
