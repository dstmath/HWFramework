package sun.net;

import java.io.FileDescriptor;
import java.io.IOException;
import java.net.InetAddress;

public final class NetHooks {
    public static void beforeTcpBind(FileDescriptor fdObj, InetAddress address, int port) throws IOException {
    }

    public static void beforeTcpConnect(FileDescriptor fdObj, InetAddress address, int port) throws IOException {
    }
}
