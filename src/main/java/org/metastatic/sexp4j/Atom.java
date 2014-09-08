package org.metastatic.sexp4j;

import java.util.Arrays;

import com.google.common.base.Preconditions;

public class Atom implements Cloneable, Expression
{
    private final byte[] bytes;

    public Atom(byte[] bytes)
    {
        Preconditions.checkNotNull(bytes);
        this.bytes = bytes.clone();
    }

    public Atom(byte[] bytes, int offset, int length)
    {
        Preconditions.checkNotNull(bytes);
        Preconditions.checkArgument(offset > 0);
        Preconditions.checkArgument(offset + length <= bytes.length);
        this.bytes = new byte[length];
        System.arraycopy(bytes, offset, this.bytes, 0, length);
    }

    public int length()
    {
        return bytes.length;
    }

    public byte[] bytes()
    {
        return bytes;
    }

    @Override
    public int hashCode()
    {
        return Arrays.hashCode(bytes);
    }

    @Override
    public boolean equals(Object obj)
    {
        return obj instanceof Atom && Arrays.equals(bytes, ((Atom) obj).bytes);
    }

    @Override
    public String toString() {
        return String.format("%s [bytes: %d bytes long]", super.toString(), bytes.length);
    }

    @Override
    public Atom clone() throws CloneNotSupportedException
    {
        return (Atom) super.clone();
    }
}
