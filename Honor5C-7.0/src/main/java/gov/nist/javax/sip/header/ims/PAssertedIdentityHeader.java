package gov.nist.javax.sip.header.ims;

import javax.sip.header.Header;
import javax.sip.header.HeaderAddress;

public interface PAssertedIdentityHeader extends HeaderAddress, Header {
    public static final String NAME = "P-Asserted-Identity";
}
