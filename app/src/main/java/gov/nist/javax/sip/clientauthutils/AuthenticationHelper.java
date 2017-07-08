package gov.nist.javax.sip.clientauthutils;

import javax.sip.ClientTransaction;
import javax.sip.SipException;
import javax.sip.SipProvider;
import javax.sip.message.Request;
import javax.sip.message.Response;

public interface AuthenticationHelper {
    ClientTransaction handleChallenge(Response response, ClientTransaction clientTransaction, SipProvider sipProvider, int i) throws SipException, NullPointerException;

    void removeCachedAuthenticationHeaders(String str);

    void setAuthenticationHeaders(Request request);
}
