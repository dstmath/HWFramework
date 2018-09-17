package sun.nio.ch;

import java.io.IOException;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.Channel;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

public class FileLockImpl extends FileLock {
    static final /* synthetic */ boolean -assertionsDisabled = (FileLockImpl.class.desiredAssertionStatus() ^ 1);
    private volatile boolean valid = true;

    FileLockImpl(FileChannel channel, long position, long size, boolean shared) {
        super(channel, position, size, shared);
    }

    FileLockImpl(AsynchronousFileChannel channel, long position, long size, boolean shared) {
        super(channel, position, size, shared);
    }

    public boolean isValid() {
        return this.valid;
    }

    void invalidate() {
        if (-assertionsDisabled || Thread.holdsLock(this)) {
            this.valid = false;
            return;
        }
        throw new AssertionError();
    }

    public synchronized void release() throws IOException {
        Channel ch = acquiredBy();
        if (!ch.isOpen()) {
            throw new ClosedChannelException();
        } else if (this.valid) {
            if (ch instanceof FileChannelImpl) {
                ((FileChannelImpl) ch).release(this);
            } else if (ch instanceof AsynchronousFileChannelImpl) {
                ((AsynchronousFileChannelImpl) ch).release(this);
            } else {
                throw new AssertionError();
            }
            this.valid = false;
        }
    }
}
