package gov.nist.javax.sip.parser;

import gov.nist.javax.sip.header.Authorization;
import gov.nist.javax.sip.header.SIPHeader;
import java.text.ParseException;

public class AuthorizationParser extends ChallengeParser {
    public AuthorizationParser(String authorization) {
        super(authorization);
    }

    protected AuthorizationParser(Lexer lexer) {
        super(lexer);
    }

    public SIPHeader parse() throws ParseException {
        dbg_enter("parse");
        try {
            headerName(TokenTypes.AUTHORIZATION);
            Authorization auth = new Authorization();
            super.parse(auth);
            return auth;
        } finally {
            dbg_leave("parse");
        }
    }
}
