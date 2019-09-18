package libcore.net;

import android.icu.impl.locale.XLocaleDistance;
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

    /* access modifiers changed from: protected */
    public abstract boolean isRetained(char c);

    private static boolean isWhitelisted(char c) {
        return ('a' <= c && c <= 'z') || ('A' <= c && c <= 'Z') || ('0' <= c && c <= '9');
    }

    private boolean isWhitelistedOrRetained(char c) {
        return isWhitelisted(c) || isRetained(c);
    }

    public final String validate(String uri, int start, int end, String name) throws URISyntaxException {
        int i;
        for (int i2 = start; i2 < end; i2 = i) {
            i = i2 + 1;
            char c = uri.charAt(i2);
            if (!isWhitelistedOrRetained(c)) {
                if (c == '%') {
                    int j = 0;
                    while (j < 2) {
                        int i3 = i + 1;
                        char c2 = getNextCharacter(uri, i, end, name);
                        if (hexCharToValue(c2) >= 0) {
                            j++;
                            i = i3;
                        } else {
                            throw unexpectedCharacterException(uri, name, c2, i3 - 1);
                        }
                    }
                    continue;
                } else {
                    throw unexpectedCharacterException(uri, name, c, i - 1);
                }
            }
        }
        return uri.substring(start, end);
    }

    private static int hexCharToValue(char c) {
        if ('0' <= c && c <= '9') {
            return c - '0';
        }
        if ('a' <= c && c <= 'f') {
            return (10 + c) - 97;
        }
        if ('A' > c || c > 'F') {
            return -1;
        }
        return (10 + c) - 65;
    }

    private static URISyntaxException unexpectedCharacterException(String uri, String name, char unexpected, int index) {
        String nameString;
        if (name == null) {
            nameString = "";
        } else {
            nameString = " in [" + name + "]";
        }
        return new URISyntaxException(uri, "Unexpected character" + nameString + PluralRules.KEYWORD_RULE_SEPARATOR + unexpected, index);
    }

    private static char getNextCharacter(String uri, int index, int end, String name) throws URISyntaxException {
        String nameString;
        if (index < end) {
            return uri.charAt(index);
        }
        if (name == null) {
            nameString = "";
        } else {
            nameString = " in [" + name + "]";
        }
        throw new URISyntaxException(uri, "Unexpected end of string" + nameString, index);
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
            } else if (!cBuffer.hasRemaining()) {
                encoder.flush(byteBuffer);
                if (result == CoderResult.UNDERFLOW) {
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
                    return;
                }
                throw new IllegalArgumentException("Error encoding, unexpected result [" + result.toString() + "] flushing encoder for [" + encoder.charset().name() + "]");
            } else {
                throw new IllegalArgumentException("Encoder for [" + encoder.charset().name() + "] failed with underflow with remaining input [" + cBuffer + "]");
            }
        }
    }

    private static char intToHexDigit(int b) {
        if (b < 10) {
            return (char) (48 + b);
        }
        return (char) ((65 + b) - 10);
    }

    public static String decode(String s, boolean convertPlus, Charset charset, boolean throwOnFailure) {
        StringBuilder builder = new StringBuilder(s.length());
        appendDecoded(builder, s, convertPlus, charset, throwOnFailure);
        return builder.toString();
    }

    private static void appendDecoded(StringBuilder builder, String s, boolean convertPlus, Charset charset, boolean throwOnFailure) {
        CharsetDecoder decoder = charset.newDecoder().onMalformedInput(CodingErrorAction.REPLACE).replaceWith(XLocaleDistance.ANY).onUnmappableCharacter(CodingErrorAction.REPORT);
        ByteBuffer byteBuffer = ByteBuffer.allocate(s.length());
        int i = 0;
        while (i < s.length()) {
            char c = s.charAt(i);
            i++;
            if (c != '%') {
                char c2 = '+';
                if (c != '+') {
                    flushDecodingByteAccumulator(builder, decoder, byteBuffer, throwOnFailure);
                    builder.append(c);
                } else {
                    flushDecodingByteAccumulator(builder, decoder, byteBuffer, throwOnFailure);
                    if (convertPlus) {
                        c2 = ' ';
                    }
                    builder.append(c2);
                }
            } else {
                byte hexValue = 0;
                byte hexValue2 = c;
                int i2 = i;
                int j = 0;
                while (true) {
                    if (j >= 2) {
                        break;
                    }
                    try {
                        char c3 = getNextCharacter(s, i2, s.length(), null);
                        i2++;
                        int newDigit = hexCharToValue(c3);
                        if (newDigit >= 0) {
                            hexValue = (byte) ((hexValue * 16) + newDigit);
                            j++;
                        } else if (!throwOnFailure) {
                            flushDecodingByteAccumulator(builder, decoder, byteBuffer, throwOnFailure);
                            builder.append(INVALID_INPUT_CHARACTER);
                        } else {
                            throw new IllegalArgumentException(unexpectedCharacterException(s, null, c3, i2 - 1));
                        }
                    } catch (URISyntaxException e) {
                        if (!throwOnFailure) {
                            flushDecodingByteAccumulator(builder, decoder, byteBuffer, throwOnFailure);
                            builder.append(INVALID_INPUT_CHARACTER);
                            return;
                        }
                        throw new IllegalArgumentException(e);
                    }
                }
                byteBuffer.put(hexValue);
                i = i2;
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
                if (!throwOnFailure) {
                    builder.append(INVALID_INPUT_CHARACTER);
                } else {
                    throw new IllegalArgumentException(e);
                }
            } catch (Throwable th) {
                byteBuffer.flip();
                byteBuffer.limit(byteBuffer.capacity());
                throw th;
            }
            byteBuffer.flip();
            byteBuffer.limit(byteBuffer.capacity());
        }
    }

    public static String decode(String s) {
        return decode(s, false, StandardCharsets.UTF_8, true);
    }
}
