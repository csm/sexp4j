package org.metastatic.sexp4j;

import com.google.common.base.Preconditions;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;

public class Primitives {
    public static byte[] bytes(String string, Charset charset) {
        return string.getBytes(charset);
    }

    public static byte[] bytes(String string) {
        return bytes(string, Charset.forName("UTF-8"));
    }

    public static byte[] bytes(short value) {
        return bytes(value, ByteOrder.BIG_ENDIAN);
    }

    public static byte[] bytes(short value, ByteOrder order) {
        byte[] result = new byte[Short.BYTES];
        ByteBuffer.wrap(result).order(order).putShort(value);
        return result;
    }

    public static byte[] bytes(int value, ByteOrder order) {
        byte[] result = new byte[Integer.BYTES];
        ByteBuffer.wrap(result).order(order).putInt(value);
        return result;
    }

    public static byte[] bytes(int value) {
        return bytes(value, ByteOrder.BIG_ENDIAN);
    }

    public static byte[] bytes(long value, ByteOrder order) {
        byte[] result = new byte[Long.BYTES];
        ByteBuffer.wrap(result).order(order).putLong(value);
        return result;
    }

    public static byte[] bytes(float value) {
        return bytes(value, ByteOrder.BIG_ENDIAN);
    }

    public static byte[] bytes(float value, ByteOrder order) {
        byte[] result = new byte[Float.BYTES];
        ByteBuffer.wrap(result).order(order).putFloat(value);
        return result;
    }

    public static byte[] bytes(double value) {
        return bytes(value, ByteOrder.BIG_ENDIAN);
    }

    public static byte[] bytes(double value, ByteOrder order) {
        byte[] result = new byte[Double.BYTES];
        ByteBuffer.wrap(result).order(order).putDouble(value);
        return result;
    }

    public static byte[] bytes(long value) {
        return bytes(value, ByteOrder.BIG_ENDIAN);
    }

    public static byte[] bytes(char value) {
        return bytes((short) value);
    }

    public static byte[] bytes(char value, ByteOrder order) {
        return bytes((short) value, order);
    }

    // short

    public static short toShort(byte[] bytes) {
        return toShort(bytes, 0);
    }

    public static short toShort(byte[] bytes, int offset) {
        return toShort(bytes, offset, ByteOrder.BIG_ENDIAN);
    }

    public static short toShort(byte[] bytes, ByteOrder order) {
        return toShort(bytes, 0, order);
    }

    public static short toShort(byte[] bytes, int offset, ByteOrder order) {
        return ByteBuffer.wrap(bytes).order(order).getShort(offset);
    }

    // int

    public static int toInt(byte[] bytes, ByteOrder order) {
        return toInt(bytes, 0, order);
    }

    public static int toInt(byte[] bytes, int offset, ByteOrder order) {
        return ByteBuffer.wrap(bytes).order(order).getInt(offset);
    }

    public static int toInt(byte[] bytes, int offset) {
        return toInt(bytes, offset, ByteOrder.BIG_ENDIAN);
    }

    public static int toInt(byte[] bytes) {
        return toInt(bytes, 0);
    }

    // long

    public static long toLong(byte[] bytes, int offset, ByteOrder order) {
        return ByteBuffer.wrap(bytes).order(order).getLong(offset);
    }

    public static long toLong(byte[] bytes, ByteOrder order) {
        return toLong(bytes, 0, order);
    }

    public static long toLong(byte[] bytes, int offset) {
        return toLong(bytes, offset, ByteOrder.BIG_ENDIAN);
    }

    public static long toLong(byte[] bytes) {
        return toLong(bytes, 0);
    }

    // float

    public static float toFloat(byte[] bytes) {
        return toFloat(bytes, ByteOrder.BIG_ENDIAN);
    }

    public static float toFloat(byte[] bytes, ByteOrder order) {
        return toFloat(bytes, 0, order);
    }

    public static float toFloat(byte[] bytes, int offset) {
        return toFloat(bytes, offset, ByteOrder.BIG_ENDIAN);
    }

    public static float toFloat(byte[] bytes, int offset, ByteOrder order) {
        return ByteBuffer.wrap(bytes).order(order).getFloat(offset);
    }

    // double

    public static double toDouble(byte[] bytes) {
        return toDouble(bytes, ByteOrder.BIG_ENDIAN);
    }

    public static double toDouble(byte[] bytes, ByteOrder order) {
        return toDouble(bytes, 0, order);
    }

    public static double toDouble(byte[] bytes, int offset) {
        return toDouble(bytes, offset, ByteOrder.BIG_ENDIAN);
    }

    public static double toDouble(byte[] bytes, int offset, ByteOrder order) {
        return ByteBuffer.wrap(bytes).order(order).getDouble(offset);
    }

    // char

    public static char toChar(byte[] bytes) {
        return (char) toShort(bytes);
    }

    public static char toChar(byte[] bytes, ByteOrder order) {
        return (char) toShort(bytes, order);
    }

    public static char toChar(byte[] bytes, int offset) {
        return (char) toShort(bytes, offset);
    }

    public static char toChar(byte[] bytes, int offset, ByteOrder order) {
        return (char) toShort(bytes, offset, order);
    }
}
