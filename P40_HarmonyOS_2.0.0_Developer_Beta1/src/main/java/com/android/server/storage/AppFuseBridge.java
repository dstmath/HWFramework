package com.android.server.storage;

import android.os.FileUtils;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.util.SparseArray;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.os.FuseUnavailableMountException;
import com.android.internal.util.Preconditions;
import com.android.server.NativeDaemonConnectorException;
import java.util.concurrent.CountDownLatch;
import libcore.io.IoUtils;

public class AppFuseBridge implements Runnable {
    private static final String APPFUSE_MOUNT_NAME_TEMPLATE = "/mnt/appfuse/%d_%d";
    public static final String TAG = "AppFuseBridge";
    @GuardedBy({"this"})
    private long mNativeLoop = native_new();
    @GuardedBy({"this"})
    private final SparseArray<MountScope> mScopes = new SparseArray<>();

    private native int native_add_bridge(long j, int i, int i2);

    private native void native_delete(long j);

    private native long native_new();

    private native void native_start_loop(long j);

    public ParcelFileDescriptor addBridge(MountScope mountScope) throws FuseUnavailableMountException, NativeDaemonConnectorException {
        ParcelFileDescriptor result;
        int tempMountId = mountScope.mountId;
        try {
            synchronized (this.mScopes) {
                Preconditions.checkArgument(this.mScopes.indexOfKey(mountScope.mountId) < 0);
                this.mScopes.put(mountScope.mountId, mountScope);
            }
            synchronized (this) {
                if (this.mNativeLoop != 0) {
                    int fd = native_add_bridge(this.mNativeLoop, mountScope.mountId, mountScope.open().detachFd());
                    if (fd != -1) {
                        result = ParcelFileDescriptor.adoptFd(fd);
                        Log.i(TAG, "addBridge, add mountScope mountId = " + mountScope.mountId);
                    } else {
                        throw new FuseUnavailableMountException(mountScope.mountId);
                    }
                } else {
                    throw new FuseUnavailableMountException(mountScope.mountId);
                }
            }
            IoUtils.closeQuietly((AutoCloseable) null);
            return result;
        } catch (FuseUnavailableMountException e) {
            Log.w(TAG, "rethrow FuseUnavailableMountException exception");
            synchronized (this.mScopes) {
                this.mScopes.remove(tempMountId);
                throw e;
            }
        } catch (Throwable th) {
            IoUtils.closeQuietly(mountScope);
            throw th;
        }
    }

    @Override // java.lang.Runnable
    public void run() {
        native_start_loop(this.mNativeLoop);
        synchronized (this) {
            native_delete(this.mNativeLoop);
            this.mNativeLoop = 0;
        }
    }

    public ParcelFileDescriptor openFile(int mountId, int fileId, int mode) throws FuseUnavailableMountException, InterruptedException {
        MountScope scope;
        Log.i(TAG, "openFile begin, mountId = " + mountId);
        synchronized (this.mScopes) {
            scope = this.mScopes.get(mountId);
            if (scope == null) {
                throw new FuseUnavailableMountException(mountId);
            }
        }
        if (scope.waitForMount()) {
            try {
                int flags = FileUtils.translateModePfdToPosix(mode);
                Log.i(TAG, "openFile, mountId = " + mountId);
                return scope.openFile(mountId, fileId, flags);
            } catch (NativeDaemonConnectorException e) {
                throw new FuseUnavailableMountException(mountId);
            }
        } else {
            throw new FuseUnavailableMountException(mountId);
        }
    }

    private void onMount(int mountId) {
        Log.i(TAG, "onMount, mountId = " + mountId);
        synchronized (this.mScopes) {
            MountScope scope = this.mScopes.get(mountId);
            if (scope != null) {
                scope.setMountResultLocked(true);
            }
        }
    }

    private void onClosed(int mountId) {
        Log.i(TAG, "onClosed, mountId = " + mountId);
        synchronized (this.mScopes) {
            MountScope scope = this.mScopes.get(mountId);
            if (scope != null) {
                scope.setMountResultLocked(false);
                IoUtils.closeQuietly(scope);
                this.mScopes.remove(mountId);
            }
        }
    }

    public static abstract class MountScope implements AutoCloseable {
        private boolean mMountResult = false;
        private final CountDownLatch mMounted = new CountDownLatch(1);
        public final int mountId;
        public final int uid;

        public abstract ParcelFileDescriptor open() throws NativeDaemonConnectorException;

        public abstract ParcelFileDescriptor openFile(int i, int i2, int i3) throws NativeDaemonConnectorException;

        public MountScope(int uid2, int mountId2) {
            this.uid = uid2;
            this.mountId = mountId2;
        }

        /* access modifiers changed from: package-private */
        @GuardedBy({"AppFuseBridge.this"})
        public void setMountResultLocked(boolean result) {
            if (this.mMounted.getCount() != 0) {
                this.mMountResult = result;
                this.mMounted.countDown();
            }
        }

        /* access modifiers changed from: package-private */
        public boolean waitForMount() throws InterruptedException {
            this.mMounted.await();
            return this.mMountResult;
        }
    }
}
