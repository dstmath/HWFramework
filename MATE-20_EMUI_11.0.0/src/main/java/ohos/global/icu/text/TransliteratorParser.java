package ohos.global.icu.text;

import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import ohos.global.icu.impl.IllegalIcuArgumentException;
import ohos.global.icu.impl.PatternProps;
import ohos.global.icu.impl.Utility;
import ohos.global.icu.lang.UCharacter;
import ohos.global.icu.text.Normalizer;
import ohos.global.icu.text.RuleBasedTransliterator;
import ohos.global.icu.text.TransliteratorIDParser;

/* access modifiers changed from: package-private */
public class TransliteratorParser {
    private static final char ALT_FORWARD_RULE_OP = 8594;
    private static final char ALT_FUNCTION = 8710;
    private static final char ALT_FWDREV_RULE_OP = 8596;
    private static final char ALT_REVERSE_RULE_OP = 8592;
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
    private static UnicodeSet ILLEGAL_FUNC = new UnicodeSet("[\\^\\(\\.\\*\\+\\?\\{\\}\\|\\@]");
    private static UnicodeSet ILLEGAL_SEG = new UnicodeSet("[\\{\\}\\|\\@]");
    private static UnicodeSet ILLEGAL_TOP = new UnicodeSet("[\\)]");
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
    private RuleBasedTransliterator.Data curData;
    public List<RuleBasedTransliterator.Data> dataVector;
    private int direction;
    private int dotStandIn = -1;
    public List<String> idBlockVector;
    private ParseData parseData;
    private List<StringMatcher> segmentObjects;
    private StringBuffer segmentStandins;
    private String undefinedVariableName;
    private char variableLimit;
    private Map<String, char[]> variableNames;
    private char variableNext;
    private List<Object> variablesVector;

    /* access modifiers changed from: private */
    public class ParseData implements SymbolTable {
        private ParseData() {
        }

        @Override // ohos.global.icu.text.SymbolTable
        public char[] lookup(String str) {
            return (char[]) TransliteratorParser.this.variableNames.get(str);
        }

        @Override // ohos.global.icu.text.SymbolTable
        public UnicodeMatcher lookupMatcher(int i) {
            int i2 = i - TransliteratorParser.this.curData.variablesBase;
            if (i2 < 0 || i2 >= TransliteratorParser.this.variablesVector.size()) {
                return null;
            }
            return (UnicodeMatcher) TransliteratorParser.this.variablesVector.get(i2);
        }

        @Override // ohos.global.icu.text.SymbolTable
        public String parseReference(String str, ParsePosition parsePosition, int i) {
            int index = parsePosition.getIndex();
            int i2 = index;
            while (i2 < i) {
                char charAt = str.charAt(i2);
                if ((i2 == index && !UCharacter.isUnicodeIdentifierStart(charAt)) || !UCharacter.isUnicodeIdentifierPart(charAt)) {
                    break;
                }
                i2++;
            }
            if (i2 == index) {
                return null;
            }
            parsePosition.setIndex(i2);
            return str.substring(index, i2);
        }

        public boolean isMatcher(int i) {
            int i2 = i - TransliteratorParser.this.curData.variablesBase;
            if (i2 < 0 || i2 >= TransliteratorParser.this.variablesVector.size()) {
                return true;
            }
            return TransliteratorParser.this.variablesVector.get(i2) instanceof UnicodeMatcher;
        }

        public boolean isReplacer(int i) {
            int i2 = i - TransliteratorParser.this.curData.variablesBase;
            if (i2 < 0 || i2 >= TransliteratorParser.this.variablesVector.size()) {
                return true;
            }
            return TransliteratorParser.this.variablesVector.get(i2) instanceof UnicodeReplacer;
        }
    }

    /* access modifiers changed from: private */
    public static abstract class RuleBody {
        /* access modifiers changed from: package-private */
        public abstract String handleNextLine();

        /* access modifiers changed from: package-private */
        public abstract void reset();

        private RuleBody() {
        }

        /* access modifiers changed from: package-private */
        public String nextLine() {
            String handleNextLine;
            String handleNextLine2 = handleNextLine();
            if (handleNextLine2 == null || handleNextLine2.length() <= 0 || handleNextLine2.charAt(handleNextLine2.length() - 1) != '\\') {
                return handleNextLine2;
            }
            StringBuilder sb = new StringBuilder(handleNextLine2);
            do {
                sb.deleteCharAt(sb.length() - 1);
                handleNextLine = handleNextLine();
                if (handleNextLine != null) {
                    sb.append(handleNextLine);
                    if (handleNextLine.length() <= 0) {
                        break;
                    }
                } else {
                    break;
                }
            } while (handleNextLine.charAt(handleNextLine.length() - 1) == '\\');
            return sb.toString();
        }
    }

    /* access modifiers changed from: private */
    public static class RuleArray extends RuleBody {
        String[] array;
        int i = 0;

        public RuleArray(String[] strArr) {
            super();
            this.array = strArr;
        }

        @Override // ohos.global.icu.text.TransliteratorParser.RuleBody
        public String handleNextLine() {
            int i2 = this.i;
            String[] strArr = this.array;
            if (i2 >= strArr.length) {
                return null;
            }
            this.i = i2 + 1;
            return strArr[i2];
        }

        @Override // ohos.global.icu.text.TransliteratorParser.RuleBody
        public void reset() {
            this.i = 0;
        }
    }

    /* access modifiers changed from: private */
    public static class RuleHalf {
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

        public int parse(String str, int i, int i2, TransliteratorParser transliteratorParser) {
            StringBuffer stringBuffer = new StringBuffer();
            int parseSection = parseSection(str, i, i2, transliteratorParser, stringBuffer, TransliteratorParser.ILLEGAL_TOP, false);
            this.text = stringBuffer.toString();
            if (this.cursorOffset > 0 && this.cursor != this.cursorOffsetPos) {
                TransliteratorParser.syntaxError("Misplaced |", str, i);
            }
            return parseSection;
        }

        private int parseSection(String str, int i, int i2, TransliteratorParser transliteratorParser, StringBuffer stringBuffer, UnicodeSet unicodeSet, boolean z) {
            int i3;
            int i4;
            int i5;
            int[] iArr;
            boolean z2;
            int i6;
            boolean z3;
            int[] iArr2;
            int i7;
            int i8;
            int[] iArr3;
            int i9;
            int i10;
            int i11;
            int i12;
            int i13;
            int i14;
            int i15;
            int i16 = i2;
            int[] iArr4 = new int[1];
            int length = stringBuffer.length();
            int i17 = -1;
            ParsePosition parsePosition = null;
            int i18 = -1;
            int i19 = -1;
            int i20 = -1;
            int i21 = -1;
            int i22 = i;
            while (i22 < i16) {
                int i23 = i22 + 1;
                char charAt = str.charAt(i22);
                if (PatternProps.isWhiteSpace(charAt)) {
                    i22 = i23;
                } else {
                    if (TransliteratorParser.HALF_ENDERS.indexOf(charAt) < 0) {
                        if (this.anchorEnd) {
                            TransliteratorParser.syntaxError("Malformed variable reference", str, i);
                        }
                        int i24 = i23 - 1;
                        if (UnicodeSet.resemblesPattern(str, i24)) {
                            ParsePosition parsePosition2 = parsePosition == null ? new ParsePosition(0) : parsePosition;
                            parsePosition2.setIndex(i24);
                            stringBuffer.append(transliteratorParser.parseSet(str, parsePosition2));
                            i15 = parsePosition2.getIndex();
                            parsePosition = parsePosition2;
                        } else if (charAt == '\\') {
                            if (i23 == i16) {
                                TransliteratorParser.syntaxError("Trailing backslash", str, i);
                            }
                            iArr4[0] = i23;
                            int unescapeAt = Utility.unescapeAt(str, iArr4);
                            i15 = iArr4[0];
                            if (unescapeAt == i17) {
                                TransliteratorParser.syntaxError("Malformed escape", str, i);
                            }
                            transliteratorParser.checkVariableRange(unescapeAt, str, i);
                            UTF16.append(stringBuffer, unescapeAt);
                        } else if (charAt == '\'') {
                            int indexOf = str.indexOf(39, i23);
                            if (indexOf == i23) {
                                stringBuffer.append(charAt);
                                i22 = i23 + 1;
                            } else {
                                i20 = stringBuffer.length();
                                while (true) {
                                    if (indexOf < 0) {
                                        TransliteratorParser.syntaxError("Unterminated quote", str, i);
                                    }
                                    stringBuffer.append(str.substring(i23, indexOf));
                                    i23 = indexOf + 1;
                                    if (i23 < i16 && str.charAt(i23) == '\'') {
                                        indexOf = str.indexOf(39, i23 + 1);
                                    }
                                }
                                i19 = stringBuffer.length();
                                for (int i25 = i20; i25 < i19; i25++) {
                                    transliteratorParser.checkVariableRange(stringBuffer.charAt(i25), str, i);
                                }
                                i22 = i23;
                            }
                        } else {
                            transliteratorParser.checkVariableRange(charAt, str, i);
                            if (unicodeSet.contains(charAt)) {
                                TransliteratorParser.syntaxError("Illegal character '" + charAt + '\'', str, i);
                            }
                            if (charAt != '$') {
                                if (charAt != '&') {
                                    if (charAt != '.') {
                                        if (charAt == '^') {
                                            i7 = length;
                                            iArr2 = iArr4;
                                            i4 = -1;
                                            i8 = i18;
                                            i5 = i19;
                                            if (stringBuffer.length() != 0 || this.anchorStart) {
                                                TransliteratorParser.syntaxError("Misplaced anchor start", str, i);
                                            } else {
                                                z3 = true;
                                                this.anchorStart = true;
                                                i6 = i8;
                                                i3 = i7;
                                                iArr = iArr2;
                                            }
                                        } else if (charAt != 8710) {
                                            if (charAt != '?') {
                                                if (charAt != '@') {
                                                    switch (charAt) {
                                                        case '(':
                                                            int length2 = stringBuffer.length();
                                                            int i26 = this.nextSegmentNumber;
                                                            this.nextSegmentNumber = i26 + 1;
                                                            i3 = length;
                                                            i22 = parseSection(str, i23, i2, transliteratorParser, stringBuffer, TransliteratorParser.ILLEGAL_SEG, true);
                                                            transliteratorParser.setSegmentObject(i26, new StringMatcher(stringBuffer.substring(length2), i26, transliteratorParser.curData));
                                                            stringBuffer.setLength(length2);
                                                            stringBuffer.append(transliteratorParser.getSegmentStandin(i26));
                                                            i4 = -1;
                                                            i18 = i18;
                                                            i5 = i19;
                                                            iArr = iArr4;
                                                            z2 = true;
                                                            i16 = i2;
                                                            break;
                                                        case ')':
                                                            break;
                                                        case '*':
                                                        case '+':
                                                            break;
                                                        default:
                                                            switch (charAt) {
                                                                case '{':
                                                                    if (this.ante >= 0) {
                                                                        TransliteratorParser.syntaxError("Multiple ante contexts", str, i);
                                                                    }
                                                                    this.ante = stringBuffer.length();
                                                                    break;
                                                                case '|':
                                                                    if (this.cursor >= 0) {
                                                                        TransliteratorParser.syntaxError("Multiple cursors", str, i);
                                                                    }
                                                                    this.cursor = stringBuffer.length();
                                                                    break;
                                                                case '}':
                                                                    if (this.post >= 0) {
                                                                        TransliteratorParser.syntaxError("Multiple post contexts", str, i);
                                                                    }
                                                                    this.post = stringBuffer.length();
                                                                    break;
                                                                default:
                                                                    if (charAt >= '!' && charAt <= '~' && ((charAt < '0' || charAt > '9') && ((charAt < 'A' || charAt > 'Z') && (charAt < 'a' || charAt > 'z')))) {
                                                                        TransliteratorParser.syntaxError("Unquoted " + charAt, str, i);
                                                                    }
                                                                    stringBuffer.append(charAt);
                                                                    break;
                                                            }
                                                            i6 = i18;
                                                            i3 = length;
                                                            iArr = iArr4;
                                                            z3 = true;
                                                            i4 = -1;
                                                            i5 = i19;
                                                            break;
                                                    }
                                                    i19 = i5;
                                                    i17 = i4;
                                                    length = i3;
                                                    iArr4 = iArr;
                                                } else {
                                                    i6 = i18;
                                                    i9 = i19;
                                                    i3 = length;
                                                    iArr3 = iArr4;
                                                    int i27 = this.cursorOffset;
                                                    if (i27 < 0) {
                                                        if (stringBuffer.length() > 0) {
                                                            TransliteratorParser.syntaxError("Misplaced " + charAt, str, i);
                                                        }
                                                        this.cursorOffset--;
                                                    } else if (i27 > 0) {
                                                        if (stringBuffer.length() != this.cursorOffsetPos || this.cursor >= 0) {
                                                            TransliteratorParser.syntaxError("Misplaced " + charAt, str, i);
                                                        }
                                                        this.cursorOffset++;
                                                    } else if (this.cursor == 0 && stringBuffer.length() == 0) {
                                                        this.cursorOffset = -1;
                                                    } else if (this.cursor < 0) {
                                                        this.cursorOffsetPos = stringBuffer.length();
                                                        z3 = true;
                                                        this.cursorOffset = 1;
                                                        i4 = -1;
                                                        i5 = i9;
                                                        iArr = iArr3;
                                                        i16 = i2;
                                                    } else {
                                                        TransliteratorParser.syntaxError("Misplaced " + charAt, str, i);
                                                    }
                                                    i4 = -1;
                                                    i5 = i9;
                                                    iArr = iArr3;
                                                    z3 = true;
                                                    i16 = i2;
                                                }
                                            }
                                            i6 = i18;
                                            i9 = i19;
                                            iArr3 = iArr4;
                                            if (z) {
                                                i10 = length;
                                                if (stringBuffer.length() == i10) {
                                                    TransliteratorParser.syntaxError("Misplaced quantifier", str, i);
                                                    i4 = -1;
                                                    i3 = i10;
                                                    i5 = i9;
                                                    iArr = iArr3;
                                                    z3 = true;
                                                    i16 = i2;
                                                }
                                            } else {
                                                i10 = length;
                                            }
                                            i5 = i9;
                                            if (stringBuffer.length() == i5) {
                                                i11 = i5;
                                                i13 = i20;
                                                i12 = i6;
                                            } else {
                                                i12 = i6;
                                                if (stringBuffer.length() == i12) {
                                                    i11 = i12;
                                                    i13 = i21;
                                                } else {
                                                    i13 = stringBuffer.length() - 1;
                                                    i11 = i13 + 1;
                                                }
                                            }
                                            try {
                                                StringMatcher stringMatcher = new StringMatcher(stringBuffer.toString(), i13, i11, 0, transliteratorParser.curData);
                                                int i28 = Integer.MAX_VALUE;
                                                if (charAt == '+') {
                                                    i14 = 1;
                                                } else if (charAt != '?') {
                                                    i14 = 0;
                                                } else {
                                                    i14 = 0;
                                                    i28 = 1;
                                                }
                                                Quantifier quantifier = new Quantifier(stringMatcher, i14, i28);
                                                stringBuffer.setLength(i13);
                                                stringBuffer.append(transliteratorParser.generateStandInFor(quantifier));
                                                i6 = i12;
                                                i4 = -1;
                                                i3 = i10;
                                                iArr = iArr3;
                                                z3 = true;
                                                i16 = i2;
                                            } catch (RuntimeException e) {
                                                throw new IllegalIcuArgumentException("Failure in rule: " + (i23 < 50 ? str.substring(0, i23) : "..." + str.substring(i23 - 50, i23)) + "$$$" + (i2 - i23 <= 50 ? str.substring(i23, i2) : str.substring(i23, i23 + 50) + "...")).initCause((Throwable) e);
                                            }
                                        }
                                        z2 = z3;
                                        i22 = i23;
                                        i18 = i6;
                                        i19 = i5;
                                        i17 = i4;
                                        length = i3;
                                        iArr4 = iArr;
                                    } else {
                                        i7 = length;
                                        iArr2 = iArr4;
                                        i4 = -1;
                                        i8 = i18;
                                        i5 = i19;
                                        stringBuffer.append(transliteratorParser.getDotStandIn());
                                    }
                                    i6 = i8;
                                    i3 = i7;
                                    iArr = iArr2;
                                    z3 = true;
                                    z2 = z3;
                                    i22 = i23;
                                    i18 = i6;
                                    i19 = i5;
                                    i17 = i4;
                                    length = i3;
                                    iArr4 = iArr;
                                }
                                i4 = -1;
                                i5 = i19;
                                iArr4[0] = i23;
                                TransliteratorIDParser.SingleID parseFilterID = TransliteratorIDParser.parseFilterID(str, iArr4);
                                if (parseFilterID == null || !Utility.parseChar(str, iArr4, TransliteratorParser.SEGMENT_OPEN)) {
                                    TransliteratorParser.syntaxError("Invalid function", str, i);
                                }
                                Transliterator instance = parseFilterID.getInstance();
                                if (instance == null) {
                                    TransliteratorParser.syntaxError("Invalid function ID", str, i);
                                }
                                int length3 = stringBuffer.length();
                                i3 = length;
                                iArr = iArr4;
                                i6 = i18;
                                i22 = parseSection(str, iArr4[0], i2, transliteratorParser, stringBuffer, TransliteratorParser.ILLEGAL_FUNC, true);
                                FunctionReplacer functionReplacer = new FunctionReplacer(instance, new StringReplacer(stringBuffer.substring(length3), transliteratorParser.curData));
                                stringBuffer.setLength(length3);
                                stringBuffer.append(transliteratorParser.generateStandInFor(functionReplacer));
                            } else {
                                i6 = i18;
                                i3 = length;
                                iArr = iArr4;
                                i4 = -1;
                                i5 = i19;
                                if (i23 == i16) {
                                    z3 = true;
                                    this.anchorEnd = true;
                                    z2 = z3;
                                    i22 = i23;
                                    i18 = i6;
                                    i19 = i5;
                                    i17 = i4;
                                    length = i3;
                                    iArr4 = iArr;
                                } else {
                                    int digit = UCharacter.digit(str.charAt(i23), 10);
                                    if (digit < 1 || digit > 9) {
                                        ParsePosition parsePosition3 = parsePosition == null ? new ParsePosition(0) : parsePosition;
                                        parsePosition3.setIndex(i23);
                                        String parseReference = transliteratorParser.parseData.parseReference(str, parsePosition3, i16);
                                        if (parseReference == null) {
                                            z2 = true;
                                            this.anchorEnd = true;
                                            parsePosition = parsePosition3;
                                            i22 = i23;
                                            i18 = i6;
                                            i19 = i5;
                                            i17 = i4;
                                            length = i3;
                                            iArr4 = iArr;
                                        } else {
                                            z2 = true;
                                            int index = parsePosition3.getIndex();
                                            i21 = stringBuffer.length();
                                            transliteratorParser.appendVariableDef(parseReference, stringBuffer);
                                            i18 = stringBuffer.length();
                                            parsePosition = parsePosition3;
                                            i22 = index;
                                            i19 = i5;
                                            i17 = i4;
                                            length = i3;
                                            iArr4 = iArr;
                                        }
                                    } else {
                                        iArr[0] = i23;
                                        int parseNumber = Utility.parseNumber(str, iArr, 10);
                                        if (parseNumber < 0) {
                                            TransliteratorParser.syntaxError("Undefined segment reference", str, i);
                                        }
                                        i22 = iArr[0];
                                        stringBuffer.append(transliteratorParser.getSegmentStandin(parseNumber));
                                    }
                                }
                            }
                            i18 = i6;
                            z2 = true;
                            i19 = i5;
                            i17 = i4;
                            length = i3;
                            iArr4 = iArr;
                        }
                        i22 = i15;
                    } else if (z) {
                        TransliteratorParser.syntaxError("Unclosed segment", str, i);
                    }
                    return i23;
                }
            }
            return i22;
        }

        /* access modifiers changed from: package-private */
        public void removeContext() {
            String str = this.text;
            int i = this.ante;
            if (i < 0) {
                i = 0;
            }
            int i2 = this.post;
            if (i2 < 0) {
                i2 = this.text.length();
            }
            this.text = str.substring(i, i2);
            this.post = -1;
            this.ante = -1;
            this.anchorEnd = false;
            this.anchorStart = false;
        }

        public boolean isValidOutput(TransliteratorParser transliteratorParser) {
            int i = 0;
            while (i < this.text.length()) {
                int charAt = UTF16.charAt(this.text, i);
                i += UTF16.getCharCount(charAt);
                if (!transliteratorParser.parseData.isReplacer(charAt)) {
                    return false;
                }
            }
            return true;
        }

        public boolean isValidInput(TransliteratorParser transliteratorParser) {
            int i = 0;
            while (i < this.text.length()) {
                int charAt = UTF16.charAt(this.text, i);
                i += UTF16.getCharCount(charAt);
                if (!transliteratorParser.parseData.isMatcher(charAt)) {
                    return false;
                }
            }
            return true;
        }
    }

    public void parse(String str, int i) {
        parseRules(new RuleArray(new String[]{str}), i);
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Removed duplicated region for block: B:100:0x01bf  */
    /* JADX WARNING: Removed duplicated region for block: B:101:0x01c5  */
    /* JADX WARNING: Removed duplicated region for block: B:105:0x01d3 A[LOOP:3: B:103:0x01cb->B:105:0x01d3, LOOP_END] */
    /* JADX WARNING: Removed duplicated region for block: B:110:0x0204 A[Catch:{ IllegalArgumentException -> 0x024e }] */
    /* JADX WARNING: Removed duplicated region for block: B:122:0x0222 A[Catch:{ IllegalArgumentException -> 0x024e }, LOOP:4: B:120:0x021a->B:122:0x0222, LOOP_END] */
    /* JADX WARNING: Removed duplicated region for block: B:132:0x025b  */
    /* JADX WARNING: Removed duplicated region for block: B:141:0x028a A[RETURN] */
    /* JADX WARNING: Removed duplicated region for block: B:142:0x028b  */
    /* JADX WARNING: Removed duplicated region for block: B:144:0x0186 A[SYNTHETIC] */
    public void parseRules(RuleBody ruleBody, int i) {
        int i2;
        RuntimeException runtimeException;
        int i3;
        RuleBasedTransliterator.Data data;
        IllegalArgumentException e;
        this.dataVector = new ArrayList();
        this.idBlockVector = new ArrayList();
        RuleBasedTransliterator.Data data2 = null;
        this.curData = null;
        this.direction = i;
        this.compoundFilter = null;
        this.variablesVector = new ArrayList();
        this.variableNames = new HashMap();
        this.parseData = new ParseData();
        ArrayList arrayList = new ArrayList();
        ruleBody.reset();
        StringBuilder sb = new StringBuilder();
        this.compoundFilter = null;
        boolean z = true;
        int i4 = 0;
        int i5 = 0;
        int i6 = -1;
        loop0:
        while (true) {
            String nextLine = ruleBody.nextLine();
            if (nextLine == null) {
                break;
            }
            int length = nextLine.length();
            int i7 = i5;
            int i8 = i6;
            boolean z2 = z;
            int i9 = 0;
            while (i9 < length) {
                int i10 = i9 + 1;
                char charAt = nextLine.charAt(i9);
                if (!PatternProps.isWhiteSpace(charAt)) {
                    if (charAt == '#') {
                        i9 = nextLine.indexOf("\n", i10) + 1;
                        if (i9 == 0) {
                            break;
                        }
                    } else if (charAt != ';') {
                        i4++;
                        int i11 = i10 - 1;
                        int i12 = i11 + 2;
                        if (i12 + 1 <= length) {
                            try {
                                if (nextLine.regionMatches(i11, ID_TOKEN, 0, 2)) {
                                    try {
                                        char charAt2 = nextLine.charAt(i12);
                                        int i13 = i12;
                                        while (PatternProps.isWhiteSpace(charAt2) && i13 < length) {
                                            i13++;
                                            charAt2 = nextLine.charAt(i13);
                                        }
                                        int[] iArr = {i13};
                                        if (!z2) {
                                            if (this.curData != null) {
                                                if (this.direction == 0) {
                                                    this.dataVector.add(this.curData);
                                                } else {
                                                    this.dataVector.add(0, this.curData);
                                                }
                                                this.curData = data2;
                                            }
                                            z2 = true;
                                        }
                                        TransliteratorIDParser.SingleID parseSingleID = TransliteratorIDParser.parseSingleID(nextLine, iArr, this.direction);
                                        if (iArr[0] == i13 || !Utility.parseChar(nextLine, iArr, END_OF_RULE)) {
                                            int[] iArr2 = {-1};
                                            UnicodeSet parseGlobalFilter = TransliteratorIDParser.parseGlobalFilter(nextLine, iArr, this.direction, iArr2, null);
                                            if (parseGlobalFilter == null || !Utility.parseChar(nextLine, iArr, END_OF_RULE)) {
                                                syntaxError("Invalid ::ID", nextLine, i13);
                                            } else if ((this.direction == 0) == (iArr2[0] == 0)) {
                                                if (this.compoundFilter != null) {
                                                    syntaxError("Multiple global filters", nextLine, i13);
                                                }
                                                this.compoundFilter = parseGlobalFilter;
                                                i8 = i4;
                                            }
                                        } else if (this.direction == 0) {
                                            sb.append(parseSingleID.canonID);
                                            sb.append(END_OF_RULE);
                                        } else {
                                            sb.insert(0, parseSingleID.canonID + END_OF_RULE);
                                        }
                                        i9 = iArr[0];
                                        data2 = null;
                                    } catch (IllegalArgumentException e2) {
                                        e = e2;
                                        i11 = i12;
                                        if (i7 == 30) {
                                        }
                                    }
                                }
                            } catch (IllegalArgumentException e3) {
                                e = e3;
                                if (i7 == 30) {
                                }
                            }
                        }
                        if (z2) {
                            if (this.direction == 0) {
                                this.idBlockVector.add(sb.toString());
                            } else {
                                this.idBlockVector.add(0, sb.toString());
                            }
                            sb.delete(0, sb.length());
                            try {
                                this.curData = new RuleBasedTransliterator.Data();
                                setVariableRange(61440, 63743);
                                z2 = false;
                            } catch (IllegalArgumentException e4) {
                                e = e4;
                                z2 = false;
                                if (i7 == 30) {
                                    IllegalIcuArgumentException illegalIcuArgumentException = new IllegalIcuArgumentException("\nMore than 30 errors; further messages squelched");
                                    illegalIcuArgumentException.initCause((Throwable) e);
                                    arrayList.add(illegalIcuArgumentException);
                                    z = z2;
                                    i6 = i8;
                                    if (z) {
                                    }
                                    if (this.direction != 0) {
                                    }
                                    while (i2 < this.dataVector.size()) {
                                    }
                                    this.variablesVector = null;
                                    if (this.compoundFilter != null) {
                                    }
                                    while (i3 < this.dataVector.size()) {
                                    }
                                    this.idBlockVector.remove(0);
                                    if (arrayList.size() == 0) {
                                    }
                                } else {
                                    data2 = null;
                                    e.fillInStackTrace();
                                    arrayList.add(e);
                                    i7++;
                                    i9 = ruleEnd(nextLine, i11, length) + 1;
                                }
                            }
                        }
                        if (resemblesPragma(nextLine, i11, length)) {
                            i9 = parsePragma(nextLine, i11, length);
                            if (i9 < 0) {
                                syntaxError("Unrecognized pragma", nextLine, i11);
                            }
                        } else {
                            i9 = parseRule(nextLine, i11, length);
                        }
                        data2 = null;
                    }
                }
                i9 = i10;
            }
            z = z2;
            i6 = i8;
            i5 = i7;
        }
        if (z || sb.length() <= 0) {
            if (!z && (data = this.curData) != null) {
                if (this.direction != 0) {
                    this.dataVector.add(data);
                } else {
                    this.dataVector.add(0, data);
                }
            }
        } else if (this.direction == 0) {
            this.idBlockVector.add(sb.toString());
        } else {
            this.idBlockVector.add(0, sb.toString());
        }
        for (i2 = 0; i2 < this.dataVector.size(); i2++) {
            RuleBasedTransliterator.Data data3 = this.dataVector.get(i2);
            data3.variables = new Object[this.variablesVector.size()];
            this.variablesVector.toArray(data3.variables);
            data3.variableNames = new HashMap();
            data3.variableNames.putAll(this.variableNames);
        }
        this.variablesVector = null;
        try {
            if (this.compoundFilter != null) {
                if (this.direction != 0 || i6 == 1) {
                    if (this.direction == 1) {
                        if (i6 == i4) {
                        }
                    }
                }
                throw new IllegalIcuArgumentException("Compound filters misplaced");
            }
            for (i3 = 0; i3 < this.dataVector.size(); i3++) {
                this.dataVector.get(i3).ruleSet.freeze();
            }
            if (this.idBlockVector.size() == 1 && this.idBlockVector.get(0).length() == 0) {
                this.idBlockVector.remove(0);
            }
        } catch (IllegalArgumentException e5) {
            e5.fillInStackTrace();
            arrayList.add(e5);
        }
        if (arrayList.size() == 0) {
            for (int size = arrayList.size() - 1; size > 0; size--) {
                Object obj = arrayList.get(size - 1);
                while (true) {
                    runtimeException = (RuntimeException) obj;
                    if (runtimeException.getCause() == null) {
                        break;
                    }
                    obj = runtimeException.getCause();
                }
                runtimeException.initCause((Throwable) arrayList.get(size));
            }
            throw ((RuntimeException) arrayList.get(0));
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:3:0x0036, code lost:
        if (ohos.global.icu.text.TransliteratorParser.OPERATORS.indexOf(r9) < 0) goto L_0x003a;
     */
    private int parseRule(String str, int i, int i2) {
        char c;
        this.segmentStandins = new StringBuffer();
        this.segmentObjects = new ArrayList();
        UnicodeMatcher[] unicodeMatcherArr = null;
        RuleHalf ruleHalf = new RuleHalf();
        RuleHalf ruleHalf2 = new RuleHalf();
        this.undefinedVariableName = null;
        int parse = ruleHalf.parse(str, i, i2, this);
        if (parse != i2) {
            parse--;
            c = str.charAt(parse);
        } else {
            c = 0;
        }
        syntaxError("No operator pos=" + parse, str, i);
        int i3 = parse + 1;
        if (c == '<' && i3 < i2 && str.charAt(i3) == '>') {
            i3++;
            c = '~';
        }
        if (c == 8592) {
            c = '<';
        } else if (c == 8594) {
            c = '>';
        } else if (c == 8596) {
            c = '~';
        }
        int parse2 = ruleHalf2.parse(str, i3, i2, this);
        if (parse2 < i2) {
            parse2--;
            if (str.charAt(parse2) == ';') {
                parse2++;
            } else {
                syntaxError("Unquoted operator", str, i);
            }
        }
        if (c == '=') {
            if (this.undefinedVariableName == null) {
                syntaxError("Missing '$' or duplicate definition", str, i);
            }
            if (!(ruleHalf.text.length() == 1 && ruleHalf.text.charAt(0) == this.variableLimit)) {
                syntaxError("Malformed LHS", str, i);
            }
            if (ruleHalf.anchorStart || ruleHalf.anchorEnd || ruleHalf2.anchorStart || ruleHalf2.anchorEnd) {
                syntaxError("Malformed variable def", str, i);
            }
            int length = ruleHalf2.text.length();
            char[] cArr = new char[length];
            ruleHalf2.text.getChars(0, length, cArr, 0);
            this.variableNames.put(this.undefinedVariableName, cArr);
            this.variableLimit = (char) (this.variableLimit + 1);
            return parse2;
        }
        if (this.undefinedVariableName != null) {
            syntaxError("Undefined variable $" + this.undefinedVariableName, str, i);
        }
        if (this.segmentStandins.length() > this.segmentObjects.size()) {
            syntaxError("Undefined segment reference", str, i);
        }
        for (int i4 = 0; i4 < this.segmentStandins.length(); i4++) {
            if (this.segmentStandins.charAt(i4) == 0) {
                syntaxError("Internal error", str, i);
            }
        }
        for (int i5 = 0; i5 < this.segmentObjects.size(); i5++) {
            if (this.segmentObjects.get(i5) == null) {
                syntaxError("Internal error", str, i);
            }
        }
        if (c != '~') {
            if ((this.direction == 0) != (c == '>')) {
                return parse2;
            }
        }
        if (this.direction != 1) {
            ruleHalf2 = ruleHalf;
            ruleHalf = ruleHalf2;
        }
        if (c == '~') {
            ruleHalf.removeContext();
            ruleHalf2.cursor = -1;
            ruleHalf2.cursorOffset = 0;
        }
        if (ruleHalf2.ante < 0) {
            ruleHalf2.ante = 0;
        }
        if (ruleHalf2.post < 0) {
            ruleHalf2.post = ruleHalf2.text.length();
        }
        if (ruleHalf.ante >= 0 || ruleHalf.post >= 0 || ruleHalf2.cursor >= 0 || ((ruleHalf.cursorOffset != 0 && ruleHalf.cursor < 0) || ruleHalf.anchorStart || ruleHalf.anchorEnd || !ruleHalf2.isValidInput(this) || !ruleHalf.isValidOutput(this) || ruleHalf2.ante > ruleHalf2.post)) {
            syntaxError("Malformed rule", str, i);
        }
        if (this.segmentObjects.size() > 0) {
            unicodeMatcherArr = new UnicodeMatcher[this.segmentObjects.size()];
            this.segmentObjects.toArray(unicodeMatcherArr);
        }
        this.curData.ruleSet.addRule(new TransliterationRule(ruleHalf2.text, ruleHalf2.ante, ruleHalf2.post, ruleHalf.text, ruleHalf.cursor, ruleHalf.cursorOffset, unicodeMatcherArr, ruleHalf2.anchorStart, ruleHalf2.anchorEnd, this.curData));
        return parse2;
    }

    private void setVariableRange(int i, int i2) {
        if (i > i2 || i < 0 || i2 > 65535) {
            throw new IllegalIcuArgumentException("Invalid variable range " + i + ", " + i2);
        }
        char c = (char) i;
        this.curData.variablesBase = c;
        if (this.dataVector.size() == 0) {
            this.variableNext = c;
            this.variableLimit = (char) (i2 + 1);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void checkVariableRange(int i, String str, int i2) {
        if (i >= this.curData.variablesBase && i < this.variableLimit) {
            syntaxError("Variable range character in rule", str, i2);
        }
    }

    private void pragmaMaximumBackup(int i) {
        throw new IllegalIcuArgumentException("use maximum backup pragma not implemented yet");
    }

    private void pragmaNormalizeRules(Normalizer.Mode mode) {
        throw new IllegalIcuArgumentException("use normalize rules pragma not implemented yet");
    }

    static boolean resemblesPragma(String str, int i, int i2) {
        return Utility.parsePattern(str, i, i2, "use ", null) >= 0;
    }

    private int parsePragma(String str, int i, int i2) {
        int[] iArr = new int[2];
        int i3 = i + 4;
        int parsePattern = Utility.parsePattern(str, i3, i2, "~variable range # #~;", iArr);
        if (parsePattern >= 0) {
            setVariableRange(iArr[0], iArr[1]);
            return parsePattern;
        }
        int parsePattern2 = Utility.parsePattern(str, i3, i2, "~maximum backup #~;", iArr);
        if (parsePattern2 >= 0) {
            pragmaMaximumBackup(iArr[0]);
            return parsePattern2;
        }
        int parsePattern3 = Utility.parsePattern(str, i3, i2, "~nfd rules~;", null);
        if (parsePattern3 >= 0) {
            pragmaNormalizeRules(Normalizer.NFD);
            return parsePattern3;
        }
        int parsePattern4 = Utility.parsePattern(str, i3, i2, "~nfc rules~;", null);
        if (parsePattern4 < 0) {
            return -1;
        }
        pragmaNormalizeRules(Normalizer.NFC);
        return parsePattern4;
    }

    static final void syntaxError(String str, String str2, int i) {
        int ruleEnd = ruleEnd(str2, i, str2.length());
        throw new IllegalIcuArgumentException(str + " in \"" + Utility.escape(str2.substring(i, ruleEnd)) + '\"');
    }

    static final int ruleEnd(String str, int i, int i2) {
        int quotedIndexOf = Utility.quotedIndexOf(str, i, i2, ";");
        return quotedIndexOf < 0 ? i2 : quotedIndexOf;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private final char parseSet(String str, ParsePosition parsePosition) {
        UnicodeSet unicodeSet = new UnicodeSet(str, parsePosition, this.parseData);
        if (this.variableNext < this.variableLimit) {
            unicodeSet.compact();
            return generateStandInFor(unicodeSet);
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

    public char getSegmentStandin(int i) {
        if (this.segmentStandins.length() < i) {
            this.segmentStandins.setLength(i);
        }
        int i2 = i - 1;
        char charAt = this.segmentStandins.charAt(i2);
        if (charAt == 0) {
            charAt = this.variableNext;
            if (charAt < this.variableLimit) {
                this.variableNext = (char) (charAt + 1);
                this.variablesVector.add(null);
                this.segmentStandins.setCharAt(i2, charAt);
            } else {
                throw new RuntimeException("Variable range exhausted");
            }
        }
        return charAt;
    }

    public void setSegmentObject(int i, StringMatcher stringMatcher) {
        while (this.segmentObjects.size() < i) {
            this.segmentObjects.add(null);
        }
        int segmentStandin = getSegmentStandin(i) - this.curData.variablesBase;
        int i2 = i - 1;
        if (this.segmentObjects.get(i2) == null && this.variablesVector.get(segmentStandin) == null) {
            this.segmentObjects.set(i2, stringMatcher);
            this.variablesVector.set(segmentStandin, stringMatcher);
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
    /* access modifiers changed from: public */
    private void appendVariableDef(String str, StringBuffer stringBuffer) {
        char[] cArr = this.variableNames.get(str);
        if (cArr != null) {
            stringBuffer.append(cArr);
        } else if (this.undefinedVariableName == null) {
            this.undefinedVariableName = str;
            char c = this.variableNext;
            char c2 = this.variableLimit;
            if (c < c2) {
                char c3 = (char) (c2 - 1);
                this.variableLimit = c3;
                stringBuffer.append(c3);
                return;
            }
            throw new RuntimeException("Private use variables exhausted");
        } else {
            throw new IllegalIcuArgumentException("Undefined variable $" + str);
        }
    }
}
