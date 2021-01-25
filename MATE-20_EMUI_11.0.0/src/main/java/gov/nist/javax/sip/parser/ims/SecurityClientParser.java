package gov.nist.javax.sip.parser.ims;

import gov.nist.javax.sip.header.SIPHeader;
import gov.nist.javax.sip.header.ims.SecurityClient;
import gov.nist.javax.sip.header.ims.SecurityClientList;
import gov.nist.javax.sip.parser.Lexer;
import gov.nist.javax.sip.parser.TokenTypes;
import java.text.ParseException;

public class SecurityClientParser extends SecurityAgreeParser {
    public SecurityClientParser(String security) {
        super(security);
    }

    protected SecurityClientParser(Lexer lexer) {
        super(lexer);
    }

    @Override // gov.nist.javax.sip.parser.HeaderParser
    public SIPHeader parse() throws ParseException {
        dbg_enter("SecuriryClient parse");
        try {
            headerName(TokenTypes.SECURITY_CLIENT);
            return (SecurityClientList) super.parse(new SecurityClient());
        } finally {
            dbg_leave("SecuriryClient parse");
        }
    }
}
