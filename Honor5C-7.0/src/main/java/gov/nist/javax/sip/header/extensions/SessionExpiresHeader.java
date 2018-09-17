package gov.nist.javax.sip.header.extensions;

import javax.sip.InvalidArgumentException;
import javax.sip.header.ExtensionHeader;
import javax.sip.header.Header;
import javax.sip.header.Parameters;

public interface SessionExpiresHeader extends Parameters, Header, ExtensionHeader {
    public static final String NAME = "Session-Expires";

    int getExpires();

    String getRefresher();

    void setExpires(int i) throws InvalidArgumentException;

    void setRefresher(String str);
}
