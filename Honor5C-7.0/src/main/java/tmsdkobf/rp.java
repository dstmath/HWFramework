package tmsdkobf;

import android.media.ExifInterface;
import android.text.TextUtils;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import tmsdk.common.utils.d;

/* compiled from: Unknown */
public class rp {
    private static SimpleDateFormat Oh;
    public static final TimeZone Oi = null;
    public static final String Oj = null;
    private static final String[] Ok = null;
    private static final String[] Ol = null;
    private static String TAG;

    /* compiled from: Unknown */
    public static class a {
        public static final String[] Om = null;
        public static final String[] On = null;
        public static final String[] Oo = null;
        public static final String[] Op = null;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdkobf.rp.a.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdkobf.rp.a.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: tmsdkobf.rp.a.<clinit>():void");
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdkobf.rp.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdkobf.rp.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: tmsdkobf.rp.<clinit>():void");
    }

    private static long a(ExifInterface exifInterface) {
        String attribute = exifInterface.getAttribute("DateTime");
        if (attribute == null) {
            return 0;
        }
        d.c(TAG, "exif time:" + attribute);
        ParsePosition parsePosition = new ParsePosition(0);
        try {
            if (Oh == null) {
                Oh = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
                Oh.setTimeZone(TimeZone.getTimeZone("UTC"));
            }
            Date parse = Oh.parse(attribute, parsePosition);
            if (parse == null) {
                return 0;
            }
            long time = parse.getTime();
            return time - ((long) Oi.getOffset(time));
        } catch (Throwable e) {
            d.a(TAG, "exifDateTime", e);
            return 0;
        } catch (Exception e2) {
            return 0;
        }
    }

    public static String aN(String str) {
        if (str == null) {
            return str;
        }
        int lastIndexOf = str.lastIndexOf("/");
        return (lastIndexOf >= 0 && lastIndexOf < str.length() - 1) ? str.substring(lastIndexOf + 1, str.length()) : str;
    }

    public static boolean dE(String str) {
        String toLowerCase = str.toLowerCase();
        for (CharSequence contains : Ok) {
            if (toLowerCase.contains(contains)) {
                return true;
            }
        }
        return false;
    }

    public static boolean dF(String str) {
        String toLowerCase = dG(str).toLowerCase();
        for (String equals : a.On) {
            if (equals.equals(toLowerCase)) {
                return true;
            }
        }
        return false;
    }

    public static String dG(String str) {
        String str2 = null;
        if (str == null) {
            return null;
        }
        int lastIndexOf = str.lastIndexOf(".");
        if (lastIndexOf >= 0 && lastIndexOf < str.length() - 1) {
            str2 = str.substring(lastIndexOf + 1);
        }
        return str2;
    }

    public static boolean dH(String str) {
        if (str == null) {
            return false;
        }
        String toLowerCase = dG(str).toLowerCase();
        for (String equals : a.Op) {
            if (equals.equals(toLowerCase)) {
                return true;
            }
        }
        return false;
    }

    public static boolean dI(String str) {
        return !TextUtils.isEmpty(str) ? str.startsWith(Oj) : false;
    }

    public static boolean dJ(String str) {
        if (str != null) {
            for (String startsWith : Ol) {
                if (str.startsWith(startsWith)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static long dK(String str) {
        ExifInterface exifInterface;
        try {
            exifInterface = new ExifInterface(str);
        } catch (Throwable th) {
            d.a(TAG, "getImageTakenTime", th);
            exifInterface = null;
        }
        return exifInterface == null ? 0 : a(exifInterface);
    }
}
