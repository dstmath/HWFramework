package sun.misc;

import java.net.InetAddress;

public final class IoTrace {
    private IoTrace() {
    }

    public static Object socketReadBegin() {
        return null;
    }

    public static void socketReadEnd(Object context, InetAddress address, int port, int timeout, long bytesRead) {
    }

    public static Object socketWriteBegin() {
        return null;
    }

    public static void socketWriteEnd(Object context, InetAddress address, int port, long bytesWritten) {
    }

    public static Object fileReadBegin(String path) {
        return null;
    }

    public static void fileReadEnd(Object context, long bytesRead) {
    }

    public static Object fileWriteBegin(String path) {
        return null;
    }

    public static void fileWriteEnd(Object context, long bytesWritten) {
    }
}
