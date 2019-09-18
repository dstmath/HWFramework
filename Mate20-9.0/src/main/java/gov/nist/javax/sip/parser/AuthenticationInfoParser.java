package gov.nist.javax.sip.parser;

import gov.nist.javax.sip.header.AuthenticationInfo;
import gov.nist.javax.sip.header.SIPHeader;
import java.text.ParseException;

public class AuthenticationInfoParser extends ParametersParser {
    public AuthenticationInfoParser(String authenticationInfo) {
        super(authenticationInfo);
    }

    protected AuthenticationInfoParser(Lexer lexer) {
        super(lexer);
    }

    public SIPHeader parse() throws ParseException {
        String str;
        if (debug) {
            dbg_enter("AuthenticationInfoParser.parse");
        }
        try {
            headerName(TokenTypes.AUTHENTICATION_INFO);
            AuthenticationInfo authenticationInfo = new AuthenticationInfo();
            authenticationInfo.setHeaderName("Authentication-Info");
            this.lexer.SPorHT();
            authenticationInfo.setParameter(super.nameValue());
            this.lexer.SPorHT();
            while (this.lexer.lookAhead(0) == ',') {
                this.lexer.match(44);
                this.lexer.SPorHT();
                authenticationInfo.setParameter(super.nameValue());
                this.lexer.SPorHT();
            }
            this.lexer.SPorHT();
            return authenticationInfo;
        } finally {
            if (debug) {
                str = "AuthenticationInfoParser.parse";
                dbg_leave(str);
            }
        }
    }
}
