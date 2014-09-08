package org.metastatic.sexp4j;

import java.nio.ByteOrder;
import java.nio.charset.Charset;
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

    public static Atom atom(byte[] bytes) {
        return new Atom(bytes);
    }

    public static Atom atom(byte[] bytes, int offset, int length) {
        return new Atom(bytes, offset, length);
    }

    public static Atom atom(String string, Charset charset) {
        return new Atom(string.getBytes(charset));
    }

    public static Atom atom(String string) {
        return atom(string, Charset.forName("UTF-8"));
    }

    public static Atom atom(int value, ByteOrder order) {
        return new Atom(Primitives.bytes(value, order));
    }

    public static Atom atom(int value) {
        return atom(value, ByteOrder.BIG_ENDIAN);
    }

    public static Atom atom(long value, ByteOrder order) {
        return new Atom(Primitives.bytes(value, order));
    }

    public static Atom atom(long value) {
        return atom(value, ByteOrder.BIG_ENDIAN);
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
