package org.metastatic.sexp4j;

import java.util.*;

public class ExpressionList extends AbstractList<Expression> implements Expression
{
    private final List<Expression> expressions;

    public ExpressionList()
    {
        this.expressions = new ArrayList<>();
    }

    public ExpressionList(int capacity) {
        this.expressions = new ArrayList<>(capacity);
    }

    public ExpressionList(Class<? extends List<Expression>> listClass)
            throws IllegalAccessException, InstantiationException
    {
        this.expressions = listClass.newInstance();
    }

    public static ExpressionList list(Expression... expressions) {
        ExpressionList list = new ExpressionList();
        Collections.addAll(list, expressions);
        return list;
    }

    @Override
    public Expression get(int index) {
        return expressions.get(index);
    }

    @Override
    public int size() {
        return expressions.size();
    }

    @Override
    public boolean add(Expression expression) {
        return expressions.add(expression);
    }

    @Override
    public void add(int index, Expression element) {
        expressions.add(index, element);
    }

    @Override
    public boolean addAll(int index, Collection<? extends Expression> c) {
        return expressions.addAll(index, c);
    }

    @Override
    public Iterator<Expression> iterator() {
        return expressions.iterator();
    }

    @Override
    public ListIterator<Expression> listIterator() {
        return expressions.listIterator();
    }

    @Override
    public ListIterator<Expression> listIterator(int index) {
        return expressions.listIterator(index);
    }

    @Override
    public List<Expression> subList(int fromIndex, int toIndex) {
        return expressions.subList(fromIndex, toIndex);
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof ExpressionList) && expressions.equals(((ExpressionList) o).expressions);
    }

    @Override
    public int hashCode() {
        return expressions.hashCode();
    }

    @Override
    public boolean contains(Object o) {
        return expressions.contains(o);
    }

    @Override
    public Object[] toArray() {
        return expressions.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return expressions.toArray(a);
    }

    @Override
    public boolean remove(Object o) {
        return expressions.remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return expressions.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends Expression> c) {
        return expressions.addAll(c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return expressions.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return expressions.retainAll(c);
    }

    @Override
    public void clear() {
        expressions.clear();
    }

    @Override
    public int indexOf(Object o) {
        return expressions.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return expressions.lastIndexOf(o);
    }

    @Override
    public Expression remove(int index) {
        return expressions.remove(index);
    }

    @Override
    public Expression set(int index, Expression element) {
        return expressions.set(index, element);
    }
}
