package org.metastatic.sexp4j.mapper;

import com.google.common.base.Preconditions;
import org.metastatic.sexp4j.*;

import java.lang.reflect.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by cmarshall on 12/4/14.
 */
public class ObjectMapper {

    private interface ValueSetter {
        void setValue(Object o, String fieldName, Object value) throws IllegalAccessException, InvocationTargetException;
    }

    private interface ValueGetter {
        Object getValue(Object o, String fieldName) throws IllegalAccessException, InvocationTargetException;
    }

    private class ClassFieldIndex {
        final Class type;
        final String name;
        final ValueSetter setter;
        final ValueGetter getter;

        private ClassFieldIndex(Class type, String name, ValueSetter setter, ValueGetter getter) {
            this.type = type;
            this.name = name;
            this.setter = setter;
            this.getter = getter;
        }
    }

    private String fieldName(String accessorName, String prefix) {
        StringBuilder s = new StringBuilder(accessorName.substring(prefix.length()));
        if (Character.isUpperCase(s.charAt(0))) {
            s.setCharAt(0, Character.toLowerCase(s.charAt(0)));
        }
        return s.toString();
    }

    private boolean isPublicInstance(Method m) {
        return ((m.getModifiers() & Modifier.PUBLIC) == Modifier.PUBLIC)
                && ((m.getModifiers() & Modifier.STATIC) != Modifier.STATIC);
    }

    private boolean isGetter(Method m) {
        return isPublicInstance(m) && m.getParameterCount() == 0 && !Void.TYPE.equals(m.getReturnType());
    }

    private boolean isSetter(Method m) {
        return isPublicInstance(m) && m.getParameterCount() == 1 && Void.TYPE.equals(m.getReturnType());
    }

    private boolean isSetterFor(Method getter, Method m) {
        return isSetter(m) && getter.getReturnType().equals(m.getParameterTypes()[0]);
    }

    private Map<String, ClassFieldIndex> index(Class clazz) {
        Map<String, ClassFieldIndex> fieldIndex = new LinkedHashMap<>();
        for (Field field : clazz.getFields()) {
            fieldIndex.put(field.getName(), new ClassFieldIndex(field.getType(), field.getName(),
                    (o, n, v) -> field.set(o, v), (o, n) -> field.get(o)));
        }
        List<Method> getters = Arrays.asList(clazz.getMethods()).stream().filter(m -> isGetter(m) && !m.getName().equals("get") && m.getName().startsWith("get")).collect(Collectors.toList());
        List<Method> setters = Arrays.asList(clazz.getMethods()).stream().filter(m -> isSetter(m) && !m.getName().equals("set") && m.getName().startsWith("set")).collect(Collectors.toList());
        for (Method getter : getters) {
            String gettableName = fieldName(getter.getName(), "get");
            for (Method setter : setters) {
                String settableName = fieldName(setter.getName(), "set");
                if (gettableName.equals(settableName) && isSetterFor(getter, setter)) {
                    fieldIndex.put(gettableName, new ClassFieldIndex(getter.getReturnType(), gettableName,
                            (o, n, v) -> setter.invoke(o, v), (o, n) -> getter.invoke(o)));
                }
            }
        }
        return fieldIndex;
    }

    public Expression writeObject(Object o) throws InvocationTargetException, IllegalAccessException {
        if (o == null)
            return new Atom(new byte[0]);
        Class clazz = o.getClass();
        if (clazz.equals(Byte.class))
            return Atom.atom((Byte) o);
        if (clazz.equals(Short.class))
            return Atom.atom((Short) o);
        if (clazz.equals(Character.class))
            return Atom.atom((Character) o);
        if (clazz.equals(Integer.class))
            return Atom.atom((Integer) o);
        if (clazz.equals(Long.class))
            return Atom.atom(((Long) o).longValue());
        if (clazz.equals(Float.class))
            return Atom.atom(((Float) o).floatValue());
        if (clazz.equals(Double.class))
            return Atom.atom(((Double) o).doubleValue());
        if (clazz.equals(String.class))
            return Atom.atom((String) o);
        if (clazz.equals(BigInteger.class))
            return Atom.atom(((BigInteger) o).toByteArray());
        if (clazz.equals(BigDecimal.class))
            return Atom.atom(o.toString());
        if (clazz.isArray()) {
            if (clazz.getComponentType().equals(Byte.TYPE))
                return new Atom((byte[]) o);
            ExpressionList list = new ExpressionList(Array.getLength(o));
            for (int i = 0; i < Array.getLength(o); i++) {
                list.add(writeObject(Array.get(o, i)));
            }
            return list;
        }
        Map<String, ClassFieldIndex> index = index(clazz);
        ExpressionList list = new ExpressionList(index.size());
        for (Map.Entry<String, ClassFieldIndex> e : index.entrySet()) {
            list.add(ExpressionList.list(writeObject(e.getKey()), writeObject(e.getValue().getter.getValue(o, e.getKey()))));
        }
        return list;
    }

    public <T> T readObject(Expression expr, Class<T> clazz) throws InstantiationException, IllegalAccessException, MapperException, InvocationTargetException {
        if (expr instanceof ExpressionList)
            return readObject((ExpressionList) expr, clazz);
        return readObject((Atom) expr, clazz);
    }

    public <T> T readObject(ExpressionList list, Class<T> clazz) throws IllegalAccessException, InstantiationException, MapperException, InvocationTargetException {
        if (clazz.isArray()) {
            Class elemClass = clazz.getComponentType();
            T result = (T) Array.newInstance(elemClass, list.size());
            int i = 0;
            for (Expression e : list) {
                Array.set(result, i++, readObject(e, elemClass));
            }
            return result;
        }
        Map<String, ClassFieldIndex> index = index(clazz);
        T result = clazz.newInstance();
        for (Expression e : list) {
            if (!(e instanceof ExpressionList) || ((ExpressionList) e).size() != 2 || !(((ExpressionList) e).get(0) instanceof Atom))
                throw new IllegalArgumentException("expected a list of two elements, with an atom for the first");
            String key = ((Atom) ((ExpressionList) e).get(0)).stringValue();
            if (!index.containsKey(key))
                throw new MapperException("could not find field to set for key: " + key);
            index.get(key).setter.setValue(result, key, readObject(((ExpressionList) e).get(1), index.get(key).type));
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    public <T> T readObject(Atom atom, Class<T> clazz) {
        if (clazz.equals(Byte.class) || clazz.equals(Byte.TYPE)) {
            return (T) Byte.valueOf(atom.byteValue());
        }
        if (clazz.equals(Character.class) || clazz.equals(Character.TYPE)) {
            return (T) Character.valueOf(atom.charValue());
        }
        if (clazz.equals(Short.class) || clazz.equals(Short.TYPE)) {
            return (T) Short.valueOf(atom.shortValue());
        }
        if (clazz.equals(Integer.class) || clazz.equals(Integer.TYPE)) {
            return (T) Integer.valueOf(atom.intValue());
        }
        if (clazz.equals(Long.class) || clazz.equals(Long.TYPE)) {
            return (T) Long.valueOf(atom.longValue());
        }
        if (clazz.equals(Float.class) || clazz.equals(Float.TYPE)) {
            return (T) Float.valueOf(atom.floatValue());
        }
        if (clazz.equals(Double.class) || clazz.equals(Double.TYPE)) {
            return (T) Double.valueOf(atom.doubleValue());
        }
        if (clazz.equals(String.class)) {
            return (T) atom.stringValue();
        }
        if (clazz.equals(BigInteger.class)) {
            return (T) atom.bigIntegerValue();
        }
        if (clazz.equals(BigDecimal.class)) {
            return (T) atom.bigDecimalValue();
        }
        if (clazz.isArray() && clazz.getComponentType().equals(Byte.TYPE))
            return (T) atom.bytes();
        return null;
    }
}
