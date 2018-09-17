package libcore.net;

import android.icu.text.PluralRules;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;

public abstract class UriCodec {
    private static final char INVALID_INPUT_CHARACTER = '�';

    protected abstract boolean isRetained(char c);

    private static boolean isWhitelisted(char c) {
        if ('a' <= c && c <= 'z') {
            return true;
        }
        if ('A' > c || c > 'Z') {
            return '0' <= c && c <= '9';
        } else {
            return true;
        }
    }

    private boolean isWhitelistedOrRetained(char c) {
        return !isWhitelisted(c) ? isRetained(c) : true;
    }

    public final String validate(String uri, int start, int end, String name) throws URISyntaxException {
        int i = start;
        while (i < end) {
            int i2 = i + 1;
            char c = uri.charAt(i);
            if (!isWhitelistedOrRetained(c)) {
                if (c == '%') {
                    int j = 0;
                    while (true) {
                        i = i2;
                        if (j >= 2) {
                            continue;
                            break;
                        }
                        i2 = i + 1;
                        c = getNextCharacter(uri, i, end, name);
                        if (hexCharToValue(c) < 0) {
                            throw unexpectedCharacterException(uri, name, c, i2 - 1);
                        }
                        j++;
                    }
                } else {
                    throw unexpectedCharacterException(uri, name, c, i2 - 1);
                }
            }
            i = i2;
        }
        return uri.substring(start, end);
    }

    private static int hexCharToValue(char c) {
        if ('0' <= c && c <= '9') {
            return c - 48;
        }
        if ('a' <= c && c <= 'f') {
            return (c + 10) - 97;
        }
        if ('A' > c || c > 'F') {
            return -1;
        }
        return (c + 10) - 65;
    }

    private static URISyntaxException unexpectedCharacterException(String uri, String name, char unexpected, int index) {
        return new URISyntaxException(uri, "Unexpected character" + (name == null ? "" : " in [" + name + "]") + PluralRules.KEYWORD_RULE_SEPARATOR + unexpected, index);
    }

    private static char getNextCharacter(String uri, int index, int end, String name) throws URISyntaxException {
        if (index < end) {
            return uri.charAt(index);
        }
        throw new URISyntaxException(uri, "Unexpected end of string" + (name == null ? "" : " in [" + name + "]"), index);
    }

    public static void validateSimple(String uri, String legal) throws URISyntaxException {
        int i = 0;
        while (i < uri.length()) {
            char c = uri.charAt(i);
            if (isWhitelisted(c) || legal.indexOf(c) >= 0) {
                i++;
            } else {
                throw unexpectedCharacterException(uri, null, c, i);
            }
        }
    }

    public final String encode(String s, Charset charset) {
        StringBuilder builder = new StringBuilder(s.length());
        appendEncoded(builder, s, charset, false);
        return builder.toString();
    }

    public final void appendEncoded(StringBuilder builder, String s) {
        appendEncoded(builder, s, StandardCharsets.UTF_8, false);
    }

    public final void appendPartiallyEncoded(StringBuilder builder, String s) {
        appendEncoded(builder, s, StandardCharsets.UTF_8, true);
    }

    private void appendEncoded(StringBuilder builder, String s, Charset charset, boolean partiallyEncoded) {
        CharsetEncoder encoder = charset.newEncoder().onMalformedInput(CodingErrorAction.REPORT).onUnmappableCharacter(CodingErrorAction.REPORT);
        CharBuffer cBuffer = CharBuffer.allocate(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '%' && partiallyEncoded) {
                flushEncodingCharBuffer(builder, encoder, cBuffer);
                builder.append('%');
            } else if (c == ' ' && isRetained(' ')) {
                flushEncodingCharBuffer(builder, encoder, cBuffer);
                builder.append('+');
            } else if (isWhitelistedOrRetained(c)) {
                flushEncodingCharBuffer(builder, encoder, cBuffer);
                builder.append(c);
            } else {
                cBuffer.put(c);
            }
        }
        flushEncodingCharBuffer(builder, encoder, cBuffer);
    }

    private static void flushEncodingCharBuffer(StringBuilder builder, CharsetEncoder encoder, CharBuffer cBuffer) {
        if (cBuffer.position() != 0) {
            cBuffer.flip();
            ByteBuffer byteBuffer = ByteBuffer.allocate(cBuffer.remaining() * ((int) Math.ceil((double) encoder.maxBytesPerChar())));
            byteBuffer.position(0);
            CoderResult result = encoder.encode(cBuffer, byteBuffer, true);
            if (result != CoderResult.UNDERFLOW) {
                throw new IllegalArgumentException("Error encoding, unexpected result [" + result.toString() + "] using encoder for [" + encoder.charset().name() + "]");
            } else if (cBuffer.hasRemaining()) {
                throw new IllegalArgumentException("Encoder for [" + encoder.charset().name() + "] failed with underflow with " + "remaining input [" + cBuffer + "]");
            } else {
                encoder.flush(byteBuffer);
                if (result != CoderResult.UNDERFLOW) {
                    throw new IllegalArgumentException("Error encoding, unexpected result [" + result.toString() + "] flushing encoder for [" + encoder.charset().name() + "]");
                }
                encoder.reset();
                byteBuffer.flip();
                while (byteBuffer.hasRemaining()) {
                    byte b = byteBuffer.get();
                    builder.append('%');
                    builder.append(intToHexDigit((b & 240) >>> 4));
                    builder.append(intToHexDigit(b & 15));
                }
                cBuffer.flip();
                cBuffer.limit(cBuffer.capacity());
            }
        }
    }

    private static char intToHexDigit(int b) {
        if (b < 10) {
            return (char) (b + 48);
        }
        return (char) ((b + 65) - 10);
    }

    public static String decode(String s, boolean convertPlus, Charset charset, boolean throwOnFailure) {
        StringBuilder builder = new StringBuilder(s.length());
        appendDecoded(builder, s, convertPlus, charset, throwOnFailure);
        return builder.toString();
    }

    private static void appendDecoded(StringBuilder builder, String s, boolean convertPlus, Charset charset, boolean throwOnFailure) {
        CharsetDecoder decoder = charset.newDecoder().onMalformedInput(CodingErrorAction.REPLACE).replaceWith("�").onUnmappableCharacter(CodingErrorAction.REPORT);
        ByteBuffer byteBuffer = ByteBuffer.allocate(s.length());
        int i = 0;
        while (i < s.length()) {
            char c = s.charAt(i);
            i++;
            switch (c) {
                case '%':
                    byte hexValue = (byte) 0;
                    int j = 0;
                    while (j < 2) {
                        try {
                            c = getNextCharacter(s, i, s.length(), null);
                            i++;
                            int newDigit = hexCharToValue(c);
                            if (newDigit < 0) {
                                if (!throwOnFailure) {
                                    flushDecodingByteAccumulator(builder, decoder, byteBuffer, throwOnFailure);
                                    builder.append(INVALID_INPUT_CHARACTER);
                                    byteBuffer.put(hexValue);
                                    break;
                                }
                                throw new IllegalArgumentException(unexpectedCharacterException(s, null, c, i - 1));
                            }
                            hexValue = (byte) ((hexValue * 16) + newDigit);
                            j++;
                        } catch (URISyntaxException e) {
                            if (throwOnFailure) {
                                throw new IllegalArgumentException(e);
                            }
                            flushDecodingByteAccumulator(builder, decoder, byteBuffer, throwOnFailure);
                            builder.append(INVALID_INPUT_CHARACTER);
                            return;
                        }
                    }
                    byteBuffer.put(hexValue);
                case '+':
                    flushDecodingByteAccumulator(builder, decoder, byteBuffer, throwOnFailure);
                    builder.append(convertPlus ? ' ' : '+');
                    break;
                default:
                    flushDecodingByteAccumulator(builder, decoder, byteBuffer, throwOnFailure);
                    builder.append(c);
                    break;
            }
        }
        flushDecodingByteAccumulator(builder, decoder, byteBuffer, throwOnFailure);
    }

    private static void flushDecodingByteAccumulator(StringBuilder builder, CharsetDecoder decoder, ByteBuffer byteBuffer, boolean throwOnFailure) {
        if (byteBuffer.position() != 0) {
            byteBuffer.flip();
            try {
                builder.append(decoder.decode(byteBuffer));
            } catch (CharacterCodingException e) {
                if (throwOnFailure) {
                    throw new IllegalArgumentException(e);
                }
                builder.append(INVALID_INPUT_CHARACTER);
            } finally {
                byteBuffer.flip();
                byteBuffer.limit(byteBuffer.capacity());
            }
        }
    }

    public static String decode(String s) {
        return decode(s, false, StandardCharsets.UTF_8, true);
    }
}
