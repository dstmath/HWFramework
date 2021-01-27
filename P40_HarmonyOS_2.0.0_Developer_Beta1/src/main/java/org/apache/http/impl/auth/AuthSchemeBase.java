package org.apache.http.impl.auth;

import org.apache.http.FormattedHeader;
import org.apache.http.Header;
import org.apache.http.auth.AUTH;
import org.apache.http.auth.AuthScheme;
import org.apache.http.auth.MalformedChallengeException;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.CharArrayBuffer;

@Deprecated
public abstract class AuthSchemeBase implements AuthScheme {
    private boolean proxy;

    /* access modifiers changed from: protected */
    public abstract void parseChallenge(CharArrayBuffer charArrayBuffer, int i, int i2) throws MalformedChallengeException;

    @Override // org.apache.http.auth.AuthScheme
    public void processChallenge(Header header) throws MalformedChallengeException {
        CharArrayBuffer buffer;
        int pos;
        if (header != null) {
            String authheader = header.getName();
            if (authheader.equalsIgnoreCase(AUTH.WWW_AUTH)) {
                this.proxy = false;
            } else if (authheader.equalsIgnoreCase(AUTH.PROXY_AUTH)) {
                this.proxy = true;
            } else {
                throw new MalformedChallengeException("Unexpected header name: " + authheader);
            }
            if (header instanceof FormattedHeader) {
                CharArrayBuffer buffer2 = ((FormattedHeader) header).getBuffer();
                buffer = buffer2;
                pos = ((FormattedHeader) header).getValuePos();
            } else {
                String s = header.getValue();
                if (s != null) {
                    buffer = new CharArrayBuffer(s.length());
                    buffer.append(s);
                    pos = 0;
                } else {
                    throw new MalformedChallengeException("Header value is null");
                }
            }
            while (pos < buffer.length() && HTTP.isWhitespace(buffer.charAt(pos))) {
                pos++;
            }
            while (pos < buffer.length() && !HTTP.isWhitespace(buffer.charAt(pos))) {
                pos++;
            }
            String s2 = buffer.substring(pos, pos);
            if (s2.equalsIgnoreCase(getSchemeName())) {
                parseChallenge(buffer, pos, buffer.length());
                return;
            }
            throw new MalformedChallengeException("Invalid scheme identifier: " + s2);
        }
        throw new IllegalArgumentException("Header may not be null");
    }

    public boolean isProxy() {
        return this.proxy;
    }
}
