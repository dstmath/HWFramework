package tmsdkobf;

import android.telephony.PhoneNumberUtils;

/* compiled from: Unknown */
public class rb extends PhoneNumberUtils {
    public static final String[] KI = null;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdkobf.rb.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdkobf.rb.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: tmsdkobf.rb.<clinit>():void");
    }

    public static String da(String str) {
        if (str == null || str.length() <= 2) {
            return str;
        }
        for (String str2 : KI) {
            if (str.startsWith(str2)) {
                return str.substring(str2.length());
            }
        }
        return str;
    }

    public static boolean db(String str) {
        for (String startsWith : KI) {
            if (str.startsWith(startsWith)) {
                return true;
            }
        }
        return false;
    }

    public static String dc(String str) {
        return str == null ? str : stripSeparators(str).replace("-", "").replace(" ", "").trim();
    }

    public static boolean dd(String str) {
        int length = str.length();
        do {
            length--;
            if (length < 0) {
                return true;
            }
        } while (isISODigit(str.charAt(length)));
        return false;
    }

    public static String de(String str) {
        return j(df(da(str)), 8);
    }

    public static String df(String str) {
        Object obj = null;
        if (str == null) {
            return null;
        }
        int length = str.length();
        StringBuilder stringBuilder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            char charAt = str.charAt(i);
            if (charAt == '+') {
                if (obj == null) {
                    obj = 1;
                } else {
                    continue;
                }
            }
            if (!isDialable(charAt)) {
                if (isStartsPostDial(charAt)) {
                    break;
                }
            } else {
                stringBuilder.append(charAt);
            }
        }
        return stringBuilder.toString();
    }

    private static String j(String str, int i) {
        if (str == null) {
            return null;
        }
        StringBuilder stringBuilder = new StringBuilder(i);
        int length = str.length();
        int i2 = length - 1;
        while (i2 >= 0 && length - i2 <= i) {
            stringBuilder.append(str.charAt(i2));
            i2--;
        }
        return stringBuilder.toString();
    }
}
