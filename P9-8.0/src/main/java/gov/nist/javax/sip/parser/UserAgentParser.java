package gov.nist.javax.sip.parser;

import gov.nist.core.Separators;
import gov.nist.javax.sip.header.SIPHeader;
import gov.nist.javax.sip.header.UserAgent;
import java.text.ParseException;

public class UserAgentParser extends HeaderParser {
    public UserAgentParser(String userAgent) {
        super(userAgent);
    }

    protected UserAgentParser(Lexer lexer) {
        super(lexer);
    }

    public SIPHeader parse() throws ParseException {
        if (debug) {
            dbg_enter("UserAgentParser.parse");
        }
        UserAgent userAgent = new UserAgent();
        try {
            headerName(TokenTypes.USER_AGENT);
            if (this.lexer.lookAhead(0) == 10) {
                throw createParseException("empty header");
            }
            while (this.lexer.lookAhead(0) != 10 && this.lexer.lookAhead(0) != 0) {
                if (this.lexer.lookAhead(0) == '(') {
                    userAgent.addProductToken('(' + this.lexer.comment() + ')');
                } else {
                    getLexer().SPorHT();
                    String product = this.lexer.byteStringNoSlash();
                    if (product == null) {
                        throw createParseException("Expected product string");
                    }
                    StringBuffer productSb = new StringBuffer(product);
                    if (this.lexer.peekNextToken().getTokenType() == 47) {
                        this.lexer.match(47);
                        getLexer().SPorHT();
                        String productVersion = this.lexer.byteStringNoSlash();
                        if (productVersion == null) {
                            throw createParseException("Expected product version");
                        }
                        productSb.append(Separators.SLASH);
                        productSb.append(productVersion);
                    }
                    userAgent.addProductToken(productSb.toString());
                }
                this.lexer.SPorHT();
            }
            if (debug) {
                dbg_leave("UserAgentParser.parse");
            }
            return userAgent;
        } catch (Throwable th) {
            if (debug) {
                dbg_leave("UserAgentParser.parse");
            }
        }
    }

    public static void main(String[] args) throws ParseException {
        String[] userAgent = new String[]{"User-Agent: Softphone/Beta1.5 \n", "User-Agent:Nist/Beta1 (beta version) \n", "User-Agent: Nist UA (beta version)\n", "User-Agent: Nist1.0/Beta2 Ubi/vers.1.0 (very cool) \n", "User-Agent: SJphone/1.60.299a/L (SJ Labs)\n", "User-Agent: sipXecs/3.5.11 sipXecs/sipxbridge (Linux)\n"};
        for (String userAgentParser : userAgent) {
            System.out.println("encoded = " + ((UserAgent) new UserAgentParser(userAgentParser).parse()).encode());
        }
    }
}
