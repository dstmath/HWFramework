package gov.nist.javax.sip.parser;

import gov.nist.javax.sip.header.ProxyAuthenticate;
import gov.nist.javax.sip.header.SIPHeader;
import java.text.ParseException;

public class ProxyAuthenticateParser extends ChallengeParser {
    public ProxyAuthenticateParser(String proxyAuthenticate) {
        super(proxyAuthenticate);
    }

    protected ProxyAuthenticateParser(Lexer lexer) {
        super(lexer);
    }

    public SIPHeader parse() throws ParseException {
        headerName(TokenTypes.PROXY_AUTHENTICATE);
        ProxyAuthenticate proxyAuthenticate = new ProxyAuthenticate();
        super.parse(proxyAuthenticate);
        return proxyAuthenticate;
    }
}
