package com.huawei.zxing.qrcode.decoder;

import android.telephony.HwCarrierConfigManager;
import com.huawei.android.app.AppOpsManagerEx;
import com.huawei.internal.telephony.uicc.IccConstantsEx;
import com.huawei.motiondetection.MotionTypeApps;
import com.huawei.zxing.DecodeHintType;
import com.huawei.zxing.FormatException;
import com.huawei.zxing.common.BitSource;
import com.huawei.zxing.common.CharacterSetECI;
import com.huawei.zxing.common.DecoderResult;
import com.huawei.zxing.common.StringUtils;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

final class DecodedBitStreamParser {
    private static final char[] ALPHANUMERIC_CHARS = null;
    private static final int GB2312_SUBSET = 1;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.zxing.qrcode.decoder.DecodedBitStreamParser.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.huawei.zxing.qrcode.decoder.DecodedBitStreamParser.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.zxing.qrcode.decoder.DecodedBitStreamParser.<clinit>():void");
    }

    private DecodedBitStreamParser() {
    }

    static DecoderResult decode(byte[] bytes, Version version, ErrorCorrectionLevel ecLevel, Map<DecodeHintType, ?> hints) throws FormatException {
        BitSource bits = new BitSource(bytes);
        StringBuilder result = new StringBuilder(50);
        List byteSegments = new ArrayList(GB2312_SUBSET);
        CharacterSetECI currentCharacterSetECI = null;
        boolean fc1InEffect = false;
        Mode mode;
        do {
            if (bits.available() < 4) {
                mode = Mode.TERMINATOR;
            } else {
                try {
                    mode = Mode.forBits(bits.readBits(4));
                } catch (IllegalArgumentException e) {
                    throw FormatException.getFormatInstance();
                }
            }
            if (mode != Mode.TERMINATOR) {
                if (mode == Mode.FNC1_FIRST_POSITION || mode == Mode.FNC1_SECOND_POSITION) {
                    fc1InEffect = true;
                } else if (mode == Mode.STRUCTURED_APPEND) {
                    if (bits.available() < 16) {
                        throw FormatException.getFormatInstance();
                    }
                    bits.readBits(16);
                } else if (mode == Mode.ECI) {
                    currentCharacterSetECI = CharacterSetECI.getCharacterSetECIByValue(parseECIValue(bits));
                    if (currentCharacterSetECI == null) {
                        throw FormatException.getFormatInstance();
                    }
                } else if (mode == Mode.HANZI) {
                    int subset = bits.readBits(4);
                    int countHanzi = bits.readBits(mode.getCharacterCountBits(version));
                    if (subset == GB2312_SUBSET) {
                        decodeHanziSegment(bits, result, countHanzi);
                    }
                } else {
                    int count = bits.readBits(mode.getCharacterCountBits(version));
                    if (mode == Mode.NUMERIC) {
                        decodeNumericSegment(bits, result, count);
                    } else if (mode == Mode.ALPHANUMERIC) {
                        decodeAlphanumericSegment(bits, result, count, fc1InEffect);
                    } else if (mode == Mode.BYTE) {
                        decodeByteSegment(bits, result, count, currentCharacterSetECI, byteSegments, hints);
                    } else if (mode == Mode.KANJI) {
                        decodeKanjiSegment(bits, result, count);
                    } else {
                        throw FormatException.getFormatInstance();
                    }
                }
            }
        } while (mode != Mode.TERMINATOR);
        String stringBuilder = result.toString();
        if (byteSegments.isEmpty()) {
            byteSegments = null;
        }
        return new DecoderResult(bytes, stringBuilder, byteSegments, ecLevel == null ? null : ecLevel.toString());
    }

    private static void decodeHanziSegment(BitSource bits, StringBuilder result, int count) throws FormatException {
        if (count * 13 > bits.available()) {
            throw FormatException.getFormatInstance();
        }
        byte[] buffer = new byte[(count * 2)];
        int offset = 0;
        while (count > 0) {
            int twoBytes = bits.readBits(13);
            int assembledTwoBytes = ((twoBytes / 96) << 8) | (twoBytes % 96);
            if (assembledTwoBytes < 959) {
                assembledTwoBytes += 41377;
            } else {
                assembledTwoBytes += 42657;
            }
            buffer[offset] = (byte) ((assembledTwoBytes >> 8) & IccConstantsEx.RUIM_SMS_BEARER_DATA_LEN);
            buffer[offset + GB2312_SUBSET] = (byte) (assembledTwoBytes & IccConstantsEx.RUIM_SMS_BEARER_DATA_LEN);
            offset += 2;
            count--;
        }
        try {
            result.append(new String(buffer, StringUtils.GB2312));
        } catch (UnsupportedEncodingException e) {
            throw FormatException.getFormatInstance();
        }
    }

    private static void decodeKanjiSegment(BitSource bits, StringBuilder result, int count) throws FormatException {
        if (count * 13 > bits.available()) {
            throw FormatException.getFormatInstance();
        }
        byte[] buffer = new byte[(count * 2)];
        int offset = 0;
        while (count > 0) {
            int twoBytes = bits.readBits(13);
            int assembledTwoBytes = ((twoBytes / HwCarrierConfigManager.HD_ICON_MASK_DIALER) << 8) | (twoBytes % HwCarrierConfigManager.HD_ICON_MASK_DIALER);
            if (assembledTwoBytes < 7936) {
                assembledTwoBytes += 33088;
            } else {
                assembledTwoBytes += 49472;
            }
            buffer[offset] = (byte) (assembledTwoBytes >> 8);
            buffer[offset + GB2312_SUBSET] = (byte) assembledTwoBytes;
            offset += 2;
            count--;
        }
        try {
            result.append(new String(buffer, StringUtils.SHIFT_JIS));
        } catch (UnsupportedEncodingException e) {
            throw FormatException.getFormatInstance();
        }
    }

    private static void decodeByteSegment(BitSource bits, StringBuilder result, int count, CharacterSetECI currentCharacterSetECI, Collection<byte[]> byteSegments, Map<DecodeHintType, ?> hints) throws FormatException {
        if ((count << 3) > bits.available()) {
            throw FormatException.getFormatInstance();
        }
        String encoding;
        byte[] readBytes = new byte[count];
        for (int i = 0; i < count; i += GB2312_SUBSET) {
            readBytes[i] = (byte) bits.readBits(8);
        }
        if (currentCharacterSetECI == null) {
            encoding = StringUtils.guessEncoding(readBytes, hints);
        } else {
            encoding = currentCharacterSetECI.name();
        }
        try {
            result.append(new String(readBytes, encoding));
            byteSegments.add(readBytes);
        } catch (UnsupportedEncodingException e) {
            throw FormatException.getFormatInstance();
        }
    }

    private static char toAlphaNumericChar(int value) throws FormatException {
        if (value < ALPHANUMERIC_CHARS.length) {
            return ALPHANUMERIC_CHARS[value];
        }
        throw FormatException.getFormatInstance();
    }

    private static void decodeAlphanumericSegment(BitSource bits, StringBuilder result, int count, boolean fc1InEffect) throws FormatException {
        int start = result.length();
        while (count > GB2312_SUBSET) {
            if (bits.available() < 11) {
                throw FormatException.getFormatInstance();
            }
            int nextTwoCharsBits = bits.readBits(11);
            result.append(toAlphaNumericChar(nextTwoCharsBits / 45));
            result.append(toAlphaNumericChar(nextTwoCharsBits % 45));
            count -= 2;
        }
        if (count == GB2312_SUBSET) {
            if (bits.available() < 6) {
                throw FormatException.getFormatInstance();
            }
            result.append(toAlphaNumericChar(bits.readBits(6)));
        }
        if (fc1InEffect) {
            int i = start;
            while (i < result.length()) {
                if (result.charAt(i) == '%') {
                    if (i >= result.length() - 1 || result.charAt(i + GB2312_SUBSET) != '%') {
                        result.setCharAt(i, '\u001d');
                    } else {
                        result.deleteCharAt(i + GB2312_SUBSET);
                    }
                }
                i += GB2312_SUBSET;
            }
        }
    }

    private static void decodeNumericSegment(BitSource bits, StringBuilder result, int count) throws FormatException {
        while (count >= 3) {
            if (bits.available() < 10) {
                throw FormatException.getFormatInstance();
            }
            int threeDigitsBits = bits.readBits(10);
            if (threeDigitsBits >= MotionTypeApps.TYPE_TAKE_OFF) {
                throw FormatException.getFormatInstance();
            }
            result.append(toAlphaNumericChar(threeDigitsBits / 100));
            result.append(toAlphaNumericChar((threeDigitsBits / 10) % 10));
            result.append(toAlphaNumericChar(threeDigitsBits % 10));
            count -= 3;
        }
        if (count == 2) {
            if (bits.available() < 7) {
                throw FormatException.getFormatInstance();
            }
            int twoDigitsBits = bits.readBits(7);
            if (twoDigitsBits >= 100) {
                throw FormatException.getFormatInstance();
            }
            result.append(toAlphaNumericChar(twoDigitsBits / 10));
            result.append(toAlphaNumericChar(twoDigitsBits % 10));
        } else if (count != GB2312_SUBSET) {
        } else {
            if (bits.available() < 4) {
                throw FormatException.getFormatInstance();
            }
            int digitBits = bits.readBits(4);
            if (digitBits >= 10) {
                throw FormatException.getFormatInstance();
            }
            result.append(toAlphaNumericChar(digitBits));
        }
    }

    private static int parseECIValue(BitSource bits) throws FormatException {
        int firstByte = bits.readBits(8);
        if ((firstByte & AppOpsManagerEx.TYPE_MICROPHONE) == 0) {
            return firstByte & 127;
        }
        if ((firstByte & HwCarrierConfigManager.HD_ICON_MASK_DIALER) == AppOpsManagerEx.TYPE_MICROPHONE) {
            return ((firstByte & 63) << 8) | bits.readBits(8);
        } else if ((firstByte & 224) == HwCarrierConfigManager.HD_ICON_MASK_DIALER) {
            return ((firstByte & 31) << 16) | bits.readBits(16);
        } else {
            throw FormatException.getFormatInstance();
        }
    }
}
