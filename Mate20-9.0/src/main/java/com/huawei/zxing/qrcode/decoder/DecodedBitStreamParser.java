package com.huawei.zxing.qrcode.decoder;

import android.telephony.HwCarrierConfigManager;
import com.huawei.android.app.AppOpsManagerEx;
import com.huawei.android.hishow.AlarmInfoEx;
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
    private static final char[] ALPHANUMERIC_CHARS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', ' ', '$', '%', '*', '+', '-', '.', '/', ':'};
    private static final int GB2312_SUBSET = 1;

    private DecodedBitStreamParser() {
    }

    static DecoderResult decode(byte[] bytes, Version version, ErrorCorrectionLevel ecLevel, Map<DecodeHintType, ?> hints) throws FormatException {
        Mode mode;
        Mode mode2;
        byte[] bArr = bytes;
        Version version2 = version;
        BitSource bits = new BitSource(bArr);
        StringBuilder result = new StringBuilder(50);
        List<byte[]> byteSegments = new ArrayList<>(1);
        CharacterSetECI currentCharacterSetECI = null;
        boolean fc1InEffect = false;
        do {
            boolean fc1InEffect2 = fc1InEffect;
            try {
                if (bits.available() < 4) {
                    mode = Mode.TERMINATOR;
                } else {
                    mode = Mode.forBits(bits.readBits(4));
                }
                mode2 = mode;
                if (mode2 != Mode.TERMINATOR) {
                    if (mode2 != Mode.FNC1_FIRST_POSITION) {
                        if (mode2 != Mode.FNC1_SECOND_POSITION) {
                            if (mode2 == Mode.STRUCTURED_APPEND) {
                                if (bits.available() >= 16) {
                                    bits.readBits(16);
                                } else {
                                    throw FormatException.getFormatInstance();
                                }
                            } else if (mode2 == Mode.ECI) {
                                currentCharacterSetECI = CharacterSetECI.getCharacterSetECIByValue(parseECIValue(bits));
                                if (currentCharacterSetECI == null) {
                                    throw FormatException.getFormatInstance();
                                }
                            } else if (mode2 == Mode.HANZI) {
                                int subset = bits.readBits(4);
                                int countHanzi = bits.readBits(mode2.getCharacterCountBits(version2));
                                if (subset == 1) {
                                    decodeHanziSegment(bits, result, countHanzi);
                                }
                            } else {
                                int count = bits.readBits(mode2.getCharacterCountBits(version2));
                                if (mode2 == Mode.NUMERIC) {
                                    decodeNumericSegment(bits, result, count);
                                } else if (mode2 == Mode.ALPHANUMERIC) {
                                    decodeAlphanumericSegment(bits, result, count, fc1InEffect2);
                                } else if (mode2 == Mode.BYTE) {
                                    decodeByteSegment(bits, result, count, currentCharacterSetECI, byteSegments, hints);
                                } else if (mode2 == Mode.KANJI) {
                                    decodeKanjiSegment(bits, result, count);
                                } else {
                                    throw FormatException.getFormatInstance();
                                }
                            }
                        }
                    }
                    fc1InEffect = true;
                }
                fc1InEffect = fc1InEffect2;
            } catch (IllegalArgumentException e) {
                throw FormatException.getFormatInstance();
            }
        } while (mode2 != Mode.TERMINATOR);
        String sb = result.toString();
        String str = null;
        List<byte[]> list = byteSegments.isEmpty() ? null : byteSegments;
        if (ecLevel != null) {
            str = ecLevel.toString();
        }
        return new DecoderResult(bArr, sb, list, str);
    }

    private static void decodeHanziSegment(BitSource bits, StringBuilder result, int count) throws FormatException {
        int i;
        if (count * 13 <= bits.available()) {
            byte[] buffer = new byte[(2 * count)];
            int offset = 0;
            while (count > 0) {
                int twoBytes = bits.readBits(13);
                int assembledTwoBytes = ((twoBytes / 96) << 8) | (twoBytes % 96);
                if (assembledTwoBytes < 959) {
                    i = 41377;
                } else {
                    i = 42657;
                }
                int assembledTwoBytes2 = assembledTwoBytes + i;
                buffer[offset] = (byte) ((assembledTwoBytes2 >> 8) & 255);
                buffer[offset + 1] = (byte) (assembledTwoBytes2 & 255);
                offset += 2;
                count--;
            }
            try {
                result.append(new String(buffer, StringUtils.GB2312));
            } catch (UnsupportedEncodingException e) {
                throw FormatException.getFormatInstance();
            }
        } else {
            throw FormatException.getFormatInstance();
        }
    }

    private static void decodeKanjiSegment(BitSource bits, StringBuilder result, int count) throws FormatException {
        int i;
        if (count * 13 <= bits.available()) {
            byte[] buffer = new byte[(2 * count)];
            int offset = 0;
            while (count > 0) {
                int twoBytes = bits.readBits(13);
                int assembledTwoBytes = ((twoBytes / HwCarrierConfigManager.HD_ICON_MASK_DIALER) << 8) | (twoBytes % HwCarrierConfigManager.HD_ICON_MASK_DIALER);
                if (assembledTwoBytes < 7936) {
                    i = 33088;
                } else {
                    i = 49472;
                }
                int assembledTwoBytes2 = assembledTwoBytes + i;
                buffer[offset] = (byte) (assembledTwoBytes2 >> 8);
                buffer[offset + 1] = (byte) assembledTwoBytes2;
                offset += 2;
                count--;
            }
            try {
                result.append(new String(buffer, StringUtils.SHIFT_JIS));
            } catch (UnsupportedEncodingException e) {
                throw FormatException.getFormatInstance();
            }
        } else {
            throw FormatException.getFormatInstance();
        }
    }

    private static void decodeByteSegment(BitSource bits, StringBuilder result, int count, CharacterSetECI currentCharacterSetECI, Collection<byte[]> byteSegments, Map<DecodeHintType, ?> hints) throws FormatException {
        String encoding;
        if ((count << 3) <= bits.available()) {
            byte[] readBytes = new byte[count];
            for (int i = 0; i < count; i++) {
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
        } else {
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
        while (count > 1) {
            if (bits.available() >= 11) {
                int nextTwoCharsBits = bits.readBits(11);
                result.append(toAlphaNumericChar(nextTwoCharsBits / 45));
                result.append(toAlphaNumericChar(nextTwoCharsBits % 45));
                count -= 2;
            } else {
                throw FormatException.getFormatInstance();
            }
        }
        if (count == 1) {
            if (bits.available() >= 6) {
                result.append(toAlphaNumericChar(bits.readBits(6)));
            } else {
                throw FormatException.getFormatInstance();
            }
        }
        if (fc1InEffect) {
            for (int i = start; i < result.length(); i++) {
                if (result.charAt(i) == '%') {
                    if (i >= result.length() - 1 || result.charAt(i + 1) != '%') {
                        result.setCharAt(i, 29);
                    } else {
                        result.deleteCharAt(i + 1);
                    }
                }
            }
        }
    }

    private static void decodeNumericSegment(BitSource bits, StringBuilder result, int count) throws FormatException {
        while (count >= 3) {
            if (bits.available() >= 10) {
                int threeDigitsBits = bits.readBits(10);
                if (threeDigitsBits < 1000) {
                    result.append(toAlphaNumericChar(threeDigitsBits / 100));
                    result.append(toAlphaNumericChar((threeDigitsBits / 10) % 10));
                    result.append(toAlphaNumericChar(threeDigitsBits % 10));
                    count -= 3;
                } else {
                    throw FormatException.getFormatInstance();
                }
            } else {
                throw FormatException.getFormatInstance();
            }
        }
        if (count == 2) {
            if (bits.available() >= 7) {
                int twoDigitsBits = bits.readBits(7);
                if (twoDigitsBits < 100) {
                    result.append(toAlphaNumericChar(twoDigitsBits / 10));
                    result.append(toAlphaNumericChar(twoDigitsBits % 10));
                    return;
                }
                throw FormatException.getFormatInstance();
            }
            throw FormatException.getFormatInstance();
        } else if (count != 1) {
        } else {
            if (bits.available() >= 4) {
                int digitBits = bits.readBits(4);
                if (digitBits < 10) {
                    result.append(toAlphaNumericChar(digitBits));
                    return;
                }
                throw FormatException.getFormatInstance();
            }
            throw FormatException.getFormatInstance();
        }
    }

    private static int parseECIValue(BitSource bits) throws FormatException {
        int firstByte = bits.readBits(8);
        if ((firstByte & AppOpsManagerEx.TYPE_MICROPHONE) == 0) {
            return firstByte & AlarmInfoEx.EVERYDAY_CODE;
        }
        if ((firstByte & HwCarrierConfigManager.HD_ICON_MASK_DIALER) == 128) {
            return ((firstByte & 63) << 8) | bits.readBits(8);
        } else if ((firstByte & 224) == 192) {
            return ((firstByte & 31) << 16) | bits.readBits(16);
        } else {
            throw FormatException.getFormatInstance();
        }
    }
}
