package com.android.internal.os;

import android.os.Handler;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.os.ProxyFileDescriptorCallback;
import android.system.ErrnoException;
import android.system.OsConstants;
import android.util.Log;
import android.util.SparseArray;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.util.Preconditions;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ThreadFactory;

public class FuseAppLoop implements Handler.Callback {
    private static final int ARGS_POOL_SIZE = 50;
    private static final boolean DEBUG = Log.isLoggable(TAG, 3);
    private static final int FUSE_FSYNC = 20;
    private static final int FUSE_GETATTR = 3;
    private static final int FUSE_LOOKUP = 1;
    private static final int FUSE_MAX_WRITE = 131072;
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
    private final LinkedList<Args> mArgsPool = new LinkedList<>();
    @GuardedBy("mLock")
    private final BytesMap mBytesMap = new BytesMap();
    @GuardedBy("mLock")
    private final SparseArray<CallbackEntry> mCallbackMap = new SparseArray<>();
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

        private Args() {
        }
    }

    private static class BytesMap {
        final Map<Long, BytesMapEntry> mEntries;

        private BytesMap() {
            this.mEntries = new HashMap();
        }

        /* access modifiers changed from: package-private */
        public byte[] startUsing(long threadId) {
            BytesMapEntry entry = this.mEntries.get(Long.valueOf(threadId));
            if (entry == null) {
                entry = new BytesMapEntry();
                this.mEntries.put(Long.valueOf(threadId), entry);
            }
            entry.counter++;
            return entry.bytes;
        }

        /* access modifiers changed from: package-private */
        public void stopUsing(long threadId) {
            BytesMapEntry entry = this.mEntries.get(Long.valueOf(threadId));
            Preconditions.checkNotNull(entry);
            entry.counter--;
            if (entry.counter <= 0) {
                this.mEntries.remove(Long.valueOf(threadId));
            }
        }

        /* access modifiers changed from: package-private */
        public void clear() {
            this.mEntries.clear();
        }
    }

    private static class BytesMapEntry {
        byte[] bytes;
        int counter;

        private BytesMapEntry() {
            this.counter = 0;
            this.bytes = new byte[131072];
        }
    }

    private static class CallbackEntry {
        final ProxyFileDescriptorCallback callback;
        final Handler handler;
        boolean opened;

        CallbackEntry(ProxyFileDescriptorCallback callback2, Handler handler2) {
            this.callback = (ProxyFileDescriptorCallback) Preconditions.checkNotNull(callback2);
            this.handler = (Handler) Preconditions.checkNotNull(handler2);
        }

        /* access modifiers changed from: package-private */
        public long getThreadId() {
            return this.handler.getLooper().getThread().getId();
        }
    }

    public static class UnmountedException extends Exception {
    }

    /* access modifiers changed from: package-private */
    public native void native_delete(long j);

    /* access modifiers changed from: package-private */
    public native long native_new(int i);

    /* access modifiers changed from: package-private */
    public native void native_replyGetAttr(long j, long j2, long j3, long j4);

    /* access modifiers changed from: package-private */
    public native void native_replyLookup(long j, long j2, long j3, long j4);

    /* access modifiers changed from: package-private */
    public native void native_replyOpen(long j, long j2, long j3);

    /* access modifiers changed from: package-private */
    public native void native_replyRead(long j, long j2, int i, byte[] bArr);

    /* access modifiers changed from: package-private */
    public native void native_replySimple(long j, long j2, int i);

    /* access modifiers changed from: package-private */
    public native void native_replyWrite(long j, long j2, int i);

    /* access modifiers changed from: package-private */
    public native void native_start(long j);

    public FuseAppLoop(int mountPointId, ParcelFileDescriptor fd, ThreadFactory factory) {
        this.mMountPointId = mountPointId;
        factory = factory == null ? sDefaultThreadFactory : factory;
        this.mInstance = native_new(fd.detachFd());
        this.mThread = factory.newThread(new Runnable() {
            public final void run() {
                FuseAppLoop.lambda$new$0(FuseAppLoop.this);
            }
        });
        this.mThread.start();
    }

    public static /* synthetic */ void lambda$new$0(FuseAppLoop fuseAppLoop) {
        fuseAppLoop.native_start(fuseAppLoop.mInstance);
        synchronized (fuseAppLoop.mLock) {
            fuseAppLoop.native_delete(fuseAppLoop.mInstance);
            fuseAppLoop.mInstance = 0;
            fuseAppLoop.mBytesMap.clear();
        }
    }

    public int registerCallback(ProxyFileDescriptorCallback callback, Handler handler) throws FuseUnavailableMountException {
        int id;
        synchronized (this.mLock) {
            Preconditions.checkNotNull(callback);
            Preconditions.checkNotNull(handler);
            boolean z = false;
            Preconditions.checkState(this.mCallbackMap.size() < 2147483645, "Too many opened files.");
            if (Thread.currentThread().getId() != handler.getLooper().getThread().getId()) {
                z = true;
            }
            Preconditions.checkArgument(z, "Handler must be different from the current thread");
            if (this.mInstance != 0) {
                do {
                    id = this.mNextInode;
                    this.mNextInode++;
                    if (this.mNextInode < 0) {
                        this.mNextInode = 2;
                    }
                } while (this.mCallbackMap.get(id) != null);
                this.mCallbackMap.put(id, new CallbackEntry(callback, new Handler(handler.getLooper(), this)));
            } else {
                throw new FuseUnavailableMountException(this.mMountPointId);
            }
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

    /* JADX WARNING: type inference failed for: r16v0 */
    /* JADX WARNING: type inference failed for: r16v1 */
    /* JADX WARNING: type inference failed for: r16v7 */
    /* JADX WARNING: type inference failed for: r16v13 */
    /* JADX WARNING: type inference failed for: r16v19 */
    /* JADX WARNING: type inference failed for: r16v26 */
    /* JADX WARNING: type inference failed for: r16v29 */
    /* JADX WARNING: type inference failed for: r16v32 */
    /* JADX WARNING: type inference failed for: r16v35 */
    /* JADX WARNING: Multi-variable type inference failed */
    /* JADX WARNING: Removed duplicated region for block: B:147:0x01c9  */
    public boolean handleMessage(Message msg) {
        boolean z;
        long unique;
        Object obj;
        Object obj2;
        Object obj3;
        Object obj4;
        Object obj5;
        Object obj6;
        Object obj7;
        Object obj8;
        Message message = msg;
        Args args = (Args) message.obj;
        CallbackEntry entry = args.entry;
        long inode = args.inode;
        long unique2 = args.unique;
        int size = args.size;
        long offset = args.offset;
        byte[] data = args.data;
        try {
            int i = message.what;
            ? r16 = 0;
            if (i == 1) {
                z = true;
                int i2 = size;
                unique = unique2;
                long inode2 = inode;
                long inode3 = offset;
                long fileSize = entry.callback.onGetSize();
                Object obj9 = this.mLock;
                synchronized (obj9) {
                    try {
                        if (this.mInstance != 0) {
                            Object obj10 = obj9;
                            r16 = obj10;
                            native_replyLookup(this.mInstance, unique, inode2, fileSize);
                            obj2 = obj10;
                        } else {
                            obj2 = obj9;
                        }
                        recycleLocked(args);
                    } catch (Throwable th) {
                        th = th;
                        obj = r16;
                        throw th;
                    }
                }
            } else if (i != 3) {
                if (i == 18) {
                    z = true;
                    int i3 = size;
                    long j = inode;
                    long inode4 = offset;
                    entry.callback.onRelease();
                    synchronized (this.mLock) {
                        if (this.mInstance != 0) {
                            native_replySimple(this.mInstance, unique2, 0);
                        }
                        this.mBytesMap.stopUsing(entry.getThreadId());
                        recycleLocked(args);
                    }
                } else if (i != 20) {
                    switch (i) {
                        case 15:
                            byte[] data2 = data;
                            z = true;
                            long j2 = inode;
                            try {
                                int readSize = entry.callback.onRead(offset, size, data2);
                                Object obj11 = this.mLock;
                                synchronized (obj11) {
                                    try {
                                        if (this.mInstance != 0) {
                                            Object obj12 = obj11;
                                            int i4 = size;
                                            r16 = obj12;
                                            native_replyRead(this.mInstance, unique2, readSize, data2);
                                            obj6 = obj12;
                                        } else {
                                            obj6 = obj11;
                                            int i5 = size;
                                        }
                                        recycleLocked(args);
                                        break;
                                    } catch (Throwable th2) {
                                        th = th2;
                                        obj5 = r16;
                                        throw th;
                                    }
                                }
                            } catch (Exception e) {
                                e = e;
                                int i6 = size;
                                unique = unique2;
                                Exception error = e;
                                synchronized (this.mLock) {
                                }
                            }
                            break;
                        case 16:
                            try {
                                int writeSize = entry.callback.onWrite(offset, size, data);
                                Object obj13 = this.mLock;
                                synchronized (obj13) {
                                    long offset2 = offset;
                                    try {
                                        if (this.mInstance != 0) {
                                            Object obj14 = obj13;
                                            z = true;
                                            byte[] bArr = data;
                                            long j3 = inode;
                                            long inode5 = offset2;
                                            r16 = obj14;
                                            native_replyWrite(this.mInstance, unique2, writeSize);
                                            obj8 = obj14;
                                        } else {
                                            obj8 = obj13;
                                            byte[] bArr2 = data;
                                            long j4 = inode;
                                            long inode6 = offset2;
                                            z = true;
                                        }
                                        recycleLocked(args);
                                        int i7 = size;
                                        break;
                                    } catch (Throwable th3) {
                                        th = th3;
                                        obj7 = r16;
                                        throw th;
                                    }
                                }
                            } catch (Exception e2) {
                                e = e2;
                                byte[] bArr3 = data;
                                z = true;
                                long j5 = inode;
                                long inode7 = offset;
                                int i8 = size;
                                unique = unique2;
                                Exception error2 = e;
                                synchronized (this.mLock) {
                                }
                            }
                            break;
                        default:
                            try {
                                throw new IllegalArgumentException("Unknown FUSE command: " + message.what);
                            } catch (Exception e3) {
                                e = e3;
                                byte[] bArr4 = data;
                                z = true;
                                int i9 = size;
                                unique = unique2;
                                long j6 = inode;
                                long inode8 = offset;
                                Exception error22 = e;
                                synchronized (this.mLock) {
                                }
                            }
                            break;
                    }
                } else {
                    z = true;
                    int i10 = size;
                    long j7 = inode;
                    long inode9 = offset;
                    entry.callback.onFsync();
                    synchronized (this.mLock) {
                        if (this.mInstance != 0) {
                            native_replySimple(this.mInstance, unique2, 0);
                        }
                        recycleLocked(args);
                    }
                }
                unique = unique2;
            } else {
                z = true;
                int i11 = size;
                long inode10 = inode;
                long inode11 = offset;
                try {
                    long unique3 = unique2;
                    long fileSize2 = entry.callback.onGetSize();
                    try {
                        Object obj15 = this.mLock;
                        synchronized (obj15) {
                            try {
                                if (this.mInstance != 0) {
                                    Object obj16 = obj15;
                                    unique = unique3;
                                    r16 = obj16;
                                    native_replyGetAttr(this.mInstance, unique3, inode10, fileSize2);
                                    obj4 = obj16;
                                } else {
                                    obj4 = obj15;
                                    unique = unique3;
                                }
                                recycleLocked(args);
                            } catch (Throwable th4) {
                                th = th4;
                                obj3 = r16;
                                throw th;
                            }
                        }
                    } catch (Exception e4) {
                        e = e4;
                        unique = unique3;
                        Exception error222 = e;
                        synchronized (this.mLock) {
                        }
                    }
                } catch (Exception e5) {
                    e = e5;
                    unique = unique2;
                    Exception error2222 = e;
                    synchronized (this.mLock) {
                    }
                }
            }
            long j8 = unique;
        } catch (Exception e6) {
            e = e6;
            byte[] bArr5 = data;
            z = true;
            int i12 = size;
            unique = unique2;
            long j9 = inode;
            long inode12 = offset;
            Exception error22222 = e;
            synchronized (this.mLock) {
                try {
                    Log.e(TAG, "", error22222);
                    replySimpleLocked(unique, getError(error22222));
                    recycleLocked(args);
                } catch (Throwable th5) {
                    th = th5;
                    throw th;
                }
            }
        }
        return z;
        unique = unique2;
        Exception error222222 = e;
        synchronized (this.mLock) {
        }
        return z;
    }

    private void onCommand(int command, long unique, long inode, long offset, int size, byte[] data) {
        Args args;
        synchronized (this.mLock) {
            try {
                if (this.mArgsPool.size() == 0) {
                    args = new Args();
                } else {
                    args = this.mArgsPool.pop();
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
    }

    private byte[] onOpen(long unique, long inode) {
        synchronized (this.mLock) {
            try {
                CallbackEntry entry = getCallbackEntryOrThrowLocked(inode);
                if (!entry.opened) {
                    if (this.mInstance != 0) {
                        native_replyOpen(this.mInstance, unique, inode);
                        entry.opened = true;
                        byte[] startUsing = this.mBytesMap.startUsing(entry.getThreadId());
                        return startUsing;
                    }
                    return null;
                }
                throw new ErrnoException("onOpen", OsConstants.EMFILE);
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

    @GuardedBy("mLock")
    private CallbackEntry getCallbackEntryOrThrowLocked(long inode) throws ErrnoException {
        CallbackEntry entry = this.mCallbackMap.get(checkInode(inode));
        if (entry != null) {
            return entry;
        }
        throw new ErrnoException("getCallbackEntryOrThrowLocked", OsConstants.ENOENT);
    }

    @GuardedBy("mLock")
    private void recycleLocked(Args args) {
        if (this.mArgsPool.size() < 50) {
            this.mArgsPool.add(args);
        }
    }

    @GuardedBy("mLock")
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
