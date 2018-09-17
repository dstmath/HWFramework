package gov.nist.javax.sip.header.extensions;

import javax.sip.header.Header;
import javax.sip.header.HeaderAddress;
import javax.sip.header.Parameters;

public interface ReferredByHeader extends Header, HeaderAddress, Parameters {
    public static final String NAME = "Referred-By";
}
