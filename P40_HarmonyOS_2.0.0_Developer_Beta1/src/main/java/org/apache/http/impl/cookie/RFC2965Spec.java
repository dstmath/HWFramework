package org.apache.http.impl.cookie;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.NameValuePair;
import org.apache.http.cookie.ClientCookie;
import org.apache.http.cookie.Cookie;
import org.apache.http.cookie.CookieAttributeHandler;
import org.apache.http.cookie.CookieOrigin;
import org.apache.http.cookie.MalformedCookieException;
import org.apache.http.cookie.SM;
import org.apache.http.message.BufferedHeader;
import org.apache.http.util.CharArrayBuffer;

@Deprecated
public class RFC2965Spec extends RFC2109Spec {
    public RFC2965Spec() {
        this(null, false);
    }

    public RFC2965Spec(String[] datepatterns, boolean oneHeader) {
        super(datepatterns, oneHeader);
        registerAttribHandler(ClientCookie.DOMAIN_ATTR, new RFC2965DomainAttributeHandler());
        registerAttribHandler(ClientCookie.PORT_ATTR, new RFC2965PortAttributeHandler());
        registerAttribHandler(ClientCookie.COMMENTURL_ATTR, new RFC2965CommentUrlAttributeHandler());
        registerAttribHandler(ClientCookie.DISCARD_ATTR, new RFC2965DiscardAttributeHandler());
        registerAttribHandler(ClientCookie.VERSION_ATTR, new RFC2965VersionAttributeHandler());
    }

    private BasicClientCookie createCookie(String name, String value, CookieOrigin origin) {
        BasicClientCookie cookie = new BasicClientCookie(name, value);
        cookie.setPath(getDefaultPath(origin));
        cookie.setDomain(getDefaultDomain(origin));
        return cookie;
    }

    private BasicClientCookie createCookie2(String name, String value, CookieOrigin origin) {
        BasicClientCookie2 cookie = new BasicClientCookie2(name, value);
        cookie.setPath(getDefaultPath(origin));
        cookie.setDomain(getDefaultDomain(origin));
        cookie.setPorts(new int[]{origin.getPort()});
        return cookie;
    }

    @Override // org.apache.http.impl.cookie.RFC2109Spec, org.apache.http.cookie.CookieSpec
    public List<Cookie> parse(Header header, CookieOrigin origin) throws MalformedCookieException {
        BasicClientCookie cookie;
        RFC2965Spec rFC2965Spec = this;
        if (header == null) {
            throw new IllegalArgumentException("Header may not be null");
        } else if (origin != null) {
            CookieOrigin origin2 = adjustEffectiveHost(origin);
            HeaderElement[] elems = header.getElements();
            List<Cookie> cookies = new ArrayList<>(elems.length);
            int length = elems.length;
            int i = 0;
            while (i < length) {
                HeaderElement headerelement = elems[i];
                String name = headerelement.getName();
                String value = headerelement.getValue();
                if (name == null || name.length() == 0) {
                    throw new MalformedCookieException("Cookie name may not be empty");
                }
                if (header.getName().equals(SM.SET_COOKIE2)) {
                    cookie = rFC2965Spec.createCookie2(name, value, origin2);
                } else {
                    cookie = rFC2965Spec.createCookie(name, value, origin2);
                }
                NameValuePair[] attribs = headerelement.getParameters();
                Map<String, NameValuePair> attribmap = new HashMap<>(attribs.length);
                for (int j = attribs.length - 1; j >= 0; j--) {
                    NameValuePair param = attribs[j];
                    attribmap.put(param.getName().toLowerCase(Locale.ENGLISH), param);
                }
                for (Map.Entry<String, NameValuePair> entry : attribmap.entrySet()) {
                    NameValuePair attrib = entry.getValue();
                    String s = attrib.getName().toLowerCase(Locale.ENGLISH);
                    cookie.setAttribute(s, attrib.getValue());
                    CookieAttributeHandler handler = rFC2965Spec.findAttribHandler(s);
                    if (handler != null) {
                        handler.parse(cookie, attrib.getValue());
                    }
                    rFC2965Spec = this;
                    origin2 = origin2;
                }
                cookies.add(cookie);
                i++;
                rFC2965Spec = this;
            }
            return cookies;
        } else {
            throw new IllegalArgumentException("Cookie origin may not be null");
        }
    }

    @Override // org.apache.http.impl.cookie.RFC2109Spec, org.apache.http.impl.cookie.CookieSpecBase, org.apache.http.cookie.CookieSpec
    public void validate(Cookie cookie, CookieOrigin origin) throws MalformedCookieException {
        if (cookie == null) {
            throw new IllegalArgumentException("Cookie may not be null");
        } else if (origin != null) {
            super.validate(cookie, adjustEffectiveHost(origin));
        } else {
            throw new IllegalArgumentException("Cookie origin may not be null");
        }
    }

    @Override // org.apache.http.impl.cookie.CookieSpecBase, org.apache.http.cookie.CookieSpec
    public boolean match(Cookie cookie, CookieOrigin origin) {
        if (cookie == null) {
            throw new IllegalArgumentException("Cookie may not be null");
        } else if (origin != null) {
            return super.match(cookie, adjustEffectiveHost(origin));
        } else {
            throw new IllegalArgumentException("Cookie origin may not be null");
        }
    }

    /* access modifiers changed from: protected */
    @Override // org.apache.http.impl.cookie.RFC2109Spec
    public void formatCookieAsVer(CharArrayBuffer buffer, Cookie cookie, int version) {
        String s;
        int[] ports;
        super.formatCookieAsVer(buffer, cookie, version);
        if ((cookie instanceof ClientCookie) && (s = ((ClientCookie) cookie).getAttribute(ClientCookie.PORT_ATTR)) != null) {
            buffer.append("; $Port");
            buffer.append("=\"");
            if (s.trim().length() > 0 && (ports = cookie.getPorts()) != null) {
                int len = ports.length;
                for (int i = 0; i < len; i++) {
                    if (i > 0) {
                        buffer.append(",");
                    }
                    buffer.append(Integer.toString(ports[i]));
                }
            }
            buffer.append("\"");
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:8:0x001c, code lost:
        r1 = false;
     */
    private static CookieOrigin adjustEffectiveHost(CookieOrigin origin) {
        String host = origin.getHost();
        boolean isLocalHost = true;
        int i = 0;
        while (true) {
            if (i >= host.length()) {
                break;
            }
            char ch = host.charAt(i);
            if (ch == '.' || ch == ':') {
                break;
            }
            i++;
        }
        if (!isLocalHost) {
            return origin;
        }
        return new CookieOrigin(host + ".local", origin.getPort(), origin.getPath(), origin.isSecure());
    }

    @Override // org.apache.http.impl.cookie.RFC2109Spec, org.apache.http.cookie.CookieSpec
    public int getVersion() {
        return 1;
    }

    @Override // org.apache.http.impl.cookie.RFC2109Spec, org.apache.http.cookie.CookieSpec
    public Header getVersionHeader() {
        CharArrayBuffer buffer = new CharArrayBuffer(40);
        buffer.append(SM.COOKIE2);
        buffer.append(": ");
        buffer.append("$Version=");
        buffer.append(Integer.toString(getVersion()));
        return new BufferedHeader(buffer);
    }
}
