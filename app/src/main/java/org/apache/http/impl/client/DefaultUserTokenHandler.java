package org.apache.http.impl.client;

import java.security.Principal;
import javax.net.ssl.SSLSession;
import org.apache.http.auth.AuthScheme;
import org.apache.http.auth.AuthState;
import org.apache.http.auth.Credentials;
import org.apache.http.client.UserTokenHandler;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.conn.ManagedClientConnection;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;

@Deprecated
public class DefaultUserTokenHandler implements UserTokenHandler {
    public Object getUserToken(HttpContext context) {
        Object obj = null;
        AuthState targetAuthState = (AuthState) context.getAttribute(ClientContext.TARGET_AUTH_STATE);
        if (targetAuthState != null) {
            obj = getAuthPrincipal(targetAuthState);
            if (obj == null) {
                obj = getAuthPrincipal((AuthState) context.getAttribute(ClientContext.PROXY_AUTH_STATE));
            }
        }
        if (obj != null) {
            return obj;
        }
        ManagedClientConnection conn = (ManagedClientConnection) context.getAttribute(ExecutionContext.HTTP_CONNECTION);
        if (!conn.isOpen()) {
            return obj;
        }
        SSLSession sslsession = conn.getSSLSession();
        if (sslsession != null) {
            return sslsession.getLocalPrincipal();
        }
        return obj;
    }

    private static Principal getAuthPrincipal(AuthState authState) {
        AuthScheme scheme = authState.getAuthScheme();
        if (scheme != null && scheme.isComplete() && scheme.isConnectionBased()) {
            Credentials creds = authState.getCredentials();
            if (creds != null) {
                return creds.getUserPrincipal();
            }
        }
        return null;
    }
}
