package java.net;

import java.io.IOException;
import java.util.List;
import sun.security.util.SecurityConstants;

public abstract class ProxySelector {
    private static ProxySelector theProxySelector;

    public abstract void connectFailed(URI uri, SocketAddress socketAddress, IOException iOException);

    public abstract List<Proxy> select(URI uri);

    static {
        try {
            Class<?> c = Class.forName("sun.net.spi.DefaultProxySelector");
            if (c != null && ProxySelector.class.isAssignableFrom(c)) {
                theProxySelector = (ProxySelector) c.newInstance();
            }
        } catch (Exception e) {
            theProxySelector = null;
        }
    }

    public static ProxySelector getDefault() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(SecurityConstants.GET_PROXYSELECTOR_PERMISSION);
        }
        return theProxySelector;
    }

    public static void setDefault(ProxySelector ps) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(SecurityConstants.SET_PROXYSELECTOR_PERMISSION);
        }
        theProxySelector = ps;
    }
}
