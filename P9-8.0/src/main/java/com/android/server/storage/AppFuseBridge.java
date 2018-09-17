package com.android.server.storage;

import android.os.ParcelFileDescriptor;
import android.util.SparseArray;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.os.FuseUnavailableMountException;
import com.android.internal.util.Preconditions;
import com.android.server.NativeDaemonConnectorException;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.concurrent.CountDownLatch;
import libcore.io.IoUtils;

public class AppFuseBridge implements Runnable {
    private static final String APPFUSE_MOUNT_NAME_TEMPLATE = "/mnt/appfuse/%d_%d";
    public static final String TAG = "AppFuseBridge";
    @GuardedBy("this")
    private long mNativeLoop = native_new();
    @GuardedBy("this")
    private final SparseArray<MountScope> mScopes = new SparseArray();

    public static abstract class MountScope implements AutoCloseable {
        private boolean mMountResult = false;
        private final CountDownLatch mMounted = new CountDownLatch(1);
        public final int mountId;
        public final File mountPoint;
        public final int pid;
        public final int uid;

        public abstract ParcelFileDescriptor open() throws NativeDaemonConnectorException;

        public MountScope(int uid, int pid, int mountId) {
            this.uid = uid;
            this.pid = pid;
            this.mountId = mountId;
            this.mountPoint = new File(String.format(AppFuseBridge.APPFUSE_MOUNT_NAME_TEMPLATE, new Object[]{Integer.valueOf(uid), Integer.valueOf(mountId)}));
        }

        @GuardedBy("AppFuseBridge.this")
        void setMountResultLocked(boolean result) {
            if (this.mMounted.getCount() != 0) {
                this.mMountResult = result;
                this.mMounted.countDown();
            }
        }

        boolean waitForMount() throws InterruptedException {
            this.mMounted.await();
            return this.mMountResult;
        }
    }

    private native int native_add_bridge(long j, int i, int i2);

    private native void native_delete(long j);

    private native long native_new();

    private native void native_start_loop(long j);

    public ParcelFileDescriptor addBridge(MountScope mountScope) throws FuseUnavailableMountException, NativeDaemonConnectorException {
        AutoCloseable mountScope2;
        boolean z = false;
        try {
            ParcelFileDescriptor result;
            synchronized (this) {
                if (this.mScopes.indexOfKey(mountScope.mountId) < 0) {
                    z = true;
                }
                Preconditions.checkArgument(z);
                if (this.mNativeLoop == 0) {
                    throw new FuseUnavailableMountException(mountScope.mountId);
                }
                int fd = native_add_bridge(this.mNativeLoop, mountScope.mountId, mountScope.open().detachFd());
                if (fd == -1) {
                    throw new FuseUnavailableMountException(mountScope.mountId);
                }
                result = ParcelFileDescriptor.adoptFd(fd);
                this.mScopes.put(mountScope.mountId, mountScope);
                mountScope2 = null;
            }
            return result;
        } finally {
            IoUtils.closeQuietly(mountScope2);
        }
    }

    public void run() {
        native_start_loop(this.mNativeLoop);
        synchronized (this) {
            native_delete(this.mNativeLoop);
            this.mNativeLoop = 0;
        }
    }

    public ParcelFileDescriptor openFile(int pid, int mountId, int fileId, int mode) throws FuseUnavailableMountException, InterruptedException {
        MountScope scope;
        synchronized (this) {
            scope = (MountScope) this.mScopes.get(mountId);
            if (scope == null) {
                throw new FuseUnavailableMountException(mountId);
            }
        }
        if (scope.pid != pid) {
            throw new SecurityException("PID does not match");
        } else if (scope.waitForMount()) {
            try {
                return ParcelFileDescriptor.open(new File(scope.mountPoint, String.valueOf(fileId)), mode);
            } catch (FileNotFoundException e) {
                throw new FuseUnavailableMountException(mountId);
            }
        } else {
            throw new FuseUnavailableMountException(mountId);
        }
    }

    private synchronized void onMount(int mountId) {
        MountScope scope = (MountScope) this.mScopes.get(mountId);
        if (scope != null) {
            scope.setMountResultLocked(true);
        }
    }

    private synchronized void onClosed(int mountId) {
        MountScope scope = (MountScope) this.mScopes.get(mountId);
        if (scope != null) {
            scope.setMountResultLocked(false);
            IoUtils.closeQuietly(scope);
            this.mScopes.remove(mountId);
        }
    }
}
