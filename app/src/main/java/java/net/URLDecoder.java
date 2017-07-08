package java.net;

import java.io.UnsupportedEncodingException;
import sun.util.logging.PlatformLogger;

public class URLDecoder {
    static String dfltEncName;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.net.URLDecoder.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.net.URLDecoder.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: java.net.URLDecoder.<clinit>():void");
    }

    @Deprecated
    public static String decode(String s) {
        String str = null;
        try {
            str = decode(s, dfltEncName);
        } catch (UnsupportedEncodingException e) {
        }
        return str;
    }

    public static String decode(String s, String enc) throws UnsupportedEncodingException {
        int i;
        boolean needToChange = false;
        int numChars = s.length();
        if (numChars > PlatformLogger.FINE) {
            i = numChars / 2;
        } else {
            i = numChars;
        }
        StringBuffer sb = new StringBuffer(i);
        int i2 = 0;
        if (enc.length() == 0) {
            throw new UnsupportedEncodingException("URLDecoder: empty string enc parameter");
        }
        byte[] bArr = null;
        while (i2 < numChars) {
            char c = s.charAt(i2);
            switch (c) {
                case '%':
                    if (bArr == null) {
                        try {
                            bArr = new byte[((numChars - i2) / 3)];
                        } catch (NumberFormatException e) {
                            throw new IllegalArgumentException("URLDecoder: Illegal hex characters in escape (%) pattern - " + e.getMessage());
                        }
                    }
                    int pos = 0;
                    while (i2 + 2 < numChars && c == '%') {
                        if (isValidHexChar(s.charAt(i2 + 1)) && isValidHexChar(s.charAt(i2 + 2))) {
                            int v = Integer.parseInt(s.substring(i2 + 1, i2 + 3), 16);
                            if (v < 0) {
                                throw new IllegalArgumentException("URLDecoder: Illegal hex characters in escape (%) pattern - negative value : " + s.substring(i2, i2 + 3));
                            }
                            int pos2 = pos + 1;
                            bArr[pos] = (byte) v;
                            i2 += 3;
                            if (i2 < numChars) {
                                c = s.charAt(i2);
                            }
                            pos = pos2;
                        } else {
                            throw new IllegalArgumentException("URLDecoder: Illegal hex characters in escape (%) pattern : " + s.substring(i2, i2 + 3));
                        }
                    }
                    if (i2 >= numChars || c != '%') {
                        sb.append(new String(bArr, 0, pos, enc));
                        needToChange = true;
                        break;
                    }
                    throw new IllegalArgumentException("URLDecoder: Incomplete trailing escape (%) pattern");
                    break;
                case '+':
                    sb.append(' ');
                    i2++;
                    needToChange = true;
                    break;
                default:
                    sb.append(c);
                    i2++;
                    break;
            }
        }
        return needToChange ? sb.toString() : s;
    }

    private static boolean isValidHexChar(char c) {
        if ('0' <= c && c <= '9') {
            return true;
        }
        if ('a' > c || c > 'f') {
            return 'A' <= c && c <= 'F';
        } else {
            return true;
        }
    }
}
