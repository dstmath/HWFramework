package com.android.server;

import android.app.AppOpsManager;
import android.content.Context;
import android.os.Binder;
import android.os.IBinder;
import android.os.IBinder.DeathRecipient;
import android.os.MemoryFile;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.util.Log;
import android.view.IGraphicsStats.Stub;
import android.view.ThreadedRenderer;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

public class GraphicsStatsService extends Stub {
    private static final int ASHMEM_SIZE = 464;
    public static final String GRAPHICS_STATS_SERVICE = "graphicsstats";
    private static final int HISTORY_SIZE = 20;
    private static final String TAG = "GraphicsStatsService";
    private ArrayList<ActiveBuffer> mActive;
    private final AppOpsManager mAppOps;
    private final Context mContext;
    private HistoricalData[] mHistoricalLog;
    private final Object mLock;
    private int mNextHistoricalSlot;
    private byte[] mTempBuffer;

    private final class ActiveBuffer implements DeathRecipient {
        final String mPackageName;
        final int mPid;
        HistoricalData mPreviousData;
        MemoryFile mProcessBuffer;
        final IBinder mToken;
        final int mUid;

        ActiveBuffer(IBinder token, int uid, int pid, String packageName) throws RemoteException, IOException {
            this.mUid = uid;
            this.mPid = pid;
            this.mPackageName = packageName;
            this.mToken = token;
            this.mToken.linkToDeath(this, 0);
            this.mProcessBuffer = new MemoryFile("GFXStats-" + uid, GraphicsStatsService.ASHMEM_SIZE);
            this.mPreviousData = GraphicsStatsService.this.removeHistoricalDataLocked(this.mUid, this.mPackageName);
            if (this.mPreviousData != null) {
                this.mProcessBuffer.writeBytes(this.mPreviousData.mBuffer, 0, 0, GraphicsStatsService.ASHMEM_SIZE);
            }
        }

        public void binderDied() {
            this.mToken.unlinkToDeath(this, 0);
            GraphicsStatsService.this.processDied(this);
        }

        void closeAllBuffers() {
            if (this.mProcessBuffer != null) {
                this.mProcessBuffer.close();
                this.mProcessBuffer = null;
            }
        }
    }

    private static final class HistoricalData {
        final byte[] mBuffer;
        String mPackageName;
        int mUid;

        private HistoricalData() {
            this.mBuffer = new byte[GraphicsStatsService.ASHMEM_SIZE];
        }

        void update(String packageName, int uid, MemoryFile file) {
            this.mUid = uid;
            this.mPackageName = packageName;
            try {
                file.readBytes(this.mBuffer, 0, 0, GraphicsStatsService.ASHMEM_SIZE);
            } catch (IOException e) {
            }
        }
    }

    public GraphicsStatsService(Context context) {
        this.mLock = new Object();
        this.mActive = new ArrayList();
        this.mHistoricalLog = new HistoricalData[HISTORY_SIZE];
        this.mNextHistoricalSlot = 0;
        this.mTempBuffer = new byte[ASHMEM_SIZE];
        this.mContext = context;
        this.mAppOps = (AppOpsManager) context.getSystemService(AppOpsManager.class);
    }

    public ParcelFileDescriptor requestBufferForProcess(String packageName, IBinder token) throws RemoteException {
        int uid = Binder.getCallingUid();
        int pid = Binder.getCallingPid();
        ParcelFileDescriptor parcelFileDescriptor = null;
        long callingIdentity = Binder.clearCallingIdentity();
        try {
            this.mAppOps.checkPackage(uid, packageName);
            synchronized (this.mLock) {
                parcelFileDescriptor = requestBufferForProcessLocked(token, uid, pid, packageName);
            }
            return parcelFileDescriptor;
        } finally {
            Binder.restoreCallingIdentity(callingIdentity);
        }
    }

    private ParcelFileDescriptor getPfd(MemoryFile file) {
        try {
            return new ParcelFileDescriptor(file.getFileDescriptor());
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to get PFD from memory file", ex);
        }
    }

    private ParcelFileDescriptor requestBufferForProcessLocked(IBinder token, int uid, int pid, String packageName) throws RemoteException {
        return getPfd(fetchActiveBuffersLocked(token, uid, pid, packageName).mProcessBuffer);
    }

    private void processDied(ActiveBuffer buffer) {
        synchronized (this.mLock) {
            this.mActive.remove(buffer);
            Log.d("GraphicsStats", "Buffer count: " + this.mActive.size());
        }
        HistoricalData data = buffer.mPreviousData;
        buffer.mPreviousData = null;
        if (data == null) {
            data = this.mHistoricalLog[this.mNextHistoricalSlot];
            if (data == null) {
                data = new HistoricalData();
            }
        }
        data.update(buffer.mPackageName, buffer.mUid, buffer.mProcessBuffer);
        buffer.closeAllBuffers();
        this.mHistoricalLog[this.mNextHistoricalSlot] = data;
        this.mNextHistoricalSlot = (this.mNextHistoricalSlot + 1) % this.mHistoricalLog.length;
    }

    private ActiveBuffer fetchActiveBuffersLocked(IBinder token, int uid, int pid, String packageName) throws RemoteException {
        int size = this.mActive.size();
        for (int i = 0; i < size; i++) {
            ActiveBuffer buffers = (ActiveBuffer) this.mActive.get(i);
            if (buffers.mPid == pid && buffers.mUid == uid) {
                return buffers;
            }
        }
        try {
            buffers = new ActiveBuffer(token, uid, pid, packageName);
            this.mActive.add(buffers);
            return buffers;
        } catch (IOException e) {
            throw new RemoteException("Failed to allocate space");
        }
    }

    private HistoricalData removeHistoricalDataLocked(int uid, String packageName) {
        for (int i = 0; i < this.mHistoricalLog.length; i++) {
            HistoricalData data = this.mHistoricalLog[i];
            if (data != null && data.mUid == uid && data.mPackageName.equals(packageName)) {
                if (i == this.mNextHistoricalSlot) {
                    this.mHistoricalLog[i] = null;
                } else {
                    this.mHistoricalLog[i] = this.mHistoricalLog[this.mNextHistoricalSlot];
                    this.mHistoricalLog[this.mNextHistoricalSlot] = null;
                }
                return data;
            }
        }
        return null;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected void dump(FileDescriptor fd, PrintWriter fout, String[] args) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.DUMP", TAG);
        synchronized (this.mLock) {
            int i = 0;
            while (true) {
                if (i < this.mActive.size()) {
                    ActiveBuffer buffer = (ActiveBuffer) this.mActive.get(i);
                    fout.print("Package: ");
                    fout.print(buffer.mPackageName);
                    fout.flush();
                    try {
                        buffer.mProcessBuffer.readBytes(this.mTempBuffer, 0, 0, ASHMEM_SIZE);
                        ThreadedRenderer.dumpProfileData(this.mTempBuffer, fd);
                    } catch (IOException e) {
                        fout.println("Failed to dump");
                    }
                    fout.println();
                    i++;
                } else {
                    for (HistoricalData buffer2 : this.mHistoricalLog) {
                        if (buffer2 != null) {
                            fout.print("Package: ");
                            fout.print(buffer2.mPackageName);
                            fout.flush();
                            ThreadedRenderer.dumpProfileData(buffer2.mBuffer, fd);
                            fout.println();
                        }
                    }
                }
            }
        }
    }
}
