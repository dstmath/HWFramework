package com.android.server.connectivity;

import android.net.NetworkUtils;
import android.net.SocketKeepalive;
import android.net.TcpKeepalivePacketData;
import android.net.TcpKeepalivePacketDataParcelable;
import android.net.TcpRepairWindow;
import android.os.Handler;
import android.os.MessageQueue;
import android.system.ErrnoException;
import android.system.Int32Ref;
import android.system.Os;
import android.system.OsConstants;
import android.util.Log;
import android.util.SparseArray;
import com.android.internal.annotations.GuardedBy;
import com.android.server.connectivity.KeepaliveTracker;
import java.io.FileDescriptor;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;

public class TcpKeepaliveController {
    private static final boolean DBG = false;
    private static final int FD_EVENTS = 5;
    private static final int SIOCINQ = OsConstants.FIONREAD;
    private static final int SIOCOUTQ = OsConstants.TIOCOUTQ;
    private static final String TAG = "TcpKeepaliveController";
    private static final int TCP_NO_QUEUE = 0;
    private static final int TCP_QUEUE_SEQ = 21;
    private static final int TCP_RECV_QUEUE = 1;
    private static final int TCP_REPAIR = 19;
    private static final int TCP_REPAIR_OFF = 0;
    private static final int TCP_REPAIR_ON = 1;
    private static final int TCP_REPAIR_QUEUE = 20;
    private static final int TCP_SEND_QUEUE = 2;
    private final MessageQueue mFdHandlerQueue;
    @GuardedBy({"mListeners"})
    private final SparseArray<FileDescriptor> mListeners = new SparseArray<>();

    public TcpKeepaliveController(Handler connectivityServiceHandler) {
        this.mFdHandlerQueue = connectivityServiceHandler.getLooper().getQueue();
    }

    public static TcpKeepalivePacketData getTcpKeepalivePacket(FileDescriptor fd) throws SocketKeepalive.InvalidPacketException, SocketKeepalive.InvalidSocketException {
        try {
            return TcpKeepalivePacketData.tcpKeepalivePacket(switchToRepairMode(fd));
        } catch (SocketKeepalive.InvalidPacketException | SocketKeepalive.InvalidSocketException e) {
            switchOutOfRepairMode(fd);
            throw e;
        }
    }

    private static TcpKeepalivePacketDataParcelable switchToRepairMode(FileDescriptor fd) throws SocketKeepalive.InvalidSocketException {
        TcpKeepalivePacketDataParcelable tcpDetails = new TcpKeepalivePacketDataParcelable();
        try {
            SocketAddress srcSockAddr = Os.getsockname(fd);
            if (srcSockAddr instanceof InetSocketAddress) {
                tcpDetails.srcAddress = getAddress((InetSocketAddress) srcSockAddr);
                tcpDetails.srcPort = getPort((InetSocketAddress) srcSockAddr);
                try {
                    SocketAddress dstSockAddr = Os.getpeername(fd);
                    if (dstSockAddr instanceof InetSocketAddress) {
                        tcpDetails.dstAddress = getAddress((InetSocketAddress) dstSockAddr);
                        tcpDetails.dstPort = getPort((InetSocketAddress) dstSockAddr);
                        dropAllIncomingPackets(fd, true);
                        try {
                            Os.setsockoptInt(fd, OsConstants.IPPROTO_TCP, 19, 1);
                            if (isSocketIdle(fd)) {
                                Os.setsockoptInt(fd, OsConstants.IPPROTO_TCP, 20, 2);
                                tcpDetails.seq = Os.getsockoptInt(fd, OsConstants.IPPROTO_TCP, 21);
                                Os.setsockoptInt(fd, OsConstants.IPPROTO_TCP, 20, 1);
                                tcpDetails.ack = Os.getsockoptInt(fd, OsConstants.IPPROTO_TCP, 21);
                                Os.setsockoptInt(fd, OsConstants.IPPROTO_TCP, 20, 0);
                                if (!isReceiveQueueEmpty(fd)) {
                                    Log.e(TAG, "Fatal: receive queue of this socket is not empty");
                                    throw new SocketKeepalive.InvalidSocketException(-25);
                                } else if (isSendQueueEmpty(fd)) {
                                    TcpRepairWindow trw = NetworkUtils.getTcpRepairWindow(fd);
                                    tcpDetails.rcvWnd = trw.rcvWnd;
                                    tcpDetails.rcvWndScale = trw.rcvWndScale;
                                    if (tcpDetails.srcAddress.length == 4) {
                                        tcpDetails.tos = Os.getsockoptInt(fd, OsConstants.IPPROTO_IP, OsConstants.IP_TOS);
                                        tcpDetails.ttl = Os.getsockoptInt(fd, OsConstants.IPPROTO_IP, OsConstants.IP_TTL);
                                    }
                                    dropAllIncomingPackets(fd, false);
                                    tcpDetails.seq--;
                                    return tcpDetails;
                                } else {
                                    Log.e(TAG, "Socket is not idle");
                                    throw new SocketKeepalive.InvalidSocketException(-26);
                                }
                            } else {
                                Log.e(TAG, "Socket is not idle");
                                throw new SocketKeepalive.InvalidSocketException(-26);
                            }
                        } catch (ErrnoException e) {
                            Log.e(TAG, "Exception reading TCP state from socket", e);
                            if (e.errno == OsConstants.ENOPROTOOPT) {
                                throw new SocketKeepalive.InvalidSocketException(-30, e);
                            }
                            throw new SocketKeepalive.InvalidSocketException(-25, e);
                        } catch (Throwable th) {
                            dropAllIncomingPackets(fd, false);
                            throw th;
                        }
                    } else {
                        Log.e(TAG, "Invalid or mismatched peer SocketAddress");
                        throw new SocketKeepalive.InvalidSocketException(-25);
                    }
                } catch (ErrnoException e2) {
                    Log.e(TAG, "Get peername fail: ", e2);
                    throw new SocketKeepalive.InvalidSocketException(-25, e2);
                }
            } else {
                Log.e(TAG, "Invalid or mismatched SocketAddress");
                throw new SocketKeepalive.InvalidSocketException(-25);
            }
        } catch (ErrnoException e3) {
            Log.e(TAG, "Get sockname fail: ", e3);
            throw new SocketKeepalive.InvalidSocketException(-25, e3);
        }
    }

    private static void switchOutOfRepairMode(FileDescriptor fd) {
        try {
            Os.setsockoptInt(fd, OsConstants.IPPROTO_TCP, 19, 0);
        } catch (ErrnoException e) {
            Log.e(TAG, "Cannot switch socket out of repair mode", e);
        }
    }

    public void startSocketMonitor(FileDescriptor fd, KeepaliveTracker.KeepaliveInfo ki, int slot) throws IllegalArgumentException, SocketKeepalive.InvalidSocketException {
        synchronized (this.mListeners) {
            if (this.mListeners.get(slot) == null) {
                for (int i = 0; i < this.mListeners.size(); i++) {
                    if (fd.equals(this.mListeners.valueAt(i))) {
                        Log.e(TAG, "This fd is already registered.");
                        throw new SocketKeepalive.InvalidSocketException(-25);
                    }
                }
                this.mFdHandlerQueue.addOnFileDescriptorEventListener(fd, 5, new MessageQueue.OnFileDescriptorEventListener() {
                    /* class com.android.server.connectivity.$$Lambda$TcpKeepaliveController$mLZJWrEAOnfgV5N3ZSa2J3iTmxE */

                    @Override // android.os.MessageQueue.OnFileDescriptorEventListener
                    public final int onFileDescriptorEvents(FileDescriptor fileDescriptor, int i) {
                        return TcpKeepaliveController.lambda$startSocketMonitor$0(KeepaliveTracker.KeepaliveInfo.this, fileDescriptor, i);
                    }
                });
                this.mListeners.put(slot, fd);
            } else {
                throw new IllegalArgumentException("This slot is already taken");
            }
        }
    }

    static /* synthetic */ int lambda$startSocketMonitor$0(KeepaliveTracker.KeepaliveInfo ki, FileDescriptor readyFd, int events) {
        int reason;
        if ((events & 4) != 0) {
            reason = -25;
        } else {
            reason = -2;
        }
        ki.onFileDescriptorInitiatedStop(reason);
        return 0;
    }

    public void stopSocketMonitor(int slot) {
        synchronized (this.mListeners) {
            FileDescriptor fd = this.mListeners.get(slot);
            if (fd != null) {
                this.mListeners.remove(slot);
                this.mFdHandlerQueue.removeOnFileDescriptorEventListener(fd);
                switchOutOfRepairMode(fd);
            }
        }
    }

    private static byte[] getAddress(InetSocketAddress inetAddr) {
        return inetAddr.getAddress().getAddress();
    }

    private static int getPort(InetSocketAddress inetAddr) {
        return inetAddr.getPort();
    }

    private static boolean isSocketIdle(FileDescriptor fd) throws ErrnoException {
        return isReceiveQueueEmpty(fd) && isSendQueueEmpty(fd);
    }

    private static boolean isReceiveQueueEmpty(FileDescriptor fd) throws ErrnoException {
        Int32Ref result = new Int32Ref(-1);
        Os.ioctlInt(fd, SIOCINQ, result);
        if (result.value == 0) {
            return true;
        }
        Log.e(TAG, "Read queue has data");
        return false;
    }

    private static boolean isSendQueueEmpty(FileDescriptor fd) throws ErrnoException {
        Int32Ref result = new Int32Ref(-1);
        Os.ioctlInt(fd, SIOCOUTQ, result);
        if (result.value == 0) {
            return true;
        }
        Log.e(TAG, "Write queue has data");
        return false;
    }

    private static void dropAllIncomingPackets(FileDescriptor fd, boolean enable) throws SocketKeepalive.InvalidSocketException {
        if (enable) {
            try {
                NetworkUtils.attachDropAllBPFFilter(fd);
            } catch (SocketException e) {
                Log.e(TAG, "Socket Exception");
                throw new SocketKeepalive.InvalidSocketException(-25, e);
            }
        } else {
            NetworkUtils.detachBPFFilter(fd);
        }
    }
}
