package com.android.server.rms.iaware.cpu;

import android.net.netlink.NetlinkSocket;
import android.net.netlink.StructNdMsg;
import android.net.netlink.StructNlMsgHdr;
import android.os.Process;
import android.rms.iaware.AwareLog;
import android.system.ErrnoException;
import android.system.NetlinkSocketAddress;
import com.android.server.security.trustcircle.tlv.tree.Cert;
import java.io.InterruptedIOException;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class CPUNetLink {
    private static final int SOCKET_PORT = 33;
    private static final String TAG = "CPUNetLink";
    private CPUHighFgControl mCPUHighFgControl;
    private volatile boolean mIsStart;
    private NetlinkSocket mLocalNetlinkSocket;
    private ReceiveKernelThread mReceviveKernelThread;
    private Thread mThread;

    class ReceiveKernelThread implements Runnable {
        ReceiveKernelThread() {
        }

        public void run() {
            Thread.currentThread().setPriority(10);
            while (CPUNetLink.this.mIsStart) {
                try {
                    ByteBuffer response = CPUNetLink.this.mLocalNetlinkSocket.recvMessage();
                    if (response == null) {
                        break;
                    } else if (StructNlMsgHdr.hasAvailableSpace(response)) {
                        int loadValue = CPUNetLink.this.parse(response);
                        if (loadValue != -1) {
                            CPUNetLink.this.mCPUHighFgControl.notifyLoadChange(loadValue);
                        }
                    }
                } catch (ErrnoException e) {
                    AwareLog.e(CPUNetLink.TAG, "ReceiveKernelThread ErrnoException");
                } catch (InterruptedIOException e2) {
                    AwareLog.e(CPUNetLink.TAG, "ReceiveKernelThread InterruptedIOException");
                }
            }
            CPUNetLink.this.destroyImpl();
        }
    }

    public CPUNetLink(CPUHighFgControl highLoad) {
        this.mLocalNetlinkSocket = null;
        this.mCPUHighFgControl = highLoad;
    }

    private byte[] newMsgStruct(int seqNo) {
        byte[] bytes = new byte[28];
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        byteBuffer.order(ByteOrder.nativeOrder());
        StructNlMsgHdr nlmsghdr = new StructNlMsgHdr();
        nlmsghdr.nlmsg_len = 28;
        nlmsghdr.nlmsg_type = (short) 30;
        nlmsghdr.nlmsg_flags = Cert.TAG_AUTH_PK_INFO_SIGNATURE;
        nlmsghdr.nlmsg_seq = seqNo;
        nlmsghdr.nlmsg_pid = Process.myPid();
        nlmsghdr.pack(byteBuffer);
        new StructNdMsg().pack(byteBuffer);
        return bytes;
    }

    private int parse(ByteBuffer byteBuffer) {
        if (StructNlMsgHdr.parse(byteBuffer) != null && byteBuffer.remaining() >= 4) {
            return byteBuffer.getInt();
        }
        return -1;
    }

    public void start() {
        if (createImpl()) {
            try {
                this.mLocalNetlinkSocket.bind(new NetlinkSocketAddress(Process.myPid(), 0));
                try {
                    byte[] request = newMsgStruct(0);
                    if (this.mLocalNetlinkSocket.sendMessage(request, 0, request.length)) {
                        if (this.mReceviveKernelThread == null) {
                            this.mReceviveKernelThread = new ReceiveKernelThread();
                        }
                        if (this.mThread == null || !this.mThread.isAlive()) {
                            this.mThread = new Thread(this.mReceviveKernelThread, "mReceviveKernelThread");
                        }
                        this.mIsStart = true;
                        this.mThread.start();
                        return;
                    }
                    destroyImpl();
                    return;
                } catch (ErrnoException e) {
                    destroyImpl();
                    return;
                } catch (InterruptedIOException e2) {
                    destroyImpl();
                    return;
                }
            } catch (ErrnoException e3) {
                AwareLog.e(TAG, "start ErrnoException msg: " + e3.getMessage());
                destroyImpl();
                return;
            } catch (SocketException e4) {
                AwareLog.e(TAG, "start SocketException msg: " + e4.getMessage());
                destroyImpl();
                return;
            }
        }
        AwareLog.e(TAG, "Failed to create netlink connection");
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
            this.mLocalNetlinkSocket = new NetlinkSocket(SOCKET_PORT);
            return true;
        } catch (ErrnoException e) {
            AwareLog.e(TAG, "Failed to create connection, ErrnoException");
            destroyImpl();
            return false;
        }
    }

    private void destroyImpl() {
        if (this.mLocalNetlinkSocket != null) {
            this.mLocalNetlinkSocket.close();
            this.mLocalNetlinkSocket = null;
        }
    }
}
