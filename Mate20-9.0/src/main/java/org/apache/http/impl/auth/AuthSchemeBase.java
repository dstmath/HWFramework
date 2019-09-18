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

    public void processChallenge(Header header) throws MalformedChallengeException {
        CharArrayBuffer buffer;
        if (header != null) {
            String authheader = header.getName();
            int pos = 0;
            if (authheader.equalsIgnoreCase(AUTH.WWW_AUTH)) {
                this.proxy = false;
            } else if (authheader.equalsIgnoreCase(AUTH.PROXY_AUTH)) {
                this.proxy = true;
            } else {
                throw new MalformedChallengeException("Unexpected header name: " + authheader);
            }
            if (header instanceof FormattedHeader) {
                buffer = ((FormattedHeader) header).getBuffer();
                pos = ((FormattedHeader) header).getValuePos();
            } else {
                String s = header.getValue();
                if (s != null) {
                    CharArrayBuffer buffer2 = new CharArrayBuffer(s.length());
                    buffer2.append(s);
                    buffer = buffer2;
                } else {
                    throw new MalformedChallengeException("Header value is null");
                }
            }
            while (pos < buffer.length() && HTTP.isWhitespace(buffer.charAt(pos))) {
                pos++;
            }
            int pos2 = pos;
            while (pos2 < buffer.length() && !HTTP.isWhitespace(buffer.charAt(pos2))) {
                pos2++;
            }
            String s2 = buffer.substring(pos, pos2);
            if (s2.equalsIgnoreCase(getSchemeName())) {
                parseChallenge(buffer, pos2, buffer.length());
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
