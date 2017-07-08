package android.icu.impl;

import android.icu.lang.UScript;
import android.icu.text.UTF16;
import android.icu.text.UnicodeSet;
import com.android.dex.DexFormat;
import dalvik.bytecode.Opcodes;

public class PatternTokenizer {
    private static final int AFTER_QUOTE = -1;
    public static final char BACK_SLASH = '\\';
    public static final int BROKEN_ESCAPE = 4;
    public static final int BROKEN_QUOTE = 3;
    public static final int DONE = 0;
    private static final int HEX = 4;
    private static int IN_QUOTE = 0;
    public static final int LITERAL = 2;
    private static final int NONE = 0;
    private static final int NORMAL_QUOTE = 2;
    private static int NO_QUOTE = 0;
    public static final char SINGLE_QUOTE = '\'';
    private static final int SLASH_START = 3;
    private static final int START_QUOTE = 1;
    public static final int SYNTAX = 1;
    public static final int UNKNOWN = 5;
    private UnicodeSet escapeCharacters;
    private UnicodeSet extraQuotingCharacters;
    private UnicodeSet ignorableCharacters;
    private int limit;
    private transient UnicodeSet needingQuoteCharacters;
    private String pattern;
    private int start;
    private UnicodeSet syntaxCharacters;
    private boolean usingQuote;
    private boolean usingSlash;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.impl.PatternTokenizer.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.impl.PatternTokenizer.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.PatternTokenizer.<clinit>():void");
    }

    public PatternTokenizer() {
        this.ignorableCharacters = new UnicodeSet();
        this.syntaxCharacters = new UnicodeSet();
        this.extraQuotingCharacters = new UnicodeSet();
        this.escapeCharacters = new UnicodeSet();
        this.usingSlash = false;
        this.usingQuote = false;
        this.needingQuoteCharacters = null;
    }

    public UnicodeSet getIgnorableCharacters() {
        return (UnicodeSet) this.ignorableCharacters.clone();
    }

    public PatternTokenizer setIgnorableCharacters(UnicodeSet ignorableCharacters) {
        this.ignorableCharacters = (UnicodeSet) ignorableCharacters.clone();
        this.needingQuoteCharacters = null;
        return this;
    }

    public UnicodeSet getSyntaxCharacters() {
        return (UnicodeSet) this.syntaxCharacters.clone();
    }

    public UnicodeSet getExtraQuotingCharacters() {
        return (UnicodeSet) this.extraQuotingCharacters.clone();
    }

    public PatternTokenizer setSyntaxCharacters(UnicodeSet syntaxCharacters) {
        this.syntaxCharacters = (UnicodeSet) syntaxCharacters.clone();
        this.needingQuoteCharacters = null;
        return this;
    }

    public PatternTokenizer setExtraQuotingCharacters(UnicodeSet syntaxCharacters) {
        this.extraQuotingCharacters = (UnicodeSet) syntaxCharacters.clone();
        this.needingQuoteCharacters = null;
        return this;
    }

    public UnicodeSet getEscapeCharacters() {
        return (UnicodeSet) this.escapeCharacters.clone();
    }

    public PatternTokenizer setEscapeCharacters(UnicodeSet escapeCharacters) {
        this.escapeCharacters = (UnicodeSet) escapeCharacters.clone();
        return this;
    }

    public boolean isUsingQuote() {
        return this.usingQuote;
    }

    public PatternTokenizer setUsingQuote(boolean usingQuote) {
        this.usingQuote = usingQuote;
        this.needingQuoteCharacters = null;
        return this;
    }

    public boolean isUsingSlash() {
        return this.usingSlash;
    }

    public PatternTokenizer setUsingSlash(boolean usingSlash) {
        this.usingSlash = usingSlash;
        this.needingQuoteCharacters = null;
        return this;
    }

    public int getLimit() {
        return this.limit;
    }

    public PatternTokenizer setLimit(int limit) {
        this.limit = limit;
        return this;
    }

    public int getStart() {
        return this.start;
    }

    public PatternTokenizer setStart(int start) {
        this.start = start;
        return this;
    }

    public PatternTokenizer setPattern(CharSequence pattern) {
        return setPattern(pattern.toString());
    }

    public PatternTokenizer setPattern(String pattern) {
        if (pattern == null) {
            throw new IllegalArgumentException("Inconsistent arguments");
        }
        this.start = NONE;
        this.limit = pattern.length();
        this.pattern = pattern;
        return this;
    }

    public String quoteLiteral(CharSequence string) {
        return quoteLiteral(string.toString());
    }

    public String quoteLiteral(String string) {
        if (this.needingQuoteCharacters == null) {
            this.needingQuoteCharacters = new UnicodeSet().addAll(this.syntaxCharacters).addAll(this.ignorableCharacters).addAll(this.extraQuotingCharacters);
            if (this.usingSlash) {
                this.needingQuoteCharacters.add(92);
            }
            if (this.usingQuote) {
                this.needingQuoteCharacters.add(39);
            }
        }
        StringBuffer result = new StringBuffer();
        int quotedChar = NO_QUOTE;
        int i = NONE;
        while (i < string.length()) {
            int cp = UTF16.charAt(string, i);
            if (this.escapeCharacters.contains(cp)) {
                if (quotedChar == IN_QUOTE) {
                    result.append(SINGLE_QUOTE);
                    quotedChar = NO_QUOTE;
                }
                appendEscaped(result, cp);
            } else if (!this.needingQuoteCharacters.contains(cp)) {
                if (quotedChar == IN_QUOTE) {
                    result.append(SINGLE_QUOTE);
                    quotedChar = NO_QUOTE;
                }
                UTF16.append(result, cp);
            } else if (quotedChar == IN_QUOTE) {
                UTF16.append(result, cp);
                if (this.usingQuote && cp == 39) {
                    result.append(SINGLE_QUOTE);
                }
            } else if (this.usingSlash) {
                result.append(BACK_SLASH);
                UTF16.append(result, cp);
            } else if (!this.usingQuote) {
                appendEscaped(result, cp);
            } else if (cp == 39) {
                result.append(SINGLE_QUOTE);
                result.append(SINGLE_QUOTE);
            } else {
                result.append(SINGLE_QUOTE);
                UTF16.append(result, cp);
                quotedChar = IN_QUOTE;
            }
            i += UTF16.getCharCount(cp);
        }
        if (quotedChar == IN_QUOTE) {
            result.append(SINGLE_QUOTE);
        }
        return result.toString();
    }

    private void appendEscaped(StringBuffer result, int cp) {
        if (cp <= DexFormat.MAX_TYPE_IDX) {
            result.append("\\u").append(Utility.hex((long) cp, HEX));
        } else {
            result.append("\\U").append(Utility.hex((long) cp, 8));
        }
    }

    public String normalize() {
        int oldStart = this.start;
        StringBuffer result = new StringBuffer();
        CharSequence buffer = new StringBuffer();
        while (true) {
            buffer.setLength(NONE);
            int status = next(buffer);
            if (status == 0) {
                this.start = oldStart;
                return result.toString();
            } else if (status != SYNTAX) {
                result.append(quoteLiteral(buffer));
            } else {
                result.append(buffer);
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int next(StringBuffer buffer) {
        if (this.start >= this.limit) {
            return NONE;
        }
        int status = UNKNOWN;
        int lastQuote = UNKNOWN;
        int quoteStatus = NONE;
        int hexCount = NONE;
        int hexValue = NONE;
        int i = this.start;
        while (i < this.limit) {
            int cp = UTF16.charAt(this.pattern, i);
            switch (quoteStatus) {
                case AFTER_QUOTE /*-1*/:
                    if (cp == lastQuote) {
                        UTF16.append(buffer, cp);
                        quoteStatus = NORMAL_QUOTE;
                        break;
                    }
                    quoteStatus = NONE;
                case SYNTAX /*1*/:
                    if (cp != lastQuote) {
                        UTF16.append(buffer, cp);
                        quoteStatus = NORMAL_QUOTE;
                        break;
                    }
                    UTF16.append(buffer, cp);
                    quoteStatus = NONE;
                    break;
                case NORMAL_QUOTE /*2*/:
                    if (cp != lastQuote) {
                        UTF16.append(buffer, cp);
                        break;
                    }
                    quoteStatus = AFTER_QUOTE;
                    break;
                case SLASH_START /*3*/:
                    switch (cp) {
                        case Opcodes.OP_IGET_BOOLEAN /*85*/:
                            quoteStatus = HEX;
                            hexCount = 8;
                            hexValue = NONE;
                            continue;
                        case Opcodes.OP_INVOKE_SUPER_RANGE /*117*/:
                            quoteStatus = HEX;
                            hexCount = HEX;
                            hexValue = NONE;
                            continue;
                        default:
                            if (this.usingSlash) {
                                UTF16.append(buffer, cp);
                                quoteStatus = NONE;
                                continue;
                            } else {
                                buffer.append(BACK_SLASH);
                                quoteStatus = NONE;
                            }
                    }
                case HEX /*4*/:
                    hexValue = (hexValue << HEX) + cp;
                    switch (cp) {
                        case Opcodes.OP_CMPG_DOUBLE /*48*/:
                        case Opcodes.OP_CMP_LONG /*49*/:
                        case Opcodes.OP_IF_EQ /*50*/:
                        case Opcodes.OP_IF_NE /*51*/:
                        case Opcodes.OP_IF_LT /*52*/:
                        case Opcodes.OP_IF_GE /*53*/:
                        case Opcodes.OP_IF_GT /*54*/:
                        case Opcodes.OP_IF_LE /*55*/:
                        case Opcodes.OP_IF_EQZ /*56*/:
                        case Opcodes.OP_IF_NEZ /*57*/:
                            hexValue -= 48;
                            break;
                        case UScript.BRAHMI /*65*/:
                        case UScript.CHAM /*66*/:
                        case UScript.CIRTH /*67*/:
                        case Opcodes.OP_AGET /*68*/:
                        case Opcodes.OP_AGET_WIDE /*69*/:
                        case Opcodes.OP_AGET_OBJECT /*70*/:
                            hexValue -= 55;
                            break;
                        case Opcodes.OP_SGET_WIDE /*97*/:
                        case Opcodes.OP_SGET_OBJECT /*98*/:
                        case Opcodes.OP_SGET_BOOLEAN /*99*/:
                        case Opcodes.OP_SGET_BYTE /*100*/:
                        case Opcodes.OP_SGET_CHAR /*101*/:
                        case Opcodes.OP_SGET_SHORT /*102*/:
                            hexValue -= 87;
                            break;
                        default:
                            this.start = i;
                            return HEX;
                    }
                    hexCount += AFTER_QUOTE;
                    if (hexCount != 0) {
                        break;
                    }
                    quoteStatus = NONE;
                    UTF16.append(buffer, hexValue);
                    break;
                default:
                    if (!this.ignorableCharacters.contains(cp)) {
                        if (!this.syntaxCharacters.contains(cp)) {
                            status = NORMAL_QUOTE;
                            if (cp != 92) {
                                if (!this.usingQuote || cp != 39) {
                                    UTF16.append(buffer, cp);
                                    break;
                                }
                                lastQuote = cp;
                                quoteStatus = SYNTAX;
                                break;
                            }
                            quoteStatus = SLASH_START;
                            break;
                        } else if (status == UNKNOWN) {
                            UTF16.append(buffer, cp);
                            this.start = UTF16.getCharCount(cp) + i;
                            return SYNTAX;
                        } else {
                            this.start = i;
                            return status;
                        }
                    }
                    continue;
            }
            i += UTF16.getCharCount(cp);
        }
        this.start = this.limit;
        switch (quoteStatus) {
            case SYNTAX /*1*/:
            case NORMAL_QUOTE /*2*/:
                status = SLASH_START;
                break;
            case SLASH_START /*3*/:
                if (!this.usingSlash) {
                    buffer.append(BACK_SLASH);
                    break;
                }
                status = HEX;
                break;
            case HEX /*4*/:
                status = HEX;
                break;
        }
        return status;
    }
}
