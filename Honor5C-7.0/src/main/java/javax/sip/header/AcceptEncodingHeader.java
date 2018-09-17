package javax.sip.header;

import javax.sip.InvalidArgumentException;

public interface AcceptEncodingHeader extends Encoding, Header, Parameters {
    public static final String NAME = "Accept-Encoding";

    float getQValue();

    void setQValue(float f) throws InvalidArgumentException;
}
