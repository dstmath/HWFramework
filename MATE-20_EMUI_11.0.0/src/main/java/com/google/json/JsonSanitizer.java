package com.google.json;

public final class JsonSanitizer {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    public static final int DEFAULT_NESTING_DEPTH = 64;
    private static final char[] HEX_DIGITS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
    public static final int MAXIMUM_NESTING_DEPTH = 4096;
    private static final boolean SUPER_VERBOSE_AND_SLOW_LOGGING = false;
    private static final UnbracketedComma UNBRACKETED_COMMA = new UnbracketedComma();
    private int bracketDepth;
    private int cleaned;
    private boolean[] isMap;
    private final String jsonish;
    private final int maximumNestingDepth;
    private StringBuilder sanitizedJson;

    /* access modifiers changed from: private */
    public enum State {
        START_ARRAY,
        BEFORE_ELEMENT,
        AFTER_ELEMENT,
        START_MAP,
        BEFORE_KEY,
        AFTER_KEY,
        BEFORE_VALUE,
        AFTER_VALUE
    }

    public static String sanitize(String str) {
        return sanitize(str, 64);
    }

    public static String sanitize(String str, int i) {
        JsonSanitizer jsonSanitizer = new JsonSanitizer(str, i);
        jsonSanitizer.sanitize();
        return jsonSanitizer.toString();
    }

    JsonSanitizer(String str) {
        this(str, 64);
    }

    JsonSanitizer(String str, int i) {
        this.maximumNestingDepth = Math.min(Math.max(1, i), (int) MAXIMUM_NESTING_DEPTH);
        this.jsonish = str == null ? "null" : str;
    }

    /* access modifiers changed from: package-private */
    public int getMaximumNestingDepth() {
        return this.maximumNestingDepth;
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Removed duplicated region for block: B:191:0x024b  */
    /* JADX WARNING: Removed duplicated region for block: B:243:0x00d1 A[SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:52:0x0093 A[Catch:{ UnbracketedComma -> 0x0231 }] */
    /* JADX WARNING: Removed duplicated region for block: B:53:0x009a A[Catch:{ UnbracketedComma -> 0x0231 }] */
    /* JADX WARNING: Removed duplicated region for block: B:74:0x00c7 A[Catch:{ UnbracketedComma -> 0x0231 }] */
    /* JADX WARNING: Removed duplicated region for block: B:84:0x00e1 A[Catch:{ UnbracketedComma -> 0x0231 }] */
    /* JADX WARNING: Removed duplicated region for block: B:88:0x00f3 A[Catch:{ UnbracketedComma -> 0x0231 }] */
    public void sanitize() {
        StringBuilder sb;
        State requireValueState;
        int i;
        int i2;
        boolean z;
        boolean z2 = false;
        this.cleaned = 0;
        this.bracketDepth = 0;
        this.sanitizedJson = null;
        State state = State.START_ARRAY;
        int length = this.jsonish.length();
        State state2 = state;
        int i3 = 0;
        while (true) {
            if (i3 < length) {
                try {
                    char charAt = this.jsonish.charAt(i3);
                    if (!(charAt == '\t' || charAt == '\n' || charAt == '\r' || charAt == ' ')) {
                        if (charAt != '\"') {
                            if (charAt != ',') {
                                if (charAt == '/') {
                                    i = i3 + 1;
                                    if (i < length) {
                                        char charAt2 = this.jsonish.charAt(i);
                                        if (charAt2 == '*') {
                                            if (i3 + 3 < length) {
                                                i2 = i3 + 2;
                                                while (true) {
                                                    i2 = this.jsonish.indexOf(47, i2 + 1);
                                                    if (i2 >= 0) {
                                                        if (this.jsonish.charAt(i2 - 1) == '*') {
                                                        }
                                                    }
                                                }
                                            }
                                            i = length;
                                        } else if (charAt2 == '/') {
                                            i2 = i3 + 2;
                                            while (true) {
                                                if (i2 < length) {
                                                    char charAt3 = this.jsonish.charAt(i2);
                                                    if (!(charAt3 == '\n' || charAt3 == '\r' || charAt3 == 8232)) {
                                                        if (charAt3 != 8233) {
                                                            i2++;
                                                        }
                                                    }
                                                }
                                            }
                                            i = length;
                                        }
                                        i = i2 + 1;
                                    }
                                    elide(i3, i);
                                } else if (charAt != ':') {
                                    if (charAt != '[') {
                                        if (charAt != ']') {
                                            if (charAt != '{') {
                                                if (charAt != '}') {
                                                    switch (charAt) {
                                                        case '\'':
                                                            break;
                                                        case '(':
                                                        case ')':
                                                            elide(i3, i3 + 1);
                                                            break;
                                                        default:
                                                            i = i3;
                                                            while (i < length) {
                                                                char charAt4 = this.jsonish.charAt(i);
                                                                if (('a' <= charAt4 && charAt4 <= 'z') || (('0' <= charAt4 && charAt4 <= '9') || charAt4 == '+' || charAt4 == '-' || charAt4 == '.' || (('A' <= charAt4 && charAt4 <= 'Z') || charAt4 == '_' || charAt4 == '$'))) {
                                                                    i++;
                                                                } else if (i != i3) {
                                                                    elide(i3, i3 + 1);
                                                                    break;
                                                                } else {
                                                                    state2 = requireValueState(i3, state2, true);
                                                                    if (!(('0' <= charAt && charAt <= '9') || charAt == '.' || charAt == '+')) {
                                                                        if (charAt != '-') {
                                                                            z = false;
                                                                            boolean z3 = z && isKeyword(i3, i);
                                                                            if (!z && !z3) {
                                                                                while (true) {
                                                                                    if (i < length) {
                                                                                        if (!isJsonSpecialChar(i)) {
                                                                                            i++;
                                                                                        }
                                                                                    }
                                                                                }
                                                                                if (i < length && this.jsonish.charAt(i) == '\"') {
                                                                                    i++;
                                                                                }
                                                                            }
                                                                            if (state2 == State.AFTER_KEY) {
                                                                                if (!z) {
                                                                                    if (!z3) {
                                                                                        insert(i3, '\"');
                                                                                        sanitizeString(i3, i);
                                                                                        break;
                                                                                    }
                                                                                } else {
                                                                                    normalizeNumber(i3, i);
                                                                                    break;
                                                                                }
                                                                            } else {
                                                                                insert(i3, '\"');
                                                                                if (!z) {
                                                                                    sanitizeString(i3, i);
                                                                                    break;
                                                                                } else {
                                                                                    canonicalizeNumber(i3, i);
                                                                                    insert(i, '\"');
                                                                                    break;
                                                                                }
                                                                            }
                                                                        }
                                                                    }
                                                                    z = true;
                                                                    if (z) {
                                                                    }
                                                                    while (true) {
                                                                        if (i < length) {
                                                                        }
                                                                        i++;
                                                                    }
                                                                    i++;
                                                                    if (state2 == State.AFTER_KEY) {
                                                                    }
                                                                }
                                                            }
                                                            if (i != i3) {
                                                            }
                                                            break;
                                                    }
                                                }
                                            }
                                        }
                                        if (this.bracketDepth == 0) {
                                            elide(i3, this.jsonish.length());
                                        } else {
                                            int i4 = AnonymousClass1.$SwitchMap$com$google$json$JsonSanitizer$State[state2.ordinal()];
                                            if (i4 == 1) {
                                                insert(i3, "null");
                                            } else if (i4 == 2 || i4 == 3) {
                                                elideTrailingComma(i3);
                                            } else if (i4 == 4) {
                                                insert(i3, ":null");
                                            }
                                            this.bracketDepth--;
                                            char c = this.isMap[this.bracketDepth] ? '}' : ']';
                                            if (charAt != c) {
                                                replace(i3, i3 + 1, c);
                                            }
                                            if (this.bracketDepth != 0) {
                                                if (this.isMap[this.bracketDepth - 1]) {
                                                    requireValueState = State.AFTER_VALUE;
                                                    state2 = requireValueState;
                                                }
                                            }
                                            requireValueState = State.AFTER_ELEMENT;
                                            state2 = requireValueState;
                                        }
                                    }
                                    requireValueState(i3, state2, false);
                                    if (this.isMap == null) {
                                        this.isMap = new boolean[this.maximumNestingDepth];
                                    }
                                    boolean z4 = charAt == '{';
                                    this.isMap[this.bracketDepth] = z4;
                                    this.bracketDepth++;
                                    if (z4) {
                                        requireValueState = State.START_MAP;
                                    } else {
                                        requireValueState = State.START_ARRAY;
                                    }
                                    state2 = requireValueState;
                                } else if (state2 == State.AFTER_KEY) {
                                    requireValueState = State.BEFORE_VALUE;
                                    state2 = requireValueState;
                                } else {
                                    elide(i3, i3 + 1);
                                }
                                i3 = i - 1;
                            } else if (this.bracketDepth != 0) {
                                switch (state2) {
                                    case BEFORE_VALUE:
                                        insert(i3, "null");
                                        state2 = State.BEFORE_KEY;
                                        continue;
                                    case BEFORE_ELEMENT:
                                    case START_ARRAY:
                                        insert(i3, "null");
                                        state2 = State.BEFORE_ELEMENT;
                                        continue;
                                    case BEFORE_KEY:
                                    case AFTER_KEY:
                                    case START_MAP:
                                        elide(i3, i3 + 1);
                                        continue;
                                    case AFTER_ELEMENT:
                                        state2 = State.BEFORE_ELEMENT;
                                        continue;
                                    case AFTER_VALUE:
                                        state2 = State.BEFORE_KEY;
                                        continue;
                                    default:
                                        continue;
                                }
                            } else {
                                throw UNBRACKETED_COMMA;
                            }
                        }
                        requireValueState = requireValueState(i3, state2, true);
                        try {
                            int endOfQuotedString = endOfQuotedString(this.jsonish, i3);
                            sanitizeString(i3, endOfQuotedString);
                            i3 = endOfQuotedString - 1;
                            state2 = requireValueState;
                        } catch (UnbracketedComma unused) {
                            state2 = requireValueState;
                            elide(i3, this.jsonish.length());
                            insert(length, "null");
                            state2 = State.AFTER_ELEMENT;
                            sb = this.sanitizedJson;
                            if (sb == null) {
                            }
                        }
                    }
                    i3++;
                    z2 = false;
                } catch (UnbracketedComma unused2) {
                    elide(i3, this.jsonish.length());
                    insert(length, "null");
                    state2 = State.AFTER_ELEMENT;
                    sb = this.sanitizedJson;
                    if (sb == null) {
                    }
                }
            }
        }
        if (state2 == State.START_ARRAY && this.bracketDepth == 0) {
            insert(length, "null");
            state2 = State.AFTER_ELEMENT;
        }
        sb = this.sanitizedJson;
        if ((sb == null && sb.length() != 0) || this.cleaned != 0 || this.bracketDepth != 0) {
            if (this.sanitizedJson == null) {
                this.sanitizedJson = new StringBuilder(this.bracketDepth + length);
            }
            this.sanitizedJson.append((CharSequence) this.jsonish, this.cleaned, length);
            this.cleaned = length;
            int i5 = AnonymousClass1.$SwitchMap$com$google$json$JsonSanitizer$State[state2.ordinal()];
            if (i5 == 1) {
                this.sanitizedJson.append("null");
            } else if (i5 == 2 || i5 == 3) {
                elideTrailingComma(length);
            } else if (i5 == 4) {
                this.sanitizedJson.append(":null");
            }
            while (true) {
                int i6 = this.bracketDepth;
                if (i6 != 0) {
                    StringBuilder sb2 = this.sanitizedJson;
                    boolean[] zArr = this.isMap;
                    int i7 = i6 - 1;
                    this.bracketDepth = i7;
                    sb2.append(zArr[i7] ? '}' : ']');
                } else {
                    return;
                }
            }
        }
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x006b, code lost:
        if (java.lang.Character.isLowSurrogate(r16.jsonish.charAt(r6)) != false) goto L_0x006d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:81:0x011d, code lost:
        if (isHexAt(r6) != false) goto L_0x006d;
     */
    private void sanitizeString(int i, int i2) {
        int i3;
        int i4;
        int i5 = i;
        boolean z = false;
        while (i5 < i2) {
            char charAt = this.jsonish.charAt(i5);
            if (charAt == '\n') {
                replace(i5, i5 + 1, "\\n");
            } else if (charAt == '\r') {
                replace(i5, i5 + 1, "\\r");
            } else if (charAt == '\"' || charAt == '\'') {
                if (i5 != i) {
                    int i6 = i5 + 1;
                    if (i6 == i2) {
                        char charAt2 = this.jsonish.charAt(i);
                        if (charAt2 != '\'') {
                            charAt2 = '\"';
                        }
                        z = charAt2 == charAt;
                    }
                    if (z) {
                        if (charAt == '\'') {
                            replace(i5, i6, '\"');
                        }
                    } else if (charAt == '\"') {
                        insert(i5, '\\');
                    }
                } else if (charAt == '\'') {
                    replace(i5, i5 + 1, '\"');
                }
            } else if (charAt == '<') {
                int i7 = i5 + 3;
                if (i7 < i2) {
                    int i8 = i5 + 1;
                    char charAt3 = this.jsonish.charAt(i8);
                    char charAt4 = this.jsonish.charAt(i5 + 2);
                    char charAt5 = this.jsonish.charAt(i7);
                    char c = (char) (charAt3 | ' ');
                    char c2 = (char) (charAt4 | ' ');
                    char c3 = (char) (charAt5 | ' ');
                    if ((charAt3 == '!' && charAt4 == '-' && charAt5 == '-') || ((c == 's' && c2 == 'c' && c3 == 'r') || (charAt3 == '/' && c2 == 's' && c3 == 'c'))) {
                        replace(i5, i8, "\\u003c");
                    }
                }
            } else if (charAt != '>') {
                if (charAt == '\\') {
                    int i9 = i5 + 1;
                    if (i9 == i2) {
                        elide(i5, i9);
                    } else {
                        char charAt6 = this.jsonish.charAt(i9);
                        if (!(charAt6 == '\"' || charAt6 == '\\' || charAt6 == 'b' || charAt6 == 'f' || charAt6 == 'n' || charAt6 == 'r')) {
                            if (charAt6 != 'x') {
                                switch (charAt6) {
                                    case '/':
                                        break;
                                    case '0':
                                    case '1':
                                    case '2':
                                    case '3':
                                    case '4':
                                    case '5':
                                    case '6':
                                    case '7':
                                        int i10 = i9 + 1;
                                        if (i10 >= i2 || !isOctAt(i10)) {
                                            i10 = i9;
                                        } else {
                                            if (charAt <= '3' && (i3 = i10 + 1) < i2 && isOctAt(i3)) {
                                                i10 = i3;
                                            }
                                            int i11 = 0;
                                            while (i5 < i10) {
                                                i11 = (i11 << 3) | (this.jsonish.charAt(i5) - '0');
                                                i5++;
                                            }
                                            replace(i9, i10, "u00");
                                            appendHex(i11, 2);
                                        }
                                        i5 = i10 - 1;
                                        break;
                                    default:
                                        switch (charAt6) {
                                            case 't':
                                                break;
                                            case 'u':
                                                if (i5 + 6 < i2 && isHexAt(i5 + 2) && isHexAt(i5 + 3) && isHexAt(i5 + 4)) {
                                                    i4 = i5 + 5;
                                                    break;
                                                }
                                                elide(i5, i9);
                                                break;
                                            case 'v':
                                                replace(i5, i5 + 2, "\\u0008");
                                                break;
                                            default:
                                                elide(i5, i9);
                                                break;
                                        }
                                }
                            } else {
                                if (i5 + 4 < i2) {
                                    int i12 = i5 + 2;
                                    if (isHexAt(i12)) {
                                        int i13 = i5 + 3;
                                        if (isHexAt(i13)) {
                                            replace(i5, i12, "\\u00");
                                            i5 = i13;
                                        }
                                    }
                                }
                                elide(i5, i9);
                            }
                        }
                        i5 = i9;
                    }
                } else if (charAt == ']') {
                    int i14 = i5 + 2;
                    if (i14 < i2) {
                        int i15 = i5 + 1;
                        if (']' == this.jsonish.charAt(i15) && '>' == this.jsonish.charAt(i14)) {
                            replace(i5, i15, "\\u005d");
                        }
                    }
                } else if (charAt == 8232) {
                    replace(i5, i5 + 1, "\\u2028");
                } else if (charAt != 8233) {
                    if (charAt < ' ') {
                        if (charAt != '\t') {
                            if (charAt != '\n') {
                                if (charAt == '\r') {
                                }
                            }
                        }
                    } else if (charAt >= 55296) {
                        if (charAt < 57344) {
                            if (Character.isHighSurrogate(charAt)) {
                                i4 = i5 + 1;
                                if (i4 < i2) {
                                }
                            }
                        } else if (charAt <= 65533) {
                        }
                    }
                    replace(i5, i5 + 1, "\\u");
                    int i16 = 4;
                    while (true) {
                        i16--;
                        if (i16 >= 0) {
                            this.sanitizedJson.append(HEX_DIGITS[(charAt >>> (i16 << 2)) & 15]);
                        }
                    }
                } else {
                    replace(i5, i5 + 1, "\\u2029");
                }
                i5 = i4;
            } else {
                int i17 = i5 - 2;
                if (i17 >= i && '-' == this.jsonish.charAt(i17) && '-' == this.jsonish.charAt(i5 - 1)) {
                    replace(i5, i5 + 1, "\\u003e");
                }
            }
            i5++;
        }
        if (!z) {
            insert(i2, '\"');
        }
    }

    private State requireValueState(int i, State state, boolean z) throws UnbracketedComma {
        switch (state) {
            case BEFORE_VALUE:
                return State.AFTER_VALUE;
            case BEFORE_ELEMENT:
            case START_ARRAY:
                return State.AFTER_ELEMENT;
            case BEFORE_KEY:
            case START_MAP:
                if (z) {
                    return State.AFTER_KEY;
                }
                insert(i, "\"\":");
                return State.AFTER_VALUE;
            case AFTER_KEY:
                insert(i, ':');
                return State.AFTER_VALUE;
            case AFTER_ELEMENT:
                if (this.bracketDepth != 0) {
                    insert(i, ',');
                    return State.AFTER_ELEMENT;
                }
                throw UNBRACKETED_COMMA;
            case AFTER_VALUE:
                if (z) {
                    insert(i, ',');
                    return State.AFTER_KEY;
                }
                insert(i, ",\"\":");
                return State.AFTER_VALUE;
            default:
                throw new AssertionError();
        }
    }

    private void insert(int i, char c) {
        replace(i, i, c);
    }

    private void insert(int i, String str) {
        replace(i, i, str);
    }

    private void elide(int i, int i2) {
        if (this.sanitizedJson == null) {
            this.sanitizedJson = new StringBuilder(this.jsonish.length() + 16);
        }
        this.sanitizedJson.append((CharSequence) this.jsonish, this.cleaned, i);
        this.cleaned = i2;
    }

    private void replace(int i, int i2, char c) {
        elide(i, i2);
        this.sanitizedJson.append(c);
    }

    private void replace(int i, int i2, String str) {
        elide(i, i2);
        this.sanitizedJson.append(str);
    }

    private static int endOfQuotedString(String str, int i) {
        int i2;
        char charAt = str.charAt(i);
        int i3 = i;
        do {
            i3 = str.indexOf(charAt, i3 + 1);
            if (i3 < 0) {
                return str.length();
            }
            i2 = i3;
            while (i2 > i && str.charAt(i2 - 1) == '\\') {
                i2--;
            }
        } while (((i3 - i2) & 1) != 0);
        return i3 + 1;
    }

    private void elideTrailingComma(int i) {
        while (true) {
            i--;
            if (i >= this.cleaned) {
                char charAt = this.jsonish.charAt(i);
                if (charAt != '\t' && charAt != '\n' && charAt != '\r' && charAt != ' ') {
                    if (charAt == ',') {
                        elide(i, i + 1);
                        return;
                    }
                    throw new AssertionError("" + this.jsonish.charAt(i));
                }
            } else {
                int length = this.sanitizedJson.length();
                while (true) {
                    length--;
                    if (length >= 0) {
                        char charAt2 = this.sanitizedJson.charAt(length);
                        if (charAt2 != '\t' && charAt2 != '\n' && charAt2 != '\r' && charAt2 != ' ') {
                            if (charAt2 == ',') {
                                this.sanitizedJson.setLength(length);
                                return;
                            }
                            throw new AssertionError("" + this.sanitizedJson.charAt(length));
                        }
                    } else {
                        throw new AssertionError("Trailing comma not found in " + this.jsonish + " or " + ((Object) this.sanitizedJson));
                    }
                }
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:40:0x008b  */
    private void normalizeNumber(int i, int i2) {
        int i3;
        char charAt;
        long j;
        int length;
        char charAt2;
        int i4;
        if (i < i2) {
            char charAt3 = this.jsonish.charAt(i);
            if (charAt3 == '+') {
                int i5 = i + 1;
                elide(i, i5);
                i = i5;
            } else if (charAt3 == '-') {
                i++;
            }
        }
        int endOfDigitRun = endOfDigitRun(i, i2);
        if (i == endOfDigitRun) {
            insert(i, '0');
        } else if ('0' == this.jsonish.charAt(i)) {
            boolean z = false;
            int i6 = endOfDigitRun - i;
            if (i6 == 1 && endOfDigitRun < i2 && 120 == (this.jsonish.charAt(endOfDigitRun) | ' ')) {
                endOfDigitRun++;
                long j2 = 0;
                while (endOfDigitRun < i2) {
                    char charAt4 = this.jsonish.charAt(endOfDigitRun);
                    if ('0' > charAt4 || charAt4 > '9') {
                        char c = (char) (charAt4 | ' ');
                        if ('a' > c || c > 'f') {
                            break;
                        }
                        i4 = c - 'W';
                    } else {
                        i4 = charAt4 - '0';
                    }
                    j2 = (j2 << 4) | ((long) i4);
                    endOfDigitRun++;
                }
                j = j2;
            } else if (i6 > 1) {
                j = 0;
                for (int i7 = i; i7 < endOfDigitRun; i7++) {
                    int charAt5 = this.jsonish.charAt(i7) - '0';
                    if (charAt5 < 0) {
                        break;
                    }
                    j = (j << 3) | ((long) charAt5);
                }
            } else {
                j = 0;
                if (z) {
                    elide(i, endOfDigitRun);
                    if (j < 0 && (length = this.sanitizedJson.length() - 1) >= 0 && ((charAt2 = this.sanitizedJson.charAt(length)) == '-' || charAt2 == '+')) {
                        elide(length, length + 1);
                        if (charAt2 == '-') {
                            j = -j;
                        }
                    }
                    this.sanitizedJson.append(j);
                }
            }
            z = true;
            if (z) {
            }
        }
        if (endOfDigitRun >= i2 || this.jsonish.charAt(endOfDigitRun) != '.') {
            i3 = endOfDigitRun;
        } else {
            int i8 = endOfDigitRun + 1;
            i3 = endOfDigitRun(i8, i2);
            if (i3 == i8) {
                insert(i8, '0');
            }
        }
        if (i3 < i2 && 101 == (this.jsonish.charAt(i3) | ' ')) {
            int i9 = i3 + 1;
            if (i9 < i2 && ((charAt = this.jsonish.charAt(i9)) == '+' || charAt == '-')) {
                i9++;
            }
            int endOfDigitRun2 = endOfDigitRun(i9, i2);
            if (endOfDigitRun2 == i9) {
                insert(i9, '0');
            }
            i3 = endOfDigitRun2;
        }
        if (i3 != i2) {
            elide(i3, i2);
        }
    }

    private boolean canonicalizeNumber(int i, int i2) {
        elide(i, i);
        int length = this.sanitizedJson.length();
        normalizeNumber(i, i2);
        elide(i2, i2);
        return canonicalizeNumber(this.sanitizedJson, length, this.sanitizedJson.length());
    }

    private static boolean canonicalizeNumber(StringBuilder sb, int i, int i2) {
        int i3;
        int i4;
        char c;
        char charAt;
        char c2 = '-';
        int i5 = i + (sb.charAt(i) == '-' ? 1 : 0);
        int i6 = i5;
        while (i6 < i2 && '0' <= (r9 = sb.charAt(i6)) && r9 <= '9') {
            i6++;
        }
        if (i6 != i2 && '.' == sb.charAt(i6)) {
            do {
                i6++;
                if (i6 >= i2 || '0' > (charAt = sb.charAt(i6))) {
                    break;
                }
            } while (charAt <= '9');
        }
        if (i6 == i2) {
            i3 = i2;
        } else {
            i3 = i6 + 1;
            if (sb.charAt(i3) == '+') {
                i3++;
            }
        }
        if (i2 == i3) {
            i4 = 0;
        } else {
            try {
                i4 = Integer.parseInt(sb.substring(i3, i2), 10);
            } catch (NumberFormatException unused) {
                return false;
            }
        }
        int i7 = i4;
        int i8 = i5;
        int i9 = i8;
        boolean z = false;
        int i10 = 0;
        boolean z2 = true;
        while (i8 < i6) {
            char charAt2 = sb.charAt(i8);
            if (charAt2 == '.') {
                if (z2) {
                    i10 = 0;
                }
                z = true;
            } else {
                if ((!z2 || charAt2 != '0') && !z) {
                    i7++;
                }
                if (charAt2 == '0') {
                    i10++;
                } else {
                    if (z2) {
                        if (z) {
                            i7 -= i10;
                        }
                        i10 = 0;
                    }
                    while (true) {
                        if (i10 == 0 && charAt2 == 0) {
                            break;
                        }
                        if (i10 == 0) {
                            c = 0;
                        } else {
                            i10--;
                            c = charAt2;
                            charAt2 = '0';
                        }
                        sb.setCharAt(i9, charAt2);
                        charAt2 = c;
                        i9++;
                    }
                    z2 = false;
                    i7 = i7;
                }
            }
            i8++;
        }
        sb.setLength(i9);
        int i11 = i9 - i5;
        if (z2) {
            sb.setLength(i);
            sb.append('0');
            return true;
        }
        if (i11 <= i7 && i7 <= 21) {
            while (i11 < i7) {
                sb.append('0');
                i11++;
            }
        } else if (i7 > 0 && i7 <= 21) {
            sb.insert(i5 + i7, '.');
        } else if (-6 >= i7 || i7 > 0) {
            if (i11 != 1) {
                sb.insert(i5 + 1, '.');
            }
            int i12 = i7 - 1;
            sb.append('e');
            if (i12 >= 0) {
                c2 = '+';
            }
            sb.append(c2);
            sb.append(Math.abs(i12));
        } else {
            sb.insert(i5, "0.000000".substring(0, 2 - i7));
        }
        return true;
    }

    private boolean isKeyword(int i, int i2) {
        int i3 = i2 - i;
        if (i3 == 5) {
            return "false".regionMatches(0, this.jsonish, i, i3);
        }
        if (i3 != 4) {
            return false;
        }
        if ("null".regionMatches(0, this.jsonish, i, i3) || "true".regionMatches(0, this.jsonish, i, i3)) {
            return true;
        }
        return false;
    }

    private boolean isOctAt(int i) {
        char charAt = this.jsonish.charAt(i);
        return '0' <= charAt && charAt <= '7';
    }

    private boolean isHexAt(int i) {
        char charAt = this.jsonish.charAt(i);
        if ('0' <= charAt && charAt <= '9') {
            return true;
        }
        char c = (char) (charAt | ' ');
        return 'a' <= c && c <= 'f';
    }

    private boolean isJsonSpecialChar(int i) {
        char charAt = this.jsonish.charAt(i);
        return charAt <= ' ' || charAt == '\"' || charAt == ',' || charAt == ':' || charAt == '[' || charAt == ']' || charAt == '{' || charAt == '}';
    }

    private void appendHex(int i, int i2) {
        int i3 = 0;
        while (i3 < i2) {
            int i4 = i & 15;
            this.sanitizedJson.append(i4 + (i4 < 10 ? 48 : 87));
            i3++;
            i >>>= 4;
        }
    }

    /* access modifiers changed from: private */
    public static final class UnbracketedComma extends Exception {
        private static final long serialVersionUID = 783239978717247850L;

        private UnbracketedComma() {
        }
    }

    private int endOfDigitRun(int i, int i2) {
        while (i < i2) {
            char charAt = this.jsonish.charAt(i);
            if ('0' > charAt || charAt > '9') {
                return i;
            }
            i++;
        }
        return i2;
    }

    static {
        UNBRACKETED_COMMA.setStackTrace(new StackTraceElement[0]);
    }

    /* access modifiers changed from: package-private */
    public CharSequence toCharSequence() {
        CharSequence charSequence = this.sanitizedJson;
        if (charSequence == null) {
            charSequence = this.jsonish;
        }
        return charSequence;
    }

    public String toString() {
        StringBuilder sb = this.sanitizedJson;
        return sb != null ? sb.toString() : this.jsonish;
    }
}
