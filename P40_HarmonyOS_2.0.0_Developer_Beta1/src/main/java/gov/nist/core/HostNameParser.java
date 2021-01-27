package gov.nist.core;

import java.io.PrintStream;
import java.text.ParseException;

public class HostNameParser extends ParserCore {
    private static LexerCore Lexer;
    private static final char[] VALID_DOMAIN_LABEL_CHAR = {65533, '-', '.'};
    private boolean stripAddressScopeZones = false;

    public HostNameParser(String hname) {
        this.lexer = new LexerCore("charLexer", hname);
        this.stripAddressScopeZones = Boolean.getBoolean("gov.nist.core.STRIP_ADDR_SCOPES");
    }

    public HostNameParser(LexerCore lexer) {
        this.lexer = lexer;
        lexer.selectLexer("charLexer");
        this.stripAddressScopeZones = Boolean.getBoolean("gov.nist.core.STRIP_ADDR_SCOPES");
    }

    /* access modifiers changed from: protected */
    public void consumeDomainLabel() throws ParseException {
        if (debug) {
            dbg_enter("domainLabel");
        }
        try {
            this.lexer.consumeValidChars(VALID_DOMAIN_LABEL_CHAR);
        } finally {
            if (debug) {
                dbg_leave("domainLabel");
            }
        }
    }

    /* access modifiers changed from: protected */
    public String ipv6Reference() throws ParseException {
        int stripLen;
        StringBuffer retval = new StringBuffer();
        if (debug) {
            dbg_enter("ipv6Reference");
        }
        try {
            if (!this.stripAddressScopeZones) {
                while (true) {
                    if (!this.lexer.hasMoreChars()) {
                        break;
                    }
                    char la = this.lexer.lookAhead(0);
                    if (LexerCore.isHexDigit(la) || la == '.' || la == ':' || la == '[') {
                        this.lexer.consume(1);
                        retval.append(la);
                    } else if (la == ']') {
                        this.lexer.consume(1);
                        retval.append(la);
                        String stringBuffer = retval.toString();
                        if (debug) {
                            dbg_leave("ipv6Reference");
                        }
                        return stringBuffer;
                    }
                }
            } else {
                while (true) {
                    if (!this.lexer.hasMoreChars()) {
                        break;
                    }
                    char la2 = this.lexer.lookAhead(0);
                    if (!(LexerCore.isHexDigit(la2) || la2 == '.' || la2 == ':')) {
                        if (la2 != '[') {
                            if (la2 == ']') {
                                this.lexer.consume(1);
                                retval.append(la2);
                                return retval.toString();
                            } else if (la2 == '%') {
                                this.lexer.consume(1);
                                String rest = this.lexer.getRest();
                                if (rest != null && rest.length() != 0 && (stripLen = rest.indexOf(93)) != -1) {
                                    this.lexer.consume(stripLen + 1);
                                    retval.append("]");
                                    String stringBuffer2 = retval.toString();
                                    if (debug) {
                                        dbg_leave("ipv6Reference");
                                    }
                                    return stringBuffer2;
                                }
                            }
                        }
                    }
                    this.lexer.consume(1);
                    retval.append(la2);
                }
            }
            throw new ParseException(this.lexer.getBuffer() + ": Illegal Host name ", this.lexer.getPtr());
        } finally {
            if (debug) {
                dbg_leave("ipv6Reference");
            }
        }
    }

    public Host host() throws ParseException {
        String hostname;
        if (debug) {
            dbg_enter("host");
        }
        try {
            if (this.lexer.lookAhead(0) == '[') {
                hostname = ipv6Reference();
            } else if (isIPv6Address(this.lexer.getRest())) {
                int startPtr = this.lexer.getPtr();
                this.lexer.consumeValidChars(new char[]{65533, ':'});
                StringBuffer stringBuffer = new StringBuffer("[");
                stringBuffer.append(this.lexer.getBuffer().substring(startPtr, this.lexer.getPtr()));
                stringBuffer.append("]");
                hostname = stringBuffer.toString();
            } else {
                int startPtr2 = this.lexer.getPtr();
                consumeDomainLabel();
                hostname = this.lexer.getBuffer().substring(startPtr2, this.lexer.getPtr());
            }
            if (hostname.length() != 0) {
                return new Host(hostname);
            }
            throw new ParseException(this.lexer.getBuffer() + ": Missing host name", this.lexer.getPtr());
        } finally {
            if (debug) {
                dbg_leave("host");
            }
        }
    }

    private boolean isIPv6Address(String uriHeader) {
        LexerCore lexerCore = Lexer;
        int hostEnd = uriHeader.indexOf(63);
        LexerCore lexerCore2 = Lexer;
        int semiColonIndex = uriHeader.indexOf(59);
        if (hostEnd == -1 || (semiColonIndex != -1 && hostEnd > semiColonIndex)) {
            hostEnd = semiColonIndex;
        }
        if (hostEnd == -1) {
            hostEnd = uriHeader.length();
        }
        String host = uriHeader.substring(0, hostEnd);
        LexerCore lexerCore3 = Lexer;
        int firstColonIndex = host.indexOf(58);
        if (firstColonIndex == -1) {
            return false;
        }
        LexerCore lexerCore4 = Lexer;
        if (host.indexOf(58, firstColonIndex + 1) == -1) {
            return false;
        }
        return true;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:41:0x0099, code lost:
        if (r8.stripAddressScopeZones != false) goto L_0x00cb;
     */
    public HostPort hostPort(boolean allowWS) throws ParseException {
        char la;
        if (debug) {
            dbg_enter("hostPort");
        }
        try {
            Host host = host();
            HostPort hp = new HostPort();
            hp.setHost(host);
            if (allowWS) {
                this.lexer.SPorHT();
            }
            if (!(!this.lexer.hasMoreChars() || (la = this.lexer.lookAhead(0)) == '\t' || la == '\n' || la == '\r' || la == ' ')) {
                if (la != '%') {
                    if (!(la == ',' || la == '/')) {
                        if (la == ':') {
                            this.lexer.consume(1);
                            if (allowWS) {
                                this.lexer.SPorHT();
                            }
                            try {
                                hp.setPort(Integer.parseInt(this.lexer.number()));
                            } catch (NumberFormatException e) {
                                throw new ParseException(this.lexer.getBuffer() + " :Error parsing port ", this.lexer.getPtr());
                            }
                        } else if (!(la == ';' || la == '>' || la == '?')) {
                        }
                    }
                }
                if (!allowWS) {
                    throw new ParseException(this.lexer.getBuffer() + " Illegal character in hostname:" + this.lexer.lookAhead(0), this.lexer.getPtr());
                }
            }
            return hp;
        } finally {
            if (debug) {
                dbg_leave("hostPort");
            }
        }
    }

    public static void main(String[] args) throws ParseException {
        String[] hostNames = {"foo.bar.com:1234", "proxima.chaplin.bt.co.uk", "129.6.55.181:2345", ":1234", "foo.bar.com:         1234", "foo.bar.com     :      1234   ", "MIK_S:1234"};
        for (int i = 0; i < hostNames.length; i++) {
            try {
                HostPort hp = new HostNameParser(hostNames[i]).hostPort(true);
                PrintStream printStream = System.out;
                printStream.println("[" + hp.encode() + "]");
            } catch (ParseException ex) {
                PrintStream printStream2 = System.out;
                printStream2.println("exception text = " + ex.getMessage());
            }
        }
    }
}
