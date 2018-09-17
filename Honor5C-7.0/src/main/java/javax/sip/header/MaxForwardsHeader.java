package javax.sip.header;

import javax.sip.InvalidArgumentException;

public interface MaxForwardsHeader extends Header {
    public static final String NAME = "Max-Forwards";

    void decrementMaxForwards() throws TooManyHopsException;

    int getMaxForwards();

    boolean hasReachedZero();

    void setMaxForwards(int i) throws InvalidArgumentException;
}
