package java.net;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import sun.security.util.SecurityConstants;

public abstract class CookieHandler {
    private static CookieHandler cookieHandler;

    public abstract Map<String, List<String>> get(URI uri, Map<String, List<String>> map) throws IOException;

    public abstract void put(URI uri, Map<String, List<String>> map) throws IOException;

    public static synchronized CookieHandler getDefault() {
        CookieHandler cookieHandler;
        synchronized (CookieHandler.class) {
            SecurityManager sm = System.getSecurityManager();
            if (sm != null) {
                sm.checkPermission(SecurityConstants.GET_COOKIEHANDLER_PERMISSION);
            }
            cookieHandler = cookieHandler;
        }
        return cookieHandler;
    }

    public static synchronized void setDefault(CookieHandler cHandler) {
        synchronized (CookieHandler.class) {
            SecurityManager sm = System.getSecurityManager();
            if (sm != null) {
                sm.checkPermission(SecurityConstants.SET_COOKIEHANDLER_PERMISSION);
            }
            cookieHandler = cHandler;
        }
    }
}
