package java.net;

public interface CookiePolicy {
    public static final CookiePolicy ACCEPT_ALL = new CookiePolicy() {
        public boolean shouldAccept(URI uri, HttpCookie cookie) {
            return true;
        }
    };
    public static final CookiePolicy ACCEPT_NONE = new CookiePolicy() {
        public boolean shouldAccept(URI uri, HttpCookie cookie) {
            return false;
        }
    };
    public static final CookiePolicy ACCEPT_ORIGINAL_SERVER = new CookiePolicy() {
        public boolean shouldAccept(URI uri, HttpCookie cookie) {
            if (uri == null || cookie == null) {
                return false;
            }
            return HttpCookie.domainMatches(cookie.getDomain(), uri.getHost());
        }
    };

    boolean shouldAccept(URI uri, HttpCookie httpCookie);
}
