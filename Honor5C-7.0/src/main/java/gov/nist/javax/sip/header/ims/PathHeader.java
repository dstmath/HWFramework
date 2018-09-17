package gov.nist.javax.sip.header.ims;

import javax.sip.header.Header;
import javax.sip.header.HeaderAddress;
import javax.sip.header.Parameters;

public interface PathHeader extends HeaderAddress, Parameters, Header {
    public static final String NAME = "Path";
}
