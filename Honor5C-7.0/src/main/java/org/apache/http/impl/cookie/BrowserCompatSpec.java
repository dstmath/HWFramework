package org.apache.http.impl.cookie;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
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
public class BrowserCompatSpec extends CookieSpecBase {
    protected static final String[] DATE_PATTERNS = null;
    private final String[] datepatterns;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: org.apache.http.impl.cookie.BrowserCompatSpec.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: org.apache.http.impl.cookie.BrowserCompatSpec.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: org.apache.http.impl.cookie.BrowserCompatSpec.<clinit>():void");
    }

    public BrowserCompatSpec(String[] datepatterns) {
        if (datepatterns != null) {
            this.datepatterns = (String[]) datepatterns.clone();
        } else {
            this.datepatterns = DATE_PATTERNS;
        }
        registerAttribHandler(ClientCookie.PATH_ATTR, new BasicPathHandler());
        registerAttribHandler(ClientCookie.DOMAIN_ATTR, new BasicDomainHandler());
        registerAttribHandler(ClientCookie.MAX_AGE_ATTR, new BasicMaxAgeHandler());
        registerAttribHandler(ClientCookie.SECURE_ATTR, new BasicSecureHandler());
        registerAttribHandler(ClientCookie.COMMENT_ATTR, new BasicCommentHandler());
        registerAttribHandler(ClientCookie.EXPIRES_ATTR, new BasicExpiresHandler(this.datepatterns));
    }

    public BrowserCompatSpec() {
        this(null);
    }

    public List<Cookie> parse(Header header, CookieOrigin origin) throws MalformedCookieException {
        if (header == null) {
            throw new IllegalArgumentException("Header may not be null");
        } else if (origin == null) {
            throw new IllegalArgumentException("Cookie origin may not be null");
        } else {
            HeaderElement[] elems;
            String headervalue = header.getValue();
            boolean isNetscapeCookie = false;
            int i1 = headervalue.toLowerCase(Locale.ENGLISH).indexOf("expires=");
            if (i1 != -1) {
                i1 += "expires=".length();
                int i2 = headervalue.indexOf(59, i1);
                if (i2 == -1) {
                    i2 = headervalue.length();
                }
                try {
                    DateUtils.parseDate(headervalue.substring(i1, i2), this.datepatterns);
                    isNetscapeCookie = true;
                } catch (DateParseException e) {
                }
            }
            if (isNetscapeCookie) {
                CharArrayBuffer buffer;
                ParserCursor cursor;
                NetscapeDraftHeaderParser parser = NetscapeDraftHeaderParser.DEFAULT;
                if (header instanceof FormattedHeader) {
                    buffer = ((FormattedHeader) header).getBuffer();
                    cursor = new ParserCursor(((FormattedHeader) header).getValuePos(), buffer.length());
                } else {
                    String s = header.getValue();
                    if (s == null) {
                        throw new MalformedCookieException("Header value is null");
                    }
                    buffer = new CharArrayBuffer(s.length());
                    buffer.append(s);
                    cursor = new ParserCursor(0, buffer.length());
                }
                elems = new HeaderElement[]{parser.parseHeader(buffer, cursor)};
            } else {
                elems = header.getElements();
            }
            return parse(elems, origin);
        }
    }

    public List<Header> formatCookies(List<Cookie> cookies) {
        if (cookies == null) {
            throw new IllegalArgumentException("List of cookies may not be null");
        } else if (cookies.isEmpty()) {
            throw new IllegalArgumentException("List of cookies may not be empty");
        } else {
            CharArrayBuffer buffer = new CharArrayBuffer(cookies.size() * 20);
            buffer.append(SM.COOKIE);
            buffer.append(": ");
            for (int i = 0; i < cookies.size(); i++) {
                Cookie cookie = (Cookie) cookies.get(i);
                if (i > 0) {
                    buffer.append("; ");
                }
                buffer.append(cookie.getName());
                buffer.append("=");
                String s = cookie.getValue();
                if (s != null) {
                    buffer.append(s);
                }
            }
            List<Header> headers = new ArrayList(1);
            headers.add(new BufferedHeader(buffer));
            return headers;
        }
    }

    public int getVersion() {
        return 0;
    }

    public Header getVersionHeader() {
        return null;
    }
}
