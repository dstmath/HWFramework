package sun.security.ssl;

import java.security.AccessControlContext;
import java.security.Permission;
import java.security.Principal;
import javax.crypto.SecretKey;
import javax.security.auth.Subject;
import javax.security.auth.login.LoginException;

public interface Krb5Proxy {
    Subject getClientSubject(AccessControlContext accessControlContext) throws LoginException;

    String getPrincipalHostName(Principal principal);

    SecretKey[] getServerKeys(AccessControlContext accessControlContext) throws LoginException;

    String getServerPrincipalName(SecretKey secretKey);

    Subject getServerSubject(AccessControlContext accessControlContext) throws LoginException;

    Permission getServicePermission(String str, String str2);
}
