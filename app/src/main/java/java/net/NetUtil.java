package java.net;

import java.security.AccessController;
import java.security.PrivilegedExceptionAction;

class NetUtil {
    private static volatile boolean propRevealLocalAddr;
    private static boolean revealLocalAddress;

    NetUtil() {
    }

    static boolean doRevealLocalAddress() {
        if (propRevealLocalAddr) {
            return revealLocalAddress;
        }
        return readRevealLocalAddr();
    }

    private static boolean readRevealLocalAddr() {
        if (System.getSecurityManager() != null) {
            try {
                revealLocalAddress = Boolean.parseBoolean((String) AccessController.doPrivileged(new PrivilegedExceptionAction<String>() {
                    public String run() {
                        return System.getProperty("jdk.net.revealLocalAddress");
                    }
                }));
            } catch (Exception e) {
            }
            propRevealLocalAddr = true;
        }
        return revealLocalAddress;
    }
}
