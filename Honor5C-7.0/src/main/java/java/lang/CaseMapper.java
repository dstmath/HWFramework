package java.lang;

import android.icu.text.Transliterator;
import dalvik.bytecode.Opcodes;
import java.util.Locale;
import libcore.icu.ICU;

class CaseMapper {
    private static final ThreadLocal<Transliterator> EL_UPPER = null;
    private static final char GREEK_CAPITAL_SIGMA = '\u03a3';
    private static final char GREEK_SMALL_FINAL_SIGMA = '\u03c2';
    private static final char LATIN_CAPITAL_I_WITH_DOT = '\u0130';
    private static final char[] upperValues = null;
    private static final char[] upperValues2 = null;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.lang.CaseMapper.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.lang.CaseMapper.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: java.lang.CaseMapper.<clinit>():void");
    }

    private CaseMapper() {
    }

    public static String toLowerCase(Locale locale, String s) {
        String languageCode = locale.getLanguage();
        if (languageCode.equals("tr") || languageCode.equals("az") || languageCode.equals("lt")) {
            return ICU.toLowerCase(s, locale);
        }
        String newString = null;
        int i = 0;
        int end = s.length();
        while (i < end) {
            char ch = s.charAt(i);
            if (ch == LATIN_CAPITAL_I_WITH_DOT || Character.isHighSurrogate(ch)) {
                return ICU.toLowerCase(s, locale);
            }
            char c;
            if (ch == GREEK_CAPITAL_SIGMA && isFinalSigma(s, i)) {
                c = GREEK_SMALL_FINAL_SIGMA;
            } else {
                c = Character.toLowerCase(ch);
            }
            if (ch != c) {
                if (newString == null) {
                    newString = StringFactory.newStringFromString(s);
                }
                newString.setCharAt(i, c);
            }
            i++;
        }
        if (newString == null) {
            newString = s;
        }
        return newString;
    }

    private static boolean isFinalSigma(String s, int index) {
        if (index <= 0) {
            return false;
        }
        boolean z;
        char previous = s.charAt(index - 1);
        if (Character.isLowerCase(previous) || Character.isUpperCase(previous)) {
            z = true;
        } else {
            z = Character.isTitleCase(previous);
        }
        if (!z) {
            return false;
        }
        if (index + 1 >= s.length()) {
            return true;
        }
        char next = s.charAt(index + 1);
        return (Character.isLowerCase(next) || Character.isUpperCase(next) || Character.isTitleCase(next)) ? false : true;
    }

    private static int upperIndex(int ch) {
        int index = -1;
        if (ch >= Opcodes.OP_XOR_INT_LIT8) {
            if (ch <= 1415) {
                switch (ch) {
                    case Opcodes.OP_XOR_INT_LIT8 /*223*/:
                        return 0;
                    case 329:
                        return 1;
                    case 496:
                        return 2;
                    case 912:
                        return 3;
                    case 944:
                        return 4;
                    case 1415:
                        return 5;
                }
            } else if (ch >= 7830) {
                if (ch <= 7834) {
                    index = (ch + 6) - 7830;
                } else if (ch >= 8016 && ch <= 8188) {
                    index = upperValues2[ch - 8016];
                    if (index == 0) {
                        index = -1;
                    }
                } else if (ch >= 64256) {
                    if (ch <= 64262) {
                        index = (ch + 90) - 64256;
                    } else if (ch >= 64275 && ch <= 64279) {
                        index = (ch + 97) - 64275;
                    }
                }
            }
        }
        return index;
    }

    public static String toUpperCase(Locale locale, String s, int count) {
        String languageCode = locale.getLanguage();
        if (!languageCode.equals("tr")) {
            if (!languageCode.equals("az")) {
                if (!languageCode.equals("lt")) {
                    if (languageCode.equals("el")) {
                        return ((Transliterator) EL_UPPER.get()).transliterate(s);
                    }
                    int length;
                    char[] output = null;
                    String newString = null;
                    int o = 0;
                    int end = count;
                    int i = 0;
                    while (o < count) {
                        char ch = s.charAt(o);
                        if (Character.isHighSurrogate(ch)) {
                            return ICU.toUpperCase(s, locale);
                        }
                        int i2;
                        int index = upperIndex(ch);
                        char[] newoutput;
                        if (index == -1) {
                            if (output != null) {
                                length = output.length;
                                if (i >= r0) {
                                    newoutput = new char[((output.length + (count / 6)) + 2)];
                                    System.arraycopy(output, 0, newoutput, 0, output.length);
                                    output = newoutput;
                                }
                            }
                            char upch = Character.toUpperCase(ch);
                            if (output != null) {
                                i2 = i + 1;
                                output[i] = upch;
                            } else if (ch != upch) {
                                if (newString == null) {
                                    newString = StringFactory.newStringFromString(s);
                                }
                                newString.setCharAt(o, upch);
                                i2 = i;
                            } else {
                                i2 = i;
                            }
                        } else {
                            int target = index * 3;
                            char val3 = upperValues[target + 2];
                            if (output == null) {
                                output = new char[(((count / 6) + count) + 2)];
                                i2 = o;
                                if (newString != null) {
                                    System.arraycopy(newString.toCharArray(), 0, output, 0, i2);
                                } else {
                                    System.arraycopy(s.toCharArray(), 0, output, 0, i2);
                                }
                            } else {
                                if ((val3 == '\u0000' ? 1 : 2) + i >= output.length) {
                                    newoutput = new char[((output.length + (count / 6)) + 3)];
                                    System.arraycopy(output, 0, newoutput, 0, output.length);
                                    output = newoutput;
                                    i2 = i;
                                } else {
                                    i2 = i;
                                }
                            }
                            i = i2 + 1;
                            output[i2] = upperValues[target];
                            i2 = i + 1;
                            output[i] = upperValues[target + 1];
                            if (val3 != '\u0000') {
                                i = i2 + 1;
                                output[i2] = val3;
                                i2 = i;
                            }
                        }
                        o++;
                        i = i2;
                    }
                    if (output != null) {
                        String str;
                        length = output.length;
                        if (r0 != i) {
                            if (output.length - i >= 8) {
                                str = new String(output, 0, i);
                                return r17;
                            }
                        }
                        str = new String(0, i, output);
                        return r17;
                    } else if (newString != null) {
                        return newString;
                    } else {
                        return s;
                    }
                }
            }
        }
        return ICU.toUpperCase(s, locale);
    }
}
