package javax.sip.header;

import java.text.ParseException;

public interface AuthenticationInfoHeader extends Header, Parameters {
    public static final String NAME = "Authentication-Info";

    String getCNonce();

    String getNextNonce();

    int getNonceCount();

    String getQop();

    String getResponse();

    void setCNonce(String str) throws ParseException;

    void setNextNonce(String str) throws ParseException;

    void setNonceCount(int i) throws ParseException;

    void setQop(String str) throws ParseException;

    void setResponse(String str) throws ParseException;
}
