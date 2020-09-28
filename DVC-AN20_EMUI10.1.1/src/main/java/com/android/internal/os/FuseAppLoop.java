package com.android.internal.os;

import android.annotation.UnsupportedAppUsage;
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
        /* class com.android.internal.os.FuseAppLoop.AnonymousClass1 */

        public Thread newThread(Runnable r) {
            return new Thread(r, FuseAppLoop.TAG);
        }
    };
    @GuardedBy({"mLock"})
    private final LinkedList<Args> mArgsPool = new LinkedList<>();
    @GuardedBy({"mLock"})
    private final BytesMap mBytesMap = new BytesMap();
    @GuardedBy({"mLock"})
    private final SparseArray<CallbackEntry> mCallbackMap = new SparseArray<>();
    @GuardedBy({"mLock"})
    private long mInstance;
    private final Object mLock = new Object();
    private final int mMountPointId;
    @GuardedBy({"mLock"})
    private int mNextInode = 2;
    private final Thread mThread;

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
            /* class com.android.internal.os.$$Lambda$FuseAppLoop$e9Yru2f_btesWlxIgerkPnHibpg */

            public final void run() {
                FuseAppLoop.this.lambda$new$0$FuseAppLoop();
            }
        });
        this.mThread.start();
    }

    public /* synthetic */ void lambda$new$0$FuseAppLoop() {
        native_start(this.mInstance);
        synchronized (this.mLock) {
            native_delete(this.mInstance);
            this.mInstance = 0;
            this.mBytesMap.clear();
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

    /* JADX DEBUG: Failed to insert an additional move for type inference into block B:19:0x003a */
    /* JADX DEBUG: Failed to insert an additional move for type inference into block B:15:0x002f */
    /* JADX DEBUG: Failed to insert an additional move for type inference into block B:105:0x017c */
    /* JADX DEBUG: Failed to insert an additional move for type inference into block B:129:0x01bf */
    /* JADX DEBUG: Multi-variable search result rejected for r0v36, resolved type: android.os.ProxyFileDescriptorCallback */
    /* JADX DEBUG: Multi-variable search result rejected for r0v46, resolved type: android.os.ProxyFileDescriptorCallback */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r7v0, types: [int] */
    /* JADX WARN: Type inference failed for: r16v0 */
    /* JADX WARN: Type inference failed for: r16v1 */
    /* JADX WARN: Type inference failed for: r16v6 */
    /* JADX WARN: Type inference failed for: r16v7 */
    /* JADX WARN: Type inference failed for: r16v12 */
    /* JADX WARN: Type inference failed for: r7v5 */
    /* JADX WARN: Type inference failed for: r16v14 */
    /* JADX WARN: Type inference failed for: r7v6 */
    /* JADX WARN: Type inference failed for: r7v7 */
    /* JADX WARN: Type inference failed for: r7v8 */
    /* JADX WARN: Type inference failed for: r16v19 */
    /* JADX WARN: Type inference failed for: r7v11 */
    /* JADX WARN: Type inference failed for: r16v20 */
    /* JADX WARN: Type inference failed for: r16v21 */
    /* JADX WARN: Type inference failed for: r7v13 */
    /* JADX WARNING: Removed duplicated region for block: B:150:0x01f6  */
    @Override // android.os.Handler.Callback
    public boolean handleMessage(Message msg) {
        boolean z;
        long unique;
        Object obj;
        Object obj2;
        Object obj3;
        Object obj4;
        Object obj5;
        boolean z2;
        Object obj6;
        Object obj7;
        byte[] bArr;
        Args args = (Args) msg.obj;
        CallbackEntry entry = args.entry;
        long inode = args.inode;
        long unique2 = args.unique;
        ?? r7 = args.size;
        long offset = args.offset;
        byte[] data = args.data;
        try {
            int i = msg.what;
            ?? r16 = 0;
            ?? r162 = 0;
            ?? r163 = 0;
            if (i == 1) {
                z = true;
                unique = unique2;
                long fileSize = entry.callback.onGetSize();
                Object obj8 = this.mLock;
                synchronized (obj8) {
                    try {
                        if (this.mInstance != 0) {
                            r16 = obj8;
                            native_replyLookup(this.mInstance, unique, inode, fileSize);
                            obj2 = r16;
                        } else {
                            obj2 = obj8;
                        }
                        recycleLocked(args);
                    } catch (Throwable th) {
                        th = th;
                        obj = r16;
                        throw th;
                    }
                }
            } else if (i == 3) {
                z = true;
                try {
                    long fileSize2 = entry.callback.onGetSize();
                    try {
                        Object obj9 = this.mLock;
                        synchronized (obj9) {
                            try {
                                if (this.mInstance != 0) {
                                    r163 = obj9;
                                    unique = unique2;
                                    native_replyGetAttr(this.mInstance, unique2, inode, fileSize2);
                                    obj4 = r163;
                                } else {
                                    obj4 = obj9;
                                    unique = unique2;
                                }
                                recycleLocked(args);
                            } catch (Throwable th2) {
                                th = th2;
                                obj3 = r163;
                                throw th;
                            }
                        }
                    } catch (Exception e) {
                        error = e;
                        unique = unique2;
                        synchronized (this.mLock) {
                        }
                    }
                } catch (Exception e2) {
                    error = e2;
                    unique = unique2;
                    synchronized (this.mLock) {
                    }
                }
            } else if (i == 18) {
                z = true;
                entry.callback.onRelease();
                synchronized (this.mLock) {
                    if (this.mInstance != 0) {
                        native_replySimple(this.mInstance, unique2, 0);
                    }
                    this.mBytesMap.stopUsing(entry.getThreadId());
                    recycleLocked(args);
                }
                unique = unique2;
            } else if (i == 20) {
                z = true;
                entry.callback.onFsync();
                synchronized (this.mLock) {
                    if (this.mInstance != 0) {
                        native_replySimple(this.mInstance, unique2, 0);
                    }
                    recycleLocked(args);
                }
                unique = unique2;
            } else if (i == 15) {
                z = true;
                try {
                    try {
                        int readSize = entry.callback.onRead(offset, r7, data);
                        Object obj10 = this.mLock;
                        synchronized (obj10) {
                            try {
                                if (this.mInstance != 0) {
                                    obj5 = obj10;
                                    native_replyRead(this.mInstance, unique2, readSize, data);
                                } else {
                                    obj5 = obj10;
                                }
                                recycleLocked(args);
                                unique = unique2;
                            } catch (Throwable th3) {
                                th = th3;
                                throw th;
                            }
                        }
                    } catch (Exception e3) {
                        error = e3;
                        unique = unique2;
                        synchronized (this.mLock) {
                            try {
                                Log.e(TAG, "", error);
                                replySimpleLocked(unique, getError(error));
                                recycleLocked(args);
                            } catch (Throwable th4) {
                                th = th4;
                                throw th;
                            }
                        }
                    }
                } catch (Exception e4) {
                    error = e4;
                    unique = unique2;
                    synchronized (this.mLock) {
                    }
                }
            } else if (i == 16) {
                try {
                    int writeSize = entry.callback.onWrite(offset, r7, data);
                    Object obj11 = this.mLock;
                    synchronized (obj11) {
                        try {
                            if (this.mInstance != 0) {
                                r162 = obj11;
                                z2 = true;
                                z2 = true;
                                z = true;
                                r7 = data;
                                native_replyWrite(this.mInstance, unique2, writeSize);
                                bArr = r7;
                                obj7 = r162;
                            } else {
                                obj7 = obj11;
                                z = true;
                                bArr = data;
                            }
                            recycleLocked(args);
                            unique = unique2;
                        } catch (Throwable th5) {
                            th = th5;
                            r7 = r7;
                            obj6 = r162;
                            throw th;
                        }
                    }
                } catch (Exception e5) {
                    error = e5;
                    z = true;
                    unique = unique2;
                    synchronized (this.mLock) {
                    }
                }
            } else {
                throw new IllegalArgumentException("Unknown FUSE command: " + msg.what);
            }
        } catch (Exception e6) {
            error = e6;
            z = true;
            unique = unique2;
            synchronized (this.mLock) {
            }
        }
        return z;
        return z;
    }

    @UnsupportedAppUsage
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

    @UnsupportedAppUsage
    private byte[] onOpen(long unique, long inode) {
        synchronized (this.mLock) {
            try {
                CallbackEntry entry = getCallbackEntryOrThrowLocked(inode);
                if (!entry.opened) {
                    if (this.mInstance != 0) {
                        native_replyOpen(this.mInstance, unique, inode);
                        entry.opened = true;
                        return this.mBytesMap.startUsing(entry.getThreadId());
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
        int errno;
        if (!(error instanceof ErrnoException) || (errno = ((ErrnoException) error).errno) == OsConstants.ENOSYS) {
            return -OsConstants.EBADF;
        }
        return -errno;
    }

    @GuardedBy({"mLock"})
    private CallbackEntry getCallbackEntryOrThrowLocked(long inode) throws ErrnoException {
        CallbackEntry entry = this.mCallbackMap.get(checkInode(inode));
        if (entry != null) {
            return entry;
        }
        throw new ErrnoException("getCallbackEntryOrThrowLocked", OsConstants.ENOENT);
    }

    @GuardedBy({"mLock"})
    private void recycleLocked(Args args) {
        if (this.mArgsPool.size() < 50) {
            this.mArgsPool.add(args);
        }
    }

    @GuardedBy({"mLock"})
    private void replySimpleLocked(long unique, int result) {
        long j = this.mInstance;
        if (j != 0) {
            native_replySimple(j, unique, result);
        }
    }

    private static int checkInode(long inode) {
        Preconditions.checkArgumentInRange(inode, 2, 2147483647L, "checkInode");
        return (int) inode;
    }

    /* access modifiers changed from: private */
    public static class CallbackEntry {
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

    /* access modifiers changed from: private */
    public static class BytesMapEntry {
        byte[] bytes;
        int counter;

        private BytesMapEntry() {
            this.counter = 0;
            this.bytes = new byte[131072];
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

    /* access modifiers changed from: private */
    public static class Args {
        byte[] data;
        CallbackEntry entry;
        long inode;
        long offset;
        int size;
        long unique;

        private Args() {
        }
    }
}
