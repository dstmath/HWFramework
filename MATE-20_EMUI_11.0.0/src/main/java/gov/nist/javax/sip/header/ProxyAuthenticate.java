package gov.nist.javax.sip.header;

import javax.sip.address.URI;
import javax.sip.header.ProxyAuthenticateHeader;

public class ProxyAuthenticate extends AuthenticationHeader implements ProxyAuthenticateHeader {
    private static final long serialVersionUID = 3826145955463251116L;

    public ProxyAuthenticate() {
        super("Proxy-Authenticate");
    }

    @Override // gov.nist.javax.sip.header.AuthenticationHeader, javax.sip.header.WWWAuthenticateHeader, javax.sip.header.AuthorizationHeader
    public URI getURI() {
        return null;
    }

    @Override // gov.nist.javax.sip.header.AuthenticationHeader, javax.sip.header.WWWAuthenticateHeader, javax.sip.header.AuthorizationHeader
    public void setURI(URI uri) {
    }
}
