package javax.sip.header;

import java.text.ParseException;

public interface SubjectHeader extends Header {
    public static final String NAME = "Subject";

    String getSubject();

    void setSubject(String str) throws ParseException;
}
