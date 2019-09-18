package gov.nist.javax.sip.parser;

import gov.nist.javax.sip.header.Organization;
import gov.nist.javax.sip.header.SIPHeader;
import java.text.ParseException;

public class OrganizationParser extends HeaderParser {
    public OrganizationParser(String organization) {
        super(organization);
    }

    protected OrganizationParser(Lexer lexer) {
        super(lexer);
    }

    public SIPHeader parse() throws ParseException {
        String str;
        if (debug) {
            dbg_enter("OrganizationParser.parse");
        }
        Organization organization = new Organization();
        try {
            headerName(TokenTypes.ORGANIZATION);
            organization.setHeaderName("Organization");
            this.lexer.SPorHT();
            organization.setOrganization(this.lexer.getRest().trim());
            return organization;
        } finally {
            if (debug) {
                str = "OrganizationParser.parse";
                dbg_leave(str);
            }
        }
    }
}
