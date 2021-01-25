package ohos.com.sun.org.apache.regexp.internal;

import java.io.Serializable;
import java.util.Vector;

public class RE implements Serializable {
    static final char E_ALNUM = 'w';
    static final char E_BOUND = 'b';
    static final char E_DIGIT = 'd';
    static final char E_NALNUM = 'W';
    static final char E_NBOUND = 'B';
    static final char E_NDIGIT = 'D';
    static final char E_NSPACE = 'S';
    static final char E_SPACE = 's';
    public static final int MATCH_CASEINDEPENDENT = 1;
    public static final int MATCH_MULTILINE = 2;
    public static final int MATCH_NORMAL = 0;
    public static final int MATCH_SINGLELINE = 4;
    static final int MAX_PAREN = 16;
    static final char OP_ANY = '.';
    static final char OP_ANYOF = '[';
    static final char OP_ATOM = 'A';
    static final char OP_BACKREF = '#';
    static final char OP_BOL = '^';
    static final char OP_BRANCH = '|';
    static final char OP_CLOSE = ')';
    static final char OP_CLOSE_CLUSTER = '>';
    static final char OP_END = 'E';
    static final char OP_EOL = '$';
    static final char OP_ESCAPE = '\\';
    static final char OP_GOTO = 'G';
    static final char OP_MAYBE = '?';
    static final char OP_NOTHING = 'N';
    static final char OP_OPEN = '(';
    static final char OP_OPEN_CLUSTER = '<';
    static final char OP_PLUS = '+';
    static final char OP_POSIXCLASS = 'P';
    static final char OP_RELUCTANTMAYBE = '/';
    static final char OP_RELUCTANTPLUS = '=';
    static final char OP_RELUCTANTSTAR = '8';
    static final char OP_STAR = '*';
    static final char POSIX_CLASS_ALNUM = 'w';
    static final char POSIX_CLASS_ALPHA = 'a';
    static final char POSIX_CLASS_BLANK = 'b';
    static final char POSIX_CLASS_CNTRL = 'c';
    static final char POSIX_CLASS_DIGIT = 'd';
    static final char POSIX_CLASS_GRAPH = 'g';
    static final char POSIX_CLASS_JPART = 'k';
    static final char POSIX_CLASS_JSTART = 'j';
    static final char POSIX_CLASS_LOWER = 'l';
    static final char POSIX_CLASS_PRINT = 'p';
    static final char POSIX_CLASS_PUNCT = '!';
    static final char POSIX_CLASS_SPACE = 's';
    static final char POSIX_CLASS_UPPER = 'u';
    static final char POSIX_CLASS_XDIGIT = 'x';
    public static final int REPLACE_ALL = 0;
    public static final int REPLACE_BACKREFERENCES = 2;
    public static final int REPLACE_FIRSTONLY = 1;
    static final int maxNode = 65536;
    static final int nodeSize = 3;
    static final int offsetNext = 2;
    static final int offsetOpcode = 0;
    static final int offsetOpdata = 1;
    transient int end0;
    transient int end1;
    transient int end2;
    transient int[] endBackref;
    transient int[] endn;
    int matchFlags;
    int maxParen;
    transient int parenCount;
    REProgram program;
    transient CharacterIterator search;
    transient int start0;
    transient int start1;
    transient int start2;
    transient int[] startBackref;
    transient int[] startn;

    public RE(String str) throws RESyntaxException {
        this(str, 0);
    }

    public RE(String str, int i) throws RESyntaxException {
        this(new RECompiler().compile(str));
        setMatchFlags(i);
    }

    public RE(REProgram rEProgram, int i) {
        this.maxParen = 16;
        setProgram(rEProgram);
        setMatchFlags(i);
    }

    public RE(REProgram rEProgram) {
        this(rEProgram, 0);
    }

    public RE() {
        this((REProgram) null, 0);
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    public static String simplePatternToFullRegularExpression(String str) {
        StringBuffer stringBuffer = new StringBuffer();
        for (int i = 0; i < str.length(); i++) {
            char charAt = str.charAt(i);
            if (!(charAt == '$' || charAt == '.' || charAt == '?')) {
                switch (charAt) {
                    case '(':
                    case ')':
                    case '+':
                        break;
                    case '*':
                        stringBuffer.append(".*");
                        continue;
                    default:
                        switch (charAt) {
                            default:
                                switch (charAt) {
                                }
                            case '[':
                            case '\\':
                            case ']':
                            case '^':
                                stringBuffer.append('\\');
                                break;
                        }
                }
                stringBuffer.append(charAt);
            }
            stringBuffer.append('\\');
            stringBuffer.append(charAt);
        }
        return stringBuffer.toString();
    }

    public void setMatchFlags(int i) {
        this.matchFlags = i;
    }

    public int getMatchFlags() {
        return this.matchFlags;
    }

    public void setProgram(REProgram rEProgram) {
        this.program = rEProgram;
        if (rEProgram == null || rEProgram.maxParens == -1) {
            this.maxParen = 16;
        } else {
            this.maxParen = rEProgram.maxParens;
        }
    }

    public REProgram getProgram() {
        return this.program;
    }

    public int getParenCount() {
        return this.parenCount;
    }

    public String getParen(int i) {
        int parenStart;
        if (i >= this.parenCount || (parenStart = getParenStart(i)) < 0) {
            return null;
        }
        return this.search.substring(parenStart, getParenEnd(i));
    }

    public final int getParenStart(int i) {
        if (i >= this.parenCount) {
            return -1;
        }
        if (i == 0) {
            return this.start0;
        }
        if (i == 1) {
            return this.start1;
        }
        if (i == 2) {
            return this.start2;
        }
        if (this.startn == null) {
            allocParens();
        }
        return this.startn[i];
    }

    public final int getParenEnd(int i) {
        if (i >= this.parenCount) {
            return -1;
        }
        if (i == 0) {
            return this.end0;
        }
        if (i == 1) {
            return this.end1;
        }
        if (i == 2) {
            return this.end2;
        }
        if (this.endn == null) {
            allocParens();
        }
        return this.endn[i];
    }

    public final int getParenLength(int i) {
        if (i < this.parenCount) {
            return getParenEnd(i) - getParenStart(i);
        }
        return -1;
    }

    /* access modifiers changed from: protected */
    public final void setParenStart(int i, int i2) {
        if (i >= this.parenCount) {
            return;
        }
        if (i == 0) {
            this.start0 = i2;
        } else if (i == 1) {
            this.start1 = i2;
        } else if (i != 2) {
            if (this.startn == null) {
                allocParens();
            }
            this.startn[i] = i2;
        } else {
            this.start2 = i2;
        }
    }

    /* access modifiers changed from: protected */
    public final void setParenEnd(int i, int i2) {
        if (i >= this.parenCount) {
            return;
        }
        if (i == 0) {
            this.end0 = i2;
        } else if (i == 1) {
            this.end1 = i2;
        } else if (i != 2) {
            if (this.endn == null) {
                allocParens();
            }
            this.endn[i] = i2;
        } else {
            this.end2 = i2;
        }
    }

    /* access modifiers changed from: protected */
    public void internalError(String str) throws Error {
        throw new Error("RE internal error: " + str);
    }

    private final void allocParens() {
        int i = this.maxParen;
        this.startn = new int[i];
        this.endn = new int[i];
        for (int i2 = 0; i2 < this.maxParen; i2++) {
            this.startn[i2] = -1;
            this.endn[i2] = -1;
        }
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    /* access modifiers changed from: protected */
    /* JADX WARNING: Code restructure failed: missing block: B:68:0x00e8, code lost:
        if (r8 != 'w') goto L_0x0360;
     */
    public int matchNodes(int i, int i2, int i3) {
        int matchNodes;
        char[] cArr = this.program.instruction;
        int i4 = i;
        int i5 = i3;
        while (i4 < i2) {
            char c = cArr[i4 + 0];
            int i6 = ((short) cArr[i4 + 2]) + i4;
            char c2 = cArr[i4 + 1];
            boolean z = true;
            if (c == '#') {
                int i7 = 0;
                int i8 = this.startBackref[c2];
                int i9 = this.endBackref[c2];
                if (i8 == -1 || i9 == -1) {
                    return -1;
                }
                if (i8 != i9) {
                    int i10 = i9 - i8;
                    if (this.search.isEnd((i5 + i10) - 1)) {
                        return -1;
                    }
                    if ((this.matchFlags & 1) == 0) {
                        z = false;
                    }
                    while (i7 < i10) {
                        int i11 = i5 + 1;
                        if (compareChars(this.search.charAt(i5), this.search.charAt(i8 + i7), z) != 0) {
                            return -1;
                        }
                        i7++;
                        i5 = i11;
                    }
                }
            } else if (c != '$') {
                if (c == '(') {
                    if ((this.program.flags & 1) != 0) {
                        this.startBackref[c2] = i5;
                    }
                    int matchNodes2 = matchNodes(i6, 65536, i5);
                    if (matchNodes2 != -1) {
                        int i12 = c2 + 1;
                        if (i12 > this.parenCount) {
                            this.parenCount = i12;
                        }
                        if (getParenStart(c2) == -1) {
                            setParenStart(c2, i5);
                        }
                    }
                    return matchNodes2;
                } else if (c != ')') {
                    if (c != '.') {
                        if (c == '/') {
                            int i13 = 0;
                            while (true) {
                                int matchNodes3 = matchNodes(i6, 65536, i5);
                                if (matchNodes3 != -1) {
                                    return matchNodes3;
                                }
                                int i14 = i13 + 1;
                                if (i13 == 0 && (i5 = matchNodes(i4 + 3, i6, i5)) != -1) {
                                    i13 = i14;
                                }
                            }
                            return -1;
                        } else if (c == '8') {
                            do {
                                int matchNodes4 = matchNodes(i6, 65536, i5);
                                if (matchNodes4 != -1) {
                                    return matchNodes4;
                                }
                                i5 = matchNodes(i4 + 3, i6, i5);
                            } while (i5 != -1);
                            return -1;
                        } else if (c != 'A') {
                            if (c == 'E') {
                                setParenEnd(0, i5);
                                return i5;
                            } else if (!(c == 'G' || c == 'N')) {
                                if (c != 'P') {
                                    if (c != '^') {
                                        if (c != '|') {
                                            if (c != '[') {
                                                if (c != '\\') {
                                                    switch (c) {
                                                        case '<':
                                                        case '>':
                                                            return matchNodes(i6, 65536, i5);
                                                        case '=':
                                                            do {
                                                                i5 = matchNodes(i4 + 3, i6, i5);
                                                                if (i5 == -1) {
                                                                    return -1;
                                                                }
                                                                matchNodes = matchNodes(i6, 65536, i5);
                                                            } while (matchNodes == -1);
                                                            return matchNodes;
                                                        default:
                                                            internalError("Invalid opcode '" + ((int) c) + "'");
                                                            break;
                                                    }
                                                } else {
                                                    if (c2 != 'B') {
                                                        if (!(c2 == 'D' || c2 == 'S' || c2 == 'W')) {
                                                            if (c2 != 'b') {
                                                                if (!(c2 == 'd' || c2 == 's' || c2 == 'w')) {
                                                                    internalError("Unrecognized escape '" + ((int) c2) + "'");
                                                                }
                                                            }
                                                        }
                                                        if (this.search.isEnd(i5)) {
                                                            return -1;
                                                        }
                                                        char charAt = this.search.charAt(i5);
                                                        if (c2 != 'D') {
                                                            if (c2 != 'S') {
                                                                if (c2 != 'W') {
                                                                    if (c2 != 'd') {
                                                                        if (c2 != 's') {
                                                                        }
                                                                    }
                                                                }
                                                                boolean z2 = Character.isLetterOrDigit(charAt) || charAt == '_';
                                                                if (c2 != 'w') {
                                                                    z = false;
                                                                }
                                                                if (z2 != z) {
                                                                    return -1;
                                                                }
                                                            }
                                                            boolean isWhitespace = Character.isWhitespace(charAt);
                                                            if (c2 != 's') {
                                                                z = false;
                                                            }
                                                            if (isWhitespace != z) {
                                                                return -1;
                                                            }
                                                        }
                                                        boolean isDigit = Character.isDigit(charAt);
                                                        if (c2 != 'd') {
                                                            z = false;
                                                        }
                                                        if (isDigit != z) {
                                                            return -1;
                                                        }
                                                    }
                                                    boolean z3 = Character.isLetterOrDigit(i5 == 0 ? '\n' : this.search.charAt(i5 + -1)) == Character.isLetterOrDigit(this.search.isEnd(i5) ? '\n' : this.search.charAt(i5));
                                                    if (c2 != 'b') {
                                                        z = false;
                                                    }
                                                    if (z3 == z) {
                                                        return -1;
                                                    }
                                                }
                                            } else if (this.search.isEnd(i5)) {
                                                return -1;
                                            } else {
                                                char charAt2 = this.search.charAt(i5);
                                                boolean z4 = (this.matchFlags & 1) != 0;
                                                int i15 = i4 + 3;
                                                int i16 = (c2 * 2) + i15;
                                                int i17 = i15;
                                                boolean z5 = false;
                                                while (!z5 && i17 < i16) {
                                                    int i18 = i17 + 1;
                                                    int i19 = i18 + 1;
                                                    z5 = compareChars(charAt2, cArr[i17], z4) >= 0 && compareChars(charAt2, cArr[i18], z4) <= 0;
                                                    i17 = i19;
                                                }
                                                if (!z5) {
                                                    return -1;
                                                }
                                            }
                                        } else if (cArr[i6 + 0] != '|') {
                                            i4 += 3;
                                        } else {
                                            do {
                                                int matchNodes5 = matchNodes(i4 + 3, 65536, i5);
                                                if (matchNodes5 != -1) {
                                                    return matchNodes5;
                                                }
                                                short s = (short) cArr[i4 + 2];
                                                i4 += s;
                                                if (s != 0) {
                                                }
                                                return -1;
                                            } while (cArr[i4 + 0] == '|');
                                            return -1;
                                        }
                                    } else if (i5 != 0 && ((this.matchFlags & 2) != 2 || i5 <= 0 || !isNewline(i5 - 1))) {
                                        return -1;
                                    }
                                } else if (this.search.isEnd(i5)) {
                                    return -1;
                                } else {
                                    if (c2 == '!') {
                                        switch (Character.getType(this.search.charAt(i5))) {
                                            case 20:
                                            case 21:
                                            case 22:
                                            case 23:
                                            case 24:
                                                break;
                                            default:
                                                return -1;
                                        }
                                    } else if (c2 == 'g') {
                                        switch (Character.getType(this.search.charAt(i5))) {
                                            case 25:
                                            case 26:
                                            case 27:
                                            case 28:
                                                break;
                                            default:
                                                return -1;
                                        }
                                    } else if (c2 != 'p') {
                                        if (c2 != 's') {
                                            if (c2 != 'u') {
                                                if (c2 != 'w') {
                                                    if (c2 != 'x') {
                                                        switch (c2) {
                                                            case 'a':
                                                                if (!Character.isLetter(this.search.charAt(i5))) {
                                                                    return -1;
                                                                }
                                                                break;
                                                            case 'b':
                                                                if (!Character.isSpaceChar(this.search.charAt(i5))) {
                                                                    return -1;
                                                                }
                                                                break;
                                                            case 'c':
                                                                if (Character.getType(this.search.charAt(i5)) != 15) {
                                                                    return -1;
                                                                }
                                                                break;
                                                            case 'd':
                                                                if (!Character.isDigit(this.search.charAt(i5))) {
                                                                    return -1;
                                                                }
                                                                break;
                                                            default:
                                                                switch (c2) {
                                                                    case 'j':
                                                                        if (!Character.isJavaIdentifierStart(this.search.charAt(i5))) {
                                                                            return -1;
                                                                        }
                                                                        break;
                                                                    case 'k':
                                                                        if (!Character.isJavaIdentifierPart(this.search.charAt(i5))) {
                                                                            return -1;
                                                                        }
                                                                        break;
                                                                    case 'l':
                                                                        if (Character.getType(this.search.charAt(i5)) != 2) {
                                                                            return -1;
                                                                        }
                                                                        break;
                                                                    default:
                                                                        internalError("Bad posix class");
                                                                        break;
                                                                }
                                                        }
                                                    } else {
                                                        if ((this.search.charAt(i5) < '0' || this.search.charAt(i5) > '9') && ((this.search.charAt(i5) < 'a' || this.search.charAt(i5) > 'f') && (this.search.charAt(i5) < 'A' || this.search.charAt(i5) > 'F'))) {
                                                            z = false;
                                                        }
                                                        if (!z) {
                                                            return -1;
                                                        }
                                                    }
                                                } else if (!Character.isLetterOrDigit(this.search.charAt(i5))) {
                                                    return -1;
                                                }
                                            } else if (Character.getType(this.search.charAt(i5)) != 1) {
                                                return -1;
                                            }
                                        } else if (!Character.isWhitespace(this.search.charAt(i5))) {
                                            return -1;
                                        }
                                    } else if (Character.getType(this.search.charAt(i5)) == 15) {
                                        return -1;
                                    }
                                }
                            }
                        } else if (this.search.isEnd(i5)) {
                            return -1;
                        } else {
                            int i20 = i4 + 3;
                            if (this.search.isEnd((c2 + i5) - 1)) {
                                return -1;
                            }
                            if ((this.matchFlags & 1) == 0) {
                                z = false;
                            }
                            int i21 = i5;
                            int i22 = 0;
                            while (i22 < c2) {
                                int i23 = i21 + 1;
                                if (compareChars(this.search.charAt(i21), cArr[i20 + i22], z) != 0) {
                                    return -1;
                                }
                                i22++;
                                i21 = i23;
                            }
                            i5 = i21;
                        }
                    } else if ((this.matchFlags & 4) == 4) {
                        if (this.search.isEnd(i5)) {
                            return -1;
                        }
                    } else if (this.search.isEnd(i5) || isNewline(i5)) {
                        return -1;
                    }
                    i5++;
                } else {
                    if ((this.program.flags & 1) != 0) {
                        this.endBackref[c2] = i5;
                    }
                    int matchNodes6 = matchNodes(i6, 65536, i5);
                    if (matchNodes6 != -1) {
                        int i24 = c2 + 1;
                        if (i24 > this.parenCount) {
                            this.parenCount = i24;
                        }
                        if (getParenEnd(c2) == -1) {
                            setParenEnd(c2, i5);
                        }
                    }
                    return matchNodes6;
                }
            } else if (!this.search.isEnd(0) && !this.search.isEnd(i5) && ((this.matchFlags & 2) != 2 || !isNewline(i5))) {
                return -1;
            }
            i4 = i6;
        }
        internalError("Corrupt program");
        return -1;
    }

    /* access modifiers changed from: protected */
    public boolean matchAt(int i) {
        this.start0 = -1;
        this.end0 = -1;
        this.start1 = -1;
        this.end1 = -1;
        this.start2 = -1;
        this.end2 = -1;
        this.startn = null;
        this.endn = null;
        this.parenCount = 1;
        setParenStart(0, i);
        if ((this.program.flags & 1) != 0) {
            int i2 = this.maxParen;
            this.startBackref = new int[i2];
            this.endBackref = new int[i2];
        }
        int matchNodes = matchNodes(0, 65536, i);
        if (matchNodes != -1) {
            setParenEnd(0, matchNodes);
            return true;
        }
        this.parenCount = 0;
        return false;
    }

    public boolean match(String str, int i) {
        return match(new StringCharacterIterator(str), i);
    }

    public boolean match(CharacterIterator characterIterator, int i) {
        int i2;
        if (this.program == null) {
            internalError("No RE program to run!");
        }
        this.search = characterIterator;
        if (this.program.prefix == null) {
            while (!characterIterator.isEnd(i - 1)) {
                if (matchAt(i)) {
                    return true;
                }
                i++;
            }
            return false;
        }
        boolean z = (this.matchFlags & 1) != 0;
        char[] cArr = this.program.prefix;
        while (!characterIterator.isEnd((cArr.length + i) - 1)) {
            int i3 = i;
            int i4 = 0;
            while (true) {
                int i5 = i3 + 1;
                i2 = i4 + 1;
                if (!(compareChars(characterIterator.charAt(i3), cArr[i4], z) == 0) || i2 >= cArr.length) {
                    break;
                }
                i3 = i5;
                i4 = i2;
            }
            if (i2 == cArr.length && matchAt(i)) {
                return true;
            }
            i++;
        }
        return false;
    }

    public boolean match(String str) {
        return match(str, 0);
    }

    public String[] split(String str) {
        Vector vector = new Vector();
        int length = str.length();
        int i = 0;
        while (i < length && match(str, i)) {
            int parenStart = getParenStart(0);
            int parenEnd = getParenEnd(0);
            if (parenEnd == i) {
                vector.addElement(str.substring(i, parenStart + 1));
                parenEnd++;
            } else {
                vector.addElement(str.substring(i, parenStart));
            }
            i = parenEnd;
        }
        String substring = str.substring(i);
        if (substring.length() != 0) {
            vector.addElement(substring);
        }
        String[] strArr = new String[vector.size()];
        vector.copyInto(strArr);
        return strArr;
    }

    public String subst(String str, String str2) {
        return subst(str, str2, 0);
    }

    public String subst(String str, String str2, int i) {
        int i2;
        char charAt;
        StringBuffer stringBuffer = new StringBuffer();
        int length = str.length();
        int i3 = 0;
        while (i3 < length && match(str, i3)) {
            stringBuffer.append(str.substring(i3, getParenStart(0)));
            if ((i & 2) != 0) {
                int length2 = str2.length();
                boolean z = false;
                int i4 = -2;
                int i5 = 0;
                while (true) {
                    int indexOf = str2.indexOf("$", i5);
                    if (indexOf < 0) {
                        break;
                    }
                    if ((indexOf == 0 || str2.charAt(indexOf - 1) != '\\') && (i2 = indexOf + 1) < length2 && (charAt = str2.charAt(i2)) >= '0' && charAt <= '9') {
                        if (!z) {
                            stringBuffer.append(str2.substring(0, indexOf));
                            z = true;
                        } else {
                            stringBuffer.append(str2.substring(i4 + 2, indexOf));
                        }
                        stringBuffer.append(getParen(charAt - '0'));
                        i4 = indexOf;
                    }
                    i5 = indexOf + 1;
                }
                stringBuffer.append(str2.substring(i4 + 2, length2));
            } else {
                stringBuffer.append(str2);
            }
            int parenEnd = getParenEnd(0);
            if (parenEnd == i3) {
                parenEnd++;
            }
            i3 = parenEnd;
            if ((i & 1) != 0) {
                break;
            }
        }
        if (i3 < length) {
            stringBuffer.append(str.substring(i3));
        }
        return stringBuffer.toString();
    }

    public String[] grep(Object[] objArr) {
        Vector vector = new Vector();
        for (Object obj : objArr) {
            String obj2 = obj.toString();
            if (match(obj2)) {
                vector.addElement(obj2);
            }
        }
        String[] strArr = new String[vector.size()];
        vector.copyInto(strArr);
        return strArr;
    }

    private boolean isNewline(int i) {
        char charAt = this.search.charAt(i);
        return charAt == '\n' || charAt == '\r' || charAt == 133 || charAt == 8232 || charAt == 8233;
    }

    private int compareChars(char c, char c2, boolean z) {
        if (z) {
            c = Character.toLowerCase(c);
            c2 = Character.toLowerCase(c2);
        }
        return c - c2;
    }
}
