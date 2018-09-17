package gov.nist.javax.sip.header.ims;

import javax.sip.InvalidArgumentException;
import javax.sip.header.Header;

public interface PMediaAuthorizationHeader extends Header {
    public static final String NAME = "P-Media-Authorization";

    String getToken();

    void setMediaAuthorizationToken(String str) throws InvalidArgumentException;
}
