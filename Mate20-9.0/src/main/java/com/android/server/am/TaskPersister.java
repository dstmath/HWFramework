package com.android.server.am;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.FileUtils;
import android.os.Process;
import android.os.SystemClock;
import android.util.ArraySet;
import android.util.AtomicFile;
import android.util.Slog;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.util.Xml;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.util.FastXmlSerializer;
import com.android.internal.util.XmlUtils;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import libcore.io.IoUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public class TaskPersister {
    static final boolean DEBUG = false;
    private static final long FLUSH_QUEUE = -1;
    private static final String IMAGES_DIRNAME = "recent_images";
    static final String IMAGE_EXTENSION = ".png";
    private static final long INTER_WRITE_DELAY_MS = 500;
    private static final int MAX_WRITE_QUEUE_LENGTH = 6;
    private static final String PERSISTED_TASK_IDS_FILENAME = "persisted_taskIds.txt";
    private static final long PRE_TASK_DELAY_MS = 3000;
    static final String TAG = "TaskPersister";
    private static final String TAG_TASK = "task";
    private static final String TASKS_DIRNAME = "recent_tasks";
    private static final String TASK_FILENAME_SUFFIX = "_task.xml";
    private final Object mIoLock = new Object();
    private final LazyTaskWriterThread mLazyTaskWriterThread;
    /* access modifiers changed from: private */
    public long mNextWriteTime = 0;
    /* access modifiers changed from: private */
    public final RecentTasks mRecentTasks;
    /* access modifiers changed from: private */
    public final ActivityManagerService mService;
    private final ActivityStackSupervisor mStackSupervisor;
    private final File mTaskIdsDir;
    private final SparseArray<SparseBooleanArray> mTaskIdsInFile = new SparseArray<>();
    ArrayList<WriteQueueItem> mWriteQueue = new ArrayList<>();

    private static class ImageWriteQueueItem extends WriteQueueItem {
        final String mFilePath;
        Bitmap mImage;

        ImageWriteQueueItem(String filePath, Bitmap image) {
            super();
            this.mFilePath = filePath;
            this.mImage = image;
        }
    }

    private class LazyTaskWriterThread extends Thread {
        LazyTaskWriterThread(String name) {
            super(name);
        }

        public void run() {
            boolean probablyDone;
            Process.setThreadPriority(10);
            ArraySet<Integer> persistentTaskIds = new ArraySet<>();
            while (true) {
                synchronized (TaskPersister.this) {
                    probablyDone = TaskPersister.this.mWriteQueue.isEmpty();
                }
                if (probablyDone) {
                    persistentTaskIds.clear();
                    synchronized (TaskPersister.this.mService) {
                        try {
                            ActivityManagerService.boostPriorityForLockedSection();
                            TaskPersister.this.mRecentTasks.getPersistableTaskIds(persistentTaskIds);
                            TaskPersister.this.mService.mWindowManager.removeObsoleteTaskFiles(persistentTaskIds, TaskPersister.this.mRecentTasks.usersWithRecentsLoadedLocked());
                        } catch (Throwable th) {
                            while (true) {
                                ActivityManagerService.resetPriorityAfterLockedSection();
                                throw th;
                            }
                        }
                    }
                    ActivityManagerService.resetPriorityAfterLockedSection();
                    TaskPersister.this.removeObsoleteFiles(persistentTaskIds);
                }
                TaskPersister.this.writeTaskIdsFiles();
                processNextItem();
            }
        }

        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v5, resolved type: android.util.AtomicFile} */
        /* JADX WARNING: type inference failed for: r2v3 */
        /* JADX WARNING: type inference failed for: r2v7, types: [java.lang.AutoCloseable] */
        /* JADX WARNING: type inference failed for: r2v13 */
        /* JADX WARNING: type inference failed for: r2v14 */
        /* JADX WARNING: type inference failed for: r2v15 */
        /* JADX WARNING: type inference failed for: r2v16 */
        /* JADX WARNING: Multi-variable type inference failed */
        private void processNextItem() {
            WriteQueueItem item;
            synchronized (TaskPersister.this) {
                if (TaskPersister.this.mNextWriteTime != -1) {
                    long unused = TaskPersister.this.mNextWriteTime = SystemClock.uptimeMillis() + 500;
                }
                while (TaskPersister.this.mWriteQueue.isEmpty()) {
                    if (TaskPersister.this.mNextWriteTime != 0) {
                        long unused2 = TaskPersister.this.mNextWriteTime = 0;
                        TaskPersister.this.notifyAll();
                    }
                    try {
                        TaskPersister.this.wait();
                    } catch (InterruptedException e) {
                    }
                }
                item = TaskPersister.this.mWriteQueue.remove(0);
                for (long now = SystemClock.uptimeMillis(); now < TaskPersister.this.mNextWriteTime; now = SystemClock.uptimeMillis()) {
                    try {
                        TaskPersister.this.wait(TaskPersister.this.mNextWriteTime - now);
                    } catch (InterruptedException e2) {
                    }
                }
            }
            WriteQueueItem item2 = item;
            ? atomicFile = 0;
            if (item2 instanceof ImageWriteQueueItem) {
                ImageWriteQueueItem imageWriteQueueItem = (ImageWriteQueueItem) item2;
                String filePath = imageWriteQueueItem.mFilePath;
                if (!TaskPersister.createParentDirectory(filePath)) {
                    Slog.e(TaskPersister.TAG, "Error while creating images directory for file: " + filePath);
                    return;
                }
                Bitmap bitmap = imageWriteQueueItem.mImage;
                try {
                    FileOutputStream imageFile = new FileOutputStream(new File(filePath));
                    atomicFile = imageFile;
                    if (bitmap != null) {
                        atomicFile = imageFile;
                        if (!bitmap.isRecycled()) {
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, imageFile);
                            atomicFile = imageFile;
                        }
                    }
                } catch (Exception e3) {
                    Slog.e(TaskPersister.TAG, "saveImage: unable to save " + filePath, e3);
                } catch (Throwable th) {
                    IoUtils.closeQuietly(null);
                    throw th;
                }
                IoUtils.closeQuietly(atomicFile);
            } else if (item2 instanceof TaskWriteQueueItem) {
                StringWriter stringWriter = null;
                TaskRecord task = ((TaskWriteQueueItem) item2).mTask;
                synchronized (TaskPersister.this.mService) {
                    try {
                        ActivityManagerService.boostPriorityForLockedSection();
                        if (task.inRecents) {
                            try {
                                stringWriter = TaskPersister.this.saveToXml(task);
                            } catch (IOException | XmlPullParserException e4) {
                            }
                        }
                    } catch (Throwable th2) {
                        while (true) {
                            ActivityManagerService.resetPriorityAfterLockedSection();
                            throw th2;
                        }
                    }
                }
                ActivityManagerService.resetPriorityAfterLockedSection();
                if (stringWriter != null) {
                    FileOutputStream file = null;
                    try {
                        File userTasksDir = TaskPersister.getUserTasksDir(task.userId);
                        AtomicFile atomicFile2 = new AtomicFile(new File(userTasksDir, String.valueOf(task.taskId) + TaskPersister.TASK_FILENAME_SUFFIX));
                        file = atomicFile2.startWrite();
                        file.write(stringWriter.toString().getBytes());
                        file.write(10);
                        atomicFile2.finishWrite(file);
                        atomicFile = atomicFile2;
                    } catch (IOException e5) {
                        if (file != null) {
                            atomicFile.failWrite(file);
                        }
                        Slog.e(TaskPersister.TAG, "Unable to open " + atomicFile + " for persisting. " + e5);
                    }
                }
            }
        }
    }

    private static class TaskWriteQueueItem extends WriteQueueItem {
        final TaskRecord mTask;

        TaskWriteQueueItem(TaskRecord task) {
            super();
            this.mTask = task;
        }
    }

    private static class WriteQueueItem {
        private WriteQueueItem() {
        }
    }

    TaskPersister(File systemDir, ActivityStackSupervisor stackSupervisor, ActivityManagerService service, RecentTasks recentTasks) {
        File legacyImagesDir = new File(systemDir, IMAGES_DIRNAME);
        if (legacyImagesDir.exists() && (!FileUtils.deleteContents(legacyImagesDir) || !legacyImagesDir.delete())) {
            Slog.i(TAG, "Failure deleting legacy images directory: " + legacyImagesDir);
        }
        File legacyTasksDir = new File(systemDir, TASKS_DIRNAME);
        if (legacyTasksDir.exists() && (!FileUtils.deleteContents(legacyTasksDir) || !legacyTasksDir.delete())) {
            Slog.i(TAG, "Failure deleting legacy tasks directory: " + legacyTasksDir);
        }
        this.mTaskIdsDir = new File(Environment.getDataDirectory(), "system_de");
        this.mStackSupervisor = stackSupervisor;
        this.mService = service;
        this.mRecentTasks = recentTasks;
        this.mLazyTaskWriterThread = new LazyTaskWriterThread("LazyTaskWriterThread");
    }

    @VisibleForTesting
    TaskPersister(File workingDir) {
        this.mTaskIdsDir = workingDir;
        this.mStackSupervisor = null;
        this.mService = null;
        this.mRecentTasks = null;
        this.mLazyTaskWriterThread = new LazyTaskWriterThread("LazyTaskWriterThreadTest");
    }

    /* access modifiers changed from: package-private */
    public void startPersisting() {
        if (!this.mLazyTaskWriterThread.isAlive()) {
            this.mLazyTaskWriterThread.start();
        }
    }

    private void removeThumbnails(TaskRecord task) {
        String taskString = Integer.toString(task.taskId);
        for (int queueNdx = this.mWriteQueue.size() - 1; queueNdx >= 0; queueNdx--) {
            WriteQueueItem item = this.mWriteQueue.get(queueNdx);
            if ((item instanceof ImageWriteQueueItem) && new File(((ImageWriteQueueItem) item).mFilePath).getName().startsWith(taskString)) {
                this.mWriteQueue.remove(queueNdx);
            }
        }
    }

    private void yieldIfQueueTooDeep() {
        boolean stall = false;
        synchronized (this) {
            if (this.mNextWriteTime == -1) {
                stall = true;
            }
        }
        if (stall) {
            Thread.yield();
        }
    }

    /* access modifiers changed from: package-private */
    public SparseBooleanArray loadPersistedTaskIdsForUser(int userId) {
        if (this.mTaskIdsInFile.get(userId) != null) {
            return this.mTaskIdsInFile.get(userId).clone();
        }
        SparseBooleanArray persistedTaskIds = new SparseBooleanArray();
        synchronized (this.mIoLock) {
            try {
                BufferedReader reader = new BufferedReader(new FileReader(getUserPersistedTaskIdsFile(userId)));
                while (true) {
                    String readLine = reader.readLine();
                    String line = readLine;
                    if (readLine == null) {
                        break;
                    }
                    for (String taskIdString : line.split("\\s+")) {
                        persistedTaskIds.put(Integer.parseInt(taskIdString), true);
                    }
                }
                IoUtils.closeQuietly(reader);
            } catch (FileNotFoundException e) {
                IoUtils.closeQuietly(null);
            } catch (Exception e2) {
                try {
                    Slog.e(TAG, "Error while reading taskIds file for user " + userId, e2);
                    IoUtils.closeQuietly(null);
                } catch (Throwable th) {
                    IoUtils.closeQuietly(null);
                    throw th;
                }
            }
        }
        this.mTaskIdsInFile.put(userId, persistedTaskIds);
        return persistedTaskIds.clone();
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void writePersistedTaskIdsForUser(SparseBooleanArray taskIds, int userId) {
        if (userId >= 0 && taskIds != null) {
            File persistedTaskIdsFile = getUserPersistedTaskIdsFile(userId);
            synchronized (this.mIoLock) {
                try {
                    BufferedWriter writer = new BufferedWriter(new FileWriter(persistedTaskIdsFile));
                    for (int i = 0; i < taskIds.size(); i++) {
                        if (taskIds.valueAt(i)) {
                            writer.write(String.valueOf(taskIds.keyAt(i)));
                            writer.newLine();
                        }
                    }
                    IoUtils.closeQuietly(writer);
                } catch (Exception e) {
                    try {
                        Slog.e(TAG, "Error while writing taskIds file for user " + userId, e);
                        IoUtils.closeQuietly(null);
                    } catch (Throwable th) {
                        IoUtils.closeQuietly(null);
                        throw th;
                    }
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void unloadUserDataFromMemory(int userId) {
        this.mTaskIdsInFile.delete(userId);
    }

    /* access modifiers changed from: package-private */
    public void wakeup(TaskRecord task, boolean flush) {
        synchronized (this) {
            if (task != null) {
                try {
                    int queueNdx = this.mWriteQueue.size() - 1;
                    while (true) {
                        if (queueNdx < 0) {
                            break;
                        }
                        WriteQueueItem item = this.mWriteQueue.get(queueNdx);
                        if (!(item instanceof TaskWriteQueueItem) || ((TaskWriteQueueItem) item).mTask != task) {
                            queueNdx--;
                        } else if (!task.inRecents) {
                            removeThumbnails(task);
                        }
                    }
                    if (queueNdx < 0 && task.isPersistable) {
                        this.mWriteQueue.add(new TaskWriteQueueItem(task));
                    }
                } catch (Throwable th) {
                    while (true) {
                        throw th;
                    }
                }
            } else {
                this.mWriteQueue.add(new WriteQueueItem());
            }
            if (!flush) {
                if (this.mWriteQueue.size() <= 6) {
                    if (this.mNextWriteTime == 0) {
                        this.mNextWriteTime = SystemClock.uptimeMillis() + PRE_TASK_DELAY_MS;
                    }
                    notifyAll();
                }
            }
            this.mNextWriteTime = -1;
            notifyAll();
        }
        yieldIfQueueTooDeep();
    }

    /* access modifiers changed from: package-private */
    public void flush() {
        synchronized (this) {
            this.mNextWriteTime = -1;
            notifyAll();
            do {
                try {
                    wait();
                } catch (InterruptedException e) {
                }
            } while (this.mNextWriteTime == -1);
        }
    }

    /* access modifiers changed from: package-private */
    public void saveImage(Bitmap image, String filePath) {
        synchronized (this) {
            int queueNdx = this.mWriteQueue.size() - 1;
            while (true) {
                if (queueNdx < 0) {
                    break;
                }
                WriteQueueItem item = this.mWriteQueue.get(queueNdx);
                if (item instanceof ImageWriteQueueItem) {
                    ImageWriteQueueItem imageWriteQueueItem = (ImageWriteQueueItem) item;
                    if (imageWriteQueueItem.mFilePath.equals(filePath)) {
                        imageWriteQueueItem.mImage = image;
                        break;
                    }
                }
                queueNdx--;
            }
            if (queueNdx < 0) {
                this.mWriteQueue.add(new ImageWriteQueueItem(filePath, image));
            }
            if (this.mWriteQueue.size() > 6) {
                this.mNextWriteTime = -1;
            } else if (this.mNextWriteTime == 0) {
                this.mNextWriteTime = SystemClock.uptimeMillis() + PRE_TASK_DELAY_MS;
            }
            notifyAll();
        }
        yieldIfQueueTooDeep();
    }

    /* access modifiers changed from: package-private */
    public Bitmap getTaskDescriptionIcon(String filePath) {
        Bitmap icon = getImageFromWriteQueue(filePath);
        if (icon != null) {
            return icon;
        }
        return restoreImage(filePath);
    }

    /* access modifiers changed from: package-private */
    public Bitmap getImageFromWriteQueue(String filePath) {
        synchronized (this) {
            for (int queueNdx = this.mWriteQueue.size() - 1; queueNdx >= 0; queueNdx--) {
                WriteQueueItem item = this.mWriteQueue.get(queueNdx);
                if (item instanceof ImageWriteQueueItem) {
                    ImageWriteQueueItem imageWriteQueueItem = (ImageWriteQueueItem) item;
                    if (imageWriteQueueItem.mFilePath.equals(filePath)) {
                        Bitmap bitmap = imageWriteQueueItem.mImage;
                        return bitmap;
                    }
                }
            }
            return null;
        }
    }

    /* access modifiers changed from: private */
    public StringWriter saveToXml(TaskRecord task) throws IOException, XmlPullParserException {
        XmlSerializer xmlSerializer = new FastXmlSerializer();
        StringWriter stringWriter = new StringWriter();
        xmlSerializer.setOutput(stringWriter);
        xmlSerializer.startDocument(null, true);
        xmlSerializer.startTag(null, TAG_TASK);
        task.saveToXml(xmlSerializer);
        xmlSerializer.endTag(null, TAG_TASK);
        xmlSerializer.endDocument();
        xmlSerializer.flush();
        return stringWriter;
    }

    private String fileToString(File file) {
        String newline = System.lineSeparator();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            StringBuffer sb = new StringBuffer(((int) file.length()) * 2);
            while (true) {
                String readLine = reader.readLine();
                String line = readLine;
                if (readLine != null) {
                    sb.append(line + newline);
                } else {
                    reader.close();
                    return sb.toString();
                }
            }
        } catch (IOException e) {
            Slog.e(TAG, "Couldn't read file " + file.getName());
            return null;
        }
    }

    private TaskRecord taskIdToTask(int taskId, ArrayList<TaskRecord> tasks) {
        if (taskId < 0) {
            return null;
        }
        for (int taskNdx = tasks.size() - 1; taskNdx >= 0; taskNdx--) {
            TaskRecord task = tasks.get(taskNdx);
            if (task.taskId == taskId) {
                return task;
            }
        }
        Slog.e(TAG, "Restore affiliation error looking for taskId=" + taskId);
        return null;
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Removed duplicated region for block: B:71:0x01f8  */
    /* JADX WARNING: Removed duplicated region for block: B:87:0x0209 A[SYNTHETIC] */
    public List<TaskRecord> restoreTasksForUserLocked(int userId, SparseBooleanArray preaddedTasks) {
        int taskNdx;
        File[] recentFiles;
        File[] recentFiles2;
        int i = userId;
        ArrayList<TaskRecord> tasks = new ArrayList<>();
        ArraySet arraySet = new ArraySet();
        File userTasksDir = getUserTasksDir(userId);
        File[] recentFiles3 = userTasksDir.listFiles();
        if (recentFiles3 == null) {
            Slog.e(TAG, "restoreTasksForUserLocked: Unable to list files from " + userTasksDir);
            return tasks;
        }
        boolean z = false;
        int taskNdx2 = 0;
        while (true) {
            int taskNdx3 = taskNdx2;
            int i2 = 1;
            if (taskNdx3 >= recentFiles3.length) {
                break;
            }
            File taskFile = recentFiles3[taskNdx3];
            if (taskFile.getName().endsWith(TASK_FILENAME_SUFFIX)) {
                try {
                    if (preaddedTasks.get(Integer.parseInt(taskFile.getName().substring(z ? 1 : 0, taskFile.getName().length() - TASK_FILENAME_SUFFIX.length())), z)) {
                        try {
                            Slog.w(TAG, "Task #" + taskId + " has already been created so we don't restore again");
                        } catch (NumberFormatException e) {
                            e = e;
                            recentFiles2 = recentFiles3;
                            taskNdx = taskNdx3;
                        }
                    } else {
                        BufferedReader reader = null;
                        boolean deleteFile = z;
                        try {
                            reader = new BufferedReader(new FileReader(taskFile));
                            XmlPullParser in = Xml.newPullParser();
                            in.setInput(reader);
                            while (true) {
                                int next = in.next();
                                int event = next;
                                if (next == i2 || event == 3) {
                                    recentFiles = recentFiles3;
                                    taskNdx = taskNdx3;
                                    IoUtils.closeQuietly(reader);
                                } else {
                                    String name = in.getName();
                                    if (event != 2) {
                                        recentFiles = recentFiles3;
                                        taskNdx = taskNdx3;
                                    } else if (TAG_TASK.equals(name)) {
                                        TaskRecord task = TaskRecord.restoreFromXml(in, this.mStackSupervisor);
                                        if (task != null) {
                                            int taskId = task.taskId;
                                            recentFiles = recentFiles3;
                                            try {
                                                if (this.mStackSupervisor.anyTaskForIdLocked(taskId, 1) != null) {
                                                    StringBuilder sb = new StringBuilder();
                                                    taskNdx = taskNdx3;
                                                    try {
                                                        sb.append("Existing task with taskId ");
                                                        sb.append(taskId);
                                                        sb.append("found");
                                                        Slog.wtf(TAG, sb.toString());
                                                    } catch (Exception e2) {
                                                        e = e2;
                                                        try {
                                                            Slog.wtf(TAG, "Unable to parse " + taskFile + ". Error ", e);
                                                            StringBuilder sb2 = new StringBuilder();
                                                            sb2.append("Failing file: ");
                                                            sb2.append(fileToString(taskFile));
                                                            Slog.e(TAG, sb2.toString());
                                                            IoUtils.closeQuietly(reader);
                                                            if (1 == 0) {
                                                            }
                                                            taskFile.delete();
                                                            taskNdx2 = taskNdx + 1;
                                                            recentFiles3 = recentFiles;
                                                            z = false;
                                                        } catch (Throwable th) {
                                                            th = th;
                                                            IoUtils.closeQuietly(reader);
                                                            if (deleteFile) {
                                                                taskFile.delete();
                                                            }
                                                            throw th;
                                                        }
                                                    }
                                                } else {
                                                    taskNdx = taskNdx3;
                                                    if (i != task.userId) {
                                                        Slog.wtf(TAG, "Task with userId " + task.userId + " found in " + userTasksDir.getAbsolutePath());
                                                    } else {
                                                        this.mStackSupervisor.setNextTaskIdForUserLocked(taskId, i);
                                                        task.isPersistable = true;
                                                        tasks.add(task);
                                                        arraySet.add(Integer.valueOf(taskId));
                                                    }
                                                }
                                            } catch (Exception e3) {
                                                e = e3;
                                                taskNdx = taskNdx3;
                                                Slog.wtf(TAG, "Unable to parse " + taskFile + ". Error ", e);
                                                StringBuilder sb22 = new StringBuilder();
                                                sb22.append("Failing file: ");
                                                sb22.append(fileToString(taskFile));
                                                Slog.e(TAG, sb22.toString());
                                                IoUtils.closeQuietly(reader);
                                                if (1 == 0) {
                                                    taskNdx2 = taskNdx + 1;
                                                    recentFiles3 = recentFiles;
                                                    z = false;
                                                }
                                                taskFile.delete();
                                                taskNdx2 = taskNdx + 1;
                                                recentFiles3 = recentFiles;
                                                z = false;
                                            } catch (Throwable th2) {
                                                th = th2;
                                                int i3 = taskNdx3;
                                                IoUtils.closeQuietly(reader);
                                                if (deleteFile) {
                                                }
                                                throw th;
                                            }
                                        } else {
                                            recentFiles = recentFiles3;
                                            taskNdx = taskNdx3;
                                            Slog.e(TAG, "restoreTasksForUserLocked: Unable to restore taskFile=" + taskFile + ": " + fileToString(taskFile));
                                        }
                                    } else {
                                        recentFiles = recentFiles3;
                                        taskNdx = taskNdx3;
                                        Slog.wtf(TAG, "restoreTasksForUserLocked: Unknown xml event=" + event + " name=" + name);
                                    }
                                    XmlUtils.skipCurrentTag(in);
                                    recentFiles3 = recentFiles;
                                    taskNdx3 = taskNdx;
                                    i2 = 1;
                                    SparseBooleanArray sparseBooleanArray = preaddedTasks;
                                }
                            }
                            recentFiles = recentFiles3;
                            taskNdx = taskNdx3;
                            IoUtils.closeQuietly(reader);
                            if (!deleteFile) {
                                taskNdx2 = taskNdx + 1;
                                recentFiles3 = recentFiles;
                                z = false;
                            }
                        } catch (Exception e4) {
                            e = e4;
                            recentFiles = recentFiles3;
                            taskNdx = taskNdx3;
                            Slog.wtf(TAG, "Unable to parse " + taskFile + ". Error ", e);
                            StringBuilder sb222 = new StringBuilder();
                            sb222.append("Failing file: ");
                            sb222.append(fileToString(taskFile));
                            Slog.e(TAG, sb222.toString());
                            IoUtils.closeQuietly(reader);
                            if (1 == 0) {
                            }
                            taskFile.delete();
                            taskNdx2 = taskNdx + 1;
                            recentFiles3 = recentFiles;
                            z = false;
                        } catch (Throwable th3) {
                            th = th3;
                            File[] fileArr = recentFiles3;
                            int i4 = taskNdx3;
                            IoUtils.closeQuietly(reader);
                            if (deleteFile) {
                            }
                            throw th;
                        }
                        taskFile.delete();
                        taskNdx2 = taskNdx + 1;
                        recentFiles3 = recentFiles;
                        z = false;
                    }
                } catch (NumberFormatException e5) {
                    e = e5;
                    recentFiles2 = recentFiles3;
                    taskNdx = taskNdx3;
                    Slog.w(TAG, "Unexpected task file name", e);
                    taskNdx2 = taskNdx + 1;
                    recentFiles3 = recentFiles;
                    z = false;
                }
            }
            recentFiles = recentFiles3;
            taskNdx = taskNdx3;
            taskNdx2 = taskNdx + 1;
            recentFiles3 = recentFiles;
            z = false;
        }
        removeObsoleteFiles(arraySet, userTasksDir.listFiles());
        for (int taskNdx4 = tasks.size() - 1; taskNdx4 >= 0; taskNdx4--) {
            TaskRecord task2 = tasks.get(taskNdx4);
            task2.setPrevAffiliate(taskIdToTask(task2.mPrevAffiliateTaskId, tasks));
            task2.setNextAffiliate(taskIdToTask(task2.mNextAffiliateTaskId, tasks));
        }
        Collections.sort(tasks, new Comparator<TaskRecord>() {
            public int compare(TaskRecord lhs, TaskRecord rhs) {
                long diff = rhs.mLastTimeMoved - lhs.mLastTimeMoved;
                if (diff < 0) {
                    return -1;
                }
                if (diff > 0) {
                    return 1;
                }
                return 0;
            }
        });
        return tasks;
    }

    private static void removeObsoleteFiles(ArraySet<Integer> persistentTaskIds, File[] files) {
        if (files == null) {
            Slog.e(TAG, "File error accessing recents directory (directory doesn't exist?).");
            return;
        }
        for (File file : files) {
            String filename = file.getName();
            int taskIdEnd = filename.indexOf(95);
            if (taskIdEnd > 0) {
                try {
                    if (!persistentTaskIds.contains(Integer.valueOf(Integer.parseInt(filename.substring(0, taskIdEnd))))) {
                        file.delete();
                    }
                } catch (Exception e) {
                    Slog.wtf(TAG, "removeObsoleteFiles: Can't parse file=" + file.getName());
                    file.delete();
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public void writeTaskIdsFiles() {
        int i;
        SparseArray<SparseBooleanArray> changedTaskIdsPerUser = new SparseArray<>();
        synchronized (this.mService) {
            try {
                ActivityManagerService.boostPriorityForLockedSection();
                i = 0;
                for (int userId : this.mRecentTasks.usersWithRecentsLoadedLocked()) {
                    SparseBooleanArray taskIdsToSave = this.mRecentTasks.getTaskIdsForUser(userId);
                    SparseBooleanArray persistedIdsInFile = this.mTaskIdsInFile.get(userId);
                    if (persistedIdsInFile == null || !persistedIdsInFile.equals(taskIdsToSave)) {
                        SparseBooleanArray taskIdsToSaveCopy = taskIdsToSave.clone();
                        this.mTaskIdsInFile.put(userId, taskIdsToSaveCopy);
                        changedTaskIdsPerUser.put(userId, taskIdsToSaveCopy);
                    }
                }
            } catch (Throwable th) {
                while (true) {
                    ActivityManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
        ActivityManagerService.resetPriorityAfterLockedSection();
        while (true) {
            int i2 = i;
            if (i2 < changedTaskIdsPerUser.size()) {
                writePersistedTaskIdsForUser(changedTaskIdsPerUser.valueAt(i2), changedTaskIdsPerUser.keyAt(i2));
                i = i2 + 1;
            } else {
                return;
            }
        }
    }

    /* access modifiers changed from: private */
    public void removeObsoleteFiles(ArraySet<Integer> persistentTaskIds) {
        int[] candidateUserIds;
        synchronized (this.mService) {
            try {
                ActivityManagerService.boostPriorityForLockedSection();
                candidateUserIds = this.mRecentTasks.usersWithRecentsLoadedLocked();
            } catch (Throwable th) {
                while (true) {
                    ActivityManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
        ActivityManagerService.resetPriorityAfterLockedSection();
        for (int userId : candidateUserIds) {
            removeObsoleteFiles(persistentTaskIds, getUserImagesDir(userId).listFiles());
            removeObsoleteFiles(persistentTaskIds, getUserTasksDir(userId).listFiles());
        }
    }

    static Bitmap restoreImage(String filename) {
        return BitmapFactory.decodeFile(filename);
    }

    private File getUserPersistedTaskIdsFile(int userId) {
        File userTaskIdsDir = new File(this.mTaskIdsDir, String.valueOf(userId));
        if (!userTaskIdsDir.exists() && !userTaskIdsDir.mkdirs()) {
            Slog.e(TAG, "Error while creating user directory: " + userTaskIdsDir);
        }
        return new File(userTaskIdsDir, PERSISTED_TASK_IDS_FILENAME);
    }

    static File getUserTasksDir(int userId) {
        File userTasksDir = new File(Environment.getDataSystemCeDirectory(userId), TASKS_DIRNAME);
        if (!userTasksDir.exists() && !userTasksDir.mkdir()) {
            Slog.e(TAG, "Failure creating tasks directory for user " + userId + ": " + userTasksDir);
        }
        return userTasksDir;
    }

    static File getUserImagesDir(int userId) {
        return new File(Environment.getDataSystemCeDirectory(userId), IMAGES_DIRNAME);
    }

    /* access modifiers changed from: private */
    public static boolean createParentDirectory(String filePath) {
        File parentDir = new File(filePath).getParentFile();
        return parentDir.exists() || parentDir.mkdirs();
    }
}
