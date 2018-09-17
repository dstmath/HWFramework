package gov.nist.javax.sip.header.ims;

import javax.sip.header.Header;
import javax.sip.header.HeaderAddress;

public interface PPreferredIdentityHeader extends HeaderAddress, Header {
    public static final String NAME = "P-Preferred-Identity";
}
