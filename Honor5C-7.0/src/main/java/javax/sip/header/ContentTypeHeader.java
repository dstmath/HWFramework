package javax.sip.header;

import java.text.ParseException;

public interface ContentTypeHeader extends Header, MediaType, Parameters {
    public static final String NAME = "Content-Type";

    String getCharset();

    void setContentType(String str, String str2) throws ParseException;
}
