package javax.sip.header;

import javax.sip.InvalidArgumentException;

public interface ContactHeader extends HeaderAddress, Header, Parameters {
    public static final String NAME = "Contact";

    int getExpires();

    float getQValue();

    boolean isWildCard();

    void setExpires(int i) throws InvalidArgumentException;

    void setQValue(float f) throws InvalidArgumentException;

    void setWildCard();

    void setWildCardFlag(boolean z);
}
