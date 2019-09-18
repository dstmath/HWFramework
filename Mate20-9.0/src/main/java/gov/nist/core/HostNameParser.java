package gov.nist.core;

import java.io.PrintStream;
import java.text.ParseException;
import javax.sip.header.WarningHeader;

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
        String str;
        if (debug) {
            dbg_enter("domainLabel");
        }
        try {
            this.lexer.consumeValidChars(VALID_DOMAIN_LABEL_CHAR);
        } finally {
            if (debug) {
                str = "domainLabel";
                dbg_leave(str);
            }
        }
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0037, code lost:
        if (r1 != ']') goto L_0x004f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0039, code lost:
        r9.lexer.consume(1);
        r0.append(r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x004e, code lost:
        return r0.toString();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0051, code lost:
        if (r1 != '%') goto L_0x00ce;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:?, code lost:
        r9.lexer.consume(1);
        r2 = r9.lexer.getRest();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x005e, code lost:
        if (r2 == null) goto L_0x00ce;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x0064, code lost:
        if (r2.length() == 0) goto L_0x00ce;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0066, code lost:
        r3 = r2.indexOf(93);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x006b, code lost:
        if (r3 == -1) goto L_0x00ce;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x006d, code lost:
        r9.lexer.consume(r3 + 1);
        r0.append("]");
        r4 = r0.toString();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x007f, code lost:
        if (debug == false) goto L_0x0086;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x0081, code lost:
        dbg_leave("ipv6Reference");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x0086, code lost:
        return r4;
     */
    public String ipv6Reference() throws ParseException {
        String str;
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
                    if (!(LexerCore.isHexDigit(la) || la == '.' || la == ':')) {
                        if (la != '[') {
                            if (la == ']') {
                                this.lexer.consume(1);
                                retval.append(la);
                                String stringBuffer = retval.toString();
                                if (debug) {
                                    dbg_leave("ipv6Reference");
                                }
                                return stringBuffer;
                            }
                        }
                    }
                    this.lexer.consume(1);
                    retval.append(la);
                }
            } else {
                while (true) {
                    if (!this.lexer.hasMoreChars()) {
                        break;
                    }
                    char la2 = this.lexer.lookAhead(0);
                    if (!(LexerCore.isHexDigit(la2) || la2 == '.' || la2 == ':')) {
                        if (la2 != '[') {
                            break;
                        }
                    }
                    this.lexer.consume(1);
                    retval.append(la2);
                }
            }
            throw new ParseException(this.lexer.getBuffer() + ": Illegal Host name ", this.lexer.getPtr());
        } finally {
            if (debug) {
                str = "ipv6Reference";
                dbg_leave(str);
            }
        }
    }

    public Host host() throws ParseException {
        String str;
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
                str = "host";
                dbg_leave(str);
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

    /* JADX WARNING: Can't fix incorrect switch cases order */
    public HostPort hostPort(boolean allowWS) throws ParseException {
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
            if (this.lexer.hasMoreChars()) {
                switch (this.lexer.lookAhead(0)) {
                    case 9:
                    case WarningHeader.ATTRIBUTE_NOT_UNDERSTOOD /*10*/:
                    case 13:
                    case ' ':
                    case ',':
                    case '/':
                    case ';':
                    case '>':
                    case '?':
                        break;
                    case '%':
                        if (this.stripAddressScopeZones) {
                            break;
                        }
                    case ':':
                        this.lexer.consume(1);
                        if (allowWS) {
                            this.lexer.SPorHT();
                        }
                        hp.setPort(Integer.parseInt(this.lexer.number()));
                        break;
                }
                if (!allowWS) {
                    throw new ParseException(this.lexer.getBuffer() + " Illegal character in hostname:" + this.lexer.lookAhead(0), this.lexer.getPtr());
                }
            }
            if (debug) {
                dbg_leave("hostPort");
            }
            return hp;
        } catch (NumberFormatException e) {
            throw new ParseException(this.lexer.getBuffer() + " :Error parsing port ", this.lexer.getPtr());
        } catch (Throwable th) {
            if (debug) {
                dbg_leave("hostPort");
            }
            throw th;
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
