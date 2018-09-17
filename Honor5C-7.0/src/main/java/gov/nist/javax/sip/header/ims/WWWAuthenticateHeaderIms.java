package gov.nist.javax.sip.header.ims;

import java.text.ParseException;
import javax.sip.header.WWWAuthenticateHeader;

public interface WWWAuthenticateHeaderIms extends WWWAuthenticateHeader {
    public static final String CK = "ck";
    public static final String IK = "ik";

    String getCK();

    String getIK();

    void setCK(String str) throws ParseException;

    void setIK(String str) throws ParseException;
}
