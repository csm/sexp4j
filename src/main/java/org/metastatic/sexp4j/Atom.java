package org.metastatic.sexp4j;

import java.util.Arrays;

import com.google.common.base.Preconditions;

public class Atom extends Expression implements Cloneable
{
    private final byte[] bytes;

    public Atom(byte[] bytes)
    {
        Preconditions.checkNotNull(bytes);
        this.bytes = bytes.clone();
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
    protected Atom clone() throws CloneNotSupportedException
    {
        return (Atom) super.clone();
    }
}
