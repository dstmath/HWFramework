package org.apache.http.impl.auth;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.Header;
import org.apache.http.HttpRequest;
import org.apache.http.auth.AUTH;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.MalformedChallengeException;
import org.apache.http.auth.params.AuthParams;
import org.apache.http.message.BufferedHeader;
import org.apache.http.util.CharArrayBuffer;
import org.apache.http.util.EncodingUtils;

@Deprecated
public class BasicScheme extends RFC2617Scheme {
    private boolean complete = false;

    @Override // org.apache.http.auth.AuthScheme
    public String getSchemeName() {
        return "basic";
    }

    @Override // org.apache.http.impl.auth.AuthSchemeBase, org.apache.http.auth.AuthScheme
    public void processChallenge(Header header) throws MalformedChallengeException {
        super.processChallenge(header);
        this.complete = true;
    }

    @Override // org.apache.http.auth.AuthScheme
    public boolean isComplete() {
        return this.complete;
    }

    @Override // org.apache.http.auth.AuthScheme
    public boolean isConnectionBased() {
        return false;
    }

    @Override // org.apache.http.auth.AuthScheme
    public Header authenticate(Credentials credentials, HttpRequest request) throws AuthenticationException {
        if (credentials == null) {
            throw new IllegalArgumentException("Credentials may not be null");
        } else if (request != null) {
            return authenticate(credentials, AuthParams.getCredentialCharset(request.getParams()), isProxy());
        } else {
            throw new IllegalArgumentException("HTTP request may not be null");
        }
    }

    public static Header authenticate(Credentials credentials, String charset, boolean proxy) {
        if (credentials == null) {
            throw new IllegalArgumentException("Credentials may not be null");
        } else if (charset != null) {
            StringBuilder tmp = new StringBuilder();
            tmp.append(credentials.getUserPrincipal().getName());
            tmp.append(":");
            tmp.append(credentials.getPassword() == null ? "null" : credentials.getPassword());
            byte[] base64password = Base64.encodeBase64(EncodingUtils.getBytes(tmp.toString(), charset));
            CharArrayBuffer buffer = new CharArrayBuffer(32);
            if (proxy) {
                buffer.append(AUTH.PROXY_AUTH_RESP);
            } else {
                buffer.append(AUTH.WWW_AUTH_RESP);
            }
            buffer.append(": Basic ");
            buffer.append(base64password, 0, base64password.length);
            return new BufferedHeader(buffer);
        } else {
            throw new IllegalArgumentException("charset may not be null");
        }
    }
}
