package android.icu.text;

import android.icu.impl.ICUConfig;
import android.icu.impl.PatternProps;
import android.icu.impl.PatternTokenizer;
import android.icu.impl.UCharacterProperty;
import android.icu.util.Freezable;
import android.icu.util.ICUCloneNotSupportedException;
import java.util.ArrayList;
import java.util.Locale;

public final class MessagePattern implements Cloneable, Freezable<MessagePattern> {
    static final /* synthetic */ boolean -assertionsDisabled = (MessagePattern.class.desiredAssertionStatus() ^ 1);
    public static final int ARG_NAME_NOT_NUMBER = -1;
    public static final int ARG_NAME_NOT_VALID = -2;
    private static final int MAX_PREFIX_LENGTH = 24;
    public static final double NO_NUMERIC_VALUE = -1.23456789E8d;
    private static final ArgType[] argTypes = ArgType.values();
    private static final ApostropheMode defaultAposMode = ApostropheMode.valueOf(ICUConfig.get("android.icu.text.MessagePattern.ApostropheMode", "DOUBLE_OPTIONAL"));
    private ApostropheMode aposMode;
    private volatile boolean frozen;
    private boolean hasArgNames;
    private boolean hasArgNumbers;
    private String msg;
    private boolean needsAutoQuoting;
    private ArrayList<Double> numericValues;
    private ArrayList<Part> parts;

    public enum ApostropheMode {
        DOUBLE_OPTIONAL,
        DOUBLE_REQUIRED
    }

    public enum ArgType {
        NONE,
        SIMPLE,
        CHOICE,
        PLURAL,
        SELECT,
        SELECTORDINAL;

        public boolean hasPluralStyle() {
            return this == PLURAL || this == SELECTORDINAL;
        }
    }

    public static final class Part {
        private static final int MAX_LENGTH = 65535;
        private static final int MAX_VALUE = 32767;
        private final int index;
        private final char length;
        private int limitPartIndex;
        private final Type type;
        private short value;

        public enum Type {
            MSG_START,
            MSG_LIMIT,
            SKIP_SYNTAX,
            INSERT_CHAR,
            REPLACE_NUMBER,
            ARG_START,
            ARG_LIMIT,
            ARG_NUMBER,
            ARG_NAME,
            ARG_TYPE,
            ARG_STYLE,
            ARG_SELECTOR,
            ARG_INT,
            ARG_DOUBLE;

            public boolean hasNumericValue() {
                return this == ARG_INT || this == ARG_DOUBLE;
            }
        }

        /* synthetic */ Part(Type t, int i, int l, int v, Part -this4) {
            this(t, i, l, v);
        }

        private Part(Type t, int i, int l, int v) {
            this.type = t;
            this.index = i;
            this.length = (char) l;
            this.value = (short) v;
        }

        public Type getType() {
            return this.type;
        }

        public int getIndex() {
            return this.index;
        }

        public int getLength() {
            return this.length;
        }

        public int getLimit() {
            return this.index + this.length;
        }

        public int getValue() {
            return this.value;
        }

        public ArgType getArgType() {
            Type type = getType();
            if (type == Type.ARG_START || type == Type.ARG_LIMIT) {
                return MessagePattern.argTypes[this.value];
            }
            return ArgType.NONE;
        }

        public String toString() {
            String valueString = (this.type == Type.ARG_START || this.type == Type.ARG_LIMIT) ? getArgType().name() : Integer.toString(this.value);
            return this.type.name() + "(" + valueString + ")@" + this.index;
        }

        public boolean equals(Object other) {
            boolean z = true;
            if (this == other) {
                return true;
            }
            if (other == null || getClass() != other.getClass()) {
                return false;
            }
            Part o = (Part) other;
            if (!this.type.equals(o.type) || this.index != o.index || this.length != o.length || this.value != o.value) {
                z = false;
            } else if (this.limitPartIndex != o.limitPartIndex) {
                z = false;
            }
            return z;
        }

        public int hashCode() {
            return (((((this.type.hashCode() * 37) + this.index) * 37) + this.length) * 37) + this.value;
        }
    }

    public MessagePattern() {
        this.parts = new ArrayList();
        this.aposMode = defaultAposMode;
    }

    public MessagePattern(ApostropheMode mode) {
        this.parts = new ArrayList();
        this.aposMode = mode;
    }

    public MessagePattern(String pattern) {
        this.parts = new ArrayList();
        this.aposMode = defaultAposMode;
        parse(pattern);
    }

    public MessagePattern parse(String pattern) {
        preParse(pattern);
        parseMessage(0, 0, 0, ArgType.NONE);
        postParse();
        return this;
    }

    public MessagePattern parseChoiceStyle(String pattern) {
        preParse(pattern);
        parseChoiceStyle(0, 0);
        postParse();
        return this;
    }

    public MessagePattern parsePluralStyle(String pattern) {
        preParse(pattern);
        parsePluralOrSelectStyle(ArgType.PLURAL, 0, 0);
        postParse();
        return this;
    }

    public MessagePattern parseSelectStyle(String pattern) {
        preParse(pattern);
        parsePluralOrSelectStyle(ArgType.SELECT, 0, 0);
        postParse();
        return this;
    }

    public void clear() {
        if (isFrozen()) {
            throw new UnsupportedOperationException("Attempt to clear() a frozen MessagePattern instance.");
        }
        this.msg = null;
        this.hasArgNumbers = false;
        this.hasArgNames = false;
        this.needsAutoQuoting = false;
        this.parts.clear();
        if (this.numericValues != null) {
            this.numericValues.clear();
        }
    }

    public void clearPatternAndSetApostropheMode(ApostropheMode mode) {
        clear();
        this.aposMode = mode;
    }

    public boolean equals(Object other) {
        boolean z = false;
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        MessagePattern o = (MessagePattern) other;
        if (this.aposMode.equals(o.aposMode) && (this.msg != null ? !this.msg.equals(o.msg) : o.msg != null)) {
            z = this.parts.equals(o.parts);
        }
        return z;
    }

    public int hashCode() {
        return (((this.msg != null ? this.msg.hashCode() : 0) + (this.aposMode.hashCode() * 37)) * 37) + this.parts.hashCode();
    }

    public ApostropheMode getApostropheMode() {
        return this.aposMode;
    }

    boolean jdkAposMode() {
        return this.aposMode == ApostropheMode.DOUBLE_REQUIRED;
    }

    public String getPatternString() {
        return this.msg;
    }

    public boolean hasNamedArguments() {
        return this.hasArgNames;
    }

    public boolean hasNumberedArguments() {
        return this.hasArgNumbers;
    }

    public String toString() {
        return this.msg;
    }

    public static int validateArgumentName(String name) {
        if (PatternProps.isIdentifier(name)) {
            return parseArgNumber(name, 0, name.length());
        }
        return -2;
    }

    public String autoQuoteApostropheDeep() {
        if (!this.needsAutoQuoting) {
            return this.msg;
        }
        StringBuilder modified = null;
        int i = countParts();
        while (i > 0) {
            i--;
            Part part = getPart(i);
            if (part.getType() == Type.INSERT_CHAR) {
                if (modified == null) {
                    modified = new StringBuilder(this.msg.length() + 10).append(this.msg);
                }
                modified.insert(part.index, (char) part.value);
            }
        }
        if (modified == null) {
            return this.msg;
        }
        return modified.toString();
    }

    public int countParts() {
        return this.parts.size();
    }

    public Part getPart(int i) {
        return (Part) this.parts.get(i);
    }

    public Type getPartType(int i) {
        return ((Part) this.parts.get(i)).type;
    }

    public int getPatternIndex(int partIndex) {
        return ((Part) this.parts.get(partIndex)).index;
    }

    public String getSubstring(Part part) {
        int index = part.index;
        return this.msg.substring(index, part.length + index);
    }

    public boolean partSubstringMatches(Part part, String s) {
        return part.length == s.length() ? this.msg.regionMatches(part.index, s, 0, part.length) : false;
    }

    public double getNumericValue(Part part) {
        Type type = part.type;
        if (type == Type.ARG_INT) {
            return (double) part.value;
        }
        if (type == Type.ARG_DOUBLE) {
            return ((Double) this.numericValues.get(part.value)).doubleValue();
        }
        return -1.23456789E8d;
    }

    public double getPluralOffset(int pluralStart) {
        Part part = (Part) this.parts.get(pluralStart);
        if (part.type.hasNumericValue()) {
            return getNumericValue(part);
        }
        return 0.0d;
    }

    public int getLimitPartIndex(int start) {
        int limit = ((Part) this.parts.get(start)).limitPartIndex;
        if (limit < start) {
            return start;
        }
        return limit;
    }

    public Object clone() {
        if (isFrozen()) {
            return this;
        }
        return cloneAsThawed();
    }

    public MessagePattern cloneAsThawed() {
        try {
            MessagePattern newMsg = (MessagePattern) super.clone();
            newMsg.parts = (ArrayList) this.parts.clone();
            if (this.numericValues != null) {
                newMsg.numericValues = (ArrayList) this.numericValues.clone();
            }
            newMsg.frozen = false;
            return newMsg;
        } catch (Throwable e) {
            throw new ICUCloneNotSupportedException(e);
        }
    }

    public MessagePattern freeze() {
        this.frozen = true;
        return this;
    }

    public boolean isFrozen() {
        return this.frozen;
    }

    private void preParse(String pattern) {
        if (isFrozen()) {
            throw new UnsupportedOperationException("Attempt to parse(" + prefix(pattern) + ") on frozen MessagePattern instance.");
        }
        this.msg = pattern;
        this.hasArgNumbers = false;
        this.hasArgNames = false;
        this.needsAutoQuoting = false;
        this.parts.clear();
        if (this.numericValues != null) {
            this.numericValues.clear();
        }
    }

    private void postParse() {
    }

    private int parseMessage(int index, int msgStartLength, int nestingLevel, ArgType parentType) {
        if (nestingLevel > 32767) {
            throw new IndexOutOfBoundsException();
        }
        int msgStart = this.parts.size();
        addPart(Type.MSG_START, index, msgStartLength, nestingLevel);
        index += msgStartLength;
        while (index < this.msg.length()) {
            int index2 = index + 1;
            char c = this.msg.charAt(index);
            if (c == PatternTokenizer.SINGLE_QUOTE) {
                if (index2 == this.msg.length()) {
                    addPart(Type.INSERT_CHAR, index2, 0, 39);
                    this.needsAutoQuoting = true;
                    index = index2;
                } else {
                    c = this.msg.charAt(index2);
                    if (c == PatternTokenizer.SINGLE_QUOTE) {
                        index = index2 + 1;
                        addPart(Type.SKIP_SYNTAX, index2, 1, 0);
                    } else if (this.aposMode == ApostropheMode.DOUBLE_REQUIRED || c == '{' || c == '}' || ((parentType == ArgType.CHOICE && c == '|') || (parentType.hasPluralStyle() && c == '#'))) {
                        addPart(Type.SKIP_SYNTAX, index2 - 1, 1, 0);
                        index = index2;
                        while (true) {
                            index = this.msg.indexOf(39, index + 1);
                            if (index < 0) {
                                index = this.msg.length();
                                addPart(Type.INSERT_CHAR, index, 0, 39);
                                this.needsAutoQuoting = true;
                                break;
                            } else if (index + 1 >= this.msg.length() || this.msg.charAt(index + 1) != PatternTokenizer.SINGLE_QUOTE) {
                                index2 = index + 1;
                                addPart(Type.SKIP_SYNTAX, index, 1, 0);
                                index = index2;
                            } else {
                                index++;
                                addPart(Type.SKIP_SYNTAX, index, 1, 0);
                            }
                        }
                        index2 = index + 1;
                        addPart(Type.SKIP_SYNTAX, index, 1, 0);
                        index = index2;
                    } else {
                        addPart(Type.INSERT_CHAR, index2, 0, 39);
                        this.needsAutoQuoting = true;
                        index = index2;
                    }
                }
            } else if (parentType.hasPluralStyle() && c == '#') {
                addPart(Type.REPLACE_NUMBER, index2 - 1, 1, 0);
                index = index2;
            } else if (c == '{') {
                index = parseArg(index2 - 1, 1, nestingLevel);
            } else if ((nestingLevel <= 0 || c != '}') && !(parentType == ArgType.CHOICE && c == '|')) {
                index = index2;
            } else {
                int limitLength = (parentType == ArgType.CHOICE && c == '}') ? 0 : 1;
                addLimitPart(msgStart, Type.MSG_LIMIT, index2 - 1, limitLength, nestingLevel);
                if (parentType == ArgType.CHOICE) {
                    return index2 - 1;
                }
                return index2;
            }
        }
        if (nestingLevel <= 0 || (inTopLevelChoiceMessage(nestingLevel, parentType) ^ 1) == 0) {
            addLimitPart(msgStart, Type.MSG_LIMIT, index, 0, nestingLevel);
            return index;
        }
        throw new IllegalArgumentException("Unmatched '{' braces in message " + prefix());
    }

    private int parseArg(int index, int argStartLength, int nestingLevel) {
        int argStart = this.parts.size();
        ArgType argType = ArgType.NONE;
        addPart(Type.ARG_START, index, argStartLength, argType.ordinal());
        index = skipWhiteSpace(index + argStartLength);
        int nameIndex = index;
        if (index == this.msg.length()) {
            throw new IllegalArgumentException("Unmatched '{' braces in message " + prefix());
        }
        int length;
        int index2 = skipIdentifier(index);
        int number = parseArgNumber(index, index2);
        if (number >= 0) {
            length = index2 - index;
            if (length > DateTimePatternGenerator.MATCH_ALL_FIELDS_LENGTH || number > 32767) {
                throw new IndexOutOfBoundsException("Argument number too large: " + prefix(index));
            }
            this.hasArgNumbers = true;
            addPart(Type.ARG_NUMBER, index, length, number);
        } else if (number == -1) {
            length = index2 - index;
            if (length > DateTimePatternGenerator.MATCH_ALL_FIELDS_LENGTH) {
                throw new IndexOutOfBoundsException("Argument name too long: " + prefix(index));
            }
            this.hasArgNames = true;
            addPart(Type.ARG_NAME, index, length, 0);
        } else {
            throw new IllegalArgumentException("Bad argument syntax: " + prefix(index));
        }
        index2 = skipWhiteSpace(index2);
        if (index2 == this.msg.length()) {
            throw new IllegalArgumentException("Unmatched '{' braces in message " + prefix());
        }
        char c = this.msg.charAt(index2);
        if (c == '}') {
            index = index2;
        } else if (c != ',') {
            throw new IllegalArgumentException("Bad argument syntax: " + prefix(index));
        } else {
            index2 = skipWhiteSpace(index2 + 1);
            int typeIndex = index2;
            int index3 = index2;
            while (index3 < this.msg.length() && isArgTypeChar(this.msg.charAt(index3))) {
                index3++;
            }
            length = index3 - index2;
            index3 = skipWhiteSpace(index3);
            if (index3 == this.msg.length()) {
                throw new IllegalArgumentException("Unmatched '{' braces in message " + prefix());
            }
            if (length != 0) {
                c = this.msg.charAt(index3);
                if (c == ',' || c == '}') {
                    if (length > DateTimePatternGenerator.MATCH_ALL_FIELDS_LENGTH) {
                        throw new IndexOutOfBoundsException("Argument type name too long: " + prefix(index));
                    }
                    argType = ArgType.SIMPLE;
                    if (length == 6) {
                        if (isChoice(index2)) {
                            argType = ArgType.CHOICE;
                        } else if (isPlural(index2)) {
                            argType = ArgType.PLURAL;
                        } else if (isSelect(index2)) {
                            argType = ArgType.SELECT;
                        }
                    } else if (length == 13 && isSelect(index2)) {
                        if (isOrdinal(index2 + 6)) {
                            argType = ArgType.SELECTORDINAL;
                        }
                    }
                    ((Part) this.parts.get(argStart)).value = (short) argType.ordinal();
                    if (argType == ArgType.SIMPLE) {
                        addPart(Type.ARG_TYPE, index2, length, 0);
                    }
                    if (c != '}') {
                        index = index3 + 1;
                        index = argType == ArgType.SIMPLE ? parseSimpleStyle(index) : argType == ArgType.CHOICE ? parseChoiceStyle(index, nestingLevel) : parsePluralOrSelectStyle(argType, index, nestingLevel);
                    } else if (argType != ArgType.SIMPLE) {
                        throw new IllegalArgumentException("No style field for complex argument: " + prefix(index));
                    } else {
                        index = index3;
                    }
                }
            }
            throw new IllegalArgumentException("Bad argument syntax: " + prefix(index));
        }
        addLimitPart(argStart, Type.ARG_LIMIT, index, 1, argType.ordinal());
        return index + 1;
    }

    private int parseSimpleStyle(int index) {
        int start = index;
        int nestedBraces = 0;
        while (index < this.msg.length()) {
            int index2 = index + 1;
            char c = this.msg.charAt(index);
            if (c == PatternTokenizer.SINGLE_QUOTE) {
                index = this.msg.indexOf(39, index2);
                if (index < 0) {
                    throw new IllegalArgumentException("Quoted literal argument style text reaches to the end of the message: " + prefix(start));
                }
                index++;
            } else if (c == '{') {
                nestedBraces++;
                index = index2;
            } else if (c != '}') {
                index = index2;
            } else if (nestedBraces > 0) {
                nestedBraces--;
                index = index2;
            } else {
                index = index2 - 1;
                int length = index - start;
                if (length > DateTimePatternGenerator.MATCH_ALL_FIELDS_LENGTH) {
                    throw new IndexOutOfBoundsException("Argument style text too long: " + prefix(start));
                }
                addPart(Type.ARG_STYLE, start, length, 0);
                return index;
            }
        }
        throw new IllegalArgumentException("Unmatched '{' braces in message " + prefix());
    }

    private int parseChoiceStyle(int index, int nestingLevel) {
        int start = index;
        index = skipWhiteSpace(index);
        if (index == this.msg.length() || this.msg.charAt(index) == '}') {
            throw new IllegalArgumentException("Missing choice argument pattern in " + prefix());
        }
        while (true) {
            int numberIndex = index;
            index = skipDouble(index);
            int length = index - numberIndex;
            if (length == 0) {
                throw new IllegalArgumentException("Bad choice pattern syntax: " + prefix(start));
            } else if (length > DateTimePatternGenerator.MATCH_ALL_FIELDS_LENGTH) {
                throw new IndexOutOfBoundsException("Choice number too long: " + prefix(numberIndex));
            } else {
                parseDouble(numberIndex, index, true);
                index = skipWhiteSpace(index);
                if (index == this.msg.length()) {
                    throw new IllegalArgumentException("Bad choice pattern syntax: " + prefix(start));
                }
                char c = this.msg.charAt(index);
                if (c == '#' || c == '<' || c == 8804) {
                    addPart(Type.ARG_SELECTOR, index, 1, 0);
                    index = parseMessage(index + 1, 0, nestingLevel + 1, ArgType.CHOICE);
                    if (index == this.msg.length()) {
                        return index;
                    }
                    if (this.msg.charAt(index) != '}') {
                        index = skipWhiteSpace(index + 1);
                    } else if (inMessageFormatPattern(nestingLevel)) {
                        return index;
                    } else {
                        throw new IllegalArgumentException("Bad choice pattern syntax: " + prefix(start));
                    }
                }
                throw new IllegalArgumentException("Expected choice separator (#<≤) instead of '" + c + "' in choice pattern " + prefix(start));
            }
        }
    }

    /* JADX WARNING: Missing block: B:8:0x0020, code:
            if (r0 != inMessageFormatPattern(r15)) goto L_0x0057;
     */
    /* JADX WARNING: Missing block: B:10:0x0054, code:
            throw new java.lang.IllegalArgumentException("Bad " + r13.toString().toLowerCase(java.util.Locale.ENGLISH) + " pattern syntax: " + prefix(r6));
     */
    /* JADX WARNING: Missing block: B:12:0x0057, code:
            if (false != false) goto L_0x008c;
     */
    /* JADX WARNING: Missing block: B:14:0x008b, code:
            throw new java.lang.IllegalArgumentException("Missing 'other' keyword in " + r13.toString().toLowerCase(java.util.Locale.ENGLISH) + " pattern in " + prefix());
     */
    /* JADX WARNING: Missing block: B:15:0x008c, code:
            return r14;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private int parsePluralOrSelectStyle(ArgType argType, int index, int nestingLevel) {
        int start = index;
        while (true) {
            index = skipWhiteSpace(index);
            boolean eos = index == this.msg.length();
            if (!eos && this.msg.charAt(index) != '}') {
                int index2;
                int selectorIndex = index;
                int length;
                if (argType.hasPluralStyle() && this.msg.charAt(index) == '=') {
                    index2 = skipDouble(index + 1);
                    length = index2 - index;
                    if (length == 1) {
                        throw new IllegalArgumentException("Bad " + argType.toString().toLowerCase(Locale.ENGLISH) + " pattern syntax: " + prefix(start));
                    } else if (length > DateTimePatternGenerator.MATCH_ALL_FIELDS_LENGTH) {
                        throw new IndexOutOfBoundsException("Argument selector too long: " + prefix(index));
                    } else {
                        addPart(Type.ARG_SELECTOR, index, length, 0);
                        parseDouble(index + 1, index2, false);
                    }
                } else {
                    index2 = skipIdentifier(index);
                    length = index2 - index;
                    if (length == 0) {
                        throw new IllegalArgumentException("Bad " + argType.toString().toLowerCase(Locale.ENGLISH) + " pattern syntax: " + prefix(start));
                    } else if (argType.hasPluralStyle() && length == 6 && index2 < this.msg.length() && this.msg.regionMatches(index, "offset:", 0, 7)) {
                        if (true) {
                            int valueIndex = skipWhiteSpace(index2 + 1);
                            index = skipDouble(valueIndex);
                            if (index == valueIndex) {
                                throw new IllegalArgumentException("Missing value for plural 'offset:' " + prefix(start));
                            } else if (index - valueIndex > DateTimePatternGenerator.MATCH_ALL_FIELDS_LENGTH) {
                                throw new IndexOutOfBoundsException("Plural offset value too long: " + prefix(valueIndex));
                            } else {
                                parseDouble(valueIndex, index, false);
                            }
                        } else {
                            throw new IllegalArgumentException("Plural argument 'offset:' (if present) must precede key-message pairs: " + prefix(start));
                        }
                    } else if (length > DateTimePatternGenerator.MATCH_ALL_FIELDS_LENGTH) {
                        throw new IndexOutOfBoundsException("Argument selector too long: " + prefix(index));
                    } else {
                        addPart(Type.ARG_SELECTOR, index, length, 0);
                        if (this.msg.regionMatches(index, "other", 0, length)) {
                        }
                    }
                }
                index2 = skipWhiteSpace(index2);
                if (index2 != this.msg.length() && this.msg.charAt(index2) == '{') {
                    index = parseMessage(index2, 1, nestingLevel + 1, argType);
                }
            }
        }
        throw new IllegalArgumentException("No message fragment after " + argType.toString().toLowerCase(Locale.ENGLISH) + " selector: " + prefix(index));
    }

    private static int parseArgNumber(CharSequence s, int start, int limit) {
        if (start >= limit) {
            return -2;
        }
        int number;
        boolean badNumber;
        int start2 = start + 1;
        char c = s.charAt(start);
        if (c == '0') {
            if (start2 == limit) {
                return 0;
            }
            number = 0;
            badNumber = true;
        } else if ('1' > c || c > '9') {
            return -1;
        } else {
            number = c - 48;
            badNumber = false;
        }
        while (start2 < limit) {
            start = start2 + 1;
            c = s.charAt(start2);
            if ('0' > c || c > '9') {
                return -1;
            }
            if (number >= 214748364) {
                badNumber = true;
            }
            number = (number * 10) + (c - 48);
            start2 = start;
        }
        if (badNumber) {
            return -2;
        }
        return number;
    }

    private int parseArgNumber(int start, int limit) {
        return parseArgNumber(this.msg, start, limit);
    }

    /* JADX WARNING: Removed duplicated region for block: B:26:0x0074 A:{LOOP_START, PHI: r0 r1 r6 , LOOP:0: B:26:0x0074->B:25:0x006c} */
    /* JADX WARNING: Removed duplicated region for block: B:14:0x004b A:{SKIP} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void parseDouble(int start, int limit, boolean allowInfinity) {
        if (-assertionsDisabled || start < limit) {
            int value = 0;
            int isNegative = 0;
            int index = start;
            index = start + 1;
            char c = this.msg.charAt(start);
            int index2;
            if (c == '-') {
                isNegative = 1;
                if (index != limit) {
                    index2 = index + 1;
                    c = this.msg.charAt(index);
                    index = index2;
                    if (c == 8734) {
                        while (true) {
                            index2 = index;
                            if ('0' > c || c > '9') {
                                break;
                            }
                            value = (value * 10) + (c - 48);
                            if (value > isNegative + 32767) {
                                break;
                            } else if (index2 == limit) {
                                Type type = Type.ARG_INT;
                                int i = limit - start;
                                if (isNegative != 0) {
                                    value = -value;
                                }
                                addPart(type, start, i, value);
                                return;
                            } else {
                                index = index2 + 1;
                                c = this.msg.charAt(index2);
                            }
                        }
                        addArgDoublePart(Double.parseDouble(this.msg.substring(start, limit)), start, limit - start);
                        return;
                    } else if (allowInfinity && index == limit) {
                        addArgDoublePart(isNegative != 0 ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY, start, limit - start);
                        return;
                    }
                }
            }
            if (c == '+') {
                if (index != limit) {
                    index2 = index + 1;
                    c = this.msg.charAt(index);
                    index = index2;
                }
            }
            if (c == 8734) {
            }
            throw new NumberFormatException("Bad syntax for numeric value: " + this.msg.substring(start, limit));
        }
        throw new AssertionError();
    }

    static void appendReducedApostrophes(String s, int start, int limit, StringBuilder sb) {
        int doubleApos = -1;
        while (true) {
            int i = s.indexOf(39, start);
            if (i < 0 || i >= limit) {
                sb.append(s, start, limit);
            } else if (i == doubleApos) {
                sb.append(PatternTokenizer.SINGLE_QUOTE);
                start++;
                doubleApos = -1;
            } else {
                sb.append(s, start, i);
                start = i + 1;
                doubleApos = start;
            }
        }
        sb.append(s, start, limit);
    }

    private int skipWhiteSpace(int index) {
        return PatternProps.skipWhiteSpace(this.msg, index);
    }

    private int skipIdentifier(int index) {
        return PatternProps.skipIdentifier(this.msg, index);
    }

    private int skipDouble(int index) {
        while (index < this.msg.length()) {
            char c = this.msg.charAt(index);
            if ((c < '0' && "+-.".indexOf(c) < 0) || (c > '9' && c != 'e' && c != 'E' && c != 8734)) {
                break;
            }
            index++;
        }
        return index;
    }

    private static boolean isArgTypeChar(int c) {
        if (97 > c || c > 122) {
            return 65 <= c && c <= 90;
        } else {
            return true;
        }
    }

    private boolean isChoice(int index) {
        boolean z = true;
        int index2 = index + 1;
        char c = this.msg.charAt(index);
        if (c == 'c' || c == 'C') {
            index = index2 + 1;
            c = this.msg.charAt(index2);
            if (c == 'h' || c == 'H') {
                index2 = index + 1;
                c = this.msg.charAt(index);
                if (c == 'o' || c == 'O') {
                    index = index2 + 1;
                    c = this.msg.charAt(index2);
                    if (c == UCharacterProperty.LATIN_SMALL_LETTER_I_ || c == 'I') {
                        index2 = index + 1;
                        c = this.msg.charAt(index);
                        if (c == 'c' || c == 'C') {
                            c = this.msg.charAt(index2);
                            if (!(c == 'e' || c == 'E')) {
                                z = false;
                            }
                            index = index2;
                            return z;
                        }
                    }
                }
            }
        }
        return false;
    }

    private boolean isPlural(int index) {
        boolean z = true;
        int index2 = index + 1;
        char c = this.msg.charAt(index);
        if (c == 'p' || c == 'P') {
            index = index2 + 1;
            c = this.msg.charAt(index2);
            if (c == 'l' || c == 'L') {
                index2 = index + 1;
                c = this.msg.charAt(index);
                if (c == 'u' || c == 'U') {
                    index = index2 + 1;
                    c = this.msg.charAt(index2);
                    if (c == 'r' || c == 'R') {
                        index2 = index + 1;
                        c = this.msg.charAt(index);
                        if (c == 'a' || c == 'A') {
                            c = this.msg.charAt(index2);
                            if (!(c == 'l' || c == 'L')) {
                                z = false;
                            }
                            index = index2;
                            return z;
                        }
                    }
                }
            }
        }
        return false;
    }

    private boolean isSelect(int index) {
        boolean z = true;
        int index2 = index + 1;
        char c = this.msg.charAt(index);
        if (c == 's' || c == 'S') {
            index = index2 + 1;
            c = this.msg.charAt(index2);
            if (c == 'e' || c == 'E') {
                index2 = index + 1;
                c = this.msg.charAt(index);
                if (c == 'l' || c == 'L') {
                    index = index2 + 1;
                    c = this.msg.charAt(index2);
                    if (c == 'e' || c == 'E') {
                        index2 = index + 1;
                        c = this.msg.charAt(index);
                        if (c == 'c' || c == 'C') {
                            c = this.msg.charAt(index2);
                            if (!(c == 't' || c == 'T')) {
                                z = false;
                            }
                            index = index2;
                            return z;
                        }
                    }
                }
            }
        }
        return false;
    }

    private boolean isOrdinal(int index) {
        int index2 = index + 1;
        char c = this.msg.charAt(index);
        if (c == 'o' || c == 'O') {
            index = index2 + 1;
            c = this.msg.charAt(index2);
            if (c == 'r' || c == 'R') {
                index2 = index + 1;
                c = this.msg.charAt(index);
                if (c == 'd' || c == 'D') {
                    index = index2 + 1;
                    c = this.msg.charAt(index2);
                    if (c == UCharacterProperty.LATIN_SMALL_LETTER_I_ || c == 'I') {
                        index2 = index + 1;
                        c = this.msg.charAt(index);
                        if (c == 'n' || c == 'N') {
                            index = index2 + 1;
                            c = this.msg.charAt(index2);
                            if (c == 'a' || c == 'A') {
                                c = this.msg.charAt(index);
                                if (c == 'l' || c == 'L') {
                                    return true;
                                }
                                return false;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    private boolean inMessageFormatPattern(int nestingLevel) {
        return nestingLevel > 0 || ((Part) this.parts.get(0)).type == Type.MSG_START;
    }

    private boolean inTopLevelChoiceMessage(int nestingLevel, ArgType parentType) {
        if (nestingLevel != 1 || parentType != ArgType.CHOICE) {
            return false;
        }
        if (((Part) this.parts.get(0)).type != Type.MSG_START) {
            return true;
        }
        return false;
    }

    private void addPart(Type type, int index, int length, int value) {
        this.parts.add(new Part(type, index, length, value, null));
    }

    private void addLimitPart(int start, Type type, int index, int length, int value) {
        ((Part) this.parts.get(start)).limitPartIndex = this.parts.size();
        addPart(type, index, length, value);
    }

    private void addArgDoublePart(double numericValue, int start, int length) {
        int numericIndex;
        if (this.numericValues == null) {
            this.numericValues = new ArrayList();
            numericIndex = 0;
        } else {
            numericIndex = this.numericValues.size();
            if (numericIndex > 32767) {
                throw new IndexOutOfBoundsException("Too many numeric values");
            }
        }
        this.numericValues.add(Double.valueOf(numericValue));
        addPart(Type.ARG_DOUBLE, start, length, numericIndex);
    }

    private static String prefix(String s, int start) {
        StringBuilder prefix = new StringBuilder(44);
        if (start == 0) {
            prefix.append("\"");
        } else {
            prefix.append("[at pattern index ").append(start).append("] \"");
        }
        if (s.length() - start <= 24) {
            if (start != 0) {
                s = s.substring(start);
            }
            prefix.append(s);
        } else {
            int limit = (start + 24) - 4;
            if (Character.isHighSurrogate(s.charAt(limit - 1))) {
                limit--;
            }
            prefix.append(s, start, limit).append(" ...");
        }
        return prefix.append("\"").toString();
    }

    private static String prefix(String s) {
        return prefix(s, 0);
    }

    private String prefix(int start) {
        return prefix(this.msg, start);
    }

    private String prefix() {
        return prefix(this.msg, 0);
    }
}
