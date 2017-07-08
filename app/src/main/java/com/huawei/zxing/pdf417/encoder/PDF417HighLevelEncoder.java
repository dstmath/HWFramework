package com.huawei.zxing.pdf417.encoder;

import com.huawei.internal.telephony.uicc.IccConstantsEx;
import com.huawei.zxing.WriterException;
import java.math.BigInteger;

final class PDF417HighLevelEncoder {
    private static final int BYTE_COMPACTION = 1;
    private static final int LATCH_TO_BYTE = 924;
    private static final int LATCH_TO_BYTE_PADDED = 901;
    private static final int LATCH_TO_NUMERIC = 902;
    private static final int LATCH_TO_TEXT = 900;
    private static final byte[] MIXED = null;
    private static final int NUMERIC_COMPACTION = 2;
    private static final byte[] PUNCTUATION = null;
    private static final int SHIFT_TO_BYTE = 913;
    private static final int SUBMODE_ALPHA = 0;
    private static final int SUBMODE_LOWER = 1;
    private static final int SUBMODE_MIXED = 2;
    private static final int SUBMODE_PUNCTUATION = 3;
    private static final int TEXT_COMPACTION = 0;
    private static final byte[] TEXT_MIXED_RAW = null;
    private static final byte[] TEXT_PUNCTUATION_RAW = null;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.zxing.pdf417.encoder.PDF417HighLevelEncoder.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.huawei.zxing.pdf417.encoder.PDF417HighLevelEncoder.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.zxing.pdf417.encoder.PDF417HighLevelEncoder.<clinit>():void");
    }

    private PDF417HighLevelEncoder() {
    }

    private static byte[] getBytesForMessage(String msg) {
        return msg.getBytes();
    }

    static String encodeHighLevel(String msg, Compaction compaction) throws WriterException {
        byte[] bArr = null;
        StringBuilder sb = new StringBuilder(msg.length());
        int len = msg.length();
        int p = SUBMODE_ALPHA;
        int textSubMode = SUBMODE_ALPHA;
        if (compaction == Compaction.TEXT) {
            encodeText(msg, SUBMODE_ALPHA, len, sb, SUBMODE_ALPHA);
        } else if (compaction == Compaction.BYTE) {
            bArr = getBytesForMessage(msg);
            encodeBinary(bArr, SUBMODE_ALPHA, bArr.length, SUBMODE_LOWER, sb);
        } else if (compaction == Compaction.NUMERIC) {
            sb.append('\u0386');
            encodeNumeric(msg, SUBMODE_ALPHA, len, sb);
        } else {
            int encodingMode = SUBMODE_ALPHA;
            while (p < len) {
                int n = determineConsecutiveDigitCount(msg, p);
                if (n >= 13) {
                    sb.append('\u0386');
                    encodingMode = SUBMODE_MIXED;
                    textSubMode = SUBMODE_ALPHA;
                    encodeNumeric(msg, p, n, sb);
                    p += n;
                } else {
                    int t = determineConsecutiveTextCount(msg, p);
                    if (t >= 5 || n == len) {
                        if (encodingMode != 0) {
                            sb.append('\u0384');
                            encodingMode = SUBMODE_ALPHA;
                            textSubMode = SUBMODE_ALPHA;
                        }
                        textSubMode = encodeText(msg, p, t, sb, textSubMode);
                        p += t;
                    } else {
                        if (bArr == null) {
                            bArr = getBytesForMessage(msg);
                        }
                        int b = determineConsecutiveBinaryCount(msg, bArr, p);
                        if (b == 0) {
                            b = SUBMODE_LOWER;
                        }
                        if (b == SUBMODE_LOWER && encodingMode == 0) {
                            encodeBinary(bArr, p, SUBMODE_LOWER, SUBMODE_ALPHA, sb);
                        } else {
                            encodeBinary(bArr, p, b, encodingMode, sb);
                            encodingMode = SUBMODE_LOWER;
                            textSubMode = SUBMODE_ALPHA;
                        }
                        p += b;
                    }
                }
            }
        }
        return sb.toString();
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static int encodeText(CharSequence msg, int startpos, int count, StringBuilder sb, int initialSubmode) {
        StringBuilder tmp = new StringBuilder(count);
        int submode = initialSubmode;
        int idx = SUBMODE_ALPHA;
        while (true) {
            char ch = msg.charAt(startpos + idx);
            switch (submode) {
                case SUBMODE_ALPHA /*0*/:
                    if (isAlphaUpper(ch)) {
                        if (ch == ' ') {
                            tmp.append('\u001a');
                        } else {
                            tmp.append((char) (ch - 65));
                        }
                    } else if (isAlphaLower(ch)) {
                        submode = SUBMODE_LOWER;
                        tmp.append('\u001b');
                        break;
                    } else if (isMixed(ch)) {
                        submode = SUBMODE_MIXED;
                        tmp.append('\u001c');
                        break;
                    } else {
                        tmp.append('\u001d');
                        tmp.append((char) PUNCTUATION[ch]);
                    }
                case SUBMODE_LOWER /*1*/:
                    if (isAlphaLower(ch)) {
                        if (ch == ' ') {
                            tmp.append('\u001a');
                        } else {
                            tmp.append((char) (ch - 97));
                        }
                    } else if (isAlphaUpper(ch)) {
                        tmp.append('\u001b');
                        tmp.append((char) (ch - 65));
                    } else if (isMixed(ch)) {
                        submode = SUBMODE_MIXED;
                        tmp.append('\u001c');
                        break;
                    } else {
                        tmp.append('\u001d');
                        tmp.append((char) PUNCTUATION[ch]);
                    }
                case SUBMODE_MIXED /*2*/:
                    if (!isMixed(ch)) {
                        if (!isAlphaUpper(ch)) {
                            if (!isAlphaLower(ch)) {
                                if ((startpos + idx) + SUBMODE_LOWER < count && isPunctuation(msg.charAt((startpos + idx) + SUBMODE_LOWER))) {
                                    submode = SUBMODE_PUNCTUATION;
                                    tmp.append('\u0019');
                                    break;
                                }
                                tmp.append('\u001d');
                                tmp.append((char) PUNCTUATION[ch]);
                            } else {
                                submode = SUBMODE_LOWER;
                                tmp.append('\u001b');
                                break;
                            }
                        }
                        submode = SUBMODE_ALPHA;
                        tmp.append('\u001c');
                        break;
                    }
                    tmp.append((char) MIXED[ch]);
                    break;
                default:
                    if (!isPunctuation(ch)) {
                        submode = SUBMODE_ALPHA;
                        tmp.append('\u001d');
                        break;
                    }
                    tmp.append((char) PUNCTUATION[ch]);
                    idx += SUBMODE_LOWER;
                    if (idx < count) {
                        break;
                    }
                    int h = SUBMODE_ALPHA;
                    int len = tmp.length();
                    for (int i = SUBMODE_ALPHA; i < len; i += SUBMODE_LOWER) {
                        if (i % SUBMODE_MIXED != 0) {
                            h = (char) ((h * 30) + tmp.charAt(i));
                            sb.append(h);
                        } else {
                            h = tmp.charAt(i);
                        }
                    }
                    if (len % SUBMODE_MIXED != 0) {
                        sb.append((char) ((h * 30) + 29));
                    }
                    return submode;
            }
        }
    }

    private static void encodeBinary(byte[] bytes, int startpos, int count, int startmode, StringBuilder sb) {
        int i;
        if (count == SUBMODE_LOWER && startmode == 0) {
            sb.append('\u0391');
        } else {
            if (count % 6 == 0) {
                sb.append('\u039c');
            } else {
                sb.append('\u0385');
            }
        }
        int idx = startpos;
        if (count >= 6) {
            char[] chars = new char[5];
            while ((startpos + count) - idx >= 6) {
                long t = 0;
                for (i = SUBMODE_ALPHA; i < 6; i += SUBMODE_LOWER) {
                    t = (t << 8) + ((long) (bytes[idx + i] & IccConstantsEx.RUIM_SMS_BEARER_DATA_LEN));
                }
                for (i = SUBMODE_ALPHA; i < 5; i += SUBMODE_LOWER) {
                    chars[i] = (char) ((int) (t % 900));
                    t /= 900;
                }
                for (i = chars.length - 1; i >= 0; i--) {
                    sb.append(chars[i]);
                }
                idx += 6;
            }
        }
        for (i = idx; i < startpos + count; i += SUBMODE_LOWER) {
            sb.append((char) (bytes[i] & IccConstantsEx.RUIM_SMS_BEARER_DATA_LEN));
        }
    }

    private static void encodeNumeric(String msg, int startpos, int count, StringBuilder sb) {
        int idx = SUBMODE_ALPHA;
        StringBuilder tmp = new StringBuilder((count / SUBMODE_PUNCTUATION) + SUBMODE_LOWER);
        BigInteger num900 = BigInteger.valueOf(900);
        BigInteger num0 = BigInteger.valueOf(0);
        while (idx < count - 1) {
            tmp.setLength(SUBMODE_ALPHA);
            int len = Math.min(44, count - idx);
            BigInteger bigint = new BigInteger('1' + msg.substring(startpos + idx, (startpos + idx) + len));
            do {
                tmp.append((char) bigint.mod(num900).intValue());
                bigint = bigint.divide(num900);
            } while (!bigint.equals(num0));
            for (int i = tmp.length() - 1; i >= 0; i--) {
                sb.append(tmp.charAt(i));
            }
            idx += len;
        }
    }

    private static boolean isDigit(char ch) {
        return ch >= '0' && ch <= '9';
    }

    private static boolean isAlphaUpper(char ch) {
        if (ch != ' ') {
            return ch >= 'A' && ch <= 'Z';
        } else {
            return true;
        }
    }

    private static boolean isAlphaLower(char ch) {
        if (ch != ' ') {
            return ch >= 'a' && ch <= 'z';
        } else {
            return true;
        }
    }

    private static boolean isMixed(char ch) {
        return MIXED[ch] != -1;
    }

    private static boolean isPunctuation(char ch) {
        return PUNCTUATION[ch] != -1;
    }

    private static boolean isText(char ch) {
        if (ch == '\t' || ch == '\n' || ch == '\r') {
            return true;
        }
        return ch >= ' ' && ch <= '~';
    }

    private static int determineConsecutiveDigitCount(CharSequence msg, int startpos) {
        int count = SUBMODE_ALPHA;
        int len = msg.length();
        int idx = startpos;
        if (startpos < len) {
            char ch = msg.charAt(startpos);
            while (isDigit(ch) && idx < len) {
                count += SUBMODE_LOWER;
                idx += SUBMODE_LOWER;
                if (idx < len) {
                    ch = msg.charAt(idx);
                }
            }
        }
        return count;
    }

    private static int determineConsecutiveTextCount(CharSequence msg, int startpos) {
        int len = msg.length();
        int idx = startpos;
        while (idx < len) {
            char ch = msg.charAt(idx);
            int numericCount = SUBMODE_ALPHA;
            while (numericCount < 13 && isDigit(ch) && idx < len) {
                numericCount += SUBMODE_LOWER;
                idx += SUBMODE_LOWER;
                if (idx < len) {
                    ch = msg.charAt(idx);
                }
            }
            if (numericCount >= 13) {
                return (idx - startpos) - numericCount;
            }
            if (numericCount <= 0) {
                if (!isText(msg.charAt(idx))) {
                    break;
                }
                idx += SUBMODE_LOWER;
            }
        }
        return idx - startpos;
    }

    private static int determineConsecutiveBinaryCount(CharSequence msg, byte[] bytes, int startpos) throws WriterException {
        int len = msg.length();
        int idx = startpos;
        while (idx < len) {
            char ch = msg.charAt(idx);
            int numericCount = SUBMODE_ALPHA;
            while (numericCount < 13 && isDigit(ch)) {
                numericCount += SUBMODE_LOWER;
                int i = idx + numericCount;
                if (i >= len) {
                    break;
                }
                ch = msg.charAt(i);
            }
            if (numericCount >= 13) {
                return idx - startpos;
            }
            int textCount = SUBMODE_ALPHA;
            while (textCount < 5 && isText(ch)) {
                textCount += SUBMODE_LOWER;
                i = idx + textCount;
                if (i >= len) {
                    break;
                }
                ch = msg.charAt(i);
            }
            if (textCount >= 5) {
                return idx - startpos;
            }
            ch = msg.charAt(idx);
            if (bytes[idx] != (byte) 63 || ch == '?') {
                idx += SUBMODE_LOWER;
            } else {
                throw new WriterException("Non-encodable character detected: " + ch + " (Unicode: " + ch + ')');
            }
        }
        return idx - startpos;
    }
}
