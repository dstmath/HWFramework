package gov.nist.core;

import gov.nist.javax.sip.parser.TokenTypes;
import java.text.ParseException;
import javax.sip.header.WarningHeader;
import org.ccil.cowan.tagsoup.HTMLModels;

public class HostNameParser extends ParserCore {
    private static LexerCore Lexer;
    private static final char[] VALID_DOMAIN_LABEL_CHAR = null;
    private boolean stripAddressScopeZones;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: gov.nist.core.HostNameParser.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: gov.nist.core.HostNameParser.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: gov.nist.core.HostNameParser.<clinit>():void");
    }

    public HostNameParser(String hname) {
        this.stripAddressScopeZones = false;
        this.lexer = new LexerCore("charLexer", hname);
        this.stripAddressScopeZones = Boolean.getBoolean("gov.nist.core.STRIP_ADDR_SCOPES");
    }

    public HostNameParser(LexerCore lexer) {
        this.stripAddressScopeZones = false;
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
            if (debug) {
                dbg_leave("domainLabel");
            }
        } catch (Throwable th) {
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
            if (this.lexer.lookAhead(0) == '[') {
                hostname = ipv6Reference();
            } else if (isIPv6Address(this.lexer.getRest())) {
                startPtr = this.lexer.getPtr();
                this.lexer.consumeValidChars(new char[]{'\ufffd', ':'});
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

    /* JADX WARNING: inconsistent code. */
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
                    case TokenTypes.HT /*9*/:
                    case WarningHeader.ATTRIBUTE_NOT_UNDERSTOOD /*10*/:
                    case '\r':
                    case HTMLModels.M_CELL /*32*/:
                    case ',':
                    case TokenTypes.SLASH /*47*/:
                    case TokenTypes.SEMICOLON /*59*/:
                    case TokenTypes.GREATER_THAN /*62*/:
                    case TokenTypes.QUESTION /*63*/:
                        break;
                    case TokenTypes.PERCENT /*37*/:
                        if (this.stripAddressScopeZones) {
                            break;
                        }
                    case TokenTypes.COLON /*58*/:
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
