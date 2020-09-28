package javax.sip.header;

import javax.sip.address.URI;

public interface AlertInfoHeader extends Header, Parameters {
    public static final String NAME = "Alert-Info";

    URI getAlertInfo();

    void setAlertInfo(String str);

    void setAlertInfo(URI uri);
}
