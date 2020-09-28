package gov.nist.javax.sip.parser.ims;

import gov.nist.javax.sip.header.SIPHeader;
import gov.nist.javax.sip.header.ims.SecurityVerify;
import gov.nist.javax.sip.header.ims.SecurityVerifyList;
import gov.nist.javax.sip.parser.Lexer;
import gov.nist.javax.sip.parser.TokenTypes;
import java.text.ParseException;

public class SecurityVerifyParser extends SecurityAgreeParser {
    public SecurityVerifyParser(String security) {
        super(security);
    }

    protected SecurityVerifyParser(Lexer lexer) {
        super(lexer);
    }

    @Override // gov.nist.javax.sip.parser.HeaderParser
    public SIPHeader parse() throws ParseException {
        dbg_enter("SecuriryVerify parse");
        try {
            headerName(TokenTypes.SECURITY_VERIFY);
            return (SecurityVerifyList) super.parse(new SecurityVerify());
        } finally {
            dbg_leave("SecuriryVerify parse");
        }
    }
}
