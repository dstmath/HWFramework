package org.apache.http.cookie;

import java.io.Serializable;
import java.util.Comparator;

@Deprecated
public class CookieIdentityComparator implements Serializable, Comparator<Cookie> {
    private static final long serialVersionUID = 4466565437490631532L;

    public int compare(Cookie c1, Cookie c2) {
        int res = c1.getName().compareTo(c2.getName());
        if (res != 0) {
            return res;
        }
        String d1 = c1.getDomain();
        if (d1 == null) {
            d1 = "";
        }
        String d2 = c2.getDomain();
        if (d2 == null) {
            d2 = "";
        }
        return d1.compareToIgnoreCase(d2);
    }
}
