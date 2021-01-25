package com.huawei.android.system;

import android.os.Process;
import android.system.ErrnoException;
import android.system.NetlinkSocketAddress;
import android.system.Os;
import java.io.FileDescriptor;
import java.net.SocketAddress;
import java.net.SocketException;

public class OsEx {
    public static int ioctlInt(FileDescriptor fd, int cmd, Int32RefEx arg) throws ErrnoException {
        return Os.ioctlInt(fd, cmd, arg.getInner());
    }

    public static void bindNetlinkSocketAddress(FileDescriptor fd) throws ErrnoException, SocketException {
        NetlinkSocketAddress localAddr = new NetlinkSocketAddress(Process.myPid(), 0);
        if (localAddr instanceof SocketAddress) {
            Os.bind(fd, localAddr);
        }
    }
}
