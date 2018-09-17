package java.nio.channels;

import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

public abstract class SelectionKey {
    public static final int OP_ACCEPT = 16;
    public static final int OP_CONNECT = 8;
    public static final int OP_READ = 1;
    public static final int OP_WRITE = 4;
    private static final AtomicReferenceFieldUpdater<SelectionKey, Object> attachmentUpdater = AtomicReferenceFieldUpdater.newUpdater(SelectionKey.class, Object.class, "attachment");
    private volatile Object attachment = null;

    public abstract void cancel();

    public abstract SelectableChannel channel();

    public abstract int interestOps();

    public abstract SelectionKey interestOps(int i);

    public abstract boolean isValid();

    public abstract int readyOps();

    public abstract Selector selector();

    protected SelectionKey() {
    }

    public final boolean isReadable() {
        return (readyOps() & 1) != 0;
    }

    public final boolean isWritable() {
        return (readyOps() & 4) != 0;
    }

    public final boolean isConnectable() {
        return (readyOps() & 8) != 0;
    }

    public final boolean isAcceptable() {
        return (readyOps() & 16) != 0;
    }

    public final Object attach(Object ob) {
        return attachmentUpdater.getAndSet(this, ob);
    }

    public final Object attachment() {
        return this.attachment;
    }
}
