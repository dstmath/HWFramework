package gov.nist.core;

import java.text.ParseException;
import javax.sip.header.WarningHeader;

public class HostNameParser extends ParserCore {
    private static LexerCore Lexer;
    private static final char[] VALID_DOMAIN_LABEL_CHAR = new char[]{65533, '-', '.'};
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

    protected void consumeDomainLabel() throws ParseException {
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

    protected String ipv6Reference() throws ParseException {
        StringBuffer retval = new StringBuffer();
        if (debug) {
            dbg_enter("ipv6Reference");
        }
        try {
            char la;
            String stringBuffer;
            if (this.stripAddressScopeZones) {
                while (this.lexer.hasMoreChars()) {
                    la = this.lexer.lookAhead(0);
                    if (StringTokenizer.isHexDigit(la) || la == '.' || la == ':' || la == '[') {
                        this.lexer.consume(1);
                        retval.append(la);
                    } else if (la == ']') {
                        this.lexer.consume(1);
                        retval.append(la);
                        stringBuffer = retval.toString();
                        return stringBuffer;
                    } else if (la == '%') {
                        this.lexer.consume(1);
                        String rest = this.lexer.getRest();
                        if (!(rest == null || rest.length() == 0)) {
                            int stripLen = rest.indexOf(93);
                            if (stripLen != -1) {
                                this.lexer.consume(stripLen + 1);
                                retval.append("]");
                                stringBuffer = retval.toString();
                                if (debug) {
                                    dbg_leave("ipv6Reference");
                                }
                                return stringBuffer;
                            }
                        }
                    }
                }
            }
            while (this.lexer.hasMoreChars()) {
                la = this.lexer.lookAhead(0);
                if (StringTokenizer.isHexDigit(la) || la == '.' || la == ':' || la == '[') {
                    this.lexer.consume(1);
                    retval.append(la);
                } else if (la == ']') {
                    this.lexer.consume(1);
                    retval.append(la);
                    stringBuffer = retval.toString();
                    if (debug) {
                        dbg_leave("ipv6Reference");
                    }
                    return stringBuffer;
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
        if (debug) {
            dbg_enter("host");
        }
        try {
            String hostname;
            int startPtr;
            if (this.lexer.lookAhead(0) == '[') {
                hostname = ipv6Reference();
            } else if (isIPv6Address(this.lexer.getRest())) {
                startPtr = this.lexer.getPtr();
                this.lexer.consumeValidChars(new char[]{65533, ':'});
                hostname = new StringBuffer("[").append(this.lexer.getBuffer().substring(startPtr, this.lexer.getPtr())).append("]").toString();
            } else {
                startPtr = this.lexer.getPtr();
                consumeDomainLabel();
                hostname = this.lexer.getBuffer().substring(startPtr, this.lexer.getPtr());
            }
            if (hostname.length() == 0) {
                throw new ParseException(this.lexer.getBuffer() + ": Missing host name", this.lexer.getPtr());
            }
            Host host = new Host(hostname);
            return host;
        } finally {
            if (debug) {
                dbg_leave("host");
            }
        }
    }

    private boolean isIPv6Address(String uriHeader) {
        int hostEnd = uriHeader.indexOf(63);
        int semiColonIndex = uriHeader.indexOf(59);
        if (hostEnd == -1 || (semiColonIndex != -1 && hostEnd > semiColonIndex)) {
            hostEnd = semiColonIndex;
        }
        if (hostEnd == -1) {
            hostEnd = uriHeader.length();
        }
        String host = uriHeader.substring(0, hostEnd);
        int firstColonIndex = host.indexOf(58);
        if (firstColonIndex == -1 || host.indexOf(58, firstColonIndex + 1) == -1) {
            return false;
        }
        return true;
    }

    /* JADX WARNING: Missing block: B:34:0x00bc, code:
            if (r9.stripAddressScopeZones != false) goto L_0x0088;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
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
                        break;
                    case ':':
                        this.lexer.consume(1);
                        if (allowWS) {
                            this.lexer.SPorHT();
                        }
                        hp.setPort(Integer.parseInt(this.lexer.number()));
                        break;
                    default:
                        if (!allowWS) {
                            throw new ParseException(this.lexer.getBuffer() + " Illegal character in hostname:" + this.lexer.lookAhead(0), this.lexer.getPtr());
                        }
                        break;
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
        }
    }

    public static void main(String[] args) throws ParseException {
        String[] hostNames = new String[]{"foo.bar.com:1234", "proxima.chaplin.bt.co.uk", "129.6.55.181:2345", ":1234", "foo.bar.com:         1234", "foo.bar.com     :      1234   ", "MIK_S:1234"};
        for (String hostNameParser : hostNames) {
            try {
                System.out.println("[" + new HostNameParser(hostNameParser).hostPort(true).encode() + "]");
            } catch (ParseException ex) {
                System.out.println("exception text = " + ex.getMessage());
            }
        }
    }
}
