package android.net.util;

import android.annotation.SystemApi;
import android.net.NetworkUtils;
import android.system.ErrnoException;
import android.system.NetlinkSocketAddress;
import android.system.Os;
import android.system.OsConstants;
import android.system.PacketSocketAddress;
import java.io.FileDescriptor;
import java.io.IOException;
import java.net.SocketAddress;
import libcore.io.IoBridge;

@SystemApi
public final class SocketUtils {
    public static void bindSocketToInterface(FileDescriptor socket, String iface) throws ErrnoException {
        Os.setsockoptIfreq(socket, OsConstants.SOL_SOCKET, OsConstants.SO_BINDTODEVICE, iface);
        NetworkUtils.protectFromVpn(socket);
    }

    public static SocketAddress makeNetlinkSocketAddress(int portId, int groupsMask) {
        return new NetlinkSocketAddress(portId, groupsMask);
    }

    public static SocketAddress makePacketSocketAddress(int protocol, int ifIndex) {
        return new PacketSocketAddress((short) protocol, ifIndex);
    }

    public static SocketAddress makePacketSocketAddress(int ifIndex, byte[] hwAddr) {
        return new PacketSocketAddress(ifIndex, hwAddr);
    }

    public static void closeSocket(FileDescriptor fd) throws IOException {
        IoBridge.closeAndSignalBlockedThreads(fd);
    }

    private SocketUtils() {
    }
}
