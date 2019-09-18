package gov.nist.javax.sip.parser;

import gov.nist.core.Separators;
import gov.nist.javax.sip.header.SIPHeader;
import gov.nist.javax.sip.header.UserAgent;
import java.io.PrintStream;
import java.text.ParseException;

public class UserAgentParser extends HeaderParser {
    public UserAgentParser(String userAgent) {
        super(userAgent);
    }

    protected UserAgentParser(Lexer lexer) {
        super(lexer);
    }

    public SIPHeader parse() throws ParseException {
        String str;
        if (debug) {
            dbg_enter("UserAgentParser.parse");
        }
        UserAgent userAgent = new UserAgent();
        try {
            headerName(TokenTypes.USER_AGENT);
            if (this.lexer.lookAhead(0) != 10) {
                while (this.lexer.lookAhead(0) != 10 && this.lexer.lookAhead(0) != 0) {
                    if (this.lexer.lookAhead(0) == '(') {
                        String comment = this.lexer.comment();
                        userAgent.addProductToken('(' + comment + ')');
                    } else {
                        getLexer().SPorHT();
                        String product = this.lexer.byteStringNoSlash();
                        if (product != null) {
                            StringBuffer productSb = new StringBuffer(product);
                            if (this.lexer.peekNextToken().getTokenType() == 47) {
                                this.lexer.match(47);
                                getLexer().SPorHT();
                                String productVersion = this.lexer.byteStringNoSlash();
                                if (productVersion != null) {
                                    productSb.append(Separators.SLASH);
                                    productSb.append(productVersion);
                                } else {
                                    throw createParseException("Expected product version");
                                }
                            }
                            userAgent.addProductToken(productSb.toString());
                        } else {
                            throw createParseException("Expected product string");
                        }
                    }
                    this.lexer.SPorHT();
                }
                return userAgent;
            }
            throw createParseException("empty header");
        } finally {
            if (debug) {
                str = "UserAgentParser.parse";
                dbg_leave(str);
            }
        }
    }

    public static void main(String[] args) throws ParseException {
        String[] userAgent = {"User-Agent: Softphone/Beta1.5 \n", "User-Agent:Nist/Beta1 (beta version) \n", "User-Agent: Nist UA (beta version)\n", "User-Agent: Nist1.0/Beta2 Ubi/vers.1.0 (very cool) \n", "User-Agent: SJphone/1.60.299a/L (SJ Labs)\n", "User-Agent: sipXecs/3.5.11 sipXecs/sipxbridge (Linux)\n"};
        for (int i = 0; i < userAgent.length; i++) {
            PrintStream printStream = System.out;
            printStream.println("encoded = " + ((UserAgent) new UserAgentParser(userAgent[i]).parse()).encode());
        }
    }
}
