package android.support.v4.net;

import android.net.TrafficStats;
import android.os.Build.VERSION;
import android.os.ParcelFileDescriptor;
import android.support.annotation.RequiresApi;
import java.net.DatagramSocket;
import java.net.Socket;
import java.net.SocketException;

public final class TrafficStatsCompat {
    private static final TrafficStatsCompatBaseImpl IMPL;

    static class TrafficStatsCompatBaseImpl {
        TrafficStatsCompatBaseImpl() {
        }

        public void tagDatagramSocket(DatagramSocket socket) throws SocketException {
            ParcelFileDescriptor pfd = ParcelFileDescriptor.fromDatagramSocket(socket);
            TrafficStats.tagSocket(new DatagramSocketWrapper(socket, pfd.getFileDescriptor()));
            pfd.detachFd();
        }

        public void untagDatagramSocket(DatagramSocket socket) throws SocketException {
            ParcelFileDescriptor pfd = ParcelFileDescriptor.fromDatagramSocket(socket);
            TrafficStats.untagSocket(new DatagramSocketWrapper(socket, pfd.getFileDescriptor()));
            pfd.detachFd();
        }
    }

    @RequiresApi(24)
    static class TrafficStatsCompatApi24Impl extends TrafficStatsCompatBaseImpl {
        TrafficStatsCompatApi24Impl() {
        }

        public void tagDatagramSocket(DatagramSocket socket) throws SocketException {
            TrafficStats.tagDatagramSocket(socket);
        }

        public void untagDatagramSocket(DatagramSocket socket) throws SocketException {
            TrafficStats.untagDatagramSocket(socket);
        }
    }

    static {
        if (VERSION.SDK_INT >= 24) {
            IMPL = new TrafficStatsCompatApi24Impl();
        } else {
            IMPL = new TrafficStatsCompatBaseImpl();
        }
    }

    @Deprecated
    public static void clearThreadStatsTag() {
        TrafficStats.clearThreadStatsTag();
    }

    @Deprecated
    public static int getThreadStatsTag() {
        return TrafficStats.getThreadStatsTag();
    }

    @Deprecated
    public static void incrementOperationCount(int operationCount) {
        TrafficStats.incrementOperationCount(operationCount);
    }

    @Deprecated
    public static void incrementOperationCount(int tag, int operationCount) {
        TrafficStats.incrementOperationCount(tag, operationCount);
    }

    @Deprecated
    public static void setThreadStatsTag(int tag) {
        TrafficStats.setThreadStatsTag(tag);
    }

    @Deprecated
    public static void tagSocket(Socket socket) throws SocketException {
        TrafficStats.tagSocket(socket);
    }

    @Deprecated
    public static void untagSocket(Socket socket) throws SocketException {
        TrafficStats.untagSocket(socket);
    }

    public static void tagDatagramSocket(DatagramSocket socket) throws SocketException {
        IMPL.tagDatagramSocket(socket);
    }

    public static void untagDatagramSocket(DatagramSocket socket) throws SocketException {
        IMPL.untagDatagramSocket(socket);
    }

    private TrafficStatsCompat() {
    }
}
