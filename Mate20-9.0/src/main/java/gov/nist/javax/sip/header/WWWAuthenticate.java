package gov.nist.javax.sip.header;

import gov.nist.javax.sip.header.ims.WWWAuthenticateHeaderIms;
import javax.sip.address.URI;
import javax.sip.header.WWWAuthenticateHeader;

public class WWWAuthenticate extends AuthenticationHeader implements WWWAuthenticateHeader, WWWAuthenticateHeaderIms {
    private static final long serialVersionUID = 115378648697363486L;

    public WWWAuthenticate() {
        super("WWW-Authenticate");
    }

    public URI getURI() {
        return null;
    }

    public void setURI(URI uri) {
    }
}
