package com.android.server.wm;

import android.app.ActivityManager;
import android.graphics.Bitmap;
import android.os.Process;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.util.ArraySet;
import android.util.Slog;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.os.AtomicFile;
import com.android.server.wm.nano.WindowManagerProtos;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayDeque;

class TaskSnapshotPersister {
    private static final String BITMAP_EXTENSION = ".jpg";
    static final int CONFIG_SCALE = SystemProperties.getInt("ro.config.hw_snapshot_scale", -1);
    private static final long DELAY_MS = 100;
    static final boolean DISABLE_FULL_SIZED_BITMAPS = ActivityManager.isLowRamDeviceStatic();
    private static final int MAX_STORE_QUEUE_DEPTH = 2;
    private static final String PROTO_EXTENSION = ".proto";
    private static final int QUALITY = 95;
    private static final String REDUCED_POSTFIX = "_reduced";
    static final float REDUCED_SCALE = (ActivityManager.isLowRamDeviceStatic() ? 0.6f : 0.5f);
    private static final String SNAPSHOTS_DIRNAME = "snapshots";
    private static final String TAG = "WindowManager";
    static final boolean USE_CONFIG_SCALE = (CONFIG_SCALE >= 50 && CONFIG_SCALE < 100);
    private final DirectoryResolver mDirectoryResolver;
    /* access modifiers changed from: private */
    public final Object mLock = new Object();
    /* access modifiers changed from: private */
    @GuardedBy("mLock")
    public boolean mPaused;
    /* access modifiers changed from: private */
    @GuardedBy("mLock")
    public final ArraySet<Integer> mPersistedTaskIdsSinceLastRemoveObsolete = new ArraySet<>();
    private Thread mPersister = new Thread("TaskSnapshotPersister") {
        public void run() {
            WriteQueueItem next;
            Process.setThreadPriority(10);
            while (true) {
                synchronized (TaskSnapshotPersister.this.mLock) {
                    if (TaskSnapshotPersister.this.mPaused) {
                        next = null;
                    } else {
                        next = (WriteQueueItem) TaskSnapshotPersister.this.mWriteQueue.poll();
                        if (next != null) {
                            next.onDequeuedLocked();
                        }
                    }
                }
                if (next != null) {
                    next.write();
                    SystemClock.sleep(TaskSnapshotPersister.DELAY_MS);
                }
                synchronized (TaskSnapshotPersister.this.mLock) {
                    boolean writeQueueEmpty = TaskSnapshotPersister.this.mWriteQueue.isEmpty();
                    if (writeQueueEmpty || TaskSnapshotPersister.this.mPaused) {
                        try {
                            boolean unused = TaskSnapshotPersister.this.mQueueIdling = writeQueueEmpty;
                            TaskSnapshotPersister.this.mLock.wait();
                            boolean unused2 = TaskSnapshotPersister.this.mQueueIdling = false;
                        } catch (InterruptedException e) {
                        }
                    }
                }
            }
            while (true) {
            }
        }
    };
    /* access modifiers changed from: private */
    @GuardedBy("mLock")
    public boolean mQueueIdling;
    private boolean mStarted;
    /* access modifiers changed from: private */
    @GuardedBy("mLock")
    public final ArrayDeque<StoreWriteQueueItem> mStoreQueueItems = new ArrayDeque<>();
    /* access modifiers changed from: private */
    @GuardedBy("mLock")
    public final ArrayDeque<WriteQueueItem> mWriteQueue = new ArrayDeque<>();

    private class DeleteWriteQueueItem extends WriteQueueItem {
        private final int mTaskId;
        private final int mUserId;

        DeleteWriteQueueItem(int taskId, int userId) {
            super();
            this.mTaskId = taskId;
            this.mUserId = userId;
        }

        /* access modifiers changed from: package-private */
        public void write() {
            TaskSnapshotPersister.this.deleteSnapshot(this.mTaskId, this.mUserId);
        }
    }

    interface DirectoryResolver {
        File getSystemDirectoryForUser(int i);
    }

    @VisibleForTesting
    class RemoveObsoleteFilesQueueItem extends WriteQueueItem {
        private final ArraySet<Integer> mPersistentTaskIds;
        private final int[] mRunningUserIds;

        @VisibleForTesting
        RemoveObsoleteFilesQueueItem(ArraySet<Integer> persistentTaskIds, int[] runningUserIds) {
            super();
            this.mPersistentTaskIds = new ArraySet<>(persistentTaskIds);
            this.mRunningUserIds = runningUserIds;
        }

        /* access modifiers changed from: package-private */
        public void write() {
            ArraySet<Integer> newPersistedTaskIds;
            synchronized (TaskSnapshotPersister.this.mLock) {
                newPersistedTaskIds = new ArraySet<>(TaskSnapshotPersister.this.mPersistedTaskIdsSinceLastRemoveObsolete);
            }
            for (int userId : this.mRunningUserIds) {
                File dir = TaskSnapshotPersister.this.getDirectory(userId);
                String[] files = dir.list();
                if (files != null) {
                    for (String file : files) {
                        int taskId = getTaskId(file);
                        if (!this.mPersistentTaskIds.contains(Integer.valueOf(taskId)) && !newPersistedTaskIds.contains(Integer.valueOf(taskId))) {
                            new File(dir, file).delete();
                        }
                    }
                }
            }
            if (this.mPersistentTaskIds != null) {
                this.mPersistentTaskIds.clear();
            }
        }

        /* access modifiers changed from: package-private */
        @VisibleForTesting
        public int getTaskId(String fileName) {
            if (!fileName.endsWith(TaskSnapshotPersister.PROTO_EXTENSION) && !fileName.endsWith(TaskSnapshotPersister.BITMAP_EXTENSION)) {
                return -1;
            }
            int end = fileName.lastIndexOf(46);
            if (end == -1) {
                return -1;
            }
            String name = fileName.substring(0, end);
            if (name.endsWith(TaskSnapshotPersister.REDUCED_POSTFIX)) {
                name = name.substring(0, name.length() - TaskSnapshotPersister.REDUCED_POSTFIX.length());
            }
            try {
                return Integer.parseInt(name);
            } catch (NumberFormatException e) {
                return -1;
            }
        }
    }

    private class StoreWriteQueueItem extends WriteQueueItem {
        private final ActivityManager.TaskSnapshot mSnapshot;
        /* access modifiers changed from: private */
        public final int mTaskId;
        private final int mUserId;

        StoreWriteQueueItem(int taskId, int userId, ActivityManager.TaskSnapshot snapshot) {
            super();
            this.mTaskId = taskId;
            this.mUserId = userId;
            this.mSnapshot = snapshot;
        }

        /* access modifiers changed from: package-private */
        @GuardedBy("mLock")
        public void onQueuedLocked() {
            TaskSnapshotPersister.this.mStoreQueueItems.offer(this);
        }

        /* access modifiers changed from: package-private */
        @GuardedBy("mLock")
        public void onDequeuedLocked() {
            TaskSnapshotPersister.this.mStoreQueueItems.remove(this);
        }

        /* access modifiers changed from: package-private */
        public void write() {
            if (!TaskSnapshotPersister.this.createDirectory(this.mUserId)) {
                Slog.e(TaskSnapshotPersister.TAG, "Unable to create snapshot directory for user dir=" + TaskSnapshotPersister.this.getDirectory(this.mUserId));
            }
            boolean failed = false;
            if (!writeProto()) {
                failed = true;
            }
            if (!writeBuffer()) {
                failed = true;
            }
            if (failed) {
                TaskSnapshotPersister.this.deleteSnapshot(this.mTaskId, this.mUserId);
            }
        }

        /* access modifiers changed from: package-private */
        public boolean writeProto() {
            WindowManagerProtos.TaskSnapshotProto proto = new WindowManagerProtos.TaskSnapshotProto();
            proto.orientation = this.mSnapshot.getOrientation();
            proto.insetLeft = this.mSnapshot.getContentInsets().left;
            proto.insetTop = this.mSnapshot.getContentInsets().top;
            proto.insetRight = this.mSnapshot.getContentInsets().right;
            proto.insetBottom = this.mSnapshot.getContentInsets().bottom;
            proto.isRealSnapshot = this.mSnapshot.isRealSnapshot();
            proto.windowingMode = this.mSnapshot.getWindowingMode();
            proto.systemUiVisibility = this.mSnapshot.getSystemUiVisibility();
            proto.isTranslucent = this.mSnapshot.isTranslucent();
            byte[] bytes = WindowManagerProtos.TaskSnapshotProto.toByteArray(proto);
            File file = TaskSnapshotPersister.this.getProtoFile(this.mTaskId, this.mUserId);
            AtomicFile atomicFile = new AtomicFile(file);
            FileOutputStream fos = null;
            try {
                fos = atomicFile.startWrite();
                fos.write(bytes);
                atomicFile.finishWrite(fos);
                return true;
            } catch (IOException e) {
                atomicFile.failWrite(fos);
                Slog.e(TaskSnapshotPersister.TAG, "Unable to open " + file + " for persisting. " + e);
                return false;
            }
        }

        /* access modifiers changed from: package-private */
        public boolean writeBuffer() {
            Bitmap bitmap = Bitmap.createHardwareBitmap(this.mSnapshot.getSnapshot());
            if (bitmap == null) {
                Slog.e(TaskSnapshotPersister.TAG, "Invalid task snapshot hw bitmap");
                return false;
            }
            Bitmap swBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, false);
            if (swBitmap == null) {
                Slog.e(TaskSnapshotPersister.TAG, "Invalid task snapshot sw bitmap");
                return false;
            }
            File reducedFile = TaskSnapshotPersister.this.getReducedResolutionBitmapFile(this.mTaskId, this.mUserId);
            Bitmap reduced = (this.mSnapshot.isReducedResolution() || TaskSnapshotPersister.USE_CONFIG_SCALE) ? swBitmap : Bitmap.createScaledBitmap(swBitmap, (int) (((float) bitmap.getWidth()) * TaskSnapshotPersister.REDUCED_SCALE), (int) (((float) bitmap.getHeight()) * TaskSnapshotPersister.REDUCED_SCALE), true);
            try {
                FileOutputStream reducedFos = new FileOutputStream(reducedFile);
                if (reduced == null) {
                    Slog.e(TaskSnapshotPersister.TAG, "createScaledBitmap error");
                    reducedFos.close();
                    return false;
                }
                reduced.compress(Bitmap.CompressFormat.JPEG, TaskSnapshotPersister.QUALITY, reducedFos);
                reducedFos.close();
                if (this.mSnapshot.isReducedResolution()) {
                    return true;
                }
                File file = TaskSnapshotPersister.this.getBitmapFile(this.mTaskId, this.mUserId);
                try {
                    FileOutputStream fos = new FileOutputStream(file);
                    swBitmap.compress(Bitmap.CompressFormat.JPEG, TaskSnapshotPersister.QUALITY, fos);
                    fos.close();
                    return true;
                } catch (IOException e) {
                    Slog.e(TaskSnapshotPersister.TAG, "Unable to open " + file + " for persisting.", e);
                    return false;
                }
            } catch (IOException e2) {
                Slog.e(TaskSnapshotPersister.TAG, "Unable to open " + reducedFile + " for persisting.", e2);
                return false;
            }
        }
    }

    private abstract class WriteQueueItem {
        /* access modifiers changed from: package-private */
        public abstract void write();

        private WriteQueueItem() {
        }

        /* access modifiers changed from: package-private */
        public void onQueuedLocked() {
        }

        /* access modifiers changed from: package-private */
        public void onDequeuedLocked() {
        }
    }

    TaskSnapshotPersister(DirectoryResolver resolver) {
        this.mDirectoryResolver = resolver;
    }

    /* access modifiers changed from: package-private */
    public void start() {
        if (!this.mStarted) {
            this.mStarted = true;
            this.mPersister.start();
        }
    }

    /* access modifiers changed from: package-private */
    public void persistSnapshot(int taskId, int userId, ActivityManager.TaskSnapshot snapshot) {
        synchronized (this.mLock) {
            this.mPersistedTaskIdsSinceLastRemoveObsolete.add(Integer.valueOf(taskId));
            sendToQueueLocked(new StoreWriteQueueItem(taskId, userId, snapshot));
        }
    }

    /* access modifiers changed from: package-private */
    public void onTaskRemovedFromRecents(int taskId, int userId) {
        synchronized (this.mLock) {
            this.mPersistedTaskIdsSinceLastRemoveObsolete.remove(Integer.valueOf(taskId));
            sendToQueueLocked(new DeleteWriteQueueItem(taskId, userId));
        }
    }

    /* access modifiers changed from: package-private */
    public void removeObsoleteFiles(ArraySet<Integer> persistentTaskIds, int[] runningUserIds) {
        synchronized (this.mLock) {
            this.mPersistedTaskIdsSinceLastRemoveObsolete.clear();
            sendToQueueLocked(new RemoveObsoleteFilesQueueItem(persistentTaskIds, runningUserIds));
        }
    }

    /* access modifiers changed from: package-private */
    public void setPaused(boolean paused) {
        synchronized (this.mLock) {
            this.mPaused = paused;
            if (!paused) {
                this.mLock.notifyAll();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void waitForQueueEmpty() {
        while (true) {
            synchronized (this.mLock) {
                if (this.mWriteQueue.isEmpty() && this.mQueueIdling) {
                    return;
                }
            }
            SystemClock.sleep(DELAY_MS);
        }
        while (true) {
        }
    }

    @GuardedBy("mLock")
    private void sendToQueueLocked(WriteQueueItem item) {
        this.mWriteQueue.offer(item);
        item.onQueuedLocked();
        ensureStoreQueueDepthLocked();
        if (!this.mPaused) {
            this.mLock.notifyAll();
        }
    }

    @GuardedBy("mLock")
    private void ensureStoreQueueDepthLocked() {
        while (this.mStoreQueueItems.size() > 2) {
            StoreWriteQueueItem item = this.mStoreQueueItems.poll();
            this.mWriteQueue.remove(item);
            Slog.i(TAG, "Queue is too deep! Purged item with taskid=" + item.mTaskId);
        }
    }

    /* access modifiers changed from: private */
    public File getDirectory(int userId) {
        return new File(this.mDirectoryResolver.getSystemDirectoryForUser(userId), SNAPSHOTS_DIRNAME);
    }

    /* access modifiers changed from: package-private */
    public File getProtoFile(int taskId, int userId) {
        File directory = getDirectory(userId);
        return new File(directory, taskId + PROTO_EXTENSION);
    }

    /* access modifiers changed from: package-private */
    public File getBitmapFile(int taskId, int userId) {
        if (DISABLE_FULL_SIZED_BITMAPS) {
            Slog.wtf(TAG, "This device does not support full sized resolution bitmaps.");
            return null;
        }
        File directory = getDirectory(userId);
        return new File(directory, taskId + BITMAP_EXTENSION);
    }

    /* access modifiers changed from: package-private */
    public File getReducedResolutionBitmapFile(int taskId, int userId) {
        File directory = getDirectory(userId);
        return new File(directory, taskId + REDUCED_POSTFIX + BITMAP_EXTENSION);
    }

    /* access modifiers changed from: private */
    public boolean createDirectory(int userId) {
        File dir = getDirectory(userId);
        return dir.exists() || dir.mkdirs();
    }

    /* access modifiers changed from: private */
    public void deleteSnapshot(int taskId, int userId) {
        File protoFile = getProtoFile(taskId, userId);
        File bitmapReducedFile = getReducedResolutionBitmapFile(taskId, userId);
        protoFile.delete();
        bitmapReducedFile.delete();
        if (!DISABLE_FULL_SIZED_BITMAPS) {
            getBitmapFile(taskId, userId).delete();
        }
    }
}
