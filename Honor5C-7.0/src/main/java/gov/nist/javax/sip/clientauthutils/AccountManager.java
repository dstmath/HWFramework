package gov.nist.javax.sip.clientauthutils;

import javax.sip.ClientTransaction;

public interface AccountManager {
    UserCredentials getCredentials(ClientTransaction clientTransaction, String str);
}
