package android.icu.text;

import android.icu.impl.IllegalIcuArgumentException;
import android.icu.impl.PatternProps;
import android.icu.impl.Utility;
import android.icu.lang.UCharacter;
import android.icu.lang.UScript;
import android.icu.text.Normalizer.Mode;
import android.icu.util.AnnualTimeZoneRule;
import com.android.dex.DexFormat;
import dalvik.bytecode.Opcodes;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.w3c.dom.traversal.NodeFilter;

class TransliteratorParser {
    private static final char ALT_FORWARD_RULE_OP = '\u2192';
    private static final char ALT_FUNCTION = '\u2206';
    private static final char ALT_FWDREV_RULE_OP = '\u2194';
    private static final char ALT_REVERSE_RULE_OP = '\u2190';
    private static final char ANCHOR_START = '^';
    private static final char CONTEXT_ANTE = '{';
    private static final char CONTEXT_POST = '}';
    private static final char CURSOR_OFFSET = '@';
    private static final char CURSOR_POS = '|';
    private static final char DOT = '.';
    private static final String DOT_SET = "[^[:Zp:][:Zl:]\\r\\n$]";
    private static final char END_OF_RULE = ';';
    private static final char ESCAPE = '\\';
    private static final char FORWARD_RULE_OP = '>';
    private static final char FUNCTION = '&';
    private static final char FWDREV_RULE_OP = '~';
    private static final String HALF_ENDERS = "=><\u2190\u2192\u2194;";
    private static final String ID_TOKEN = "::";
    private static final int ID_TOKEN_LEN = 2;
    private static UnicodeSet ILLEGAL_FUNC = null;
    private static UnicodeSet ILLEGAL_SEG = null;
    private static UnicodeSet ILLEGAL_TOP = null;
    private static final char KLEENE_STAR = '*';
    private static final char ONE_OR_MORE = '+';
    private static final String OPERATORS = "=><\u2190\u2192\u2194";
    private static final char QUOTE = '\'';
    private static final char REVERSE_RULE_OP = '<';
    private static final char RULE_COMMENT_CHAR = '#';
    private static final char SEGMENT_CLOSE = ')';
    private static final char SEGMENT_OPEN = '(';
    private static final char VARIABLE_DEF_OP = '=';
    private static final char ZERO_OR_ONE = '?';
    public UnicodeSet compoundFilter;
    private Data curData;
    public List<Data> dataVector;
    private int direction;
    private int dotStandIn;
    public List<String> idBlockVector;
    private ParseData parseData;
    private List<StringMatcher> segmentObjects;
    private StringBuffer segmentStandins;
    private String undefinedVariableName;
    private char variableLimit;
    private Map<String, char[]> variableNames;
    private char variableNext;
    private List<Object> variablesVector;

    private class ParseData implements SymbolTable {
        private ParseData() {
        }

        public char[] lookup(String name) {
            return (char[]) TransliteratorParser.this.variableNames.get(name);
        }

        public UnicodeMatcher lookupMatcher(int ch) {
            int i = ch - TransliteratorParser.this.curData.variablesBase;
            if (i < 0 || i >= TransliteratorParser.this.variablesVector.size()) {
                return null;
            }
            return (UnicodeMatcher) TransliteratorParser.this.variablesVector.get(i);
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public String parseReference(String text, ParsePosition pos, int limit) {
            int start = pos.getIndex();
            int i = start;
            while (i < limit) {
                char c = text.charAt(i);
                if ((i != start || UCharacter.isUnicodeIdentifierStart(c)) && UCharacter.isUnicodeIdentifierPart(c)) {
                    i++;
                }
            }
            if (i == start) {
                return null;
            }
            pos.setIndex(i);
            return text.substring(start, i);
        }

        public boolean isMatcher(int ch) {
            int i = ch - TransliteratorParser.this.curData.variablesBase;
            if (i < 0 || i >= TransliteratorParser.this.variablesVector.size()) {
                return true;
            }
            return TransliteratorParser.this.variablesVector.get(i) instanceof UnicodeMatcher;
        }

        public boolean isReplacer(int ch) {
            int i = ch - TransliteratorParser.this.curData.variablesBase;
            if (i < 0 || i >= TransliteratorParser.this.variablesVector.size()) {
                return true;
            }
            return TransliteratorParser.this.variablesVector.get(i) instanceof UnicodeReplacer;
        }
    }

    private static abstract class RuleBody {
        abstract String handleNextLine();

        abstract void reset();

        private RuleBody() {
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        String nextLine() {
            String s = handleNextLine();
            if (s == null || s.length() <= 0 || s.charAt(s.length() - 1) != TransliteratorParser.ESCAPE) {
                return s;
            }
            StringBuilder b = new StringBuilder(s);
            while (true) {
                b.deleteCharAt(b.length() - 1);
                s = handleNextLine();
                if (s == null) {
                    break;
                }
                b.append(s);
                if (s.length() > 0 && s.charAt(s.length() - 1) == TransliteratorParser.ESCAPE) {
                }
            }
            return b.toString();
        }
    }

    private static class RuleArray extends RuleBody {
        String[] array;
        int i;

        public RuleArray(String[] array) {
            super();
            this.array = array;
            this.i = 0;
        }

        public String handleNextLine() {
            if (this.i >= this.array.length) {
                return null;
            }
            String[] strArr = this.array;
            int i = this.i;
            this.i = i + 1;
            return strArr[i];
        }

        public void reset() {
            this.i = 0;
        }
    }

    private static class RuleHalf {
        public boolean anchorEnd;
        public boolean anchorStart;
        public int ante;
        public int cursor;
        public int cursorOffset;
        private int cursorOffsetPos;
        private int nextSegmentNumber;
        public int post;
        public String text;

        private RuleHalf() {
            this.cursor = -1;
            this.ante = -1;
            this.post = -1;
            this.cursorOffset = 0;
            this.cursorOffsetPos = 0;
            this.anchorStart = false;
            this.anchorEnd = false;
            this.nextSegmentNumber = 1;
        }

        public int parse(String rule, int pos, int limit, TransliteratorParser parser) {
            int start = pos;
            StringBuffer buf = new StringBuffer();
            pos = parseSection(rule, pos, limit, parser, buf, TransliteratorParser.ILLEGAL_TOP, false);
            this.text = buf.toString();
            if (this.cursorOffset > 0 && this.cursor != this.cursorOffsetPos) {
                TransliteratorParser.syntaxError("Misplaced |", rule, start);
            }
            return pos;
        }

        private int parseSection(String rule, int pos, int limit, TransliteratorParser parser, StringBuffer buf, UnicodeSet illegal, boolean isSegment) {
            int start = pos;
            ParsePosition pp = null;
            int quoteStart = -1;
            int quoteLimit = -1;
            int varStart = -1;
            int varLimit = -1;
            int[] iref = new int[1];
            int bufStart = buf.length();
            int pos2 = pos;
            while (pos2 < limit) {
                pos = pos2 + 1;
                char c = rule.charAt(pos2);
                if (PatternProps.isWhiteSpace(c)) {
                    pos2 = pos;
                } else if (TransliteratorParser.HALF_ENDERS.indexOf(c) < 0) {
                    if (this.anchorEnd) {
                        TransliteratorParser.syntaxError("Malformed variable reference", rule, start);
                    }
                    ParsePosition parsePosition;
                    if (UnicodeSet.resemblesPattern(rule, pos - 1)) {
                        if (pp == null) {
                            parsePosition = new ParsePosition(0);
                        }
                        pp.setIndex(pos - 1);
                        buf.append(parser.parseSet(rule, pp));
                        pos2 = pp.getIndex();
                    } else if (c == TransliteratorParser.ESCAPE) {
                        if (pos == limit) {
                            TransliteratorParser.syntaxError("Trailing backslash", rule, start);
                        }
                        iref[0] = pos;
                        int escaped = Utility.unescapeAt(rule, iref);
                        pos = iref[0];
                        if (escaped == -1) {
                            TransliteratorParser.syntaxError("Malformed escape", rule, start);
                        }
                        parser.checkVariableRange(escaped, rule, start);
                        UTF16.append(buf, escaped);
                        pos2 = pos;
                    } else if (c == TransliteratorParser.QUOTE) {
                        int iq = rule.indexOf(39, pos);
                        if (iq == pos) {
                            buf.append(c);
                            pos++;
                        } else {
                            quoteStart = buf.length();
                            while (true) {
                                if (iq < 0) {
                                    TransliteratorParser.syntaxError("Unterminated quote", rule, start);
                                }
                                buf.append(rule.substring(pos, iq));
                                pos = iq + 1;
                                if (pos >= limit || rule.charAt(pos) != TransliteratorParser.QUOTE) {
                                    quoteLimit = buf.length();
                                    for (iq = quoteStart; iq < quoteLimit; iq++) {
                                        parser.checkVariableRange(buf.charAt(iq), rule, start);
                                    }
                                } else {
                                    iq = rule.indexOf(39, pos + 1);
                                }
                            }
                        }
                        pos2 = pos;
                    } else {
                        parser.checkVariableRange(c, rule, start);
                        if (illegal.contains((int) c)) {
                            TransliteratorParser.syntaxError("Illegal character '" + c + TransliteratorParser.QUOTE, rule, start);
                        }
                        int bufSegStart;
                        switch (c) {
                            case Opcodes.OP_FILLED_NEW_ARRAY /*36*/:
                                if (pos != limit) {
                                    int r = UCharacter.digit(rule.charAt(pos), 10);
                                    if (r >= 1 && r <= 9) {
                                        iref[0] = pos;
                                        r = Utility.parseNumber(rule, iref, 10);
                                        if (r < 0) {
                                            TransliteratorParser.syntaxError("Undefined segment reference", rule, start);
                                        }
                                        pos = iref[0];
                                        buf.append(parser.getSegmentStandin(r));
                                        break;
                                    }
                                    if (pp == null) {
                                        parsePosition = new ParsePosition(0);
                                    }
                                    pp.setIndex(pos);
                                    String name = parser.parseData.parseReference(rule, pp, limit);
                                    if (name != null) {
                                        pos = pp.getIndex();
                                        varStart = buf.length();
                                        parser.appendVariableDef(name, buf);
                                        varLimit = buf.length();
                                        break;
                                    }
                                    this.anchorEnd = true;
                                    break;
                                }
                                this.anchorEnd = true;
                                break;
                            case Opcodes.OP_FILL_ARRAY_DATA /*38*/:
                            case '\u2206':
                                iref[0] = pos;
                                SingleID single = TransliteratorIDParser.parseFilterID(rule, iref);
                                if (single == null || !Utility.parseChar(rule, iref, TransliteratorParser.SEGMENT_OPEN)) {
                                    TransliteratorParser.syntaxError("Invalid function", rule, start);
                                }
                                Transliterator t = single.getInstance();
                                if (t == null) {
                                    TransliteratorParser.syntaxError("Invalid function ID", rule, start);
                                }
                                bufSegStart = buf.length();
                                pos = parseSection(rule, iref[0], limit, parser, buf, TransliteratorParser.ILLEGAL_FUNC, true);
                                FunctionReplacer functionReplacer = new FunctionReplacer(t, new StringReplacer(buf.substring(bufSegStart), parser.curData));
                                buf.setLength(bufSegStart);
                                buf.append(parser.generateStandInFor(functionReplacer));
                                break;
                            case Opcodes.OP_GOTO /*40*/:
                                bufSegStart = buf.length();
                                int segmentNumber = this.nextSegmentNumber;
                                this.nextSegmentNumber = segmentNumber + 1;
                                pos = parseSection(rule, pos, limit, parser, buf, TransliteratorParser.ILLEGAL_SEG, true);
                                parser.setSegmentObject(segmentNumber, new StringMatcher(buf.substring(bufSegStart), segmentNumber, parser.curData));
                                buf.setLength(bufSegStart);
                                buf.append(parser.getSegmentStandin(segmentNumber));
                                break;
                            case Opcodes.OP_GOTO_16 /*41*/:
                                return pos;
                            case Opcodes.OP_GOTO_32 /*42*/:
                            case Opcodes.OP_PACKED_SWITCH /*43*/:
                            case UScript.BATAK /*63*/:
                                if (isSegment && buf.length() == bufStart) {
                                    TransliteratorParser.syntaxError("Misplaced quantifier", rule, start);
                                    break;
                                }
                                int qstart;
                                int qlimit;
                                if (buf.length() == quoteLimit) {
                                    qstart = quoteStart;
                                    qlimit = quoteLimit;
                                } else if (buf.length() == varLimit) {
                                    qstart = varStart;
                                    qlimit = varLimit;
                                } else {
                                    qstart = buf.length() - 1;
                                    qlimit = qstart + 1;
                                }
                                try {
                                    UnicodeMatcher m = new StringMatcher(buf.toString(), qstart, qlimit, 0, parser.curData);
                                    int min = 0;
                                    int max = AnnualTimeZoneRule.MAX_YEAR;
                                    switch (c) {
                                        case Opcodes.OP_PACKED_SWITCH /*43*/:
                                            min = 1;
                                            break;
                                        case UScript.BATAK /*63*/:
                                            min = 0;
                                            max = 1;
                                            break;
                                    }
                                    UnicodeMatcher quantifier = new Quantifier(m, min, max);
                                    buf.setLength(qstart);
                                    buf.append(parser.generateStandInFor(quantifier));
                                    break;
                                } catch (Throwable e) {
                                    String precontext;
                                    String postContext;
                                    if (pos < 50) {
                                        precontext = rule.substring(0, pos);
                                    } else {
                                        precontext = "..." + rule.substring(pos - 50, pos);
                                    }
                                    if (limit - pos <= 50) {
                                        postContext = rule.substring(pos, limit);
                                    } else {
                                        postContext = rule.substring(pos, pos + 50) + "...";
                                    }
                                    throw new IllegalIcuArgumentException("Failure in rule: " + precontext + "$$$" + postContext).initCause(e);
                                }
                            case Opcodes.OP_CMPG_FLOAT /*46*/:
                                buf.append(parser.getDotStandIn());
                                break;
                            case NodeFilter.SHOW_PROCESSING_INSTRUCTION /*64*/:
                                if (this.cursorOffset >= 0) {
                                    if (this.cursorOffset <= 0) {
                                        if (this.cursor != 0 || buf.length() != 0) {
                                            if (this.cursor >= 0) {
                                                TransliteratorParser.syntaxError("Misplaced " + c, rule, start);
                                                break;
                                            }
                                            this.cursorOffsetPos = buf.length();
                                            this.cursorOffset = 1;
                                            break;
                                        }
                                        this.cursorOffset = -1;
                                        break;
                                    }
                                    if (buf.length() != this.cursorOffsetPos || this.cursor >= 0) {
                                        TransliteratorParser.syntaxError("Misplaced " + c, rule, start);
                                    }
                                    this.cursorOffset++;
                                    break;
                                }
                                if (buf.length() > 0) {
                                    TransliteratorParser.syntaxError("Misplaced " + c, rule, start);
                                }
                                this.cursorOffset--;
                                break;
                                break;
                            case Opcodes.OP_IPUT_CHAR /*94*/:
                                if (buf.length() == 0 && !this.anchorStart) {
                                    this.anchorStart = true;
                                    break;
                                }
                                TransliteratorParser.syntaxError("Misplaced anchor start", rule, start);
                                break;
                            case Opcodes.OP_NEG_INT /*123*/:
                                if (this.ante >= 0) {
                                    TransliteratorParser.syntaxError("Multiple ante contexts", rule, start);
                                }
                                this.ante = buf.length();
                                break;
                            case Opcodes.OP_NOT_INT /*124*/:
                                if (this.cursor >= 0) {
                                    TransliteratorParser.syntaxError("Multiple cursors", rule, start);
                                }
                                this.cursor = buf.length();
                                break;
                            case Opcodes.OP_NEG_LONG /*125*/:
                                if (this.post >= 0) {
                                    TransliteratorParser.syntaxError("Multiple post contexts", rule, start);
                                }
                                this.post = buf.length();
                                break;
                            default:
                                if (c >= '!' && c <= TransliteratorParser.FWDREV_RULE_OP && ((c < '0' || c > '9') && ((c < 'A' || c > 'Z') && (c < 'a' || c > 'z')))) {
                                    TransliteratorParser.syntaxError("Unquoted " + c, rule, start);
                                }
                                buf.append(c);
                                break;
                        }
                        pos2 = pos;
                    }
                } else if (!isSegment) {
                    return pos;
                } else {
                    TransliteratorParser.syntaxError("Unclosed segment", rule, start);
                    return pos;
                }
            }
            return pos2;
        }

        void removeContext() {
            this.text = this.text.substring(this.ante < 0 ? 0 : this.ante, this.post < 0 ? this.text.length() : this.post);
            this.post = -1;
            this.ante = -1;
            this.anchorEnd = false;
            this.anchorStart = false;
        }

        public boolean isValidOutput(TransliteratorParser parser) {
            int i = 0;
            while (i < this.text.length()) {
                int c = UTF16.charAt(this.text, i);
                i += UTF16.getCharCount(c);
                if (!parser.parseData.isReplacer(c)) {
                    return false;
                }
            }
            return true;
        }

        public boolean isValidInput(TransliteratorParser parser) {
            int i = 0;
            while (i < this.text.length()) {
                int c = UTF16.charAt(this.text, i);
                i += UTF16.getCharCount(c);
                if (!parser.parseData.isMatcher(c)) {
                    return false;
                }
            }
            return true;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.text.TransliteratorParser.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.text.TransliteratorParser.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.icu.text.TransliteratorParser.<clinit>():void");
    }

    public TransliteratorParser() {
        this.dotStandIn = -1;
    }

    public void parse(String rules, int dir) {
        parseRules(new RuleArray(new String[]{rules}), dir);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    void parseRules(RuleBody ruleArray, int dir) {
        boolean parsingIDs = true;
        int ruleCount = 0;
        this.dataVector = new ArrayList();
        this.idBlockVector = new ArrayList();
        this.curData = null;
        this.direction = dir;
        this.compoundFilter = null;
        this.variablesVector = new ArrayList();
        this.variableNames = new HashMap();
        this.parseData = new ParseData(null);
        List<RuntimeException> errors = new ArrayList();
        int errorCount = 0;
        ruleArray.reset();
        StringBuilder idBlockResult = new StringBuilder();
        this.compoundFilter = null;
        int compoundFilterOffset = -1;
        loop0:
        while (true) {
            String rule = ruleArray.nextLine();
            if (rule == null) {
                break;
            }
            int pos;
            int limit = rule.length();
            int pos2 = 0;
            while (pos2 < limit) {
                pos = pos2 + 1;
                char c = rule.charAt(pos2);
                if (PatternProps.isWhiteSpace(c)) {
                    pos2 = pos;
                } else if (c == '#') {
                    pos = rule.indexOf("\n", pos) + 1;
                    if (pos == 0) {
                        break;
                    }
                    pos2 = pos;
                } else if (c == ';') {
                    pos2 = pos;
                } else {
                    ruleCount++;
                    pos--;
                    if ((pos + ID_TOKEN_LEN) + 1 <= limit) {
                        try {
                            if (rule.regionMatches(pos, ID_TOKEN, 0, ID_TOKEN_LEN)) {
                                pos += ID_TOKEN_LEN;
                                c = rule.charAt(pos);
                                while (PatternProps.isWhiteSpace(c) && pos < limit) {
                                    pos++;
                                    c = rule.charAt(pos);
                                }
                                int[] p = new int[]{pos};
                                if (!parsingIDs) {
                                    if (this.curData != null) {
                                        if (this.direction == 0) {
                                            this.dataVector.add(this.curData);
                                        } else {
                                            this.dataVector.add(0, this.curData);
                                        }
                                        this.curData = null;
                                    }
                                    parsingIDs = true;
                                }
                                SingleID id = TransliteratorIDParser.parseSingleID(rule, p, this.direction);
                                if (p[0] == pos || !Utility.parseChar(rule, p, END_OF_RULE)) {
                                    int[] withParens = new int[]{-1};
                                    UnicodeSet f = TransliteratorIDParser.parseGlobalFilter(rule, p, this.direction, withParens, null);
                                    if (f == null || !Utility.parseChar(rule, p, END_OF_RULE)) {
                                        syntaxError("Invalid ::ID", rule, pos);
                                    } else {
                                        if ((this.direction == 0 ? 1 : null) == (withParens[0] == 0 ? 1 : null)) {
                                            if (this.compoundFilter != null) {
                                                syntaxError("Multiple global filters", rule, pos);
                                            }
                                            this.compoundFilter = f;
                                            compoundFilterOffset = ruleCount;
                                        }
                                    }
                                } else if (this.direction == 0) {
                                    idBlockResult.append(id.canonID).append(END_OF_RULE);
                                } else {
                                    idBlockResult.insert(0, id.canonID + END_OF_RULE);
                                }
                                pos = p[0];
                                pos2 = pos;
                            }
                        } catch (Throwable e) {
                            if (errorCount == 30) {
                                break loop0;
                                IllegalIcuArgumentException icuEx = new IllegalIcuArgumentException("\nMore than 30 errors; further messages squelched");
                                icuEx.initCause(e);
                                errors.add(icuEx);
                            } else {
                                e.fillInStackTrace();
                                errors.add(e);
                                errorCount++;
                                pos = ruleEnd(rule, pos, limit) + 1;
                            }
                        }
                    }
                    if (parsingIDs) {
                        if (this.direction == 0) {
                            this.idBlockVector.add(idBlockResult.toString());
                        } else {
                            this.idBlockVector.add(0, idBlockResult.toString());
                        }
                        idBlockResult.delete(0, idBlockResult.length());
                        parsingIDs = false;
                        this.curData = new Data();
                        setVariableRange(61440, 63743);
                    }
                    if (resemblesPragma(rule, pos, limit)) {
                        int ppp = parsePragma(rule, pos, limit);
                        if (ppp < 0) {
                            syntaxError("Unrecognized pragma", rule, pos);
                        }
                        pos = ppp;
                    } else {
                        pos = parseRule(rule, pos, limit);
                    }
                    pos2 = pos;
                }
            }
            pos = pos2;
        }
        if (!parsingIDs || idBlockResult.length() <= 0) {
            if (!(parsingIDs || this.curData == null)) {
                if (this.direction == 0) {
                    this.dataVector.add(this.curData);
                } else {
                    this.dataVector.add(0, this.curData);
                }
            }
        } else if (this.direction == 0) {
            this.idBlockVector.add(idBlockResult.toString());
        } else {
            this.idBlockVector.add(0, idBlockResult.toString());
        }
        int i = 0;
        while (true) {
            if (i >= this.dataVector.size()) {
                break;
            }
            Data data = (Data) this.dataVector.get(i);
            data.variables = new Object[this.variablesVector.size()];
            this.variablesVector.toArray(data.variables);
            data.variableNames = new HashMap();
            data.variableNames.putAll(this.variableNames);
            i++;
        }
        this.variablesVector = null;
        try {
            if (this.compoundFilter != null) {
                if (this.direction != 0 || compoundFilterOffset == 1) {
                    int i2 = this.direction;
                    if (r0 == 1) {
                    }
                }
                throw new IllegalIcuArgumentException("Compound filters misplaced");
            }
            i = 0;
            while (true) {
                if (i >= this.dataVector.size()) {
                    break;
                }
                ((Data) this.dataVector.get(i)).ruleSet.freeze();
                i++;
            }
            if (this.idBlockVector.size() == 1) {
                if (((String) this.idBlockVector.get(0)).length() == 0) {
                    this.idBlockVector.remove(0);
                }
            }
        } catch (IllegalArgumentException e2) {
            e2.fillInStackTrace();
            errors.add(e2);
        }
        if (errors.size() != 0) {
            for (i = errors.size() - 1; i > 0; i--) {
                RuntimeException previous = (RuntimeException) errors.get(i - 1);
                while (previous.getCause() != null) {
                    previous = (RuntimeException) previous.getCause();
                }
                previous.initCause((Throwable) errors.get(i));
            }
            throw ((RuntimeException) errors.get(0));
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private int parseRule(String rule, int pos, int limit) {
        int start = pos;
        char c = '\u0000';
        this.segmentStandins = new StringBuffer();
        this.segmentObjects = new ArrayList();
        RuleHalf ruleHalf = new RuleHalf();
        ruleHalf = new RuleHalf();
        this.undefinedVariableName = null;
        pos = ruleHalf.parse(rule, pos, limit, this);
        if (pos != limit) {
            String str = OPERATORS;
            pos--;
            c = rule.charAt(pos);
        }
        syntaxError("No operator pos=" + pos, rule, start);
        pos++;
        if (c == '<' && pos < limit && rule.charAt(pos) == FORWARD_RULE_OP) {
            pos++;
            c = FWDREV_RULE_OP;
        }
        switch (c) {
            case '\u2190':
                c = REVERSE_RULE_OP;
                break;
            case '\u2192':
                c = FORWARD_RULE_OP;
                break;
            case '\u2194':
                c = FWDREV_RULE_OP;
                break;
        }
        pos = ruleHalf.parse(rule, pos, limit, this);
        if (pos < limit) {
            pos--;
            if (rule.charAt(pos) == END_OF_RULE) {
                pos++;
            } else {
                syntaxError("Unquoted operator", rule, start);
            }
        }
        if (c == '=') {
            if (this.undefinedVariableName == null) {
                syntaxError("Missing '$' or duplicate definition", rule, start);
            }
            if (!(ruleHalf.text.length() == 1 && ruleHalf.text.charAt(0) == this.variableLimit)) {
                syntaxError("Malformed LHS", rule, start);
            }
            if (ruleHalf.anchorStart || ruleHalf.anchorEnd || ruleHalf.anchorStart || ruleHalf.anchorEnd) {
                syntaxError("Malformed variable def", rule, start);
            }
            int n = ruleHalf.text.length();
            Object value = new char[n];
            ruleHalf.text.getChars(0, n, value, 0);
            this.variableNames.put(this.undefinedVariableName, value);
            this.variableLimit = (char) (this.variableLimit + 1);
            return pos;
        }
        int i;
        RuleHalf right;
        RuleHalf left;
        UnicodeMatcher[] unicodeMatcherArr;
        if (this.undefinedVariableName != null) {
            syntaxError("Undefined variable $" + this.undefinedVariableName, rule, start);
        }
        if (this.segmentStandins.length() > this.segmentObjects.size()) {
            syntaxError("Undefined segment reference", rule, start);
        }
        for (i = 0; i < this.segmentStandins.length(); i++) {
            if (this.segmentStandins.charAt(i) == '\u0000') {
                syntaxError("Internal error", rule, start);
            }
        }
        for (i = 0; i < this.segmentObjects.size(); i++) {
            if (this.segmentObjects.get(i) == null) {
                syntaxError("Internal error", rule, start);
            }
        }
        if (c != '~') {
            if ((this.direction == 0 ? 1 : null) != (c == '>' ? 1 : null)) {
                return pos;
            }
        }
        if (this.direction == 1) {
            RuleHalf temp = ruleHalf;
            right = ruleHalf;
            left = ruleHalf;
        }
        if (c == '~') {
            right.removeContext();
            left.cursor = -1;
            left.cursorOffset = 0;
        }
        if (left.ante < 0) {
            left.ante = 0;
        }
        if (left.post < 0) {
            left.post = left.text.length();
        }
        if (right.ante < 0 && right.post < 0 && left.cursor < 0 && ((right.cursorOffset == 0 || right.cursor >= 0) && !right.anchorStart && !right.anchorEnd && left.isValidInput(this) && right.isValidOutput(this))) {
            if (left.ante > left.post) {
            }
            unicodeMatcherArr = null;
            if (this.segmentObjects.size() > 0) {
                unicodeMatcherArr = new UnicodeMatcher[this.segmentObjects.size()];
                this.segmentObjects.toArray(unicodeMatcherArr);
            }
            this.curData.ruleSet.addRule(new TransliterationRule(left.text, left.ante, left.post, right.text, right.cursor, right.cursorOffset, unicodeMatcherArr, left.anchorStart, left.anchorEnd, this.curData));
            return pos;
        }
        syntaxError("Malformed rule", rule, start);
        unicodeMatcherArr = null;
        if (this.segmentObjects.size() > 0) {
            unicodeMatcherArr = new UnicodeMatcher[this.segmentObjects.size()];
            this.segmentObjects.toArray(unicodeMatcherArr);
        }
        this.curData.ruleSet.addRule(new TransliterationRule(left.text, left.ante, left.post, right.text, right.cursor, right.cursorOffset, unicodeMatcherArr, left.anchorStart, left.anchorEnd, this.curData));
        return pos;
    }

    private void setVariableRange(int start, int end) {
        if (start > end || start < 0 || end > DexFormat.MAX_TYPE_IDX) {
            throw new IllegalIcuArgumentException("Invalid variable range " + start + ", " + end);
        }
        this.curData.variablesBase = (char) start;
        if (this.dataVector.size() == 0) {
            this.variableNext = (char) start;
            this.variableLimit = (char) (end + 1);
        }
    }

    private void checkVariableRange(int ch, String rule, int start) {
        if (ch >= this.curData.variablesBase && ch < this.variableLimit) {
            syntaxError("Variable range character in rule", rule, start);
        }
    }

    private void pragmaMaximumBackup(int backup) {
        throw new IllegalIcuArgumentException("use maximum backup pragma not implemented yet");
    }

    private void pragmaNormalizeRules(Mode mode) {
        throw new IllegalIcuArgumentException("use normalize rules pragma not implemented yet");
    }

    static boolean resemblesPragma(String rule, int pos, int limit) {
        return Utility.parsePattern(rule, pos, limit, "use ", null) >= 0;
    }

    private int parsePragma(String rule, int pos, int limit) {
        int[] array = new int[ID_TOKEN_LEN];
        pos += 4;
        int p = Utility.parsePattern(rule, pos, limit, "~variable range # #~;", array);
        if (p >= 0) {
            setVariableRange(array[0], array[1]);
            return p;
        }
        p = Utility.parsePattern(rule, pos, limit, "~maximum backup #~;", array);
        if (p >= 0) {
            pragmaMaximumBackup(array[0]);
            return p;
        }
        p = Utility.parsePattern(rule, pos, limit, "~nfd rules~;", null);
        if (p >= 0) {
            pragmaNormalizeRules(Normalizer.NFD);
            return p;
        }
        p = Utility.parsePattern(rule, pos, limit, "~nfc rules~;", null);
        if (p < 0) {
            return -1;
        }
        pragmaNormalizeRules(Normalizer.NFC);
        return p;
    }

    static final void syntaxError(String msg, String rule, int start) {
        throw new IllegalIcuArgumentException(msg + " in \"" + Utility.escape(rule.substring(start, ruleEnd(rule, start, rule.length()))) + '\"');
    }

    static final int ruleEnd(String rule, int start, int limit) {
        int end = Utility.quotedIndexOf(rule, start, limit, ";");
        if (end < 0) {
            return limit;
        }
        return end;
    }

    private final char parseSet(String rule, ParsePosition pos) {
        UnicodeSet set = new UnicodeSet(rule, pos, this.parseData);
        if (this.variableNext >= this.variableLimit) {
            throw new RuntimeException("Private use variables exhausted");
        }
        set.compact();
        return generateStandInFor(set);
    }

    char generateStandInFor(Object obj) {
        for (int i = 0; i < this.variablesVector.size(); i++) {
            if (this.variablesVector.get(i) == obj) {
                return (char) (this.curData.variablesBase + i);
            }
        }
        if (this.variableNext >= this.variableLimit) {
            throw new RuntimeException("Variable range exhausted");
        }
        this.variablesVector.add(obj);
        char c = this.variableNext;
        this.variableNext = (char) (c + 1);
        return (char) c;
    }

    public char getSegmentStandin(int seg) {
        if (this.segmentStandins.length() < seg) {
            this.segmentStandins.setLength(seg);
        }
        char c = this.segmentStandins.charAt(seg - 1);
        if (c != '\u0000') {
            return c;
        }
        if (this.variableNext >= this.variableLimit) {
            throw new RuntimeException("Variable range exhausted");
        }
        char c2 = this.variableNext;
        this.variableNext = (char) (c2 + 1);
        c = (char) c2;
        this.variablesVector.add(null);
        this.segmentStandins.setCharAt(seg - 1, c);
        return c;
    }

    public void setSegmentObject(int seg, StringMatcher obj) {
        while (this.segmentObjects.size() < seg) {
            this.segmentObjects.add(null);
        }
        int index = getSegmentStandin(seg) - this.curData.variablesBase;
        if (this.segmentObjects.get(seg - 1) == null && this.variablesVector.get(index) == null) {
            this.segmentObjects.set(seg - 1, obj);
            this.variablesVector.set(index, obj);
            return;
        }
        throw new RuntimeException();
    }

    char getDotStandIn() {
        if (this.dotStandIn == -1) {
            this.dotStandIn = generateStandInFor(new UnicodeSet(DOT_SET));
        }
        return (char) this.dotStandIn;
    }

    private void appendVariableDef(String name, StringBuffer buf) {
        char[] ch = (char[]) this.variableNames.get(name);
        if (ch != null) {
            buf.append(ch);
        } else if (this.undefinedVariableName == null) {
            this.undefinedVariableName = name;
            if (this.variableNext >= this.variableLimit) {
                throw new RuntimeException("Private use variables exhausted");
            }
            char c = (char) (this.variableLimit - 1);
            this.variableLimit = c;
            buf.append(c);
        } else {
            throw new IllegalIcuArgumentException("Undefined variable $" + name);
        }
    }
}
