package com.android.internal.os;

import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.os.ProxyFileDescriptorCallback;
import android.system.ErrnoException;
import android.system.OsConstants;
import android.util.Log;
import android.util.LogException;
import android.util.SparseArray;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.util.Preconditions;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ThreadFactory;

public class FuseAppLoop implements Callback {
    private static final int ARGS_POOL_SIZE = 50;
    private static final boolean DEBUG = Log.isLoggable(TAG, 3);
    private static final int FUSE_FSYNC = 20;
    private static final int FUSE_GETATTR = 3;
    private static final int FUSE_LOOKUP = 1;
    private static final int FUSE_MAX_WRITE = 262144;
    private static final int FUSE_OK = 0;
    private static final int FUSE_OPEN = 14;
    private static final int FUSE_READ = 15;
    private static final int FUSE_RELEASE = 18;
    private static final int FUSE_WRITE = 16;
    private static final int MIN_INODE = 2;
    public static final int ROOT_INODE = 1;
    private static final String TAG = "FuseAppLoop";
    private static final ThreadFactory sDefaultThreadFactory = new ThreadFactory() {
        public Thread newThread(Runnable r) {
            return new Thread(r, FuseAppLoop.TAG);
        }
    };
    @GuardedBy("mLock")
    private final LinkedList<Args> mArgsPool = new LinkedList();
    @GuardedBy("mLock")
    private final BytesMap mBytesMap = new BytesMap();
    @GuardedBy("mLock")
    private final SparseArray<CallbackEntry> mCallbackMap = new SparseArray();
    @GuardedBy("mLock")
    private long mInstance;
    private final Object mLock = new Object();
    private final int mMountPointId;
    @GuardedBy("mLock")
    private int mNextInode = 2;
    private final Thread mThread;

    private static class Args {
        byte[] data;
        CallbackEntry entry;
        long inode;
        long offset;
        int size;
        long unique;

        /* synthetic */ Args(Args -this0) {
            this();
        }

        private Args() {
        }
    }

    private static class BytesMap {
        final Map<Long, BytesMapEntry> mEntries;

        /* synthetic */ BytesMap(BytesMap -this0) {
            this();
        }

        private BytesMap() {
            this.mEntries = new HashMap();
        }

        byte[] startUsing(long threadId) {
            BytesMapEntry entry = (BytesMapEntry) this.mEntries.get(Long.valueOf(threadId));
            if (entry == null) {
                entry = new BytesMapEntry();
                this.mEntries.put(Long.valueOf(threadId), entry);
            }
            entry.counter++;
            return entry.bytes;
        }

        void stopUsing(long threadId) {
            BytesMapEntry entry = (BytesMapEntry) this.mEntries.get(Long.valueOf(threadId));
            Preconditions.checkNotNull(entry);
            entry.counter--;
            if (entry.counter <= 0) {
                this.mEntries.remove(Long.valueOf(threadId));
            }
        }

        void clear() {
            this.mEntries.clear();
        }
    }

    private static class BytesMapEntry {
        byte[] bytes;
        int counter;

        /* synthetic */ BytesMapEntry(BytesMapEntry -this0) {
            this();
        }

        private BytesMapEntry() {
            this.counter = 0;
            this.bytes = new byte[262144];
        }
    }

    private static class CallbackEntry {
        final ProxyFileDescriptorCallback callback;
        final Handler handler;
        boolean opened;

        CallbackEntry(ProxyFileDescriptorCallback callback, Handler handler) {
            this.callback = (ProxyFileDescriptorCallback) Preconditions.checkNotNull(callback);
            this.handler = (Handler) Preconditions.checkNotNull(handler);
        }

        long getThreadId() {
            return this.handler.getLooper().getThread().getId();
        }
    }

    public static class UnmountedException extends Exception {
    }

    native void native_delete(long j);

    native long native_new(int i);

    native void native_replyGetAttr(long j, long j2, long j3, long j4);

    native void native_replyLookup(long j, long j2, long j3, long j4);

    native void native_replyOpen(long j, long j2, long j3);

    native void native_replyRead(long j, long j2, int i, byte[] bArr);

    native void native_replySimple(long j, long j2, int i);

    native void native_replyWrite(long j, long j2, int i);

    native void native_start(long j);

    public FuseAppLoop(int mountPointId, ParcelFileDescriptor fd, ThreadFactory factory) {
        this.mMountPointId = mountPointId;
        if (factory == null) {
            factory = sDefaultThreadFactory;
        }
        this.mInstance = native_new(fd.detachFd());
        this.mThread = factory.newThread(new -$Lambda$7ZK-l4tRY1lJoOPMxlJZMSKtyQY(this));
        this.mThread.start();
    }

    /* synthetic */ void lambda$-com_android_internal_os_FuseAppLoop_2801() {
        native_start(this.mInstance);
        synchronized (this.mLock) {
            native_delete(this.mInstance);
            this.mInstance = 0;
            this.mBytesMap.clear();
        }
    }

    public int registerCallback(ProxyFileDescriptorCallback callback, Handler handler) throws FuseUnavailableMountException {
        int id;
        boolean z = true;
        synchronized (this.mLock) {
            boolean z2;
            Preconditions.checkNotNull(callback);
            Preconditions.checkNotNull(handler);
            if (this.mCallbackMap.size() < 2147483645) {
                z2 = true;
            } else {
                z2 = false;
            }
            Preconditions.checkState(z2, "Too many opened files.");
            if (Thread.currentThread().getId() == handler.getLooper().getThread().getId()) {
                z = false;
            }
            Preconditions.checkArgument(z, "Handler must be different from the current thread");
            if (this.mInstance == 0) {
                throw new FuseUnavailableMountException(this.mMountPointId);
            }
            do {
                id = this.mNextInode;
                this.mNextInode++;
                if (this.mNextInode < 0) {
                    this.mNextInode = 2;
                }
            } while (this.mCallbackMap.get(id) != null);
            this.mCallbackMap.put(id, new CallbackEntry(callback, new Handler(handler.getLooper(), this)));
        }
        return id;
    }

    public void unregisterCallback(int id) {
        synchronized (this.mLock) {
            this.mCallbackMap.remove(id);
        }
    }

    public int getMountPointId() {
        return this.mMountPointId;
    }

    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean handleMessage(Message msg) {
        Args args = msg.obj;
        CallbackEntry entry = args.entry;
        long inode = args.inode;
        long unique = args.unique;
        int size = args.size;
        long offset = args.offset;
        byte[] data = args.data;
        try {
            long fileSize;
            switch (msg.what) {
                case 1:
                    fileSize = entry.callback.onGetSize();
                    synchronized (this.mLock) {
                        if (this.mInstance != 0) {
                            native_replyLookup(this.mInstance, unique, inode, fileSize);
                        }
                        recycleLocked(args);
                    }
                case 3:
                    fileSize = entry.callback.onGetSize();
                    synchronized (this.mLock) {
                        if (this.mInstance != 0) {
                            native_replyGetAttr(this.mInstance, unique, inode, fileSize);
                        }
                        recycleLocked(args);
                    }
                case 15:
                    int readSize = entry.callback.onRead(offset, size, data);
                    synchronized (this.mLock) {
                        if (this.mInstance != 0) {
                            native_replyRead(this.mInstance, unique, readSize, data);
                        }
                        recycleLocked(args);
                    }
                case 16:
                    int writeSize = entry.callback.onWrite(offset, size, data);
                    synchronized (this.mLock) {
                        if (this.mInstance != 0) {
                            native_replyWrite(this.mInstance, unique, writeSize);
                        }
                        recycleLocked(args);
                    }
                case 18:
                    entry.callback.onRelease();
                    synchronized (this.mLock) {
                        if (this.mInstance != 0) {
                            native_replySimple(this.mInstance, unique, 0);
                        }
                        this.mBytesMap.stopUsing(entry.getThreadId());
                        recycleLocked(args);
                    }
                case 20:
                    entry.callback.onFsync();
                    synchronized (this.mLock) {
                        if (this.mInstance != 0) {
                            native_replySimple(this.mInstance, unique, 0);
                        }
                        recycleLocked(args);
                    }
                default:
                    throw new IllegalArgumentException("Unknown FUSE command: " + msg.what);
            }
        } catch (Throwable error) {
            synchronized (this.mLock) {
                Log.e(TAG, LogException.NO_VALUE, error);
                replySimpleLocked(unique, getError(error));
                recycleLocked(args);
            }
        }
        return true;
    }

    private void onCommand(int command, long unique, long inode, long offset, int size, byte[] data) {
        synchronized (this.mLock) {
            try {
                Args args;
                if (this.mArgsPool.size() == 0) {
                    args = new Args();
                } else {
                    args = (Args) this.mArgsPool.pop();
                }
                args.unique = unique;
                args.inode = inode;
                args.offset = offset;
                args.size = size;
                args.data = data;
                args.entry = getCallbackEntryOrThrowLocked(inode);
                if (!args.entry.handler.sendMessage(Message.obtain(args.entry.handler, command, 0, 0, args))) {
                    throw new ErrnoException("onCommand", OsConstants.EBADF);
                }
            } catch (Exception error) {
                replySimpleLocked(unique, getError(error));
            }
        }
        return;
    }

    private byte[] onOpen(long unique, long inode) {
        synchronized (this.mLock) {
            try {
                CallbackEntry entry = getCallbackEntryOrThrowLocked(inode);
                if (entry.opened) {
                    throw new ErrnoException("onOpen", OsConstants.EMFILE);
                }
                if (this.mInstance != 0) {
                    native_replyOpen(this.mInstance, unique, inode);
                    entry.opened = true;
                    byte[] startUsing = this.mBytesMap.startUsing(entry.getThreadId());
                    return startUsing;
                }
                return null;
            } catch (ErrnoException error) {
                replySimpleLocked(unique, getError(error));
            }
        }
    }

    private static int getError(Exception error) {
        if (error instanceof ErrnoException) {
            int errno = ((ErrnoException) error).errno;
            if (errno != OsConstants.ENOSYS) {
                return -errno;
            }
        }
        return -OsConstants.EBADF;
    }

    private CallbackEntry getCallbackEntryOrThrowLocked(long inode) throws ErrnoException {
        CallbackEntry entry = (CallbackEntry) this.mCallbackMap.get(checkInode(inode));
        if (entry != null) {
            return entry;
        }
        throw new ErrnoException("getCallbackEntryOrThrowLocked", OsConstants.ENOENT);
    }

    private void recycleLocked(Args args) {
        if (this.mArgsPool.size() < 50) {
            this.mArgsPool.add(args);
        }
    }

    private void replySimpleLocked(long unique, int result) {
        if (this.mInstance != 0) {
            native_replySimple(this.mInstance, unique, result);
        }
    }

    private static int checkInode(long inode) {
        Preconditions.checkArgumentInRange(inode, 2, 2147483647L, "checkInode");
        return (int) inode;
    }
}
