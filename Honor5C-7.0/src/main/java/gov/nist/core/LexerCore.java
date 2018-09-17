package gov.nist.core;

import java.text.ParseException;
import java.util.Hashtable;

public class LexerCore extends StringTokenizer {
    public static final int ALPHA = 4099;
    static final char ALPHADIGIT_VALID_CHARS = '\ufffd';
    static final char ALPHA_VALID_CHARS = '\uffff';
    public static final int AND = 38;
    public static final int AT = 64;
    public static final int BACKSLASH = 92;
    public static final int BACK_QUOTE = 96;
    public static final int BAR = 124;
    public static final int COLON = 58;
    public static final int DIGIT = 4098;
    static final char DIGIT_VALID_CHARS = '\ufffe';
    public static final int DOLLAR = 36;
    public static final int DOT = 46;
    public static final int DOUBLEQUOTE = 34;
    public static final int END = 4096;
    public static final int EQUALS = 61;
    public static final int EXCLAMATION = 33;
    public static final int GREATER_THAN = 62;
    public static final int HAT = 94;
    public static final int HT = 9;
    public static final int ID = 4095;
    public static final int LESS_THAN = 60;
    public static final int LPAREN = 40;
    public static final int L_CURLY = 123;
    public static final int L_SQUARE_BRACKET = 91;
    public static final int MINUS = 45;
    public static final int NULL = 0;
    public static final int PERCENT = 37;
    public static final int PLUS = 43;
    public static final int POUND = 35;
    public static final int QUESTION = 63;
    public static final int QUOTE = 39;
    public static final int RPAREN = 41;
    public static final int R_CURLY = 125;
    public static final int R_SQUARE_BRACKET = 93;
    public static final int SAFE = 4094;
    public static final int SEMICOLON = 59;
    public static final int SLASH = 47;
    public static final int SP = 32;
    public static final int STAR = 42;
    public static final int START = 2048;
    public static final int TILDE = 126;
    public static final int UNDERSCORE = 95;
    public static final int WHITESPACE = 4097;
    protected static final Hashtable globalSymbolTable = null;
    protected static final Hashtable lexerTables = null;
    protected Hashtable currentLexer;
    protected String currentLexerName;
    protected Token currentMatch;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: gov.nist.core.LexerCore.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: gov.nist.core.LexerCore.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: gov.nist.core.LexerCore.<clinit>():void");
    }

    protected void addKeyword(String name, int value) {
        Integer val = Integer.valueOf(value);
        this.currentLexer.put(name, val);
        if (!globalSymbolTable.containsKey(val)) {
            globalSymbolTable.put(val, name);
        }
    }

    public String lookupToken(int value) {
        if (value > START) {
            return (String) globalSymbolTable.get(Integer.valueOf(value));
        }
        return Character.valueOf((char) value).toString();
    }

    protected Hashtable addLexer(String lexerName) {
        this.currentLexer = (Hashtable) lexerTables.get(lexerName);
        if (this.currentLexer == null) {
            this.currentLexer = new Hashtable();
            lexerTables.put(lexerName, this.currentLexer);
        }
        return this.currentLexer;
    }

    public void selectLexer(String lexerName) {
        this.currentLexerName = lexerName;
    }

    protected LexerCore() {
        this.currentLexer = new Hashtable();
        this.currentLexerName = "charLexer";
    }

    public LexerCore(String lexerName, String buffer) {
        super(buffer);
        this.currentLexerName = lexerName;
    }

    public String peekNextId() {
        int oldPtr = this.ptr;
        String retval = ttoken();
        this.savedPtr = this.ptr;
        this.ptr = oldPtr;
        return retval;
    }

    public String getNextId() {
        return ttoken();
    }

    public Token getNextToken() {
        return this.currentMatch;
    }

    public Token peekNextToken() throws ParseException {
        return peekNextToken(1)[NULL];
    }

    public Token[] peekNextToken(int ntokens) throws ParseException {
        int old = this.ptr;
        Token[] retval = new Token[ntokens];
        for (int i = NULL; i < ntokens; i++) {
            Token tok = new Token();
            if (startsId()) {
                String id = ttoken();
                tok.tokenValue = id;
                String idUppercase = id.toUpperCase();
                if (this.currentLexer.containsKey(idUppercase)) {
                    tok.tokenType = ((Integer) this.currentLexer.get(idUppercase)).intValue();
                } else {
                    tok.tokenType = ID;
                }
            } else {
                char nextChar = getNextChar();
                tok.tokenValue = String.valueOf(nextChar);
                if (StringTokenizer.isAlpha(nextChar)) {
                    tok.tokenType = ALPHA;
                } else if (StringTokenizer.isDigit(nextChar)) {
                    tok.tokenType = DIGIT;
                } else {
                    tok.tokenType = nextChar;
                }
            }
            retval[i] = tok;
        }
        this.savedPtr = this.ptr;
        this.ptr = old;
        return retval;
    }

    public Token match(int tok) throws ParseException {
        if (Debug.parserDebug) {
            Debug.println("match " + tok);
        }
        if (tok <= START || tok >= END) {
            char next;
            if (tok > END) {
                next = lookAhead(NULL);
                if (tok == DIGIT) {
                    if (StringTokenizer.isDigit(next)) {
                        this.currentMatch = new Token();
                        this.currentMatch.tokenValue = String.valueOf(next);
                        this.currentMatch.tokenType = tok;
                        consume(1);
                    } else {
                        throw new ParseException(this.buffer + "\nExpecting DIGIT", this.ptr);
                    }
                } else if (tok == ALPHA) {
                    if (StringTokenizer.isAlpha(next)) {
                        this.currentMatch = new Token();
                        this.currentMatch.tokenValue = String.valueOf(next);
                        this.currentMatch.tokenType = tok;
                        consume(1);
                    } else {
                        throw new ParseException(this.buffer + "\nExpecting ALPHA", this.ptr);
                    }
                }
            }
            char ch = (char) tok;
            next = lookAhead(NULL);
            if (next == ch) {
                consume(1);
            } else {
                throw new ParseException(this.buffer + "\nExpecting  >>>" + ch + "<<< got >>>" + next + "<<<", this.ptr);
            }
        } else if (tok == ID) {
            if (startsId()) {
                id = getNextId();
                this.currentMatch = new Token();
                this.currentMatch.tokenValue = id;
                this.currentMatch.tokenType = ID;
            } else {
                throw new ParseException(this.buffer + "\nID expected", this.ptr);
            }
        } else if (tok != SAFE) {
            String nexttok = getNextId();
            Integer cur = (Integer) this.currentLexer.get(nexttok.toUpperCase());
            if (cur == null || cur.intValue() != tok) {
                throw new ParseException(this.buffer + "\nUnexpected Token : " + nexttok, this.ptr);
            }
            this.currentMatch = new Token();
            this.currentMatch.tokenValue = nexttok;
            this.currentMatch.tokenType = tok;
        } else if (startsSafeToken()) {
            id = ttokenSafe();
            this.currentMatch = new Token();
            this.currentMatch.tokenValue = id;
            this.currentMatch.tokenType = SAFE;
        } else {
            throw new ParseException(this.buffer + "\nID expected", this.ptr);
        }
        return this.currentMatch;
    }

    public void SPorHT() {
        try {
            char c = lookAhead(NULL);
            while (true) {
                if (c == ' ' || c == '\t') {
                    consume(1);
                    c = lookAhead(NULL);
                } else {
                    return;
                }
            }
        } catch (ParseException e) {
        }
    }

    public static final boolean isTokenChar(char c) {
        if (StringTokenizer.isAlphaDigit(c)) {
            return true;
        }
        switch (c) {
            case EXCLAMATION /*33*/:
            case PERCENT /*37*/:
            case QUOTE /*39*/:
            case STAR /*42*/:
            case PLUS /*43*/:
            case MINUS /*45*/:
            case DOT /*46*/:
            case UNDERSCORE /*95*/:
            case BACK_QUOTE /*96*/:
            case TILDE /*126*/:
                return true;
            default:
                return false;
        }
    }

    public boolean startsId() {
        try {
            return isTokenChar(lookAhead(NULL));
        } catch (ParseException e) {
            return false;
        }
    }

    public boolean startsSafeToken() {
        try {
            char nextChar = lookAhead(NULL);
            if (StringTokenizer.isAlphaDigit(nextChar)) {
                return true;
            }
            switch (nextChar) {
                case EXCLAMATION /*33*/:
                case DOUBLEQUOTE /*34*/:
                case POUND /*35*/:
                case DOLLAR /*36*/:
                case PERCENT /*37*/:
                case QUOTE /*39*/:
                case STAR /*42*/:
                case PLUS /*43*/:
                case MINUS /*45*/:
                case DOT /*46*/:
                case SLASH /*47*/:
                case COLON /*58*/:
                case SEMICOLON /*59*/:
                case EQUALS /*61*/:
                case QUESTION /*63*/:
                case AT /*64*/:
                case L_SQUARE_BRACKET /*91*/:
                case R_SQUARE_BRACKET /*93*/:
                case HAT /*94*/:
                case UNDERSCORE /*95*/:
                case BACK_QUOTE /*96*/:
                case L_CURLY /*123*/:
                case BAR /*124*/:
                case R_CURLY /*125*/:
                case TILDE /*126*/:
                    return true;
                default:
                    return false;
            }
        } catch (ParseException e) {
            return false;
        }
    }

    public String ttoken() {
        int startIdx = this.ptr;
        while (hasMoreChars() && isTokenChar(lookAhead(NULL))) {
            try {
                consume(1);
            } catch (ParseException e) {
                return null;
            }
        }
        return this.buffer.substring(startIdx, this.ptr);
    }

    public String ttokenSafe() {
        int startIdx = this.ptr;
        while (hasMoreChars()) {
            try {
                char nextChar = lookAhead(NULL);
                if (StringTokenizer.isAlphaDigit(nextChar)) {
                    consume(1);
                } else {
                    boolean isValidChar = false;
                    switch (nextChar) {
                        case EXCLAMATION /*33*/:
                        case DOUBLEQUOTE /*34*/:
                        case POUND /*35*/:
                        case DOLLAR /*36*/:
                        case PERCENT /*37*/:
                        case QUOTE /*39*/:
                        case STAR /*42*/:
                        case PLUS /*43*/:
                        case MINUS /*45*/:
                        case DOT /*46*/:
                        case SLASH /*47*/:
                        case COLON /*58*/:
                        case SEMICOLON /*59*/:
                        case QUESTION /*63*/:
                        case AT /*64*/:
                        case L_SQUARE_BRACKET /*91*/:
                        case R_SQUARE_BRACKET /*93*/:
                        case HAT /*94*/:
                        case UNDERSCORE /*95*/:
                        case BACK_QUOTE /*96*/:
                        case L_CURLY /*123*/:
                        case BAR /*124*/:
                        case R_CURLY /*125*/:
                        case TILDE /*126*/:
                            isValidChar = true;
                            break;
                    }
                    if (!isValidChar) {
                        return this.buffer.substring(startIdx, this.ptr);
                    }
                    consume(1);
                }
            } catch (ParseException e) {
                return null;
            }
        }
        return this.buffer.substring(startIdx, this.ptr);
    }

    public void consumeValidChars(char[] validChars) {
        int validCharsLength = validChars.length;
        while (hasMoreChars()) {
            try {
                char nextChar = lookAhead(NULL);
                boolean isValid = false;
                int i = NULL;
                while (i < validCharsLength) {
                    char validChar = validChars[i];
                    switch (validChar) {
                        case '\ufffd':
                            isValid = StringTokenizer.isAlphaDigit(nextChar);
                            break;
                        case '\ufffe':
                            isValid = StringTokenizer.isDigit(nextChar);
                            break;
                        case '\uffff':
                            isValid = StringTokenizer.isAlpha(nextChar);
                            break;
                        default:
                            if (nextChar != validChar) {
                                isValid = false;
                                break;
                            } else {
                                isValid = true;
                                break;
                            }
                    }
                    if (!isValid) {
                        i++;
                    } else if (isValid) {
                        consume(1);
                    } else {
                        return;
                    }
                }
                if (isValid) {
                    consume(1);
                } else {
                    return;
                }
            } catch (ParseException e) {
                return;
            }
        }
    }

    public String quotedString() throws ParseException {
        int startIdx = this.ptr + 1;
        if (lookAhead(NULL) != '\"') {
            return null;
        }
        consume(1);
        while (true) {
            char next = getNextChar();
            if (next == '\"') {
                return this.buffer.substring(startIdx, this.ptr - 1);
            }
            if (next == '\u0000') {
                break;
            } else if (next == '\\') {
                consume(1);
            }
        }
        throw new ParseException(this.buffer + " :unexpected EOL", this.ptr);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public String comment() throws ParseException {
        StringBuffer retval = new StringBuffer();
        if (lookAhead(NULL) != '(') {
            return null;
        }
        consume(1);
        while (true) {
            char next = getNextChar();
            if (next == ')') {
                return retval.toString();
            }
            if (next == '\u0000') {
                break;
            } else if (next == '\\') {
                retval.append(next);
                next = getNextChar();
                if (next == '\u0000') {
                    break;
                }
                retval.append(next);
            } else {
                retval.append(next);
            }
        }
        throw new ParseException(this.buffer + " : unexpected EOL", this.ptr);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public String byteStringNoSemicolon() {
        StringBuffer retval = new StringBuffer();
        while (true) {
            try {
                char next = lookAhead(NULL);
                if (next != '\u0000' && next != '\n' && next != ';' && next != ',') {
                    consume(1);
                    retval.append(next);
                }
            } catch (ParseException e) {
                return retval.toString();
            }
        }
        return retval.toString();
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public String byteStringNoSlash() {
        StringBuffer retval = new StringBuffer();
        while (true) {
            try {
                char next = lookAhead(NULL);
                if (next != '\u0000' && next != '\n' && next != '/') {
                    consume(1);
                    retval.append(next);
                }
            } catch (ParseException e) {
                return retval.toString();
            }
        }
        return retval.toString();
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public String byteStringNoComma() {
        StringBuffer retval = new StringBuffer();
        while (true) {
            try {
                char next = lookAhead(NULL);
                if (!(next == '\n' || next == ',')) {
                    consume(1);
                    retval.append(next);
                }
            } catch (ParseException e) {
            }
        }
        return retval.toString();
    }

    public static String charAsString(char ch) {
        return String.valueOf(ch);
    }

    public String charAsString(int nchars) {
        return this.buffer.substring(this.ptr, this.ptr + nchars);
    }

    public String number() throws ParseException {
        int startIdx = this.ptr;
        try {
            if (StringTokenizer.isDigit(lookAhead(NULL))) {
                consume(1);
                while (StringTokenizer.isDigit(lookAhead(NULL))) {
                    consume(1);
                }
                return this.buffer.substring(startIdx, this.ptr);
            }
            throw new ParseException(this.buffer + ": Unexpected token at " + lookAhead(NULL), this.ptr);
        } catch (ParseException e) {
            return this.buffer.substring(startIdx, this.ptr);
        }
    }

    public int markInputPosition() {
        return this.ptr;
    }

    public void rewindInputPosition(int position) {
        this.ptr = position;
    }

    public String getRest() {
        if (this.ptr >= this.buffer.length()) {
            return null;
        }
        return this.buffer.substring(this.ptr);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public String getString(char c) throws ParseException {
        StringBuffer retval = new StringBuffer();
        while (true) {
            char next = lookAhead(NULL);
            if (next == '\u0000') {
                break;
            } else if (next == c) {
                consume(1);
                return retval.toString();
            } else if (next == '\\') {
                consume(1);
                char nextchar = lookAhead(NULL);
                if (nextchar == '\u0000') {
                    break;
                }
                consume(1);
                retval.append(nextchar);
            } else {
                consume(1);
                retval.append(next);
            }
        }
        throw new ParseException(this.buffer + "unexpected EOL", this.ptr);
    }

    public int getPtr() {
        return this.ptr;
    }

    public String getBuffer() {
        return this.buffer;
    }

    public ParseException createParseException() {
        return new ParseException(this.buffer, this.ptr);
    }
}
