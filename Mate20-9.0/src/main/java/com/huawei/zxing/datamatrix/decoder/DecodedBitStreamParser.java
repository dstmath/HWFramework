package com.huawei.zxing.datamatrix.decoder;

import com.huawei.android.app.AppOpsManagerEx;
import com.huawei.immersion.Vibetonz;
import com.huawei.zxing.FormatException;
import com.huawei.zxing.common.BitSource;
import com.huawei.zxing.common.DecoderResult;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

final class DecodedBitStreamParser {
    private static final char[] C40_BASIC_SET_CHARS = {'*', '*', '*', ' ', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'};
    private static final char[] C40_SHIFT2_SET_CHARS = {'!', '\"', '#', '$', '%', '&', '\'', '(', ')', '*', '+', ',', '-', '.', '/', ':', ';', '<', '=', '>', '?', '@', '[', '\\', ']', '^', '_'};
    private static final char[] TEXT_BASIC_SET_CHARS = {'*', '*', '*', ' ', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'};
    private static final char[] TEXT_SHIFT3_SET_CHARS = {'`', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '{', '|', '}', '~', 127};

    private enum Mode {
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

    /* JADX WARNING: Removed duplicated region for block: B:20:0x0059  */
    /* JADX WARNING: Removed duplicated region for block: B:23:0x006d  */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x006f  */
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
                switch (mode) {
                    case C40_ENCODE:
                        decodeC40Segment(bits, result);
                        break;
                    case TEXT_ENCODE:
                        decodeTextSegment(bits, result);
                        break;
                    case ANSIX12_ENCODE:
                        decodeAnsiX12Segment(bits, result);
                        break;
                    case EDIFACT_ENCODE:
                        decodeEdifactSegment(bits, result);
                        break;
                    case BASE256_ENCODE:
                        decodeBase256Segment(bits, result, byteSegments);
                        break;
                    default:
                        throw FormatException.getFormatInstance();
                }
                mode = Mode.ASCII_ENCODE;
            }
            if (mode != Mode.PAD_ENCODE) {
            }
            if (resultTrailer.length() > 0) {
                result.append(resultTrailer.toString());
            }
            return new DecoderResult(bytes, result.toString(), !byteSegments.isEmpty() ? null : byteSegments, null);
        } while (bits.available() > 0);
        if (resultTrailer.length() > 0) {
        }
        return new DecoderResult(bytes, result.toString(), !byteSegments.isEmpty() ? null : byteSegments, null);
    }

    private static Mode decodeAsciiSegment(BitSource bits, StringBuilder result, StringBuilder resultTrailer) throws FormatException {
        boolean upperShift = false;
        do {
            int oneByte = bits.readBits(8);
            if (oneByte == 0) {
                throw FormatException.getFormatInstance();
            } else if (oneByte <= 128) {
                if (upperShift) {
                    oneByte += AppOpsManagerEx.TYPE_MICROPHONE;
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
                        result.append(29);
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
        int[] cValues = new int[3];
        boolean shift = false;
        int shift2 = 0;
        while (bits.available() != 8) {
            int firstByte = bits.readBits(8);
            if (firstByte != 254) {
                parseTwoBytes(firstByte, bits.readBits(8), cValues);
                boolean upperShift = shift;
                int shift3 = shift2;
                for (int i = 0; i < 3; i++) {
                    int cValue = cValues[i];
                    switch (shift3) {
                        case 0:
                            if (cValue < 3) {
                                shift3 = cValue + 1;
                                break;
                            } else if (cValue < C40_BASIC_SET_CHARS.length) {
                                char c40char = C40_BASIC_SET_CHARS[cValue];
                                if (!upperShift) {
                                    result.append(c40char);
                                    break;
                                } else {
                                    result.append((char) (c40char + 128));
                                    upperShift = false;
                                    break;
                                }
                            } else {
                                throw FormatException.getFormatInstance();
                            }
                        case 1:
                            if (upperShift) {
                                result.append((char) (cValue + AppOpsManagerEx.TYPE_MICROPHONE));
                                upperShift = false;
                            } else {
                                result.append((char) cValue);
                            }
                            shift3 = 0;
                            break;
                        case 2:
                            if (cValue < C40_SHIFT2_SET_CHARS.length) {
                                char c40char2 = C40_SHIFT2_SET_CHARS[cValue];
                                if (upperShift) {
                                    result.append((char) (c40char2 + 128));
                                    upperShift = false;
                                } else {
                                    result.append(c40char2);
                                }
                            } else if (cValue == 27) {
                                result.append(29);
                            } else if (cValue == 30) {
                                upperShift = true;
                            } else {
                                throw FormatException.getFormatInstance();
                            }
                            shift3 = 0;
                            break;
                        case 3:
                            if (upperShift) {
                                result.append((char) (cValue + 224));
                                upperShift = false;
                            } else {
                                result.append((char) (cValue + 96));
                            }
                            shift3 = 0;
                            break;
                        default:
                            throw FormatException.getFormatInstance();
                    }
                }
                if (bits.available() > 0) {
                    shift2 = shift3;
                    shift = upperShift;
                } else {
                    return;
                }
            } else {
                return;
            }
        }
    }

    private static void decodeTextSegment(BitSource bits, StringBuilder result) throws FormatException {
        int[] cValues = new int[3];
        boolean shift = false;
        int shift2 = 0;
        while (bits.available() != 8) {
            int firstByte = bits.readBits(8);
            if (firstByte != 254) {
                parseTwoBytes(firstByte, bits.readBits(8), cValues);
                boolean upperShift = shift;
                int shift3 = shift2;
                for (int i = 0; i < 3; i++) {
                    int cValue = cValues[i];
                    switch (shift3) {
                        case 0:
                            if (cValue < 3) {
                                shift3 = cValue + 1;
                                break;
                            } else if (cValue < TEXT_BASIC_SET_CHARS.length) {
                                char textChar = TEXT_BASIC_SET_CHARS[cValue];
                                if (!upperShift) {
                                    result.append(textChar);
                                    break;
                                } else {
                                    result.append((char) (textChar + 128));
                                    upperShift = false;
                                    break;
                                }
                            } else {
                                throw FormatException.getFormatInstance();
                            }
                        case 1:
                            if (upperShift) {
                                result.append((char) (cValue + AppOpsManagerEx.TYPE_MICROPHONE));
                                upperShift = false;
                            } else {
                                result.append((char) cValue);
                            }
                            shift3 = 0;
                            break;
                        case 2:
                            if (cValue < C40_SHIFT2_SET_CHARS.length) {
                                char c40char = C40_SHIFT2_SET_CHARS[cValue];
                                if (upperShift) {
                                    result.append((char) (c40char + 128));
                                    upperShift = false;
                                } else {
                                    result.append(c40char);
                                }
                            } else if (cValue == 27) {
                                result.append(29);
                            } else if (cValue == 30) {
                                upperShift = true;
                            } else {
                                throw FormatException.getFormatInstance();
                            }
                            shift3 = 0;
                            break;
                        case 3:
                            if (cValue < TEXT_SHIFT3_SET_CHARS.length) {
                                char textChar2 = TEXT_SHIFT3_SET_CHARS[cValue];
                                if (upperShift) {
                                    result.append((char) (textChar2 + 128));
                                    upperShift = false;
                                } else {
                                    result.append(textChar2);
                                }
                                shift3 = 0;
                                break;
                            } else {
                                throw FormatException.getFormatInstance();
                            }
                        default:
                            throw FormatException.getFormatInstance();
                    }
                }
                if (bits.available() > 0) {
                    shift2 = shift3;
                    shift = upperShift;
                } else {
                    return;
                }
            } else {
                return;
            }
        }
    }

    private static void decodeAnsiX12Segment(BitSource bits, StringBuilder result) throws FormatException {
        int[] cValues = new int[3];
        while (bits.available() != 8) {
            int firstByte = bits.readBits(8);
            if (firstByte != 254) {
                parseTwoBytes(firstByte, bits.readBits(8), cValues);
                for (int i = 0; i < 3; i++) {
                    int cValue = cValues[i];
                    if (cValue == 0) {
                        result.append(13);
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
            } else {
                return;
            }
        }
    }

    private static void parseTwoBytes(int firstByte, int secondByte, int[] result) {
        int fullBitValue = ((firstByte << 8) + secondByte) - 1;
        int temp = fullBitValue / Vibetonz.HAPTIC_EVENT_CONTACT_ALPHA_SWITCH;
        result[0] = temp;
        int fullBitValue2 = fullBitValue - (temp * Vibetonz.HAPTIC_EVENT_CONTACT_ALPHA_SWITCH);
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
        int codewordPosition = 1 + bits.getByteOffset();
        int codewordPosition2 = codewordPosition + 1;
        int d1 = unrandomize255State(bits.readBits(8), codewordPosition);
        if (d1 == 0) {
            count = bits.available() / 8;
        } else if (d1 < 250) {
            count = d1;
        } else {
            count = (250 * (d1 - 249)) + unrandomize255State(bits.readBits(8), codewordPosition2);
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
        int tempVariable = randomizedBase256Codeword - (((149 * base256CodewordPosition) % 255) + 1);
        return tempVariable >= 0 ? tempVariable : tempVariable + 256;
    }
}
