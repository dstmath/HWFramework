package com.huawei.zxing.maxicode.decoder;

import com.huawei.zxing.common.DecoderResult;
import huawei.android.widget.DialogContentHelper.Dex;
import huawei.android.widget.ViewDragHelper;
import java.text.DecimalFormat;
import java.text.NumberFormat;

final class DecodedBitStreamParser {
    private static final char ECI = '\ufffa';
    private static final char FS = '\u001c';
    private static final char GS = '\u001d';
    private static final char LATCHA = '\ufff7';
    private static final char LATCHB = '\ufff8';
    private static final char LOCK = '\ufff9';
    private static final NumberFormat NINE_DIGITS = null;
    private static final char NS = '\ufffb';
    private static final char PAD = '\ufffc';
    private static final char RS = '\u001e';
    private static final String[] SETS = null;
    private static final char SHIFTA = '\ufff0';
    private static final char SHIFTB = '\ufff1';
    private static final char SHIFTC = '\ufff2';
    private static final char SHIFTD = '\ufff3';
    private static final char SHIFTE = '\ufff4';
    private static final char THREESHIFTA = '\ufff6';
    private static final NumberFormat THREE_DIGITS = null;
    private static final char TWOSHIFTA = '\ufff5';

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.zxing.maxicode.decoder.DecodedBitStreamParser.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.huawei.zxing.maxicode.decoder.DecodedBitStreamParser.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.zxing.maxicode.decoder.DecodedBitStreamParser.<clinit>():void");
    }

    private DecodedBitStreamParser() {
    }

    static DecoderResult decode(byte[] bytes, int mode) {
        StringBuilder result = new StringBuilder(144);
        switch (mode) {
            case ViewDragHelper.STATE_SETTLING /*2*/:
            case ViewDragHelper.DIRECTION_ALL /*3*/:
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
            case ViewDragHelper.EDGE_TOP /*4*/:
                result.append(getMessage(bytes, 1, 93));
                break;
            case Dex.DIALOG_BODY_TWO_IMAGES /*5*/:
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
                case '\ufff0':
                case '\ufff1':
                case '\ufff2':
                case '\ufff3':
                case '\ufff4':
                    lastset = set;
                    set = c - 65520;
                    shift2 = 1;
                    break;
                case '\ufff5':
                    lastset = set;
                    set = 0;
                    shift2 = 2;
                    break;
                case '\ufff6':
                    lastset = set;
                    set = 0;
                    shift2 = 3;
                    break;
                case '\ufff7':
                    set = 0;
                    shift2 = -1;
                    break;
                case '\ufff8':
                    set = 1;
                    shift2 = -1;
                    break;
                case '\ufff9':
                    shift2 = -1;
                    break;
                case '\ufffb':
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
