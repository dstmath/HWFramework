package javax.sip.header;

import javax.sip.InvalidArgumentException;

public interface AcceptHeader extends Header, MediaType, Parameters {
    public static final String NAME = "Accept";

    boolean allowsAllContentSubTypes();

    boolean allowsAllContentTypes();

    float getQValue();

    boolean hasQValue();

    void removeQValue();

    void setQValue(float f) throws InvalidArgumentException;
}
