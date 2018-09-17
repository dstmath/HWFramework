package com.android.server.wm;

import android.app.ActivityManager.TaskSnapshot;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.os.Process;
import android.os.SystemClock;
import android.util.ArraySet;
import android.util.Slog;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.os.AtomicFile;
import com.android.server.wm.nano.WindowManagerProtos.TaskSnapshotProto;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayDeque;

class TaskSnapshotPersister {
    private static final String BITMAP_EXTENSION = ".jpg";
    private static final long DELAY_MS = 100;
    private static final int MAX_STORE_QUEUE_DEPTH = 2;
    private static final String PROTO_EXTENSION = ".proto";
    private static final int QUALITY = 95;
    private static final String REDUCED_POSTFIX = "_reduced";
    static final float REDUCED_SCALE = 0.5f;
    private static final String SNAPSHOTS_DIRNAME = "snapshots";
    private static final String TAG = "WindowManager";
    private final DirectoryResolver mDirectoryResolver;
    private final Object mLock = new Object();
    @GuardedBy("mLock")
    private boolean mPaused;
    @GuardedBy("mLock")
    private final ArraySet<Integer> mPersistedTaskIdsSinceLastRemoveObsolete = new ArraySet();
    private Thread mPersister = new Thread("TaskSnapshotPersister") {
        public void run() {
            Process.setThreadPriority(10);
            while (true) {
                WriteQueueItem next;
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
                    if (writeQueueEmpty || (TaskSnapshotPersister.this.mPaused ^ 1) == 0) {
                        try {
                            try {
                                TaskSnapshotPersister.this.mQueueIdling = writeQueueEmpty;
                                try {
                                    try {
                                        try {
                                            TaskSnapshotPersister.this.mLock.wait();
                                            try {
                                                try {
                                                    TaskSnapshotPersister.this.mQueueIdling = false;
                                                } catch (InterruptedException e) {
                                                }
                                            } catch (InterruptedException e2) {
                                            }
                                        } catch (InterruptedException e3) {
                                        }
                                    } catch (InterruptedException e4) {
                                    }
                                } catch (InterruptedException e5) {
                                }
                            } catch (InterruptedException e6) {
                            }
                        } catch (InterruptedException e7) {
                        }
                    }
                }
            }
        }
    };
    @GuardedBy("mLock")
    private boolean mQueueIdling;
    private boolean mStarted;
    @GuardedBy("mLock")
    private final ArrayDeque<StoreWriteQueueItem> mStoreQueueItems = new ArrayDeque();
    @GuardedBy("mLock")
    private final ArrayDeque<WriteQueueItem> mWriteQueue = new ArrayDeque();

    interface DirectoryResolver {
        File getSystemDirectoryForUser(int i);
    }

    private abstract class WriteQueueItem {
        /* synthetic */ WriteQueueItem(TaskSnapshotPersister this$0, WriteQueueItem -this1) {
            this();
        }

        abstract void write();

        private WriteQueueItem() {
        }

        void onQueuedLocked() {
        }

        void onDequeuedLocked() {
        }
    }

    private class DeleteWriteQueueItem extends WriteQueueItem {
        private final int mTaskId;
        private final int mUserId;

        DeleteWriteQueueItem(int taskId, int userId) {
            super(TaskSnapshotPersister.this, null);
            this.mTaskId = taskId;
            this.mUserId = userId;
        }

        void write() {
            TaskSnapshotPersister.this.deleteSnapshot(this.mTaskId, this.mUserId);
        }
    }

    class RemoveObsoleteFilesQueueItem extends WriteQueueItem {
        private final ArraySet<Integer> mPersistentTaskIds;
        private final int[] mRunningUserIds;

        RemoveObsoleteFilesQueueItem(ArraySet<Integer> persistentTaskIds, int[] runningUserIds) {
            super(TaskSnapshotPersister.this, null);
            this.mPersistentTaskIds = new ArraySet(persistentTaskIds);
            this.mRunningUserIds = runningUserIds;
        }

        void write() {
            synchronized (TaskSnapshotPersister.this.mLock) {
                ArraySet<Integer> newPersistedTaskIds = new ArraySet(TaskSnapshotPersister.this.mPersistedTaskIdsSinceLastRemoveObsolete);
            }
            for (int userId : this.mRunningUserIds) {
                File dir = TaskSnapshotPersister.this.getDirectory(userId);
                String[] files = dir.list();
                if (files != null) {
                    for (String file : files) {
                        int taskId = getTaskId(file);
                        if (!(this.mPersistentTaskIds.contains(Integer.valueOf(taskId)) || (newPersistedTaskIds.contains(Integer.valueOf(taskId)) ^ 1) == 0)) {
                            new File(dir, file).delete();
                        }
                    }
                }
            }
            if (this.mPersistentTaskIds != null) {
                this.mPersistentTaskIds.clear();
            }
        }

        int getTaskId(String fileName) {
            if (!fileName.endsWith(TaskSnapshotPersister.PROTO_EXTENSION) && (fileName.endsWith(TaskSnapshotPersister.BITMAP_EXTENSION) ^ 1) != 0) {
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
        private final TaskSnapshot mSnapshot;
        private final int mTaskId;
        private final int mUserId;

        StoreWriteQueueItem(int taskId, int userId, TaskSnapshot snapshot) {
            super(TaskSnapshotPersister.this, null);
            this.mTaskId = taskId;
            this.mUserId = userId;
            this.mSnapshot = snapshot;
        }

        void onQueuedLocked() {
            TaskSnapshotPersister.this.mStoreQueueItems.offer(this);
        }

        void onDequeuedLocked() {
            TaskSnapshotPersister.this.mStoreQueueItems.remove(this);
        }

        void write() {
            if (!TaskSnapshotPersister.this.createDirectory(this.mUserId)) {
                Slog.e(TaskSnapshotPersister.TAG, "Unable to create snapshot directory for user dir=" + TaskSnapshotPersister.this.getDirectory(this.mUserId));
            }
            boolean failed = false;
            if (!writeProto()) {
                failed = true;
            }
            if (!writeBuffer()) {
                writeBuffer();
                failed = true;
            }
            if (failed) {
                TaskSnapshotPersister.this.deleteSnapshot(this.mTaskId, this.mUserId);
            }
        }

        boolean writeProto() {
            TaskSnapshotProto proto = new TaskSnapshotProto();
            proto.orientation = this.mSnapshot.getOrientation();
            proto.insetLeft = this.mSnapshot.getContentInsets().left;
            proto.insetTop = this.mSnapshot.getContentInsets().top;
            proto.insetRight = this.mSnapshot.getContentInsets().right;
            proto.insetBottom = this.mSnapshot.getContentInsets().bottom;
            byte[] bytes = TaskSnapshotProto.toByteArray(proto);
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

        boolean writeBuffer() {
            File file = TaskSnapshotPersister.this.getBitmapFile(this.mTaskId, this.mUserId);
            File reducedFile = TaskSnapshotPersister.this.getReducedResolutionBitmapFile(this.mTaskId, this.mUserId);
            Bitmap bitmap = Bitmap.createHardwareBitmap(this.mSnapshot.getSnapshot());
            if (bitmap == null) {
                Slog.e(TaskSnapshotPersister.TAG, "createHardwareBitmap error");
                return false;
            }
            Bitmap swBitmap = bitmap.copy(Config.ARGB_8888, false);
            if (swBitmap == null) {
                Slog.e(TaskSnapshotPersister.TAG, "bitmap copy error");
                return false;
            }
            Bitmap reduced = Bitmap.createScaledBitmap(swBitmap, (int) (((float) bitmap.getWidth()) * 0.5f), (int) (((float) bitmap.getHeight()) * 0.5f), true);
            try {
                FileOutputStream fos = new FileOutputStream(file);
                swBitmap.compress(CompressFormat.JPEG, TaskSnapshotPersister.QUALITY, fos);
                fos.close();
                FileOutputStream reducedFos = new FileOutputStream(reducedFile);
                if (reduced == null) {
                    Slog.e(TaskSnapshotPersister.TAG, "createScaledBitmap error");
                    reducedFos.close();
                    return false;
                }
                reduced.compress(CompressFormat.JPEG, TaskSnapshotPersister.QUALITY, reducedFos);
                reducedFos.close();
                return true;
            } catch (IOException e) {
                Slog.e(TaskSnapshotPersister.TAG, "Unable to open " + file + " or " + reducedFile + " for persisting.", e);
                return false;
            }
        }
    }

    TaskSnapshotPersister(DirectoryResolver resolver) {
        this.mDirectoryResolver = resolver;
    }

    void start() {
        if (!this.mStarted) {
            this.mStarted = true;
            this.mPersister.start();
        }
    }

    void persistSnapshot(int taskId, int userId, TaskSnapshot snapshot) {
        synchronized (this.mLock) {
            this.mPersistedTaskIdsSinceLastRemoveObsolete.add(Integer.valueOf(taskId));
            sendToQueueLocked(new StoreWriteQueueItem(taskId, userId, snapshot));
        }
    }

    void onTaskRemovedFromRecents(int taskId, int userId) {
        synchronized (this.mLock) {
            this.mPersistedTaskIdsSinceLastRemoveObsolete.remove(Integer.valueOf(taskId));
            sendToQueueLocked(new DeleteWriteQueueItem(taskId, userId));
        }
    }

    void removeObsoleteFiles(ArraySet<Integer> persistentTaskIds, int[] runningUserIds) {
        synchronized (this.mLock) {
            this.mPersistedTaskIdsSinceLastRemoveObsolete.clear();
            sendToQueueLocked(new RemoveObsoleteFilesQueueItem(persistentTaskIds, runningUserIds));
        }
    }

    void setPaused(boolean paused) {
        synchronized (this.mLock) {
            this.mPaused = paused;
            if (!paused) {
                this.mLock.notifyAll();
            }
        }
    }

    void waitForQueueEmpty() {
        while (true) {
            synchronized (this.mLock) {
                if (this.mWriteQueue.isEmpty() && this.mQueueIdling) {
                    return;
                }
            }
            SystemClock.sleep(DELAY_MS);
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
            StoreWriteQueueItem item = (StoreWriteQueueItem) this.mStoreQueueItems.poll();
            this.mWriteQueue.remove(item);
            Slog.i(TAG, "Queue is too deep! Purged item with taskid=" + item.mTaskId);
        }
    }

    private File getDirectory(int userId) {
        return new File(this.mDirectoryResolver.getSystemDirectoryForUser(userId), SNAPSHOTS_DIRNAME);
    }

    File getProtoFile(int taskId, int userId) {
        return new File(getDirectory(userId), taskId + PROTO_EXTENSION);
    }

    File getBitmapFile(int taskId, int userId) {
        return new File(getDirectory(userId), taskId + BITMAP_EXTENSION);
    }

    File getReducedResolutionBitmapFile(int taskId, int userId) {
        return new File(getDirectory(userId), taskId + REDUCED_POSTFIX + BITMAP_EXTENSION);
    }

    private boolean createDirectory(int userId) {
        File dir = getDirectory(userId);
        return !dir.exists() ? dir.mkdirs() : true;
    }

    private void deleteSnapshot(int taskId, int userId) {
        File protoFile = getProtoFile(taskId, userId);
        File bitmapFile = getBitmapFile(taskId, userId);
        File bitmapReducedFile = getReducedResolutionBitmapFile(taskId, userId);
        protoFile.delete();
        bitmapFile.delete();
        bitmapReducedFile.delete();
    }
}
