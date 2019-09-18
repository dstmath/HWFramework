package gov.nist.javax.sip.header.ims;

import javax.sip.header.Header;

public interface PAssertedServiceHeader extends Header {
    public static final String NAME = "P-Asserted-Service";

    String getApplicationIdentifiers();

    String getSubserviceIdentifiers();

    void setApplicationIdentifiers(String str);

    void setSubserviceIdentifiers(String str);
}
