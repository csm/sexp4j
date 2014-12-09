package org.metastatic.sexp4j;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Locale;

import com.google.common.base.Preconditions;

public class CanonicalWriter implements Writer {
    private final OutputStream out;
    private int listDepth = 0;

    private static enum WriteType
    {
        None, List, Atom
    }
    private WriteType firstWrite = WriteType.None;

    public CanonicalWriter(OutputStream out)
    {
        this.out = out;
    }

    @Override
    public int writeAtom(Atom atom) throws IOException {
        if (firstWrite == WriteType.Atom)
            throw new WriteException("already wrote an atom as the first value");
        int length = 0;
        if (atom.displayHint().isPresent()) {
            Atom hintAtom = atom.displayHint().get().atom();
            out.write('[');
            byte[] hintLengthTag = String.format(Locale.ENGLISH, "%d:", hintAtom.length()).getBytes("UTF-8");
            out.write(hintLengthTag);
            hintAtom.writeTo(out);
            out.write(']');
            length += 2 + hintLengthTag.length + hintAtom.length();
        }
        byte[] lengthTag = String.format(Locale.ENGLISH, "%d:", atom.length()).getBytes("UTF-8");
        out.write(lengthTag);
        atom.writeTo(out);
        if (firstWrite == WriteType.None)
            firstWrite = WriteType.Atom;
        length += lengthTag.length + atom.length();
        return length;
    }

    @Override
    public int writeList(ExpressionList list) throws IOException {
        if (firstWrite == WriteType.Atom)
            throw new WriteException("already wrote an atom as the first value");
        beginList();
        int len = 0;
        for (Expression expr : list)
            len += writeExpression(expr);
        endList();
        return 2 + len;
    }

    @Override
    public void beginList() throws IOException {
        if (firstWrite == WriteType.Atom)
            throw new WriteException("already wrote an atom as the first value");
        out.write('(');
        if (firstWrite == WriteType.None)
            firstWrite = WriteType.List;
        listDepth++;
    }

    @Override
    public void endList() throws IOException {
        if (listDepth <= 0)
            throw new WriteException("can't end list, list not started");
        out.write(')');
        listDepth--;
    }

    @Override
    public int writeExpression(Expression expression) throws IOException {
        Preconditions.checkNotNull(expression);
        if (expression instanceof Atom)
            return writeAtom(((Atom) expression));
        else if (expression instanceof ExpressionList)
            return writeList(((ExpressionList) expression));
        else
            throw new IllegalArgumentException("unknown expression of type " + expression.getClass().getName());
    }
}
