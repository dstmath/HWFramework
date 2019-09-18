package android.icu.text;

import android.icu.impl.ICUConfig;
import android.icu.impl.PatternProps;
import android.icu.impl.PatternTokenizer;
import android.icu.util.Freezable;
import android.icu.util.ICUCloneNotSupportedException;
import java.util.ArrayList;
import java.util.Locale;

public final class MessagePattern implements Cloneable, Freezable<MessagePattern> {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    public static final int ARG_NAME_NOT_NUMBER = -1;
    public static final int ARG_NAME_NOT_VALID = -2;
    private static final int MAX_PREFIX_LENGTH = 24;
    public static final double NO_NUMERIC_VALUE = -1.23456789E8d;
    /* access modifiers changed from: private */
    public static final ArgType[] argTypes = ArgType.values();
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
        /* access modifiers changed from: private */
        public final int index;
        /* access modifiers changed from: private */
        public final char length;
        /* access modifiers changed from: private */
        public int limitPartIndex;
        /* access modifiers changed from: private */
        public final Type type;
        /* access modifiers changed from: private */
        public short value;

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
            Type type2 = getType();
            if (type2 == Type.ARG_START || type2 == Type.ARG_LIMIT) {
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
            if (!(this.type.equals(o.type) && this.index == o.index && this.length == o.length && this.value == o.value && this.limitPartIndex == o.limitPartIndex)) {
                z = false;
            }
            return z;
        }

        public int hashCode() {
            return (((((this.type.hashCode() * 37) + this.index) * 37) + this.length) * 37) + this.value;
        }
    }

    public MessagePattern() {
        this.parts = new ArrayList<>();
        this.aposMode = defaultAposMode;
    }

    public MessagePattern(ApostropheMode mode) {
        this.parts = new ArrayList<>();
        this.aposMode = mode;
    }

    public MessagePattern(String pattern) {
        this.parts = new ArrayList<>();
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
        if (!isFrozen()) {
            this.msg = null;
            this.hasArgNumbers = false;
            this.hasArgNames = false;
            this.needsAutoQuoting = false;
            this.parts.clear();
            if (this.numericValues != null) {
                this.numericValues.clear();
                return;
            }
            return;
        }
        throw new UnsupportedOperationException("Attempt to clear() a frozen MessagePattern instance.");
    }

    public void clearPatternAndSetApostropheMode(ApostropheMode mode) {
        clear();
        this.aposMode = mode;
    }

    public boolean equals(Object other) {
        boolean z = true;
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        MessagePattern o = (MessagePattern) other;
        if (!this.aposMode.equals(o.aposMode) || (this.msg != null ? !this.msg.equals(o.msg) : o.msg != null) || !this.parts.equals(o.parts)) {
            z = false;
        }
        return z;
    }

    public int hashCode() {
        return (((this.aposMode.hashCode() * 37) + (this.msg != null ? this.msg.hashCode() : 0)) * 37) + this.parts.hashCode();
    }

    public ApostropheMode getApostropheMode() {
        return this.aposMode;
    }

    /* access modifiers changed from: package-private */
    public boolean jdkAposMode() {
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
        if (!PatternProps.isIdentifier(name)) {
            return -2;
        }
        return parseArgNumber(name, 0, name.length());
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
            Part part2 = part;
            if (part.getType() == Part.Type.INSERT_CHAR) {
                if (modified == null) {
                    modified = new StringBuilder(this.msg.length() + 10).append(this.msg);
                }
                modified.insert(part2.index, (char) part2.value);
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
        return this.parts.get(i);
    }

    public Part.Type getPartType(int i) {
        return this.parts.get(i).type;
    }

    public int getPatternIndex(int partIndex) {
        return this.parts.get(partIndex).index;
    }

    public String getSubstring(Part part) {
        int index = part.index;
        return this.msg.substring(index, part.length + index);
    }

    public boolean partSubstringMatches(Part part, String s) {
        return part.length == s.length() && this.msg.regionMatches(part.index, s, 0, part.length);
    }

    public double getNumericValue(Part part) {
        Part.Type type = part.type;
        if (type == Part.Type.ARG_INT) {
            return (double) part.value;
        }
        if (type == Part.Type.ARG_DOUBLE) {
            return this.numericValues.get(part.value).doubleValue();
        }
        return -1.23456789E8d;
    }

    public double getPluralOffset(int pluralStart) {
        Part part = this.parts.get(pluralStart);
        if (part.type.hasNumericValue()) {
            return getNumericValue(part);
        }
        return 0.0d;
    }

    public int getLimitPartIndex(int start) {
        int limit = this.parts.get(start).limitPartIndex;
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
        } catch (CloneNotSupportedException e) {
            throw new ICUCloneNotSupportedException((Throwable) e);
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
        if (!isFrozen()) {
            this.msg = pattern;
            this.hasArgNumbers = false;
            this.hasArgNames = false;
            this.needsAutoQuoting = false;
            this.parts.clear();
            if (this.numericValues != null) {
                this.numericValues.clear();
                return;
            }
            return;
        }
        throw new UnsupportedOperationException("Attempt to parse(" + prefix(pattern) + ") on frozen MessagePattern instance.");
    }

    private void postParse() {
    }

    private int parseMessage(int index, int msgStartLength, int nestingLevel, ArgType parentType) {
        int index2;
        int index3;
        if (nestingLevel <= 32767) {
            int msgStart = this.parts.size();
            addPart(Part.Type.MSG_START, index, msgStartLength, nestingLevel);
            int index4 = index + msgStartLength;
            while (index4 < this.msg.length()) {
                int index5 = index4 + 1;
                int index6 = this.msg.charAt(index4);
                if (index6 == 39) {
                    if (index5 == this.msg.length()) {
                        addPart(Part.Type.INSERT_CHAR, index5, 0, 39);
                        this.needsAutoQuoting = true;
                    } else {
                        char c = this.msg.charAt(index5);
                        if (c == '\'') {
                            addPart(Part.Type.SKIP_SYNTAX, index5, 1, 0);
                            index4 = index5 + 1;
                        } else if (this.aposMode == ApostropheMode.DOUBLE_REQUIRED || c == '{' || c == '}' || ((parentType == ArgType.CHOICE && c == '|') || (parentType.hasPluralStyle() && c == '#'))) {
                            addPart(Part.Type.SKIP_SYNTAX, index5 - 1, 1, 0);
                            while (true) {
                                index3 = this.msg.indexOf(39, index5 + 1);
                                if (index3 < 0) {
                                    index2 = this.msg.length();
                                    addPart(Part.Type.INSERT_CHAR, index2, 0, 39);
                                    this.needsAutoQuoting = true;
                                    break;
                                } else if (index3 + 1 >= this.msg.length() || this.msg.charAt(index3 + 1) != '\'') {
                                    addPart(Part.Type.SKIP_SYNTAX, index3, 1, 0);
                                    index4 = index3 + 1;
                                } else {
                                    index5 = index3 + 1;
                                    addPart(Part.Type.SKIP_SYNTAX, index5, 1, 0);
                                }
                            }
                            addPart(Part.Type.SKIP_SYNTAX, index3, 1, 0);
                            index4 = index3 + 1;
                        } else {
                            addPart(Part.Type.INSERT_CHAR, index5, 0, 39);
                            this.needsAutoQuoting = true;
                        }
                    }
                    index4 = index5;
                } else if (parentType.hasPluralStyle() && index6 == 35) {
                    addPart(Part.Type.REPLACE_NUMBER, index5 - 1, 1, 0);
                    index4 = index5;
                } else if (index6 == 123) {
                    index2 = parseArg(index5 - 1, 1, nestingLevel);
                } else {
                    if ((nestingLevel > 0 && index6 == 125) || (parentType == ArgType.CHOICE && index6 == 124)) {
                        addLimitPart(msgStart, Part.Type.MSG_LIMIT, index5 - 1, (parentType == ArgType.CHOICE && index6 == 125) ? 0 : 1, nestingLevel);
                        if (parentType == ArgType.CHOICE) {
                            return index5 - 1;
                        }
                        return index5;
                    }
                    index4 = index5;
                }
                index4 = index2;
            }
            if (nestingLevel <= 0 || inTopLevelChoiceMessage(nestingLevel, parentType)) {
                addLimitPart(msgStart, Part.Type.MSG_LIMIT, index4, 0, nestingLevel);
                return index4;
            }
            throw new IllegalArgumentException("Unmatched '{' braces in message " + prefix());
        }
        throw new IndexOutOfBoundsException();
    }

    private int parseArg(int index, int argStartLength, int nestingLevel) {
        int argStart = this.parts.size();
        ArgType argType = ArgType.NONE;
        addPart(Part.Type.ARG_START, index, argStartLength, argType.ordinal());
        int skipWhiteSpace = skipWhiteSpace(index + argStartLength);
        int index2 = skipWhiteSpace;
        int nameIndex = skipWhiteSpace;
        if (index2 != this.msg.length()) {
            int index3 = skipIdentifier(index2);
            int number = parseArgNumber(nameIndex, index3);
            if (number >= 0) {
                int length = index3 - nameIndex;
                if (length > 65535 || number > 32767) {
                    throw new IndexOutOfBoundsException("Argument number too large: " + prefix(nameIndex));
                }
                this.hasArgNumbers = true;
                addPart(Part.Type.ARG_NUMBER, nameIndex, length, number);
            } else if (number == -1) {
                int length2 = index3 - nameIndex;
                if (length2 <= 65535) {
                    this.hasArgNames = true;
                    addPart(Part.Type.ARG_NAME, nameIndex, length2, 0);
                } else {
                    throw new IndexOutOfBoundsException("Argument name too long: " + prefix(nameIndex));
                }
            } else {
                throw new IllegalArgumentException("Bad argument syntax: " + prefix(nameIndex));
            }
            int index4 = skipWhiteSpace(index3);
            if (index4 != this.msg.length()) {
                char c = this.msg.charAt(index4);
                if (c != '}') {
                    if (c == ',') {
                        int typeIndex = skipWhiteSpace(index4 + 1);
                        int index5 = typeIndex;
                        while (index5 < this.msg.length() && isArgTypeChar(this.msg.charAt(index5))) {
                            index5++;
                        }
                        int length3 = index5 - typeIndex;
                        index4 = skipWhiteSpace(index5);
                        if (index4 != this.msg.length()) {
                            if (length3 != 0) {
                                char charAt = this.msg.charAt(index4);
                                c = charAt;
                                if (charAt == ',' || c == '}') {
                                    if (length3 <= 65535) {
                                        argType = ArgType.SIMPLE;
                                        if (length3 == 6) {
                                            if (isChoice(typeIndex)) {
                                                argType = ArgType.CHOICE;
                                            } else if (isPlural(typeIndex)) {
                                                argType = ArgType.PLURAL;
                                            } else if (isSelect(typeIndex)) {
                                                argType = ArgType.SELECT;
                                            }
                                        } else if (length3 == 13 && isSelect(typeIndex) && isOrdinal(typeIndex + 6)) {
                                            argType = ArgType.SELECTORDINAL;
                                        }
                                        short unused = this.parts.get(argStart).value = (short) argType.ordinal();
                                        if (argType == ArgType.SIMPLE) {
                                            addPart(Part.Type.ARG_TYPE, typeIndex, length3, 0);
                                        }
                                        if (c != '}') {
                                            int index6 = index4 + 1;
                                            if (argType == ArgType.SIMPLE) {
                                                index4 = parseSimpleStyle(index6);
                                            } else if (argType == ArgType.CHOICE) {
                                                index4 = parseChoiceStyle(index6, nestingLevel);
                                            } else {
                                                index4 = parsePluralOrSelectStyle(argType, index6, nestingLevel);
                                            }
                                        } else if (argType != ArgType.SIMPLE) {
                                            throw new IllegalArgumentException("No style field for complex argument: " + prefix(nameIndex));
                                        }
                                    } else {
                                        throw new IndexOutOfBoundsException("Argument type name too long: " + prefix(nameIndex));
                                    }
                                }
                            }
                            throw new IllegalArgumentException("Bad argument syntax: " + prefix(nameIndex));
                        }
                        throw new IllegalArgumentException("Unmatched '{' braces in message " + prefix());
                    }
                    throw new IllegalArgumentException("Bad argument syntax: " + prefix(nameIndex));
                }
                ArgType argType2 = argType;
                addLimitPart(argStart, Part.Type.ARG_LIMIT, index4, 1, argType2.ordinal());
                return index4 + 1;
            }
            throw new IllegalArgumentException("Unmatched '{' braces in message " + prefix());
        }
        throw new IllegalArgumentException("Unmatched '{' braces in message " + prefix());
    }

    private int parseSimpleStyle(int index) {
        int start = index;
        int index2 = index;
        int nestedBraces = 0;
        while (index2 < this.msg.length()) {
            int index3 = index2 + 1;
            char c = this.msg.charAt(index2);
            if (c == '\'') {
                int index4 = this.msg.indexOf(39, index3);
                if (index4 >= 0) {
                    index2 = index4 + 1;
                } else {
                    throw new IllegalArgumentException("Quoted literal argument style text reaches to the end of the message: " + prefix(start));
                }
            } else {
                if (c == '{') {
                    nestedBraces++;
                } else if (c == '}') {
                    if (nestedBraces > 0) {
                        nestedBraces--;
                    } else {
                        int index5 = index3 - 1;
                        int length = index5 - start;
                        if (length <= 65535) {
                            addPart(Part.Type.ARG_STYLE, start, length, 0);
                            return index5;
                        }
                        throw new IndexOutOfBoundsException("Argument style text too long: " + prefix(start));
                    }
                }
                index2 = index3;
            }
        }
        throw new IllegalArgumentException("Unmatched '{' braces in message " + prefix());
    }

    private int parseChoiceStyle(int index, int nestingLevel) {
        int start = index;
        int index2 = skipWhiteSpace(index);
        if (index2 == this.msg.length() || this.msg.charAt(index2) == '}') {
            throw new IllegalArgumentException("Missing choice argument pattern in " + prefix());
        }
        while (true) {
            int numberIndex = index2;
            int index3 = skipDouble(index2);
            int length = index3 - numberIndex;
            if (length == 0) {
                throw new IllegalArgumentException("Bad choice pattern syntax: " + prefix(start));
            } else if (length <= 65535) {
                parseDouble(numberIndex, index3, true);
                int index4 = skipWhiteSpace(index3);
                if (index4 != this.msg.length()) {
                    char c = this.msg.charAt(index4);
                    if (c == '#' || c == '<' || c == 8804) {
                        addPart(Part.Type.ARG_SELECTOR, index4, 1, 0);
                        int index5 = parseMessage(index4 + 1, 0, nestingLevel + 1, ArgType.CHOICE);
                        if (index5 == this.msg.length()) {
                            return index5;
                        }
                        if (this.msg.charAt(index5) != '}') {
                            index2 = skipWhiteSpace(index5 + 1);
                        } else if (inMessageFormatPattern(nestingLevel)) {
                            return index5;
                        } else {
                            throw new IllegalArgumentException("Bad choice pattern syntax: " + prefix(start));
                        }
                    } else {
                        throw new IllegalArgumentException("Expected choice separator (#<≤) instead of '" + c + "' in choice pattern " + prefix(start));
                    }
                } else {
                    throw new IllegalArgumentException("Bad choice pattern syntax: " + prefix(start));
                }
            } else {
                throw new IndexOutOfBoundsException("Choice number too long: " + prefix(numberIndex));
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:52:0x0182, code lost:
        throw new java.lang.IllegalArgumentException("No message fragment after " + r13.toString().toLowerCase(java.util.Locale.ENGLISH) + " selector: " + prefix(r6));
     */
    private int parsePluralOrSelectStyle(ArgType argType, int index, int nestingLevel) {
        int index2;
        boolean eos;
        int index3;
        int start = index;
        boolean isEmpty = true;
        int index4 = index;
        boolean hasOther = false;
        while (true) {
            index2 = skipWhiteSpace(index4);
            eos = index2 == this.msg.length();
            if (!eos && this.msg.charAt(index2) != '}') {
                int selectorIndex = index2;
                if (!argType.hasPluralStyle() || this.msg.charAt(selectorIndex) != '=') {
                    index3 = skipIdentifier(index2);
                    int length = index3 - selectorIndex;
                    if (length == 0) {
                        throw new IllegalArgumentException("Bad " + argType.toString().toLowerCase(Locale.ENGLISH) + " pattern syntax: " + prefix(start));
                    } else if (!argType.hasPluralStyle() || length != 6 || index3 >= this.msg.length() || !this.msg.regionMatches(selectorIndex, "offset:", 0, 7)) {
                        if (length <= 65535) {
                            addPart(Part.Type.ARG_SELECTOR, selectorIndex, length, 0);
                            if (this.msg.regionMatches(selectorIndex, PluralRules.KEYWORD_OTHER, 0, length)) {
                                hasOther = true;
                            }
                        } else {
                            throw new IndexOutOfBoundsException("Argument selector too long: " + prefix(selectorIndex));
                        }
                    } else if (isEmpty) {
                        int valueIndex = skipWhiteSpace(index3 + 1);
                        index4 = skipDouble(valueIndex);
                        if (index4 == valueIndex) {
                            throw new IllegalArgumentException("Missing value for plural 'offset:' " + prefix(start));
                        } else if (index4 - valueIndex <= 65535) {
                            parseDouble(valueIndex, index4, false);
                            isEmpty = false;
                        } else {
                            throw new IndexOutOfBoundsException("Plural offset value too long: " + prefix(valueIndex));
                        }
                    } else {
                        throw new IllegalArgumentException("Plural argument 'offset:' (if present) must precede key-message pairs: " + prefix(start));
                    }
                } else {
                    index3 = skipDouble(index2 + 1);
                    int length2 = index3 - selectorIndex;
                    if (length2 == 1) {
                        throw new IllegalArgumentException("Bad " + argType.toString().toLowerCase(Locale.ENGLISH) + " pattern syntax: " + prefix(start));
                    } else if (length2 <= 65535) {
                        addPart(Part.Type.ARG_SELECTOR, selectorIndex, length2, 0);
                        parseDouble(selectorIndex + 1, index3, false);
                    } else {
                        throw new IndexOutOfBoundsException("Argument selector too long: " + prefix(selectorIndex));
                    }
                }
                int index5 = skipWhiteSpace(index3);
                if (index5 == this.msg.length() || this.msg.charAt(index5) != '{') {
                } else {
                    index4 = parseMessage(index5, 1, nestingLevel + 1, argType);
                    isEmpty = false;
                }
            }
        }
        if (eos == inMessageFormatPattern(nestingLevel)) {
            throw new IllegalArgumentException("Bad " + argType.toString().toLowerCase(Locale.ENGLISH) + " pattern syntax: " + prefix(start));
        } else if (hasOther) {
            return index2;
        } else {
            throw new IllegalArgumentException("Missing 'other' keyword in " + argType.toString().toLowerCase(Locale.ENGLISH) + " pattern in " + prefix());
        }
    }

    private static int parseArgNumber(CharSequence s, int start, int limit) {
        boolean badNumber;
        int number;
        if (start >= limit) {
            return -2;
        }
        int start2 = start + 1;
        int start3 = s.charAt(start);
        if (start3 == 48) {
            if (start2 == limit) {
                return 0;
            }
            number = 0;
            badNumber = true;
        } else if (49 > start3 || start3 > 57) {
            return -1;
        } else {
            number = start3 - 48;
            badNumber = false;
        }
        while (start2 < limit) {
            int start4 = start2 + 1;
            char c = s.charAt(start2);
            if ('0' > c || c > '9') {
                return -1;
            }
            if (number >= 214748364) {
                badNumber = true;
            }
            number = (number * 10) + (c - '0');
            start2 = start4;
        }
        if (badNumber) {
            return -2;
        }
        return number;
    }

    private int parseArgNumber(int start, int limit) {
        return parseArgNumber(this.msg, start, limit);
    }

    private void parseDouble(int start, int limit, boolean allowInfinity) {
        int index;
        int value = 0;
        int isNegative = 0;
        int index2 = start;
        int index3 = index2 + 1;
        char c = this.msg.charAt(index2);
        if (c == '-') {
            isNegative = 1;
            if (index3 != limit) {
                index = index3 + 1;
                c = this.msg.charAt(index3);
            }
            throw new NumberFormatException("Bad syntax for numeric value: " + this.msg.substring(start, limit));
        } else if (c == '+') {
            if (index3 != limit) {
                index = index3 + 1;
                c = this.msg.charAt(index3);
            }
            throw new NumberFormatException("Bad syntax for numeric value: " + this.msg.substring(start, limit));
        } else {
            index = index3;
        }
        if (c == 8734) {
            if (allowInfinity && index == limit) {
                addArgDoublePart(isNegative != 0 ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY, start, limit - start);
                return;
            }
            throw new NumberFormatException("Bad syntax for numeric value: " + this.msg.substring(start, limit));
        }
        while ('0' <= c && c <= '9') {
            value = (value * 10) + (c - '0');
            if (value > 32767 + isNegative) {
                break;
            } else if (index == limit) {
                addPart(Part.Type.ARG_INT, start, limit - start, isNegative != 0 ? -value : value);
                return;
            } else {
                c = this.msg.charAt(index);
                index++;
            }
        }
        addArgDoublePart(Double.parseDouble(this.msg.substring(start, limit)), start, limit - start);
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
                int i2 = i + 1;
                start = i2;
                doubleApos = i2;
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
        return (97 <= c && c <= 122) || (65 <= c && c <= 90);
    }

    private boolean isChoice(int index) {
        int index2 = index + 1;
        char index3 = this.msg.charAt(index);
        char c = index3;
        if (index3 == 'c' || c == 'C') {
            int index4 = index2 + 1;
            char charAt = this.msg.charAt(index2);
            char c2 = charAt;
            if (charAt == 'h' || c2 == 'H') {
                int index5 = index4 + 1;
                char charAt2 = this.msg.charAt(index4);
                char c3 = charAt2;
                if (charAt2 == 'o' || c3 == 'O') {
                    index4 = index5 + 1;
                    char charAt3 = this.msg.charAt(index5);
                    char c4 = charAt3;
                    if (charAt3 == 'i' || c4 == 'I') {
                        int index6 = index4 + 1;
                        char charAt4 = this.msg.charAt(index4);
                        char c5 = charAt4;
                        if (charAt4 == 'c' || c5 == 'C') {
                            char charAt5 = this.msg.charAt(index6);
                            char c6 = charAt5;
                            if (charAt5 == 'e' || c6 == 'E') {
                                return true;
                            }
                        }
                    }
                }
            }
            int i = index4;
        }
        return false;
    }

    private boolean isPlural(int index) {
        int index2 = index + 1;
        char index3 = this.msg.charAt(index);
        char c = index3;
        if (index3 == 'p' || c == 'P') {
            int index4 = index2 + 1;
            char charAt = this.msg.charAt(index2);
            char c2 = charAt;
            if (charAt == 'l' || c2 == 'L') {
                int index5 = index4 + 1;
                char charAt2 = this.msg.charAt(index4);
                char c3 = charAt2;
                if (charAt2 == 'u' || c3 == 'U') {
                    index4 = index5 + 1;
                    char charAt3 = this.msg.charAt(index5);
                    char c4 = charAt3;
                    if (charAt3 == 'r' || c4 == 'R') {
                        int index6 = index4 + 1;
                        char charAt4 = this.msg.charAt(index4);
                        char c5 = charAt4;
                        if (charAt4 == 'a' || c5 == 'A') {
                            char charAt5 = this.msg.charAt(index6);
                            char c6 = charAt5;
                            if (charAt5 == 'l' || c6 == 'L') {
                                return true;
                            }
                        }
                    }
                }
            }
            int i = index4;
        } else {
            int i2 = index2;
        }
        return false;
    }

    private boolean isSelect(int index) {
        int index2 = index + 1;
        char index3 = this.msg.charAt(index);
        char c = index3;
        if (index3 == 's' || c == 'S') {
            int index4 = index2 + 1;
            char charAt = this.msg.charAt(index2);
            char c2 = charAt;
            if (charAt == 'e' || c2 == 'E') {
                int index5 = index4 + 1;
                char charAt2 = this.msg.charAt(index4);
                char c3 = charAt2;
                if (charAt2 == 'l' || c3 == 'L') {
                    index4 = index5 + 1;
                    char charAt3 = this.msg.charAt(index5);
                    char c4 = charAt3;
                    if (charAt3 == 'e' || c4 == 'E') {
                        int index6 = index4 + 1;
                        char charAt4 = this.msg.charAt(index4);
                        char c5 = charAt4;
                        if (charAt4 == 'c' || c5 == 'C') {
                            char charAt5 = this.msg.charAt(index6);
                            char c6 = charAt5;
                            if (charAt5 == 't' || c6 == 'T') {
                                return true;
                            }
                        }
                    }
                } else {
                    int i = index5;
                }
            }
            int i2 = index4;
        }
        return false;
    }

    private boolean isOrdinal(int index) {
        int index2 = index + 1;
        char index3 = this.msg.charAt(index);
        char c = index3;
        if (index3 == 'o' || c == 'O') {
            int index4 = index2 + 1;
            char charAt = this.msg.charAt(index2);
            char c2 = charAt;
            if (charAt == 'r' || c2 == 'R') {
                index2 = index4 + 1;
                char charAt2 = this.msg.charAt(index4);
                char c3 = charAt2;
                if (charAt2 == 'd' || c3 == 'D') {
                    int index5 = index2 + 1;
                    char charAt3 = this.msg.charAt(index2);
                    char c4 = charAt3;
                    if (charAt3 == 'i' || c4 == 'I') {
                        index2 = index5 + 1;
                        char charAt4 = this.msg.charAt(index5);
                        char c5 = charAt4;
                        if (charAt4 == 'n' || c5 == 'N') {
                            int index6 = index2 + 1;
                            char charAt5 = this.msg.charAt(index2);
                            char c6 = charAt5;
                            if (charAt5 == 'a' || c6 == 'A') {
                                char charAt6 = this.msg.charAt(index6);
                                char c7 = charAt6;
                                if (charAt6 == 'l' || c7 == 'L') {
                                    return true;
                                }
                            }
                        }
                    }
                }
            }
            return false;
        }
        int i = index2;
        return false;
    }

    private boolean inMessageFormatPattern(int nestingLevel) {
        return nestingLevel > 0 || this.parts.get(0).type == Part.Type.MSG_START;
    }

    private boolean inTopLevelChoiceMessage(int nestingLevel, ArgType parentType) {
        return nestingLevel == 1 && parentType == ArgType.CHOICE && this.parts.get(0).type != Part.Type.MSG_START;
    }

    private void addPart(Part.Type type, int index, int length, int value) {
        ArrayList<Part> arrayList = this.parts;
        Part part = new Part(type, index, length, value);
        arrayList.add(part);
    }

    private void addLimitPart(int start, Part.Type type, int index, int length, int value) {
        int unused = this.parts.get(start).limitPartIndex = this.parts.size();
        addPart(type, index, length, value);
    }

    private void addArgDoublePart(double numericValue, int start, int length) {
        int numericIndex;
        if (this.numericValues == null) {
            this.numericValues = new ArrayList<>();
            numericIndex = 0;
        } else {
            numericIndex = this.numericValues.size();
            if (numericIndex > 32767) {
                throw new IndexOutOfBoundsException("Too many numeric values");
            }
        }
        this.numericValues.add(Double.valueOf(numericValue));
        addPart(Part.Type.ARG_DOUBLE, start, length, numericIndex);
    }

    private static String prefix(String s, int start) {
        StringBuilder prefix = new StringBuilder(44);
        if (start == 0) {
            prefix.append("\"");
        } else {
            prefix.append("[at pattern index ");
            prefix.append(start);
            prefix.append("] \"");
        }
        if (s.length() - start <= 24) {
            prefix.append(start == 0 ? s : s.substring(start));
        } else {
            int limit = (start + 24) - 4;
            if (Character.isHighSurrogate(s.charAt(limit - 1))) {
                limit--;
            }
            prefix.append(s, start, limit);
            prefix.append(" ...");
        }
        prefix.append("\"");
        return prefix.toString();
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
