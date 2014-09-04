package org.metastatic.sexp4j;

import java.util.*;

public class ExpressionList extends AbstractList<Expression> implements Expression
{
    private final List<Expression> expressions;

    public ExpressionList()
    {
        this.expressions = new ArrayList<>();
    }

    public ExpressionList(Class<? extends List<Expression>> listClass)
            throws IllegalAccessException, InstantiationException
    {
        this.expressions = listClass.newInstance();
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
    public Expression set(int index, Expression element) {
        return expressions.set(index, element);
    }
}
