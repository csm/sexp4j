package org.metastatic.sexp4j;

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

    public static byte[] bytes(int value, ByteOrder order) {
        byte[] result = new byte[4];
        ByteBuffer.wrap(result).order(order).putInt(value);
        return result;
    }

    public static byte[] bytes(int value) {
        return bytes(value, ByteOrder.BIG_ENDIAN);
    }

    public static byte[] bytes(long value, ByteOrder order) {
        byte[] result = new byte[8];
        ByteBuffer.wrap(result).order(order).putLong(value);
        return result;
    }

    public static byte[] bytes(long value) {
        return bytes(value, ByteOrder.BIG_ENDIAN);
    }

    public static int toInt(byte[] bytes, ByteOrder order) {
        return ByteBuffer.wrap(bytes).order(order).getInt();
    }

    public static int toInt(byte[] bytes) {
        return toInt(bytes, ByteOrder.BIG_ENDIAN);
    }

    public static long toLong(byte[] bytes, ByteOrder order) {
        return ByteBuffer.wrap(bytes).order(order).getLong();
    }

    public static long toLong(byte[] bytes) {
        return toLong(bytes, ByteOrder.BIG_ENDIAN);
    }
}
