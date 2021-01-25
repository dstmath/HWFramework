package org.apache.http.impl.cookie;

import java.util.ArrayList;
import java.util.List;
import org.apache.http.FormattedHeader;
import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.cookie.ClientCookie;
import org.apache.http.cookie.Cookie;
import org.apache.http.cookie.CookieOrigin;
import org.apache.http.cookie.MalformedCookieException;
import org.apache.http.cookie.SM;
import org.apache.http.message.BufferedHeader;
import org.apache.http.message.ParserCursor;
import org.apache.http.util.CharArrayBuffer;

@Deprecated
public class NetscapeDraftSpec extends CookieSpecBase {
    protected static final String EXPIRES_PATTERN = "EEE, dd-MMM-yyyy HH:mm:ss z";
    private final String[] datepatterns;

    public NetscapeDraftSpec(String[] datepatterns2) {
        if (datepatterns2 != null) {
            this.datepatterns = (String[]) datepatterns2.clone();
        } else {
            this.datepatterns = new String[]{EXPIRES_PATTERN};
        }
        registerAttribHandler(ClientCookie.PATH_ATTR, new BasicPathHandler());
        registerAttribHandler(ClientCookie.DOMAIN_ATTR, new NetscapeDomainHandler());
        registerAttribHandler(ClientCookie.MAX_AGE_ATTR, new BasicMaxAgeHandler());
        registerAttribHandler(ClientCookie.SECURE_ATTR, new BasicSecureHandler());
        registerAttribHandler(ClientCookie.COMMENT_ATTR, new BasicCommentHandler());
        registerAttribHandler("expires", new BasicExpiresHandler(this.datepatterns));
    }

    public NetscapeDraftSpec() {
        this(null);
    }

    @Override // org.apache.http.cookie.CookieSpec
    public List<Cookie> parse(Header header, CookieOrigin origin) throws MalformedCookieException {
        ParserCursor cursor;
        CharArrayBuffer buffer;
        if (header == null) {
            throw new IllegalArgumentException("Header may not be null");
        } else if (origin != null) {
            NetscapeDraftHeaderParser parser = NetscapeDraftHeaderParser.DEFAULT;
            if (header instanceof FormattedHeader) {
                buffer = ((FormattedHeader) header).getBuffer();
                cursor = new ParserCursor(((FormattedHeader) header).getValuePos(), buffer.length());
            } else {
                String s = header.getValue();
                if (s != null) {
                    CharArrayBuffer buffer2 = new CharArrayBuffer(s.length());
                    buffer2.append(s);
                    cursor = new ParserCursor(0, buffer2.length());
                    buffer = buffer2;
                } else {
                    throw new MalformedCookieException("Header value is null");
                }
            }
            return parse(new HeaderElement[]{parser.parseHeader(buffer, cursor)}, origin);
        } else {
            throw new IllegalArgumentException("Cookie origin may not be null");
        }
    }

    @Override // org.apache.http.cookie.CookieSpec
    public List<Header> formatCookies(List<Cookie> cookies) {
        if (cookies == null) {
            throw new IllegalArgumentException("List of cookies may not be null");
        } else if (!cookies.isEmpty()) {
            CharArrayBuffer buffer = new CharArrayBuffer(cookies.size() * 20);
            buffer.append(SM.COOKIE);
            buffer.append(": ");
            for (int i = 0; i < cookies.size(); i++) {
                Cookie cookie = cookies.get(i);
                if (i > 0) {
                    buffer.append("; ");
                }
                buffer.append(cookie.getName());
                String s = cookie.getValue();
                if (s != null) {
                    buffer.append("=");
                    buffer.append(s);
                }
            }
            List<Header> headers = new ArrayList<>(1);
            headers.add(new BufferedHeader(buffer));
            return headers;
        } else {
            throw new IllegalArgumentException("List of cookies may not be empty");
        }
    }

    @Override // org.apache.http.cookie.CookieSpec
    public int getVersion() {
        return 0;
    }

    @Override // org.apache.http.cookie.CookieSpec
    public Header getVersionHeader() {
        return null;
    }
}
