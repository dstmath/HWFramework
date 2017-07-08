package android.icu.impl;

import android.icu.lang.UScript;
import android.icu.text.StringPrep;
import android.icu.text.StringPrepParseException;
import android.icu.text.UCharacterIterator;
import dalvik.bytecode.Opcodes;

public final class IDNA2003 {
    private static char[] ACE_PREFIX = null;
    private static final int CAPITAL_A = 65;
    private static final int CAPITAL_Z = 90;
    private static final int FULL_STOP = 46;
    private static final int HYPHEN = 45;
    private static final int LOWER_CASE_DELTA = 32;
    private static final int MAX_DOMAIN_NAME_LENGTH = 255;
    private static final int MAX_LABEL_LENGTH = 63;
    private static final StringPrep namePrep = null;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.impl.IDNA2003.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.impl.IDNA2003.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.IDNA2003.<clinit>():void");
    }

    private static boolean startsWithPrefix(StringBuffer src) {
        boolean startsWithPrefix = true;
        if (src.length() < ACE_PREFIX.length) {
            return false;
        }
        for (int i = 0; i < ACE_PREFIX.length; i++) {
            if (toASCIILower(src.charAt(i)) != ACE_PREFIX[i]) {
                startsWithPrefix = false;
            }
        }
        return startsWithPrefix;
    }

    private static char toASCIILower(char ch) {
        if ('A' > ch || ch > 'Z') {
            return ch;
        }
        return (char) (ch + LOWER_CASE_DELTA);
    }

    private static StringBuffer toASCIILower(CharSequence src) {
        StringBuffer dest = new StringBuffer();
        for (int i = 0; i < src.length(); i++) {
            dest.append(toASCIILower(src.charAt(i)));
        }
        return dest;
    }

    private static int compareCaseInsensitiveASCII(StringBuffer s1, StringBuffer s2) {
        for (int i = 0; i != s1.length(); i++) {
            char c1 = s1.charAt(i);
            char c2 = s2.charAt(i);
            if (c1 != c2) {
                int rc = toASCIILower(c1) - toASCIILower(c2);
                if (rc != 0) {
                    return rc;
                }
            }
        }
        return 0;
    }

    private static int getSeparatorIndex(char[] src, int start, int limit) {
        while (start < limit && !isLabelSeparator(src[start])) {
            start++;
        }
        return start;
    }

    private static boolean isLDHChar(int ch) {
        if (ch > UScript.INSCRIPTIONAL_PAHLAVI) {
            return false;
        }
        if (ch == HYPHEN || ((48 <= ch && ch <= 57) || ((CAPITAL_A <= ch && ch <= CAPITAL_Z) || (97 <= ch && ch <= UScript.INSCRIPTIONAL_PAHLAVI)))) {
            return true;
        }
        return false;
    }

    private static boolean isLabelSeparator(int ch) {
        switch (ch) {
            case FULL_STOP /*46*/:
            case 12290:
            case 65294:
            case 65377:
                return true;
            default:
                return false;
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static StringBuffer convertToASCII(UCharacterIterator src, int options) throws StringPrepParseException {
        StringBuffer processOut;
        boolean srcIsASCII = true;
        boolean srcIsLDH = true;
        boolean useSTD3ASCIIRules = (options & 2) != 0;
        while (true) {
            int ch = src.next();
            if (ch == -1) {
                break;
            } else if (ch > 127) {
                srcIsASCII = false;
            }
        }
        int failPos = -1;
        src.setToStart();
        if (srcIsASCII) {
            processOut = new StringBuffer(src.getText());
        } else {
            processOut = namePrep.prepare(src, options);
        }
        int poLen = processOut.length();
        if (poLen == 0) {
            throw new StringPrepParseException("Found zero length lable after NamePrep.", 10);
        }
        StringBuffer dest = new StringBuffer();
        srcIsASCII = true;
        for (int j = 0; j < poLen; j++) {
            ch = processOut.charAt(j);
            if (ch > 127) {
                srcIsASCII = false;
            } else if (!isLDHChar(ch)) {
                srcIsLDH = false;
                failPos = j;
            }
        }
        if (useSTD3ASCIIRules) {
            if (srcIsLDH && processOut.charAt(0) != HYPHEN) {
            }
            String str;
            String stringBuffer;
            if (!srcIsLDH) {
                str = "The input does not conform to the STD 3 ASCII rules";
                stringBuffer = processOut.toString();
                if (failPos > 0) {
                    failPos--;
                }
                throw new StringPrepParseException(str, 5, stringBuffer, failPos);
            } else if (processOut.charAt(0) == HYPHEN) {
                throw new StringPrepParseException("The input does not conform to the STD 3 ASCII rules", 5, processOut.toString(), 0);
            } else {
                str = "The input does not conform to the STD 3 ASCII rules";
                stringBuffer = processOut.toString();
                if (poLen > 0) {
                    poLen--;
                }
                throw new StringPrepParseException(str, 5, stringBuffer, poLen);
            }
        }
        if (srcIsASCII) {
            dest = processOut;
        } else if (startsWithPrefix(processOut)) {
            throw new StringPrepParseException("The input does not start with the ACE Prefix.", 6, processOut.toString(), 0);
        } else {
            StringBuffer lowerOut = toASCIILower(Punycode.encode(processOut, new boolean[poLen]));
            dest.append(ACE_PREFIX, 0, ACE_PREFIX.length);
            dest.append(lowerOut);
        }
        if (dest.length() <= MAX_LABEL_LENGTH) {
            return dest;
        }
        throw new StringPrepParseException("The labels in the input are too long. Length > 63.", 8, dest.toString(), 0);
    }

    public static StringBuffer convertIDNToASCII(String src, int options) throws StringPrepParseException {
        char[] srcArr = src.toCharArray();
        StringBuffer result = new StringBuffer();
        int sepIndex = 0;
        int oldSepIndex = 0;
        while (true) {
            sepIndex = getSeparatorIndex(srcArr, sepIndex, srcArr.length);
            String label = new String(srcArr, oldSepIndex, sepIndex - oldSepIndex);
            if (!(label.length() == 0 && sepIndex == srcArr.length)) {
                result.append(convertToASCII(UCharacterIterator.getInstance(label), options));
            }
            if (sepIndex == srcArr.length) {
                break;
            }
            sepIndex++;
            oldSepIndex = sepIndex;
            result.append('.');
        }
        if (result.length() <= MAX_DOMAIN_NAME_LENGTH) {
            return result;
        }
        throw new StringPrepParseException("The output exceed the max allowed length.", 11);
    }

    public static StringBuffer convertToUnicode(UCharacterIterator src, int options) throws StringPrepParseException {
        StringBuffer processOut;
        boolean srcIsASCII = true;
        int saveIndex = src.getIndex();
        while (true) {
            int ch = src.next();
            if (ch == -1) {
                break;
            } else if (ch > Opcodes.OP_NEG_FLOAT) {
                srcIsASCII = false;
            }
        }
        if (srcIsASCII) {
            processOut = new StringBuffer(src.getText());
        } else {
            try {
                src.setIndex(saveIndex);
                processOut = namePrep.prepare(src, options);
            } catch (StringPrepParseException e) {
                return new StringBuffer(src.getText());
            }
        }
        if (startsWithPrefix(processOut)) {
            StringBuffer stringBuffer;
            try {
                stringBuffer = new StringBuffer(Punycode.decode(processOut.substring(ACE_PREFIX.length, processOut.length()), null));
            } catch (StringPrepParseException e2) {
                stringBuffer = null;
            }
            if (!(stringBuffer == null || compareCaseInsensitiveASCII(processOut, convertToASCII(UCharacterIterator.getInstance(stringBuffer), options)) == 0)) {
                stringBuffer = null;
            }
            if (stringBuffer != null) {
                return stringBuffer;
            }
        }
        return new StringBuffer(src.getText());
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static StringBuffer convertIDNToUnicode(String src, int options) throws StringPrepParseException {
        char[] srcArr = src.toCharArray();
        StringBuffer result = new StringBuffer();
        int sepIndex = 0;
        int oldSepIndex = 0;
        while (true) {
            sepIndex = getSeparatorIndex(srcArr, sepIndex, srcArr.length);
            String label = new String(srcArr, oldSepIndex, sepIndex - oldSepIndex);
            if (label.length() == 0 && sepIndex != srcArr.length) {
                break;
            }
            result.append(convertToUnicode(UCharacterIterator.getInstance(label), options));
            if (sepIndex == srcArr.length) {
                break;
            }
            result.append(srcArr[sepIndex]);
            sepIndex++;
            oldSepIndex = sepIndex;
        }
        throw new StringPrepParseException("Found zero length lable after NamePrep.", 10);
    }

    public static int compare(String s1, String s2, int options) throws StringPrepParseException {
        return compareCaseInsensitiveASCII(convertIDNToASCII(s1, options), convertIDNToASCII(s2, options));
    }
}
