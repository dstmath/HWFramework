package gov.nist.javax.sip.header.extensions;

import java.text.ParseException;
import javax.sip.header.Header;
import javax.sip.header.Parameters;

public interface JoinHeader extends Parameters, Header {
    public static final String NAME = "Join";

    String getCallId();

    String getFromTag();

    String getToTag();

    void setCallId(String str) throws ParseException;

    void setFromTag(String str) throws ParseException;

    void setToTag(String str) throws ParseException;
}
