package gov.nist.javax.sip.parser.ims;

import gov.nist.javax.sip.header.SIPHeader;
import gov.nist.javax.sip.header.ims.SecurityServer;
import gov.nist.javax.sip.header.ims.SecurityServerList;
import gov.nist.javax.sip.parser.Lexer;
import gov.nist.javax.sip.parser.TokenTypes;
import java.text.ParseException;

public class SecurityServerParser extends SecurityAgreeParser {
    public SecurityServerParser(String security) {
        super(security);
    }

    protected SecurityServerParser(Lexer lexer) {
        super(lexer);
    }

    public SIPHeader parse() throws ParseException {
        String str;
        dbg_enter("SecuriryServer parse");
        try {
            headerName(TokenTypes.SECURITY_SERVER);
            return (SecurityServerList) super.parse(new SecurityServer());
        } finally {
            str = "SecuriryServer parse";
            dbg_leave(str);
        }
    }
}
