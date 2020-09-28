package com.huawei.zxing.common;

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
        int gbchars = 1;
        int dbchars = 1;
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
        if (gbchars <= 1 || gbchars * 100 <= dbchars * 80) {
            return false;
        }
        return true;
    }

    public static String guessEncoding(byte[] bytes, Map<DecodeHintType, ?> hints) {
        int length;
        String characterSet;
        byte[] bArr = bytes;
        if (hints != null && (characterSet = (String) hints.get(DecodeHintType.CHARACTER_SET)) != null) {
            return characterSet;
        }
        int length2 = bArr.length;
        boolean canBeShiftJIS = true;
        boolean canBeUTF8 = true;
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
        boolean utf8bom = false;
        boolean canBeISO88591 = true;
        if (bArr.length > 3 && bArr[0] == -17 && bArr[1] == -69 && bArr[2] == -65) {
            utf8bom = true;
        }
        int i = 0;
        while (true) {
            if (i < length2) {
                if (!canBeISO88591 && !canBeShiftJIS && !canBeUTF8) {
                    length = length2;
                    break;
                }
                int value = bArr[i] & 255;
                if (canBeUTF8) {
                    if (utf8BytesLeft > 0) {
                        if ((value & 128) == 0) {
                            canBeUTF8 = false;
                        } else {
                            utf8BytesLeft--;
                        }
                    } else if ((value & 128) != 0) {
                        if ((value & 64) == 0) {
                            canBeUTF8 = false;
                        } else {
                            utf8BytesLeft++;
                            if ((value & 32) == 0) {
                                utf2BytesChars++;
                            } else {
                                utf8BytesLeft++;
                                if ((value & 16) == 0) {
                                    utf3BytesChars++;
                                } else {
                                    utf8BytesLeft++;
                                    if ((value & 8) == 0) {
                                        utf4BytesChars++;
                                    } else {
                                        canBeUTF8 = false;
                                    }
                                }
                            }
                        }
                    }
                }
                if (canBeISO88591) {
                    if (value > 127 && value < 160) {
                        canBeISO88591 = false;
                    } else if (value > 159 && (value < 192 || value == 215 || value == 247)) {
                        isoHighOther++;
                    }
                }
                if (canBeShiftJIS) {
                    if (sjisBytesLeft > 0) {
                        if (value < 64 || value == 127 || value > 252) {
                            canBeShiftJIS = false;
                        } else {
                            sjisBytesLeft--;
                        }
                    } else if (value == 128 || value == 160 || value > 239) {
                        canBeShiftJIS = false;
                    } else if (value > 160 && value < 224) {
                        sjisKatakanaChars++;
                        sjisCurKatakanaWordLength++;
                        if (sjisCurKatakanaWordLength > sjisMaxKatakanaWordLength) {
                            sjisMaxKatakanaWordLength = sjisCurKatakanaWordLength;
                            sjisCurDoubleBytesWordLength = 0;
                        } else {
                            sjisCurDoubleBytesWordLength = 0;
                        }
                    } else if (value > 127) {
                        sjisBytesLeft++;
                        sjisCurDoubleBytesWordLength++;
                        if (sjisCurDoubleBytesWordLength > sjisMaxDoubleBytesWordLength) {
                            sjisMaxDoubleBytesWordLength = sjisCurDoubleBytesWordLength;
                            sjisCurKatakanaWordLength = 0;
                        } else {
                            sjisCurKatakanaWordLength = 0;
                        }
                    } else {
                        sjisCurDoubleBytesWordLength = 0;
                        sjisCurKatakanaWordLength = 0;
                    }
                }
                i++;
                bArr = bytes;
                length2 = length2;
            } else {
                length = length2;
                break;
            }
        }
        if (canBeUTF8 && utf8BytesLeft > 0) {
            canBeUTF8 = false;
        }
        if (canBeShiftJIS && sjisBytesLeft > 0) {
            canBeShiftJIS = false;
        }
        if (canBeUTF8 && (utf8bom || utf2BytesChars + utf3BytesChars + utf4BytesChars > 0)) {
            return UTF8;
        }
        if (canBeGB2312(bytes)) {
            return GB2312;
        }
        if (canBeShiftJIS && (ASSUME_SHIFT_JIS || sjisMaxKatakanaWordLength >= 3 || sjisMaxDoubleBytesWordLength >= 3)) {
            return SHIFT_JIS;
        }
        if (!canBeISO88591 || !canBeShiftJIS) {
            if (canBeUTF8) {
                return UTF8;
            }
            if (canBeISO88591) {
                return ISO88591;
            }
            if (canBeShiftJIS) {
                return SHIFT_JIS;
            }
            return PLATFORM_DEFAULT_ENCODING;
        } else if (sjisMaxKatakanaWordLength == 2 && sjisKatakanaChars == 2) {
            return SHIFT_JIS;
        } else {
            if (isoHighOther * 10 >= length) {
                return SHIFT_JIS;
            }
            return ISO88591;
        }
    }
}
