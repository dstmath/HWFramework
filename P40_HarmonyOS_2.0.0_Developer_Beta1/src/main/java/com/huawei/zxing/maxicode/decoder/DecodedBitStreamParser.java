package com.huawei.zxing.maxicode.decoder;

import com.huawei.zxing.common.DecoderResult;
import java.text.DecimalFormat;
import java.text.NumberFormat;

/* access modifiers changed from: package-private */
public final class DecodedBitStreamParser {
    private static final char ECI = 65530;
    private static final char FS = 28;
    private static final char GS = 29;
    private static final char LATCHA = 65527;
    private static final char LATCHB = 65528;
    private static final char LOCK = 65529;
    private static final NumberFormat NINE_DIGITS = new DecimalFormat("000000000");
    private static final char NS = 65531;
    private static final char PAD = 65532;
    private static final char RS = 30;
    private static final String[] SETS = {"\nABCDEFGHIJKLMNOPQRSTUVWXYZ￺\u001c\u001d\u001e￻ ￼\"#$%&'()*+,-./0123456789:￱￲￳￴￸", "`abcdefghijklmnopqrstuvwxyz￺\u001c\u001d\u001e￻{￼}~;<=>?[\\]^_ ,./:@!|￼￵￶￼￰￲￳￴￷", "ÀÁÂÃÄÅÆÇÈÉÊËÌÍÎÏÐÑÒÓÔÕÖ×ØÙÚ￺\u001c\u001d\u001eÛÜÝÞßª¬±²³µ¹º¼½¾￷ ￹￳￴￸", "àáâãäåæçèéêëìíîïðñòóôõö÷øùú￺\u001c\u001d\u001e￻ûüýþÿ¡¨«¯°´·¸»¿￷ ￲￹￴￸", "\u0000\u0001\u0002\u0003\u0004\u0005\u0006\u0007\b\t\n\u000b\f\r\u000e\u000f\u0010\u0011\u0012\u0013\u0014\u0015\u0016\u0017\u0018\u0019\u001a￺￼￼\u001b￻\u001c\u001d\u001e\u001f ¢£¤¥¦§©­®¶￷ ￲￳￹￸", "\u0000\u0001\u0002\u0003\u0004\u0005\u0006\u0007\b\t\n\u000b\f\r\u000e\u000f\u0010\u0011\u0012\u0013\u0014\u0015\u0016\u0017\u0018\u0019\u001a\u001b\u001c\u001d\u001e\u001f !\"#$%&'()*+,-./0123456789:;<=>?"};
    private static final char SHIFTA = 65520;
    private static final char SHIFTB = 65521;
    private static final char SHIFTC = 65522;
    private static final char SHIFTD = 65523;
    private static final char SHIFTE = 65524;
    private static final char THREESHIFTA = 65526;
    private static final NumberFormat THREE_DIGITS = new DecimalFormat("000");
    private static final char TWOSHIFTA = 65525;

    private DecodedBitStreamParser() {
    }

    static DecoderResult decode(byte[] bytes, int mode) {
        String postcode;
        StringBuilder result = new StringBuilder(144);
        if (mode == 2 || mode == 3) {
            if (mode == 2) {
                postcode = new DecimalFormat("0000000000".substring(0, getPostCode2Length(bytes))).format((long) getPostCode2(bytes));
            } else {
                postcode = getPostCode3(bytes);
            }
            String country = THREE_DIGITS.format((long) getCountry(bytes));
            String service = THREE_DIGITS.format((long) getServiceClass(bytes));
            result.append(getMessage(bytes, 10, 84));
            if (result.toString().startsWith("[)>\u001e01\u001d")) {
                result.insert(9, postcode + GS + country + GS + service + GS);
            } else {
                result.insert(0, postcode + GS + country + GS + service + GS);
            }
        } else if (mode == 4) {
            result.append(getMessage(bytes, 1, 93));
        } else if (mode == 5) {
            result.append(getMessage(bytes, 1, 77));
        }
        return new DecoderResult(bytes, result.toString(), null, String.valueOf(mode));
    }

    private static int getBit(int bit, byte[] bytes) {
        int bit2 = bit - 1;
        return (bytes[bit2 / 6] & (1 << (5 - (bit2 % 6)))) == 0 ? 0 : 1;
    }

    private static int getInt(byte[] bytes, byte[] x) {
        int val = 0;
        for (int i = 0; i < x.length; i++) {
            val += getBit(x[i], bytes) << ((x.length - i) - 1);
        }
        return val;
    }

    private static int getCountry(byte[] bytes) {
        return getInt(bytes, new byte[]{53, 54, 43, 44, 45, 46, 47, 48, 37, 38});
    }

    private static int getServiceClass(byte[] bytes) {
        return getInt(bytes, new byte[]{55, 56, 57, 58, 59, 60, 49, 50, 51, 52});
    }

    private static int getPostCode2Length(byte[] bytes) {
        return getInt(bytes, new byte[]{39, 40, 41, 42, 31, 32});
    }

    private static int getPostCode2(byte[] bytes) {
        return getInt(bytes, new byte[]{33, 34, 35, 36, 25, 26, 27, 28, 29, 30, 19, 20, 21, 22, 23, 24, 13, 14, 15, 16, 17, 18, 7, 8, 9, 10, 11, 12, 1, 2});
    }

    private static String getPostCode3(byte[] bytes) {
        return String.valueOf(new char[]{SETS[0].charAt(getInt(bytes, new byte[]{39, 40, 41, 42, 31, 32})), SETS[0].charAt(getInt(bytes, new byte[]{33, 34, 35, 36, 25, 26})), SETS[0].charAt(getInt(bytes, new byte[]{27, 28, 29, 30, 19, 20})), SETS[0].charAt(getInt(bytes, new byte[]{21, 22, 23, 24, 13, 14})), SETS[0].charAt(getInt(bytes, new byte[]{15, 16, 17, 18, 7, 8})), SETS[0].charAt(getInt(bytes, new byte[]{9, 10, 11, 12, 1, 2}))});
    }

    private static String getMessage(byte[] bytes, int start, int len) {
        StringBuilder sb = new StringBuilder();
        int shift = -1;
        int set = 0;
        int lastset = 0;
        int i = start;
        while (i < start + len) {
            char c = SETS[set].charAt(bytes[i]);
            switch (c) {
                case 65520:
                case 65521:
                case 65522:
                case 65523:
                case 65524:
                    lastset = set;
                    set = c - SHIFTA;
                    shift = 1;
                    break;
                case 65525:
                    lastset = set;
                    set = 0;
                    shift = 2;
                    break;
                case 65526:
                    lastset = set;
                    set = 0;
                    shift = 3;
                    break;
                case 65527:
                    set = 0;
                    shift = -1;
                    break;
                case 65528:
                    set = 1;
                    shift = -1;
                    break;
                case 65529:
                    shift = -1;
                    break;
                case 65530:
                default:
                    sb.append(c);
                    break;
                case 65531:
                    int i2 = i + 1;
                    int i3 = i2 + 1;
                    int i4 = i3 + 1;
                    int i5 = i4 + 1;
                    i = i5 + 1;
                    sb.append(NINE_DIGITS.format((long) ((bytes[i2] << 24) + (bytes[i3] << 18) + (bytes[i4] << 12) + (bytes[i5] << 6) + bytes[i])));
                    break;
            }
            int shift2 = shift - 1;
            if (shift == 0) {
                set = lastset;
            }
            i++;
            shift = shift2;
        }
        while (sb.length() > 0 && sb.charAt(sb.length() - 1) == 65532) {
            sb.setLength(sb.length() - 1);
        }
        return sb.toString();
    }
}
