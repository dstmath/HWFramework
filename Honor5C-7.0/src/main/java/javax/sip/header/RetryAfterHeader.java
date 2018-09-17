package javax.sip.header;

import java.text.ParseException;
import javax.sip.InvalidArgumentException;

public interface RetryAfterHeader extends Header, Parameters {
    public static final String NAME = "Retry-After";

    String getComment();

    int getDuration();

    int getRetryAfter();

    boolean hasComment();

    void removeComment();

    void removeDuration();

    void setComment(String str) throws ParseException;

    void setDuration(int i) throws InvalidArgumentException;

    void setRetryAfter(int i) throws InvalidArgumentException;
}
