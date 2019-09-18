package com.android.server;

import android.app.AlarmManager;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.MemoryFile;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.os.Trace;
import android.os.UserHandle;
import android.util.Log;
import android.view.IGraphicsStats;
import android.view.IGraphicsStatsCallback;
import com.android.internal.util.DumpUtils;
import com.android.server.job.controllers.JobStatus;
import com.android.server.utils.PriorityDump;
import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.TimeZone;

public class GraphicsStatsService extends IGraphicsStats.Stub {
    private static final int DELETE_OLD = 2;
    public static final String GRAPHICS_STATS_SERVICE = "graphicsstats";
    private static final int SAVE_BUFFER = 1;
    private static final String TAG = "GraphicsStatsService";
    /* access modifiers changed from: private */
    public final int ASHMEM_SIZE = nGetAshmemSize();
    /* access modifiers changed from: private */
    public final byte[] ZERO_DATA = new byte[this.ASHMEM_SIZE];
    private ArrayList<ActiveBuffer> mActive = new ArrayList<>();
    private final AlarmManager mAlarmManager;
    private final AppOpsManager mAppOps;
    private final Context mContext;
    private final Object mFileAccessLock = new Object();
    private File mGraphicsStatsDir;
    private final Object mLock = new Object();
    private boolean mRotateIsScheduled = false;
    private Handler mWriteOutHandler;

    private final class ActiveBuffer implements IBinder.DeathRecipient {
        final IGraphicsStatsCallback mCallback;
        final BufferInfo mInfo;
        final int mPid;
        MemoryFile mProcessBuffer;
        final IBinder mToken = this.mCallback.asBinder();
        final int mUid;

        ActiveBuffer(IGraphicsStatsCallback token, int uid, int pid, String packageName, long versionCode) throws RemoteException, IOException {
            BufferInfo bufferInfo = new BufferInfo(packageName, versionCode, System.currentTimeMillis());
            this.mInfo = bufferInfo;
            this.mUid = uid;
            this.mPid = pid;
            this.mCallback = token;
            this.mToken.linkToDeath(this, 0);
            this.mProcessBuffer = new MemoryFile("GFXStats-" + pid, GraphicsStatsService.this.ASHMEM_SIZE);
            this.mProcessBuffer.writeBytes(GraphicsStatsService.this.ZERO_DATA, 0, 0, GraphicsStatsService.this.ASHMEM_SIZE);
        }

        public void binderDied() {
            this.mToken.unlinkToDeath(this, 0);
            GraphicsStatsService.this.processDied(this);
        }

        /* access modifiers changed from: package-private */
        public void closeAllBuffers() {
            if (this.mProcessBuffer != null) {
                this.mProcessBuffer.close();
                this.mProcessBuffer = null;
            }
        }
    }

    private final class BufferInfo {
        long endTime;
        final String packageName;
        long startTime;
        final long versionCode;

        BufferInfo(String packageName2, long versionCode2, long startTime2) {
            this.packageName = packageName2;
            this.versionCode = versionCode2;
            this.startTime = startTime2;
        }
    }

    private final class HistoricalBuffer {
        final byte[] mData = new byte[GraphicsStatsService.this.ASHMEM_SIZE];
        final BufferInfo mInfo;

        HistoricalBuffer(ActiveBuffer active) throws IOException {
            this.mInfo = active.mInfo;
            this.mInfo.endTime = System.currentTimeMillis();
            active.mProcessBuffer.readBytes(this.mData, 0, 0, GraphicsStatsService.this.ASHMEM_SIZE);
        }
    }

    private static native void nAddToDump(long j, String str);

    private static native void nAddToDump(long j, String str, String str2, long j2, long j3, long j4, byte[] bArr);

    private static native long nCreateDump(int i, boolean z);

    private static native void nFinishDump(long j);

    private static native int nGetAshmemSize();

    private static native void nSaveBuffer(String str, String str2, long j, long j2, long j3, byte[] bArr);

    public GraphicsStatsService(Context context) {
        this.mContext = context;
        this.mAppOps = (AppOpsManager) context.getSystemService(AppOpsManager.class);
        this.mAlarmManager = (AlarmManager) context.getSystemService(AlarmManager.class);
        this.mGraphicsStatsDir = new File(new File(Environment.getDataDirectory(), "system"), GRAPHICS_STATS_SERVICE);
        this.mGraphicsStatsDir.mkdirs();
        if (this.mGraphicsStatsDir.exists()) {
            HandlerThread bgthread = new HandlerThread("GraphicsStats-disk", 10);
            bgthread.start();
            this.mWriteOutHandler = new Handler(bgthread.getLooper(), new Handler.Callback() {
                public boolean handleMessage(Message msg) {
                    switch (msg.what) {
                        case 1:
                            GraphicsStatsService.this.saveBuffer((HistoricalBuffer) msg.obj);
                            break;
                        case 2:
                            GraphicsStatsService.this.deleteOldBuffers();
                            break;
                    }
                    return true;
                }
            });
            return;
        }
        throw new IllegalStateException("Graphics stats directory does not exist: " + this.mGraphicsStatsDir.getAbsolutePath());
    }

    private void scheduleRotateLocked() {
        if (!this.mRotateIsScheduled) {
            this.mRotateIsScheduled = true;
            Calendar calendar = normalizeDate(System.currentTimeMillis());
            calendar.add(5, 1);
            this.mAlarmManager.setExact(1, calendar.getTimeInMillis(), TAG, new AlarmManager.OnAlarmListener() {
                public final void onAlarm() {
                    GraphicsStatsService.this.onAlarm();
                }
            }, this.mWriteOutHandler);
        }
    }

    /* access modifiers changed from: private */
    public void onAlarm() {
        ActiveBuffer[] activeCopy;
        synchronized (this.mLock) {
            this.mRotateIsScheduled = false;
            scheduleRotateLocked();
            activeCopy = (ActiveBuffer[]) this.mActive.toArray(new ActiveBuffer[0]);
        }
        for (ActiveBuffer active : activeCopy) {
            try {
                active.mCallback.onRotateGraphicsStatsBuffer();
            } catch (RemoteException e) {
                Log.w(TAG, String.format("Failed to notify '%s' (pid=%d) to rotate buffers", new Object[]{active.mInfo.packageName, Integer.valueOf(active.mPid)}), e);
            }
        }
        this.mWriteOutHandler.sendEmptyMessageDelayed(2, JobStatus.DEFAULT_TRIGGER_UPDATE_DELAY);
    }

    public ParcelFileDescriptor requestBufferForProcess(String packageName, IGraphicsStatsCallback token) throws RemoteException {
        Object obj;
        String str = packageName;
        int uid = Binder.getCallingUid();
        int pid = Binder.getCallingPid();
        long callingIdentity = Binder.clearCallingIdentity();
        try {
            this.mAppOps.checkPackage(uid, str);
            PackageInfo info = this.mContext.getPackageManager().getPackageInfoAsUser(str, 0, UserHandle.getUserId(uid));
            Object obj2 = this.mLock;
            synchronized (obj2) {
                try {
                    obj = obj2;
                    try {
                        ParcelFileDescriptor pfd = requestBufferForProcessLocked(token, uid, pid, str, info.getLongVersionCode());
                        try {
                            Binder.restoreCallingIdentity(callingIdentity);
                            return pfd;
                        } catch (Throwable th) {
                            th = th;
                            ParcelFileDescriptor parcelFileDescriptor = pfd;
                            throw th;
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    obj = obj2;
                    throw th;
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
            throw new RemoteException("Unable to find package: '" + str + "'");
        } catch (Throwable th4) {
            Binder.restoreCallingIdentity(callingIdentity);
            throw th4;
        }
    }

    private ParcelFileDescriptor getPfd(MemoryFile file) {
        try {
            if (file.getFileDescriptor().valid()) {
                return new ParcelFileDescriptor(file.getFileDescriptor());
            }
            throw new IllegalStateException("Invalid file descriptor");
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to get PFD from memory file", ex);
        }
    }

    private ParcelFileDescriptor requestBufferForProcessLocked(IGraphicsStatsCallback token, int uid, int pid, String packageName, long versionCode) throws RemoteException {
        ActiveBuffer buffer = fetchActiveBuffersLocked(token, uid, pid, packageName, versionCode);
        scheduleRotateLocked();
        return getPfd(buffer.mProcessBuffer);
    }

    private Calendar normalizeDate(long timestamp) {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.setTimeInMillis(timestamp);
        calendar.set(11, 0);
        calendar.set(12, 0);
        calendar.set(13, 0);
        calendar.set(14, 0);
        return calendar;
    }

    private File pathForApp(BufferInfo info) {
        return new File(this.mGraphicsStatsDir, String.format("%d/%s/%d/total", new Object[]{Long.valueOf(normalizeDate(info.startTime).getTimeInMillis()), info.packageName, Long.valueOf(info.versionCode)}));
    }

    /* access modifiers changed from: private */
    public void saveBuffer(HistoricalBuffer buffer) {
        if (Trace.isTagEnabled(524288)) {
            Trace.traceBegin(524288, "saving graphicsstats for " + buffer.mInfo.packageName);
        }
        synchronized (this.mFileAccessLock) {
            File path = pathForApp(buffer.mInfo);
            File parent = path.getParentFile();
            parent.mkdirs();
            if (!parent.exists()) {
                Log.w(TAG, "Unable to create path: '" + parent.getAbsolutePath() + "'");
                return;
            }
            nSaveBuffer(path.getAbsolutePath(), buffer.mInfo.packageName, buffer.mInfo.versionCode, buffer.mInfo.startTime, buffer.mInfo.endTime, buffer.mData);
            Trace.traceEnd(524288);
        }
    }

    private void deleteRecursiveLocked(File file) {
        if (file.isDirectory()) {
            for (File child : file.listFiles()) {
                deleteRecursiveLocked(child);
            }
        }
        if (!file.delete()) {
            Log.w(TAG, "Failed to delete '" + file.getAbsolutePath() + "'!");
        }
    }

    /* access modifiers changed from: private */
    public void deleteOldBuffers() {
        Trace.traceBegin(524288, "deleting old graphicsstats buffers");
        synchronized (this.mFileAccessLock) {
            File[] files = this.mGraphicsStatsDir.listFiles();
            if (files != null) {
                if (files.length > 3) {
                    long[] sortedDates = new long[files.length];
                    for (int i = 0; i < files.length; i++) {
                        try {
                            sortedDates[i] = Long.parseLong(files[i].getName());
                        } catch (NumberFormatException e) {
                        }
                    }
                    if (sortedDates.length > 3) {
                        Arrays.sort(sortedDates);
                        for (int i2 = 0; i2 < sortedDates.length - 3; i2++) {
                            deleteRecursiveLocked(new File(this.mGraphicsStatsDir, Long.toString(sortedDates[i2])));
                        }
                        Trace.traceEnd(524288);
                    }
                }
            }
        }
    }

    private void addToSaveQueue(ActiveBuffer buffer) {
        try {
            Message.obtain(this.mWriteOutHandler, 1, new HistoricalBuffer(buffer)).sendToTarget();
        } catch (IOException e) {
            Log.w(TAG, "Failed to copy graphicsstats from " + buffer.mInfo.packageName, e);
        }
        buffer.closeAllBuffers();
    }

    /* access modifiers changed from: private */
    public void processDied(ActiveBuffer buffer) {
        synchronized (this.mLock) {
            this.mActive.remove(buffer);
        }
        addToSaveQueue(buffer);
    }

    private ActiveBuffer fetchActiveBuffersLocked(IGraphicsStatsCallback token, int uid, int pid, String packageName, long versionCode) throws RemoteException {
        int i;
        int i2;
        int size = this.mActive.size();
        long today = normalizeDate(System.currentTimeMillis()).getTimeInMillis();
        int i3 = 0;
        while (true) {
            if (i3 >= size) {
                i = uid;
                i2 = pid;
                break;
            }
            ActiveBuffer buffer = this.mActive.get(i3);
            i2 = pid;
            if (buffer.mPid == i2) {
                i = uid;
                if (buffer.mUid == i) {
                    if (buffer.mInfo.startTime >= today) {
                        return buffer;
                    }
                    buffer.binderDied();
                }
            } else {
                int i4 = uid;
            }
            i3++;
        }
        try {
            ActiveBuffer activeBuffer = new ActiveBuffer(token, i, i2, packageName, versionCode);
            this.mActive.add(activeBuffer);
            return activeBuffer;
        } catch (IOException e) {
            throw new RemoteException("Failed to allocate space");
        }
    }

    private HashSet<File> dumpActiveLocked(long dump, ArrayList<HistoricalBuffer> buffers) {
        HashSet<File> skipFiles = new HashSet<>(buffers.size());
        for (int i = 0; i < buffers.size(); i++) {
            HistoricalBuffer buffer = buffers.get(i);
            File path = pathForApp(buffer.mInfo);
            skipFiles.add(path);
            nAddToDump(dump, path.getAbsolutePath(), buffer.mInfo.packageName, buffer.mInfo.versionCode, buffer.mInfo.startTime, buffer.mInfo.endTime, buffer.mData);
        }
        ArrayList<HistoricalBuffer> arrayList = buffers;
        return skipFiles;
    }

    private void dumpHistoricalLocked(long dump, HashSet<File> skipFiles) {
        File[] fileArr;
        File[] listFiles = this.mGraphicsStatsDir.listFiles();
        int length = listFiles.length;
        int i = 0;
        while (i < length) {
            File[] listFiles2 = listFiles[i].listFiles();
            int length2 = listFiles2.length;
            int i2 = 0;
            while (i2 < length2) {
                File[] listFiles3 = listFiles2[i2].listFiles();
                int length3 = listFiles3.length;
                int i3 = 0;
                while (i3 < length3) {
                    File data = new File(listFiles3[i3], "total");
                    if (skipFiles.contains(data)) {
                        fileArr = listFiles;
                        long j = dump;
                    } else {
                        fileArr = listFiles;
                        nAddToDump(dump, data.getAbsolutePath());
                    }
                    i3++;
                    listFiles = fileArr;
                }
                HashSet<File> hashSet = skipFiles;
                long j2 = dump;
                i2++;
                listFiles = listFiles;
            }
            HashSet<File> hashSet2 = skipFiles;
            long j3 = dump;
            i++;
            listFiles = listFiles;
        }
        long j4 = dump;
        HashSet<File> hashSet3 = skipFiles;
    }

    /* access modifiers changed from: protected */
    public void dump(FileDescriptor fd, PrintWriter fout, String[] args) {
        ArrayList<HistoricalBuffer> buffers;
        if (DumpUtils.checkDumpAndUsageStatsPermission(this.mContext, TAG, fout)) {
            boolean dumpProto = false;
            int length = args.length;
            int i = 0;
            while (true) {
                if (i >= length) {
                    break;
                } else if (PriorityDump.PROTO_ARG.equals(args[i])) {
                    dumpProto = true;
                    break;
                } else {
                    i++;
                }
            }
            synchronized (this.mLock) {
                buffers = new ArrayList<>(this.mActive.size());
                for (int i2 = 0; i2 < this.mActive.size(); i2++) {
                    try {
                        buffers.add(new HistoricalBuffer(this.mActive.get(i2)));
                    } catch (IOException e) {
                    }
                }
            }
            ArrayList<HistoricalBuffer> buffers2 = buffers;
            long dump = nCreateDump(fd.getInt$(), dumpProto);
            try {
                synchronized (this.mFileAccessLock) {
                    HashSet<File> skipList = dumpActiveLocked(dump, buffers2);
                    buffers2.clear();
                    dumpHistoricalLocked(dump, skipList);
                }
                nFinishDump(dump);
            } catch (Throwable th) {
                nFinishDump(dump);
                throw th;
            }
        }
    }
}
