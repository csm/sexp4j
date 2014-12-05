package org.metastatic.sexp4j;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Base64;
import java.util.BitSet;

import com.google.common.base.Preconditions;
import com.google.common.collect.Range;

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
        return bytes.clone();
    }

    public void writeTo(OutputStream out, int offset, int length) throws IOException {
        out.write(bytes, offset, length);
    }

    public void writeTo(OutputStream out) throws IOException {
        out.write(bytes);
    }

    private static final BitSet symbolChars = new BitSet(256);
    private static final BitSet quotedStringChars = new BitSet(256);

    static {
        symbolChars.set('a', 'z' + 1);
        symbolChars.set('A', 'Z' + 1);
        symbolChars.set('0', '9' + 1);

        quotedStringChars.set(0x20);
        quotedStringChars.set('!');
        quotedStringChars.set('#', '~' + 1);
    }

    public boolean canBeSymbol() {
        if (bytes.length == 0)
            return false;
        for (byte b : bytes) {
            if (!symbolChars.get(b & 0xFF))
                return false;
        }
        return true;
    }

    public boolean canBeQuotedString() {
        for (byte b : bytes) {
            if (!quotedStringChars.get(b & 0xff))
                return false;
        }
        return true;
    }

    private byte hexByte(int i) {
        if (i >= 0 && i < 10) {
            return (byte) ('0' + i);
        }
        return (byte) ('a' + (i - 10));
    }

    public byte[] asHexBytes() {
        byte[] result = new byte[bytes.length * 2];
        int i = 0;
        for (byte b : bytes) {
            int x = b & 0xFF;
            result[i++] = hexByte((x >> 4) & 0xf);
            result[i++] = hexByte(x & 0xf);
        }
        return result;
    }

    public byte[] asBase64Bytes() {
        return Base64.getEncoder().encode(bytes);
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
