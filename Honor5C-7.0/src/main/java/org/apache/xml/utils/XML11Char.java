package org.apache.xml.utils;

import org.apache.xpath.axes.WalkerFactory;

public class XML11Char {
    public static final int MASK_XML11_CONTENT = 32;
    public static final int MASK_XML11_CONTENT_INTERNAL = 48;
    public static final int MASK_XML11_CONTROL = 16;
    public static final int MASK_XML11_NAME = 8;
    public static final int MASK_XML11_NAME_START = 4;
    public static final int MASK_XML11_NCNAME = 128;
    public static final int MASK_XML11_NCNAME_START = 64;
    public static final int MASK_XML11_SPACE = 2;
    public static final int MASK_XML11_VALID = 1;
    private static final byte[] XML11CHARS = null;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: org.apache.xml.utils.XML11Char.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: org.apache.xml.utils.XML11Char.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: org.apache.xml.utils.XML11Char.<clinit>():void");
    }

    public static boolean isXML11Space(int c) {
        return c < WalkerFactory.BIT_CHILD && (XML11CHARS[c] & MASK_XML11_SPACE) != 0;
    }

    public static boolean isXML11Valid(int c) {
        if (c < WalkerFactory.BIT_CHILD && (XML11CHARS[c] & MASK_XML11_VALID) != 0) {
            return true;
        }
        if (WalkerFactory.BIT_CHILD > c || c > 1114111) {
            return false;
        }
        return true;
    }

    public static boolean isXML11Invalid(int c) {
        return !isXML11Valid(c);
    }

    public static boolean isXML11ValidLiteral(int c) {
        if (c < WalkerFactory.BIT_CHILD && (XML11CHARS[c] & MASK_XML11_VALID) != 0 && (XML11CHARS[c] & MASK_XML11_CONTROL) == 0) {
            return true;
        }
        if (WalkerFactory.BIT_CHILD > c || c > 1114111) {
            return false;
        }
        return true;
    }

    public static boolean isXML11Content(int c) {
        if (c < WalkerFactory.BIT_CHILD && (XML11CHARS[c] & MASK_XML11_CONTENT) != 0) {
            return true;
        }
        if (WalkerFactory.BIT_CHILD > c || c > 1114111) {
            return false;
        }
        return true;
    }

    public static boolean isXML11InternalEntityContent(int c) {
        if (c < WalkerFactory.BIT_CHILD && (XML11CHARS[c] & MASK_XML11_CONTENT_INTERNAL) != 0) {
            return true;
        }
        if (WalkerFactory.BIT_CHILD > c || c > 1114111) {
            return false;
        }
        return true;
    }

    public static boolean isXML11NameStart(int c) {
        if (c < WalkerFactory.BIT_CHILD && (XML11CHARS[c] & MASK_XML11_NAME_START) != 0) {
            return true;
        }
        if (WalkerFactory.BIT_CHILD > c || c >= 983040) {
            return false;
        }
        return true;
    }

    public static boolean isXML11Name(int c) {
        if (c < WalkerFactory.BIT_CHILD && (XML11CHARS[c] & MASK_XML11_NAME) != 0) {
            return true;
        }
        if (c < WalkerFactory.BIT_CHILD || c >= 983040) {
            return false;
        }
        return true;
    }

    public static boolean isXML11NCNameStart(int c) {
        if (c < WalkerFactory.BIT_CHILD && (XML11CHARS[c] & MASK_XML11_NCNAME_START) != 0) {
            return true;
        }
        if (WalkerFactory.BIT_CHILD > c || c >= 983040) {
            return false;
        }
        return true;
    }

    public static boolean isXML11NCName(int c) {
        if (c < WalkerFactory.BIT_CHILD && (XML11CHARS[c] & MASK_XML11_NCNAME) != 0) {
            return true;
        }
        if (WalkerFactory.BIT_CHILD > c || c >= 983040) {
            return false;
        }
        return true;
    }

    public static boolean isXML11NameHighSurrogate(int c) {
        return 55296 <= c && c <= 56191;
    }

    public static boolean isXML11ValidName(String name) {
        int length = name.length();
        if (length == 0) {
            return false;
        }
        char ch2;
        int i = MASK_XML11_VALID;
        char ch = name.charAt(0);
        if (!isXML11NameStart(ch)) {
            if (length <= MASK_XML11_VALID || !isXML11NameHighSurrogate(ch)) {
                return false;
            }
            ch2 = name.charAt(MASK_XML11_VALID);
            if (!XMLChar.isLowSurrogate(ch2) || !isXML11NameStart(XMLChar.supplemental(ch, ch2))) {
                return false;
            }
            i = MASK_XML11_SPACE;
        }
        while (i < length) {
            ch = name.charAt(i);
            if (!isXML11Name(ch)) {
                i += MASK_XML11_VALID;
                if (i >= length || !isXML11NameHighSurrogate(ch)) {
                    return false;
                }
                ch2 = name.charAt(i);
                if (!XMLChar.isLowSurrogate(ch2) || !isXML11Name(XMLChar.supplemental(ch, ch2))) {
                    return false;
                }
            }
            i += MASK_XML11_VALID;
        }
        return true;
    }

    public static boolean isXML11ValidNCName(String ncName) {
        int length = ncName.length();
        if (length == 0) {
            return false;
        }
        char ch2;
        int i = MASK_XML11_VALID;
        char ch = ncName.charAt(0);
        if (!isXML11NCNameStart(ch)) {
            if (length <= MASK_XML11_VALID || !isXML11NameHighSurrogate(ch)) {
                return false;
            }
            ch2 = ncName.charAt(MASK_XML11_VALID);
            if (!XMLChar.isLowSurrogate(ch2) || !isXML11NCNameStart(XMLChar.supplemental(ch, ch2))) {
                return false;
            }
            i = MASK_XML11_SPACE;
        }
        while (i < length) {
            ch = ncName.charAt(i);
            if (!isXML11NCName(ch)) {
                i += MASK_XML11_VALID;
                if (i >= length || !isXML11NameHighSurrogate(ch)) {
                    return false;
                }
                ch2 = ncName.charAt(i);
                if (!XMLChar.isLowSurrogate(ch2) || !isXML11NCName(XMLChar.supplemental(ch, ch2))) {
                    return false;
                }
            }
            i += MASK_XML11_VALID;
        }
        return true;
    }

    public static boolean isXML11ValidNmtoken(String nmtoken) {
        int length = nmtoken.length();
        if (length == 0) {
            return false;
        }
        int i = 0;
        while (i < length) {
            char ch = nmtoken.charAt(i);
            if (!isXML11Name(ch)) {
                i += MASK_XML11_VALID;
                if (i >= length || !isXML11NameHighSurrogate(ch)) {
                    return false;
                }
                char ch2 = nmtoken.charAt(i);
                if (!XMLChar.isLowSurrogate(ch2) || !isXML11Name(XMLChar.supplemental(ch, ch2))) {
                    return false;
                }
            }
            i += MASK_XML11_VALID;
        }
        return true;
    }

    public static boolean isXML11ValidQName(String str) {
        boolean z = false;
        int colon = str.indexOf(58);
        if (colon == 0 || colon == str.length() - 1) {
            return false;
        }
        if (colon <= 0) {
            return isXML11ValidNCName(str);
        }
        String prefix = str.substring(0, colon);
        String localPart = str.substring(colon + MASK_XML11_VALID);
        if (isXML11ValidNCName(prefix)) {
            z = isXML11ValidNCName(localPart);
        }
        return z;
    }
}
