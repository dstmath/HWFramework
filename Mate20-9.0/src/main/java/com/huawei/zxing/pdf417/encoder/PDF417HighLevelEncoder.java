package com.huawei.zxing.pdf417.encoder;

import com.huawei.android.app.AppOpsManagerEx;
import com.huawei.zxing.WriterException;
import java.math.BigInteger;
import java.util.Arrays;

final class PDF417HighLevelEncoder {
    private static final int BYTE_COMPACTION = 1;
    private static final int LATCH_TO_BYTE = 924;
    private static final int LATCH_TO_BYTE_PADDED = 901;
    private static final int LATCH_TO_NUMERIC = 902;
    private static final int LATCH_TO_TEXT = 900;
    private static final byte[] MIXED = new byte[AppOpsManagerEx.TYPE_MICROPHONE];
    private static final int NUMERIC_COMPACTION = 2;
    private static final byte[] PUNCTUATION = new byte[AppOpsManagerEx.TYPE_MICROPHONE];
    private static final int SHIFT_TO_BYTE = 913;
    private static final int SUBMODE_ALPHA = 0;
    private static final int SUBMODE_LOWER = 1;
    private static final int SUBMODE_MIXED = 2;
    private static final int SUBMODE_PUNCTUATION = 3;
    private static final int TEXT_COMPACTION = 0;
    private static final byte[] TEXT_MIXED_RAW = {48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 38, 13, 9, 44, 58, 35, 45, 46, 36, 47, 43, 37, 42, 61, 94, 0, 32, 0, 0, 0};
    private static final byte[] TEXT_PUNCTUATION_RAW = {59, 60, 62, 64, 91, 92, 93, 95, 96, 126, 33, 13, 9, 44, 58, 10, 45, 46, 36, 47, 34, 124, 42, 40, 41, 63, 123, 125, 39, 0};

    static {
        Arrays.fill(MIXED, (byte) -1);
        for (byte i = 0; i < TEXT_MIXED_RAW.length; i = (byte) (i + 1)) {
            byte b = TEXT_MIXED_RAW[i];
            if (b > 0) {
                MIXED[b] = i;
            }
        }
        Arrays.fill(PUNCTUATION, (byte) -1);
        for (byte i2 = 0; i2 < TEXT_PUNCTUATION_RAW.length; i2 = (byte) (i2 + 1)) {
            byte b2 = TEXT_PUNCTUATION_RAW[i2];
            if (b2 > 0) {
                PUNCTUATION[b2] = i2;
            }
        }
    }

    private PDF417HighLevelEncoder() {
    }

    private static byte[] getBytesForMessage(String msg) {
        return msg.getBytes();
    }

    static String encodeHighLevel(String msg, Compaction compaction) throws WriterException {
        StringBuilder sb = new StringBuilder(msg.length());
        int len = msg.length();
        int p = 0;
        int textSubMode = 0;
        if (compaction == Compaction.TEXT) {
            encodeText(msg, 0, len, sb, 0);
        } else if (compaction == Compaction.BYTE) {
            byte[] bytes = getBytesForMessage(msg);
            encodeBinary(bytes, 0, bytes.length, 1, sb);
        } else if (compaction == Compaction.NUMERIC) {
            sb.append(902);
            encodeNumeric(msg, 0, len, sb);
        } else {
            byte[] bytes2 = null;
            int encodingMode = 0;
            while (p < len) {
                int n = determineConsecutiveDigitCount(msg, p);
                if (n >= 13) {
                    sb.append(902);
                    encodingMode = 2;
                    textSubMode = 0;
                    encodeNumeric(msg, p, n, sb);
                    p += n;
                } else {
                    int t = determineConsecutiveTextCount(msg, p);
                    if (t >= 5 || n == len) {
                        if (encodingMode != 0) {
                            sb.append(900);
                            encodingMode = 0;
                            textSubMode = 0;
                        }
                        textSubMode = encodeText(msg, p, t, sb, textSubMode);
                        p += t;
                    } else {
                        if (bytes2 == null) {
                            bytes2 = getBytesForMessage(msg);
                        }
                        int b = determineConsecutiveBinaryCount(msg, bytes2, p);
                        if (b == 0) {
                            b = 1;
                        }
                        if (b == 1 && encodingMode == 0) {
                            encodeBinary(bytes2, p, 1, 0, sb);
                        } else {
                            encodeBinary(bytes2, p, b, encodingMode, sb);
                            encodingMode = 1;
                            textSubMode = 0;
                        }
                        p += b;
                    }
                }
            }
            byte[] bArr = bytes2;
        }
        return sb.toString();
    }

    /* JADX WARNING: Can't fix incorrect switch cases order */
    private static int encodeText(CharSequence msg, int startpos, int count, StringBuilder sb, int initialSubmode) {
        CharSequence charSequence = msg;
        int i = count;
        StringBuilder sb2 = sb;
        StringBuilder tmp = new StringBuilder(i);
        int submode = initialSubmode;
        int idx = 0;
        while (true) {
            char ch = charSequence.charAt(startpos + idx);
            switch (submode) {
                case 0:
                    if (isAlphaUpper(ch)) {
                        if (ch == ' ') {
                            tmp.append(26);
                        } else {
                            tmp.append((char) (ch - 'A'));
                        }
                    } else if (isAlphaLower(ch)) {
                        submode = 1;
                        tmp.append(27);
                        break;
                    } else if (isMixed(ch)) {
                        submode = 2;
                        tmp.append(28);
                        break;
                    } else {
                        tmp.append(29);
                        tmp.append((char) PUNCTUATION[ch]);
                    }
                case 1:
                    if (isAlphaLower(ch)) {
                        if (ch == ' ') {
                            tmp.append(26);
                        } else {
                            tmp.append((char) (ch - 'a'));
                        }
                    } else if (isAlphaUpper(ch)) {
                        tmp.append(27);
                        tmp.append((char) (ch - 'A'));
                    } else if (isMixed(ch)) {
                        submode = 2;
                        tmp.append(28);
                        break;
                    } else {
                        tmp.append(29);
                        tmp.append((char) PUNCTUATION[ch]);
                    }
                case 2:
                    if (!isMixed(ch)) {
                        if (!isAlphaUpper(ch)) {
                            if (!isAlphaLower(ch)) {
                                if (startpos + idx + 1 < i && isPunctuation(charSequence.charAt(startpos + idx + 1))) {
                                    submode = 3;
                                    tmp.append(25);
                                    break;
                                } else {
                                    tmp.append(29);
                                    tmp.append((char) PUNCTUATION[ch]);
                                }
                            } else {
                                submode = 1;
                                tmp.append(27);
                                break;
                            }
                        } else {
                            submode = 0;
                            tmp.append(28);
                            break;
                        }
                    } else {
                        tmp.append((char) MIXED[ch]);
                    }
                    break;
                default:
                    if (!isPunctuation(ch)) {
                        submode = 0;
                        tmp.append(29);
                        break;
                    } else {
                        tmp.append((char) PUNCTUATION[ch]);
                        idx++;
                        if (idx < i) {
                            break;
                        } else {
                            int len = tmp.length();
                            char h = 0;
                            for (int i2 = 0; i2 < len; i2++) {
                                if (i2 % 2 != 0) {
                                    h = (char) ((h * 30) + tmp.charAt(i2));
                                    sb2.append(h);
                                } else {
                                    h = tmp.charAt(i2);
                                }
                            }
                            if (len % 2 != 0) {
                                sb2.append((char) ((h * 30) + 29));
                            }
                            return submode;
                        }
                    }
            }
        }
    }

    private static void encodeBinary(byte[] bytes, int startpos, int count, int startmode, StringBuilder sb) {
        int i = count;
        StringBuilder sb2 = sb;
        if (i == 1 && startmode == 0) {
            sb2.append(913);
        } else {
            if (i % 6 == 0) {
                sb2.append(924);
            } else {
                sb2.append(901);
            }
        }
        int idx = startpos;
        int i2 = 6;
        if (i >= 6) {
            int i3 = 5;
            char[] chars = new char[5];
            while ((startpos + i) - idx >= i2) {
                long t = 0;
                for (int i4 = 0; i4 < i2; i4++) {
                    t = (t << 8) + ((long) (bytes[idx + i4] & 255));
                }
                int i5 = 0;
                while (i5 < i3) {
                    chars[i5] = (char) ((int) (t % 900));
                    t /= 900;
                    i5++;
                    i3 = 5;
                }
                for (int i6 = chars.length - 1; i6 >= 0; i6--) {
                    sb2.append(chars[i6]);
                }
                idx += 6;
                i2 = 6;
                i3 = 5;
            }
        }
        for (int i7 = idx; i7 < startpos + i; i7++) {
            sb2.append((char) (bytes[i7] & 255));
        }
    }

    private static void encodeNumeric(String msg, int startpos, int count, StringBuilder sb) {
        int len;
        int idx = 0;
        StringBuilder tmp = new StringBuilder((count / 3) + 1);
        BigInteger num900 = BigInteger.valueOf(900);
        BigInteger num0 = BigInteger.valueOf(0);
        while (idx < count - 1) {
            tmp.setLength(0);
            BigInteger bigint = new BigInteger('1' + msg.substring(startpos + idx, startpos + idx + Math.min(44, count - idx)));
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
        return ch == ' ' || (ch >= 'A' && ch <= 'Z');
    }

    private static boolean isAlphaLower(char ch) {
        return ch == ' ' || (ch >= 'a' && ch <= 'z');
    }

    private static boolean isMixed(char ch) {
        return MIXED[ch] != -1;
    }

    private static boolean isPunctuation(char ch) {
        return PUNCTUATION[ch] != -1;
    }

    private static boolean isText(char ch) {
        return ch == 9 || ch == 10 || ch == 13 || (ch >= ' ' && ch <= '~');
    }

    private static int determineConsecutiveDigitCount(CharSequence msg, int startpos) {
        int count = 0;
        int len = msg.length();
        int idx = startpos;
        if (idx < len) {
            char ch = msg.charAt(idx);
            while (isDigit(ch) && idx < len) {
                count++;
                idx++;
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
            int numericCount = 0;
            while (numericCount < 13 && isDigit(ch) && idx < len) {
                numericCount++;
                idx++;
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
                idx++;
            }
        }
        return idx - startpos;
    }

    private static int determineConsecutiveBinaryCount(CharSequence msg, byte[] bytes, int startpos) throws WriterException {
        int len = msg.length();
        int idx = startpos;
        while (idx < len) {
            int textCount = 0;
            char ch = msg.charAt(idx);
            int numericCount = 0;
            while (numericCount < 13 && isDigit(ch)) {
                numericCount++;
                int i = idx + numericCount;
                if (i >= len) {
                    break;
                }
                ch = msg.charAt(i);
            }
            if (numericCount >= 13) {
                return idx - startpos;
            }
            while (textCount < 5 && isText(ch)) {
                textCount++;
                int i2 = idx + textCount;
                if (i2 >= len) {
                    break;
                }
                ch = msg.charAt(i2);
            }
            if (textCount >= 5) {
                return idx - startpos;
            }
            char ch2 = msg.charAt(idx);
            if (bytes[idx] != 63 || ch2 == '?') {
                idx++;
            } else {
                throw new WriterException("Non-encodable character detected: " + ch2 + " (Unicode: " + ch2 + ')');
            }
        }
        return idx - startpos;
    }
}
