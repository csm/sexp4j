package org.metastatic.sexp4j;

import java.io.IOException;

/**
 * An s-expression writer.
 */
public interface Writer {
    /**
     * Write an atom.
     *
     * <p>It is invalid to write an atom if a single atom has already
     * been written, or if a list in its entirety has been written
     * previously.</p>
     *
     * @param atom The atom to write.
     * @return The number of bytes written.
     * @throws NullPointerException if the argument is null.
     * @throws WriteException If an illegal write state occurs.
     * @throws IOException If an IO exception occurs.
     */
    int writeAtom(Atom atom) throws IOException;

    /**
     * Write a list.
     *
     * <p>It is invalid to write a list if a single atom was written
     * as the only value, or if a complete list was written previously.
     * </p>
     *
     * @param list The list to write.
     * @return The number of bytes written.
     * @throws NullPointerException if the argument is null.
     * @throws WriteException If an illegal write state occurs.
     * @throws IOException If an IO exception occurs.
     */
    int writeList(ExpressionList list) throws IOException;

    /**
     * Begin a list. You don't need to call this along with {@link #writeList(ExpressionList)},
     * this method exists to support writing expressions without building them
     * as {@link org.metastatic.sexp4j.ExpressionList} instances first.
     *
     * @throws WriteException If it is illegal to begin a list in the current state.
     * @throws IOException If an IO exception occurs.
     */
    void beginList() throws IOException;

    /**
     * End a list. You should call this if you use the {@link #beginList()} method
     * to start a list.
     *
     * @throws WriteException if it is illegal to end a list in the current state.
     * @throws IOException If an IO exception occurs.
     */
    void endList() throws IOException;

    /**
     * Write an expression.
     *
     * @param expression The expression to write.
     * @return The number of bytes written.
     * @throws NullPointerException if the argument is null.
     * @throws IllegalArgumentException if the argument is not a {@link Atom}
     *         or a {@link ExpressionList}.
     * @throws WriteException If an illegal write state occurs.
     * @throws IOException If an IO exception occurs.
     * @see #writeAtom(Atom)
     * @see #writeList(ExpressionList)
     */
    int writeExpression(Expression expression) throws IOException;
}
