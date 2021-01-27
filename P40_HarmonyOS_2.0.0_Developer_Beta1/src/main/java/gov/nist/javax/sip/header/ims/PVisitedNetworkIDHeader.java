package gov.nist.javax.sip.header.ims;

import gov.nist.core.Token;
import javax.sip.header.Header;
import javax.sip.header.Parameters;

public interface PVisitedNetworkIDHeader extends Parameters, Header {
    public static final String NAME = "P-Visited-Network-ID";

    String getVisitedNetworkID();

    void setVisitedNetworkID(Token token);

    void setVisitedNetworkID(String str);
}
