package com.huawei.android.net.netlink;

import android.net.netlink.NetlinkSocket;
import android.system.ErrnoException;
import com.huawei.annotation.HwSystemApi;
import java.io.FileDescriptor;
import java.io.InterruptedIOException;
import java.nio.ByteBuffer;

@HwSystemApi
public class NetlinkSocketEx {
    private NetlinkSocket mNetlinkSocket = new NetlinkSocket();

    public boolean isNetlinkSocketNull() {
        return this.mNetlinkSocket == null;
    }

    public void resetNetlinkSocket() {
        this.mNetlinkSocket = null;
    }

    public FileDescriptor forProto(int nlProto) throws ErrnoException {
        NetlinkSocket netlinkSocket = this.mNetlinkSocket;
        return NetlinkSocket.forProto(nlProto);
    }

    public int sendMessage(FileDescriptor fd, byte[] bytes, int offset, int count, long timeoutMs) throws ErrnoException, IllegalArgumentException, InterruptedIOException {
        NetlinkSocket netlinkSocket = this.mNetlinkSocket;
        return NetlinkSocket.sendMessage(fd, bytes, offset, count, timeoutMs);
    }

    public ByteBuffer recvMessage(FileDescriptor fd, int bufSize, long timeoutMs) throws ErrnoException, IllegalArgumentException, InterruptedIOException {
        NetlinkSocket netlinkSocket = this.mNetlinkSocket;
        return NetlinkSocket.recvMessage(fd, bufSize, timeoutMs);
    }
}
