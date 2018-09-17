package javax.sip.header;

import java.text.ParseException;
import javax.sip.address.URI;

public interface ErrorInfoHeader extends Header, Parameters {
    public static final String NAME = "Error-Info";

    URI getErrorInfo();

    String getErrorMessage();

    void setErrorInfo(URI uri);

    void setErrorMessage(String str) throws ParseException;
}
