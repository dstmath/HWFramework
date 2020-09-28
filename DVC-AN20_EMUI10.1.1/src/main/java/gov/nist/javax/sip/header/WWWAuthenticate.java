package gov.nist.javax.sip.header;

import gov.nist.javax.sip.header.ims.WWWAuthenticateHeaderIms;
import javax.sip.address.URI;
import javax.sip.header.WWWAuthenticateHeader;

public class WWWAuthenticate extends AuthenticationHeader implements WWWAuthenticateHeader, WWWAuthenticateHeaderIms {
    private static final long serialVersionUID = 115378648697363486L;

    public WWWAuthenticate() {
        super("WWW-Authenticate");
    }

    @Override // javax.sip.header.WWWAuthenticateHeader, javax.sip.header.AuthorizationHeader, gov.nist.javax.sip.header.AuthenticationHeader
    public URI getURI() {
        return null;
    }

    @Override // javax.sip.header.WWWAuthenticateHeader, javax.sip.header.AuthorizationHeader, gov.nist.javax.sip.header.AuthenticationHeader
    public void setURI(URI uri) {
    }
}
