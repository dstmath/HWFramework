package android.os;

import android.net.LocalSocketAddress;

public class ChildZygoteProcess extends ZygoteProcess {
    private final int mPid;

    ChildZygoteProcess(LocalSocketAddress socketAddress, int pid) {
        super(socketAddress, null);
        this.mPid = pid;
    }

    public int getPid() {
        return this.mPid;
    }
}
