package gov.nist.javax.sip.address;

import javax.sip.address.SipURI;

public interface SipURIExt extends SipURI {
    boolean hasGrParam();

    void removeHeader(String str);

    void removeHeaders();

    void setGrParam(String str);
}
