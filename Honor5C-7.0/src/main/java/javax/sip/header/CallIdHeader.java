package javax.sip.header;

import java.text.ParseException;

public interface CallIdHeader extends Header {
    public static final String NAME = "Call-ID";

    String getCallId();

    void setCallId(String str) throws ParseException;
}
