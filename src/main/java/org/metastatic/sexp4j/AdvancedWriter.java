package org.metastatic.sexp4j;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Optional;

import com.google.common.base.Preconditions;

/**
 * Created by cmarshall on 12/4/14.
 */
public class AdvancedWriter implements Writer {
    private final Optional<Integer> lineLength;
    private final Optional<Integer> indentAmount;
    private final OutputStream outputStream;

    private static enum LastWritten {
        None,
        BeginList,
        EndList,
        Atom,
        NewLine
    }

    private LastWritten lastWritten = LastWritten.None;
    private int currentLineLength = 0;
    private int indentLevel = 0;

    private AdvancedWriter(Optional<Integer> lineLength, Optional<Integer> indentAmount, OutputStream outputStream) {
        Preconditions.checkNotNull(lineLength);
        Preconditions.checkNotNull(indentAmount);
        Preconditions.checkNotNull(outputStream);
        this.lineLength = lineLength;
        this.indentAmount = indentAmount;
        this.outputStream = outputStream;
    }

    public static class Builder {
        private Optional<Integer> lineLength = Optional.empty();
        private Optional<Integer> indentAmount = Optional.empty();
        private Optional<OutputStream> outputStream = Optional.empty();

        private Builder() {
        }

        public Optional<Integer> getLineLength() {
            return lineLength;
        }

        public Optional<Integer> getIndentAmount() {
            return indentAmount;
        }

        public Optional<OutputStream> getOutputStream() {
            return outputStream;
        }

        public Builder lineLength(int lineLength) {
            Preconditions.checkArgument(lineLength > 0);
            this.lineLength = Optional.of(lineLength);
            return this;
        }

        public Builder indentAmount(int indentAmount) {
            Preconditions.checkArgument(indentAmount > 0);
            this.indentAmount = Optional.of(indentAmount);
            return this;
        }

        public Builder outputStream(OutputStream outputStream) {
            this.outputStream = Optional.of(outputStream);
            return this;
        }

        public AdvancedWriter build() {
            return new AdvancedWriter(lineLength, indentAmount, outputStream.get());
        }
    }

    public static Builder create() {
        return new Builder();
    }

    private int writeHint(Atom hint) throws IOException {
        int wrote = 0; //indentOrSpace();
        outputStream.write('[');
        wrote++;
        wrote = writeAtomBytes(hint, wrote);
        outputStream.write(']');
        wrote++;
        return wrote;
    }

    @Override
    public int writeAtom(Atom atom) throws IOException {
        int wrote = indentOrSpace();

        if (atom.displayHint().isPresent()) {
            wrote += writeHint(atom.displayHint().get().atom());
        }

        //wrote += indentOrSpace();

        wrote = writeAtomBytes(atom, wrote);
        currentLineLength += wrote;
        lastWritten = LastWritten.Atom;
        return wrote;
    }

    private int writeAtomBytes(Atom atom, int wrote) throws IOException {
        if (atom.canBeSymbol())
        {
            atom.writeTo(outputStream);
            wrote += atom.length();
        }
        else if (atom.canBeQuotedString()) {
            outputStream.write('"');
            atom.writeTo(outputStream);
            outputStream.write('"');
            wrote += atom.length() + 2;
        }
        else if (atom.length() <= 8) {
            outputStream.write('#');
            byte[] hex = atom.asHexBytes();
            outputStream.write(hex);
            outputStream.write('#');
            wrote += hex.length + 2;
        }
        else {
            byte[] b64 = atom.asBase64Bytes();
            outputStream.write('|');
            outputStream.write(b64);
            outputStream.write('|');
            wrote += b64.length + 2;
        }
        return wrote;
    }

    private int indentOrSpace() throws IOException {
        int wrote = 0;
        if ((lastWritten == LastWritten.Atom || lastWritten == LastWritten.EndList)) {
            if (lineLength.isPresent() && currentLineLength > lineLength.get()) {
                outputStream.write('\n');
                currentLineLength = -1;
                wrote++;
                for (int i = 0; i < indentLevel; i++) {
                    for (int j = 0; j < indentAmount.orElse(0); j++) {
                        outputStream.write(' ');
                        wrote++;
                    }
                }
            }
            else {
                outputStream.write(' ');
                wrote++;
            }
        }
        return wrote;
    }

    @Override
    public int writeList(ExpressionList list) throws IOException {
        int wrote = beginList0();
        for (Expression e : list) {
            wrote += writeExpression(e);
        }
        wrote += endList0();
        return wrote;
    }

    @Override
    public void beginList() throws IOException {
        currentLineLength += beginList0();
    }

    private int beginList0() throws IOException {
        int wrote = indentOrSpace();
        outputStream.write('(');
        lastWritten = LastWritten.BeginList;
        wrote += 1;
        indentLevel++;
        return wrote;
    }

    @Override
    public void endList() throws IOException {
        currentLineLength += endList0();
    }

    private int endList0() throws IOException {
        outputStream.write(')');
        lastWritten = LastWritten.EndList;
        indentLevel--;
        return 1;
    }

    @Override
    public int writeExpression(Expression expression) throws IOException {
        Preconditions.checkNotNull(expression);
        if (expression instanceof ExpressionList)
            return writeList((ExpressionList) expression);
        if (expression instanceof Atom)
            return writeAtom((Atom) expression);
        throw new IllegalArgumentException("don't know how to write a " + expression.getClass());
    }
}
