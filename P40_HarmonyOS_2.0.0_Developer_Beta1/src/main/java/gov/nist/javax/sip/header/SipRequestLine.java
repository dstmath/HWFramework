package gov.nist.javax.sip.header;

import javax.sip.address.URI;

public interface SipRequestLine {
    String getMethod();

    String getSipVersion();

    URI getUri();

    String getVersionMajor();

    String getVersionMinor();

    void setMethod(String str);

    void setSipVersion(String str);

    void setUri(URI uri);
}
