package org.apache.http.impl.cookie;

import org.apache.http.cookie.CookieSpec;
import org.apache.http.cookie.CookieSpecFactory;
import org.apache.http.cookie.params.CookieSpecPNames;
import org.apache.http.params.HttpParams;

@Deprecated
public class RFC2965SpecFactory implements CookieSpecFactory {
    public CookieSpec newInstance(HttpParams params) {
        if (params != null) {
            return new RFC2965Spec((String[]) params.getParameter(CookieSpecPNames.DATE_PATTERNS), params.getBooleanParameter(CookieSpecPNames.SINGLE_COOKIE_HEADER, false));
        }
        return new RFC2965Spec();
    }
}
