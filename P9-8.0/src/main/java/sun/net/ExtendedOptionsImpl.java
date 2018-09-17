package sun.net;

import java.io.FileDescriptor;
import java.net.SocketOption;
import jdk.net.NetworkPermission;
import jdk.net.SocketFlow;

public class ExtendedOptionsImpl {
    private ExtendedOptionsImpl() {
    }

    public static void checkSetOptionPermission(SocketOption<?> option) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new NetworkPermission("setOption." + option.name()));
        }
    }

    public static void checkGetOptionPermission(SocketOption<?> option) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new NetworkPermission("getOption." + option.name()));
        }
    }

    public static void checkValueType(Object value, Class<?> type) {
        if (!type.isAssignableFrom(value.getClass())) {
            throw new IllegalArgumentException("Found: " + value.getClass().toString() + " Expected: " + type.toString());
        }
    }

    public static void setFlowOption(FileDescriptor fd, SocketFlow f) {
        throw new UnsupportedOperationException("unsupported socket option");
    }

    public static void getFlowOption(FileDescriptor fd, SocketFlow f) {
        throw new UnsupportedOperationException("unsupported socket option");
    }

    public static boolean flowSupported() {
        return false;
    }
}
