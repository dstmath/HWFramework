package sun.nio.ch;

import java.io.FileDescriptor;
import java.io.IOException;
import java.nio.channels.SelectableChannel;

abstract class FileDispatcher extends NativeDispatcher {
    public static final int INTERRUPTED = 2;
    public static final int LOCKED = 0;
    public static final int NO_LOCK = -1;
    public static final int RET_EX_LOCK = 1;

    abstract boolean canTransferToDirectly(SelectableChannel selectableChannel);

    abstract FileDescriptor duplicateForMapping(FileDescriptor fileDescriptor) throws IOException;

    abstract int force(FileDescriptor fileDescriptor, boolean z) throws IOException;

    abstract int lock(FileDescriptor fileDescriptor, boolean z, long j, long j2, boolean z2) throws IOException;

    abstract void release(FileDescriptor fileDescriptor, long j, long j2) throws IOException;

    abstract long size(FileDescriptor fileDescriptor) throws IOException;

    abstract boolean transferToDirectlyNeedsPositionLock();

    abstract int truncate(FileDescriptor fileDescriptor, long j) throws IOException;

    FileDispatcher() {
    }
}
