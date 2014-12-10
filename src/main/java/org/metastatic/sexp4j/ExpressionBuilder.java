package org.metastatic.sexp4j;

import com.google.common.base.Optional;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.LinkedList;

public class ExpressionBuilder {
    private final LinkedList<ExpressionList> stack = new LinkedList<>();
    private Optional<Expression> first = Optional.absent();

    public static ExpressionBuilder create() {
        return new ExpressionBuilder();
    }

    public Expression build() {
        if (first.isPresent()) {
            stack.clear();
            return first.get();
        }
        throw new IllegalStateException("nothing to build");
    }

    public ExpressionBuilder atom(Atom atom) {
        if (first.isPresent() && first.get() instanceof Atom)
            throw new IllegalStateException("first expression added was an atom");
        if (stack.isEmpty())
        {
            if (first.isPresent())
                throw new IllegalStateException("attempt to add atom outside of list");
            first = Optional.<Expression> of(atom);
        }
        else
            stack.peek().add(atom);
        return this;
    }

    public ExpressionBuilder atom(byte[] atomBytes) {
        return atom(new Atom(atomBytes));
    }

    public ExpressionBuilder atom(byte[] atomBytes, int offset, int length) {
        return atom(new Atom(atomBytes, offset, length));
    }

    public ExpressionBuilder atom(String atomString) {
        return atom(atomString.getBytes());
    }

    public ExpressionBuilder atom(String atomString, Charset charset) {
        return atom(atomString.getBytes(charset));
    }

    public ExpressionBuilder atom(String atomString, String atomCharset) throws UnsupportedEncodingException {
        return atom(atomString.getBytes(atomCharset));
    }

    public ExpressionBuilder beginList() {
        if (first.isPresent() && first.get() instanceof Atom)
            throw new IllegalStateException("first expression added was an atom");
        ExpressionList list = new ExpressionList();
        if (stack.isEmpty())
        {
            if (first.isPresent())
                throw new IllegalStateException("attempt to begin list outside of list");
            first = Optional.<Expression> of(list);
        }
        else
            stack.peek().add(list);
        stack.push(list);
        return this;
    }

    public ExpressionBuilder endList()
    {
        if (stack.isEmpty())
            throw new IllegalStateException("no list to end");
        stack.pop();
        return this;
    }
}
