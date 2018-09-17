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

    protected abstract void parseChallenge(CharArrayBuffer charArrayBuffer, int i, int i2) throws MalformedChallengeException;

    public void processChallenge(Header header) throws MalformedChallengeException {
        if (header == null) {
            throw new IllegalArgumentException("Header may not be null");
        }
        CharArrayBuffer buffer;
        int pos;
        String s;
        String authheader = header.getName();
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
            s = header.getValue();
            if (s == null) {
                throw new MalformedChallengeException("Header value is null");
            }
            buffer = new CharArrayBuffer(s.length());
            buffer.append(s);
            pos = 0;
        }
        while (pos < buffer.length() && HTTP.isWhitespace(buffer.charAt(pos))) {
            pos++;
        }
        int beginIndex = pos;
        while (pos < buffer.length() && (HTTP.isWhitespace(buffer.charAt(pos)) ^ 1) != 0) {
            pos++;
        }
        int endIndex = pos;
        s = buffer.substring(beginIndex, pos);
        if (s.equalsIgnoreCase(getSchemeName())) {
            parseChallenge(buffer, pos, buffer.length());
            return;
        }
        throw new MalformedChallengeException("Invalid scheme identifier: " + s);
    }

    public boolean isProxy() {
        return this.proxy;
    }
}
