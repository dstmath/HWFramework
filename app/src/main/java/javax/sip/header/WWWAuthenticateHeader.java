package javax.sip.header;

import javax.sip.address.URI;

public interface WWWAuthenticateHeader extends AuthorizationHeader {
    public static final String NAME = "WWW-Authenticate";

    URI getURI();

    void setURI(URI uri);
}
