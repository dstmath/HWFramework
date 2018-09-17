package libcore.io;

import java.io.FileDescriptor;

public final class AsynchronousCloseMonitor {
    public static native void signalBlockedThreads(FileDescriptor fileDescriptor);

    private AsynchronousCloseMonitor() {
    }
}
