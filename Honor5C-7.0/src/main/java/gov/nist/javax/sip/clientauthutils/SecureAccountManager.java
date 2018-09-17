package gov.nist.javax.sip.clientauthutils;

import javax.sip.ClientTransaction;

public interface SecureAccountManager {
    UserCredentialHash getCredentialHash(ClientTransaction clientTransaction, String str);
}
