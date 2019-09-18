package sun.nio.ch;

import java.io.FileDescriptor;
import java.io.IOException;
import java.nio.channels.SelectableChannel;

abstract class FileDispatcher extends NativeDispatcher {
    public static final int INTERRUPTED = 2;
    public static final int LOCKED = 0;
    public static final int NO_LOCK = -1;
    public static final int RET_EX_LOCK = 1;

    /* access modifiers changed from: package-private */
    public abstract boolean canTransferToDirectly(SelectableChannel selectableChannel);

    /* access modifiers changed from: package-private */
    public abstract FileDescriptor duplicateForMapping(FileDescriptor fileDescriptor) throws IOException;

    /* access modifiers changed from: package-private */
    public abstract int force(FileDescriptor fileDescriptor, boolean z) throws IOException;

    /* access modifiers changed from: package-private */
    public abstract int lock(FileDescriptor fileDescriptor, boolean z, long j, long j2, boolean z2) throws IOException;

    /* access modifiers changed from: package-private */
    public abstract void release(FileDescriptor fileDescriptor, long j, long j2) throws IOException;

    /* access modifiers changed from: package-private */
    public abstract long size(FileDescriptor fileDescriptor) throws IOException;

    /* access modifiers changed from: package-private */
    public abstract boolean transferToDirectlyNeedsPositionLock();

    /* access modifiers changed from: package-private */
    public abstract int truncate(FileDescriptor fileDescriptor, long j) throws IOException;

    FileDispatcher() {
    }
}
