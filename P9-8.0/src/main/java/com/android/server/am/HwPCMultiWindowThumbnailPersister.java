package com.android.server.am;

import android.app.ActivityManager.TaskThumbnail;
import android.app.TaskStackListener;
import android.content.ComponentName;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.util.HwPCUtils;
import com.android.internal.os.BackgroundThread;
import com.android.server.wm.HwWindowManagerService;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.HashMap;
import libcore.io.IoUtils;

public class HwPCMultiWindowThumbnailPersister {
    private static final int DELETE_THUMBNAIL_DELAY = 2000;
    private static final int MSG_DELETE_THUMBNAIL = 1;
    private static final int MSG_SAVE_THUMBNAIL = 2;
    private static final int MSG_SCREENSHOT = 3;
    private static final int SAVE_THUMBNAIL_DELAY = 2000;
    private static final int SCREENSHOT_DELAY = 3000;
    private static final String TASK_THUMBNAILS_PATH = "/data/system/hw_recent/task_thumbnails";
    private static final String THUMBNAIL_EXTENSION = ".png";
    private static final String THUMBNAIL_SUFFIX = "_task_thumbnail";
    private static final File ThumbnailDir = new File(getTaskThumbnailsPath());
    private String TAG = "HwPCMultiWindowThumbnailPersister";
    private HwActivityManagerService mService;
    private TaskStackListener mTaskStackListener = new TaskStackListener() {
        public void onTaskRemoved(final int taskId) throws RemoteException {
            HwPCMultiWindowThumbnailPersister.this.mWorkerHandler.post(new Runnable() {
                public void run() {
                    TaskThumbnailItem item;
                    synchronized (HwPCMultiWindowThumbnailPersister.this) {
                        item = (TaskThumbnailItem) HwPCMultiWindowThumbnailPersister.this.mTasks.remove(Integer.valueOf(taskId));
                    }
                    HwPCMultiWindowThumbnailPersister.this.mWorkerHandler.sendMessageDelayed(HwPCMultiWindowThumbnailPersister.this.mWorkerHandler.obtainMessage(1, item), 2000);
                }
            });
        }

        public void onTaskCreated(int taskId, ComponentName componentName) throws RemoteException {
            synchronized (HwPCMultiWindowThumbnailPersister.this) {
                HwPCMultiWindowThumbnailPersister.this.mTasks.put(Integer.valueOf(taskId), new TaskThumbnailItem(taskId));
            }
            HwPCMultiWindowThumbnailPersister.this.mWorkerHandler.sendMessageDelayed(HwPCMultiWindowThumbnailPersister.this.mWorkerHandler.obtainMessage(3, Integer.valueOf(taskId)), 3000);
        }
    };
    HashMap<Integer, TaskThumbnailItem> mTasks = new HashMap();
    Handler mWorkerHandler = new WorkerHandler(BackgroundThread.getHandler().getLooper());

    private static class TaskThumbnailItem {
        String mReason;
        final int mTaskId;
        Bitmap mThumbnail;
        final File mThumbnailFile;

        TaskThumbnailItem(int taskId) {
            this.mTaskId = taskId;
            this.mThumbnailFile = new File(HwPCMultiWindowThumbnailPersister.ThumbnailDir, String.valueOf(taskId) + HwPCMultiWindowThumbnailPersister.THUMBNAIL_SUFFIX + HwPCMultiWindowThumbnailPersister.THUMBNAIL_EXTENSION);
        }

        public String getReason() {
            return this.mReason;
        }
    }

    private class WorkerHandler extends Handler {
        public WorkerHandler(Looper looper) {
            super(looper);
        }

        /* JADX WARNING: Missing block: B:48:0x0098, code:
            return;
     */
        /* JADX WARNING: Missing block: B:53:0x00a2, code:
            r9 = com.android.server.am.HwPCMultiWindowThumbnailPersister.-get2(r12.this$0);
     */
        /* JADX WARNING: Missing block: B:54:0x00a8, code:
            monitor-enter(r9);
     */
        /* JADX WARNING: Missing block: B:56:?, code:
            r7 = com.android.server.am.HwPCMultiWindowThumbnailPersister.-get2(r12.this$0).mStackSupervisor.anyTaskForIdLocked(r6, 1, -1);
     */
        /* JADX WARNING: Missing block: B:57:0x00b7, code:
            if (r7 == null) goto L_0x00bd;
     */
        /* JADX WARNING: Missing block: B:59:0x00bb, code:
            if (r7.mStack != null) goto L_0x00c2;
     */
        /* JADX WARNING: Missing block: B:60:0x00bd, code:
            monitor-exit(r9);
     */
        /* JADX WARNING: Missing block: B:61:0x00be, code:
            return;
     */
        /* JADX WARNING: Missing block: B:67:0x00ce, code:
            if (((com.android.server.am.HwPCMultiWindowThumbnailPersister.-get2(r12.this$0).mWindowManager instanceof com.android.server.wm.HwWindowManagerService) ^ 1) != 0) goto L_0x00bd;
     */
        /* JADX WARNING: Missing block: B:68:0x00d0, code:
            r5 = r7.topRunningActivityLocked();
     */
        /* JADX WARNING: Missing block: B:69:0x00d4, code:
            if (r5 != null) goto L_0x00d8;
     */
        /* JADX WARNING: Missing block: B:70:0x00d6, code:
            monitor-exit(r9);
     */
        /* JADX WARNING: Missing block: B:71:0x00d7, code:
            return;
     */
        /* JADX WARNING: Missing block: B:73:?, code:
            r0 = ((com.android.server.wm.HwWindowManagerService) com.android.server.am.HwPCMultiWindowThumbnailPersister.-get2(r12.this$0).mWindowManager).getTaskSnapshotForPc(r5.getDisplayId(), r5.appToken);
     */
        /* JADX WARNING: Missing block: B:74:0x00ec, code:
            monitor-exit(r9);
     */
        /* JADX WARNING: Missing block: B:75:0x00ed, code:
            if (r0 == null) goto L_0x0005;
     */
        /* JADX WARNING: Missing block: B:76:0x00ef, code:
            com.android.server.am.HwPCMultiWindowThumbnailPersister.-wrap0(r12.this$0, r6, r0, "onTaskAdded");
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void handleMessage(Message msg) {
            Object imageFile;
            Throwable th;
            TaskThumbnailItem item;
            switch (msg.what) {
                case 1:
                    try {
                        item = msg.obj;
                        if (!(item == null || !item.mThumbnailFile.exists() || item.mThumbnailFile.delete())) {
                            HwPCUtils.log(HwPCMultiWindowThumbnailPersister.this.TAG, "MSG_DELETE_THUMBNAIL delete thumbnail file failed!!!");
                            break;
                        }
                    } catch (Exception e) {
                        HwPCUtils.log(HwPCMultiWindowThumbnailPersister.this.TAG, "MSG_DELETE_THUMBNAIL failed!!!");
                        break;
                    }
                case 2:
                    synchronized (HwPCMultiWindowThumbnailPersister.this) {
                        AutoCloseable imageFile2 = null;
                        try {
                            item = (TaskThumbnailItem) msg.obj;
                            if (!(item == null || item.mThumbnail == null)) {
                                FileOutputStream imageFile3 = new FileOutputStream(item.mThumbnailFile);
                                try {
                                    item.mThumbnail.compress(CompressFormat.PNG, 100, imageFile3);
                                    item.mThumbnail = null;
                                    imageFile2 = imageFile3;
                                } catch (Exception e2) {
                                    imageFile2 = imageFile3;
                                    try {
                                        HwPCUtils.log(HwPCMultiWindowThumbnailPersister.this.TAG, "MSG_SAVE_THUMBNAIL failed!!!");
                                        IoUtils.closeQuietly(imageFile2);
                                    } catch (Throwable th2) {
                                        th = th2;
                                        IoUtils.closeQuietly(imageFile2);
                                        throw th;
                                    }
                                } catch (Throwable th3) {
                                    th = th3;
                                    imageFile2 = imageFile3;
                                    IoUtils.closeQuietly(imageFile2);
                                    throw th;
                                }
                            }
                            IoUtils.closeQuietly(imageFile2);
                        } catch (Exception e3) {
                            HwPCUtils.log(HwPCMultiWindowThumbnailPersister.this.TAG, "MSG_SAVE_THUMBNAIL failed!!!");
                            IoUtils.closeQuietly(imageFile2);
                        }
                    }
                case 3:
                    int taskId = ((Integer) msg.obj).intValue();
                    synchronized (HwPCMultiWindowThumbnailPersister.this) {
                        item = (TaskThumbnailItem) HwPCMultiWindowThumbnailPersister.this.mTasks.get(Integer.valueOf(taskId));
                        if (item != null && item.mThumbnail == null) {
                            if (!item.mThumbnailFile.exists()) {
                            }
                        }
                    }
                    break;
            }
        }
    }

    private static String getTaskThumbnailsPath() {
        return TASK_THUMBNAILS_PATH;
    }

    public HwPCMultiWindowThumbnailPersister(ActivityManagerService service) {
        this.mService = (HwActivityManagerService) service;
        if (ThumbnailDir.isDirectory() && ThumbnailDir.exists()) {
            String[] children = ThumbnailDir.list();
            if (children != null) {
                for (String file : children) {
                    if (!new File(ThumbnailDir, file).delete()) {
                        HwPCUtils.log(this.TAG, "HwPCMultiWindowThumbnailPersister delete fail!!!");
                    }
                }
            }
        }
        if (!ThumbnailDir.mkdirs()) {
            HwPCUtils.log(this.TAG, "HwPCMultiWindowThumbnailPersister mkdirs fail!!!");
        }
        this.mService.registerHwTaskStackListener(this.mTaskStackListener);
    }

    public TaskThumbnail getTaskThumbnail(int taskId) {
        TaskThumbnail taskThumbnail = new TaskThumbnail();
        Bitmap bitmap = null;
        synchronized (this.mService) {
            TaskRecord tr = this.mService.mStackSupervisor.anyTaskForIdLocked(taskId, 1, -1);
            if (!(tr == null || tr.mStack == null || !(this.mService.mWindowManager instanceof HwWindowManagerService))) {
                ActivityRecord r = tr.topRunningActivityLocked();
                if (r != null) {
                    bitmap = ((HwWindowManagerService) this.mService.mWindowManager).getTaskSnapshotForPc(r.getDisplayId(), r.appToken);
                }
            }
        }
        if (bitmap != null) {
            updateThumbnail(taskId, bitmap, "getTaskThumbnail");
        }
        synchronized (this) {
            TaskThumbnailItem item = (TaskThumbnailItem) this.mTasks.get(Integer.valueOf(taskId));
            if (item != null) {
                taskThumbnail.mainThumbnail = item.mThumbnail;
                if (taskThumbnail.mainThumbnail == null && item.mThumbnailFile.exists()) {
                    try {
                        taskThumbnail.thumbnailFileDescriptor = ParcelFileDescriptor.open(item.mThumbnailFile, 268435456);
                    } catch (FileNotFoundException e) {
                        HwPCUtils.log(this.TAG, "getTaskThumbnail FileNotFoundException");
                    }
                }
            }
        }
        return taskThumbnail;
    }

    private void updateThumbnail(int taskId, Bitmap thumbnail, String reason) {
        HwPCUtils.log(this.TAG, "updateThumbnail: taskId " + taskId + " reason " + reason + " thumbnail " + thumbnail);
        synchronized (this) {
            TaskThumbnailItem item = (TaskThumbnailItem) this.mTasks.get(Integer.valueOf(taskId));
            if (!(item == null || thumbnail == null)) {
                item.mThumbnail = thumbnail;
                item.mReason = reason;
                this.mWorkerHandler.removeMessages(2, item);
                this.mWorkerHandler.sendMessageDelayed(this.mWorkerHandler.obtainMessage(2, item), 2000);
            }
        }
    }
}
