package ohos.com.sun.org.apache.regexp.internal;

import java.util.Hashtable;
import ohos.agp.styles.attributes.ViewAttrsConstants;
import ohos.com.sun.org.apache.xalan.internal.templates.Constants;
import ohos.com.sun.org.apache.xml.internal.serializer.CharInfo;
import ohos.data.search.model.SearchParameter;
import ohos.global.icu.impl.locale.UnicodeLocaleExtension;

public class RECompiler {
    static final int ESC_BACKREF = 1048575;
    static final int ESC_CLASS = 1048573;
    static final int ESC_COMPLEX = 1048574;
    static final int ESC_MASK = 1048560;
    static final int NODE_NORMAL = 0;
    static final int NODE_NULLABLE = 1;
    static final int NODE_TOPLEVEL = 2;
    static final int bracketUnbounded = -1;
    static Hashtable hashPOSIX = new Hashtable();
    int[] bracketEnd = null;
    int[] bracketMin = null;
    int[] bracketOpt = null;
    int[] bracketStart = null;
    int brackets = 0;
    int idx;
    char[] instruction = new char[128];
    int len;
    int lenInstruction = 0;
    int maxBrackets = 10;
    int parens;
    String pattern;

    static {
        hashPOSIX.put("alnum", new Character('w'));
        hashPOSIX.put(ViewAttrsConstants.ALPHA, new Character('a'));
        hashPOSIX.put("blank", new Character('b'));
        hashPOSIX.put("cntrl", new Character('c'));
        hashPOSIX.put(Constants.ATTRNAME_DIGIT, new Character('d'));
        hashPOSIX.put("graph", new Character('g'));
        hashPOSIX.put(SearchParameter.LOWER, new Character('l'));
        hashPOSIX.put("print", new Character('p'));
        hashPOSIX.put("punct", new Character('!'));
        hashPOSIX.put("space", new Character('s'));
        hashPOSIX.put(SearchParameter.UPPER, new Character(UnicodeLocaleExtension.SINGLETON));
        hashPOSIX.put("xdigit", new Character('x'));
        hashPOSIX.put("javastart", new Character('j'));
        hashPOSIX.put("javapart", new Character('k'));
    }

    /* access modifiers changed from: package-private */
    public void ensure(int i) {
        int length = this.instruction.length;
        if (this.lenInstruction + i >= length) {
            while (true) {
                int i2 = this.lenInstruction;
                if (i2 + i >= length) {
                    length *= 2;
                } else {
                    char[] cArr = new char[length];
                    System.arraycopy(this.instruction, 0, cArr, 0, i2);
                    this.instruction = cArr;
                    return;
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void emit(char c) {
        ensure(1);
        char[] cArr = this.instruction;
        int i = this.lenInstruction;
        this.lenInstruction = i + 1;
        cArr[i] = c;
    }

    /* access modifiers changed from: package-private */
    public void nodeInsert(char c, int i, int i2) {
        ensure(3);
        char[] cArr = this.instruction;
        System.arraycopy(cArr, i2, cArr, i2 + 3, this.lenInstruction - i2);
        char[] cArr2 = this.instruction;
        cArr2[i2 + 0] = c;
        cArr2[i2 + 1] = (char) i;
        cArr2[i2 + 2] = 0;
        this.lenInstruction += 3;
    }

    /* access modifiers changed from: package-private */
    public void setNextOfEnd(int i, int i2) {
        char c = this.instruction[i + 2];
        while (c != 0) {
            int i3 = this.lenInstruction;
            if (i >= i3) {
                break;
            }
            if (i == i2) {
                i2 = i3;
            }
            i += c;
            c = this.instruction[i + 2];
        }
        if (i < this.lenInstruction) {
            this.instruction[i + 2] = (char) ((short) (i2 - i));
        }
    }

    /* access modifiers changed from: package-private */
    public int node(char c, int i) {
        ensure(3);
        char[] cArr = this.instruction;
        int i2 = this.lenInstruction;
        cArr[i2 + 0] = c;
        cArr[i2 + 1] = (char) i;
        cArr[i2 + 2] = 0;
        this.lenInstruction = i2 + 3;
        return this.lenInstruction - 3;
    }

    /* access modifiers changed from: package-private */
    public void internalError() throws Error {
        throw new Error("Internal error!");
    }

    /* access modifiers changed from: package-private */
    public void syntaxError(String str) throws RESyntaxException {
        throw new RESyntaxException(str);
    }

    /* access modifiers changed from: package-private */
    public void allocBrackets() {
        if (this.bracketStart == null) {
            int i = this.maxBrackets;
            this.bracketStart = new int[i];
            this.bracketEnd = new int[i];
            this.bracketMin = new int[i];
            this.bracketOpt = new int[i];
            for (int i2 = 0; i2 < this.maxBrackets; i2++) {
                int[] iArr = this.bracketStart;
                int[] iArr2 = this.bracketEnd;
                int[] iArr3 = this.bracketMin;
                this.bracketOpt[i2] = -1;
                iArr3[i2] = -1;
                iArr2[i2] = -1;
                iArr[i2] = -1;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public synchronized void reallocBrackets() {
        if (this.bracketStart == null) {
            allocBrackets();
        }
        int i = this.maxBrackets * 2;
        int[] iArr = new int[i];
        int[] iArr2 = new int[i];
        int[] iArr3 = new int[i];
        int[] iArr4 = new int[i];
        for (int i2 = this.brackets; i2 < i; i2++) {
            iArr4[i2] = -1;
            iArr3[i2] = -1;
            iArr2[i2] = -1;
            iArr[i2] = -1;
        }
        System.arraycopy(this.bracketStart, 0, iArr, 0, this.brackets);
        System.arraycopy(this.bracketEnd, 0, iArr2, 0, this.brackets);
        System.arraycopy(this.bracketMin, 0, iArr3, 0, this.brackets);
        System.arraycopy(this.bracketOpt, 0, iArr4, 0, this.brackets);
        this.bracketStart = iArr;
        this.bracketEnd = iArr2;
        this.bracketMin = iArr3;
        this.bracketOpt = iArr4;
        this.maxBrackets = i;
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x00a0, code lost:
        if (r7.charAt(r2) != ',') goto L_0x00a2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:3:0x0014, code lost:
        if (r2.charAt(r1) != '{') goto L_0x0016;
     */
    public void bracket() throws RESyntaxException {
        int i = this.idx;
        if (i < this.len) {
            String str = this.pattern;
            this.idx = i + 1;
        }
        internalError();
        int i2 = this.idx;
        if (i2 >= this.len || !Character.isDigit(this.pattern.charAt(i2))) {
            syntaxError("Expected digit");
        }
        StringBuffer stringBuffer = new StringBuffer();
        while (true) {
            int i3 = this.idx;
            if (i3 >= this.len || !Character.isDigit(this.pattern.charAt(i3))) {
                try {
                    this.bracketMin[this.brackets] = Integer.parseInt(stringBuffer.toString());
                } catch (NumberFormatException unused) {
                    syntaxError("Expected valid number");
                }
                if (this.idx >= this.len) {
                    syntaxError("Expected comma or right bracket");
                }
                if (this.pattern.charAt(this.idx) == '}') {
                    this.idx++;
                    this.bracketOpt[this.brackets] = 0;
                    return;
                }
                int i4 = this.idx;
                if (i4 < this.len) {
                    String str2 = this.pattern;
                    this.idx = i4 + 1;
                }
                syntaxError("Expected comma");
                if (this.idx >= this.len) {
                    syntaxError("Expected comma or right bracket");
                }
                if (this.pattern.charAt(this.idx) == '}') {
                    this.idx++;
                    this.bracketOpt[this.brackets] = -1;
                    return;
                }
                int i5 = this.idx;
                if (i5 >= this.len || !Character.isDigit(this.pattern.charAt(i5))) {
                    syntaxError("Expected digit");
                }
                stringBuffer.setLength(0);
                while (true) {
                    int i6 = this.idx;
                    if (i6 >= this.len || !Character.isDigit(this.pattern.charAt(i6))) {
                        try {
                            this.bracketOpt[this.brackets] = Integer.parseInt(stringBuffer.toString()) - this.bracketMin[this.brackets];
                        } catch (NumberFormatException unused2) {
                            syntaxError("Expected valid number");
                        }
                        if (this.bracketOpt[this.brackets] < 0) {
                            syntaxError("Bad range");
                        }
                        int i7 = this.idx;
                        if (i7 < this.len) {
                            String str3 = this.pattern;
                            this.idx = i7 + 1;
                            if (str3.charAt(i7) == '}') {
                                return;
                            }
                        }
                        syntaxError("Missing close brace");
                        return;
                    }
                    String str4 = this.pattern;
                    int i8 = this.idx;
                    this.idx = i8 + 1;
                    stringBuffer.append(str4.charAt(i8));
                }
            } else {
                String str5 = this.pattern;
                int i9 = this.idx;
                this.idx = i9 + 1;
                stringBuffer.append(str5.charAt(i9));
            }
        }
    }

    /* access modifiers changed from: package-private */
    public int escape() throws RESyntaxException {
        if (this.pattern.charAt(this.idx) != '\\') {
            internalError();
        }
        if (this.idx + 1 == this.len) {
            syntaxError("Escape terminates string");
        }
        int i = 2;
        this.idx += 2;
        char charAt = this.pattern.charAt(this.idx - 1);
        if (charAt == 'B') {
            return ESC_COMPLEX;
        }
        if (charAt == 'D' || charAt == 'S' || charAt == 'W') {
            return ESC_CLASS;
        }
        if (charAt == 'b') {
            return ESC_COMPLEX;
        }
        if (charAt == 'd') {
            return ESC_CLASS;
        }
        if (charAt == 'f') {
            return 12;
        }
        if (charAt == 'n') {
            return 10;
        }
        if (charAt == 'w') {
            return ESC_CLASS;
        }
        if (charAt != 'x') {
            switch (charAt) {
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                    int i2 = this.idx;
                    if ((i2 >= this.len || !Character.isDigit(this.pattern.charAt(i2))) && charAt != '0') {
                        return ESC_BACKREF;
                    }
                    int i3 = charAt - '0';
                    int i4 = this.idx;
                    if (i4 >= this.len || !Character.isDigit(this.pattern.charAt(i4))) {
                        return i3;
                    }
                    String str = this.pattern;
                    int i5 = this.idx;
                    this.idx = i5 + 1;
                    int charAt2 = (i3 << 3) + (str.charAt(i5) - '0');
                    int i6 = this.idx;
                    if (i6 >= this.len || !Character.isDigit(this.pattern.charAt(i6))) {
                        return charAt2;
                    }
                    String str2 = this.pattern;
                    int i7 = this.idx;
                    this.idx = i7 + 1;
                    return (charAt2 << 3) + (str2.charAt(i7) - '0');
                default:
                    switch (charAt) {
                        case 'r':
                            return 13;
                        case 's':
                            return ESC_CLASS;
                        case 't':
                            return 9;
                        case 'u':
                            break;
                        default:
                            return charAt;
                    }
            }
        }
        if (charAt == 'u') {
            i = 4;
        }
        int i8 = 0;
        while (true) {
            int i9 = this.idx;
            if (i9 < this.len) {
                int i10 = i - 1;
                if (i > 0) {
                    char charAt3 = this.pattern.charAt(i9);
                    if (charAt3 < '0' || charAt3 > '9') {
                        char lowerCase = Character.toLowerCase(charAt3);
                        if (lowerCase < 'a' || lowerCase > 'f') {
                            syntaxError("Expected " + i10 + " hexadecimal digits after \\" + charAt);
                        } else {
                            i8 = (i8 << 4) + (lowerCase - 'a') + 10;
                        }
                    } else {
                        i8 = ((i8 << 4) + charAt3) - 48;
                    }
                    this.idx++;
                    i = i10;
                }
            }
        }
        return i8;
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:6:0x0025, code lost:
        if (r3.charAt(r1) == ']') goto L_0x0027;
     */
    public int characterClass() throws RESyntaxException {
        char c;
        if (this.pattern.charAt(this.idx) != '[') {
            internalError();
        }
        int i = this.idx;
        char c2 = ']';
        if (i + 1 < this.len) {
            String str = this.pattern;
            int i2 = i + 1;
            this.idx = i2;
        }
        syntaxError("Empty or unterminated class");
        int i3 = this.idx;
        if (i3 < this.len && this.pattern.charAt(i3) == ':') {
            this.idx++;
            int i4 = this.idx;
            while (true) {
                int i5 = this.idx;
                if (i5 >= this.len || this.pattern.charAt(i5) < 'a' || this.pattern.charAt(this.idx) > 'z') {
                    break;
                }
                this.idx++;
            }
            int i6 = this.idx;
            if (i6 + 1 < this.len && this.pattern.charAt(i6) == ':' && this.pattern.charAt(this.idx + 1) == ']') {
                String substring = this.pattern.substring(i4, this.idx);
                Character ch = (Character) hashPOSIX.get(substring);
                if (ch != null) {
                    this.idx += 2;
                    return node('P', ch.charValue());
                }
                syntaxError("Invalid POSIX character class '" + substring + "'");
            }
            syntaxError("Invalid POSIX character class syntax");
        }
        int node = node('[', 0);
        int i7 = this.idx;
        RERange rERange = new RERange();
        boolean z = false;
        char c3 = 0;
        boolean z2 = true;
        char c4 = 65535;
        while (true) {
            int i8 = this.idx;
            if (i8 < this.len && this.pattern.charAt(i8) != c2) {
                char charAt = this.pattern.charAt(this.idx);
                if (charAt != '-') {
                    if (charAt == '\\') {
                        int escape = escape();
                        switch (escape) {
                            case ESC_COMPLEX /* 1048574 */:
                            case ESC_BACKREF /* 1048575 */:
                                syntaxError("Bad character class");
                            case ESC_CLASS /* 1048573 */:
                                if (z) {
                                    syntaxError("Bad character class");
                                }
                                char charAt2 = this.pattern.charAt(this.idx - 1);
                                if (charAt2 == 'D' || charAt2 == 'S' || charAt2 == 'W') {
                                    syntaxError("Bad character class");
                                } else {
                                    if (charAt2 != 'd') {
                                        if (charAt2 != 's') {
                                            if (charAt2 == 'w') {
                                                rERange.include(97, 122, z2);
                                                rERange.include(65, 90, z2);
                                                rERange.include('_', z2);
                                            }
                                            c4 = 65535;
                                            break;
                                        }
                                    }
                                    rERange.include(48, 57, z2);
                                    c4 = 65535;
                                }
                                rERange.include('\t', z2);
                                rERange.include(CharInfo.S_CARRIAGERETURN, z2);
                                rERange.include('\f', z2);
                                rERange.include('\n', z2);
                                rERange.include('\b', z2);
                                rERange.include(' ', z2);
                                c4 = 65535;
                                break;
                            default:
                                c4 = (char) escape;
                                break;
                        }
                        c2 = ']';
                    } else if (charAt != '^') {
                        String str2 = this.pattern;
                        int i9 = this.idx;
                        this.idx = i9 + 1;
                        c4 = str2.charAt(i9);
                    } else {
                        z2 = !z2;
                        if (this.idx == i7) {
                            rERange.include(0, 65535, true);
                        }
                        this.idx++;
                        c2 = ']';
                    }
                    c = ']';
                } else {
                    if (z) {
                        syntaxError("Bad class range");
                    }
                    c3 = c4 == 65535 ? 0 : c4;
                    int i10 = this.idx;
                    if (i10 + 1 < this.len) {
                        String str3 = this.pattern;
                        int i11 = i10 + 1;
                        this.idx = i11;
                        char charAt3 = str3.charAt(i11);
                        c = ']';
                        if (charAt3 == ']') {
                            z = true;
                            c4 = 65535;
                        }
                    } else {
                        c = ']';
                    }
                    z = true;
                    c2 = c;
                }
                if (z) {
                    if (c3 >= c4) {
                        syntaxError("Bad character class");
                    }
                    rERange.include(c3, c4, z2);
                    z = false;
                    c4 = 65535;
                } else {
                    int i12 = this.idx;
                    if (i12 >= this.len || this.pattern.charAt(i12) != '-') {
                        rERange.include(c4, z2);
                    }
                }
                c2 = c;
            }
        }
        if (this.idx == this.len) {
            syntaxError("Unterminated character class");
        }
        this.idx++;
        this.instruction[node + 1] = (char) rERange.num;
        for (int i13 = 0; i13 < rERange.num; i13++) {
            emit((char) rERange.minRange[i13]);
            emit((char) rERange.maxRange[i13]);
        }
        return node;
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x0091, code lost:
        if (r0 != 0) goto L_0x0098;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x0093, code lost:
        syntaxError("Missing operand to closure");
     */
    public int atom() throws RESyntaxException {
        int i = 0;
        int node = node('A', 0);
        while (true) {
            int i2 = this.idx;
            int i3 = this.len;
            if (i2 < i3) {
                if (i2 + 1 < i3) {
                    char charAt = this.pattern.charAt(i2 + 1);
                    if (this.pattern.charAt(this.idx) == '\\') {
                        int i4 = this.idx;
                        escape();
                        int i5 = this.idx;
                        if (i5 < this.len) {
                            charAt = this.pattern.charAt(i5);
                        }
                        this.idx = i4;
                    }
                    if ((charAt == '*' || charAt == '+' || charAt == '?' || charAt == '{') && i != 0) {
                    }
                }
                char charAt2 = this.pattern.charAt(this.idx);
                if (!(charAt2 == '$' || charAt2 == '.')) {
                    if (!(charAt2 == '?' || charAt2 == '{')) {
                        if (charAt2 != '|') {
                            switch (charAt2) {
                                case '(':
                                case ')':
                                    break;
                                case '*':
                                case '+':
                                    break;
                                default:
                                    switch (charAt2) {
                                        case '[':
                                        case ']':
                                        case '^':
                                            break;
                                        case '\\':
                                            int i6 = this.idx;
                                            int escape = escape();
                                            if ((escape & ESC_MASK) != ESC_MASK) {
                                                emit((char) escape);
                                                break;
                                            } else {
                                                this.idx = i6;
                                                break;
                                            }
                                        default:
                                            String str = this.pattern;
                                            int i7 = this.idx;
                                            this.idx = i7 + 1;
                                            emit(str.charAt(i7));
                                            break;
                                    }
                                    i++;
                            }
                        }
                    }
                }
            }
        }
        if (i == 0) {
            internalError();
        }
        this.instruction[node + 1] = (char) i;
        return node;
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    /*  JADX ERROR: JadxRuntimeException in pass: RegionMakerVisitor
        jadx.core.utils.exceptions.JadxRuntimeException: Failed to find switch 'out' block
        	at jadx.core.dex.visitors.regions.RegionMaker.processSwitch(RegionMaker.java:821)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverse(RegionMaker.java:157)
        	at jadx.core.dex.visitors.regions.RegionMaker.makeRegion(RegionMaker.java:94)
        	at jadx.core.dex.visitors.regions.RegionMaker.processIf(RegionMaker.java:731)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverse(RegionMaker.java:152)
        	at jadx.core.dex.visitors.regions.RegionMaker.makeRegion(RegionMaker.java:94)
        	at jadx.core.dex.visitors.regions.RegionMaker.processIf(RegionMaker.java:731)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverse(RegionMaker.java:152)
        	at jadx.core.dex.visitors.regions.RegionMaker.makeRegion(RegionMaker.java:94)
        	at jadx.core.dex.visitors.regions.RegionMakerVisitor.visit(RegionMakerVisitor.java:50)
        */
    int terminal(int[] r4) throws ohos.com.sun.org.apache.regexp.internal.RESyntaxException {
        /*
            r3 = this;
            java.lang.String r0 = r3.pattern
            int r1 = r3.idx
            char r0 = r0.charAt(r1)
            r1 = 0
            if (r0 == 0) goto L_0x0051
            r2 = 36
            if (r0 == r2) goto L_0x0040
            r2 = 46
            if (r0 == r2) goto L_0x0040
            r2 = 63
            if (r0 == r2) goto L_0x0056
            r2 = 123(0x7b, float:1.72E-43)
            if (r0 == r2) goto L_0x0056
            r2 = 124(0x7c, float:1.74E-43)
            if (r0 == r2) goto L_0x0037
            switch(r0) {
                case 40: goto L_0x0032;
                case 41: goto L_0x002c;
                case 42: goto L_0x0056;
                case 43: goto L_0x0056;
                default: goto L_0x0022;
            }
        L_0x0022:
            switch(r0) {
                case 91: goto L_0x0027;
                case 92: goto L_0x005b;
                case 93: goto L_0x003a;
                case 94: goto L_0x0040;
                default: goto L_0x0025;
            }
        L_0x0025:
            goto L_0x00a7
        L_0x0027:
            int r3 = r3.characterClass()
            return r3
        L_0x002c:
            java.lang.String r0 = "Unexpected close paren"
            r3.syntaxError(r0)
            goto L_0x0037
        L_0x0032:
            int r3 = r3.expr(r4)
            return r3
        L_0x0037:
            r3.internalError()
        L_0x003a:
            java.lang.String r0 = "Mismatched class"
            r3.syntaxError(r0)
            goto L_0x0051
        L_0x0040:
            java.lang.String r4 = r3.pattern
            int r0 = r3.idx
            int r2 = r0 + 1
            r3.idx = r2
            char r4 = r4.charAt(r0)
            int r3 = r3.node(r4, r1)
            return r3
        L_0x0051:
            java.lang.String r0 = "Unexpected end of input"
            r3.syntaxError(r0)
        L_0x0056:
            java.lang.String r0 = "Missing operand to closure"
            r3.syntaxError(r0)
        L_0x005b:
            int r0 = r3.idx
            int r2 = r3.escape()
            switch(r2) {
                case 1048573: goto L_0x0090;
                case 1048574: goto L_0x0090;
                case 1048575: goto L_0x006d;
                default: goto L_0x0064;
            }
        L_0x0064:
            r3.idx = r0
            r0 = r4[r1]
            r0 = r0 & -2
            r4[r1] = r0
            goto L_0x00a7
        L_0x006d:
            java.lang.String r0 = r3.pattern
            int r2 = r3.idx
            int r2 = r2 + -1
            char r0 = r0.charAt(r2)
            int r0 = r0 + -48
            char r0 = (char) r0
            int r2 = r3.parens
            if (r2 > r0) goto L_0x0083
            java.lang.String r2 = "Bad backreference"
            r3.syntaxError(r2)
        L_0x0083:
            r2 = r4[r1]
            r2 = r2 | 1
            r4[r1] = r2
            r4 = 35
            int r3 = r3.node(r4, r0)
            return r3
        L_0x0090:
            r0 = r4[r1]
            r0 = r0 & -2
            r4[r1] = r0
            r4 = 92
            java.lang.String r0 = r3.pattern
            int r1 = r3.idx
            int r1 = r1 + -1
            char r0 = r0.charAt(r1)
            int r3 = r3.node(r4, r0)
            return r3
        L_0x00a7:
            r0 = r4[r1]
            r0 = r0 & -2
            r4[r1] = r0
            int r3 = r3.atom()
            return r3
            switch-data {40->0x0032, 41->0x002c, 42->0x0056, 43->0x0056, }
            switch-data {91->0x0027, 92->0x005b, 93->0x003a, 94->0x0040, }
            switch-data {1048573->0x0090, 1048574->0x0090, 1048575->0x006d, }
        */
        throw new UnsupportedOperationException("Method not decompiled: ohos.com.sun.org.apache.regexp.internal.RECompiler.terminal(int[]):int");
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:7:0x002e, code lost:
        if (r5 != '{') goto L_0x0058;
     */
    /* JADX WARNING: Removed duplicated region for block: B:17:0x0053  */
    /* JADX WARNING: Removed duplicated region for block: B:25:0x0070  */
    /* JADX WARNING: Removed duplicated region for block: B:76:0x019d  */
    public int closure(int[] iArr) throws RESyntaxException {
        int i;
        boolean z;
        int i2 = this.idx;
        boolean z2 = true;
        int i3 = 0;
        int[] iArr2 = {0};
        int terminal = terminal(iArr2);
        iArr[0] = iArr[0] | iArr2[0];
        int i4 = this.idx;
        if (i4 >= this.len) {
            return terminal;
        }
        char charAt = this.pattern.charAt(i4);
        if (charAt != '*') {
            if (charAt != '+') {
                if (charAt != '?') {
                }
            }
            this.idx++;
            char c = this.instruction[terminal + 0];
            if (c == '^' || c == '$') {
                syntaxError("Bad closure operand");
            }
            if ((iArr2[0] & 1) != 0) {
                syntaxError("Closure operand can't be nullable");
            }
            i = this.idx;
            if (i < this.len || this.pattern.charAt(i) != '?') {
                z = true;
            } else {
                this.idx++;
                z = false;
            }
            if (!z) {
                if (charAt != '*') {
                    if (charAt == '+') {
                        int node = node('|', 0);
                        setNextOfEnd(terminal, node);
                        setNextOfEnd(node('G', 0), terminal);
                        setNextOfEnd(node, node('|', 0));
                        setNextOfEnd(terminal, node('N', 0));
                    } else if (charAt != '?') {
                        if (charAt == '{') {
                            allocBrackets();
                            int i5 = 0;
                            while (true) {
                                if (i5 >= this.brackets) {
                                    z2 = false;
                                    break;
                                } else if (this.bracketStart[i5] == this.idx) {
                                    break;
                                } else {
                                    i5++;
                                }
                            }
                            if (!z2) {
                                if (this.brackets >= this.maxBrackets) {
                                    reallocBrackets();
                                }
                                this.bracketStart[this.brackets] = this.idx;
                                bracket();
                                int[] iArr3 = this.bracketEnd;
                                i5 = this.brackets;
                                iArr3[i5] = this.idx;
                                this.brackets = i5 + 1;
                            }
                            int[] iArr4 = this.bracketMin;
                            int i6 = iArr4[i5];
                            iArr4[i5] = i6 - 1;
                            if (i6 <= 0) {
                                int[] iArr5 = this.bracketOpt;
                                if (iArr5[i5] == -1) {
                                    iArr5[i5] = 0;
                                    this.idx = this.bracketEnd[i5];
                                    charAt = '*';
                                } else {
                                    int i7 = iArr5[i5];
                                    iArr5[i5] = i7 - 1;
                                    if (i7 > 0) {
                                        if (iArr5[i5] > 0) {
                                            this.idx = i2;
                                        } else {
                                            this.idx = this.bracketEnd[i5];
                                        }
                                        charAt = '?';
                                    } else {
                                        this.lenInstruction = terminal;
                                        node('N', 0);
                                        this.idx = this.bracketEnd[i5];
                                    }
                                }
                            } else if (iArr4[i5] > 0 || this.bracketOpt[i5] != 0) {
                                while (true) {
                                    int i8 = this.brackets;
                                    if (i3 >= i8) {
                                        break;
                                    }
                                    if (i3 != i5) {
                                        int[] iArr6 = this.bracketStart;
                                        if (iArr6[i3] < this.idx && iArr6[i3] >= i2) {
                                            this.brackets = i8 - 1;
                                            int i9 = this.brackets;
                                            iArr6[i3] = iArr6[i9];
                                            int[] iArr7 = this.bracketEnd;
                                            iArr7[i3] = iArr7[i9];
                                            int[] iArr8 = this.bracketMin;
                                            iArr8[i3] = iArr8[i9];
                                            int[] iArr9 = this.bracketOpt;
                                            iArr9[i3] = iArr9[i9];
                                        }
                                    }
                                    i3++;
                                }
                                this.idx = i2;
                            } else {
                                this.idx = this.bracketEnd[i5];
                            }
                        }
                    }
                }
                if (z) {
                    if (charAt == '?') {
                        nodeInsert('|', 0, terminal);
                        setNextOfEnd(terminal, node('|', 0));
                        int node2 = node('N', 0);
                        setNextOfEnd(terminal, node2);
                        setNextOfEnd(terminal + 3, node2);
                    }
                    if (charAt == '*') {
                        nodeInsert('|', 0, terminal);
                        int i10 = terminal + 3;
                        setNextOfEnd(i10, node('|', 0));
                        setNextOfEnd(i10, node('G', 0));
                        setNextOfEnd(i10, terminal);
                        setNextOfEnd(terminal, node('|', 0));
                        setNextOfEnd(terminal, node('N', 0));
                    }
                }
            } else {
                setNextOfEnd(terminal, node('E', 0));
                if (charAt == '*') {
                    nodeInsert('8', 0, terminal);
                } else if (charAt == '+') {
                    nodeInsert('=', 0, terminal);
                } else if (charAt == '?') {
                    nodeInsert('/', 0, terminal);
                }
                setNextOfEnd(terminal, this.lenInstruction);
            }
            return terminal;
        }
        iArr[0] = iArr[0] | 1;
        this.idx++;
        char c2 = this.instruction[terminal + 0];
        syntaxError("Bad closure operand");
        if ((iArr2[0] & 1) != 0) {
        }
        i = this.idx;
        if (i < this.len) {
        }
        z = true;
        if (!z) {
        }
        return terminal;
    }

    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x0027: APUT  (r4v0 int[]), (0 ??[int, short, byte, char]), (0 int) */
    /* access modifiers changed from: package-private */
    public int branch(int[] iArr) throws RESyntaxException {
        int node = node('|', 0);
        int[] iArr2 = new int[1];
        boolean z = true;
        int i = -1;
        while (true) {
            int i2 = this.idx;
            if (i2 >= this.len || this.pattern.charAt(i2) == '|' || this.pattern.charAt(this.idx) == ')') {
                break;
            }
            iArr2[0] = 0;
            int closure = closure(iArr2);
            if (iArr2[0] == 0) {
                z = false;
            }
            if (i != -1) {
                setNextOfEnd(i, closure);
            }
            i = closure;
        }
        if (i == -1) {
            node('N', 0);
        }
        if (z) {
            iArr[0] = iArr[0] | 1;
        }
        return node;
    }

    /* access modifiers changed from: package-private */
    public int expr(int[] iArr) throws RESyntaxException {
        int i;
        int i2;
        int i3 = this.parens;
        char c = 2;
        if ((iArr[0] & 2) == 0 && this.pattern.charAt(this.idx) == '(') {
            int i4 = this.idx;
            if (i4 + 2 < this.len && this.pattern.charAt(i4 + 1) == '?' && this.pattern.charAt(this.idx + 2) == ':') {
                this.idx += 3;
                i = node('<', 0);
            } else {
                this.idx++;
                int i5 = this.parens;
                this.parens = i5 + 1;
                i = node('(', i5);
                c = 1;
            }
        } else {
            i = -1;
            c = 65535;
        }
        iArr[0] = iArr[0] & -3;
        int branch = branch(iArr);
        if (i == -1) {
            i = branch;
        } else {
            setNextOfEnd(i, branch);
        }
        while (true) {
            int i6 = this.idx;
            if (i6 >= this.len || this.pattern.charAt(i6) != '|') {
                break;
            }
            this.idx++;
            setNextOfEnd(i, branch(iArr));
        }
        if (c > 0) {
            int i7 = this.idx;
            if (i7 >= this.len || this.pattern.charAt(i7) != ')') {
                syntaxError("Missing close paren");
            } else {
                this.idx++;
            }
            if (c == 1) {
                i2 = node(')', i3);
            } else {
                i2 = node('>', 0);
            }
        } else {
            i2 = node('E', 0);
        }
        setNextOfEnd(i, i2);
        char c2 = this.instruction[i + 2];
        int i8 = i;
        while (c2 != 0 && i8 < this.lenInstruction) {
            if (this.instruction[i8 + 0] == '|') {
                setNextOfEnd(i8 + 3, i2);
            }
            c2 = this.instruction[i8 + 2];
            i8 += c2;
        }
        return i;
    }

    public REProgram compile(String str) throws RESyntaxException {
        this.pattern = str;
        this.len = str.length();
        this.idx = 0;
        this.lenInstruction = 0;
        this.parens = 1;
        this.brackets = 0;
        expr(new int[]{2});
        int i = this.idx;
        if (i != this.len) {
            if (str.charAt(i) == ')') {
                syntaxError("Unmatched close paren");
            }
            syntaxError("Unexpected input remains");
        }
        int i2 = this.lenInstruction;
        char[] cArr = new char[i2];
        System.arraycopy(this.instruction, 0, cArr, 0, i2);
        return new REProgram(this.parens, cArr);
    }

    /* access modifiers changed from: package-private */
    public class RERange {
        int[] maxRange;
        int[] minRange;
        int num;
        int size = 16;

        RERange() {
            int i = this.size;
            this.minRange = new int[i];
            this.maxRange = new int[i];
            this.num = 0;
        }

        /* access modifiers changed from: package-private */
        public void delete(int i) {
            int i2 = this.num;
            if (i2 != 0 && i < i2) {
                while (true) {
                    i++;
                    int i3 = this.num;
                    if (i < i3) {
                        int i4 = i - 1;
                        if (i4 >= 0) {
                            int[] iArr = this.minRange;
                            iArr[i4] = iArr[i];
                            int[] iArr2 = this.maxRange;
                            iArr2[i4] = iArr2[i];
                        }
                    } else {
                        this.num = i3 - 1;
                        return;
                    }
                }
            }
        }

        /* access modifiers changed from: package-private */
        public void merge(int i, int i2) {
            int i3 = 0;
            while (true) {
                int i4 = this.num;
                if (i3 >= i4) {
                    int i5 = this.size;
                    if (i4 >= i5) {
                        this.size = i5 * 2;
                        int i6 = this.size;
                        int[] iArr = new int[i6];
                        int[] iArr2 = new int[i6];
                        System.arraycopy(this.minRange, 0, iArr, 0, i4);
                        System.arraycopy(this.maxRange, 0, iArr2, 0, this.num);
                        this.minRange = iArr;
                        this.maxRange = iArr2;
                    }
                    int[] iArr3 = this.minRange;
                    int i7 = this.num;
                    iArr3[i7] = i;
                    this.maxRange[i7] = i2;
                    this.num = i7 + 1;
                    return;
                } else if (i >= this.minRange[i3] && i2 <= this.maxRange[i3]) {
                    return;
                } else {
                    if (i <= this.minRange[i3] && i2 >= this.maxRange[i3]) {
                        delete(i3);
                        merge(i, i2);
                        return;
                    } else if (i >= this.minRange[i3] && i <= this.maxRange[i3]) {
                        delete(i3);
                        merge(this.minRange[i3], i2);
                        return;
                    } else if (i2 < this.minRange[i3] || i2 > this.maxRange[i3]) {
                        i3++;
                    } else {
                        delete(i3);
                        merge(i, this.maxRange[i3]);
                        return;
                    }
                }
            }
        }

        /* access modifiers changed from: package-private */
        public void remove(int i, int i2) {
            for (int i3 = 0; i3 < this.num; i3++) {
                if (this.minRange[i3] < i || this.maxRange[i3] > i2) {
                    int[] iArr = this.minRange;
                    if (i >= iArr[i3]) {
                        int[] iArr2 = this.maxRange;
                        if (i2 <= iArr2[i3]) {
                            int i4 = iArr[i3];
                            int i5 = iArr2[i3];
                            delete(i3);
                            if (i4 < i) {
                                merge(i4, i - 1);
                            }
                            if (i2 < i5) {
                                merge(i2 + 1, i5);
                                return;
                            }
                            return;
                        }
                    }
                    int[] iArr3 = this.minRange;
                    if (iArr3[i3] < i || iArr3[i3] > i2) {
                        int[] iArr4 = this.maxRange;
                        if (iArr4[i3] >= i && iArr4[i3] <= i2) {
                            iArr4[i3] = i - 1;
                            return;
                        }
                    } else {
                        iArr3[i3] = i2 + 1;
                        return;
                    }
                } else {
                    delete(i3);
                    return;
                }
            }
        }

        /* access modifiers changed from: package-private */
        public void include(int i, int i2, boolean z) {
            if (z) {
                merge(i, i2);
            } else {
                remove(i, i2);
            }
        }

        /* access modifiers changed from: package-private */
        public void include(char c, boolean z) {
            include(c, c, z);
        }
    }
}
