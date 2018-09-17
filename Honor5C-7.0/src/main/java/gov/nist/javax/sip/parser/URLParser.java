package gov.nist.javax.sip.parser;

import gov.nist.core.HostNameParser;
import gov.nist.core.NameValue;
import gov.nist.core.NameValueList;
import gov.nist.core.Separators;
import gov.nist.core.StringTokenizer;
import gov.nist.core.Token;
import gov.nist.javax.sip.address.GenericURI;
import gov.nist.javax.sip.address.SipUri;
import gov.nist.javax.sip.address.TelURLImpl;
import gov.nist.javax.sip.address.TelephoneNumber;
import java.text.ParseException;
import org.ccil.cowan.tagsoup.HTMLModels;

public class URLParser extends Parser {
    protected java.lang.String uric() {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.JadxRuntimeException: Can't find block by offset: 0x007c in list []
	at jadx.core.utils.BlockUtils.getBlockByOffset(BlockUtils.java:42)
	at jadx.core.dex.instructions.IfNode.initBlocks(IfNode.java:58)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.initBlocksInIfNodes(BlockFinish.java:48)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.visit(BlockFinish.java:33)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
        /*
        r6 = this;
        r5 = 0;
        r3 = debug;
        if (r3 == 0) goto L_0x000b;
    L_0x0005:
        r3 = "uric";
        r6.dbg_enter(r3);
    L_0x000b:
        r3 = r6.lexer;	 Catch:{ Exception -> 0x0071, all -> 0x007d }
        r4 = 0;	 Catch:{ Exception -> 0x0071, all -> 0x007d }
        r1 = r3.lookAhead(r4);	 Catch:{ Exception -> 0x0071, all -> 0x007d }
        r3 = isUnreserved(r1);	 Catch:{ Exception -> 0x0071, all -> 0x007d }
        if (r3 == 0) goto L_0x002d;	 Catch:{ Exception -> 0x0071, all -> 0x007d }
    L_0x0018:
        r3 = r6.lexer;	 Catch:{ Exception -> 0x0071, all -> 0x007d }
        r4 = 1;	 Catch:{ Exception -> 0x0071, all -> 0x007d }
        r3.consume(r4);	 Catch:{ Exception -> 0x0071, all -> 0x007d }
        r3 = gov.nist.core.LexerCore.charAsString(r1);	 Catch:{ Exception -> 0x0071, all -> 0x007d }
        r4 = debug;
        if (r4 == 0) goto L_0x002c;
    L_0x0026:
        r4 = "uric";
        r6.dbg_leave(r4);
    L_0x002c:
        return r3;
    L_0x002d:
        r3 = isReserved(r1);	 Catch:{ Exception -> 0x0071, all -> 0x007d }
        if (r3 == 0) goto L_0x0048;	 Catch:{ Exception -> 0x0071, all -> 0x007d }
    L_0x0033:
        r3 = r6.lexer;	 Catch:{ Exception -> 0x0071, all -> 0x007d }
        r4 = 1;	 Catch:{ Exception -> 0x0071, all -> 0x007d }
        r3.consume(r4);	 Catch:{ Exception -> 0x0071, all -> 0x007d }
        r3 = gov.nist.core.LexerCore.charAsString(r1);	 Catch:{ Exception -> 0x0071, all -> 0x007d }
        r4 = debug;
        if (r4 == 0) goto L_0x0047;
    L_0x0041:
        r4 = "uric";
        r6.dbg_leave(r4);
    L_0x0047:
        return r3;
    L_0x0048:
        r3 = r6.isEscaped();	 Catch:{ Exception -> 0x0071, all -> 0x007d }
        if (r3 == 0) goto L_0x0066;	 Catch:{ Exception -> 0x0071, all -> 0x007d }
    L_0x004e:
        r3 = r6.lexer;	 Catch:{ Exception -> 0x0071, all -> 0x007d }
        r4 = 3;	 Catch:{ Exception -> 0x0071, all -> 0x007d }
        r2 = r3.charAsString(r4);	 Catch:{ Exception -> 0x0071, all -> 0x007d }
        r3 = r6.lexer;	 Catch:{ Exception -> 0x0071, all -> 0x007d }
        r4 = 3;	 Catch:{ Exception -> 0x0071, all -> 0x007d }
        r3.consume(r4);	 Catch:{ Exception -> 0x0071, all -> 0x007d }
        r3 = debug;
        if (r3 == 0) goto L_0x0065;
    L_0x005f:
        r3 = "uric";
        r6.dbg_leave(r3);
    L_0x0065:
        return r2;
    L_0x0066:
        r3 = debug;
        if (r3 == 0) goto L_0x0070;
    L_0x006a:
        r3 = "uric";
        r6.dbg_leave(r3);
    L_0x0070:
        return r5;
    L_0x0071:
        r0 = move-exception;
        r3 = debug;
        if (r3 == 0) goto L_0x007c;
    L_0x0076:
        r3 = "uric";
        r6.dbg_leave(r3);
    L_0x007c:
        return r5;
    L_0x007d:
        r3 = move-exception;
        r4 = debug;
        if (r4 == 0) goto L_0x0088;
    L_0x0082:
        r4 = "uric";
        r6.dbg_leave(r4);
    L_0x0088:
        throw r3;
        */
        throw new UnsupportedOperationException("Method not decompiled: gov.nist.javax.sip.parser.URLParser.uric():java.lang.String");
    }

    protected java.lang.String uricNoSlash() {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.JadxRuntimeException: Can't find block by offset: 0x007c in list []
	at jadx.core.utils.BlockUtils.getBlockByOffset(BlockUtils.java:42)
	at jadx.core.dex.instructions.IfNode.initBlocks(IfNode.java:58)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.initBlocksInIfNodes(BlockFinish.java:48)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.visit(BlockFinish.java:33)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
        /*
        r6 = this;
        r5 = 0;
        r3 = debug;
        if (r3 == 0) goto L_0x000b;
    L_0x0005:
        r3 = "uricNoSlash";
        r6.dbg_enter(r3);
    L_0x000b:
        r3 = r6.lexer;	 Catch:{ ParseException -> 0x0071, all -> 0x007d }
        r4 = 0;	 Catch:{ ParseException -> 0x0071, all -> 0x007d }
        r1 = r3.lookAhead(r4);	 Catch:{ ParseException -> 0x0071, all -> 0x007d }
        r3 = r6.isEscaped();	 Catch:{ ParseException -> 0x0071, all -> 0x007d }
        if (r3 == 0) goto L_0x0030;	 Catch:{ ParseException -> 0x0071, all -> 0x007d }
    L_0x0018:
        r3 = r6.lexer;	 Catch:{ ParseException -> 0x0071, all -> 0x007d }
        r4 = 3;	 Catch:{ ParseException -> 0x0071, all -> 0x007d }
        r2 = r3.charAsString(r4);	 Catch:{ ParseException -> 0x0071, all -> 0x007d }
        r3 = r6.lexer;	 Catch:{ ParseException -> 0x0071, all -> 0x007d }
        r4 = 3;	 Catch:{ ParseException -> 0x0071, all -> 0x007d }
        r3.consume(r4);	 Catch:{ ParseException -> 0x0071, all -> 0x007d }
        r3 = debug;
        if (r3 == 0) goto L_0x002f;
    L_0x0029:
        r3 = "uricNoSlash";
        r6.dbg_leave(r3);
    L_0x002f:
        return r2;
    L_0x0030:
        r3 = isUnreserved(r1);	 Catch:{ ParseException -> 0x0071, all -> 0x007d }
        if (r3 == 0) goto L_0x004b;	 Catch:{ ParseException -> 0x0071, all -> 0x007d }
    L_0x0036:
        r3 = r6.lexer;	 Catch:{ ParseException -> 0x0071, all -> 0x007d }
        r4 = 1;	 Catch:{ ParseException -> 0x0071, all -> 0x007d }
        r3.consume(r4);	 Catch:{ ParseException -> 0x0071, all -> 0x007d }
        r3 = gov.nist.core.LexerCore.charAsString(r1);	 Catch:{ ParseException -> 0x0071, all -> 0x007d }
        r4 = debug;
        if (r4 == 0) goto L_0x004a;
    L_0x0044:
        r4 = "uricNoSlash";
        r6.dbg_leave(r4);
    L_0x004a:
        return r3;
    L_0x004b:
        r3 = isReservedNoSlash(r1);	 Catch:{ ParseException -> 0x0071, all -> 0x007d }
        if (r3 == 0) goto L_0x0066;	 Catch:{ ParseException -> 0x0071, all -> 0x007d }
    L_0x0051:
        r3 = r6.lexer;	 Catch:{ ParseException -> 0x0071, all -> 0x007d }
        r4 = 1;	 Catch:{ ParseException -> 0x0071, all -> 0x007d }
        r3.consume(r4);	 Catch:{ ParseException -> 0x0071, all -> 0x007d }
        r3 = gov.nist.core.LexerCore.charAsString(r1);	 Catch:{ ParseException -> 0x0071, all -> 0x007d }
        r4 = debug;
        if (r4 == 0) goto L_0x0065;
    L_0x005f:
        r4 = "uricNoSlash";
        r6.dbg_leave(r4);
    L_0x0065:
        return r3;
    L_0x0066:
        r3 = debug;
        if (r3 == 0) goto L_0x0070;
    L_0x006a:
        r3 = "uricNoSlash";
        r6.dbg_leave(r3);
    L_0x0070:
        return r5;
    L_0x0071:
        r0 = move-exception;
        r3 = debug;
        if (r3 == 0) goto L_0x007c;
    L_0x0076:
        r3 = "uricNoSlash";
        r6.dbg_leave(r3);
    L_0x007c:
        return r5;
    L_0x007d:
        r3 = move-exception;
        r4 = debug;
        if (r4 == 0) goto L_0x0088;
    L_0x0082:
        r4 = "uricNoSlash";
        r6.dbg_leave(r4);
    L_0x0088:
        throw r3;
        */
        throw new UnsupportedOperationException("Method not decompiled: gov.nist.javax.sip.parser.URLParser.uricNoSlash():java.lang.String");
    }

    public URLParser(String url) {
        this.lexer = new Lexer("sip_urlLexer", url);
    }

    public URLParser(Lexer lexer) {
        this.lexer = lexer;
        this.lexer.selectLexer("sip_urlLexer");
    }

    protected static boolean isMark(char next) {
        switch (next) {
            case TokenTypes.EXCLAMATION /*33*/:
            case TokenTypes.QUOTE /*39*/:
            case TokenTypes.LPAREN /*40*/:
            case TokenTypes.RPAREN /*41*/:
            case TokenTypes.STAR /*42*/:
            case TokenTypes.MINUS /*45*/:
            case TokenTypes.DOT /*46*/:
            case TokenTypes.UNDERSCORE /*95*/:
            case TokenTypes.TILDE /*126*/:
                return true;
            default:
                return false;
        }
    }

    protected static boolean isUnreserved(char next) {
        return !StringTokenizer.isAlphaDigit(next) ? isMark(next) : true;
    }

    protected static boolean isReservedNoSlash(char next) {
        switch (next) {
            case TokenTypes.DOLLAR /*36*/:
            case TokenTypes.AND /*38*/:
            case TokenTypes.PLUS /*43*/:
            case ',':
            case TokenTypes.COLON /*58*/:
            case TokenTypes.SEMICOLON /*59*/:
            case TokenTypes.QUESTION /*63*/:
            case HTMLModels.M_COL /*64*/:
                return true;
            default:
                return false;
        }
    }

    protected static boolean isUserUnreserved(char la) {
        switch (la) {
            case TokenTypes.POUND /*35*/:
            case TokenTypes.DOLLAR /*36*/:
            case TokenTypes.AND /*38*/:
            case TokenTypes.PLUS /*43*/:
            case ',':
            case TokenTypes.SLASH /*47*/:
            case TokenTypes.SEMICOLON /*59*/:
            case TokenTypes.EQUALS /*61*/:
            case TokenTypes.QUESTION /*63*/:
                return true;
            default:
                return false;
        }
    }

    protected String unreserved() throws ParseException {
        char next = this.lexer.lookAhead(0);
        if (isUnreserved(next)) {
            this.lexer.consume(1);
            return String.valueOf(next);
        }
        throw createParseException("unreserved");
    }

    protected String paramNameOrValue() throws ParseException {
        int startIdx = this.lexer.getPtr();
        while (this.lexer.hasMoreChars()) {
            char next = this.lexer.lookAhead(0);
            boolean isValidChar = false;
            switch (next) {
                case TokenTypes.DOLLAR /*36*/:
                case TokenTypes.AND /*38*/:
                case TokenTypes.PLUS /*43*/:
                case TokenTypes.SLASH /*47*/:
                case TokenTypes.COLON /*58*/:
                case TokenTypes.L_SQUARE_BRACKET /*91*/:
                case TokenTypes.R_SQUARE_BRACKET /*93*/:
                    isValidChar = true;
                    break;
            }
            if (isValidChar || isUnreserved(next)) {
                this.lexer.consume(1);
            } else if (!isEscaped()) {
                return this.lexer.getBuffer().substring(startIdx, this.lexer.getPtr());
            } else {
                this.lexer.consume(3);
            }
        }
        return this.lexer.getBuffer().substring(startIdx, this.lexer.getPtr());
    }

    private NameValue uriParam() throws ParseException {
        if (debug) {
            dbg_enter("uriParam");
        }
        try {
            String pvalue = "";
            String pname = paramNameOrValue();
            boolean isFlagParam = true;
            if (this.lexer.lookAhead(0) == '=') {
                this.lexer.consume(1);
                pvalue = paramNameOrValue();
                isFlagParam = false;
            }
            if (pname.length() == 0 && (pvalue == null || pvalue.length() == 0)) {
                if (debug) {
                    dbg_leave("uriParam");
                }
                return null;
            }
            NameValue nameValue = new NameValue(pname, pvalue, isFlagParam);
            if (debug) {
                dbg_leave("uriParam");
            }
            return nameValue;
        } catch (Throwable th) {
            if (debug) {
                dbg_leave("uriParam");
            }
        }
    }

    protected static boolean isReserved(char next) {
        switch (next) {
            case TokenTypes.DOLLAR /*36*/:
            case TokenTypes.AND /*38*/:
            case TokenTypes.PLUS /*43*/:
            case ',':
            case TokenTypes.SLASH /*47*/:
            case TokenTypes.COLON /*58*/:
            case TokenTypes.SEMICOLON /*59*/:
            case TokenTypes.EQUALS /*61*/:
            case TokenTypes.QUESTION /*63*/:
            case HTMLModels.M_COL /*64*/:
                return true;
            default:
                return false;
        }
    }

    protected String reserved() throws ParseException {
        char next = this.lexer.lookAhead(0);
        if (isReserved(next)) {
            this.lexer.consume(1);
            return new StringBuffer().append(next).toString();
        }
        throw createParseException("reserved");
    }

    protected boolean isEscaped() {
        boolean z = false;
        try {
            if (this.lexer.lookAhead(0) == '%' && StringTokenizer.isHexDigit(this.lexer.lookAhead(1))) {
                z = StringTokenizer.isHexDigit(this.lexer.lookAhead(2));
            }
            return z;
        } catch (Exception e) {
            return false;
        }
    }

    protected String escaped() throws ParseException {
        if (debug) {
            dbg_enter("escaped");
        }
        try {
            StringBuffer retval = new StringBuffer();
            char next = this.lexer.lookAhead(0);
            char next1 = this.lexer.lookAhead(1);
            char next2 = this.lexer.lookAhead(2);
            if (next == '%' && StringTokenizer.isHexDigit(next1) && StringTokenizer.isHexDigit(next2)) {
                this.lexer.consume(3);
                retval.append(next);
                retval.append(next1);
                retval.append(next2);
                String stringBuffer = retval.toString();
                return stringBuffer;
            }
            throw createParseException("escaped");
        } finally {
            if (debug) {
                dbg_leave("escaped");
            }
        }
    }

    protected String mark() throws ParseException {
        if (debug) {
            dbg_enter("mark");
        }
        try {
            if (isMark(this.lexer.lookAhead(0))) {
                this.lexer.consume(1);
                String str = new String(new char[]{next});
                return str;
            }
            throw createParseException("mark");
        } finally {
            if (debug) {
                dbg_leave("mark");
            }
        }
    }

    protected String uricString() throws ParseException {
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

    public GenericURI uriReference(boolean inBrackets) throws ParseException {
        if (debug) {
            dbg_enter("uriReference");
        }
        Token[] tokens = this.lexer.peekNextToken(2);
        Token t1 = tokens[0];
        Token t2 = tokens[1];
        try {
            GenericURI retval;
            if (t1.getTokenType() == TokenTypes.SIP || t1.getTokenType() == TokenTypes.SIPS) {
                if (t2.getTokenType() == 58) {
                    retval = sipURL(inBrackets);
                } else {
                    throw createParseException("Expecting ':'");
                }
            } else if (t1.getTokenType() != TokenTypes.TEL) {
                retval = new GenericURI(uricString());
            } else if (t2.getTokenType() == 58) {
                retval = telURL(inBrackets);
            } else {
                throw createParseException("Expecting ':'");
            }
            if (debug) {
                dbg_leave("uriReference");
            }
            return retval;
        } catch (ParseException ex) {
            throw createParseException(ex.getMessage());
        } catch (Throwable th) {
            if (debug) {
                dbg_leave("uriReference");
            }
        }
    }

    private String base_phone_number() throws ParseException {
        String stringBuffer;
        StringBuffer s = new StringBuffer();
        if (debug) {
            dbg_enter("base_phone_number");
        }
        int lc = 0;
        while (this.lexer.hasMoreChars()) {
            try {
                char w = this.lexer.lookAhead(0);
                if (StringTokenizer.isDigit(w) || w == '-' || w == '.' || w == '(' || w == ')') {
                    this.lexer.consume(1);
                    s.append(w);
                    lc++;
                } else {
                    if (lc <= 0) {
                        throw createParseException("unexpected " + w);
                    }
                    stringBuffer = s.toString();
                    return stringBuffer;
                }
            } finally {
                if (debug) {
                    dbg_leave("base_phone_number");
                }
            }
        }
        stringBuffer = s.toString();
        return stringBuffer;
    }

    private String local_number() throws ParseException {
        String stringBuffer;
        StringBuffer s = new StringBuffer();
        if (debug) {
            dbg_enter("local_number");
        }
        int lc = 0;
        while (this.lexer.hasMoreChars()) {
            try {
                char la = this.lexer.lookAhead(0);
                if (la == '*' || la == '#' || la == '-' || la == '.' || la == '(' || la == ')' || StringTokenizer.isHexDigit(la)) {
                    this.lexer.consume(1);
                    s.append(la);
                    lc++;
                } else {
                    if (lc <= 0) {
                        throw createParseException("unexepcted " + la);
                    }
                    stringBuffer = s.toString();
                    return stringBuffer;
                }
            } finally {
                if (debug) {
                    dbg_leave("local_number");
                }
            }
        }
        stringBuffer = s.toString();
        return stringBuffer;
    }

    public final TelephoneNumber parseTelephoneNumber(boolean inBrackets) throws ParseException {
        if (debug) {
            dbg_enter("telephone_subscriber");
        }
        this.lexer.selectLexer("charLexer");
        try {
            TelephoneNumber tn;
            char c = this.lexer.lookAhead(0);
            if (c == '+') {
                tn = global_phone_number(inBrackets);
            } else if (StringTokenizer.isHexDigit(c) || c == '#' || c == '*' || c == '-' || c == '.' || c == '(' || c == ')') {
                tn = local_phone_number(inBrackets);
            } else {
                throw createParseException("unexpected char " + c);
            }
            if (debug) {
                dbg_leave("telephone_subscriber");
            }
            return tn;
        } catch (Throwable th) {
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
            if (debug) {
                dbg_leave("global_phone_number");
            }
            return tn;
        } catch (Throwable th) {
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
                switch (this.lexer.peekNextToken().getTokenType()) {
                    case TokenTypes.SEMICOLON /*59*/:
                        if (inBrackets) {
                            this.lexer.consume(1);
                            tn.setParameters(tel_parameters());
                            break;
                        }
                        break;
                }
            }
            if (debug) {
                dbg_leave("local_phone_number");
            }
            return tn;
        } catch (Throwable th) {
            if (debug) {
                dbg_leave("local_phone_number");
            }
        }
    }

    private NameValueList tel_parameters() throws ParseException {
        NameValueList nvList = new NameValueList();
        while (true) {
            NameValue nv;
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
        } else if (StringTokenizer.isAlphaDigit(la)) {
            value = this.lexer.match(TokenTypes.ID).getTokenValue();
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
        if (debug) {
            dbg_enter("sipURL");
        }
        SipUri retval = new SipUri();
        Token nextToken = this.lexer.peekNextToken();
        int sipOrSips = TokenTypes.SIP;
        String scheme = TokenNames.SIP;
        if (nextToken.getTokenType() == TokenTypes.SIPS) {
            sipOrSips = TokenTypes.SIPS;
            scheme = TokenNames.SIPS;
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
            while (this.lexer.hasMoreChars() && this.lexer.lookAhead(0) == ';' && inBrackets) {
                this.lexer.consume(1);
                NameValue parms = uriParam();
                if (parms != null) {
                    retval.setUriParameter(parms);
                }
            }
            if (this.lexer.hasMoreChars() && this.lexer.lookAhead(0) == '?') {
                this.lexer.consume(1);
                while (this.lexer.hasMoreChars()) {
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
        }
    }

    public String peekScheme() throws ParseException {
        Token[] tokens = this.lexer.peekNextToken(1);
        if (tokens.length == 0) {
            return null;
        }
        return tokens[0].getTokenValue();
    }

    protected NameValue qheader() throws ParseException {
        String name = this.lexer.getNextToken('=');
        this.lexer.consume(1);
        return new NameValue(name, hvalue(), false);
    }

    protected String hvalue() throws ParseException {
        StringBuffer retval = new StringBuffer();
        while (this.lexer.hasMoreChars()) {
            char la = this.lexer.lookAhead(0);
            boolean isValidChar = false;
            switch (la) {
                case TokenTypes.EXCLAMATION /*33*/:
                case TokenTypes.DOUBLEQUOTE /*34*/:
                case TokenTypes.DOLLAR /*36*/:
                case TokenTypes.LPAREN /*40*/:
                case TokenTypes.RPAREN /*41*/:
                case TokenTypes.STAR /*42*/:
                case TokenTypes.PLUS /*43*/:
                case TokenTypes.MINUS /*45*/:
                case TokenTypes.DOT /*46*/:
                case TokenTypes.SLASH /*47*/:
                case TokenTypes.COLON /*58*/:
                case TokenTypes.QUESTION /*63*/:
                case TokenTypes.L_SQUARE_BRACKET /*91*/:
                case TokenTypes.R_SQUARE_BRACKET /*93*/:
                case TokenTypes.UNDERSCORE /*95*/:
                case TokenTypes.TILDE /*126*/:
                    isValidChar = true;
                    break;
            }
            if (isValidChar || StringTokenizer.isAlphaDigit(la)) {
                this.lexer.consume(1);
                retval.append(la);
            } else if (la != '%') {
                return retval.toString();
            } else {
                retval.append(escaped());
            }
        }
        return retval.toString();
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected String urlString() throws ParseException {
        StringBuffer retval = new StringBuffer();
        this.lexer.selectLexer("charLexer");
        while (this.lexer.hasMoreChars()) {
            char la = this.lexer.lookAhead(0);
            if (!(la == ' ' || la == '\t' || la == '\n' || la == '>' || la == '<')) {
                this.lexer.consume(0);
                retval.append(la);
            }
        }
        return retval.toString();
    }

    protected String user() throws ParseException {
        if (debug) {
            dbg_enter("user");
        }
        try {
            int startIdx = this.lexer.getPtr();
            while (this.lexer.hasMoreChars()) {
                char la = this.lexer.lookAhead(0);
                if (!isUnreserved(la) && !isUserUnreserved(la)) {
                    if (!isEscaped()) {
                        break;
                    }
                    this.lexer.consume(3);
                } else {
                    this.lexer.consume(1);
                }
            }
            String substring = this.lexer.getBuffer().substring(startIdx, this.lexer.getPtr());
            return substring;
        } finally {
            if (debug) {
                dbg_leave("user");
            }
        }
    }

    protected String password() throws ParseException {
        int startIdx = this.lexer.getPtr();
        while (true) {
            char la = this.lexer.lookAhead(0);
            boolean isValidChar = false;
            switch (la) {
                case TokenTypes.DOLLAR /*36*/:
                case TokenTypes.AND /*38*/:
                case TokenTypes.PLUS /*43*/:
                case ',':
                case TokenTypes.EQUALS /*61*/:
                    isValidChar = true;
                    break;
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
        String[] test = new String[]{"sip:alice@example.com", "sips:alice@examples.com", "sip:3Zqkv5dajqaaas0tCjCxT0xH2ZEuEMsFl0xoasip%3A%2B3519116786244%40siplab.domain.com@213.0.115.163:7070"};
        for (int i = 0; i < test.length; i++) {
            GenericURI uri = new URLParser(test[i]).parse();
            System.out.println("uri type returned " + uri.getClass().getName());
            System.out.println(test[i] + " is SipUri? " + uri.isSipURI() + Separators.GREATER_THAN + uri.encode());
        }
    }
}
