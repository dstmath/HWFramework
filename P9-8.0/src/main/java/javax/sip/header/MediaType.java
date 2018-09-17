package javax.sip.header;

import java.text.ParseException;

public interface MediaType {
    String getContentSubType();

    String getContentType();

    void setContentSubType(String str) throws ParseException;

    void setContentType(String str) throws ParseException;
}
