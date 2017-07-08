package android.icu.impl;

import dalvik.bytecode.Opcodes;

public final class PatternProps {
    private static final byte[] index2000 = null;
    private static final byte[] latin1 = null;
    private static final int[] syntax2000 = null;
    private static final int[] syntaxOrWhiteSpace2000 = null;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.impl.PatternProps.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.impl.PatternProps.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.PatternProps.<clinit>():void");
    }

    public static boolean isSyntax(int c) {
        boolean z = true;
        if (c < 0) {
            return false;
        }
        if (c <= Opcodes.OP_CONST_CLASS_JUMBO) {
            if (latin1[c] != 3) {
                z = false;
            }
            return z;
        } else if (c < 8208) {
            return false;
        } else {
            if (c <= 12336) {
                if (((syntax2000[index2000[(c - 8192) >> 5]] >> (c & 31)) & 1) == 0) {
                    z = false;
                }
                return z;
            } else if (64830 > c || c > 65094) {
                return false;
            } else {
                if (c > 64831 && 65093 > c) {
                    z = false;
                }
                return z;
            }
        }
    }

    public static boolean isSyntaxOrWhiteSpace(int c) {
        boolean z = true;
        if (c < 0) {
            return false;
        }
        if (c <= Opcodes.OP_CONST_CLASS_JUMBO) {
            if (latin1[c] == null) {
                z = false;
            }
            return z;
        } else if (c < 8206) {
            return false;
        } else {
            if (c <= 12336) {
                if (((syntaxOrWhiteSpace2000[index2000[(c - 8192) >> 5]] >> (c & 31)) & 1) == 0) {
                    z = false;
                }
                return z;
            } else if (64830 > c || c > 65094) {
                return false;
            } else {
                if (c > 64831 && 65093 > c) {
                    z = false;
                }
                return z;
            }
        }
    }

    public static boolean isWhiteSpace(int c) {
        boolean z = true;
        if (c < 0) {
            return false;
        }
        if (c <= Opcodes.OP_CONST_CLASS_JUMBO) {
            if (latin1[c] != 5) {
                z = false;
            }
            return z;
        } else if (8206 > c || c > 8233) {
            return false;
        } else {
            if (c > 8207 && 8232 > c) {
                z = false;
            }
            return z;
        }
    }

    public static int skipWhiteSpace(CharSequence s, int i) {
        while (i < s.length() && isWhiteSpace(s.charAt(i))) {
            i++;
        }
        return i;
    }

    public static String trimWhiteSpace(String s) {
        if (s.length() == 0 || (!isWhiteSpace(s.charAt(0)) && !isWhiteSpace(s.charAt(s.length() - 1)))) {
            return s;
        }
        int start = 0;
        int limit = s.length();
        while (start < limit && isWhiteSpace(s.charAt(start))) {
            start++;
        }
        if (start < limit) {
            while (isWhiteSpace(s.charAt(limit - 1))) {
                limit--;
            }
        }
        return s.substring(start, limit);
    }

    public static boolean isIdentifier(CharSequence s) {
        int limit = s.length();
        if (limit == 0) {
            return false;
        }
        int start = 0;
        while (true) {
            int start2 = start + 1;
            if (isSyntaxOrWhiteSpace(s.charAt(start))) {
                return false;
            }
            if (start2 >= limit) {
                return true;
            }
            start = start2;
        }
    }

    public static boolean isIdentifier(CharSequence s, int start, int limit) {
        if (start >= limit) {
            return false;
        }
        while (true) {
            int start2 = start + 1;
            if (isSyntaxOrWhiteSpace(s.charAt(start))) {
                return false;
            }
            if (start2 >= limit) {
                return true;
            }
            start = start2;
        }
    }

    public static int skipIdentifier(CharSequence s, int i) {
        while (i < s.length() && !isSyntaxOrWhiteSpace(s.charAt(i))) {
            i++;
        }
        return i;
    }
}
