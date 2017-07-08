package org.apache.xml.serializer.utils;

import org.apache.xpath.VariableStack;
import org.apache.xpath.axes.WalkerFactory;

public class XMLChar {
    private static final byte[] CHARS = null;
    public static final int MASK_CONTENT = 32;
    public static final int MASK_NAME = 8;
    public static final int MASK_NAME_START = 4;
    public static final int MASK_NCNAME = 128;
    public static final int MASK_NCNAME_START = 64;
    public static final int MASK_PUBID = 16;
    public static final int MASK_SPACE = 2;
    public static final int MASK_VALID = 1;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: org.apache.xml.serializer.utils.XMLChar.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: org.apache.xml.serializer.utils.XMLChar.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: org.apache.xml.serializer.utils.XMLChar.<clinit>():void");
    }

    public static boolean isSupplemental(int c) {
        return c >= WalkerFactory.BIT_CHILD && c <= 1114111;
    }

    public static int supplemental(char h, char l) {
        return (((h - 55296) * VariableStack.CLEARLIMITATION) + (l - 56320)) + WalkerFactory.BIT_CHILD;
    }

    public static char highSurrogate(int c) {
        return (char) (((c - WalkerFactory.BIT_CHILD) >> 10) + 55296);
    }

    public static char lowSurrogate(int c) {
        return (char) (((c - WalkerFactory.BIT_CHILD) & 1023) + 56320);
    }

    public static boolean isHighSurrogate(int c) {
        return 55296 <= c && c <= 56319;
    }

    public static boolean isLowSurrogate(int c) {
        return 56320 <= c && c <= 57343;
    }

    public static boolean isValid(int c) {
        if (c < WalkerFactory.BIT_CHILD && (CHARS[c] & MASK_VALID) != 0) {
            return true;
        }
        if (WalkerFactory.BIT_CHILD > c || c > 1114111) {
            return false;
        }
        return true;
    }

    public static boolean isInvalid(int c) {
        return !isValid(c);
    }

    public static boolean isContent(int c) {
        if (c < WalkerFactory.BIT_CHILD && (CHARS[c] & MASK_CONTENT) != 0) {
            return true;
        }
        if (WalkerFactory.BIT_CHILD > c || c > 1114111) {
            return false;
        }
        return true;
    }

    public static boolean isMarkup(int c) {
        return c == 60 || c == 38 || c == 37;
    }

    public static boolean isSpace(int c) {
        return c <= MASK_CONTENT && (CHARS[c] & MASK_SPACE) != 0;
    }

    public static boolean isNameStart(int c) {
        return c < WalkerFactory.BIT_CHILD && (CHARS[c] & MASK_NAME_START) != 0;
    }

    public static boolean isName(int c) {
        return c < WalkerFactory.BIT_CHILD && (CHARS[c] & MASK_NAME) != 0;
    }

    public static boolean isNCNameStart(int c) {
        return c < WalkerFactory.BIT_CHILD && (CHARS[c] & MASK_NCNAME_START) != 0;
    }

    public static boolean isNCName(int c) {
        return c < WalkerFactory.BIT_CHILD && (CHARS[c] & MASK_NCNAME) != 0;
    }

    public static boolean isPubid(int c) {
        return c < WalkerFactory.BIT_CHILD && (CHARS[c] & MASK_PUBID) != 0;
    }

    public static boolean isValidName(String name) {
        if (name.length() == 0 || !isNameStart(name.charAt(0))) {
            return false;
        }
        for (int i = MASK_VALID; i < name.length(); i += MASK_VALID) {
            if (!isName(name.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static boolean isValidNCName(String ncName) {
        if (ncName.length() == 0 || !isNCNameStart(ncName.charAt(0))) {
            return false;
        }
        for (int i = MASK_VALID; i < ncName.length(); i += MASK_VALID) {
            if (!isNCName(ncName.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static boolean isValidNmtoken(String nmtoken) {
        if (nmtoken.length() == 0) {
            return false;
        }
        for (int i = 0; i < nmtoken.length(); i += MASK_VALID) {
            if (!isName(nmtoken.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static boolean isValidIANAEncoding(String ianaEncoding) {
        if (ianaEncoding != null) {
            int length = ianaEncoding.length();
            if (length > 0) {
                char c = ianaEncoding.charAt(0);
                if ((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z')) {
                    for (int i = MASK_VALID; i < length; i += MASK_VALID) {
                        c = ianaEncoding.charAt(i);
                        if ((c < 'A' || c > 'Z') && ((c < 'a' || c > 'z') && ((c < '0' || c > '9') && c != '.' && c != '_' && c != '-'))) {
                            return false;
                        }
                    }
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isValidJavaEncoding(String javaEncoding) {
        if (javaEncoding != null) {
            int length = javaEncoding.length();
            if (length > 0) {
                for (int i = MASK_VALID; i < length; i += MASK_VALID) {
                    char c = javaEncoding.charAt(i);
                    if ((c < 'A' || c > 'Z') && ((c < 'a' || c > 'z') && ((c < '0' || c > '9') && c != '.' && c != '_' && c != '-'))) {
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }
}
