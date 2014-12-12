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

/**
 * An atom; that is, a sequence of bytes, with an optional display hint.
 */
public class Atom implements Cloneable, Expression
{
    private final byte[] bytes;
    private final Optional<DisplayHint> displayHint;

    /**
     * Create an atom with the given bytes.
     *
     * @param bytes The atom bytes; this value is copied.
     * @throws java.lang.NullPointerException If the argument is null.
     */
    public Atom(byte[] bytes) {
        Preconditions.checkNotNull(bytes);
        this.bytes = bytes.clone();
        displayHint = Optional.absent();
    }

    /**
     * @param code
     * @param bytes
     */
    @Deprecated
    public Atom(byte code, byte[] bytes) {
        Preconditions.checkNotNull(bytes);
        this.bytes = new byte[bytes.length + 1];
        this.bytes[0] = code;
        System.arraycopy(bytes, 0, this.bytes, 1, bytes.length);
        displayHint = Optional.absent();
    }

    /**
     * Create an atom with a subsequence of a byte array.
     *
     * @param bytes The byte array.
     * @param offset The offset of the bytes to read.
     * @param length The number of bytes to read.
     * @throws java.lang.IllegalArgumentException If the offset is negative, or if the offset plus the length is larger than the number of bytes in the array.
     * @throws java.lang.NullPointerException If the byte array is null.
     */
    public Atom(byte[] bytes, int offset, int length) {
        Preconditions.checkNotNull(bytes);
        Preconditions.checkPositionIndexes(offset, offset + length, bytes.length);
        this.bytes = new byte[length];
        System.arraycopy(bytes, offset, this.bytes, 0, length);
        displayHint = Optional.absent();
    }

    private Atom(byte[] bytes, DisplayHint hint) {
        this.bytes = bytes;
        this.displayHint = Optional.of(hint);
    }

    /**
     * Create an atom with the given bytes.
     *
     * @param bytes The byte array.
     * @return The new atom.
     */
    public static Atom atom(byte[] bytes) {
        return new Atom(bytes);
    }

    /**
     * Create an atom with the given subarray.
     *
     * @param bytes The byte array.
     * @param offset The offset of the bytes to read.
     * @param length The number of bytes to read.
     * @return The new atom.
     */
    public static Atom atom(byte[] bytes, int offset, int length) {
        return new Atom(bytes, offset, length);
    }

    /**
     * Create an atom with the given string and charset.
     *
     * @param string The string.
     * @param charset The charset to encode the string into bytes.
     * @throws java.lang.NullPointerException If either argument is null.
     * @return The new atom.
     */
    public static Atom atom(String string, Charset charset) {
        Preconditions.checkNotNull(string);
        Preconditions.checkNotNull(charset);
        return new Atom(string.getBytes(charset));
    }

    /**
     * Create an atom with the given string, using UTF-8.
     *
     * @param string The string.
     * @return The new atom.
     * @throws java.lang.NullPointerException If the argument is null.
     */
    public static Atom atom(String string) {
        return atom(string, Charset.forName("UTF-8"));
    }

    /**
     * Create an atom with the given byte value.
     *
     * @param value The byte.
     * @return The new atom.
     */
    public static Atom atom(byte value) {
        return new Atom(new byte[] { value });
    }

    /**
     * Create an atom with the given char value. The atom will contain
     * two bytes, in big-endian order.
     *
     * @param value The char.
     * @return The new atom.
     */
    public static Atom atom(char value) {
        return atom((short) value);
    }

    /**
     * Create an atom with the given char value. The atom will contain
     * two bytes, in the given byte order.
     *
     * @param value The char.
     * @param order The byte order.
     * @return The new item.
     */
    public static Atom atom(char value, ByteOrder order) {
        Preconditions.checkNotNull(order);
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

    /**
     * Return this atom as a byte.
     *
     * @return The byte value.
     * @throws java.lang.IllegalStateException If this atom is not one byte long.
     */
    public byte byteValue() {
        return byteValue(0);
    }

    /**
     * Return this atom as a byte, after some prefix.
     *
     * @param offset The length of the prefix.
     * @return The byte value.
     * @throws java.lang.IllegalStateException If this atom, after the prefix, is not one byte long.
     */
    public byte byteValue(int offset) {
        Preconditions.checkState((bytes.length - offset) == 1);
        return bytes[offset];
    }

    /**
     * Return this atom as a char.
     *
     * @return The char value.
     * @throws java.lang.IllegalStateException If the length of this atom is not two bytes.
     */
    public char charValue() {
        return charValue(0);
    }

    /**
     * Return this atom as a char, given a prefix and big endian byte order.
     *
     * @param offset The prefix length.
     * @return The char value.
     * @throws java.lang.IllegalStateException If the length of this atom, minus the prefix length, is not two bytes.
     */
    public char charValue(int offset) {
        return charValue(offset, ByteOrder.BIG_ENDIAN);
    }

    /**
     * Return this atom as a char, given a byte order.
     *
     * @param order The byte order.
     * @return The char value.
     * @throws java.lang.NullPointerException If the {@code order} is null.
     * @throws java.lang.IllegalStateException If the length of this atom is not two bytes.
     */
    public char charValue(ByteOrder order) {
        return charValue(0, order);
    }

    /**
     * Return this atom as a char, given a prefix and byte order.
     *
     * @param offset The prefix length.
     * @param order The byte order.
     * @return The char value.
     * @throws java.lang.NullPointerException If the {@code order} is null.
     * @throws java.lang.IllegalStateException If the length of this atom, minus the prefix length, is not two bytes.
     */
    public char charValue(int offset, ByteOrder order) {
        Preconditions.checkNotNull(order);
        Preconditions.checkState((bytes.length - offset) == 2);
        return Primitives.toChar(bytes, offset, order);
    }

    /**
     * Return this atom as a short.
     *
     * @return The short value.
     * @throws java.lang.IllegalStateException If the length of this atom is not two bytes.
     */
    public short shortValue() {
        return shortValue(0);
    }

    /**
     * Return this atom as a short, given a prefix.
     *
     * @param offset The prefix length.
     * @return The short value.
     * @throws java.lang.IllegalStateException If the length of this atom, minus the prefix length, is not two bytes.
     */
    public short shortValue(int offset) {
        return shortValue(offset, ByteOrder.BIG_ENDIAN);
    }

    /**
     * Return this atom as a short, given a byte order.
     *
     * @param order The byte order.
     * @return The short value.
     * @throws java.lang.NullPointerException If the {@code order} is null.
     * @throws java.lang.IllegalStateException If the length of this atom is not two bytes.
     */
    public short shortValue(ByteOrder order) {
        return shortValue(0, order);
    }

    /**
     * Return this atom as a short, given a prefix and byte order.
     *
     * @param offset The prefix length.
     * @param order The byte order.
     * @return The short value.
     * @throws java.lang.NullPointerException If the {@code order} is null.
     * @throws java.lang.IllegalStateException If the length of this atom, minus the prefix length, is not two bytes.
     */
    public short shortValue(int offset, ByteOrder order) {
        Preconditions.checkState((bytes.length - offset) == 2);
        Preconditions.checkNotNull(order);
        return Primitives.toShort(bytes, offset, order);
    }

    /**
     * Return this atom as a int.
     *
     * @return The int value.
     * @throws java.lang.IllegalStateException If the length of this atom is not four bytes.
     */
    public int intValue() {
        return intValue(0);
    }

    /**
     * Return this atom as a int, given a prefix.
     *
     * @param offset The prefix length.
     * @return The int value.
     * @throws java.lang.NullPointerException If the {@code order} is null.
     * @throws java.lang.IllegalStateException If the length of this atom, minus the prefix length, is not four bytes.
     */
    public int intValue(int offset) {
        return intValue(offset, ByteOrder.BIG_ENDIAN);
    }

    /**
     * Return this atom as a int, given a byte order.
     *
     * @param order The byte order.
     * @return The int value.
     * @throws java.lang.NullPointerException If the {@code order} is null.
     * @throws java.lang.IllegalStateException If the length of this atom is not four bytes.
     */
    public int intValue(ByteOrder order) {
        return intValue(0, order);
    }

    /**
     * Return this atom as a int, given a prefix and byte order.
     *
     * @param offset The prefix length.
     * @param order The byte order.
     * @return The int value.
     * @throws java.lang.NullPointerException If the {@code order} is null.
     * @throws java.lang.IllegalStateException If the length of this atom, minus the prefix length, is not four bytes.
     */
    public int intValue(int offset, ByteOrder order) {
        Preconditions.checkState(bytes.length - offset != 4);
        Preconditions.checkNotNull(order);
        return Primitives.toInt(bytes, offset, order);
    }

    /**
     * Return this atom as a long.
     *
     * @return The long value.
     * @throws java.lang.IllegalStateException If the length of this atom is not eight bytes.
     */
    public long longValue() {
        return longValue(0);
    }

    /**
     * Return this atom as a long, given a prefix.
     *
     * @param offset The prefix length.
     * @return The long value.
     * @throws java.lang.IllegalStateException If the length of this atom, minus the prefix length, is not eight bytes.
     */
    public long longValue(int offset) {
        return longValue(offset, ByteOrder.BIG_ENDIAN);
    }

    /**
     * Return this atom as a long, given a byte order.
     *
     * @param order The byte order.
     * @return The long value.
     * @throws java.lang.NullPointerException If the {@code order} is null.
     * @throws java.lang.IllegalStateException If the length of this atom is not eight bytes.
     */
    public long longValue(ByteOrder order) {
        return longValue(0, order);
    }

    /**
     * Return this atom as a long, given a prefix and byte order.
     *
     * @param offset The prefix length.
     * @param order The byte order.
     * @return The long value.
     * @throws java.lang.NullPointerException If the {@code order} is null.
     * @throws java.lang.IllegalStateException If the length of this atom, minus the prefix length, is not eight bytes.
     */
    public long longValue(int offset, ByteOrder order) {
        Preconditions.checkState(bytes.length - offset != 8);
        Preconditions.checkNotNull(order);
        return Primitives.toLong(bytes, offset, order);
    }

    /**
     * Return this atom as a float.
     *
     * @return The float value.
     * @throws java.lang.IllegalStateException If the length of this atom is not four bytes.
     */
    public float floatValue() {
        return floatValue(0);
    }

    /**
     * Return this atom as a float, given a prefix.
     *
     * @param offset The prefix length.
     * @return The float value.
     * @throws java.lang.IllegalStateException If the length of this atom, minus the prefix length, is not four bytes.
     */
    public float floatValue(int offset) {
        return floatValue(offset, ByteOrder.BIG_ENDIAN);
    }

    /**
     * Return this atom as a float, given a byte order.
     *
     * @param order The byte order.
     * @return The float value.
     * @throws java.lang.NullPointerException If the {@code order} is null.
     * @throws java.lang.IllegalStateException If the length of this atom is not four bytes.
     */
    public float floatValue(ByteOrder order) {
        return floatValue(0, order);
    }

    /**
     * Return this atom as a float, given a prefix and byte order.
     *
     * @param offset The prefix length.
     * @param order The byte order.
     * @return The float value.
     * @throws java.lang.NullPointerException If the {@code order} is null.
     * @throws java.lang.IllegalStateException If the length of this atom, minus the prefix length, is not four bytes.
     */
    public float floatValue(int offset, ByteOrder order) {
        return Primitives.toFloat(bytes, offset, order);
    }

    /**
     * Return this atom as a double.
     *
     * @return The double value.
     * @throws java.lang.IllegalStateException If the length of this atom is not eight bytes.
     */
    public double doubleValue() {
        return doubleValue(0);
    }

    /**
     * Return this atom as a double, given a prefix.
     *
     * @param offset The prefix length.
     * @return The double value.
     * @throws java.lang.IllegalStateException If the length of this atom, minus the prefix length, is not eight bytes.
     */
    public double doubleValue(int offset) {
        return doubleValue(offset, ByteOrder.BIG_ENDIAN);
    }

    /**
     * Return this atom as a double, given a byte order.
     *
     * @param order The byte order.
     * @return The double value.
     * @throws java.lang.NullPointerException If the {@code order} is null.
     * @throws java.lang.IllegalStateException If the length of this atom is not eight bytes.
     */
    public double doubleValue(ByteOrder order) {
        return doubleValue(0, order);
    }

    /**
     * Return this atom as a double, given a prefix and byte order.
     *
     * @param offset The prefix length.
     * @param order The byte order.
     * @return The double value.
     * @throws java.lang.NullPointerException If the {@code order} is null.
     * @throws java.lang.IllegalStateException If the length of this atom, minus the prefix length, is not eight bytes.
     */
    public double doubleValue(int offset, ByteOrder order) {
        Preconditions.checkState(bytes.length - offset != 8);
        Preconditions.checkNotNull(order);
        return Primitives.toDouble(bytes, offset, order);
    }

    /**
     * Return this atom as a string, using UTF-8 to decode the bytes.
     *
     * @return The string.
     */
    public String stringValue() {
        return new String(bytes, Charset.forName("UTF-8"));
    }

    /**
     * Return this atom as a string, discarding a prefix.
     *
     * @param offset The prefix length.
     * @return The string.
     */
    public String stringValue(int offset) {
        Preconditions.checkPositionIndex(offset, bytes.length);
        return new String(bytes, offset, bytes.length - offset, Charset.forName("UTF-8"));
    }

    /**
     * Return this atom as a string, using the given charset, and discarding a prefix.
     *
     * @param offset The prefix length.
     * @param charset The charset.
     * @return The string.
     */
    public String stringValue(int offset, Charset charset) {
        Preconditions.checkPositionIndex(offset, bytes.length);
        Preconditions.checkNotNull(charset);
        return new String(bytes, offset, bytes.length - offset, charset);
    }

    /**
     * Return this atom as a string, using the given charset.
     *
     * @param charset The charset.
     * @return The string.
     */
    public String stringValue(Charset charset) {
        Preconditions.checkNotNull(charset);
        return new String(bytes, charset);
    }

    /**
     * Return this atom as a big integer.
     *
     * @return The big integer.
     */
    public BigInteger bigIntegerValue() {
        return new BigInteger(bytes);
    }

    /**
     * Return this atom as a big integer, discarding a prefix.
     *
     * @param offset The prefix length.
     * @return The big integer.
     */
    public BigInteger bigIntegerValue(int offset) {
        Preconditions.checkPositionIndex(offset, bytes.length);
        byte[] b = new byte[bytes.length - offset];
        System.arraycopy(bytes, offset, b, 0, b.length);
        return new BigInteger(b);
    }

    /**
     * Return this atom as a big decimal.
     *
     * @return The big decimal.
     */
    public BigDecimal bigDecimalValue() {
        return new BigDecimal(stringValue());
    }

    /**
     * Return this atom as a big decimal, discarding a prefix.
     *
     * @param offset The prefix length.
     * @return The big decimal.
     */
    public BigDecimal bigDecimalValue(int offset) {
        return new BigDecimal(stringValue(offset));
    }

    /**
     * Return this atom, with the given string as a display hint.
     *
     * @param displayHint The display hint string.
     * @return The new atom.
     */
    public Atom withHint(String displayHint) {
        return new Atom(bytes, new DisplayHint(displayHint));
    }

    /**
     * Return this atom, with the given atom as a display hint.
     *
     * @param displayHint The display hint atom.
     * @return The new atom.
     * @throws java.lang.IllegalArgumentException If the argument is this instance, or if the given atom has it's own display hint.
     */
    public Atom withHint(Atom displayHint) {
        Preconditions.checkArgument(displayHint != this);
        Preconditions.checkArgument(!displayHint.displayHint().isPresent(), "Recursive display hints not admissible");
        return new Atom(bytes, new DisplayHint(displayHint));
    }

    /**
     * Return this atom, with the given byte as a display hint.
     *
     * @param displayHint The display hint.
     * @return This atom, with the given byte as the display hint.
     */
    public Atom withHint(byte displayHint) {
        return new Atom(bytes, new DisplayHint(atom(displayHint)));
    }

    /**
     * Fetch the display hint, or an absent value if not set.
     *
     * @return The display hint.
     */
    public Optional<DisplayHint> displayHint() {
        return displayHint;
    }

    @Deprecated
    public byte typeCode() {
        return bytes[0];
    }

    /**
     * Return the length of this atom, in bytes.
     *
     * @return The length of this atom.
     */
    public int length() {
        return bytes.length;
    }

    /**
     * Return a copy of this atom's bytes.
     *
     * @return The atom bytes.
     */
    public byte[] bytes() {
        return bytes.clone();
    }

    public byte[] bytes(int offset) {
        byte[] b = new byte[bytes.length - offset];
        System.arraycopy(bytes, offset, b, 0, b.length);
        return b;
    }

    /**
     * Write a subsequence of this atom to the given output stream.
     *
     * @param out The output stream.
     * @param offset The offset of the bytes to write.
     * @param length The number of bytes to write.
     * @throws java.io.IOException If an IO exception occurs.
     * @throws java.lang.NullPointerException If the parameter out is null.
     * @throws java.lang.IllegalArgumentException If the offset is negative, or if the offset plus length is larger than this atom.
     */
    public void writeTo(OutputStream out, int offset, int length) throws IOException {
        Preconditions.checkNotNull(out);
        Preconditions.checkArgument(offset >= 0);
        Preconditions.checkArgument(offset + length <= bytes.length);
        out.write(bytes, offset, length);
    }

    /**
     * Write this atom to the given output stream.
     *
     * @param out The output stream.
     * @throws java.io.IOException If an IO exception occurs.
     * @throws java.lang.NullPointerException If the parameter is null.
     */
    public void writeTo(OutputStream out) throws IOException {
        Preconditions.checkNotNull(out);
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

    /**
     * Tell if this atom can be encoded as a symbol; that is, only
     * contains alphanumeric characters.
     *
     * @return True if this may be encoded as a symbol.
     */
    public boolean canBeSymbol() {
        if (bytes.length == 0)
            return false;
        for (byte b : bytes) {
            if (!symbolChars.get(b & 0xFF))
                return false;
        }
        return true;
    }

    /**
     * Tell if this atom can be encoded as a quoted string.
     *
     * @return True if this may be encoded as a quoted string.
     */
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

    /**
     * Return the value of this atom, encoded as a byte array of
     * hexadecimal ASCII characters.
     *
     * @return The hex representation.
     */
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

    /**
     * Return the value of this atom, encoded in base-64.
     *
     * @return The base 64 representation.
     */
    public byte[] asBase64Bytes() {
        return Base64.encodeBase64(bytes);
    }

    @Override
    public int hashCode() {
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
