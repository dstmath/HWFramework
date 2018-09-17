package javax.sip.header;

import java.text.ParseException;
import javax.sip.InvalidArgumentException;

public interface ReasonHeader extends Header, Parameters {
    public static final String NAME = "Reason";

    int getCause();

    String getProtocol();

    String getText();

    void setCause(int i) throws InvalidArgumentException;

    void setProtocol(String str) throws ParseException;

    void setText(String str) throws ParseException;
}
