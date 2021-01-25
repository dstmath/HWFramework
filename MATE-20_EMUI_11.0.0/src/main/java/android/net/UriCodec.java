package android.net;

import android.net.wifi.WifiScanner;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;

public final class UriCodec {
    private static final char INVALID_INPUT_CHARACTER = 65533;

    private UriCodec() {
    }

    private static int hexCharToValue(char c) {
        if ('0' <= c && c <= '9') {
            return c - '0';
        }
        if ('a' <= c && c <= 'f') {
            return (c + '\n') - 97;
        }
        if ('A' > c || c > 'F') {
            return -1;
        }
        return (c + '\n') - 65;
    }

    private static URISyntaxException unexpectedCharacterException(String uri, String name, char unexpected, int index) {
        String nameString;
        if (name == null) {
            nameString = "";
        } else {
            nameString = " in [" + name + "]";
        }
        return new URISyntaxException(uri, "Unexpected character" + nameString + ": " + unexpected, index);
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
                int j = 0;
                while (true) {
                    if (j >= 2) {
                        break;
                    }
                    try {
                        char c3 = getNextCharacter(s, i, s.length(), null);
                        i++;
                        int newDigit = hexCharToValue(c3);
                        if (newDigit >= 0) {
                            hexValue = (byte) ((hexValue * WifiScanner.PnoSettings.PnoNetwork.FLAG_SAME_NETWORK) + newDigit);
                            j++;
                        } else if (!throwOnFailure) {
                            flushDecodingByteAccumulator(builder, decoder, byteBuffer, throwOnFailure);
                            builder.append(INVALID_INPUT_CHARACTER);
                        } else {
                            throw new IllegalArgumentException(unexpectedCharacterException(s, null, c3, i - 1));
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
            }
        }
        flushDecodingByteAccumulator(builder, decoder, byteBuffer, throwOnFailure);
    }

    private static void flushDecodingByteAccumulator(StringBuilder builder, CharsetDecoder decoder, ByteBuffer byteBuffer, boolean throwOnFailure) {
        if (byteBuffer.position() != 0) {
            byteBuffer.flip();
            try {
                builder.append((CharSequence) decoder.decode(byteBuffer));
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
}
