package com.huawei.zxing.common;

import com.huawei.android.app.AppOpsManagerEx;
import com.huawei.zxing.DecodeHintType;
import java.util.Map;

public final class StringUtils {
    private static final boolean ASSUME_SHIFT_JIS = (SHIFT_JIS.equalsIgnoreCase(PLATFORM_DEFAULT_ENCODING) || EUC_JP.equalsIgnoreCase(PLATFORM_DEFAULT_ENCODING));
    private static final String EUC_JP = "EUC_JP";
    public static final String GB2312 = "GB2312";
    private static final String ISO88591 = "ISO8859_1";
    private static final String PLATFORM_DEFAULT_ENCODING = System.getProperty("file.encoding");
    public static final String SHIFT_JIS = "SJIS";
    private static final String UTF8 = "UTF8";

    private StringUtils() {
    }

    private static final boolean canBeGB2312(byte[] rawtext) {
        int rawtextlen = rawtext.length;
        int dbchars = 1;
        int gbchars = 1;
        int i = 0;
        while (i < rawtextlen - 1) {
            if (rawtext[i] < 0) {
                dbchars++;
                int value = rawtext[i] + 256;
                int next_value = rawtext[i + 1] + 256;
                if (value >= 161 && value <= 247 && next_value >= 161 && next_value <= 254) {
                    gbchars++;
                }
                i++;
            }
            i++;
        }
        return gbchars > 1 && gbchars * 100 > dbchars * 80;
    }

    public static String guessEncoding(byte[] bytes, Map<DecodeHintType, ?> hints) {
        int length;
        String str;
        boolean canBeShiftJIS;
        boolean canBeUTF8;
        byte[] bArr = bytes;
        Map<DecodeHintType, ?> map = hints;
        if (map != null) {
            String characterSet = (String) map.get(DecodeHintType.CHARACTER_SET);
            if (characterSet != null) {
                return characterSet;
            }
        }
        int length2 = bArr.length;
        boolean canBeShiftJIS2 = true;
        boolean canBeUTF82 = true;
        int utf8BytesLeft = 0;
        int utf2BytesChars = 0;
        int utf3BytesChars = 0;
        int utf4BytesChars = 0;
        int sjisBytesLeft = 0;
        int sjisKatakanaChars = 0;
        int sjisCurKatakanaWordLength = 0;
        int sjisCurDoubleBytesWordLength = 0;
        int sjisMaxKatakanaWordLength = 0;
        int sjisMaxDoubleBytesWordLength = 0;
        int isoHighOther = 0;
        boolean canBeISO88591 = true;
        int i = 0;
        boolean z = true;
        if (!(bArr.length > 3 && bArr[0] == -17 && bArr[1] == -69 && bArr[2] == -65)) {
            z = false;
        }
        boolean utf8bom = z;
        while (true) {
            int i2 = i;
            if (i2 < length2) {
                if (!canBeISO88591 && !canBeShiftJIS2 && !canBeUTF82) {
                    length = length2;
                    break;
                }
                int length3 = length2;
                int length4 = bArr[i2] & 255;
                if (canBeUTF82) {
                    if (utf8BytesLeft > 0) {
                        if ((length4 & AppOpsManagerEx.TYPE_MICROPHONE) == 0) {
                            canBeUTF8 = false;
                        } else {
                            utf8BytesLeft--;
                        }
                    } else if ((length4 & AppOpsManagerEx.TYPE_MICROPHONE) != 0) {
                        if ((length4 & 64) == 0) {
                            canBeUTF8 = false;
                        } else {
                            utf8BytesLeft++;
                            if ((length4 & 32) == 0) {
                                utf2BytesChars++;
                            } else {
                                utf8BytesLeft++;
                                if ((length4 & 16) == 0) {
                                    utf3BytesChars++;
                                } else {
                                    utf8BytesLeft++;
                                    if ((length4 & 8) == 0) {
                                        utf4BytesChars++;
                                    } else {
                                        canBeUTF8 = false;
                                    }
                                }
                            }
                        }
                    }
                    canBeUTF82 = canBeUTF8;
                }
                if (canBeISO88591) {
                    if (length4 > 127 && length4 < 160) {
                        canBeISO88591 = false;
                    } else if (length4 > 159 && (length4 < 192 || length4 == 215 || length4 == 247)) {
                        isoHighOther++;
                    }
                }
                if (canBeShiftJIS2) {
                    if (sjisBytesLeft > 0) {
                        if (length4 < 64 || length4 == 127 || length4 > 252) {
                            canBeShiftJIS = false;
                        } else {
                            sjisBytesLeft--;
                        }
                    } else if (length4 == 128 || length4 == 160 || length4 > 239) {
                        canBeShiftJIS = false;
                    } else if (length4 > 160 && length4 < 224) {
                        sjisKatakanaChars++;
                        sjisCurKatakanaWordLength++;
                        if (sjisCurKatakanaWordLength > sjisMaxKatakanaWordLength) {
                            sjisMaxKatakanaWordLength = sjisCurKatakanaWordLength;
                        }
                        sjisCurDoubleBytesWordLength = 0;
                    } else if (length4 > 127) {
                        sjisBytesLeft++;
                        sjisCurDoubleBytesWordLength++;
                        if (sjisCurDoubleBytesWordLength > sjisMaxDoubleBytesWordLength) {
                            sjisMaxDoubleBytesWordLength = sjisCurDoubleBytesWordLength;
                        }
                        sjisCurKatakanaWordLength = 0;
                    } else {
                        sjisCurKatakanaWordLength = 0;
                        sjisCurDoubleBytesWordLength = 0;
                    }
                    canBeShiftJIS2 = canBeShiftJIS;
                }
                i = i2 + 1;
                length2 = length3;
                bArr = bytes;
            } else {
                length = length2;
                break;
            }
        }
        if (canBeUTF82 && utf8BytesLeft > 0) {
            canBeUTF82 = false;
        }
        if (canBeShiftJIS2 && sjisBytesLeft > 0) {
            canBeShiftJIS2 = false;
        }
        if (canBeUTF82 && (utf8bom || utf2BytesChars + utf3BytesChars + utf4BytesChars > 0)) {
            return UTF8;
        }
        if (canBeGB2312(bytes)) {
            return GB2312;
        }
        if (canBeShiftJIS2 && (ASSUME_SHIFT_JIS || sjisMaxKatakanaWordLength >= 3 || sjisMaxDoubleBytesWordLength >= 3)) {
            return SHIFT_JIS;
        }
        if (!canBeISO88591 || !canBeShiftJIS2) {
            if (canBeUTF82) {
                return UTF8;
            }
            if (canBeISO88591) {
                return ISO88591;
            }
            if (canBeShiftJIS2) {
                return SHIFT_JIS;
            }
            return PLATFORM_DEFAULT_ENCODING;
        }
        if (sjisMaxKatakanaWordLength == 2 && sjisKatakanaChars == 2) {
            int i3 = length;
        } else if (isoHighOther * 10 < length) {
            str = ISO88591;
            return str;
        }
        str = SHIFT_JIS;
        return str;
    }
}
