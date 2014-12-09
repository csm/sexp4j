package org.metastatic.sexp4j.mapper;

import org.metastatic.sexp4j.Atom;
import org.metastatic.sexp4j.Expression;
import org.metastatic.sexp4j.ExpressionList;
import org.metastatic.sexp4j.Primitives;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Types;
import java.util.*;

/**
 * A "simple" mapper between Java objects and expressions.
 *
 * <p>The definition of a "simple" object is:</p>
 *
 * <ul>
 *     <li>Any primitive type. These will be encoded as atoms, with a one-byte type code at the beginning.</li>
 *     <li>A string.</li>
 *     <li>A byte array.</li>
 *     <li>A list of simpler objects. This is encoded as a list: a type code atom, then the sequence of simpler expressions.</li>
 *     <li>A map of strings to any simpler type. This is encoded as a list: a type code atom, then a sequence of key/value expressions.</li>
 * </ul>
 */
public class SimpleMapper {
    public static enum Type {
        Null((byte) 'z'),
        False((byte) 0),
        True((byte) 1),
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
        Bytes((byte) '['),
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
            return Atom.atom(Type.Null.code);
        Class clazz = o.getClass();
        if (clazz.equals(Boolean.class))
            return Atom.atom((boolean) o ? Type.True.code : Type.False.code);
        if (clazz.equals(Byte.class))
            return new Atom(new byte[] { Type.Byte.code, (byte) o });
        if (clazz.equals(Short.class))
            return atom(Type.Short.code, Primitives.bytes((Short) o));
        if (clazz.equals(Character.class))
            return atom(Type.Char.code, Primitives.bytes((Character) o));
        if (clazz.equals(Integer.class))
            return atom(Type.Int.code, Primitives.bytes((Integer) o));
        if (clazz.equals(Long.class))
            return atom(Type.Long.code, Primitives.bytes((Long) o));
        if (clazz.equals(Float.class))
            return atom(Type.Float.code, Primitives.bytes((Float) o));
        if (clazz.equals(Double.class))
            return atom(Type.Double.code, Primitives.bytes((Double) o));
        if (clazz.equals(String.class))
            return atom(Type.String.code, Primitives.bytes((String) o));
        if (clazz.equals(BigInteger.class))
            return atom(Type.BigInt.code, ((BigInteger) o).toByteArray());
        if (clazz.equals(BigDecimal.class))
            return atom(Type.BigDecimal.code, Primitives.bytes(((BigDecimal) o).toString()));
        if (clazz.isArray() && clazz.getComponentType().equals(Byte.TYPE))
            return atom(Type.Bytes.code, (byte[]) o);
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
            switch (((Atom) e).typeCode()) {
                case 0: return Boolean.FALSE;
                case 1: return Boolean.TRUE;
                case 'z': return null;
                case 'b': return ((Atom) e).byteValue(1);
                case 's': return ((Atom) e).shortValue(1);
                case 'c': return ((Atom) e).charValue(1);
                case 'i': return ((Atom) e).intValue(1);
                case 'l': return ((Atom) e).longValue(1);
                case 'f': return ((Atom) e).floatValue(1);
                case 'd': return ((Atom) e).doubleValue(1);
                case 'S': return ((Atom) e).stringValue(1);
                case '[': return ((Atom) e).bytes(1);
                case 'I': return ((Atom) e).bigIntegerValue(1);
                case 'D': return ((Atom) e).bigDecimalValue(1);
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
