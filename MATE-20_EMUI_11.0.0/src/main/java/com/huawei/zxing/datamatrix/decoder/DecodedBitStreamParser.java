package com.huawei.zxing.datamatrix.decoder;

import com.huawei.zxing.FormatException;
import com.huawei.zxing.common.BitSource;
import com.huawei.zxing.common.DecoderResult;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/* access modifiers changed from: package-private */
public final class DecodedBitStreamParser {
    private static final char[] C40_BASIC_SET_CHARS = {'*', '*', '*', ' ', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'};
    private static final char[] C40_SHIFT2_SET_CHARS = {'!', '\"', '#', '$', '%', '&', '\'', '(', ')', '*', '+', ',', '-', '.', '/', ':', ';', '<', '=', '>', '?', '@', '[', '\\', ']', '^', '_'};
    private static final char[] TEXT_BASIC_SET_CHARS = {'*', '*', '*', ' ', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'};
    private static final char[] TEXT_SHIFT3_SET_CHARS = {'`', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '{', '|', '}', '~', 127};

    /* access modifiers changed from: private */
    public enum Mode {
        PAD_ENCODE,
        ASCII_ENCODE,
        C40_ENCODE,
        TEXT_ENCODE,
        ANSIX12_ENCODE,
        EDIFACT_ENCODE,
        BASE256_ENCODE
    }

    private DecodedBitStreamParser() {
    }

    static DecoderResult decode(byte[] bytes) throws FormatException {
        BitSource bits = new BitSource(bytes);
        StringBuilder result = new StringBuilder(100);
        StringBuilder resultTrailer = new StringBuilder(0);
        List<byte[]> byteSegments = new ArrayList<>(1);
        Mode mode = Mode.ASCII_ENCODE;
        do {
            if (mode == Mode.ASCII_ENCODE) {
                mode = decodeAsciiSegment(bits, result, resultTrailer);
            } else {
                int i = AnonymousClass1.$SwitchMap$com$huawei$zxing$datamatrix$decoder$DecodedBitStreamParser$Mode[mode.ordinal()];
                if (i == 1) {
                    decodeC40Segment(bits, result);
                } else if (i == 2) {
                    decodeTextSegment(bits, result);
                } else if (i == 3) {
                    decodeAnsiX12Segment(bits, result);
                } else if (i == 4) {
                    decodeEdifactSegment(bits, result);
                } else if (i == 5) {
                    decodeBase256Segment(bits, result, byteSegments);
                } else {
                    throw FormatException.getFormatInstance();
                }
                mode = Mode.ASCII_ENCODE;
            }
            if (mode == Mode.PAD_ENCODE) {
                break;
            }
        } while (bits.available() > 0);
        if (resultTrailer.length() > 0) {
            result.append(resultTrailer.toString());
        }
        return new DecoderResult(bytes, result.toString(), byteSegments.isEmpty() ? null : byteSegments, null);
    }

    /* access modifiers changed from: package-private */
    /* renamed from: com.huawei.zxing.datamatrix.decoder.DecodedBitStreamParser$1  reason: invalid class name */
    public static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$huawei$zxing$datamatrix$decoder$DecodedBitStreamParser$Mode = new int[Mode.values().length];

        static {
            try {
                $SwitchMap$com$huawei$zxing$datamatrix$decoder$DecodedBitStreamParser$Mode[Mode.C40_ENCODE.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$huawei$zxing$datamatrix$decoder$DecodedBitStreamParser$Mode[Mode.TEXT_ENCODE.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$huawei$zxing$datamatrix$decoder$DecodedBitStreamParser$Mode[Mode.ANSIX12_ENCODE.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$com$huawei$zxing$datamatrix$decoder$DecodedBitStreamParser$Mode[Mode.EDIFACT_ENCODE.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$com$huawei$zxing$datamatrix$decoder$DecodedBitStreamParser$Mode[Mode.BASE256_ENCODE.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
        }
    }

    private static Mode decodeAsciiSegment(BitSource bits, StringBuilder result, StringBuilder resultTrailer) throws FormatException {
        boolean upperShift = false;
        do {
            int oneByte = bits.readBits(8);
            if (oneByte == 0) {
                throw FormatException.getFormatInstance();
            } else if (oneByte <= 128) {
                if (upperShift) {
                    oneByte += 128;
                }
                result.append((char) (oneByte - 1));
                return Mode.ASCII_ENCODE;
            } else if (oneByte == 129) {
                return Mode.PAD_ENCODE;
            } else {
                if (oneByte <= 229) {
                    int value = oneByte - 130;
                    if (value < 10) {
                        result.append('0');
                    }
                    result.append(value);
                } else if (oneByte == 230) {
                    return Mode.C40_ENCODE;
                } else {
                    if (oneByte == 231) {
                        return Mode.BASE256_ENCODE;
                    }
                    if (oneByte == 232) {
                        result.append((char) 29);
                    } else if (!(oneByte == 233 || oneByte == 234)) {
                        if (oneByte == 235) {
                            upperShift = true;
                        } else if (oneByte == 236) {
                            result.append("[)>\u001e05\u001d");
                            resultTrailer.insert(0, "\u001e\u0004");
                        } else if (oneByte == 237) {
                            result.append("[)>\u001e06\u001d");
                            resultTrailer.insert(0, "\u001e\u0004");
                        } else if (oneByte == 238) {
                            return Mode.ANSIX12_ENCODE;
                        } else {
                            if (oneByte == 239) {
                                return Mode.TEXT_ENCODE;
                            }
                            if (oneByte == 240) {
                                return Mode.EDIFACT_ENCODE;
                            }
                            if (!(oneByte == 241 || oneByte < 242 || (oneByte == 254 && bits.available() == 0))) {
                                throw FormatException.getFormatInstance();
                            }
                        }
                    }
                }
            }
        } while (bits.available() > 0);
        return Mode.ASCII_ENCODE;
    }

    private static void decodeC40Segment(BitSource bits, StringBuilder result) throws FormatException {
        int firstByte;
        boolean upperShift = false;
        int[] cValues = new int[3];
        int shift = 0;
        while (bits.available() != 8 && (firstByte = bits.readBits(8)) != 254) {
            parseTwoBytes(firstByte, bits.readBits(8), cValues);
            for (int i = 0; i < 3; i++) {
                int cValue = cValues[i];
                if (shift != 0) {
                    if (shift == 1) {
                        if (upperShift) {
                            result.append((char) (cValue + 128));
                            upperShift = false;
                        } else {
                            result.append((char) cValue);
                        }
                        shift = 0;
                    } else if (shift == 2) {
                        char[] cArr = C40_SHIFT2_SET_CHARS;
                        if (cValue < cArr.length) {
                            char c40char = cArr[cValue];
                            if (upperShift) {
                                result.append((char) (c40char + 128));
                                upperShift = false;
                            } else {
                                result.append(c40char);
                            }
                        } else if (cValue == 27) {
                            result.append((char) 29);
                        } else if (cValue == 30) {
                            upperShift = true;
                        } else {
                            throw FormatException.getFormatInstance();
                        }
                        shift = 0;
                    } else if (shift == 3) {
                        if (upperShift) {
                            result.append((char) (cValue + 224));
                            upperShift = false;
                        } else {
                            result.append((char) (cValue + 96));
                        }
                        shift = 0;
                    } else {
                        throw FormatException.getFormatInstance();
                    }
                } else if (cValue < 3) {
                    shift = cValue + 1;
                } else {
                    char[] cArr2 = C40_BASIC_SET_CHARS;
                    if (cValue < cArr2.length) {
                        char c40char2 = cArr2[cValue];
                        if (upperShift) {
                            result.append((char) (c40char2 + 128));
                            upperShift = false;
                        } else {
                            result.append(c40char2);
                        }
                    } else {
                        throw FormatException.getFormatInstance();
                    }
                }
            }
            if (bits.available() <= 0) {
                return;
            }
        }
    }

    private static void decodeTextSegment(BitSource bits, StringBuilder result) throws FormatException {
        int firstByte;
        boolean upperShift = false;
        int[] cValues = new int[3];
        int shift = 0;
        while (bits.available() != 8 && (firstByte = bits.readBits(8)) != 254) {
            parseTwoBytes(firstByte, bits.readBits(8), cValues);
            for (int i = 0; i < 3; i++) {
                int cValue = cValues[i];
                if (shift != 0) {
                    if (shift == 1) {
                        if (upperShift) {
                            result.append((char) (cValue + 128));
                            upperShift = false;
                        } else {
                            result.append((char) cValue);
                        }
                        shift = 0;
                    } else if (shift == 2) {
                        char[] cArr = C40_SHIFT2_SET_CHARS;
                        if (cValue < cArr.length) {
                            char c40char = cArr[cValue];
                            if (upperShift) {
                                result.append((char) (c40char + 128));
                                upperShift = false;
                            } else {
                                result.append(c40char);
                            }
                        } else if (cValue == 27) {
                            result.append((char) 29);
                        } else if (cValue == 30) {
                            upperShift = true;
                        } else {
                            throw FormatException.getFormatInstance();
                        }
                        shift = 0;
                    } else if (shift == 3) {
                        char[] cArr2 = TEXT_SHIFT3_SET_CHARS;
                        if (cValue < cArr2.length) {
                            char textChar = cArr2[cValue];
                            if (upperShift) {
                                result.append((char) (textChar + 128));
                                upperShift = false;
                            } else {
                                result.append(textChar);
                            }
                            shift = 0;
                        } else {
                            throw FormatException.getFormatInstance();
                        }
                    } else {
                        throw FormatException.getFormatInstance();
                    }
                } else if (cValue < 3) {
                    shift = cValue + 1;
                } else {
                    char[] cArr3 = TEXT_BASIC_SET_CHARS;
                    if (cValue < cArr3.length) {
                        char textChar2 = cArr3[cValue];
                        if (upperShift) {
                            result.append((char) (textChar2 + 128));
                            upperShift = false;
                        } else {
                            result.append(textChar2);
                        }
                    } else {
                        throw FormatException.getFormatInstance();
                    }
                }
            }
            if (bits.available() <= 0) {
                return;
            }
        }
    }

    private static void decodeAnsiX12Segment(BitSource bits, StringBuilder result) throws FormatException {
        int firstByte;
        int[] cValues = new int[3];
        while (bits.available() != 8 && (firstByte = bits.readBits(8)) != 254) {
            parseTwoBytes(firstByte, bits.readBits(8), cValues);
            for (int i = 0; i < 3; i++) {
                int cValue = cValues[i];
                if (cValue == 0) {
                    result.append('\r');
                } else if (cValue == 1) {
                    result.append('*');
                } else if (cValue == 2) {
                    result.append('>');
                } else if (cValue == 3) {
                    result.append(' ');
                } else if (cValue < 14) {
                    result.append((char) (cValue + 44));
                } else if (cValue < 40) {
                    result.append((char) (cValue + 51));
                } else {
                    throw FormatException.getFormatInstance();
                }
            }
            if (bits.available() <= 0) {
                return;
            }
        }
    }

    private static void parseTwoBytes(int firstByte, int secondByte, int[] result) {
        int fullBitValue = ((firstByte << 8) + secondByte) - 1;
        int temp = fullBitValue / 1600;
        result[0] = temp;
        int fullBitValue2 = fullBitValue - (temp * 1600);
        int temp2 = fullBitValue2 / 40;
        result[1] = temp2;
        result[2] = fullBitValue2 - (temp2 * 40);
    }

    private static void decodeEdifactSegment(BitSource bits, StringBuilder result) {
        while (bits.available() > 16) {
            for (int i = 0; i < 4; i++) {
                int edifactValue = bits.readBits(6);
                if (edifactValue == 31) {
                    int bitsLeft = 8 - bits.getBitOffset();
                    if (bitsLeft != 8) {
                        bits.readBits(bitsLeft);
                        return;
                    }
                    return;
                }
                if ((edifactValue & 32) == 0) {
                    edifactValue |= 64;
                }
                result.append((char) edifactValue);
            }
            if (bits.available() <= 0) {
                return;
            }
        }
    }

    private static void decodeBase256Segment(BitSource bits, StringBuilder result, Collection<byte[]> byteSegments) throws FormatException {
        int count;
        int codewordPosition = bits.getByteOffset() + 1;
        int codewordPosition2 = codewordPosition + 1;
        int d1 = unrandomize255State(bits.readBits(8), codewordPosition);
        if (d1 == 0) {
            count = bits.available() / 8;
        } else if (d1 < 250) {
            count = d1;
        } else {
            count = unrandomize255State(bits.readBits(8), codewordPosition2) + ((d1 - 249) * 250);
            codewordPosition2++;
        }
        if (count >= 0) {
            byte[] bytes = new byte[count];
            int i = 0;
            while (i < count) {
                if (bits.available() >= 8) {
                    bytes[i] = (byte) unrandomize255State(bits.readBits(8), codewordPosition2);
                    i++;
                    codewordPosition2++;
                } else {
                    throw FormatException.getFormatInstance();
                }
            }
            byteSegments.add(bytes);
            try {
                result.append(new String(bytes, "ISO8859_1"));
            } catch (UnsupportedEncodingException uee) {
                throw new IllegalStateException("Platform does not support required encoding: " + uee);
            }
        } else {
            throw FormatException.getFormatInstance();
        }
    }

    private static int unrandomize255State(int randomizedBase256Codeword, int base256CodewordPosition) {
        int tempVariable = randomizedBase256Codeword - (((base256CodewordPosition * 149) % 255) + 1);
        return tempVariable >= 0 ? tempVariable : tempVariable + 256;
    }
}
