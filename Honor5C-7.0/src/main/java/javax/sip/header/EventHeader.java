package javax.sip.header;

import java.text.ParseException;

public interface EventHeader extends Header, Parameters {
    public static final String NAME = "Event";

    String getEventId();

    String getEventType();

    void setEventId(String str) throws ParseException;

    void setEventType(String str) throws ParseException;
}
