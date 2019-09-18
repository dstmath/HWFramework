package android.icu.text;

import android.icu.impl.IllegalIcuArgumentException;
import android.icu.impl.PatternProps;
import android.icu.impl.Utility;
import android.icu.lang.UCharacter;
import android.icu.text.Normalizer;
import android.icu.text.RuleBasedTransliterator;
import android.icu.text.TransliteratorIDParser;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class TransliteratorParser {
    private static final char ALT_FORWARD_RULE_OP = '→';
    private static final char ALT_FUNCTION = '∆';
    private static final char ALT_FWDREV_RULE_OP = '↔';
    private static final char ALT_REVERSE_RULE_OP = '←';
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
    private static final String HALF_ENDERS = "=><←→↔;";
    private static final String ID_TOKEN = "::";
    private static final int ID_TOKEN_LEN = 2;
    /* access modifiers changed from: private */
    public static UnicodeSet ILLEGAL_FUNC = new UnicodeSet("[\\^\\(\\.\\*\\+\\?\\{\\}\\|\\@]");
    /* access modifiers changed from: private */
    public static UnicodeSet ILLEGAL_SEG = new UnicodeSet("[\\{\\}\\|\\@]");
    /* access modifiers changed from: private */
    public static UnicodeSet ILLEGAL_TOP = new UnicodeSet("[\\)]");
    private static final char KLEENE_STAR = '*';
    private static final char ONE_OR_MORE = '+';
    private static final String OPERATORS = "=><←→↔";
    private static final char QUOTE = '\'';
    private static final char REVERSE_RULE_OP = '<';
    private static final char RULE_COMMENT_CHAR = '#';
    private static final char SEGMENT_CLOSE = ')';
    private static final char SEGMENT_OPEN = '(';
    private static final char VARIABLE_DEF_OP = '=';
    private static final char ZERO_OR_ONE = '?';
    public UnicodeSet compoundFilter;
    /* access modifiers changed from: private */
    public RuleBasedTransliterator.Data curData;
    public List<RuleBasedTransliterator.Data> dataVector;
    private int direction;
    private int dotStandIn = -1;
    public List<String> idBlockVector;
    /* access modifiers changed from: private */
    public ParseData parseData;
    private List<StringMatcher> segmentObjects;
    private StringBuffer segmentStandins;
    private String undefinedVariableName;
    private char variableLimit;
    /* access modifiers changed from: private */
    public Map<String, char[]> variableNames;
    private char variableNext;
    /* access modifiers changed from: private */
    public List<Object> variablesVector;

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

        public String parseReference(String text, ParsePosition pos, int limit) {
            int start = pos.getIndex();
            int i = start;
            while (i < limit) {
                char c = text.charAt(i);
                if ((i == start && !UCharacter.isUnicodeIdentifierStart(c)) || !UCharacter.isUnicodeIdentifierPart(c)) {
                    break;
                }
                i++;
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

    private static class RuleArray extends RuleBody {
        String[] array;
        int i = 0;

        public RuleArray(String[] array2) {
            super();
            this.array = array2;
        }

        public String handleNextLine() {
            if (this.i >= this.array.length) {
                return null;
            }
            String[] strArr = this.array;
            int i2 = this.i;
            this.i = i2 + 1;
            return strArr[i2];
        }

        public void reset() {
            this.i = 0;
        }
    }

    private static abstract class RuleBody {
        /* access modifiers changed from: package-private */
        public abstract String handleNextLine();

        /* access modifiers changed from: package-private */
        public abstract void reset();

        private RuleBody() {
        }

        /* access modifiers changed from: package-private */
        public String nextLine() {
            String s;
            String s2 = handleNextLine();
            if (s2 == null || s2.length() <= 0 || s2.charAt(s2.length() - 1) != '\\') {
                return s2;
            }
            StringBuilder b = new StringBuilder(s2);
            do {
                b.deleteCharAt(b.length() - 1);
                s = handleNextLine();
                if (s != null) {
                    b.append(s);
                    if (s.length() <= 0) {
                        break;
                    }
                } else {
                    break;
                }
            } while (s.charAt(s.length() - 1) == '\\');
            return b.toString();
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
            int pos2 = parseSection(rule, pos, limit, parser, buf, TransliteratorParser.ILLEGAL_TOP, false);
            this.text = buf.toString();
            if (this.cursorOffset > 0 && this.cursor != this.cursorOffsetPos) {
                TransliteratorParser.syntaxError("Misplaced |", rule, start);
            }
            return pos2;
        }

        /* JADX WARNING: Can't fix incorrect switch cases order */
        /* JADX WARNING: Code restructure failed: missing block: B:116:0x0228, code lost:
            if (r45 == false) goto L_0x0237;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:118:0x022e, code lost:
            if (r43.length() != r5) goto L_0x0237;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:119:0x0230, code lost:
            android.icu.text.TransliteratorParser.syntaxError("Misplaced quantifier", r10, r15);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:121:0x023b, code lost:
            if (r43.length() != r6) goto L_0x0246;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:122:0x023d, code lost:
            r0 = r17;
            r8 = r6;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:123:0x0240, code lost:
            r27 = r0;
            r24 = r8;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:125:0x024a, code lost:
            if (r43.length() != r4) goto L_0x0250;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:126:0x024c, code lost:
            r0 = r18;
            r8 = r4;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:127:0x0250, code lost:
            r0 = r43.length() - 1;
            r27 = r0;
            r24 = r0 + 1;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:129:?, code lost:
            r21 = new android.icu.text.StringMatcher(r43.toString(), r27, r24, 0, android.icu.text.TransliteratorParser.access$100(r42));
         */
        /* JADX WARNING: Code restructure failed: missing block: B:130:0x026f, code lost:
            r19 = 0;
            r20 = Integer.MAX_VALUE;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:131:0x0278, code lost:
            if (r2 == '+') goto L_0x0288;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:133:0x027c, code lost:
            if (r2 == '?') goto L_0x0283;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:134:0x027e, code lost:
            r1 = r20;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:135:0x0283, code lost:
            r19 = 0;
            r20 = 1;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:136:0x0288, code lost:
            r19 = 1;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:137:0x028b, code lost:
            r28 = r2;
            r0 = new android.icu.text.Quantifier(r21, r19, r1);
            r13.setLength(r27);
            r29 = r1;
            r13.append(r12.generateStandInFor(r0));
         */
        /* JADX WARNING: Code restructure failed: missing block: B:138:0x02a3, code lost:
            r0 = move-exception;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:139:0x02a4, code lost:
            r28 = r2;
            r2 = r27;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:140:0x02aa, code lost:
            if (r3 < 50) goto L_0x02ac;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:141:0x02ac, code lost:
            r8 = r10.substring(0, r3);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:142:0x02b2, code lost:
            r8 = "..." + r10.substring(r3 - 50, r3);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:143:0x02c9, code lost:
            r1 = r8;
            r30 = r2;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:144:0x02d0, code lost:
            if ((r11 - r3) <= 50) goto L_0x02d2;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:145:0x02d2, code lost:
            r2 = r10.substring(r3, r11);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:146:0x02d7, code lost:
            r2 = r10.substring(r3, r3 + 50) + "...";
         */
        /* JADX WARNING: Code restructure failed: missing block: B:147:0x02ee, code lost:
            r31 = r3;
            r3 = new java.lang.StringBuilder();
            r32 = r4;
            r3.append("Failure in rule: ");
            r3.append(r1);
            r3.append("$$$");
            r3.append(r2);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:148:0x0314, code lost:
            throw new android.icu.impl.IllegalIcuArgumentException(r3.toString()).initCause((java.lang.Throwable) r0);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:79:0x015c, code lost:
            r22 = r4;
            r23 = r5;
            r24 = r6;
            r14 = r11;
            r37 = r15;
            r0 = true;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:80:0x0166, code lost:
            r11 = r3;
            r15 = r7;
         */
        private int parseSection(String rule, int pos, int limit, TransliteratorParser parser, StringBuffer buf, UnicodeSet illegal, boolean isSegment) {
            int pos2;
            int i;
            int quoteLimit;
            int bufStart;
            int varLimit;
            int[] iref;
            int i2;
            int start;
            boolean z;
            int start2;
            int pos3;
            boolean z2;
            int escaped;
            int pos4;
            ParsePosition pp;
            int[] iref2;
            int pos5;
            boolean z3;
            int varLimit2;
            int bufStart2;
            int quoteLimit2;
            ParsePosition pp2;
            String str = rule;
            int pos6 = limit;
            TransliteratorParser transliteratorParser = parser;
            StringBuffer stringBuffer = buf;
            int start3 = pos;
            int varLimit3 = -1;
            int[] iref3 = new int[1];
            int start4 = buf.length();
            ParsePosition pp3 = null;
            int quoteStart = -1;
            int quoteLimit3 = -1;
            int varStart = -1;
            int escaped2 = pos;
            while (escaped2 < pos6) {
                int pos7 = escaped2 + 1;
                char c = str.charAt(escaped2);
                if (PatternProps.isWhiteSpace(c) != 0) {
                    escaped2 = pos7;
                } else {
                    if (TransliteratorParser.HALF_ENDERS.indexOf(c) >= 0) {
                        if (isSegment) {
                            TransliteratorParser.syntaxError("Unclosed segment", str, start3);
                        }
                        pos2 = pos7;
                        i = varLimit3;
                    } else {
                        if (this.anchorEnd) {
                            TransliteratorParser.syntaxError("Malformed variable reference", str, start3);
                        }
                        if (UnicodeSet.resemblesPattern(str, pos7 - 1)) {
                            if (pp3 == null) {
                                pp2 = new ParsePosition(0);
                            } else {
                                pp2 = pp3;
                            }
                            pp2.setIndex(pos7 - 1);
                            stringBuffer.append(transliteratorParser.parseSet(str, pp2));
                            pp3 = pp2;
                            escaped2 = pp2.getIndex();
                        } else if (c == '\\') {
                            if (pos7 == pos6) {
                                TransliteratorParser.syntaxError("Trailing backslash", str, start3);
                            }
                            iref3[0] = pos7;
                            int escaped3 = Utility.unescapeAt(str, iref3);
                            int pos8 = iref3[0];
                            if (escaped3 == -1) {
                                TransliteratorParser.syntaxError("Malformed escape", str, start3);
                            }
                            transliteratorParser.checkVariableRange(escaped3, str, start3);
                            UTF16.append(stringBuffer, escaped3);
                            escaped2 = pos8;
                        } else if (c == '\'') {
                            int iq = str.indexOf(39, pos7);
                            if (iq == pos7) {
                                stringBuffer.append(c);
                                escaped2 = pos7 + 1;
                            } else {
                                quoteStart = buf.length();
                                while (true) {
                                    if (iq < 0) {
                                        TransliteratorParser.syntaxError("Unterminated quote", str, start3);
                                    }
                                    stringBuffer.append(str.substring(pos7, iq));
                                    pos7 = iq + 1;
                                    if (pos7 >= pos6 || str.charAt(pos7) != '\'') {
                                        quoteLimit3 = buf.length();
                                    } else {
                                        iq = str.indexOf(39, pos7 + 1);
                                    }
                                }
                                quoteLimit3 = buf.length();
                                for (int iq2 = quoteStart; iq2 < quoteLimit3; iq2++) {
                                    transliteratorParser.checkVariableRange(stringBuffer.charAt(iq2), str, start3);
                                }
                                escaped2 = pos7;
                            }
                        } else {
                            transliteratorParser.checkVariableRange(c, str, start3);
                            if (illegal.contains((int) c)) {
                                TransliteratorParser.syntaxError("Illegal character '" + c + '\'', str, start3);
                            }
                            if (c != '$') {
                                if (c != '&') {
                                    if (c == '.') {
                                        pos5 = pos7;
                                        varLimit2 = varLimit3;
                                        bufStart2 = start4;
                                        quoteLimit2 = quoteLimit3;
                                        iref2 = iref3;
                                        z3 = true;
                                        stringBuffer.append(parser.getDotStandIn());
                                    } else if (c != '^') {
                                        if (c != 8710) {
                                            switch (c) {
                                                case '(':
                                                    int varLimit4 = varLimit3;
                                                    int bufSegStart = buf.length();
                                                    int segmentNumber = this.nextSegmentNumber;
                                                    this.nextSegmentNumber = segmentNumber + 1;
                                                    char c2 = c;
                                                    int pos9 = pos7;
                                                    int i3 = pos9;
                                                    varLimit = varLimit4;
                                                    bufStart = start4;
                                                    quoteLimit = quoteLimit3;
                                                    int segmentNumber2 = segmentNumber;
                                                    int pos10 = parseSection(str, pos9, pos6, transliteratorParser, stringBuffer, TransliteratorParser.ILLEGAL_SEG, true);
                                                    transliteratorParser.setSegmentObject(segmentNumber2, new StringMatcher(stringBuffer.substring(bufSegStart), segmentNumber2, parser.curData));
                                                    stringBuffer.setLength(bufSegStart);
                                                    stringBuffer.append(transliteratorParser.getSegmentStandin(segmentNumber2));
                                                    escaped = pos10;
                                                    z = true;
                                                    start = start3;
                                                    iref = iref3;
                                                    i2 = limit;
                                                    break;
                                                case ')':
                                                    pos2 = pos7;
                                                    i = varLimit3;
                                                    break;
                                                case '*':
                                                case '+':
                                                    break;
                                                default:
                                                    switch (c) {
                                                        case '?':
                                                            break;
                                                        case '@':
                                                            if (this.cursorOffset < 0) {
                                                                if (buf.length() > 0) {
                                                                    TransliteratorParser.syntaxError("Misplaced " + c, str, start3);
                                                                }
                                                                this.cursorOffset--;
                                                            } else if (this.cursorOffset > 0) {
                                                                if (buf.length() != this.cursorOffsetPos || this.cursor >= 0) {
                                                                    TransliteratorParser.syntaxError("Misplaced " + c, str, start3);
                                                                }
                                                                this.cursorOffset++;
                                                            } else if (this.cursor == 0 && buf.length() == 0) {
                                                                this.cursorOffset = -1;
                                                            } else if (this.cursor < 0) {
                                                                this.cursorOffsetPos = buf.length();
                                                                z2 = true;
                                                                this.cursorOffset = 1;
                                                                varLimit = varLimit3;
                                                                bufStart = start4;
                                                                quoteLimit = quoteLimit3;
                                                                i2 = pos6;
                                                                start2 = start3;
                                                                break;
                                                            } else {
                                                                TransliteratorParser.syntaxError("Misplaced " + c, str, start3);
                                                            }
                                                            break;
                                                        default:
                                                            switch (c) {
                                                                case '{':
                                                                    if (this.ante >= 0) {
                                                                        TransliteratorParser.syntaxError("Multiple ante contexts", str, start3);
                                                                    }
                                                                    this.ante = buf.length();
                                                                case '|':
                                                                    if (this.cursor >= 0) {
                                                                        TransliteratorParser.syntaxError("Multiple cursors", str, start3);
                                                                    }
                                                                    this.cursor = buf.length();
                                                                case '}':
                                                                    if (this.post >= 0) {
                                                                        TransliteratorParser.syntaxError("Multiple post contexts", str, start3);
                                                                    }
                                                                    this.post = buf.length();
                                                                default:
                                                                    if (c >= '!' && c <= '~' && ((c < '0' || c > '9') && ((c < 'A' || c > 'Z') && (c < 'a' || c > 'z')))) {
                                                                        TransliteratorParser.syntaxError("Unquoted " + c, str, start3);
                                                                    }
                                                                    stringBuffer.append(c);
                                                            }
                                                    }
                                                    break;
                                            }
                                        }
                                    } else {
                                        pos5 = pos7;
                                        varLimit2 = varLimit3;
                                        bufStart2 = start4;
                                        quoteLimit2 = quoteLimit3;
                                        iref2 = iref3;
                                        z3 = true;
                                        if (buf.length() != 0 || this.anchorStart) {
                                            TransliteratorParser.syntaxError("Misplaced anchor start", str, start3);
                                        } else {
                                            this.anchorStart = true;
                                        }
                                    }
                                    z2 = z3;
                                    start2 = start3;
                                    pos3 = pos5;
                                    iref = iref2;
                                    i2 = limit;
                                }
                                varLimit = varLimit3;
                                bufStart = start4;
                                quoteLimit = quoteLimit3;
                                int[] iref4 = iref3;
                                iref4[0] = pos7;
                                int[] iref5 = iref4;
                                TransliteratorIDParser.SingleID single = TransliteratorIDParser.parseFilterID(str, iref5);
                                if (single == null || !Utility.parseChar(str, iref5, TransliteratorParser.SEGMENT_OPEN)) {
                                    TransliteratorParser.syntaxError("Invalid function", str, start3);
                                }
                                Transliterator t = single.getInstance();
                                if (t == null) {
                                    TransliteratorParser.syntaxError("Invalid function ID", str, start3);
                                }
                                int i4 = limit;
                                i2 = i4;
                                TransliteratorIDParser.SingleID singleID = single;
                                int start5 = start3;
                                iref = iref5;
                                int pos11 = parseSection(str, iref5[0], i4, transliteratorParser, stringBuffer, TransliteratorParser.ILLEGAL_FUNC, true);
                                int bufSegStart2 = buf.length();
                                FunctionReplacer r = new FunctionReplacer(t, new StringReplacer(stringBuffer.substring(bufSegStart2), parser.curData));
                                stringBuffer.setLength(bufSegStart2);
                                stringBuffer.append(transliteratorParser.generateStandInFor(r));
                                escaped = pos11;
                                start = start5;
                                z = true;
                                boolean z4 = z;
                                pos6 = i2;
                                iref3 = iref;
                                varLimit3 = varLimit;
                                quoteLimit3 = quoteLimit;
                                start3 = start;
                                start4 = bufStart;
                            } else {
                                varLimit = varLimit3;
                                bufStart = start4;
                                quoteLimit = quoteLimit3;
                                i2 = pos6;
                                start2 = start3;
                                pos3 = pos7;
                                iref = iref3;
                                if (pos3 == i2) {
                                    z2 = true;
                                    this.anchorEnd = true;
                                } else {
                                    int r2 = UCharacter.digit(str.charAt(pos3), 10);
                                    if (r2 < 1 || r2 > 9) {
                                        start = start2;
                                        if (pp3 == null) {
                                            pp = new ParsePosition(0);
                                        } else {
                                            pp = pp3;
                                        }
                                        pp.setIndex(pos3);
                                        String name = parser.parseData.parseReference(str, pp, i2);
                                        if (name == null) {
                                            z = true;
                                            this.anchorEnd = true;
                                            pp3 = pp;
                                            escaped = pos3;
                                            boolean z42 = z;
                                            pos6 = i2;
                                            iref3 = iref;
                                            varLimit3 = varLimit;
                                            quoteLimit3 = quoteLimit;
                                            start3 = start;
                                            start4 = bufStart;
                                        } else {
                                            z = true;
                                            pos4 = pp.getIndex();
                                            varStart = buf.length();
                                            transliteratorParser.appendVariableDef(name, stringBuffer);
                                            pp3 = pp;
                                            varLimit = buf.length();
                                        }
                                    } else {
                                        iref[0] = pos3;
                                        int r3 = Utility.parseNumber(str, iref, 10);
                                        if (r3 < 0) {
                                            start = start2;
                                            TransliteratorParser.syntaxError("Undefined segment reference", str, start);
                                        } else {
                                            start = start2;
                                        }
                                        int pos12 = iref[0];
                                        stringBuffer.append(transliteratorParser.getSegmentStandin(r3));
                                        pos4 = pos12;
                                        z = true;
                                    }
                                    escaped = pos4;
                                    boolean z422 = z;
                                    pos6 = i2;
                                    iref3 = iref;
                                    varLimit3 = varLimit;
                                    quoteLimit3 = quoteLimit;
                                    start3 = start;
                                    start4 = bufStart;
                                }
                            }
                            z = z2;
                            escaped = pos3;
                            start = start2;
                            boolean z4222 = z;
                            pos6 = i2;
                            iref3 = iref;
                            varLimit3 = varLimit;
                            quoteLimit3 = quoteLimit;
                            start3 = start;
                            start4 = bufStart;
                        }
                    }
                    int i5 = start4;
                    int i6 = quoteLimit3;
                    int i7 = pos6;
                    int bufStart3 = start3;
                    int i8 = i;
                    int[] iArr = iref3;
                    return pos2;
                }
            }
            int i9 = start4;
            int i10 = quoteLimit3;
            int i11 = pos6;
            int bufStart4 = start3;
            int[] iArr2 = iref3;
            return escaped2;
        }

        /* access modifiers changed from: package-private */
        public void removeContext() {
            int i;
            String str = this.text;
            if (this.ante < 0) {
                i = 0;
            } else {
                i = this.ante;
            }
            this.text = str.substring(i, this.post < 0 ? this.text.length() : this.post);
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

    public void parse(String rules, int dir) {
        parseRules(new RuleArray(new String[]{rules}), dir);
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Removed duplicated region for block: B:126:0x0213  */
    /* JADX WARNING: Removed duplicated region for block: B:127:0x021b  */
    /* JADX WARNING: Removed duplicated region for block: B:131:0x022c A[LOOP:3: B:129:0x0224->B:131:0x022c, LOOP_END] */
    /* JADX WARNING: Removed duplicated region for block: B:136:0x025d A[Catch:{ IllegalArgumentException -> 0x02ad }] */
    /* JADX WARNING: Removed duplicated region for block: B:151:0x027e A[Catch:{ IllegalArgumentException -> 0x02ad }, LOOP:4: B:149:0x0276->B:151:0x027e, LOOP_END] */
    /* JADX WARNING: Removed duplicated region for block: B:161:0x02ba  */
    /* JADX WARNING: Removed duplicated region for block: B:170:0x02ec A[RETURN] */
    /* JADX WARNING: Removed duplicated region for block: B:171:0x02ed  */
    /* JADX WARNING: Removed duplicated region for block: B:175:0x01d8 A[SYNTHETIC] */
    public void parseRules(RuleBody ruleArray, int dir) {
        int compoundFilterOffset;
        int compoundFilterOffset2;
        int i;
        RuntimeException previous;
        int i2;
        int i3;
        int i4;
        Object obj;
        int compoundFilterOffset3;
        int compoundFilterOffset4;
        int ppp;
        int i5;
        int[] p;
        TransliteratorIDParser.SingleID id;
        int compoundFilterOffset5;
        int[] iArr;
        boolean z;
        this.dataVector = new ArrayList();
        this.idBlockVector = new ArrayList();
        Object obj2 = null;
        this.curData = null;
        this.direction = dir;
        this.compoundFilter = null;
        this.variablesVector = new ArrayList();
        this.variableNames = new HashMap();
        this.parseData = new ParseData();
        ArrayList arrayList = new ArrayList();
        ruleArray.reset();
        StringBuilder idBlockResult = new StringBuilder();
        this.compoundFilter = null;
        int errorCount = 0;
        int ruleCount = 0;
        int compoundFilterOffset6 = 1;
        int compoundFilterOffset7 = -1;
        loop0:
        while (true) {
            String rule = ruleArray.nextLine();
            int i6 = 1;
            int i7 = 0;
            if (rule == null) {
                compoundFilterOffset2 = compoundFilterOffset7;
                int i8 = errorCount;
                break;
            }
            int pos = 0;
            int limit = rule.length();
            int errorCount2 = errorCount;
            int parsingIDs = compoundFilterOffset;
            int compoundFilterOffset8 = compoundFilterOffset7;
            while (pos < limit) {
                int pos2 = pos + 1;
                char c = rule.charAt(pos);
                if (!PatternProps.isWhiteSpace(c)) {
                    if (c == '#') {
                        pos2 = rule.indexOf("\n", pos2) + 1;
                        if (pos2 == 0) {
                            break;
                        }
                    } else if (c != ';') {
                        ruleCount++;
                        int pos3 = pos2 - 1;
                        if (pos3 + 2 + i6 <= limit) {
                            if (rule.regionMatches(pos3, ID_TOKEN, i7, 2)) {
                                pos3 += 2;
                                char c2 = rule.charAt(pos3);
                                while (PatternProps.isWhiteSpace(c2) != 0 && pos3 < limit) {
                                    pos3++;
                                    try {
                                        c2 = rule.charAt(pos3);
                                    } catch (IllegalArgumentException e) {
                                        e = e;
                                        compoundFilterOffset3 = compoundFilterOffset8;
                                        compoundFilterOffset = parsingIDs;
                                        if (errorCount2 == 30) {
                                        }
                                    }
                                }
                                try {
                                    p = new int[i6];
                                    p[i7] = pos3;
                                    if (parsingIDs == 0) {
                                        if (this.curData != null) {
                                            if (this.direction == 0) {
                                                this.dataVector.add(this.curData);
                                            } else {
                                                this.dataVector.add(i7, this.curData);
                                            }
                                            this.curData = null;
                                        }
                                        parsingIDs = 1;
                                    }
                                    id = TransliteratorIDParser.parseSingleID(rule, p, this.direction);
                                    if (p[i7] != pos3) {
                                        if (Utility.parseChar(rule, p, END_OF_RULE)) {
                                            if (this.direction == 0) {
                                                idBlockResult.append(id.canonID);
                                                idBlockResult.append(END_OF_RULE);
                                            } else {
                                                idBlockResult.insert(0, id.canonID + END_OF_RULE);
                                            }
                                            compoundFilterOffset4 = compoundFilterOffset8;
                                            TransliteratorIDParser.SingleID singleID = id;
                                            compoundFilterOffset5 = compoundFilterOffset4;
                                            try {
                                                ppp = p[0];
                                                compoundFilterOffset4 = compoundFilterOffset5;
                                                pos = ppp;
                                                compoundFilterOffset8 = compoundFilterOffset4;
                                                obj = null;
                                                i7 = 0;
                                                i4 = 1;
                                            } catch (IllegalArgumentException e2) {
                                                e = e2;
                                                compoundFilterOffset4 = compoundFilterOffset5;
                                                compoundFilterOffset = parsingIDs;
                                                if (errorCount2 == 30) {
                                                }
                                            }
                                            obj2 = obj;
                                            i6 = i4;
                                        }
                                    }
                                    iArr = new int[1];
                                } catch (IllegalArgumentException e3) {
                                    e = e3;
                                    compoundFilterOffset3 = compoundFilterOffset8;
                                    compoundFilterOffset = parsingIDs;
                                    if (errorCount2 == 30) {
                                        IllegalIcuArgumentException icuEx = new IllegalIcuArgumentException("\nMore than 30 errors; further messages squelched");
                                        icuEx.initCause((Throwable) e);
                                        arrayList.add(icuEx);
                                        compoundFilterOffset2 = compoundFilterOffset3;
                                        if (compoundFilterOffset != 0) {
                                        }
                                        if (this.direction != 0) {
                                        }
                                        while (i < this.dataVector.size()) {
                                        }
                                        this.variablesVector = null;
                                        if (this.compoundFilter != null) {
                                        }
                                        while (i2 < this.dataVector.size()) {
                                        }
                                        this.idBlockVector.remove(0);
                                        if (arrayList.size() == 0) {
                                        }
                                    } else {
                                        obj = null;
                                        i7 = 0;
                                        e.fillInStackTrace();
                                        arrayList.add(e);
                                        errorCount2++;
                                        i4 = 1;
                                        parsingIDs = compoundFilterOffset;
                                        pos = ruleEnd(rule, pos3, limit) + 1;
                                        compoundFilterOffset8 = compoundFilterOffset3;
                                        obj2 = obj;
                                        i6 = i4;
                                    }
                                }
                                try {
                                    iArr[0] = -1;
                                    int[] withParens = iArr;
                                    compoundFilterOffset4 = compoundFilterOffset8;
                                    try {
                                        UnicodeSet f = TransliteratorIDParser.parseGlobalFilter(rule, p, this.direction, withParens, null);
                                        if (f == null || !Utility.parseChar(rule, p, END_OF_RULE)) {
                                            syntaxError("Invalid ::ID", rule, pos3);
                                            compoundFilterOffset5 = compoundFilterOffset4;
                                            ppp = p[0];
                                            compoundFilterOffset4 = compoundFilterOffset5;
                                            pos = ppp;
                                            compoundFilterOffset8 = compoundFilterOffset4;
                                            obj = null;
                                            i7 = 0;
                                            i4 = 1;
                                            obj2 = obj;
                                            i6 = i4;
                                        } else {
                                            boolean z2 = this.direction == 0;
                                            if (withParens[0] == 0) {
                                                TransliteratorIDParser.SingleID singleID2 = id;
                                                z = true;
                                            } else {
                                                TransliteratorIDParser.SingleID singleID3 = id;
                                                z = false;
                                            }
                                            if (z2 == z) {
                                                if (this.compoundFilter != null) {
                                                    syntaxError("Multiple global filters", rule, pos3);
                                                }
                                                this.compoundFilter = f;
                                                compoundFilterOffset5 = ruleCount;
                                                ppp = p[0];
                                                compoundFilterOffset4 = compoundFilterOffset5;
                                                pos = ppp;
                                                compoundFilterOffset8 = compoundFilterOffset4;
                                                obj = null;
                                                i7 = 0;
                                                i4 = 1;
                                                obj2 = obj;
                                                i6 = i4;
                                            }
                                            compoundFilterOffset5 = compoundFilterOffset4;
                                            ppp = p[0];
                                            compoundFilterOffset4 = compoundFilterOffset5;
                                            pos = ppp;
                                            compoundFilterOffset8 = compoundFilterOffset4;
                                            obj = null;
                                            i7 = 0;
                                            i4 = 1;
                                            obj2 = obj;
                                            i6 = i4;
                                        }
                                    } catch (IllegalArgumentException e4) {
                                        e = e4;
                                        compoundFilterOffset = parsingIDs;
                                        if (errorCount2 == 30) {
                                        }
                                    }
                                } catch (IllegalArgumentException e5) {
                                    e = e5;
                                    compoundFilterOffset3 = compoundFilterOffset8;
                                    compoundFilterOffset = parsingIDs;
                                    if (errorCount2 == 30) {
                                    }
                                }
                            }
                        }
                        compoundFilterOffset4 = compoundFilterOffset8;
                        if (parsingIDs != 0) {
                            if (this.direction == 0) {
                                this.idBlockVector.add(idBlockResult.toString());
                                i5 = 0;
                            } else {
                                i5 = 0;
                                this.idBlockVector.add(0, idBlockResult.toString());
                            }
                            idBlockResult.delete(i5, idBlockResult.length());
                            parsingIDs = 0;
                            this.curData = new RuleBasedTransliterator.Data();
                            setVariableRange(61440, 63743);
                        }
                        if (resemblesPragma(rule, pos3, limit)) {
                            ppp = parsePragma(rule, pos3, limit);
                            if (ppp < 0) {
                                syntaxError("Unrecognized pragma", rule, pos3);
                            }
                        } else {
                            ppp = parseRule(rule, pos3, limit);
                        }
                        pos = ppp;
                        compoundFilterOffset8 = compoundFilterOffset4;
                        obj = null;
                        i7 = 0;
                        i4 = 1;
                        obj2 = obj;
                        i6 = i4;
                    }
                }
                pos = pos2;
            }
            int compoundFilterOffset9 = compoundFilterOffset8;
            compoundFilterOffset6 = parsingIDs;
            obj2 = obj2;
            errorCount = errorCount2;
            compoundFilterOffset7 = compoundFilterOffset9;
        }
        if (compoundFilterOffset != 0 || idBlockResult.length() <= 0) {
            if (compoundFilterOffset == 0 && this.curData != null) {
                if (this.direction != 0) {
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
        for (i = 0; i < this.dataVector.size(); i++) {
            RuleBasedTransliterator.Data data = this.dataVector.get(i);
            data.variables = new Object[this.variablesVector.size()];
            this.variablesVector.toArray(data.variables);
            data.variableNames = new HashMap();
            data.variableNames.putAll(this.variableNames);
        }
        this.variablesVector = null;
        try {
            if (this.compoundFilter != null) {
                if (this.direction == 0) {
                    i3 = 1;
                    if (compoundFilterOffset2 == 1) {
                    }
                    throw new IllegalIcuArgumentException("Compound filters misplaced");
                }
                i3 = 1;
                if (this.direction == i3) {
                    if (compoundFilterOffset2 == ruleCount) {
                    }
                    throw new IllegalIcuArgumentException("Compound filters misplaced");
                }
            }
            for (i2 = 0; i2 < this.dataVector.size(); i2++) {
                this.dataVector.get(i2).ruleSet.freeze();
            }
            if (this.idBlockVector.size() == 1 && this.idBlockVector.get(0).length() == 0) {
                this.idBlockVector.remove(0);
            }
        } catch (IllegalArgumentException e6) {
            e6.fillInStackTrace();
            arrayList.add(e6);
        }
        if (arrayList.size() == 0) {
            for (int i9 = arrayList.size() - 1; i9 > 0; i9--) {
                Object obj3 = arrayList.get(i9 - 1);
                while (true) {
                    previous = (RuntimeException) obj3;
                    if (previous.getCause() == null) {
                        break;
                    }
                    obj3 = previous.getCause();
                }
                previous.initCause((Throwable) arrayList.get(i9));
            }
            throw ((RuntimeException) arrayList.get(0));
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:3:0x0039, code lost:
        if (OPERATORS.indexOf(r9) < 0) goto L_0x003b;
     */
    private int parseRule(String rule, int pos, int limit) {
        String str = rule;
        int i = limit;
        int start = pos;
        char operator = 0;
        this.segmentStandins = new StringBuffer();
        this.segmentObjects = new ArrayList();
        RuleHalf left = new RuleHalf();
        RuleHalf right = new RuleHalf();
        this.undefinedVariableName = null;
        int pos2 = left.parse(str, pos, i, this);
        if (pos2 != i) {
            pos2--;
            char charAt = str.charAt(pos2);
            operator = charAt;
        }
        syntaxError("No operator pos=" + pos2, str, start);
        int pos3 = pos2 + 1;
        if (operator == '<' && pos3 < i && str.charAt(pos3) == '>') {
            pos3++;
            operator = FWDREV_RULE_OP;
        }
        if (operator == 8592) {
            operator = REVERSE_RULE_OP;
        } else if (operator == 8594) {
            operator = FORWARD_RULE_OP;
        } else if (operator == 8596) {
            operator = FWDREV_RULE_OP;
        }
        int pos4 = right.parse(str, pos3, i, this);
        if (pos4 < i) {
            pos4--;
            if (str.charAt(pos4) == ';') {
                pos4++;
            } else {
                syntaxError("Unquoted operator", str, start);
            }
        }
        if (operator == '=') {
            if (this.undefinedVariableName == null) {
                syntaxError("Missing '$' or duplicate definition", str, start);
            }
            if (!(left.text.length() == 1 && left.text.charAt(0) == this.variableLimit)) {
                syntaxError("Malformed LHS", str, start);
            }
            if (left.anchorStart || left.anchorEnd || right.anchorStart || right.anchorEnd) {
                syntaxError("Malformed variable def", str, start);
            }
            int n = right.text.length();
            char[] value = new char[n];
            right.text.getChars(0, n, value, 0);
            this.variableNames.put(this.undefinedVariableName, value);
            this.variableLimit = (char) (this.variableLimit + 1);
            return pos4;
        }
        if (this.undefinedVariableName != null) {
            syntaxError("Undefined variable $" + this.undefinedVariableName, str, start);
        }
        if (this.segmentStandins.length() > this.segmentObjects.size()) {
            syntaxError("Undefined segment reference", str, start);
        }
        for (int i2 = 0; i2 < this.segmentStandins.length(); i2++) {
            if (this.segmentStandins.charAt(i2) == 0) {
                syntaxError("Internal error", str, start);
            }
        }
        for (int i3 = 0; i3 < this.segmentObjects.size(); i3++) {
            if (this.segmentObjects.get(i3) == null) {
                syntaxError("Internal error", str, start);
            }
        }
        if (operator != '~') {
            if ((this.direction == 0) != (operator == '>')) {
                return pos4;
            }
        }
        if (this.direction == 1) {
            RuleHalf temp = left;
            left = right;
            right = temp;
        }
        if (operator == '~') {
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
        if (right.ante >= 0 || right.post >= 0 || left.cursor >= 0 || ((right.cursorOffset != 0 && right.cursor < 0) || right.anchorStart || right.anchorEnd || !left.isValidInput(this) || !right.isValidOutput(this) || left.ante > left.post)) {
            syntaxError("Malformed rule", str, start);
        }
        UnicodeMatcher[] segmentsArray = null;
        if (this.segmentObjects.size() > 0) {
            segmentsArray = new UnicodeMatcher[this.segmentObjects.size()];
            this.segmentObjects.toArray(segmentsArray);
        }
        TransliterationRuleSet transliterationRuleSet = this.curData.ruleSet;
        String str2 = left.text;
        int i4 = left.ante;
        int i5 = left.post;
        String str3 = right.text;
        int i6 = right.cursor;
        int i7 = right.cursorOffset;
        boolean z = left.anchorStart;
        int i8 = start;
        boolean z2 = left.anchorEnd;
        char c = operator;
        RuleBasedTransliterator.Data data = this.curData;
        TransliterationRule transliterationRule = r10;
        TransliterationRule transliterationRule2 = new TransliterationRule(str2, i4, i5, str3, i6, i7, segmentsArray, z, z2, data);
        transliterationRuleSet.addRule(transliterationRule);
        return pos4;
    }

    private void setVariableRange(int start, int end) {
        if (start > end || start < 0 || end > 65535) {
            throw new IllegalIcuArgumentException("Invalid variable range " + start + ", " + end);
        }
        this.curData.variablesBase = (char) start;
        if (this.dataVector.size() == 0) {
            this.variableNext = (char) start;
            this.variableLimit = (char) (end + 1);
        }
    }

    /* access modifiers changed from: private */
    public void checkVariableRange(int ch, String rule, int start) {
        if (ch >= this.curData.variablesBase && ch < this.variableLimit) {
            syntaxError("Variable range character in rule", rule, start);
        }
    }

    private void pragmaMaximumBackup(int backup) {
        throw new IllegalIcuArgumentException("use maximum backup pragma not implemented yet");
    }

    private void pragmaNormalizeRules(Normalizer.Mode mode) {
        throw new IllegalIcuArgumentException("use normalize rules pragma not implemented yet");
    }

    static boolean resemblesPragma(String rule, int pos, int limit) {
        return Utility.parsePattern(rule, pos, limit, "use ", null) >= 0;
    }

    private int parsePragma(String rule, int pos, int limit) {
        int[] array = new int[2];
        int pos2 = pos + 4;
        int p = Utility.parsePattern(rule, pos2, limit, "~variable range # #~;", array);
        if (p >= 0) {
            setVariableRange(array[0], array[1]);
            return p;
        }
        int p2 = Utility.parsePattern(rule, pos2, limit, "~maximum backup #~;", array);
        if (p2 >= 0) {
            pragmaMaximumBackup(array[0]);
            return p2;
        }
        int p3 = Utility.parsePattern(rule, pos2, limit, "~nfd rules~;", null);
        if (p3 >= 0) {
            pragmaNormalizeRules(Normalizer.NFD);
            return p3;
        }
        int p4 = Utility.parsePattern(rule, pos2, limit, "~nfc rules~;", null);
        if (p4 < 0) {
            return -1;
        }
        pragmaNormalizeRules(Normalizer.NFC);
        return p4;
    }

    static final void syntaxError(String msg, String rule, int start) {
        int end = ruleEnd(rule, start, rule.length());
        throw new IllegalIcuArgumentException(msg + " in \"" + Utility.escape(rule.substring(start, end)) + '\"');
    }

    static final int ruleEnd(String rule, int start, int limit) {
        int end = Utility.quotedIndexOf(rule, start, limit, ";");
        if (end < 0) {
            return limit;
        }
        return end;
    }

    /* access modifiers changed from: private */
    public final char parseSet(String rule, ParsePosition pos) {
        UnicodeSet set = new UnicodeSet(rule, pos, this.parseData);
        if (this.variableNext < this.variableLimit) {
            set.compact();
            return generateStandInFor(set);
        }
        throw new RuntimeException("Private use variables exhausted");
    }

    /* access modifiers changed from: package-private */
    public char generateStandInFor(Object obj) {
        for (int i = 0; i < this.variablesVector.size(); i++) {
            if (this.variablesVector.get(i) == obj) {
                return (char) (this.curData.variablesBase + i);
            }
        }
        if (this.variableNext < this.variableLimit) {
            this.variablesVector.add(obj);
            char c = this.variableNext;
            this.variableNext = (char) (c + 1);
            return c;
        }
        throw new RuntimeException("Variable range exhausted");
    }

    public char getSegmentStandin(int seg) {
        if (this.segmentStandins.length() < seg) {
            this.segmentStandins.setLength(seg);
        }
        char c = this.segmentStandins.charAt(seg - 1);
        if (c != 0) {
            return c;
        }
        if (this.variableNext < this.variableLimit) {
            char c2 = this.variableNext;
            this.variableNext = (char) (c2 + 1);
            char c3 = c2;
            this.variablesVector.add(null);
            this.segmentStandins.setCharAt(seg - 1, c3);
            return c3;
        }
        throw new RuntimeException("Variable range exhausted");
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

    /* access modifiers changed from: package-private */
    public char getDotStandIn() {
        if (this.dotStandIn == -1) {
            this.dotStandIn = generateStandInFor(new UnicodeSet(DOT_SET));
        }
        return (char) this.dotStandIn;
    }

    /* access modifiers changed from: private */
    public void appendVariableDef(String name, StringBuffer buf) {
        char[] ch = this.variableNames.get(name);
        if (ch != null) {
            buf.append(ch);
        } else if (this.undefinedVariableName == null) {
            this.undefinedVariableName = name;
            if (this.variableNext < this.variableLimit) {
                char c = (char) (this.variableLimit - 1);
                this.variableLimit = c;
                buf.append(c);
                return;
            }
            throw new RuntimeException("Private use variables exhausted");
        } else {
            throw new IllegalIcuArgumentException("Undefined variable $" + name);
        }
    }
}
