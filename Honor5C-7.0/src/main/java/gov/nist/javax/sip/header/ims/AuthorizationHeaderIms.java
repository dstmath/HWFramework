package gov.nist.javax.sip.header.ims;

import java.text.ParseException;
import javax.sip.InvalidArgumentException;
import javax.sip.header.AuthorizationHeader;

public interface AuthorizationHeaderIms extends AuthorizationHeader {
    public static final String NO = "no";
    public static final String YES = "yes";

    String getIntegrityProtected();

    void setIntegrityProtected(String str) throws InvalidArgumentException, ParseException;
}
