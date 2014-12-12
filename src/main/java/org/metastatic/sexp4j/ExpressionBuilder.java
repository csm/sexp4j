package org.metastatic.sexp4j;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.LinkedList;

/**
 * A builder interface for creating expressions.
 */
public class ExpressionBuilder {
    private final LinkedList<ExpressionList> stack = new LinkedList<>();
    private Optional<Expression> first = Optional.absent();

    /**
     * Create a new expression builder.
     *
     * @return The expression builder.
     */
    public static ExpressionBuilder create() {
        return new ExpressionBuilder();
    }

    /**
     * Build the expression.
     *
     * @return The built expression.
     * @throws java.lang.IllegalStateException If there is nothing to build.
     */
    public Expression build() {
        if (first.isPresent()) {
            stack.clear();
            return first.get();
        }
        throw new IllegalStateException("nothing to build");
    }

    /**
     * Append an atom to this builder.
     *
     * @param atom The atom to append.
     * @return This instance.
     * @throws java.lang.NullPointerException If the argument is null.
     * @throws java.lang.IllegalStateException If a single atom has already been added to this builder, or if this atom is being added outside of a list.
     */
    public ExpressionBuilder atom(Atom atom) {
        Preconditions.checkNotNull(atom);
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

    /**
     * Append an atom with the given bytes to this builder.
     *
     * @param atomBytes The atom bytes.
     * @return This instance.
     * @throws java.lang.NullPointerException If the argument is null.
     * @throws java.lang.IllegalStateException If a single atom has already been added to this builder, or if this atom is being added outside of a list.
     */
    public ExpressionBuilder atom(byte[] atomBytes) {
        return atom(new Atom(atomBytes));
    }

    /**
     * Append an atom with the given bytes to this builder.
     *
     * @param atomBytes The atom bytes.
     * @param offset The offset.
     * @param length The length.
     * @return This instance.
     * @throws java.lang.NullPointerException If the argument is null.
     * @throws java.lang.IllegalStateException If a single atom has already been added to this builder, or if this atom is being added outside of a list.
     */
    public ExpressionBuilder atom(byte[] atomBytes, int offset, int length) {
        return atom(new Atom(atomBytes, offset, length));
    }

    /**
     * Append an atom with the given string (in UTF-8) to this builder.
     *
     * @param atomString The string.
     * @return This instance.
     */
    public ExpressionBuilder atom(String atomString) {
        return atom(atomString.getBytes());
    }

    public ExpressionBuilder atom(String atomString, Charset charset) {
        return atom(atomString.getBytes(charset));
    }

    public ExpressionBuilder atom(String atomString, String atomCharset) throws UnsupportedEncodingException {
        return atom(atomString.getBytes(atomCharset));
    }

    /**
     * Append the beginning of a new list to this builder.
     *
     * @return This instance.
     */
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

    /**
     * Append the end of a list to this builder.
     *
     * @return This instance.
     */
    public ExpressionBuilder endList()
    {
        if (stack.isEmpty())
            throw new IllegalStateException("no list to end");
        stack.pop();
        return this;
    }
}
