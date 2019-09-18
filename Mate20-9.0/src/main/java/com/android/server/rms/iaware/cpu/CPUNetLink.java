package com.android.server.rms.iaware.cpu;

import android.net.netlink.NetlinkSocket;
import android.net.netlink.StructNdMsg;
import android.net.netlink.StructNlMsgHdr;
import android.os.Process;
import android.rms.iaware.AwareLog;
import android.system.ErrnoException;
import android.system.NetlinkSocketAddress;
import android.system.Os;
import java.io.FileDescriptor;
import java.io.InterruptedIOException;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import libcore.io.IoUtils;

public class CPUNetLink {
    private static final int BUFF_SIZE = 8192;
    private static final int CPU_HIGH_LOAD_MSG = 1;
    private static final int MAX_LENGTH = 1024;
    private static final int PROC_COMM_MSG = 3;
    private static final int PROC_FORK_MSG = 2;
    private static final int SOCKET_PORT = 33;
    private static final String TAG = "CPUNetLink";
    private static final long TIMEOUT = 0;
    /* access modifiers changed from: private */
    public static FileDescriptor mFileDescriptor;
    /* access modifiers changed from: private */
    public volatile boolean mIsStart;
    /* access modifiers changed from: private */
    public NetlinkSocket mLocalNetlinkSocket = null;
    private ReceiveKernelThread mReceviveKernelThread;
    private Thread mThread;

    class ReceiveKernelThread implements Runnable {
        ReceiveKernelThread() {
        }

        public void run() {
            Thread.currentThread().setPriority(10);
            while (CPUNetLink.this.mIsStart) {
                try {
                    NetlinkSocket unused = CPUNetLink.this.mLocalNetlinkSocket;
                    ByteBuffer response = NetlinkSocket.recvMessage(CPUNetLink.mFileDescriptor, 8192, 0);
                    if (response != null) {
                        if (StructNlMsgHdr.hasAvailableSpace(response)) {
                            CPUNetLink.this.parse(response);
                        }
                    } else {
                        return;
                    }
                } catch (ErrnoException e) {
                    AwareLog.e(CPUNetLink.TAG, "ReceiveKernelThread ErrnoException");
                    return;
                } catch (InterruptedIOException e2) {
                    AwareLog.e(CPUNetLink.TAG, "ReceiveKernelThread InterruptedIOException");
                    return;
                } catch (IllegalArgumentException e3) {
                    AwareLog.e(CPUNetLink.TAG, "ReceiveKernelThread IllegalArgumentException");
                    return;
                }
            }
        }
    }

    private static class RecvData {
        List<Integer> data;
        int len;
        int what;

        private RecvData() {
        }
    }

    private byte[] newMsgStruct(int seqNo) {
        byte[] bytes = new byte[28];
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        byteBuffer.order(ByteOrder.nativeOrder());
        StructNlMsgHdr nlmsghdr = new StructNlMsgHdr();
        nlmsghdr.nlmsg_len = 28;
        nlmsghdr.nlmsg_type = 30;
        nlmsghdr.nlmsg_flags = 769;
        nlmsghdr.nlmsg_seq = seqNo;
        nlmsghdr.nlmsg_pid = Process.myPid();
        nlmsghdr.pack(byteBuffer);
        new StructNdMsg().pack(byteBuffer);
        return bytes;
    }

    /* access modifiers changed from: private */
    public void parse(ByteBuffer byteBuffer) {
        if (StructNlMsgHdr.parse(byteBuffer) != null && byteBuffer.remaining() >= 8) {
            RecvData recvData = new RecvData();
            recvData.what = byteBuffer.getInt();
            recvData.len = byteBuffer.getInt();
            int len = recvData.len;
            if (len > 0 && len <= 1024 && byteBuffer.remaining() >= 4 * len) {
                recvData.data = new ArrayList(len);
                for (int i = 0; i < len; i++) {
                    recvData.data.add(Integer.valueOf(byteBuffer.getInt()));
                }
                handleData(recvData);
            }
        }
    }

    private void handleData(RecvData recvData) {
        switch (recvData.what) {
            case 1:
                if (recvData.len == 1) {
                    CPUHighFgControl.getInstance().notifyLoadChange(recvData.data.get(0).intValue());
                    break;
                } else {
                    AwareLog.e(TAG, "err data num:" + recvData.len + " for cpu high load, expect 1");
                    return;
                }
            case 2:
                if (recvData.len == 2) {
                    VipCgroupControl.getInstance().notifyForkChange(recvData.data.get(0).intValue(), recvData.data.get(1).intValue());
                    break;
                } else {
                    AwareLog.e(TAG, "err data num:" + recvData.len + " for proc fork connector, expect 2");
                    return;
                }
            case 3:
                if (recvData.len == 2) {
                    CpuThreadBoost.getInstance().notifyCommChange(recvData.data.get(0).intValue(), recvData.data.get(1).intValue());
                    break;
                } else {
                    AwareLog.e(TAG, "err data num:" + recvData.len + " for proc comm connector, expect 2");
                    return;
                }
            default:
                AwareLog.e(TAG, "error msg what = " + recvData.what);
                break;
        }
    }

    public void start() {
        if (!createImpl()) {
            AwareLog.e(TAG, "Failed to create netlink connection");
            return;
        }
        try {
            Os.bind(mFileDescriptor, new NetlinkSocketAddress(Process.myPid(), 0));
            try {
                byte[] request = newMsgStruct(0);
                NetlinkSocket netlinkSocket = this.mLocalNetlinkSocket;
                if (NetlinkSocket.sendMessage(mFileDescriptor, request, 0, request.length, 0) == -1) {
                    destroyImpl();
                    return;
                }
                if (this.mReceviveKernelThread == null) {
                    this.mReceviveKernelThread = new ReceiveKernelThread();
                }
                if (this.mThread == null || !this.mThread.isAlive()) {
                    this.mThread = new Thread(this.mReceviveKernelThread, "mReceviveKernelThread");
                }
                this.mIsStart = true;
                this.mThread.start();
            } catch (ErrnoException e) {
                destroyImpl();
            } catch (InterruptedIOException e2) {
                destroyImpl();
            } catch (IllegalArgumentException e3) {
                destroyImpl();
            }
        } catch (ErrnoException e4) {
            AwareLog.e(TAG, "start ErrnoException msg: " + e4.getMessage());
            destroyImpl();
        } catch (SocketException e5) {
            AwareLog.e(TAG, "start SocketException msg: " + e5.getMessage());
            destroyImpl();
        }
    }

    public void stop() {
        if (this.mThread != null && this.mThread.isAlive()) {
            this.mIsStart = false;
            this.mThread.interrupt();
        }
        destroyImpl();
        this.mThread = null;
    }

    private boolean createImpl() {
        if (this.mLocalNetlinkSocket != null) {
            return true;
        }
        try {
            this.mLocalNetlinkSocket = new NetlinkSocket();
            NetlinkSocket netlinkSocket = this.mLocalNetlinkSocket;
            mFileDescriptor = NetlinkSocket.forProto(33);
            return true;
        } catch (ErrnoException e) {
            AwareLog.e(TAG, "Failed to create connection, ErrnoException");
            destroyImpl();
            return false;
        }
    }

    private void destroyImpl() {
        if (this.mLocalNetlinkSocket != null) {
            this.mLocalNetlinkSocket = null;
        }
        IoUtils.closeQuietly(mFileDescriptor);
    }
}
