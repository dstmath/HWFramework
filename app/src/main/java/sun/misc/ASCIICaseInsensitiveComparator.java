package sun.misc;

import java.util.Comparator;

public class ASCIICaseInsensitiveComparator implements Comparator<String> {
    static final /* synthetic */ boolean -assertionsDisabled = false;
    public static final Comparator<String> CASE_INSENSITIVE_ORDER = null;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: sun.misc.ASCIICaseInsensitiveComparator.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: sun.misc.ASCIICaseInsensitiveComparator.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: sun.misc.ASCIICaseInsensitiveComparator.<clinit>():void");
    }

    public int compare(String s1, String s2) {
        int n1 = s1.length();
        int n2 = s2.length();
        int minLen = n1 < n2 ? n1 : n2;
        for (int i = 0; i < minLen; i++) {
            char c1 = s1.charAt(i);
            char c2 = s2.charAt(i);
            if (!-assertionsDisabled) {
                Object obj;
                if (c1 > '\u007f' || c2 > '\u007f') {
                    obj = null;
                } else {
                    obj = 1;
                }
                if (obj == null) {
                    throw new AssertionError();
                }
            }
            if (c1 != c2) {
                c1 = (char) toLower(c1);
                c2 = (char) toLower(c2);
                if (c1 != c2) {
                    return c1 - c2;
                }
            }
        }
        return n1 - n2;
    }

    public static int lowerCaseHashCode(String s) {
        int h = 0;
        for (int i = 0; i < s.length(); i++) {
            h = (h * 31) + toLower(s.charAt(i));
        }
        return h;
    }

    static boolean isLower(int ch) {
        return ((ch + -97) | (122 - ch)) >= 0;
    }

    static boolean isUpper(int ch) {
        return ((ch + -65) | (90 - ch)) >= 0;
    }

    static int toLower(int ch) {
        return isUpper(ch) ? ch + 32 : ch;
    }

    static int toUpper(int ch) {
        return isLower(ch) ? ch - 32 : ch;
    }
}
