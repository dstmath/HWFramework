package sun.net;

import java.net.SocketException;
import java.security.AccessController;
import java.util.concurrent.atomic.AtomicInteger;
import sun.security.action.GetPropertyAction;

public class ResourceManager {
    private static final int DEFAULT_MAX_SOCKETS = 25;
    private static final int maxSockets;
    private static final AtomicInteger numSockets = new AtomicInteger(0);

    static {
        String prop = (String) AccessController.doPrivileged(new GetPropertyAction("sun.net.maxDatagramSockets"));
        int defmax = DEFAULT_MAX_SOCKETS;
        if (prop != null) {
            try {
                defmax = Integer.parseInt(prop);
            } catch (NumberFormatException e) {
            }
        }
        maxSockets = defmax;
    }

    public static void beforeUdpCreate() throws SocketException {
        if (System.getSecurityManager() != null && numSockets.incrementAndGet() > maxSockets) {
            numSockets.decrementAndGet();
            throw new SocketException("maximum number of DatagramSockets reached");
        }
    }

    public static void afterUdpClose() {
        if (System.getSecurityManager() != null) {
            numSockets.decrementAndGet();
        }
    }
}
