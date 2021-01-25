package gov.nist.core;

import java.text.ParseException;
import java.util.Hashtable;

public class LexerCore extends StringTokenizer {
    public static final int ALPHA = 4099;
    static final char ALPHADIGIT_VALID_CHARS = 65533;
    static final char ALPHA_VALID_CHARS = 65535;
    public static final int AND = 38;
    public static final int AT = 64;
    public static final int BACKSLASH = 92;
    public static final int BACK_QUOTE = 96;
    public static final int BAR = 124;
    public static final int COLON = 58;
    public static final int DIGIT = 4098;
    static final char DIGIT_VALID_CHARS = 65534;
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
    protected static final Hashtable globalSymbolTable = new Hashtable();
    protected static final Hashtable lexerTables = new Hashtable();
    protected Hashtable currentLexer;
    protected String currentLexerName;
    protected Token currentMatch;

    /* access modifiers changed from: protected */
    public void addKeyword(String name, int value) {
        Integer val = Integer.valueOf(value);
        this.currentLexer.put(name, val);
        if (!globalSymbolTable.containsKey(val)) {
            globalSymbolTable.put(val, name);
        }
    }

    public String lookupToken(int value) {
        if (value > 2048) {
            return (String) globalSymbolTable.get(Integer.valueOf(value));
        }
        return Character.valueOf((char) value).toString();
    }

    /* access modifiers changed from: protected */
    public Hashtable addLexer(String lexerName) {
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
        return peekNextToken(1)[0];
    }

    public Token[] peekNextToken(int ntokens) throws ParseException {
        int old = this.ptr;
        Token[] retval = new Token[ntokens];
        for (int i = 0; i < ntokens; i++) {
            Token tok = new Token();
            if (startsId()) {
                String id = ttoken();
                tok.tokenValue = id;
                String idUppercase = id.toUpperCase();
                if (this.currentLexer.containsKey(idUppercase)) {
                    tok.tokenType = ((Integer) this.currentLexer.get(idUppercase)).intValue();
                } else {
                    tok.tokenType = 4095;
                }
            } else {
                char nextChar = getNextChar();
                tok.tokenValue = String.valueOf(nextChar);
                if (isAlpha(nextChar)) {
                    tok.tokenType = 4099;
                } else if (isDigit(nextChar)) {
                    tok.tokenType = 4098;
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
        if (tok <= 2048 || tok >= 4096) {
            if (tok > 4096) {
                char next = lookAhead(0);
                if (tok == 4098) {
                    if (isDigit(next)) {
                        this.currentMatch = new Token();
                        this.currentMatch.tokenValue = String.valueOf(next);
                        this.currentMatch.tokenType = tok;
                        consume(1);
                    } else {
                        throw new ParseException(this.buffer + "\nExpecting DIGIT", this.ptr);
                    }
                } else if (tok == 4099) {
                    if (isAlpha(next)) {
                        this.currentMatch = new Token();
                        this.currentMatch.tokenValue = String.valueOf(next);
                        this.currentMatch.tokenType = tok;
                        consume(1);
                    } else {
                        throw new ParseException(this.buffer + "\nExpecting ALPHA", this.ptr);
                    }
                }
            } else {
                char ch = (char) tok;
                char next2 = lookAhead(0);
                if (next2 == ch) {
                    consume(1);
                } else {
                    throw new ParseException(this.buffer + "\nExpecting  >>>" + ch + "<<< got >>>" + next2 + "<<<", this.ptr);
                }
            }
        } else if (tok == 4095) {
            if (startsId()) {
                String id = getNextId();
                this.currentMatch = new Token();
                Token token = this.currentMatch;
                token.tokenValue = id;
                token.tokenType = 4095;
            } else {
                throw new ParseException(this.buffer + "\nID expected", this.ptr);
            }
        } else if (tok != 4094) {
            String nexttok = getNextId();
            Integer cur = (Integer) this.currentLexer.get(nexttok.toUpperCase());
            if (cur == null || cur.intValue() != tok) {
                throw new ParseException(this.buffer + "\nUnexpected Token : " + nexttok, this.ptr);
            }
            this.currentMatch = new Token();
            Token token2 = this.currentMatch;
            token2.tokenValue = nexttok;
            token2.tokenType = tok;
        } else if (startsSafeToken()) {
            String id2 = ttokenSafe();
            this.currentMatch = new Token();
            Token token3 = this.currentMatch;
            token3.tokenValue = id2;
            token3.tokenType = SAFE;
        } else {
            throw new ParseException(this.buffer + "\nID expected", this.ptr);
        }
        return this.currentMatch;
    }

    public void SPorHT() {
        try {
            char c = lookAhead(0);
            while (true) {
                if (c != ' ') {
                    if (c != '\t') {
                        return;
                    }
                }
                consume(1);
                c = lookAhead(0);
            }
        } catch (ParseException e) {
        }
    }

    public static final boolean isTokenChar(char c) {
        if (isAlphaDigit(c) || c == '!' || c == '%' || c == '\'' || c == '~' || c == '*' || c == '+' || c == '-' || c == '.' || c == '_' || c == '`') {
            return true;
        }
        return false;
    }

    public boolean startsId() {
        try {
            return isTokenChar(lookAhead(0));
        } catch (ParseException e) {
            return false;
        }
    }

    public boolean startsSafeToken() {
        try {
            char nextChar = lookAhead(0);
            if (!(isAlphaDigit(nextChar) || nextChar == '\'' || nextChar == '=' || nextChar == '[' || nextChar == '*' || nextChar == '+' || nextChar == ':' || nextChar == ';' || nextChar == '?' || nextChar == '@')) {
                switch (nextChar) {
                    case '!':
                    case '\"':
                    case '#':
                    case '$':
                    case '%':
                        break;
                    default:
                        switch (nextChar) {
                            case '-':
                            case '.':
                            case '/':
                                break;
                            default:
                                switch (nextChar) {
                                    case ']':
                                    case '^':
                                    case '_':
                                    case '`':
                                        break;
                                    default:
                                        switch (nextChar) {
                                            case '{':
                                            case '|':
                                            case '}':
                                            case '~':
                                                break;
                                            default:
                                                return false;
                                        }
                                }
                        }
                }
            }
            return true;
        } catch (ParseException e) {
            return false;
        }
    }

    public String ttoken() {
        int startIdx = this.ptr;
        while (hasMoreChars() && isTokenChar(lookAhead(0))) {
            try {
                consume(1);
            } catch (ParseException e) {
                return null;
            }
        }
        return this.buffer.substring(startIdx, this.ptr);
    }

    /* JADX WARNING: Removed duplicated region for block: B:28:0x0046 A[Catch:{ ParseException -> 0x0056 }, FALL_THROUGH] */
    /* JADX WARNING: Removed duplicated region for block: B:30:0x0049 A[Catch:{ ParseException -> 0x0056 }] */
    /* JADX WARNING: Removed duplicated region for block: B:37:0x004d A[SYNTHETIC] */
    public String ttokenSafe() {
        int startIdx = this.ptr;
        while (hasMoreChars()) {
            try {
                char nextChar = lookAhead(0);
                if (isAlphaDigit(nextChar)) {
                    consume(1);
                } else {
                    boolean isValidChar = false;
                    if (!(nextChar == '\'' || nextChar == '[' || nextChar == '*' || nextChar == '+' || nextChar == ':' || nextChar == ';' || nextChar == '?' || nextChar == '@')) {
                        switch (nextChar) {
                            default:
                                switch (nextChar) {
                                    default:
                                        switch (nextChar) {
                                            default:
                                                switch (nextChar) {
                                                }
                                            case ']':
                                            case '^':
                                            case '_':
                                            case '`':
                                                isValidChar = true;
                                                break;
                                        }
                                    case '-':
                                    case '.':
                                    case '/':
                                        break;
                                }
                            case '!':
                            case '\"':
                            case '#':
                            case '$':
                            case '%':
                                break;
                        }
                        if (isValidChar) {
                            return this.buffer.substring(startIdx, this.ptr);
                        }
                        consume(1);
                    }
                    isValidChar = true;
                    if (isValidChar) {
                    }
                }
            } catch (ParseException e) {
                return null;
            }
        }
        return this.buffer.substring(startIdx, this.ptr);
    }

    public void consumeValidChars(char[] validChars) {
        boolean z;
        int validCharsLength = validChars.length;
        while (hasMoreChars()) {
            try {
                char nextChar = lookAhead(0);
                boolean isValid = false;
                int i = 0;
                while (true) {
                    if (i < validCharsLength) {
                        char validChar = validChars[i];
                        switch (validChar) {
                            case 65533:
                                isValid = isAlphaDigit(nextChar);
                                break;
                            case 65534:
                                isValid = isDigit(nextChar);
                                break;
                            case 65535:
                                isValid = isAlpha(nextChar);
                                break;
                            default:
                                if (nextChar == validChar) {
                                    z = true;
                                } else {
                                    z = false;
                                }
                                isValid = z;
                                break;
                        }
                        if (!isValid) {
                            i++;
                        }
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
        if (lookAhead(0) != '\"') {
            return null;
        }
        consume(1);
        while (true) {
            char next = getNextChar();
            if (next == '\"') {
                return this.buffer.substring(startIdx, this.ptr - 1);
            }
            if (next == 0) {
                throw new ParseException(this.buffer + " :unexpected EOL", this.ptr);
            } else if (next == '\\') {
                consume(1);
            }
        }
    }

    public String comment() throws ParseException {
        StringBuffer retval = new StringBuffer();
        if (lookAhead(0) != '(') {
            return null;
        }
        consume(1);
        while (true) {
            char next = getNextChar();
            if (next == ')') {
                return retval.toString();
            }
            if (next == 0) {
                throw new ParseException(this.buffer + " :unexpected EOL", this.ptr);
            } else if (next == '\\') {
                retval.append(next);
                char next2 = getNextChar();
                if (next2 != 0) {
                    retval.append(next2);
                } else {
                    throw new ParseException(this.buffer + " : unexpected EOL", this.ptr);
                }
            } else {
                retval.append(next);
            }
        }
    }

    public String byteStringNoSemicolon() {
        StringBuffer retval = new StringBuffer();
        while (true) {
            try {
                char next = lookAhead(0);
                if (next == 0 || next == '\n' || next == ';') {
                    break;
                } else if (next == ',') {
                    break;
                } else {
                    consume(1);
                    retval.append(next);
                }
            } catch (ParseException e) {
                return retval.toString();
            }
        }
        return retval.toString();
    }

    public String byteStringNoSlash() {
        StringBuffer retval = new StringBuffer();
        while (true) {
            try {
                char next = lookAhead(0);
                if (next == 0 || next == '\n') {
                    break;
                } else if (next == '/') {
                    break;
                } else {
                    consume(1);
                    retval.append(next);
                }
            } catch (ParseException e) {
                return retval.toString();
            }
        }
        return retval.toString();
    }

    public String byteStringNoComma() {
        StringBuffer retval = new StringBuffer();
        while (true) {
            try {
                char next = lookAhead(0);
                if (next == '\n') {
                    break;
                } else if (next == ',') {
                    break;
                } else {
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
            if (isDigit(lookAhead(0))) {
                consume(1);
                while (isDigit(lookAhead(0))) {
                    consume(1);
                }
                return this.buffer.substring(startIdx, this.ptr);
            }
            throw new ParseException(this.buffer + ": Unexpected token at " + lookAhead(0), this.ptr);
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

    public String getString(char c) throws ParseException {
        StringBuffer retval = new StringBuffer();
        while (true) {
            char next = lookAhead(0);
            if (next == 0) {
                throw new ParseException(this.buffer + "unexpected EOL", this.ptr);
            } else if (next == c) {
                consume(1);
                return retval.toString();
            } else if (next == '\\') {
                consume(1);
                char nextchar = lookAhead(0);
                if (nextchar != 0) {
                    consume(1);
                    retval.append(nextchar);
                } else {
                    throw new ParseException(this.buffer + "unexpected EOL", this.ptr);
                }
            } else {
                consume(1);
                retval.append(next);
            }
        }
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
