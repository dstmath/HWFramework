package gov.nist.javax.sip.parser;

import gov.nist.core.HostNameParser;
import gov.nist.core.NameValue;
import gov.nist.core.NameValueList;
import gov.nist.core.Separators;
import gov.nist.core.Token;
import gov.nist.javax.sip.address.GenericURI;
import gov.nist.javax.sip.address.SipUri;
import gov.nist.javax.sip.address.TelURLImpl;
import gov.nist.javax.sip.address.TelephoneNumber;
import java.io.PrintStream;
import java.text.ParseException;

public class URLParser extends Parser {
    public URLParser(String url) {
        this.lexer = new Lexer("sip_urlLexer", url);
    }

    public URLParser(Lexer lexer) {
        this.lexer = lexer;
        this.lexer.selectLexer("sip_urlLexer");
    }

    protected static boolean isMark(char next) {
        if (next == '!' || next == '_' || next == '~' || next == '-' || next == '.') {
            return true;
        }
        switch (next) {
            case '\'':
            case '(':
            case ')':
            case '*':
                return true;
            default:
                return false;
        }
    }

    protected static boolean isUnreserved(char next) {
        return Lexer.isAlphaDigit(next) || isMark(next);
    }

    protected static boolean isReservedNoSlash(char next) {
        if (next == '$' || next == '&' || next == '+' || next == ',' || next == ':' || next == ';' || next == '?' || next == '@') {
            return true;
        }
        return false;
    }

    protected static boolean isUserUnreserved(char la) {
        if (la == '#' || la == '$' || la == '&' || la == '/' || la == ';' || la == '=' || la == '?' || la == '+' || la == ',') {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public String unreserved() throws ParseException {
        char next = this.lexer.lookAhead(0);
        if (isUnreserved(next)) {
            this.lexer.consume(1);
            return String.valueOf(next);
        }
        throw createParseException("unreserved");
    }

    /* access modifiers changed from: protected */
    public String paramNameOrValue() throws ParseException {
        int startIdx = this.lexer.getPtr();
        while (this.lexer.hasMoreChars()) {
            char next = this.lexer.lookAhead(0);
            boolean isValidChar = false;
            if (next == '$' || next == '&' || next == '+' || next == '/' || next == ':' || next == '[' || next == ']') {
                isValidChar = true;
            }
            if (!isValidChar && !isUnreserved(next)) {
                if (!isEscaped()) {
                    break;
                }
                this.lexer.consume(3);
            } else {
                this.lexer.consume(1);
            }
        }
        return this.lexer.getBuffer().substring(startIdx, this.lexer.getPtr());
    }

    private NameValue uriParam() throws ParseException {
        if (debug) {
            dbg_enter("uriParam");
        }
        String pvalue = "";
        try {
            String pname = paramNameOrValue();
            boolean isFlagParam = true;
            if (this.lexer.lookAhead(0) == '=') {
                this.lexer.consume(1);
                pvalue = paramNameOrValue();
                isFlagParam = false;
            }
            if (pname.length() == 0 && (pvalue == null || pvalue.length() == 0)) {
                return null;
            }
            NameValue nameValue = new NameValue(pname, pvalue, isFlagParam);
            if (debug) {
                dbg_leave("uriParam");
            }
            return nameValue;
        } finally {
            if (debug) {
                dbg_leave("uriParam");
            }
        }
    }

    protected static boolean isReserved(char next) {
        if (next == '$' || next == '&' || next == '/' || next == '=' || next == '+' || next == ',' || next == ':' || next == ';' || next == '?' || next == '@') {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public String reserved() throws ParseException {
        char next = this.lexer.lookAhead(0);
        if (isReserved(next)) {
            this.lexer.consume(1);
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append(next);
            return stringBuffer.toString();
        }
        throw createParseException("reserved");
    }

    /* access modifiers changed from: protected */
    public boolean isEscaped() {
        try {
            return this.lexer.lookAhead(0) == '%' && Lexer.isHexDigit(this.lexer.lookAhead(1)) && Lexer.isHexDigit(this.lexer.lookAhead(2));
        } catch (Exception e) {
            return false;
        }
    }

    /* access modifiers changed from: protected */
    public String escaped() throws ParseException {
        if (debug) {
            dbg_enter("escaped");
        }
        try {
            StringBuffer retval = new StringBuffer();
            char next = this.lexer.lookAhead(0);
            char next1 = this.lexer.lookAhead(1);
            char next2 = this.lexer.lookAhead(2);
            if (next != '%' || !Lexer.isHexDigit(next1) || !Lexer.isHexDigit(next2)) {
                throw createParseException("escaped");
            }
            this.lexer.consume(3);
            retval.append(next);
            retval.append(next1);
            retval.append(next2);
            return retval.toString();
        } finally {
            if (debug) {
                dbg_leave("escaped");
            }
        }
    }

    /* access modifiers changed from: protected */
    public String mark() throws ParseException {
        if (debug) {
            dbg_enter("mark");
        }
        try {
            char next = this.lexer.lookAhead(0);
            if (isMark(next)) {
                this.lexer.consume(1);
                return new String(new char[]{next});
            }
            throw createParseException("mark");
        } finally {
            if (debug) {
                dbg_leave("mark");
            }
        }
    }

    /* access modifiers changed from: protected */
    public String uric() {
        if (debug) {
            dbg_enter("uric");
        }
        try {
            char la = this.lexer.lookAhead(0);
            if (isUnreserved(la)) {
                this.lexer.consume(1);
                String charAsString = Lexer.charAsString(la);
                if (debug) {
                    dbg_leave("uric");
                }
                return charAsString;
            } else if (isReserved(la)) {
                this.lexer.consume(1);
                String charAsString2 = Lexer.charAsString(la);
                if (debug) {
                    dbg_leave("uric");
                }
                return charAsString2;
            } else if (isEscaped()) {
                String retval = this.lexer.charAsString(3);
                this.lexer.consume(3);
                if (debug) {
                    dbg_leave("uric");
                }
                return retval;
            } else {
                if (debug) {
                    dbg_leave("uric");
                }
                return null;
            }
        } catch (Exception e) {
            if (debug) {
                dbg_leave("uric");
            }
            return null;
        } catch (Throwable th) {
            if (debug) {
                dbg_leave("uric");
            }
            throw th;
        }
    }

    /* access modifiers changed from: protected */
    public String uricNoSlash() {
        if (debug) {
            dbg_enter("uricNoSlash");
        }
        try {
            char la = this.lexer.lookAhead(0);
            if (isEscaped()) {
                String retval = this.lexer.charAsString(3);
                this.lexer.consume(3);
                if (debug) {
                    dbg_leave("uricNoSlash");
                }
                return retval;
            } else if (isUnreserved(la)) {
                this.lexer.consume(1);
                String charAsString = Lexer.charAsString(la);
                if (debug) {
                    dbg_leave("uricNoSlash");
                }
                return charAsString;
            } else if (isReservedNoSlash(la)) {
                this.lexer.consume(1);
                String charAsString2 = Lexer.charAsString(la);
                if (debug) {
                    dbg_leave("uricNoSlash");
                }
                return charAsString2;
            } else {
                if (debug) {
                    dbg_leave("uricNoSlash");
                }
                return null;
            }
        } catch (ParseException e) {
            if (debug) {
                dbg_leave("uricNoSlash");
            }
            return null;
        } catch (Throwable th) {
            if (debug) {
                dbg_leave("uricNoSlash");
            }
            throw th;
        }
    }

    /* access modifiers changed from: protected */
    public String uricString() throws ParseException {
        StringBuffer retval = new StringBuffer();
        while (true) {
            String next = uric();
            if (next != null) {
                retval.append(next);
            } else if (this.lexer.lookAhead(0) != '[') {
                return retval.toString();
            } else {
                retval.append(new HostNameParser(getLexer()).hostPort(false).toString());
            }
        }
    }

    /* JADX WARN: Multi-variable type inference failed */
    public GenericURI uriReference(boolean inBrackets) throws ParseException {
        GenericURI retval;
        if (debug) {
            dbg_enter("uriReference");
        }
        Token[] tokens = this.lexer.peekNextToken(2);
        Token t1 = tokens[0];
        Token t2 = tokens[1];
        try {
            if (t1.getTokenType() == 2051 || t1.getTokenType() == 2136) {
                if (t2.getTokenType() == 58) {
                    retval = sipURL(inBrackets);
                } else {
                    throw createParseException("Expecting ':'");
                }
            } else if (t1.getTokenType() != 2105) {
                try {
                    retval = new GenericURI(uricString());
                } catch (ParseException ex) {
                    throw createParseException(ex.getMessage());
                }
            } else if (t2.getTokenType() == 58) {
                retval = telURL(inBrackets);
            } else {
                throw createParseException("Expecting ':'");
            }
            return retval;
        } finally {
            if (debug) {
                dbg_leave("uriReference");
            }
        }
    }

    private String base_phone_number() throws ParseException {
        StringBuffer s = new StringBuffer();
        if (debug) {
            dbg_enter("base_phone_number");
        }
        int lc = 0;
        while (true) {
            try {
                if (!this.lexer.hasMoreChars()) {
                    break;
                }
                char w = this.lexer.lookAhead(0);
                if (!(Lexer.isDigit(w) || w == '-' || w == '.' || w == '(')) {
                    if (w != ')') {
                        if (lc <= 0) {
                            throw createParseException("unexpected " + w);
                        }
                    }
                }
                this.lexer.consume(1);
                s.append(w);
                lc++;
            } finally {
                if (debug) {
                    dbg_leave("base_phone_number");
                }
            }
        }
        return s.toString();
    }

    private String local_number() throws ParseException {
        StringBuffer s = new StringBuffer();
        if (debug) {
            dbg_enter("local_number");
        }
        int lc = 0;
        while (true) {
            try {
                if (!this.lexer.hasMoreChars()) {
                    break;
                }
                char la = this.lexer.lookAhead(0);
                if (!(la == '*' || la == '#' || la == '-' || la == '.' || la == '(' || la == ')')) {
                    if (!Lexer.isHexDigit(la)) {
                        if (lc <= 0) {
                            throw createParseException("unexepcted " + la);
                        }
                    }
                }
                this.lexer.consume(1);
                s.append(la);
                lc++;
            } finally {
                if (debug) {
                    dbg_leave("local_number");
                }
            }
        }
        return s.toString();
    }

    public final TelephoneNumber parseTelephoneNumber(boolean inBrackets) throws ParseException {
        TelephoneNumber tn;
        if (debug) {
            dbg_enter("telephone_subscriber");
        }
        this.lexer.selectLexer("charLexer");
        try {
            char c = this.lexer.lookAhead(0);
            if (c == '+') {
                tn = global_phone_number(inBrackets);
            } else {
                if (!(Lexer.isHexDigit(c) || c == '#' || c == '*' || c == '-' || c == '.' || c == '(')) {
                    if (c != ')') {
                        throw createParseException("unexpected char " + c);
                    }
                }
                tn = local_phone_number(inBrackets);
            }
            return tn;
        } finally {
            if (debug) {
                dbg_leave("telephone_subscriber");
            }
        }
    }

    private final TelephoneNumber global_phone_number(boolean inBrackets) throws ParseException {
        if (debug) {
            dbg_enter("global_phone_number");
        }
        try {
            TelephoneNumber tn = new TelephoneNumber();
            tn.setGlobal(true);
            this.lexer.match(43);
            tn.setPhoneNumber(base_phone_number());
            if (this.lexer.hasMoreChars() && this.lexer.lookAhead(0) == ';' && inBrackets) {
                this.lexer.consume(1);
                tn.setParameters(tel_parameters());
            }
            return tn;
        } finally {
            if (debug) {
                dbg_leave("global_phone_number");
            }
        }
    }

    private TelephoneNumber local_phone_number(boolean inBrackets) throws ParseException {
        if (debug) {
            dbg_enter("local_phone_number");
        }
        TelephoneNumber tn = new TelephoneNumber();
        tn.setGlobal(false);
        try {
            tn.setPhoneNumber(local_number());
            if (this.lexer.hasMoreChars()) {
                if (this.lexer.peekNextToken().getTokenType() == 59) {
                    if (inBrackets) {
                        this.lexer.consume(1);
                        tn.setParameters(tel_parameters());
                    }
                }
            }
            return tn;
        } finally {
            if (debug) {
                dbg_leave("local_phone_number");
            }
        }
    }

    private NameValueList tel_parameters() throws ParseException {
        NameValue nv;
        NameValueList nvList = new NameValueList();
        while (true) {
            String pname = paramNameOrValue();
            if (pname.equalsIgnoreCase("phone-context")) {
                nv = phone_context();
            } else if (this.lexer.lookAhead(0) == '=') {
                this.lexer.consume(1);
                nv = new NameValue(pname, paramNameOrValue(), false);
            } else {
                nv = new NameValue(pname, "", true);
            }
            nvList.set(nv);
            if (this.lexer.lookAhead(0) != ';') {
                return nvList;
            }
            this.lexer.consume(1);
        }
    }

    private NameValue phone_context() throws ParseException {
        Object value;
        this.lexer.match(61);
        char la = this.lexer.lookAhead(0);
        if (la == '+') {
            this.lexer.consume(1);
            value = "+" + base_phone_number();
        } else if (Lexer.isAlphaDigit(la)) {
            value = this.lexer.match(4095).getTokenValue();
        } else {
            throw new ParseException("Invalid phone-context:" + la, -1);
        }
        return new NameValue("phone-context", value, false);
    }

    public TelURLImpl telURL(boolean inBrackets) throws ParseException {
        this.lexer.match(TokenTypes.TEL);
        this.lexer.match(58);
        TelephoneNumber tn = parseTelephoneNumber(inBrackets);
        TelURLImpl telUrl = new TelURLImpl();
        telUrl.setTelephoneNumber(tn);
        return telUrl;
    }

    public SipUri sipURL(boolean inBrackets) throws ParseException {
        String scheme;
        int sipOrSips;
        if (debug) {
            dbg_enter("sipURL");
        }
        SipUri retval = new SipUri();
        if (this.lexer.peekNextToken().getTokenType() == 2136) {
            scheme = "sips";
            sipOrSips = 2136;
        } else {
            scheme = "sip";
            sipOrSips = 2051;
        }
        try {
            this.lexer.match(sipOrSips);
            this.lexer.match(58);
            retval.setScheme(scheme);
            int startOfUser = this.lexer.markInputPosition();
            String userOrHost = user();
            String passOrPort = null;
            if (this.lexer.lookAhead() == ':') {
                this.lexer.consume(1);
                passOrPort = password();
            }
            if (this.lexer.lookAhead() == '@') {
                this.lexer.consume(1);
                retval.setUser(userOrHost);
                if (passOrPort != null) {
                    retval.setUserPassword(passOrPort);
                }
            } else {
                this.lexer.rewindInputPosition(startOfUser);
            }
            retval.setHostPort(new HostNameParser(getLexer()).hostPort(false));
            this.lexer.selectLexer("charLexer");
            while (true) {
                if (!this.lexer.hasMoreChars() || this.lexer.lookAhead(0) != ';') {
                    break;
                } else if (!inBrackets) {
                    break;
                } else {
                    this.lexer.consume(1);
                    NameValue parms = uriParam();
                    if (parms != null) {
                        retval.setUriParameter(parms);
                    }
                }
            }
            if (this.lexer.hasMoreChars() && this.lexer.lookAhead(0) == '?') {
                this.lexer.consume(1);
                while (true) {
                    if (!this.lexer.hasMoreChars()) {
                        break;
                    }
                    retval.setQHeader(qheader());
                    if (this.lexer.hasMoreChars() && this.lexer.lookAhead(0) != '&') {
                        break;
                    }
                    this.lexer.consume(1);
                }
            }
            if (debug) {
                dbg_leave("sipURL");
            }
            return retval;
        } catch (RuntimeException e) {
            throw new ParseException("Invalid URL: " + this.lexer.getBuffer(), -1);
        } catch (Throwable th) {
            if (debug) {
                dbg_leave("sipURL");
            }
            throw th;
        }
    }

    public String peekScheme() throws ParseException {
        Token[] tokens = this.lexer.peekNextToken(1);
        if (tokens.length == 0) {
            return null;
        }
        return tokens[0].getTokenValue();
    }

    /* access modifiers changed from: protected */
    public NameValue qheader() throws ParseException {
        String name = this.lexer.getNextToken('=');
        this.lexer.consume(1);
        return new NameValue(name, hvalue(), false);
    }

    /* access modifiers changed from: protected */
    public String hvalue() throws ParseException {
        StringBuffer retval = new StringBuffer();
        while (this.lexer.hasMoreChars()) {
            char la = this.lexer.lookAhead(0);
            boolean isValidChar = false;
            if (!(la == '!' || la == '\"' || la == '$' || la == ':' || la == '?' || la == '[' || la == ']' || la == '_' || la == '~')) {
                switch (la) {
                    default:
                        switch (la) {
                        }
                    case '(':
                    case ')':
                    case '*':
                    case '+':
                        isValidChar = true;
                        break;
                }
                if (!isValidChar || Lexer.isAlphaDigit(la)) {
                    this.lexer.consume(1);
                    retval.append(la);
                } else if (la != '%') {
                    return retval.toString();
                } else {
                    retval.append(escaped());
                }
            }
            isValidChar = true;
            if (!isValidChar) {
            }
            this.lexer.consume(1);
            retval.append(la);
        }
        return retval.toString();
    }

    /* access modifiers changed from: protected */
    public String urlString() throws ParseException {
        char la;
        StringBuffer retval = new StringBuffer();
        this.lexer.selectLexer("charLexer");
        while (this.lexer.hasMoreChars() && (la = this.lexer.lookAhead(0)) != ' ' && la != '\t' && la != '\n' && la != '>' && la != '<') {
            this.lexer.consume(0);
            retval.append(la);
        }
        return retval.toString();
    }

    /* access modifiers changed from: protected */
    public String user() throws ParseException {
        if (debug) {
            dbg_enter("user");
        }
        try {
            int startIdx = this.lexer.getPtr();
            while (this.lexer.hasMoreChars()) {
                char la = this.lexer.lookAhead(0);
                if (!isUnreserved(la)) {
                    if (!isUserUnreserved(la)) {
                        if (!isEscaped()) {
                            break;
                        }
                        this.lexer.consume(3);
                    }
                }
                this.lexer.consume(1);
            }
            return this.lexer.getBuffer().substring(startIdx, this.lexer.getPtr());
        } finally {
            if (debug) {
                dbg_leave("user");
            }
        }
    }

    /* access modifiers changed from: protected */
    public String password() throws ParseException {
        int startIdx = this.lexer.getPtr();
        while (true) {
            char la = this.lexer.lookAhead(0);
            boolean isValidChar = false;
            if (la == '$' || la == '&' || la == '=' || la == '+' || la == ',') {
                isValidChar = true;
            }
            if (isValidChar || isUnreserved(la)) {
                this.lexer.consume(1);
            } else if (!isEscaped()) {
                return this.lexer.getBuffer().substring(startIdx, this.lexer.getPtr());
            } else {
                this.lexer.consume(3);
            }
        }
    }

    public GenericURI parse() throws ParseException {
        return uriReference(true);
    }

    public static void main(String[] args) throws ParseException {
        String[] test = {"sip:alice@example.com", "sips:alice@examples.com", "sip:3Zqkv5dajqaaas0tCjCxT0xH2ZEuEMsFl0xoasip%3A%2B3519116786244%40siplab.domain.com@213.0.115.163:7070"};
        for (int i = 0; i < test.length; i++) {
            GenericURI uri = new URLParser(test[i]).parse();
            PrintStream printStream = System.out;
            printStream.println("uri type returned " + uri.getClass().getName());
            PrintStream printStream2 = System.out;
            printStream2.println(test[i] + " is SipUri? " + uri.isSipURI() + Separators.GREATER_THAN + uri.encode());
        }
    }
}
