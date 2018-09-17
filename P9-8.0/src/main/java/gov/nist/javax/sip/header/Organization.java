package gov.nist.javax.sip.header;

import java.text.ParseException;
import javax.sip.header.OrganizationHeader;

public class Organization extends SIPHeader implements OrganizationHeader {
    private static final long serialVersionUID = -2775003113740192712L;
    protected String organization;

    public String encodeBody() {
        return this.organization;
    }

    public Organization() {
        super("Organization");
    }

    public String getOrganization() {
        return this.organization;
    }

    public void setOrganization(String o) throws ParseException {
        if (o == null) {
            throw new NullPointerException("JAIN-SIP Exception, Organization, setOrganization(), the organization parameter is null");
        }
        this.organization = o;
    }
}
