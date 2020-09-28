package gov.nist.javax.sip.header;

import javax.sip.address.URI;
import javax.sip.header.ProxyAuthenticateHeader;

public class ProxyAuthenticate extends AuthenticationHeader implements ProxyAuthenticateHeader {
    private static final long serialVersionUID = 3826145955463251116L;

    public ProxyAuthenticate() {
        super("Proxy-Authenticate");
    }

    @Override // javax.sip.header.WWWAuthenticateHeader, javax.sip.header.AuthorizationHeader, gov.nist.javax.sip.header.AuthenticationHeader
    public URI getURI() {
        return null;
    }

    @Override // javax.sip.header.WWWAuthenticateHeader, javax.sip.header.AuthorizationHeader, gov.nist.javax.sip.header.AuthenticationHeader
    public void setURI(URI uri) {
    }
}
