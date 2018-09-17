package com.huawei.zxing.maxicode.decoder;

import com.huawei.zxing.common.DecoderResult;
import java.text.DecimalFormat;
import java.text.NumberFormat;

final class DecodedBitStreamParser {
    private static final char ECI = '￺';
    private static final char FS = '\u001c';
    private static final char GS = '\u001d';
    private static final char LATCHA = '￷';
    private static final char LATCHB = '￸';
    private static final char LOCK = '￹';
    private static final NumberFormat NINE_DIGITS = new DecimalFormat("000000000");
    private static final char NS = '￻';
    private static final char PAD = '￼';
    private static final char RS = '\u001e';
    private static final String[] SETS = new String[]{"\nABCDEFGHIJKLMNOPQRSTUVWXYZ￺\u001c\u001d\u001e￻ ￼\"#$%&'()*+,-./0123456789:￱￲￳￴￸", "`abcdefghijklmnopqrstuvwxyz￺\u001c\u001d\u001e￻{￼}~;<=>?[\\]^_ ,./:@!|￼￵￶￼￰￲￳￴￷", "ÀÁÂÃÄÅÆÇÈÉÊËÌÍÎÏÐÑÒÓÔÕÖ×ØÙÚ￺\u001c\u001d\u001eÛÜÝÞßª¬±²³µ¹º¼½¾￷ ￹￳￴￸", "àáâãäåæçèéêëìíîïðñòóôõö÷øùú￺\u001c\u001d\u001e￻ûüýþÿ¡¨«¯°´·¸»¿￷ ￲￹￴￸", "\u0000\u0001\u0002\u0003\u0004\u0005\u0006\u0007\b\t\n\u000b\f\r\u000e\u000f\u0010\u0011\u0012\u0013\u0014\u0015\u0016\u0017\u0018\u0019\u001a￺￼￼\u001b￻\u001c\u001d\u001e\u001f ¢£¤¥¦§©­®¶￷ ￲￳￹￸", "\u0000\u0001\u0002\u0003\u0004\u0005\u0006\u0007\b\t\n\u000b\f\r\u000e\u000f\u0010\u0011\u0012\u0013\u0014\u0015\u0016\u0017\u0018\u0019\u001a\u001b\u001c\u001d\u001e\u001f !\"#$%&'()*+,-./0123456789:;<=>?"};
    private static final char SHIFTA = '￰';
    private static final char SHIFTB = '￱';
    private static final char SHIFTC = '￲';
    private static final char SHIFTD = '￳';
    private static final char SHIFTE = '￴';
    private static final char THREESHIFTA = '￶';
    private static final NumberFormat THREE_DIGITS = new DecimalFormat("000");
    private static final char TWOSHIFTA = '￵';

    private DecodedBitStreamParser() {
    }

    static DecoderResult decode(byte[] bytes, int mode) {
        StringBuilder result = new StringBuilder(144);
        switch (mode) {
            case 2:
            case 3:
                String postcode;
                if (mode == 2) {
                    postcode = new DecimalFormat("0000000000".substring(0, getPostCode2Length(bytes))).format((long) getPostCode2(bytes));
                } else {
                    postcode = getPostCode3(bytes);
                }
                String country = THREE_DIGITS.format((long) getCountry(bytes));
                String service = THREE_DIGITS.format((long) getServiceClass(bytes));
                result.append(getMessage(bytes, 10, 84));
                if (!result.toString().startsWith("[)>\u001e01\u001d")) {
                    result.insert(0, postcode + GS + country + GS + service + GS);
                    break;
                }
                result.insert(9, postcode + GS + country + GS + service + GS);
                break;
            case 4:
                result.append(getMessage(bytes, 1, 93));
                break;
            case 5:
                result.append(getMessage(bytes, 1, 77));
                break;
        }
        return new DecoderResult(bytes, result.toString(), null, String.valueOf(mode));
    }

    private static int getBit(int bit, byte[] bytes) {
        bit--;
        if ((bytes[bit / 6] & (1 << (5 - (bit % 6)))) == 0) {
            return 0;
        }
        return 1;
    }

    private static int getInt(byte[] bytes, byte[] x) {
        int val = 0;
        for (int i = 0; i < x.length; i++) {
            val += getBit(x[i], bytes) << ((x.length - i) - 1);
        }
        return val;
    }

    private static int getCountry(byte[] bytes) {
        return getInt(bytes, new byte[]{(byte) 53, (byte) 54, (byte) 43, (byte) 44, (byte) 45, (byte) 46, (byte) 47, (byte) 48, (byte) 37, (byte) 38});
    }

    private static int getServiceClass(byte[] bytes) {
        return getInt(bytes, new byte[]{(byte) 55, (byte) 56, (byte) 57, (byte) 58, (byte) 59, (byte) 60, (byte) 49, (byte) 50, (byte) 51, (byte) 52});
    }

    private static int getPostCode2Length(byte[] bytes) {
        return getInt(bytes, new byte[]{(byte) 39, (byte) 40, (byte) 41, (byte) 42, (byte) 31, (byte) 32});
    }

    private static int getPostCode2(byte[] bytes) {
        return getInt(bytes, new byte[]{(byte) 33, (byte) 34, (byte) 35, (byte) 36, (byte) 25, (byte) 26, (byte) 27, (byte) 28, (byte) 29, (byte) 30, (byte) 19, (byte) 20, (byte) 21, (byte) 22, (byte) 23, (byte) 24, (byte) 13, (byte) 14, (byte) 15, (byte) 16, (byte) 17, (byte) 18, (byte) 7, (byte) 8, (byte) 9, (byte) 10, (byte) 11, (byte) 12, (byte) 1, (byte) 2});
    }

    private static String getPostCode3(byte[] bytes) {
        return String.valueOf(new char[]{SETS[0].charAt(getInt(bytes, new byte[]{(byte) 39, (byte) 40, (byte) 41, (byte) 42, (byte) 31, (byte) 32})), SETS[0].charAt(getInt(bytes, new byte[]{(byte) 33, (byte) 34, (byte) 35, (byte) 36, (byte) 25, (byte) 26})), SETS[0].charAt(getInt(bytes, new byte[]{(byte) 27, (byte) 28, (byte) 29, (byte) 30, (byte) 19, (byte) 20})), SETS[0].charAt(getInt(bytes, new byte[]{(byte) 21, (byte) 22, (byte) 23, (byte) 24, (byte) 13, (byte) 14})), SETS[0].charAt(getInt(bytes, new byte[]{(byte) 15, (byte) 16, (byte) 17, (byte) 18, (byte) 7, (byte) 8})), SETS[0].charAt(getInt(bytes, new byte[]{(byte) 9, (byte) 10, (byte) 11, (byte) 12, (byte) 1, (byte) 2}))});
    }

    private static String getMessage(byte[] bytes, int start, int len) {
        StringBuilder sb = new StringBuilder();
        int shift = -1;
        int set = 0;
        int lastset = 0;
        int i = start;
        while (i < start + len) {
            int shift2;
            char c = SETS[set].charAt(bytes[i]);
            switch (c) {
                case 65520:
                case 65521:
                case 65522:
                case 65523:
                case 65524:
                    lastset = set;
                    set = c - 65520;
                    shift2 = 1;
                    break;
                case 65525:
                    lastset = set;
                    set = 0;
                    shift2 = 2;
                    break;
                case 65526:
                    lastset = set;
                    set = 0;
                    shift2 = 3;
                    break;
                case 65527:
                    set = 0;
                    shift2 = -1;
                    break;
                case 65528:
                    set = 1;
                    shift2 = -1;
                    break;
                case 65529:
                    shift2 = -1;
                    break;
                case 65531:
                    i++;
                    i++;
                    i++;
                    i++;
                    i++;
                    sb.append(NINE_DIGITS.format((long) (((((bytes[i] << 24) + (bytes[i] << 18)) + (bytes[i] << 12)) + (bytes[i] << 6)) + bytes[i])));
                    shift2 = shift;
                    break;
                default:
                    sb.append(c);
                    shift2 = shift;
                    break;
            }
            shift = shift2 - 1;
            if (shift2 == 0) {
                set = lastset;
            }
            i++;
        }
        while (sb.length() > 0 && sb.charAt(sb.length() - 1) == PAD) {
            sb.setLength(sb.length() - 1);
        }
        return sb.toString();
    }
}
