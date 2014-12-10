package org.metastatic.sexp4j;

import com.google.common.base.Optional;
import org.apache.commons.codec.binary.Base64;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.BitSet;
import java.util.LinkedList;

/**
 * Created by cmarshall on 12/5/14.
 */
public class AdvancedStreamingParser extends StreamingParser {
    private static enum State {
        ConsumeWhitespace,
        StartingSymbolOrVerbatim,
        ConsumeSymbol,
        ConsumeQuotedString,
        ConsumeHex,
        ConsumeBase64
    }

    private int listDepth = 0;
    private State state = State.ConsumeWhitespace;
    private final LinkedList<Integer> tokens;
    private Optional<Integer> quotedStringLength = Optional.absent();
    private boolean consumingDisplayHint = false;
    private byte[] displayHint = null;

    public AdvancedStreamingParser(InputStream input) {
        super(input);
        tokens = new LinkedList<>();
    }

    @Override
    public void parse() throws IOException {
        boolean keepGoing = false;
        do {
            switch (state) {
                case ConsumeWhitespace:
                    keepGoing = consumeWhitespace();
                    break;

                case StartingSymbolOrVerbatim:
                    keepGoing = consumeStartingSymbolOrVerbatim();
                    break;

                case ConsumeBase64:
                    keepGoing = consumeBase64();
                    break;

                case ConsumeHex:
                    keepGoing = consumeHex();
                    break;

                case ConsumeQuotedString:
                    keepGoing = consumeQuotedString();
                    break;

                case ConsumeSymbol:
                    keepGoing = consumeSymbol();
                    break;
            }
        } while (keepGoing);
    }

    private boolean consumeWhitespace() throws IOException {
        int b = input.read();
        if (b == '(') {
            onListBegin();
            listDepth++;
        }
        else if (b == ')') {
            if (listDepth == 0)
                throw new ParseException("extra ')' in input");
            onListEnd();
            listDepth--;
        }
        else if (Character.isWhitespace(b))
            return true;
        else if (b == '[') {
            if (consumingDisplayHint)
                throw new ParseException("nested display-hints");
            consumingDisplayHint = true;
            return true;
        }
        else if (b == ']') {
            if (!consumingDisplayHint)
                throw new ParseException("unexpected token ']'");
            displayHint = new byte[0];
            consumingDisplayHint = false;
        }
        else if (b == '#')
            state = State.ConsumeHex;
        else if (b == '|')
            state = State.ConsumeBase64;
        else if (b == '"')
            state = State.ConsumeQuotedString;
        else if (Character.isDigit(b)) {
            tokens.addLast(b);
            state = State.StartingSymbolOrVerbatim;
        }
        else if (Character.isLetter(b)) {
            tokens.addLast(b);
            state = State.ConsumeSymbol;
        }
        else if (b < 0) {
            if (listDepth > 0)
                throw new ParseException("unbalanced parentheses");
            return false;
        }
        else
            throw new ParseException("unexpected token %c", (char) b);
        return true;
    }

    private boolean consumeStartingSymbolOrVerbatim() throws IOException {
        while (true) {
            int b = input.read();
            if (Character.isDigit(b)) {
                tokens.push(b);
            }
            else if (Character.isLetter(b)) {
                state = State.ConsumeSymbol;
                break;
            }
            else if (b == ':') {
                int length = makeLength();
                byte[] buffer = new byte[length];
                int read = input.read(buffer);
                if (read < length)
                    throw new EOFException();
                if (consumingDisplayHint) {
                    displayHint = buffer;
                    consumeWhitespaceUntil(']');
                    consumingDisplayHint = false;
                } else {
                    onAtom(buffer, Optional.fromNullable(displayHint));
                    displayHint = null;
                }
                state = State.ConsumeWhitespace;
                break;
            }
            else if (b == '"') {
                quotedStringLength = Optional.of(makeLength());
                state = State.ConsumeQuotedString;
                break;
            }
            else if (Character.isWhitespace(b)) {
                makeSymbol();
                state = State.ConsumeWhitespace;
                break;
            }
            else if (b < 0) {
                throw new ParseException("end-of-file reading symbol");
            }
            else
                throw new ParseException("unexpected token: %c", (char) b);
        }
        return true;
    }

    private void consumeWhitespaceUntil(int stopchar) throws IOException {
        while (true) {
            int ch = input.read();
            if (ch == stopchar)
                break;
            else if (Character.isWhitespace(ch))
                continue;
            else if (ch == -1)
                throw new EOFException();
            else
                throw new ParseException("unexpected token %02x", ch);
        }
    }

    private int makeLength() throws ParseException {
        int length = 0;
        while (!tokens.isEmpty()) {
            int i = tokens.removeFirst();
            int r = length * 10;
            int next = (i - '0');
            if (r < length || r + next < length)
                throw new ParseException("integer overflow: atom length is greater than 2^31-1");
            length = r + next;
        }
        return length;
    }

    private void makeSymbol() throws IOException {
        byte[] buffer = new byte[tokens.size()];
        int i = 0;
        while (!tokens.isEmpty()) {
            buffer[i++] = tokens.removeFirst().byteValue();
        }
        if (consumingDisplayHint) {
            displayHint = buffer;
            // note, don't need to consume whitespace until ']', because
            // we already got it.
            consumingDisplayHint = false;
        } else {
            onAtom(buffer, Optional.fromNullable(displayHint));
            displayHint = null;
        }
    }

    private boolean consumeSymbol() throws IOException {
        while (true) {
            int b = input.read();
            if (Character.isLetterOrDigit(b)) {
                tokens.addLast(b);
            }
            else if (Character.isWhitespace(b)) {
                makeSymbol();
                state = State.ConsumeWhitespace;
                break;
            }
            else if (b == '(') {
                makeSymbol();
                onListBegin();
                listDepth++;
                state = State.ConsumeWhitespace;
                break;
            }
            else if (b == ')') {
                if (listDepth == 0)
                    throw new ParseException("unbalanced parentheses");
                makeSymbol();
                onListEnd();
                listDepth--;
                state = State.ConsumeWhitespace;
                break;
            }
            else if (b == ']' && consumingDisplayHint) {
                makeSymbol();
                state = State.ConsumeWhitespace;
                break;
            }
            else if (b < 0) {
                if (listDepth > 0)
                    throw new EOFException();
                makeSymbol();
                state = State.ConsumeWhitespace;
                return false;
            }
            else {
                throw new ParseException("unexpected token: %c", (char) b);
            }
        }
        return true;
    }

    private boolean consumeBase64() throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(16);
        while (true) {
            int b = input.read();
            if (Character.isLetterOrDigit(b) || b == '+' || b == '/' || b == '=') {
                if (buffer.remaining() == 0)
                    buffer = grow(buffer);
                buffer.put((byte) b);
            }
            else if (Character.isWhitespace(b)) {
                continue;
            }
            else if (b == '|') {
                buffer.flip();
                byte[] bytes = new byte[buffer.remaining()];
                buffer.get(bytes);
                bytes = Base64.decodeBase64(bytes);
                if (consumingDisplayHint) {
                    displayHint = bytes;
                    consumeWhitespaceUntil(']');
                    consumingDisplayHint = false;
                } else {
                    onAtom(bytes, Optional.fromNullable(displayHint));
                    displayHint = null;
                }
                state = State.ConsumeWhitespace;
                break;
            }
            else if (b < 0) {
                throw new EOFException();
            }
            else {
                throw new ParseException("unexpected token: 0x%x", b);
            }
        }
        return true;
    }

    private int hexchar(int c) {
        if (c >= '0' && c <= '9')
            return c - '0';
        else if (c >= 'a' && c <= 'f')
            return (c - 'a') + 10;
        else if (c >= 'A' && c <= 'F')
            return (c - 'A') + 10;
        else
            throw new IllegalArgumentException("unexpected char: " + (char) c);
    }

    private boolean consumeHex() throws IOException {
        StringBuilder buffer = new StringBuilder();
        while (true) {
            int b = input.read();
            if (Character.isDigit(b) || (b >= 'a' && b <= 'f') || (b >= 'A' && b <= 'F')) {
                buffer.append((char) b);
            }
            else if (Character.isWhitespace(b)) {
                continue;
            }
            else if (b == '#') {
                if (buffer.length() % 2 == 1)
                    buffer.insert(0, '0');
                byte[] result = new byte[buffer.length() / 2];
                for (int i = 0, j = 0; i + 1 < buffer.length() && j < result.length; i += 2, j++) {
                    result[j] = (byte) ((hexchar(buffer.charAt(i)) << 4) | hexchar(buffer.charAt(i + 1)));
                }
                if (consumingDisplayHint) {
                    displayHint = result;
                    consumeWhitespaceUntil(']');
                    consumingDisplayHint = false;
                } else {
                    onAtom(result, Optional.fromNullable(displayHint));
                    displayHint = null;
                }
                state = State.ConsumeWhitespace;
                break;
            }
            else if (b < 0) {
                throw new EOFException();
            }
            else {
                throw new ParseException("unexpected token: 0x%x", b);
            }
        }
        return true;
    }

    private static final BitSet quotedStringChars = new BitSet('}');
    static {
        quotedStringChars.set('0', '9' + 1);
        quotedStringChars.set('a', 'z' + 1);
        quotedStringChars.set('A', 'Z' + 1);
        quotedStringChars.set(' ');
        quotedStringChars.set('\t');
        quotedStringChars.set('\n');
        quotedStringChars.set('\r');
        quotedStringChars.set('-');
        quotedStringChars.set('.');
        quotedStringChars.set('/');
        quotedStringChars.set('_');
        quotedStringChars.set(':');
        quotedStringChars.set('*');
        quotedStringChars.set('+');
        quotedStringChars.set('=');
        quotedStringChars.set('(');
        quotedStringChars.set(')');
        quotedStringChars.set('[');
        quotedStringChars.set(']');
        quotedStringChars.set('{');
        quotedStringChars.set('}');
        quotedStringChars.set('|');
        quotedStringChars.set('#');
        quotedStringChars.set('&');
    }

    private boolean consumeQuotedString() throws IOException {
        Optional<Integer> len = quotedStringLength;
        quotedStringLength = Optional.absent();
        ByteBuffer buffer = ByteBuffer.allocate(len.or(16));
        LinkedList<Integer> peek = new LinkedList<>();
        while (true) {
            int b;
            if (peek.isEmpty())
                b = input.read();
            else
                b = peek.removeFirst();
            if (quotedStringChars.get(b)) {
                buffer = doPut(buffer, (byte) b);
            }
            else if (b == '\\') {
                int e1 = input.read();
                switch (e1) {
                    case 'b':
                        buffer = doPut(buffer, (byte) '\b');
                        break;
                    case 't':
                        buffer = doPut(buffer, (byte) '\t');
                        break;
                    case 'v':
                        buffer = doPut(buffer, (byte) 0xb);
                        break;
                    case 'n':
                        buffer = doPut(buffer, (byte) '\n');
                        break;
                    case 'r':
                        buffer = doPut(buffer, (byte) '\r');
                        break;
                    case '"':
                        buffer = doPut(buffer, (byte) '"');
                        break;
                    case '\'':
                        buffer = doPut(buffer, (byte) '\'');
                        break;
                    case '\\':
                        buffer = doPut(buffer, (byte) '\\');
                        break;
                    case '0':
                    case '1':
                    case '2':
                    case '3':
                    case '4':
                    case '5':
                    case '6':
                    case '7': {
                        int e2 = input.read();
                        int e3 = input.read();
                        if (e2 < '0' || e2 > '7' || e3 < '0' || e3 > '7')
                            throw new ParseException("invalid octal value in quoted string");
                        int value = ((e1 - '0') << 6) | ((e2 - '0') << 3) | (e3 - '0');
                        buffer = doPut(buffer, (byte) value);
                        break;
                    }
                    case 'x': {
                        int e2 = input.read();
                        int e3 = input.read();
                        int value = (hexchar(e2) << 4) | (hexchar(e3));
                        buffer = doPut(buffer, (byte) value);
                        break;
                    }
                    case '\r': {
                        int e2 = input.read();
                        if (e2 != '\n')
                            peek.addLast(e2);
                        break; // ignore these characters
                    }
                    case '\n': {
                        int e2 = input.read();
                        if (e2 != '\r')
                            peek.addLast(e2);
                        break; // ignore these characters
                    }
                    default:
                        throw new ParseException("invalid escape character: %c (0x%x)", (char) e1, e1);
                }
            }
            else if (b == '"') {
                buffer.flip();
                if (len.isPresent() && len.get() != buffer.remaining())
                    throw new ParseException("quoted string length did not match explicit length");
                byte[] bytes = new byte[buffer.remaining()];
                buffer.get(bytes);
                if (consumingDisplayHint) {
                    displayHint = bytes;
                    consumeWhitespaceUntil(']');
                    consumingDisplayHint = false;
                } else {
                    onAtom(bytes, Optional.fromNullable(displayHint));
                    displayHint = null;
                }
                state = State.ConsumeWhitespace;
                break;
            }
            else if (b < 0) {
                throw new EOFException();
            }
            else {
                throw new ParseException("invalid character in quoted string: %c (0x%x)", (char) b, b);
            }
        }
        return true;
    }

    private ByteBuffer doPut(ByteBuffer buffer, byte value) {
        if (!buffer.hasRemaining())
            buffer = grow(buffer);
        buffer.put(value);
        return buffer;
    }

    private ByteBuffer grow(ByteBuffer buffer) {
        int newLength;
        buffer.flip();
        if (buffer.remaining() > 4096)
            newLength = buffer.remaining() + 1024;
        else
            newLength = buffer.remaining() * 2;
        ByteBuffer newBuffer = ByteBuffer.allocate(newLength);
        newBuffer.put(buffer);
        return newBuffer;
    }
}
