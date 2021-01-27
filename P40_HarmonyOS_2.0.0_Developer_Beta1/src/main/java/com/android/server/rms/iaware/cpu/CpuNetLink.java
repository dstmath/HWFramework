package com.android.server.rms.iaware.cpu;

import android.os.Process;
import android.rms.iaware.AwareLog;
import android.system.ErrnoException;
import com.huawei.android.net.netlink.NetlinkConstantsEx;
import com.huawei.android.net.netlink.NetlinkSocketEx;
import com.huawei.android.net.netlink.StructNdMsgEx;
import com.huawei.android.net.netlink.StructNlMsgHdrEx;
import com.huawei.android.system.OsEx;
import com.huawei.libcore.io.IoUtilsEx;
import java.io.FileDescriptor;
import java.io.InterruptedIOException;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

public class CpuNetLink {
    private static final int BG_HIGH_MSG = 3;
    private static final int BG_LOW_MSG = 4;
    private static final int BUFF_SIZE = 8192;
    private static final int CPU_HIGH_LOAD_MSG = 1;
    private static final int CPU_LOAD_MSG = 5;
    private static final int FG_HIGH_MSG = 5;
    private static final int MAX_LENGTH = 1024;
    private static final int NATIVE_HIGH_MSG = 1;
    private static final int NATIVE_LOW_MSG = 2;
    private static final int PID_LENGTH = 16;
    private static final int PROC_AUX_COMM_FORK_MSG = 6;
    private static final int PROC_AUX_COMM_MSG = 4;
    private static final int PROC_AUX_COMM_REMOVE_MSG = 7;
    private static final int PROC_COMM_MSG = 3;
    private static final int PROC_CPUMASK_BIG_MSG = 8;
    private static final int PROC_FORK_MSG = 2;
    private static final int SOCKET_PORT = 33;
    private static final String TAG = "CpuNetLink";
    private static final long TIMEOUT = 0;
    private static FileDescriptor sFileDescriptor;
    private volatile boolean mIsStart;
    private NetlinkSocketEx mLocalNetlinkSocket = null;
    private ReceiveKernelThread mReceviveKernelThread;
    private Thread mThread;

    /* access modifiers changed from: private */
    public static class RecvData {
        List<Integer> data;
        int len;
        int what;

        private RecvData() {
        }
    }

    private byte[] newMsgStruct(int seqNo) {
        int length = StructNdMsgEx.STRUCT_SIZE + 16;
        byte[] bytes = new byte[length];
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        byteBuffer.order(ByteOrder.nativeOrder());
        StructNlMsgHdrEx nlmsghdr = new StructNlMsgHdrEx();
        nlmsghdr.setNlmsgLen(length);
        nlmsghdr.setNlmsgType(NetlinkConstantsEx.RTM_GETNEIGH);
        nlmsghdr.setNlmsgFlag(769);
        nlmsghdr.setNlmsgSeq(seqNo);
        nlmsghdr.setNlmsgPid(Process.myPid());
        nlmsghdr.pack(byteBuffer);
        new StructNdMsgEx().pack(byteBuffer);
        return bytes;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void parse(ByteBuffer byteBuffer) {
        StructNlMsgHdrEx nlmsghdr = StructNlMsgHdrEx.parse(byteBuffer);
        if (!(nlmsghdr == null || nlmsghdr.isNlMsgHdrNull() || byteBuffer.remaining() < 8)) {
            RecvData recvData = new RecvData();
            recvData.what = byteBuffer.getInt();
            recvData.len = byteBuffer.getInt();
            int len = recvData.len;
            if (len > 0 && len <= 1024 && byteBuffer.remaining() >= len * 4) {
                recvData.data = new ArrayList(len);
                for (int i = 0; i < len; i++) {
                    recvData.data.add(Integer.valueOf(byteBuffer.getInt()));
                }
                handleData(recvData);
            }
        }
    }

    private void handleCpuLoadHighData(RecvData recvData) {
        if (recvData.len != 17) {
            AwareLog.w(TAG, "err data num:" + recvData.len + " for proc comm connector, expect: 17");
            return;
        }
        int msg = recvData.data.get(0).intValue();
        AwareLog.i(TAG, "parse msg :" + msg + " recvData.len :" + recvData.len);
        ArrayList<Integer> pids = new ArrayList<>();
        for (int i = 0; i < 16; i++) {
            int pid = recvData.data.get(i + 1).intValue();
            AwareLog.d(TAG, "parse pid :" + pid);
            if (pid == 0) {
                break;
            }
            pids.add(Integer.valueOf(pid));
        }
        if (msg == 1 || msg == 2 || msg == 3 || msg == 4 || msg == 5) {
            CpuHighLoadManager.getInstance().setCpuHighLoadTaskList(msg, pids);
        }
    }

    private void handleCpuMaskBig(RecvData recvData) {
        if (recvData.len != 1) {
            AwareLog.e(TAG, "err data num:" + recvData.len + " for cpu high load, expect 1");
            return;
        }
        CpuHighLoadManager.getInstance().setCpuMaskBig(recvData.data.get(0).intValue());
    }

    private void handleAuxCommData(RecvData recvData) {
        int msgId;
        if (recvData.len != 2) {
            AwareLog.e(TAG, "err data num:" + recvData.len + " for proc aux comm connector, expect 2");
            return;
        }
        int pid = recvData.data.get(0).intValue();
        int tgid = recvData.data.get(1).intValue();
        int delay = 0;
        if (recvData.what == 7) {
            msgId = CpuFeature.MSG_RM_AUX_THREAD;
        } else {
            msgId = CpuFeature.MSG_AUX_COMM_CHANGE;
            if (recvData.what == 6) {
                delay = AuxRtgSched.getInstance().getAuxForkDelay();
            }
        }
        AuxRtgSched.getInstance().sendAuxCommMessage(msgId, pid, tgid, delay);
    }

    private void handleData(RecvData recvData) {
        switch (recvData.what) {
            case 1:
                if (recvData.len != 1) {
                    AwareLog.e(TAG, "err data num:" + recvData.len + " for cpu high load, expect 1");
                    return;
                }
                CpuHighFgControl.getInstance().notifyLoadChange(recvData.data.get(0).intValue());
                return;
            case 2:
                if (recvData.len != 2) {
                    AwareLog.e(TAG, "err data num:" + recvData.len + " for proc fork connector, expect 2");
                    return;
                }
                VipCgroupControl.getInstance().notifyForkChange(recvData.data.get(0).intValue(), recvData.data.get(1).intValue());
                return;
            case 3:
                if (recvData.len != 2) {
                    AwareLog.e(TAG, "err data num:" + recvData.len + " for proc comm connector, expect 2");
                    return;
                }
                CpuThreadBoost.getInstance().notifyCommChange(recvData.data.get(0).intValue(), recvData.data.get(1).intValue());
                return;
            case 4:
            case 6:
            case 7:
                handleAuxCommData(recvData);
                return;
            case 5:
                handleCpuLoadHighData(recvData);
                return;
            case 8:
                handleCpuMaskBig(recvData);
                return;
            default:
                AwareLog.e(TAG, "error msg what = " + recvData.what);
                return;
        }
    }

    /* access modifiers changed from: package-private */
    public class ReceiveKernelThread implements Runnable {
        ReceiveKernelThread() {
        }

        @Override // java.lang.Runnable
        public void run() {
            Thread.currentThread().setPriority(10);
            while (CpuNetLink.this.mIsStart) {
                try {
                    ByteBuffer response = CpuNetLink.this.mLocalNetlinkSocket.recvMessage(CpuNetLink.sFileDescriptor, (int) CpuNetLink.BUFF_SIZE, 0);
                    if (response != null) {
                        if (StructNlMsgHdrEx.hasAvailableSpace(response)) {
                            CpuNetLink.this.parse(response);
                        }
                    } else {
                        return;
                    }
                } catch (ErrnoException | InterruptedIOException | IllegalArgumentException e) {
                    AwareLog.e(CpuNetLink.TAG, "ReceiveKernelThread Exception " + e.getMessage());
                    return;
                }
            }
        }
    }

    public void start() {
        if (!createImpl()) {
            AwareLog.e(TAG, "Failed to create netlink connection");
            return;
        }
        try {
            OsEx.bindNetlinkSocketAddress(sFileDescriptor);
            try {
                byte[] request = newMsgStruct(0);
                if (this.mLocalNetlinkSocket.sendMessage(sFileDescriptor, request, 0, request.length, 0) == -1) {
                    destroyImpl();
                    return;
                }
                if (this.mReceviveKernelThread == null) {
                    this.mReceviveKernelThread = new ReceiveKernelThread();
                }
                Thread thread = this.mThread;
                if (thread == null || !thread.isAlive()) {
                    this.mThread = new Thread(this.mReceviveKernelThread, "mReceviveKernelThread");
                }
                this.mIsStart = true;
                this.mThread.start();
            } catch (ErrnoException | InterruptedIOException | IllegalArgumentException e) {
                destroyImpl();
            }
        } catch (ErrnoException | SocketException e2) {
            AwareLog.e(TAG, "start Exception msg: " + e2.getMessage());
            destroyImpl();
        }
    }

    public void stop() {
        Thread thread = this.mThread;
        if (thread != null && thread.isAlive()) {
            this.mIsStart = false;
            this.mThread.interrupt();
        }
        destroyImpl();
        this.mThread = null;
    }

    private boolean createImpl() {
        NetlinkSocketEx netlinkSocketEx = this.mLocalNetlinkSocket;
        if (netlinkSocketEx != null && !netlinkSocketEx.isNetlinkSocketNull()) {
            return true;
        }
        try {
            this.mLocalNetlinkSocket = new NetlinkSocketEx();
            sFileDescriptor = this.mLocalNetlinkSocket.forProto((int) SOCKET_PORT);
            return true;
        } catch (ErrnoException e) {
            AwareLog.e(TAG, "Failed to create connection, ErrnoException");
            destroyImpl();
            return false;
        }
    }

    private void destroyImpl() {
        NetlinkSocketEx netlinkSocketEx = this.mLocalNetlinkSocket;
        if (netlinkSocketEx != null && !netlinkSocketEx.isNetlinkSocketNull()) {
            this.mLocalNetlinkSocket.resetNetlinkSocket();
            this.mLocalNetlinkSocket = null;
        }
        IoUtilsEx.closeQuietly(sFileDescriptor);
    }
}
