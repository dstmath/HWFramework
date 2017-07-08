package sun.security.util;

import java.math.BigInteger;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import sun.util.locale.LanguageTag;

public class Debug {
    private static final String args = null;
    private static final char[] hexDigits = null;
    private final String prefix;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: sun.security.util.Debug.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: sun.security.util.Debug.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: sun.security.util.Debug.<clinit>():void");
    }

    private Debug(String prefix) {
        this.prefix = prefix;
    }

    public static Debug getInstance(String option) {
        return getInstance(option, option);
    }

    public static Debug getInstance(String option, String prefix) {
        if (isOn(option)) {
            return new Debug(prefix);
        }
        return null;
    }

    public static boolean isOn(String option) {
        boolean z = true;
        if (args == null) {
            return false;
        }
        if (args.indexOf("all") != -1) {
            return true;
        }
        if (args.indexOf(option) == -1) {
            z = false;
        }
        return z;
    }

    public void println(String message) {
        System.err.println(this.prefix + ": " + message);
    }

    public void println() {
        System.err.println(this.prefix + ":");
    }

    public static String toHexString(BigInteger b) {
        String hexValue = b.toString(16);
        StringBuffer buf = new StringBuffer(hexValue.length() * 2);
        if (hexValue.startsWith(LanguageTag.SEP)) {
            buf.append("   -");
            hexValue = hexValue.substring(1);
        } else {
            buf.append("    ");
        }
        if (hexValue.length() % 2 != 0) {
            hexValue = "0" + hexValue;
        }
        int i = 0;
        while (i < hexValue.length()) {
            buf.append(hexValue.substring(i, i + 2));
            i += 2;
            if (i != hexValue.length()) {
                if (i % 64 == 0) {
                    buf.append("\n    ");
                } else if (i % 8 == 0) {
                    buf.append(" ");
                }
            }
        }
        return buf.toString();
    }

    private static String marshal(String args) {
        if (args == null) {
            return null;
        }
        StringBuffer target = new StringBuffer();
        String keyReg = "[Pp][Ee][Rr][Mm][Ii][Ss][Ss][Ii][Oo][Nn]=";
        String keyStr = "permission=";
        Matcher matcher = Pattern.compile(keyReg + "[a-zA-Z_$][a-zA-Z0-9_$]*([.][a-zA-Z_$][a-zA-Z0-9_$]*)*").matcher(new StringBuffer(args));
        StringBuffer left = new StringBuffer();
        while (matcher.find()) {
            target.append(matcher.group().replaceFirst(keyReg, keyStr));
            target.append("  ");
            matcher.appendReplacement(left, "");
        }
        matcher.appendTail(left);
        StringBuffer source = left;
        keyReg = "[Cc][Oo][Dd][Ee][Bb][Aa][Ss][Ee]=";
        keyStr = "codebase=";
        matcher = Pattern.compile(keyReg + "[^, ;]*").matcher(left);
        left = new StringBuffer();
        while (matcher.find()) {
            target.append(matcher.group().replaceFirst(keyReg, keyStr));
            target.append("  ");
            matcher.appendReplacement(left, "");
        }
        matcher.appendTail(left);
        source = left;
        target.append(left.toString().toLowerCase(Locale.ENGLISH));
        return target.toString();
    }

    public static String toString(byte[] b) {
        if (b == null) {
            return "(null)";
        }
        StringBuilder sb = new StringBuilder(b.length * 3);
        for (int i = 0; i < b.length; i++) {
            int k = b[i] & 255;
            if (i != 0) {
                sb.append(':');
            }
            sb.append(hexDigits[k >>> 4]);
            sb.append(hexDigits[k & 15]);
        }
        return sb.toString();
    }
}
