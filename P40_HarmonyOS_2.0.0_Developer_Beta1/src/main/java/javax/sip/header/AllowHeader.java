package javax.sip.header;

import java.text.ParseException;

public interface AllowHeader extends Header {
    public static final String NAME = "Allow";

    String getMethod();

    void setMethod(String str) throws ParseException;
}
