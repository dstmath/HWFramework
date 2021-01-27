package org.apache.http.client.protocol;

import java.io.IOException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Header;
import org.apache.http.HeaderIterator;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.client.CookieStore;
import org.apache.http.cookie.Cookie;
import org.apache.http.cookie.CookieOrigin;
import org.apache.http.cookie.CookieSpec;
import org.apache.http.cookie.MalformedCookieException;
import org.apache.http.cookie.SM;
import org.apache.http.protocol.HttpContext;

@Deprecated
public class ResponseProcessCookies implements HttpResponseInterceptor {
    private final Log log = LogFactory.getLog(getClass());

    @Override // org.apache.http.HttpResponseInterceptor
    public void process(HttpResponse response, HttpContext context) throws HttpException, IOException {
        if (response == null) {
            throw new IllegalArgumentException("HTTP request may not be null");
        } else if (context != null) {
            CookieStore cookieStore = (CookieStore) context.getAttribute(ClientContext.COOKIE_STORE);
            if (cookieStore == null) {
                this.log.info("Cookie store not available in HTTP context");
                return;
            }
            CookieSpec cookieSpec = (CookieSpec) context.getAttribute(ClientContext.COOKIE_SPEC);
            if (cookieSpec == null) {
                this.log.info("CookieSpec not available in HTTP context");
                return;
            }
            CookieOrigin cookieOrigin = (CookieOrigin) context.getAttribute(ClientContext.COOKIE_ORIGIN);
            if (cookieOrigin == null) {
                this.log.info("CookieOrigin not available in HTTP context");
                return;
            }
            processCookies(response.headerIterator(SM.SET_COOKIE), cookieSpec, cookieOrigin, cookieStore);
            if (cookieSpec.getVersion() > 0) {
                processCookies(response.headerIterator(SM.SET_COOKIE2), cookieSpec, cookieOrigin, cookieStore);
            }
        } else {
            throw new IllegalArgumentException("HTTP context may not be null");
        }
    }

    private void processCookies(HeaderIterator iterator, CookieSpec cookieSpec, CookieOrigin cookieOrigin, CookieStore cookieStore) {
        while (iterator.hasNext()) {
            Header header = iterator.nextHeader();
            try {
                for (Cookie cookie : cookieSpec.parse(header, cookieOrigin)) {
                    try {
                        cookieSpec.validate(cookie, cookieOrigin);
                        cookieStore.addCookie(cookie);
                        if (this.log.isDebugEnabled()) {
                            Log log2 = this.log;
                            log2.debug("Cookie accepted: \"" + cookieToString(cookie) + "\". ");
                        }
                    } catch (MalformedCookieException ex) {
                        if (this.log.isWarnEnabled()) {
                            Log log3 = this.log;
                            log3.warn("Cookie rejected: \"" + cookieToString(cookie) + "\". " + ex.getMessage());
                        }
                    }
                }
            } catch (MalformedCookieException ex2) {
                if (this.log.isWarnEnabled()) {
                    Log log4 = this.log;
                    log4.warn("Invalid cookie header: \"" + header + "\". " + ex2.getMessage());
                }
            }
        }
    }

    private String cookieToString(Cookie cookie) {
        return cookie.getClass().getSimpleName() + "[version=" + cookie.getVersion() + ",name=" + cookie.getName() + ",domain=" + cookie.getDomain() + ",path=" + cookie.getPath() + ",expiry=" + cookie.getExpiryDate() + "]";
    }
}
