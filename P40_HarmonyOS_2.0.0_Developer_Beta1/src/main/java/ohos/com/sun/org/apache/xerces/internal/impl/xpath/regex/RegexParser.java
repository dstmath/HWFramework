package ohos.com.sun.org.apache.xerces.internal.impl.xpath.regex;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Vector;
import ohos.com.sun.org.apache.xerces.internal.impl.xpath.regex.Token;
import ohos.com.sun.org.apache.xerces.internal.utils.SecuritySupport;

/* access modifiers changed from: package-private */
public class RegexParser {
    protected static final int S_INBRACKETS = 1;
    protected static final int S_INXBRACKETS = 2;
    protected static final int S_NORMAL = 0;
    static final int T_BACKSOLIDUS = 10;
    static final int T_CARET = 11;
    static final int T_CHAR = 0;
    static final int T_COMMENT = 21;
    static final int T_CONDITION = 23;
    static final int T_DOLLAR = 12;
    static final int T_DOT = 8;
    static final int T_EOF = 1;
    static final int T_INDEPENDENT = 18;
    static final int T_LBRACKET = 9;
    static final int T_LOOKAHEAD = 14;
    static final int T_LOOKBEHIND = 16;
    static final int T_LPAREN = 6;
    static final int T_LPAREN2 = 13;
    static final int T_MODIFIERS = 22;
    static final int T_NEGATIVELOOKAHEAD = 15;
    static final int T_NEGATIVELOOKBEHIND = 17;
    static final int T_OR = 2;
    static final int T_PLUS = 4;
    static final int T_POSIX_CHARCLASS_START = 20;
    static final int T_QUESTION = 5;
    static final int T_RPAREN = 7;
    static final int T_SET_OPERATIONS = 19;
    static final int T_STAR = 3;
    static final int T_XMLSCHEMA_CC_SUBTRACTION = 24;
    int chardata;
    int context = 0;
    boolean hasBackReferences;
    int nexttoken;
    int offset;
    int options;
    int parenCount = 0;
    int parenOpened = 1;
    int parennumber = 1;
    Vector references = null;
    String regex;
    int regexlen;
    ResourceBundle resources;

    private static final int hexChar(int i) {
        if (i < 48 || i > 102) {
            return -1;
        }
        if (i <= 57) {
            return i - 48;
        }
        int i2 = 65;
        if (i < 65) {
            return -1;
        }
        if (i > 70) {
            i2 = 97;
            if (i < 97) {
                return -1;
            }
        }
        return (i - i2) + 10;
    }

    /* access modifiers changed from: package-private */
    public static class ReferencePosition {
        int position;
        int refNumber;

        ReferencePosition(int i, int i2) {
            this.refNumber = i;
            this.position = i2;
        }
    }

    public RegexParser() {
        setLocale(Locale.getDefault());
    }

    public RegexParser(Locale locale) {
        setLocale(locale);
    }

    public void setLocale(Locale locale) {
        if (locale != null) {
            try {
                this.resources = SecuritySupport.getResourceBundle("com.sun.org.apache.xerces.internal.impl.xpath.regex.message", locale);
            } catch (MissingResourceException e) {
                throw new RuntimeException("Installation Problem???  Couldn't load messages: " + e.getMessage());
            }
        } else {
            this.resources = SecuritySupport.getResourceBundle("com.sun.org.apache.xerces.internal.impl.xpath.regex.message");
        }
    }

    /* access modifiers changed from: package-private */
    public final ParseException ex(String str, int i) {
        return new ParseException(this.resources.getString(str), i);
    }

    /* access modifiers changed from: protected */
    public final boolean isSet(int i) {
        return (this.options & i) == i;
    }

    /* access modifiers changed from: package-private */
    public synchronized Token parse(String str, int i) throws ParseException {
        Token parseRegex;
        this.options = i;
        this.offset = 0;
        setContext(0);
        this.parennumber = 1;
        this.parenOpened = 1;
        this.hasBackReferences = false;
        this.regex = str;
        if (isSet(16)) {
            this.regex = REUtil.stripExtendedComment(this.regex);
        }
        this.regexlen = this.regex.length();
        next();
        parseRegex = parseRegex();
        if (this.offset != this.regexlen) {
            throw ex("parser.parse.1", this.offset);
        } else if (this.parenCount < 0) {
            throw ex("parser.factor.0", this.offset);
        } else if (this.references != null) {
            for (int i2 = 0; i2 < this.references.size(); i2++) {
                ReferencePosition referencePosition = (ReferencePosition) this.references.elementAt(i2);
                if (this.parennumber <= referencePosition.refNumber) {
                    throw ex("parser.parse.2", referencePosition.position);
                }
            }
            this.references.removeAllElements();
        }
        return parseRegex;
    }

    /* access modifiers changed from: protected */
    public final void setContext(int i) {
        this.context = i;
    }

    /* access modifiers changed from: package-private */
    public final int read() {
        return this.nexttoken;
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Removed duplicated region for block: B:101:0x018f  */
    /* JADX WARNING: Removed duplicated region for block: B:102:0x0192  */
    public final void next() {
        int i;
        int i2;
        int i3 = this.offset;
        if (i3 >= this.regexlen) {
            this.chardata = -1;
            this.nexttoken = 1;
            return;
        }
        String str = this.regex;
        this.offset = i3 + 1;
        char charAt = str.charAt(i3);
        this.chardata = charAt;
        int i4 = 10;
        if (this.context == 1) {
            if (charAt != '-') {
                if (charAt != '[') {
                    if (charAt == '\\') {
                        int i5 = this.offset;
                        if (i5 < this.regexlen) {
                            String str2 = this.regex;
                            this.offset = i5 + 1;
                            this.chardata = str2.charAt(i5);
                            this.nexttoken = i4;
                            return;
                        }
                        throw ex("parser.next.1", i5 - 1);
                    }
                } else if (!isSet(512) && (i2 = this.offset) < this.regexlen && this.regex.charAt(i2) == ':') {
                    this.offset++;
                    i4 = 20;
                    this.nexttoken = i4;
                    return;
                }
                if (REUtil.isHighSurrogate(charAt) && (i = this.offset) < this.regexlen) {
                    char charAt2 = this.regex.charAt(i);
                    if (REUtil.isLowSurrogate(charAt2)) {
                        this.chardata = REUtil.composeFromSurrogates(charAt, charAt2);
                        this.offset++;
                    }
                }
            } else {
                int i6 = this.offset;
                if (i6 < this.regexlen && this.regex.charAt(i6) == '[') {
                    this.offset++;
                    i4 = 24;
                    this.nexttoken = i4;
                    return;
                }
            }
            i4 = 0;
            this.nexttoken = i4;
            return;
        }
        if (charAt != '$') {
            if (charAt == '.') {
                i4 = 8;
            } else if (charAt == '?') {
                i4 = 5;
            } else if (charAt != '^') {
                if (charAt == '|') {
                    i4 = 2;
                } else if (charAt == '[') {
                    i4 = 9;
                } else if (charAt != '\\') {
                    switch (charAt) {
                        case '(':
                            i4 = 6;
                            this.parenCount++;
                            int i7 = this.offset;
                            if (i7 < this.regexlen && this.regex.charAt(i7) == '?') {
                                int i8 = this.offset + 1;
                                this.offset = i8;
                                if (i8 < this.regexlen) {
                                    String str3 = this.regex;
                                    int i9 = this.offset;
                                    this.offset = i9 + 1;
                                    char charAt3 = str3.charAt(i9);
                                    if (charAt3 == '!') {
                                        i4 = 15;
                                        break;
                                    } else if (charAt3 != '#') {
                                        if (charAt3 != ':') {
                                            if (charAt3 == '[') {
                                                i4 = 19;
                                                break;
                                            } else {
                                                switch (charAt3) {
                                                    case '<':
                                                        int i10 = this.offset;
                                                        if (i10 < this.regexlen) {
                                                            String str4 = this.regex;
                                                            this.offset = i10 + 1;
                                                            char charAt4 = str4.charAt(i10);
                                                            if (charAt4 == '=') {
                                                                i4 = 16;
                                                                break;
                                                            } else if (charAt4 == '!') {
                                                                i4 = 17;
                                                                break;
                                                            } else {
                                                                throw ex("parser.next.3", this.offset - 3);
                                                            }
                                                        } else {
                                                            throw ex("parser.next.2", i10 - 3);
                                                        }
                                                    case '=':
                                                        i4 = 14;
                                                        break;
                                                    case '>':
                                                        i4 = 18;
                                                        break;
                                                    default:
                                                        if (charAt3 == '-' || (('a' <= charAt3 && charAt3 <= 'z') || ('A' <= charAt3 && charAt3 <= 'Z'))) {
                                                            this.offset--;
                                                            i4 = 22;
                                                            break;
                                                        } else if (charAt3 == '(') {
                                                            i4 = 23;
                                                            break;
                                                        } else {
                                                            throw ex("parser.next.2", this.offset - 2);
                                                        }
                                                }
                                            }
                                        } else {
                                            i4 = 13;
                                            break;
                                        }
                                    } else {
                                        do {
                                            int i11 = this.offset;
                                            if (i11 < this.regexlen) {
                                                String str5 = this.regex;
                                                this.offset = i11 + 1;
                                                charAt3 = str5.charAt(i11);
                                            }
                                            if (charAt3 != ')') {
                                                i4 = 21;
                                                break;
                                            } else {
                                                throw ex("parser.next.4", this.offset - 1);
                                            }
                                        } while (charAt3 != ')');
                                        if (charAt3 != ')') {
                                        }
                                    }
                                } else {
                                    throw ex("parser.next.2", this.offset - 1);
                                }
                            }
                            break;
                        case ')':
                            i4 = 7;
                            break;
                        case '*':
                            i4 = 3;
                            break;
                        case '+':
                            i4 = 4;
                            break;
                    }
                } else {
                    int i12 = this.offset;
                    if (i12 < this.regexlen) {
                        String str6 = this.regex;
                        this.offset = i12 + 1;
                        this.chardata = str6.charAt(i12);
                    } else {
                        throw ex("parser.next.1", i12 - 1);
                    }
                }
            } else if (!isSet(512)) {
                i4 = 11;
            }
            this.nexttoken = i4;
        } else if (!isSet(512)) {
            i4 = 12;
            this.nexttoken = i4;
        }
        i4 = 0;
        this.nexttoken = i4;
    }

    /* access modifiers changed from: package-private */
    public Token parseRegex() throws ParseException {
        Token parseTerm = parseTerm();
        Token.UnionToken unionToken = null;
        while (read() == 2) {
            next();
            if (unionToken == null) {
                unionToken = Token.createUnion();
                unionToken.addChild(parseTerm);
                parseTerm = unionToken;
            }
            parseTerm.addChild(parseTerm());
        }
        return parseTerm;
    }

    /* access modifiers changed from: package-private */
    public Token parseTerm() throws ParseException {
        Token token;
        int read;
        int read2 = read();
        if (read2 == 2 || read2 == 7 || read2 == 1) {
            token = Token.createEmpty();
        } else {
            Token parseFactor = parseFactor();
            Token.UnionToken unionToken = null;
            while (true) {
                read = read();
                if (read == 2 || read == 7 || read == 1) {
                    break;
                }
                if (unionToken == null) {
                    unionToken = Token.createConcat();
                    unionToken.addChild(parseFactor);
                    parseFactor = unionToken;
                }
                unionToken.addChild(parseFactor());
            }
            token = parseFactor;
            read2 = read;
        }
        if (read2 == 7) {
            this.parenCount--;
        }
        return token;
    }

    /* access modifiers changed from: package-private */
    public Token processCaret() throws ParseException {
        next();
        return Token.token_linebeginning;
    }

    /* access modifiers changed from: package-private */
    public Token processDollar() throws ParseException {
        next();
        return Token.token_lineend;
    }

    /* access modifiers changed from: package-private */
    public Token processLookahead() throws ParseException {
        next();
        Token.ParenToken createLook = Token.createLook(20, parseRegex());
        if (read() == 7) {
            next();
            return createLook;
        }
        throw ex("parser.factor.1", this.offset - 1);
    }

    /* access modifiers changed from: package-private */
    public Token processNegativelookahead() throws ParseException {
        next();
        Token.ParenToken createLook = Token.createLook(21, parseRegex());
        if (read() == 7) {
            next();
            return createLook;
        }
        throw ex("parser.factor.1", this.offset - 1);
    }

    /* access modifiers changed from: package-private */
    public Token processLookbehind() throws ParseException {
        next();
        Token.ParenToken createLook = Token.createLook(22, parseRegex());
        if (read() == 7) {
            next();
            return createLook;
        }
        throw ex("parser.factor.1", this.offset - 1);
    }

    /* access modifiers changed from: package-private */
    public Token processNegativelookbehind() throws ParseException {
        next();
        Token.ParenToken createLook = Token.createLook(23, parseRegex());
        if (read() == 7) {
            next();
            return createLook;
        }
        throw ex("parser.factor.1", this.offset - 1);
    }

    /* access modifiers changed from: package-private */
    public Token processBacksolidus_A() throws ParseException {
        next();
        return Token.token_stringbeginning;
    }

    /* access modifiers changed from: package-private */
    public Token processBacksolidus_Z() throws ParseException {
        next();
        return Token.token_stringend2;
    }

    /* access modifiers changed from: package-private */
    public Token processBacksolidus_z() throws ParseException {
        next();
        return Token.token_stringend;
    }

    /* access modifiers changed from: package-private */
    public Token processBacksolidus_b() throws ParseException {
        next();
        return Token.token_wordedge;
    }

    /* access modifiers changed from: package-private */
    public Token processBacksolidus_B() throws ParseException {
        next();
        return Token.token_not_wordedge;
    }

    /* access modifiers changed from: package-private */
    public Token processBacksolidus_lt() throws ParseException {
        next();
        return Token.token_wordbeginning;
    }

    /* access modifiers changed from: package-private */
    public Token processBacksolidus_gt() throws ParseException {
        next();
        return Token.token_wordend;
    }

    /* access modifiers changed from: package-private */
    public Token processStar(Token token) throws ParseException {
        next();
        if (read() != 5) {
            return Token.createClosure(token);
        }
        next();
        return Token.createNGClosure(token);
    }

    /* access modifiers changed from: package-private */
    public Token processPlus(Token token) throws ParseException {
        next();
        if (read() != 5) {
            return Token.createConcat(token, Token.createClosure(token));
        }
        next();
        return Token.createConcat(token, Token.createNGClosure(token));
    }

    /* access modifiers changed from: package-private */
    public Token processQuestion(Token token) throws ParseException {
        next();
        Token.UnionToken createUnion = Token.createUnion();
        if (read() == 5) {
            next();
            createUnion.addChild(Token.createEmpty());
            createUnion.addChild(token);
        } else {
            createUnion.addChild(token);
            createUnion.addChild(Token.createEmpty());
        }
        return createUnion;
    }

    /* access modifiers changed from: package-private */
    public boolean checkQuestion(int i) {
        return i < this.regexlen && this.regex.charAt(i) == '?';
    }

    /* access modifiers changed from: package-private */
    public Token processParen() throws ParseException {
        next();
        int i = this.parenOpened;
        this.parenOpened = i + 1;
        Token.ParenToken createParen = Token.createParen(parseRegex(), i);
        if (read() == 7) {
            this.parennumber++;
            next();
            return createParen;
        }
        throw ex("parser.factor.1", this.offset - 1);
    }

    /* access modifiers changed from: package-private */
    public Token processParen2() throws ParseException {
        next();
        Token.ParenToken createParen = Token.createParen(parseRegex(), 0);
        if (read() == 7) {
            next();
            return createParen;
        }
        throw ex("parser.factor.1", this.offset - 1);
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0047, code lost:
        r1 = r0;
     */
    public Token processCondition() throws ParseException {
        Token token;
        char charAt;
        int i = this.offset;
        if (i + 1 < this.regexlen) {
            int i2 = -1;
            char charAt2 = this.regex.charAt(i);
            Token token2 = null;
            if ('1' > charAt2 || charAt2 > '9') {
                if (charAt2 == '?') {
                    this.offset--;
                }
                next();
                token = parseFactor();
                int i3 = token.type;
                if (i3 != 8) {
                    switch (i3) {
                        case 20:
                        case 21:
                        case 22:
                        case 23:
                            break;
                        default:
                            throw ex("parser.factor.5", this.offset);
                    }
                } else if (read() != 7) {
                    throw ex("parser.factor.1", this.offset - 1);
                }
            } else {
                int i4 = charAt2 - '0';
                if (this.parennumber > i4) {
                    while (true) {
                        int i5 = this.offset;
                        if (i5 + 1 < this.regexlen && '1' <= (charAt = this.regex.charAt(i5 + 1)) && charAt <= '9') {
                            i2 = (charAt - '0') + (i4 * 10);
                            if (i2 >= this.parennumber) {
                                break;
                            }
                            this.offset++;
                            i4 = i2;
                        } else {
                            break;
                        }
                    }
                    this.hasBackReferences = true;
                    if (this.references == null) {
                        this.references = new Vector();
                    }
                    this.references.addElement(new ReferencePosition(i4, this.offset));
                    this.offset++;
                    if (this.regex.charAt(this.offset) == ')') {
                        this.offset++;
                        token = null;
                    } else {
                        throw ex("parser.factor.1", this.offset);
                    }
                } else {
                    throw ex("parser.parse.2", this.offset);
                }
            }
            next();
            Token parseRegex = parseRegex();
            if (parseRegex.type == 2) {
                if (parseRegex.size() == 2) {
                    token2 = parseRegex.getChild(1);
                    parseRegex = parseRegex.getChild(0);
                } else {
                    throw ex("parser.factor.6", this.offset);
                }
            }
            if (read() == 7) {
                next();
                return Token.createCondition(i2, token, parseRegex, token2);
            }
            throw ex("parser.factor.1", this.offset - 1);
        }
        throw ex("parser.factor.4", i);
    }

    /* access modifiers changed from: package-private */
    public Token processModifiers() throws ParseException {
        int optionValue;
        int optionValue2;
        int i = 0;
        char c = 65535;
        int i2 = 0;
        while (true) {
            int i3 = this.offset;
            if (i3 >= this.regexlen || (optionValue2 = REUtil.getOptionValue((c = this.regex.charAt(i3)))) == 0) {
                break;
            }
            i2 |= optionValue2;
            this.offset++;
        }
        int i4 = this.offset;
        if (i4 < this.regexlen) {
            if (c == '-') {
                this.offset = i4 + 1;
                while (true) {
                    int i5 = this.offset;
                    if (i5 >= this.regexlen || (optionValue = REUtil.getOptionValue((c = this.regex.charAt(i5)))) == 0) {
                        break;
                    }
                    i |= optionValue;
                    this.offset++;
                }
                int i6 = this.offset;
                if (i6 >= this.regexlen) {
                    throw ex("parser.factor.2", i6 - 1);
                }
            }
            if (c == ':') {
                this.offset++;
                next();
                Token.ModifierToken createModifierGroup = Token.createModifierGroup(parseRegex(), i2, i);
                if (read() == 7) {
                    next();
                    return createModifierGroup;
                }
                throw ex("parser.factor.1", this.offset - 1);
            } else if (c == ')') {
                this.offset++;
                next();
                return Token.createModifierGroup(parseRegex(), i2, i);
            } else {
                throw ex("parser.factor.3", this.offset);
            }
        } else {
            throw ex("parser.factor.2", i4 - 1);
        }
    }

    /* access modifiers changed from: package-private */
    public Token processIndependent() throws ParseException {
        next();
        Token.ParenToken createLook = Token.createLook(24, parseRegex());
        if (read() == 7) {
            next();
            return createLook;
        }
        throw ex("parser.factor.1", this.offset - 1);
    }

    /* access modifiers changed from: package-private */
    public Token processBacksolidus_c() throws ParseException {
        int i = this.offset;
        if (i < this.regexlen) {
            String str = this.regex;
            this.offset = i + 1;
            char charAt = str.charAt(i);
            if ((65504 & charAt) == 64) {
                next();
                return Token.createChar(charAt - '@');
            }
        }
        throw ex("parser.atom.1", this.offset - 1);
    }

    /* access modifiers changed from: package-private */
    public Token processBacksolidus_C() throws ParseException {
        throw ex("parser.process.1", this.offset);
    }

    /* access modifiers changed from: package-private */
    public Token processBacksolidus_i() throws ParseException {
        Token.CharToken createChar = Token.createChar(105);
        next();
        return createChar;
    }

    /* access modifiers changed from: package-private */
    public Token processBacksolidus_I() throws ParseException {
        throw ex("parser.process.1", this.offset);
    }

    /* access modifiers changed from: package-private */
    public Token processBacksolidus_g() throws ParseException {
        next();
        return Token.getGraphemePattern();
    }

    /* access modifiers changed from: package-private */
    public Token processBacksolidus_X() throws ParseException {
        next();
        return Token.getCombiningCharacterSequence();
    }

    /* access modifiers changed from: package-private */
    public Token processBackreference() throws ParseException {
        char charAt;
        int i;
        int i2 = this.chardata - 48;
        if (this.parennumber > i2) {
            while (true) {
                int i3 = this.offset;
                if (i3 >= this.regexlen || '1' > (charAt = this.regex.charAt(i3)) || charAt > '9' || (i = (i2 * 10) + (charAt - '0')) >= this.parennumber) {
                    break;
                }
                this.offset++;
                this.chardata = charAt;
                i2 = i;
            }
            Token.StringToken createBackReference = Token.createBackReference(i2);
            this.hasBackReferences = true;
            if (this.references == null) {
                this.references = new Vector();
            }
            this.references.addElement(new ReferencePosition(i2, this.offset - 2));
            next();
            return createBackReference;
        }
        throw ex("parser.parse.2", this.offset - 2);
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:77:0x00d4, code lost:
        r3 = r6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:96:0x010a, code lost:
        r6 = r8;
     */
    public Token parseFactor() throws ParseException {
        int i;
        int i2;
        int i3;
        switch (read()) {
            case 10:
                int i4 = this.chardata;
                if (i4 == 60) {
                    return processBacksolidus_lt();
                }
                if (i4 == 62) {
                    return processBacksolidus_gt();
                }
                if (i4 == 90) {
                    return processBacksolidus_Z();
                }
                if (i4 == 98) {
                    return processBacksolidus_b();
                }
                if (i4 == 122) {
                    return processBacksolidus_z();
                }
                if (i4 == 65) {
                    return processBacksolidus_A();
                }
                if (i4 == 66) {
                    return processBacksolidus_B();
                }
                break;
            case 11:
                return processCaret();
            case 12:
                return processDollar();
            case 14:
                return processLookahead();
            case 15:
                return processNegativelookahead();
            case 16:
                return processLookbehind();
            case 17:
                return processNegativelookbehind();
            case 21:
                next();
                return Token.createEmpty();
        }
        Token parseAtom = parseAtom();
        int read = read();
        if (read != 0) {
            if (read == 3) {
                return processStar(parseAtom);
            }
            if (read == 4) {
                return processPlus(parseAtom);
            }
            if (read == 5) {
                return processQuestion(parseAtom);
            }
        } else if (this.chardata == 123 && (i = this.offset) < this.regexlen) {
            int i5 = i + 1;
            char charAt = this.regex.charAt(i);
            if (charAt < '0' || charAt > '9') {
                throw ex("parser.quantifier.1", this.offset);
            }
            int i6 = charAt - '0';
            while (true) {
                if (i5 < this.regexlen) {
                    int i7 = i5 + 1;
                    charAt = this.regex.charAt(i5);
                    if (charAt >= '0' && charAt <= '9') {
                        i6 = ((i6 * 10) + charAt) - 48;
                        if (i6 >= 0) {
                            i5 = i7;
                        } else {
                            throw ex("parser.quantifier.5", this.offset);
                        }
                    }
                }
            }
            if (charAt != ',') {
                i2 = i5;
                i3 = i6;
            } else if (i5 < this.regexlen) {
                i2 = i5 + 1;
                charAt = this.regex.charAt(i5);
                if (charAt < '0' || charAt > '9') {
                    i3 = -1;
                } else {
                    int i8 = charAt - '0';
                    while (true) {
                        if (i2 < this.regexlen) {
                            int i9 = i2 + 1;
                            charAt = this.regex.charAt(i2);
                            if (charAt >= '0' && charAt <= '9') {
                                i8 = ((i8 * 10) + charAt) - 48;
                                if (i8 >= 0) {
                                    i2 = i9;
                                } else {
                                    throw ex("parser.quantifier.5", this.offset);
                                }
                            }
                        }
                    }
                    if (i6 <= i8) {
                        i3 = i8;
                    } else {
                        throw ex("parser.quantifier.4", this.offset);
                    }
                }
            } else {
                throw ex("parser.quantifier.3", this.offset);
            }
            if (charAt == '}') {
                if (checkQuestion(i2)) {
                    parseAtom = Token.createNGClosure(parseAtom);
                    this.offset = i2 + 1;
                } else {
                    parseAtom = Token.createClosure(parseAtom);
                    this.offset = i2;
                }
                parseAtom.setMin(i6);
                parseAtom.setMax(i3);
                next();
            } else {
                throw ex("parser.quantifier.2", this.offset);
            }
        }
        return parseAtom;
    }

    /* access modifiers changed from: package-private */
    public Token parseAtom() throws ParseException {
        Token token;
        int read = read();
        if (read == 0) {
            int i = this.chardata;
            if (i == 93 || i == 123 || i == 125) {
                throw ex("parser.atom.4", this.offset - 1);
            }
            Token.CharToken createChar = Token.createChar(i);
            int i2 = this.chardata;
            next();
            if (!REUtil.isHighSurrogate(i2) || read() != 0 || !REUtil.isLowSurrogate(this.chardata)) {
                return createChar;
            }
            Token.ParenToken createParen = Token.createParen(Token.createString(new String(new char[]{(char) i2, (char) this.chardata})), 0);
            next();
            return createParen;
        } else if (read == 6) {
            return processParen();
        } else {
            if (read == 13) {
                return processParen2();
            }
            if (read == 18) {
                return processIndependent();
            }
            if (read == 19) {
                return parseSetOperations();
            }
            if (read == 22) {
                return processModifiers();
            }
            if (read == 23) {
                return processCondition();
            }
            switch (read) {
                case 8:
                    next();
                    return Token.token_dot;
                case 9:
                    return parseCharacterClass(true);
                case 10:
                    int i3 = this.chardata;
                    if (i3 == 67) {
                        return processBacksolidus_C();
                    }
                    if (i3 != 68) {
                        if (i3 == 73) {
                            return processBacksolidus_I();
                        }
                        if (i3 != 80) {
                            if (i3 != 83) {
                                if (i3 == 105) {
                                    return processBacksolidus_i();
                                }
                                if (i3 != 110) {
                                    if (i3 != 112) {
                                        if (i3 != 87) {
                                            if (i3 == 88) {
                                                return processBacksolidus_X();
                                            }
                                            switch (i3) {
                                                case 49:
                                                case 50:
                                                case 51:
                                                case 52:
                                                case 53:
                                                case 54:
                                                case 55:
                                                case 56:
                                                case 57:
                                                    return processBackreference();
                                                default:
                                                    switch (i3) {
                                                        case 99:
                                                            return processBacksolidus_c();
                                                        case 100:
                                                            break;
                                                        case 101:
                                                        case 102:
                                                            break;
                                                        case 103:
                                                            return processBacksolidus_g();
                                                        default:
                                                            switch (i3) {
                                                                case 114:
                                                                case 116:
                                                                case 117:
                                                                case 118:
                                                                case 120:
                                                                    break;
                                                                case 115:
                                                                case 119:
                                                                    break;
                                                                default:
                                                                    token = Token.createChar(i3);
                                                                    next();
                                                                    return token;
                                                            }
                                                    }
                                            }
                                        }
                                    }
                                }
                                int decodeEscaped = decodeEscaped();
                                if (decodeEscaped < 65536) {
                                    token = Token.createChar(decodeEscaped);
                                } else {
                                    token = Token.createString(REUtil.decomposeToSurrogates(decodeEscaped));
                                }
                                next();
                                return token;
                            }
                        }
                        int i4 = this.offset;
                        RangeToken processBacksolidus_pP = processBacksolidus_pP(this.chardata);
                        if (processBacksolidus_pP != null) {
                            token = processBacksolidus_pP;
                            next();
                            return token;
                        }
                        throw ex("parser.atom.5", i4);
                    }
                    Token tokenForShorthand = getTokenForShorthand(this.chardata);
                    next();
                    return tokenForShorthand;
                default:
                    throw ex("parser.atom.4", this.offset - 1);
            }
        }
    }

    /* access modifiers changed from: protected */
    public RangeToken processBacksolidus_pP(int i) throws ParseException {
        next();
        if (read() == 0 && this.chardata == 123) {
            boolean z = i == 112;
            int i2 = this.offset;
            int indexOf = this.regex.indexOf(125, i2);
            if (indexOf >= 0) {
                String substring = this.regex.substring(i2, indexOf);
                this.offset = indexOf + 1;
                return Token.getRange(substring, z, isSet(512));
            }
            throw ex("parser.atom.3", this.offset);
        }
        throw ex("parser.atom.2", this.offset - 1);
    }

    /* access modifiers changed from: package-private */
    public int processCIinCharacterClass(RangeToken rangeToken, int i) {
        return decodeEscaped();
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Code restructure failed: missing block: B:137:0x01f3, code lost:
        if (read() == 1) goto L_0x020a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:138:0x01f5, code lost:
        if (r17 != false) goto L_0x01fd;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:139:0x01f7, code lost:
        if (r5 == false) goto L_0x01fd;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:140:0x01f9, code lost:
        r6.subtractRanges(r2);
        r2 = r6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:141:0x01fd, code lost:
        r2.sortRanges();
        r2.compactRanges();
        setContext(0);
        next();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:142:0x0209, code lost:
        return r2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:144:0x0210, code lost:
        throw ex("parser.cc.2", r16.offset);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:53:0x00ae, code lost:
        if (r11 < 0) goto L_0x00a7;
     */
    /* JADX WARNING: Removed duplicated region for block: B:90:0x014e  */
    public RangeToken parseCharacterClass(boolean z) throws ParseException {
        RangeToken rangeToken;
        boolean z2;
        RangeToken rangeToken2;
        boolean z3;
        int indexOf;
        boolean z4;
        setContext(1);
        next();
        char c = '^';
        RangeToken rangeToken3 = null;
        if (read() == 0 && this.chardata == 94) {
            next();
            if (z) {
                rangeToken2 = Token.createNRange();
            } else {
                rangeToken3 = Token.createRange();
                rangeToken3.addRange(0, 1114111);
                rangeToken2 = Token.createRange();
            }
            rangeToken = rangeToken3;
            z2 = true;
        } else {
            rangeToken2 = Token.createRange();
            rangeToken = null;
            z2 = false;
        }
        boolean z5 = true;
        while (true) {
            int read = read();
            if (read == 1 || (read == 0 && this.chardata == 93 && !z5)) {
                break;
            }
            int i = this.chardata;
            if (read == 10) {
                if (i != 67) {
                    if (i != 68) {
                        if (i != 73) {
                            if (i != 80) {
                                if (!(i == 83 || i == 87)) {
                                    if (i != 105) {
                                        if (i != 112) {
                                            if (!(i == 115 || i == 119)) {
                                                if (i != 99) {
                                                    if (i != 100) {
                                                        i = decodeEscaped();
                                                        z3 = false;
                                                        next();
                                                        if (!z3) {
                                                            if (read() == 0 && this.chardata == 45) {
                                                                if (read != 24) {
                                                                    next();
                                                                    int read2 = read();
                                                                    if (read2 == 1) {
                                                                        throw ex("parser.cc.2", this.offset);
                                                                    } else if (read2 == 0 && this.chardata == 93) {
                                                                        if (!isSet(2) || i > 65535) {
                                                                            rangeToken2.addRange(i, i);
                                                                        } else {
                                                                            addCaseInsensitiveChar(rangeToken2, i);
                                                                        }
                                                                        rangeToken2.addRange(45, 45);
                                                                    } else {
                                                                        int i2 = this.chardata;
                                                                        if (read2 == 10) {
                                                                            i2 = decodeEscaped();
                                                                        }
                                                                        next();
                                                                        if (i > i2) {
                                                                            throw ex("parser.ope.3", this.offset - 1);
                                                                        } else if (!isSet(2) || (i > 65535 && i2 > 65535)) {
                                                                            rangeToken2.addRange(i, i2);
                                                                        } else {
                                                                            addCaseInsensitiveCharRange(rangeToken2, i, i2);
                                                                        }
                                                                    }
                                                                } else {
                                                                    throw ex("parser.cc.8", this.offset - 1);
                                                                }
                                                            } else if (!isSet(2) || i > 65535) {
                                                                rangeToken2.addRange(i, i);
                                                            } else {
                                                                addCaseInsensitiveChar(rangeToken2, i);
                                                            }
                                                        }
                                                        if (isSet(1024) && read() == 0 && this.chardata == 44) {
                                                            next();
                                                        }
                                                        z5 = false;
                                                        c = '^';
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            int i3 = this.offset;
                            RangeToken processBacksolidus_pP = processBacksolidus_pP(i);
                            if (processBacksolidus_pP != null) {
                                rangeToken2.mergeRanges(processBacksolidus_pP);
                            } else {
                                throw ex("parser.atom.5", i3);
                            }
                        }
                    }
                    rangeToken2.mergeRanges(getTokenForShorthand(i));
                }
                i = processCIinCharacterClass(rangeToken2, i);
            } else if (read == 20) {
                indexOf = this.regex.indexOf(58, this.offset);
                if (indexOf >= 0) {
                    if (this.regex.charAt(this.offset) == c) {
                        this.offset++;
                        z4 = false;
                    } else {
                        z4 = true;
                    }
                    RangeToken range = Token.getRange(this.regex.substring(this.offset, indexOf), z4, isSet(512));
                    if (range != null) {
                        rangeToken2.mergeRanges(range);
                        int i4 = indexOf + 1;
                        if (i4 >= this.regexlen || this.regex.charAt(i4) != ']') {
                            break;
                        }
                        this.offset = indexOf + 2;
                    } else {
                        throw ex("parser.cc.3", this.offset);
                    }
                } else {
                    throw ex("parser.cc.1", this.offset);
                }
            } else {
                if (read == 24 && !z5) {
                    if (z2) {
                        if (z) {
                            rangeToken2 = (RangeToken) Token.complementRanges(rangeToken2);
                            z2 = false;
                        } else {
                            rangeToken.subtractRanges(rangeToken2);
                            z2 = false;
                            rangeToken2 = rangeToken;
                        }
                    }
                    rangeToken2.subtractRanges(parseCharacterClass(false));
                    if (read() != 0 || this.chardata != 93) {
                        throw ex("parser.cc.5", this.offset);
                    }
                }
                z3 = false;
                next();
                if (!z3) {
                }
                next();
                z5 = false;
                c = '^';
            }
            z3 = true;
            next();
            if (!z3) {
            }
            next();
            z5 = false;
            c = '^';
        }
        throw ex("parser.cc.1", indexOf);
    }

    /* access modifiers changed from: protected */
    public RangeToken parseSetOperations() throws ParseException {
        RangeToken parseCharacterClass = parseCharacterClass(false);
        while (true) {
            int read = read();
            if (read != 7) {
                int i = this.chardata;
                if ((read == 0 && (i == 45 || i == 38)) || read == 4) {
                    next();
                    if (read() == 9) {
                        RangeToken parseCharacterClass2 = parseCharacterClass(false);
                        if (read == 4) {
                            parseCharacterClass.mergeRanges(parseCharacterClass2);
                        } else if (i == 45) {
                            parseCharacterClass.subtractRanges(parseCharacterClass2);
                        } else if (i == 38) {
                            parseCharacterClass.intersectRanges(parseCharacterClass2);
                        } else {
                            throw new RuntimeException("ASSERT");
                        }
                    } else {
                        throw ex("parser.ope.1", this.offset - 1);
                    }
                } else {
                    throw ex("parser.ope.2", this.offset - 1);
                }
            } else {
                next();
                return parseCharacterClass;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public Token getTokenForShorthand(int i) {
        if (i == 68) {
            return isSet(32) ? Token.getRange("Nd", false) : Token.token_not_0to9;
        }
        if (i != 83) {
            if (i == 87) {
                return isSet(32) ? Token.getRange("IsWord", false) : Token.token_not_wordchars;
            }
            if (i != 100) {
                if (i != 115) {
                    if (i != 119) {
                        throw new RuntimeException("Internal Error: shorthands: \\u" + Integer.toString(i, 16));
                    } else if (isSet(32)) {
                        return Token.getRange("IsWord", true);
                    } else {
                        return Token.token_wordchars;
                    }
                } else if (isSet(32)) {
                    return Token.getRange("IsSpace", true);
                } else {
                    return Token.token_spaces;
                }
            } else if (isSet(32)) {
                return Token.getRange("Nd", true);
            } else {
                return Token.token_0to9;
            }
        } else if (isSet(32)) {
            return Token.getRange("IsSpace", false);
        } else {
            return Token.token_not_spaces;
        }
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    /* access modifiers changed from: package-private */
    public int decodeEscaped() throws ParseException {
        int i;
        int i2;
        int hexChar;
        int hexChar2;
        int hexChar3;
        int hexChar4;
        int hexChar5;
        int hexChar6;
        int hexChar7;
        int hexChar8;
        int hexChar9;
        if (read() == 10) {
            int i3 = this.chardata;
            if (!(i3 == 65 || i3 == 90)) {
                if (i3 == 110) {
                    return 10;
                }
                if (i3 == 114) {
                    return 13;
                }
                if (i3 == 120) {
                    next();
                    if (read() != 0) {
                        throw ex("parser.descape.1", this.offset - 1);
                    } else if (this.chardata == 123) {
                        int i4 = 0;
                        while (true) {
                            next();
                            if (read() == 0) {
                                int hexChar10 = hexChar(this.chardata);
                                if (hexChar10 >= 0) {
                                    int i5 = i4 * 16;
                                    if (i4 <= i5) {
                                        i4 = i5 + hexChar10;
                                    } else {
                                        throw ex("parser.descape.2", this.offset - 1);
                                    }
                                } else if (this.chardata != 125) {
                                    throw ex("parser.descape.3", this.offset - 1);
                                } else if (i4 <= 1114111) {
                                    return i4;
                                } else {
                                    throw ex("parser.descape.4", this.offset - 1);
                                }
                            } else {
                                throw ex("parser.descape.1", this.offset - 1);
                            }
                        }
                    } else if (read() != 0 || (i2 = hexChar(this.chardata)) < 0) {
                        throw ex("parser.descape.1", this.offset - 1);
                    } else {
                        next();
                        if (read() != 0 || (i = hexChar(this.chardata)) < 0) {
                            throw ex("parser.descape.1", this.offset - 1);
                        }
                    }
                } else if (i3 != 122) {
                    if (i3 == 101) {
                        return 27;
                    }
                    if (i3 == 102) {
                        return 12;
                    }
                    switch (i3) {
                        case 116:
                            return 9;
                        case 117:
                            next();
                            if (read() != 0 || (hexChar = hexChar(this.chardata)) < 0) {
                                throw ex("parser.descape.1", this.offset - 1);
                            }
                            next();
                            if (read() != 0 || (hexChar2 = hexChar(this.chardata)) < 0) {
                                throw ex("parser.descape.1", this.offset - 1);
                            }
                            int i6 = (hexChar * 16) + hexChar2;
                            next();
                            if (read() != 0 || (hexChar3 = hexChar(this.chardata)) < 0) {
                                throw ex("parser.descape.1", this.offset - 1);
                            }
                            i2 = (i6 * 16) + hexChar3;
                            next();
                            if (read() != 0 || (i = hexChar(this.chardata)) < 0) {
                                throw ex("parser.descape.1", this.offset - 1);
                            }
                            break;
                        case 118:
                            next();
                            if (read() != 0 || (hexChar4 = hexChar(this.chardata)) < 0) {
                                throw ex("parser.descape.1", this.offset - 1);
                            }
                            next();
                            if (read() != 0 || (hexChar5 = hexChar(this.chardata)) < 0) {
                                throw ex("parser.descape.1", this.offset - 1);
                            }
                            int i7 = (hexChar4 * 16) + hexChar5;
                            next();
                            if (read() != 0 || (hexChar6 = hexChar(this.chardata)) < 0) {
                                throw ex("parser.descape.1", this.offset - 1);
                            }
                            int i8 = (i7 * 16) + hexChar6;
                            next();
                            if (read() != 0 || (hexChar7 = hexChar(this.chardata)) < 0) {
                                throw ex("parser.descape.1", this.offset - 1);
                            }
                            int i9 = (i8 * 16) + hexChar7;
                            next();
                            if (read() != 0 || (hexChar8 = hexChar(this.chardata)) < 0) {
                                throw ex("parser.descape.1", this.offset - 1);
                            }
                            int i10 = (i9 * 16) + hexChar8;
                            next();
                            if (read() != 0 || (hexChar9 = hexChar(this.chardata)) < 0) {
                                throw ex("parser.descape.1", this.offset - 1);
                            }
                            int i11 = hexChar9 + (i10 * 16);
                            if (i11 <= 1114111) {
                                return i11;
                            }
                            throw ex("parser.descappe.4", this.offset - 1);
                        default:
                            return i3;
                    }
                }
                return i + (i2 * 16);
            }
            throw ex("parser.descape.5", this.offset - 2);
        }
        throw ex("parser.next.1", this.offset - 1);
    }

    protected static final void addCaseInsensitiveChar(RangeToken rangeToken, int i) {
        int[] iArr = CaseInsensitiveMap.get(i);
        rangeToken.addRange(i, i);
        if (iArr != null) {
            for (int i2 = 0; i2 < iArr.length; i2 += 2) {
                rangeToken.addRange(iArr[i2], iArr[i2]);
            }
        }
    }

    protected static final void addCaseInsensitiveCharRange(RangeToken rangeToken, int i, int i2) {
        if (i > i2) {
            i2 = i;
            i = i2;
        }
        rangeToken.addRange(i, i2);
        while (i <= i2) {
            int[] iArr = CaseInsensitiveMap.get(i);
            if (iArr != null) {
                for (int i3 = 0; i3 < iArr.length; i3 += 2) {
                    rangeToken.addRange(iArr[i3], iArr[i3]);
                }
            }
            i++;
        }
    }
}
