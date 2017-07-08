package com.huawei.zxing.datamatrix.decoder;

import com.huawei.android.app.AppOpsManagerEx;
import com.huawei.immersion.Vibetonz;
import com.huawei.internal.telephony.uicc.IccConstantsEx;
import com.huawei.lcagent.client.MetricConstant;
import com.huawei.zxing.FormatException;
import com.huawei.zxing.common.BitSource;
import com.huawei.zxing.common.DecoderResult;
import huawei.android.widget.DialogContentHelper.Dex;
import huawei.android.widget.ViewDragHelper;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

final class DecodedBitStreamParser {
    private static final /* synthetic */ int[] -com-huawei-zxing-datamatrix-decoder-DecodedBitStreamParser$ModeSwitchesValues = null;
    private static final char[] C40_BASIC_SET_CHARS = null;
    private static final char[] C40_SHIFT2_SET_CHARS = null;
    private static final char[] TEXT_BASIC_SET_CHARS = null;
    private static final char[] TEXT_SHIFT3_SET_CHARS = null;

    private enum Mode {
        ;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.zxing.datamatrix.decoder.DecodedBitStreamParser.Mode.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.huawei.zxing.datamatrix.decoder.DecodedBitStreamParser.Mode.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: com.huawei.zxing.datamatrix.decoder.DecodedBitStreamParser.Mode.<clinit>():void");
        }
    }

    private static /* synthetic */ int[] -getcom-huawei-zxing-datamatrix-decoder-DecodedBitStreamParser$ModeSwitchesValues() {
        if (-com-huawei-zxing-datamatrix-decoder-DecodedBitStreamParser$ModeSwitchesValues != null) {
            return -com-huawei-zxing-datamatrix-decoder-DecodedBitStreamParser$ModeSwitchesValues;
        }
        int[] iArr = new int[Mode.values().length];
        try {
            iArr[Mode.ANSIX12_ENCODE.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[Mode.ASCII_ENCODE.ordinal()] = 6;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[Mode.BASE256_ENCODE.ordinal()] = 2;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[Mode.C40_ENCODE.ordinal()] = 3;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[Mode.EDIFACT_ENCODE.ordinal()] = 4;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[Mode.PAD_ENCODE.ordinal()] = 7;
        } catch (NoSuchFieldError e6) {
        }
        try {
            iArr[Mode.TEXT_ENCODE.ordinal()] = 5;
        } catch (NoSuchFieldError e7) {
        }
        -com-huawei-zxing-datamatrix-decoder-DecodedBitStreamParser$ModeSwitchesValues = iArr;
        return iArr;
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.zxing.datamatrix.decoder.DecodedBitStreamParser.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.huawei.zxing.datamatrix.decoder.DecodedBitStreamParser.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.zxing.datamatrix.decoder.DecodedBitStreamParser.<clinit>():void");
    }

    private DecodedBitStreamParser() {
    }

    static DecoderResult decode(byte[] bytes) throws FormatException {
        String stringBuilder;
        BitSource bits = new BitSource(bytes);
        StringBuilder result = new StringBuilder(100);
        StringBuilder resultTrailer = new StringBuilder(0);
        List<byte[]> byteSegments = new ArrayList(1);
        Mode mode = Mode.ASCII_ENCODE;
        do {
            if (mode == Mode.ASCII_ENCODE) {
                mode = decodeAsciiSegment(bits, result, resultTrailer);
            } else {
                switch (-getcom-huawei-zxing-datamatrix-decoder-DecodedBitStreamParser$ModeSwitchesValues()[mode.ordinal()]) {
                    case ViewDragHelper.STATE_DRAGGING /*1*/:
                        decodeAnsiX12Segment(bits, result);
                        break;
                    case ViewDragHelper.STATE_SETTLING /*2*/:
                        decodeBase256Segment(bits, result, byteSegments);
                        break;
                    case ViewDragHelper.DIRECTION_ALL /*3*/:
                        decodeC40Segment(bits, result);
                        break;
                    case ViewDragHelper.EDGE_TOP /*4*/:
                        decodeEdifactSegment(bits, result);
                        break;
                    case Dex.DIALOG_BODY_TWO_IMAGES /*5*/:
                        decodeTextSegment(bits, result);
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
            stringBuilder = result.toString();
            if (byteSegments.isEmpty()) {
                byteSegments = null;
            }
            return new DecoderResult(bytes, stringBuilder, byteSegments, null);
        } while (bits.available() > 0);
        if (resultTrailer.length() > 0) {
            result.append(resultTrailer.toString());
        }
        stringBuilder = result.toString();
        if (byteSegments.isEmpty()) {
            byteSegments = null;
        }
        return new DecoderResult(bytes, stringBuilder, byteSegments, null);
    }

    private static Mode decodeAsciiSegment(BitSource bits, StringBuilder result, StringBuilder resultTrailer) throws FormatException {
        boolean upperShift = false;
        do {
            int oneByte = bits.readBits(8);
            if (oneByte == 0) {
                throw FormatException.getFormatInstance();
            } else if (oneByte <= AppOpsManagerEx.TYPE_MICROPHONE) {
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
                        result.append('\u001d');
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
        boolean upperShift = false;
        int[] cValues = new int[3];
        int shift = 0;
        while (bits.available() != 8) {
            int firstByte = bits.readBits(8);
            if (firstByte != 254) {
                parseTwoBytes(firstByte, bits.readBits(8), cValues);
                for (int i = 0; i < 3; i++) {
                    int cValue = cValues[i];
                    char c40char;
                    switch (shift) {
                        case ViewDragHelper.STATE_IDLE /*0*/:
                            if (cValue < 3) {
                                shift = cValue + 1;
                                break;
                            } else if (cValue < C40_BASIC_SET_CHARS.length) {
                                c40char = C40_BASIC_SET_CHARS[cValue];
                                if (!upperShift) {
                                    result.append(c40char);
                                    break;
                                }
                                result.append((char) (c40char + AppOpsManagerEx.TYPE_MICROPHONE));
                                upperShift = false;
                                break;
                            } else {
                                throw FormatException.getFormatInstance();
                            }
                        case ViewDragHelper.STATE_DRAGGING /*1*/:
                            if (upperShift) {
                                result.append((char) (cValue + AppOpsManagerEx.TYPE_MICROPHONE));
                                upperShift = false;
                            } else {
                                result.append((char) cValue);
                            }
                            shift = 0;
                            break;
                        case ViewDragHelper.STATE_SETTLING /*2*/:
                            if (cValue < C40_SHIFT2_SET_CHARS.length) {
                                c40char = C40_SHIFT2_SET_CHARS[cValue];
                                if (upperShift) {
                                    result.append((char) (c40char + AppOpsManagerEx.TYPE_MICROPHONE));
                                    upperShift = false;
                                } else {
                                    result.append(c40char);
                                }
                            } else if (cValue == 27) {
                                result.append('\u001d');
                            } else if (cValue == 30) {
                                upperShift = true;
                            } else {
                                throw FormatException.getFormatInstance();
                            }
                            shift = 0;
                            break;
                        case ViewDragHelper.DIRECTION_ALL /*3*/:
                            if (upperShift) {
                                result.append((char) (cValue + 224));
                                upperShift = false;
                            } else {
                                result.append((char) (cValue + 96));
                            }
                            shift = 0;
                            break;
                        default:
                            throw FormatException.getFormatInstance();
                    }
                }
                if (bits.available() <= 0) {
                    return;
                }
            }
            return;
        }
    }

    private static void decodeTextSegment(BitSource bits, StringBuilder result) throws FormatException {
        boolean upperShift = false;
        int[] cValues = new int[3];
        int shift = 0;
        while (bits.available() != 8) {
            int firstByte = bits.readBits(8);
            if (firstByte != 254) {
                parseTwoBytes(firstByte, bits.readBits(8), cValues);
                for (int i = 0; i < 3; i++) {
                    int cValue = cValues[i];
                    char textChar;
                    switch (shift) {
                        case ViewDragHelper.STATE_IDLE /*0*/:
                            if (cValue < 3) {
                                shift = cValue + 1;
                                break;
                            } else if (cValue < TEXT_BASIC_SET_CHARS.length) {
                                textChar = TEXT_BASIC_SET_CHARS[cValue];
                                if (!upperShift) {
                                    result.append(textChar);
                                    break;
                                }
                                result.append((char) (textChar + AppOpsManagerEx.TYPE_MICROPHONE));
                                upperShift = false;
                                break;
                            } else {
                                throw FormatException.getFormatInstance();
                            }
                        case ViewDragHelper.STATE_DRAGGING /*1*/:
                            if (upperShift) {
                                result.append((char) (cValue + AppOpsManagerEx.TYPE_MICROPHONE));
                                upperShift = false;
                            } else {
                                result.append((char) cValue);
                            }
                            shift = 0;
                            break;
                        case ViewDragHelper.STATE_SETTLING /*2*/:
                            if (cValue < C40_SHIFT2_SET_CHARS.length) {
                                char c40char = C40_SHIFT2_SET_CHARS[cValue];
                                if (upperShift) {
                                    result.append((char) (c40char + AppOpsManagerEx.TYPE_MICROPHONE));
                                    upperShift = false;
                                } else {
                                    result.append(c40char);
                                }
                            } else if (cValue == 27) {
                                result.append('\u001d');
                            } else if (cValue == 30) {
                                upperShift = true;
                            } else {
                                throw FormatException.getFormatInstance();
                            }
                            shift = 0;
                            break;
                        case ViewDragHelper.DIRECTION_ALL /*3*/:
                            if (cValue < TEXT_SHIFT3_SET_CHARS.length) {
                                textChar = TEXT_SHIFT3_SET_CHARS[cValue];
                                if (upperShift) {
                                    result.append((char) (textChar + AppOpsManagerEx.TYPE_MICROPHONE));
                                    upperShift = false;
                                } else {
                                    result.append(textChar);
                                }
                                shift = 0;
                                break;
                            }
                            throw FormatException.getFormatInstance();
                        default:
                            throw FormatException.getFormatInstance();
                    }
                }
                if (bits.available() <= 0) {
                    return;
                }
            }
            return;
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
            return;
        }
    }

    private static void parseTwoBytes(int firstByte, int secondByte, int[] result) {
        int fullBitValue = ((firstByte << 8) + secondByte) - 1;
        int temp = fullBitValue / Vibetonz.HAPTIC_EVENT_CONTACT_ALPHA_SWITCH;
        result[0] = temp;
        fullBitValue -= temp * Vibetonz.HAPTIC_EVENT_CONTACT_ALPHA_SWITCH;
        temp = fullBitValue / 40;
        result[1] = temp;
        result[2] = fullBitValue - (temp * 40);
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
        int codewordPosition = bits.getByteOffset() + 1;
        int codewordPosition2 = codewordPosition + 1;
        int d1 = unrandomize255State(bits.readBits(8), codewordPosition);
        if (d1 == 0) {
            count = bits.available() / 8;
            codewordPosition = codewordPosition2;
        } else if (d1 < 250) {
            count = d1;
            codewordPosition = codewordPosition2;
        } else {
            codewordPosition = codewordPosition2 + 1;
            count = ((d1 - 249) * 250) + unrandomize255State(bits.readBits(8), codewordPosition2);
        }
        if (count < 0) {
            throw FormatException.getFormatInstance();
        }
        byte[] bytes = new byte[count];
        int i = 0;
        codewordPosition2 = codewordPosition;
        while (i < count) {
            if (bits.available() < 8) {
                throw FormatException.getFormatInstance();
            }
            codewordPosition = codewordPosition2 + 1;
            bytes[i] = (byte) unrandomize255State(bits.readBits(8), codewordPosition2);
            i++;
            codewordPosition2 = codewordPosition;
        }
        byteSegments.add(bytes);
        try {
            result.append(new String(bytes, "ISO8859_1"));
        } catch (UnsupportedEncodingException uee) {
            throw new IllegalStateException("Platform does not support required encoding: " + uee);
        }
    }

    private static int unrandomize255State(int randomizedBase256Codeword, int base256CodewordPosition) {
        int tempVariable = randomizedBase256Codeword - (((base256CodewordPosition * 149) % IccConstantsEx.RUIM_SMS_BEARER_DATA_LEN) + 1);
        return tempVariable >= 0 ? tempVariable : tempVariable + MetricConstant.METRIC_ID_MAX;
    }
}
