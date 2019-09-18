package gov.nist.javax.sip.header.ims;

import javax.sip.address.URI;
import javax.sip.header.Header;
import javax.sip.header.HeaderAddress;
import javax.sip.header.Parameters;

public interface PAssociatedURIHeader extends HeaderAddress, Parameters, Header {
    public static final String NAME = "P-Associated-URI";

    URI getAssociatedURI();

    void setAssociatedURI(URI uri) throws NullPointerException;
}
