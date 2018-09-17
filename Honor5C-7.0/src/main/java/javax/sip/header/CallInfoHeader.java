package javax.sip.header;

import javax.sip.address.URI;

public interface CallInfoHeader extends Header, Parameters {
    public static final String NAME = "Call-Info";

    URI getInfo();

    String getPurpose();

    void setInfo(URI uri);

    void setPurpose(String str);
}
