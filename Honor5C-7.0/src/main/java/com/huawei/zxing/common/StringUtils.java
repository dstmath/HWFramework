package com.huawei.zxing.common;

import com.huawei.android.app.AppOpsManagerEx;
import com.huawei.internal.telephony.uicc.IccConstantsEx;
import com.huawei.lcagent.client.MetricConstant;
import com.huawei.zxing.DecodeHintType;
import java.util.Map;

public final class StringUtils {
    private static final boolean ASSUME_SHIFT_JIS = false;
    private static final String EUC_JP = "EUC_JP";
    public static final String GB2312 = "GB2312";
    private static final String ISO88591 = "ISO8859_1";
    private static final String PLATFORM_DEFAULT_ENCODING = null;
    public static final String SHIFT_JIS = "SJIS";
    private static final String UTF8 = "UTF8";

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.zxing.common.StringUtils.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.huawei.zxing.common.StringUtils.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.zxing.common.StringUtils.<clinit>():void");
    }

    private StringUtils() {
    }

    private static final boolean canBeGB2312(byte[] rawtext) {
        int rawtextlen = rawtext.length;
        int gbchars = 1;
        int dbchars = 1;
        int i = 0;
        while (i < rawtextlen - 1) {
            if (rawtext[i] < null) {
                dbchars++;
                int value = rawtext[i] + MetricConstant.METRIC_ID_MAX;
                int next_value = rawtext[i + 1] + MetricConstant.METRIC_ID_MAX;
                if (value >= 161 && value <= 247 && next_value >= 161 && next_value <= 254) {
                    gbchars++;
                }
                i++;
            }
            i++;
        }
        return (gbchars <= 1 || gbchars * 100 <= dbchars * 80) ? ASSUME_SHIFT_JIS : true;
    }

    public static String guessEncoding(byte[] bytes, Map<DecodeHintType, ?> hints) {
        if (hints != null) {
            String characterSet = (String) hints.get(DecodeHintType.CHARACTER_SET);
            if (characterSet != null) {
                return characterSet;
            }
        }
        int length = bytes.length;
        boolean canBeISO88591 = true;
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
        int length2 = bytes.length;
        boolean utf8bom = (r0 > 3 && bytes[0] == -17 && bytes[1] == -69) ? bytes[2] == -65 ? true : ASSUME_SHIFT_JIS : ASSUME_SHIFT_JIS;
        for (int i = 0; i < length && (canBeISO88591 || canBeShiftJIS || canBeUTF8); i++) {
            int value = bytes[i] & IccConstantsEx.RUIM_SMS_BEARER_DATA_LEN;
            if (canBeUTF8) {
                if (utf8BytesLeft > 0) {
                    if ((value & AppOpsManagerEx.TYPE_MICROPHONE) == 0) {
                        canBeUTF8 = ASSUME_SHIFT_JIS;
                    } else {
                        utf8BytesLeft--;
                    }
                } else if ((value & AppOpsManagerEx.TYPE_MICROPHONE) != 0) {
                    if ((value & 64) == 0) {
                        canBeUTF8 = ASSUME_SHIFT_JIS;
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
                                    canBeUTF8 = ASSUME_SHIFT_JIS;
                                }
                            }
                        }
                    }
                }
            }
            if (canBeISO88591) {
                if (value > 127 && value < 160) {
                    canBeISO88591 = ASSUME_SHIFT_JIS;
                } else if (value > 159) {
                    if (value >= 192 && value != 215) {
                        if (value == 247) {
                        }
                    }
                    isoHighOther++;
                }
            }
            if (canBeShiftJIS) {
                if (sjisBytesLeft > 0) {
                    if (value < 64 || value == 127 || value > 252) {
                        canBeShiftJIS = ASSUME_SHIFT_JIS;
                    } else {
                        sjisBytesLeft--;
                    }
                } else if (value == 128 || value == 160 || value > 239) {
                    canBeShiftJIS = ASSUME_SHIFT_JIS;
                } else if (value > 160 && value < 224) {
                    sjisKatakanaChars++;
                    sjisCurDoubleBytesWordLength = 0;
                    sjisCurKatakanaWordLength++;
                    if (sjisCurKatakanaWordLength > sjisMaxKatakanaWordLength) {
                        sjisMaxKatakanaWordLength = sjisCurKatakanaWordLength;
                    }
                } else if (value > 127) {
                    sjisBytesLeft++;
                    sjisCurKatakanaWordLength = 0;
                    sjisCurDoubleBytesWordLength++;
                    if (sjisCurDoubleBytesWordLength > sjisMaxDoubleBytesWordLength) {
                        sjisMaxDoubleBytesWordLength = sjisCurDoubleBytesWordLength;
                    }
                } else {
                    sjisCurKatakanaWordLength = 0;
                    sjisCurDoubleBytesWordLength = 0;
                }
            }
        }
        if (canBeUTF8 && utf8BytesLeft > 0) {
            canBeUTF8 = ASSUME_SHIFT_JIS;
        }
        if (canBeShiftJIS && sjisBytesLeft > 0) {
            canBeShiftJIS = ASSUME_SHIFT_JIS;
        }
        if (canBeUTF8 && (utf8bom || (utf2BytesChars + utf3BytesChars) + utf4BytesChars > 0)) {
            return UTF8;
        }
        if (canBeGB2312(bytes)) {
            return GB2312;
        }
        if (canBeShiftJIS && (ASSUME_SHIFT_JIS || sjisMaxKatakanaWordLength >= 3 || sjisMaxDoubleBytesWordLength >= 3)) {
            return SHIFT_JIS;
        }
        if (canBeISO88591 && canBeShiftJIS) {
            String str = (!(sjisMaxKatakanaWordLength == 2 && sjisKatakanaChars == 2) && isoHighOther * 10 < length) ? ISO88591 : SHIFT_JIS;
            return str;
        } else if (canBeUTF8) {
            return UTF8;
        } else {
            if (canBeISO88591) {
                return ISO88591;
            }
            if (canBeShiftJIS) {
                return SHIFT_JIS;
            }
            return PLATFORM_DEFAULT_ENCODING;
        }
    }
}
