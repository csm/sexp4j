package org.metastatic.sexp4j;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.BitSet;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import org.apache.commons.codec.binary.Base64;

public class Atom implements Cloneable, Expression
{
    private final byte[] bytes;
    private final Optional<DisplayHint> displayHint;

    public Atom(byte[] bytes) {
        Preconditions.checkNotNull(bytes);
        this.bytes = bytes.clone();
        displayHint = Optional.absent();
    }

    public Atom(byte code, byte[] bytes) {
        Preconditions.checkNotNull(bytes);
        this.bytes = new byte[bytes.length + 1];
        this.bytes[0] = code;
        System.arraycopy(bytes, 0, this.bytes, 1, bytes.length);
        displayHint = Optional.absent();
    }

    public Atom(byte[] bytes, int offset, int length) {
        Preconditions.checkNotNull(bytes);
        Preconditions.checkArgument(offset > 0);
        Preconditions.checkArgument(offset + length <= bytes.length);
        this.bytes = new byte[length];
        System.arraycopy(bytes, offset, this.bytes, 0, length);
        displayHint = Optional.absent();
    }

    private Atom(byte[] bytes, DisplayHint hint) {
        this.bytes = bytes;
        this.displayHint = Optional.of(hint);
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

    public static Atom atom(byte value) {
        return new Atom(new byte[] { value });
    }

    public static Atom atom(char value) {
        return atom((short) value);
    }

    public static Atom atom(char value, ByteOrder order) {
        return atom((short) value, order);
    }

    public static Atom atom(short value, ByteOrder order) {
        return new Atom(Primitives.bytes(value, order));
    }

    public static Atom atom(short value) {
        return new Atom(Primitives.bytes(value));
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

    public static Atom atom(float value) {
        return new Atom(Primitives.bytes(value));
    }

    public static Atom atom(float value, ByteOrder order) {
        return new Atom(Primitives.bytes(value, order));
    }

    public static Atom atom(double value) {
        return new Atom(Primitives.bytes(value));
    }

    public static Atom atom(double value, ByteOrder order) {
        return new Atom(Primitives.bytes(value, order));
    }

    public byte byteValue() {
        return byteValue(0);
    }

    public byte byteValue(int offset) {
        Preconditions.checkState((bytes.length - offset) == 1);
        return bytes[offset];
    }

    public char charValue() {
        return charValue(0);
    }

    public char charValue(int offset) {
        Preconditions.checkState((bytes.length - offset) == 2);
        return Primitives.toChar(bytes, offset);
    }

    public short shortValue() {
        return shortValue(0);
    }

    public short shortValue(int offset) {
        Preconditions.checkState((bytes.length - offset) == 2);
        return Primitives.toShort(bytes, offset);
    }

    public int intValue() {
        return intValue(0);
    }

    public int intValue(int offset) {
        Preconditions.checkState(bytes.length - offset == 4);
        return Primitives.toInt(bytes, offset);
    }

    public long longValue() {
        return longValue(0);
    }

    public long longValue(int offset) {
        Preconditions.checkState(bytes.length - offset == 8);
        return Primitives.toLong(bytes, offset);
    }

    public float floatValue() {
        return floatValue(0);
    }

    public float floatValue(int offset) {
        Preconditions.checkState(bytes.length - offset == 4);
        return Primitives.toFloat(bytes, offset);
    }

    public double doubleValue() {
        return doubleValue(0);
    }

    public double doubleValue(int offset) {
        Preconditions.checkState(bytes.length - offset == 8);
        return Primitives.toDouble(bytes, offset);
    }

    public String stringValue() {
        return new String(bytes, Charset.forName("UTF-8"));
    }

    public String stringValue(int offset) {
        return new String(bytes, offset, bytes.length - offset, Charset.forName("UTF-8"));
    }

    public BigInteger bigIntegerValue() {
        return new BigInteger(bytes);
    }

    public BigInteger bigIntegerValue(int offset) {
        byte[] b = new byte[bytes.length - offset];
        System.arraycopy(bytes, offset, b, 0, b.length);
        return new BigInteger(b);
    }

    public BigDecimal bigDecimalValue() {
        return new BigDecimal(stringValue());
    }

    public BigDecimal bigDecimalValue(int offset) {
        return new BigDecimal(stringValue(offset));
    }

    public Atom withHint(String displayHint) {
        return new Atom(bytes, new DisplayHint(displayHint));
    }

    public Atom withHint(Atom displayHint) {
        Preconditions.checkArgument(!displayHint.displayHint().isPresent(), "Recursive display hints not admissible");
        return new Atom(bytes, new DisplayHint(displayHint));
    }

    public Atom withHint(byte displayHint) {
        return new Atom(bytes, new DisplayHint(atom(displayHint)));
    }

    public Optional<DisplayHint> displayHint() {
        return displayHint;
    }

    public byte typeCode() {
        return bytes[0];
    }

    public int length()
    {
        return bytes.length;
    }

    public byte[] bytes()
    {
        return bytes.clone();
    }

    public byte[] bytes(int offset) {
        byte[] b = new byte[bytes.length - offset];
        System.arraycopy(bytes, offset, b, 0, b.length);
        return b;
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
        return Base64.encodeBase64(bytes);
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
