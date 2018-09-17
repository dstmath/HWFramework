package com.android.server.am;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
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
    private long mNextWriteTime = 0;
    private final RecentTasks mRecentTasks;
    private final ActivityManagerService mService;
    private final ActivityStackSupervisor mStackSupervisor;
    private final File mTaskIdsDir;
    private final SparseArray<SparseBooleanArray> mTaskIdsInFile = new SparseArray();
    ArrayList<WriteQueueItem> mWriteQueue = new ArrayList();

    private static class WriteQueueItem {
        /* synthetic */ WriteQueueItem(WriteQueueItem -this0) {
            this();
        }

        private WriteQueueItem() {
        }
    }

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
            Exception e;
            Object imageFile;
            Throwable th;
            IOException e2;
            Process.setThreadPriority(10);
            ArraySet<Integer> persistentTaskIds = new ArraySet();
            while (true) {
                boolean probablyDone;
                TaskRecord task;
                WriteQueueItem item;
                synchronized (TaskPersister.this) {
                    probablyDone = TaskPersister.this.mWriteQueue.isEmpty();
                }
                if (probablyDone) {
                    persistentTaskIds.clear();
                    synchronized (TaskPersister.this.mService) {
                        try {
                            ActivityManagerService.boostPriorityForLockedSection();
                            for (int taskNdx = TaskPersister.this.mRecentTasks.size() - 1; taskNdx >= 0; taskNdx--) {
                                task = (TaskRecord) TaskPersister.this.mRecentTasks.get(taskNdx);
                                ActivityStack stack = task.getStack();
                                if ((task.isPersistable || task.inRecents) && (stack == null || (stack.isHomeOrRecentsStack() ^ 1) != 0)) {
                                    persistentTaskIds.add(Integer.valueOf(task.taskId));
                                }
                            }
                            TaskPersister.this.mService.mWindowManager.removeObsoleteTaskFiles(persistentTaskIds, TaskPersister.this.mRecentTasks.usersWithRecentsLoadedLocked());
                        } finally {
                            ActivityManagerService.resetPriorityAfterLockedSection();
                        }
                    }
                    TaskPersister.this.removeObsoleteFiles(persistentTaskIds);
                }
                TaskPersister.this.writeTaskIdsFiles();
                synchronized (TaskPersister.this) {
                    if (TaskPersister.this.mNextWriteTime != -1) {
                        TaskPersister.this.mNextWriteTime = SystemClock.uptimeMillis() + 500;
                    }
                    while (TaskPersister.this.mWriteQueue.isEmpty()) {
                        if (TaskPersister.this.mNextWriteTime != 0) {
                            TaskPersister.this.mNextWriteTime = 0;
                            TaskPersister.this.notifyAll();
                        }
                        try {
                            TaskPersister.this.wait();
                        } catch (InterruptedException e3) {
                        }
                    }
                    item = (WriteQueueItem) TaskPersister.this.mWriteQueue.remove(0);
                    for (long now = SystemClock.uptimeMillis(); now < TaskPersister.this.mNextWriteTime; now = SystemClock.uptimeMillis()) {
                        try {
                            TaskPersister.this.wait(TaskPersister.this.mNextWriteTime - now);
                        } catch (InterruptedException e4) {
                        }
                    }
                }
                if (item instanceof ImageWriteQueueItem) {
                    ImageWriteQueueItem imageWriteQueueItem = (ImageWriteQueueItem) item;
                    String filePath = imageWriteQueueItem.mFilePath;
                    if (TaskPersister.createParentDirectory(filePath)) {
                        Bitmap bitmap = imageWriteQueueItem.mImage;
                        AutoCloseable imageFile2 = null;
                        try {
                            FileOutputStream imageFile3 = new FileOutputStream(new File(filePath));
                            if (bitmap != null) {
                                try {
                                    if ((bitmap.isRecycled() ^ 1) != 0) {
                                        bitmap.compress(CompressFormat.PNG, 100, imageFile3);
                                    }
                                } catch (Exception e5) {
                                    e = e5;
                                    imageFile2 = imageFile3;
                                    try {
                                        Slog.e(TaskPersister.TAG, "saveImage: unable to save " + filePath, e);
                                        IoUtils.closeQuietly(imageFile2);
                                    } catch (Throwable th2) {
                                        th = th2;
                                    }
                                } catch (Throwable th3) {
                                    th = th3;
                                    imageFile2 = imageFile3;
                                    IoUtils.closeQuietly(imageFile2);
                                    throw th;
                                }
                            }
                            IoUtils.closeQuietly(imageFile3);
                        } catch (Exception e6) {
                            e = e6;
                            Slog.e(TaskPersister.TAG, "saveImage: unable to save " + filePath, e);
                            IoUtils.closeQuietly(imageFile2);
                        }
                    } else {
                        Slog.e(TaskPersister.TAG, "Error while creating images directory for file: " + filePath);
                    }
                } else if (item instanceof TaskWriteQueueItem) {
                    StringWriter stringWriter = null;
                    task = ((TaskWriteQueueItem) item).mTask;
                    synchronized (TaskPersister.this.mService) {
                        try {
                            ActivityManagerService.boostPriorityForLockedSection();
                            if (task.inRecents) {
                                try {
                                    stringWriter = TaskPersister.this.saveToXml(task);
                                } catch (IOException e7) {
                                } catch (XmlPullParserException e8) {
                                }
                            }
                        } finally {
                            ActivityManagerService.resetPriorityAfterLockedSection();
                        }
                    }
                    if (stringWriter != null) {
                        FileOutputStream file = null;
                        AtomicFile atomicFile = null;
                        try {
                            AtomicFile atomicFile2 = new AtomicFile(new File(TaskPersister.getUserTasksDir(task.userId), String.valueOf(task.taskId) + TaskPersister.TASK_FILENAME_SUFFIX));
                            try {
                                file = atomicFile2.startWrite();
                                file.write(stringWriter.toString().getBytes());
                                file.write(10);
                                atomicFile2.finishWrite(file);
                            } catch (IOException e9) {
                                e2 = e9;
                                atomicFile = atomicFile2;
                            }
                        } catch (IOException e10) {
                            e2 = e10;
                            if (file != null) {
                                atomicFile.failWrite(file);
                            }
                            Slog.e(TaskPersister.TAG, "Unable to open " + atomicFile + " for persisting. " + e2);
                        }
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

    TaskPersister(File systemDir, ActivityStackSupervisor stackSupervisor, ActivityManagerService service, RecentTasks recentTasks) {
        File legacyImagesDir = new File(systemDir, IMAGES_DIRNAME);
        if (legacyImagesDir.exists() && !(FileUtils.deleteContents(legacyImagesDir) && (legacyImagesDir.delete() ^ 1) == 0)) {
            Slog.i(TAG, "Failure deleting legacy images directory: " + legacyImagesDir);
        }
        File legacyTasksDir = new File(systemDir, TASKS_DIRNAME);
        if (legacyTasksDir.exists() && !(FileUtils.deleteContents(legacyTasksDir) && (legacyTasksDir.delete() ^ 1) == 0)) {
            Slog.i(TAG, "Failure deleting legacy tasks directory: " + legacyTasksDir);
        }
        this.mTaskIdsDir = new File(Environment.getDataDirectory(), "system_de");
        this.mStackSupervisor = stackSupervisor;
        this.mService = service;
        this.mRecentTasks = recentTasks;
        this.mLazyTaskWriterThread = new LazyTaskWriterThread("LazyTaskWriterThread");
    }

    TaskPersister(File workingDir) {
        this.mTaskIdsDir = workingDir;
        this.mStackSupervisor = null;
        this.mService = null;
        this.mRecentTasks = null;
        this.mLazyTaskWriterThread = new LazyTaskWriterThread("LazyTaskWriterThreadTest");
    }

    void startPersisting() {
        if (!this.mLazyTaskWriterThread.isAlive()) {
            this.mLazyTaskWriterThread.start();
        }
    }

    private void removeThumbnails(TaskRecord task) {
        String taskString = Integer.toString(task.taskId);
        for (int queueNdx = this.mWriteQueue.size() - 1; queueNdx >= 0; queueNdx--) {
            WriteQueueItem item = (WriteQueueItem) this.mWriteQueue.get(queueNdx);
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

    /* JADX WARNING: Removed duplicated region for block: B:12:0x0032 A:{Catch:{ FileNotFoundException -> 0x008b, Exception -> 0x008e, all -> 0x0088 }} */
    /* JADX WARNING: Removed duplicated region for block: B:16:0x004a A:{SYNTHETIC, Splitter: B:16:0x004a, EDGE_INSN: B:16:0x004a->B:17:? ?: BREAK  } */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    SparseBooleanArray loadPersistedTaskIdsForUser(int userId) {
        Throwable th;
        Exception e;
        Object reader;
        if (this.mTaskIdsInFile.get(userId) != null) {
            return ((SparseBooleanArray) this.mTaskIdsInFile.get(userId)).clone();
        }
        SparseBooleanArray persistedTaskIds = new SparseBooleanArray();
        synchronized (this.mIoLock) {
            BufferedReader reader2 = null;
            try {
                BufferedReader reader3 = new BufferedReader(new FileReader(getUserPersistedTaskIdsFile(userId)));
                while (true) {
                    try {
                        String line = reader3.readLine();
                        if (line == null) {
                            String[] split = line.split("\\s+");
                            int i = 0;
                            int length = split.length;
                            while (true) {
                                if (i < length) {
                                    persistedTaskIds.put(Integer.parseInt(split[i]), true);
                                    i++;
                                }
                            }
                            String line2 = reader3.readLine();
                            if (line2 == null) {
                                try {
                                    break;
                                } catch (Throwable th2) {
                                    th = th2;
                                    throw th;
                                }
                            }
                        }
                    } catch (FileNotFoundException e2) {
                        reader2 = reader3;
                        IoUtils.closeQuietly(reader2);
                        this.mTaskIdsInFile.put(userId, persistedTaskIds);
                        return persistedTaskIds.clone();
                    } catch (Exception e3) {
                        e = e3;
                        reader = reader3;
                        try {
                            Slog.e(TAG, "Error while reading taskIds file for user " + userId, e);
                            IoUtils.closeQuietly(reader);
                            this.mTaskIdsInFile.put(userId, persistedTaskIds);
                            return persistedTaskIds.clone();
                        } catch (Throwable th3) {
                            th = th3;
                        }
                    } catch (Throwable th4) {
                        th = th4;
                        reader = reader3;
                        IoUtils.closeQuietly(reader);
                        throw th;
                    }
                }
                IoUtils.closeQuietly(reader3);
                reader2 = reader3;
            } catch (FileNotFoundException e4) {
                IoUtils.closeQuietly(reader2);
                this.mTaskIdsInFile.put(userId, persistedTaskIds);
                return persistedTaskIds.clone();
            } catch (Exception e5) {
                e = e5;
                Slog.e(TAG, "Error while reading taskIds file for user " + userId, e);
                IoUtils.closeQuietly(reader);
                this.mTaskIdsInFile.put(userId, persistedTaskIds);
                return persistedTaskIds.clone();
            }
        }
    }

    void writePersistedTaskIdsForUser(SparseBooleanArray taskIds, int userId) {
        Exception e;
        Object obj;
        Throwable th;
        if (userId >= 0 && taskIds != null) {
            File persistedTaskIdsFile = getUserPersistedTaskIdsFile(userId);
            synchronized (this.mIoLock) {
                AutoCloseable obj2 = null;
                try {
                    BufferedWriter writer = new BufferedWriter(new FileWriter(persistedTaskIdsFile));
                    int i = 0;
                    while (i < taskIds.size()) {
                        try {
                            if (taskIds.valueAt(i)) {
                                writer.write(String.valueOf(taskIds.keyAt(i)));
                                writer.newLine();
                            }
                            i++;
                        } catch (Exception e2) {
                            e = e2;
                            obj2 = writer;
                            try {
                                Slog.e(TAG, "Error while writing taskIds file for user " + userId, e);
                                try {
                                    IoUtils.closeQuietly(obj2);
                                } catch (Throwable th2) {
                                    th = th2;
                                }
                            } catch (Throwable th3) {
                                th = th3;
                                IoUtils.closeQuietly(obj2);
                                throw th;
                            }
                        } catch (Throwable th4) {
                            th = th4;
                            obj2 = writer;
                            IoUtils.closeQuietly(obj2);
                            throw th;
                        }
                    }
                    try {
                        IoUtils.closeQuietly(writer);
                        BufferedWriter bufferedWriter = writer;
                    } catch (Throwable th5) {
                        th = th5;
                        throw th;
                    }
                } catch (Exception e3) {
                    e = e3;
                    Slog.e(TAG, "Error while writing taskIds file for user " + userId, e);
                    IoUtils.closeQuietly(obj2);
                }
            }
        }
    }

    void unloadUserDataFromMemory(int userId) {
        this.mTaskIdsInFile.delete(userId);
    }

    void wakeup(TaskRecord task, boolean flush) {
        synchronized (this) {
            if (task != null) {
                int queueNdx = this.mWriteQueue.size() - 1;
                while (queueNdx >= 0) {
                    WriteQueueItem item = (WriteQueueItem) this.mWriteQueue.get(queueNdx);
                    if ((item instanceof TaskWriteQueueItem) && ((TaskWriteQueueItem) item).mTask == task) {
                        if (!task.inRecents) {
                            removeThumbnails(task);
                        }
                        if (queueNdx < 0 && task.isPersistable) {
                            this.mWriteQueue.add(new TaskWriteQueueItem(task));
                        }
                    } else {
                        queueNdx--;
                    }
                }
                this.mWriteQueue.add(new TaskWriteQueueItem(task));
            } else {
                this.mWriteQueue.add(new WriteQueueItem());
            }
            if (flush || this.mWriteQueue.size() > 6) {
                this.mNextWriteTime = -1;
            } else if (this.mNextWriteTime == 0) {
                this.mNextWriteTime = SystemClock.uptimeMillis() + PRE_TASK_DELAY_MS;
            }
            notifyAll();
        }
        yieldIfQueueTooDeep();
    }

    void flush() {
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

    void saveImage(Bitmap image, String filePath) {
        synchronized (this) {
            int queueNdx = this.mWriteQueue.size() - 1;
            while (queueNdx >= 0) {
                WriteQueueItem item = (WriteQueueItem) this.mWriteQueue.get(queueNdx);
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

    Bitmap getTaskDescriptionIcon(String filePath) {
        Bitmap icon = getImageFromWriteQueue(filePath);
        if (icon != null) {
            return icon;
        }
        return restoreImage(filePath);
    }

    Bitmap getImageFromWriteQueue(String filePath) {
        synchronized (this) {
            for (int queueNdx = this.mWriteQueue.size() - 1; queueNdx >= 0; queueNdx--) {
                WriteQueueItem item = (WriteQueueItem) this.mWriteQueue.get(queueNdx);
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

    private StringWriter saveToXml(TaskRecord task) throws IOException, XmlPullParserException {
        XmlSerializer xmlSerializer = new FastXmlSerializer();
        StringWriter stringWriter = new StringWriter();
        xmlSerializer.setOutput(stringWriter);
        xmlSerializer.startDocument(null, Boolean.valueOf(true));
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
                String line = reader.readLine();
                if (line != null) {
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
            TaskRecord task = (TaskRecord) tasks.get(taskNdx);
            if (task.taskId == taskId) {
                return task;
            }
        }
        Slog.e(TAG, "Restore affiliation error looking for taskId=" + taskId);
        return null;
    }

    /* JADX WARNING: Removed duplicated region for block: B:71:0x004d A:{SYNTHETIC} */
    /* JADX WARNING: Removed duplicated region for block: B:42:0x018c  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    List<TaskRecord> restoreTasksForUserLocked(int userId, SparseBooleanArray preaddedTasks) {
        Exception e;
        Throwable th;
        ArrayList<TaskRecord> tasks = new ArrayList();
        ArraySet<Integer> recoveredTaskIds = new ArraySet();
        File userTasksDir = getUserTasksDir(userId);
        File[] recentFiles = userTasksDir.listFiles();
        if (recentFiles == null) {
            Slog.e(TAG, "restoreTasksForUserLocked: Unable to list files from " + userTasksDir);
            return tasks;
        }
        int taskNdx;
        TaskRecord task;
        for (File taskFile : recentFiles) {
            if (taskFile.getName().endsWith(TASK_FILENAME_SUFFIX)) {
                try {
                    int taskId = Integer.parseInt(taskFile.getName().substring(0, taskFile.getName().length() - TASK_FILENAME_SUFFIX.length()));
                    if (preaddedTasks.get(taskId, false)) {
                        Slog.w(TAG, "Task #" + taskId + " has already been created so we don't restore again");
                    } else {
                        AutoCloseable autoCloseable = null;
                        try {
                            BufferedReader reader = new BufferedReader(new FileReader(taskFile));
                            try {
                                XmlPullParser in = Xml.newPullParser();
                                in.setInput(reader);
                                while (true) {
                                    int event = in.next();
                                    if (event == 1 || event == 3) {
                                        IoUtils.closeQuietly(reader);
                                    } else {
                                        String name = in.getName();
                                        if (event == 2) {
                                            if (TAG_TASK.equals(name)) {
                                                task = TaskRecord.restoreFromXml(in, this.mStackSupervisor);
                                                if (task != null) {
                                                    taskId = task.taskId;
                                                    if (this.mStackSupervisor.anyTaskForIdLocked(taskId, 1, -1) != null) {
                                                        Slog.wtf(TAG, "Existing task with taskId " + taskId + "found");
                                                    } else {
                                                        if (userId != task.userId) {
                                                            Slog.wtf(TAG, "Task with userId " + task.userId + " found in " + userTasksDir.getAbsolutePath());
                                                        } else {
                                                            this.mStackSupervisor.setNextTaskIdForUserLocked(taskId, userId);
                                                            task.isPersistable = true;
                                                            tasks.add(task);
                                                            recoveredTaskIds.add(Integer.valueOf(taskId));
                                                        }
                                                    }
                                                } else {
                                                    Slog.e(TAG, "restoreTasksForUserLocked: Unable to restore taskFile=" + taskFile + ": " + fileToString(taskFile));
                                                }
                                            } else {
                                                Slog.wtf(TAG, "restoreTasksForUserLocked: Unknown xml event=" + event + " name=" + name);
                                            }
                                        }
                                        XmlUtils.skipCurrentTag(in);
                                    }
                                }
                                IoUtils.closeQuietly(reader);
                                if (null != null) {
                                    taskFile.delete();
                                }
                            } catch (Exception e2) {
                                e = e2;
                                autoCloseable = reader;
                                try {
                                    Slog.wtf(TAG, "Unable to parse " + taskFile + ". Error ", e);
                                    Slog.e(TAG, "Failing file: " + fileToString(taskFile));
                                    IoUtils.closeQuietly(autoCloseable);
                                    if (!true) {
                                        taskFile.delete();
                                    }
                                } catch (Throwable th2) {
                                    th = th2;
                                }
                            } catch (Throwable th3) {
                                th = th3;
                                autoCloseable = reader;
                                IoUtils.closeQuietly(autoCloseable);
                                if (null != null) {
                                    taskFile.delete();
                                }
                                throw th;
                            }
                        } catch (Exception e3) {
                            e = e3;
                            Slog.wtf(TAG, "Unable to parse " + taskFile + ". Error ", e);
                            Slog.e(TAG, "Failing file: " + fileToString(taskFile));
                            IoUtils.closeQuietly(autoCloseable);
                            if (!true) {
                            }
                        }
                    }
                } catch (NumberFormatException e4) {
                    Slog.w(TAG, "Unexpected task file name", e4);
                }
            }
        }
        removeObsoleteFiles(recoveredTaskIds, userTasksDir.listFiles());
        for (taskNdx = tasks.size() - 1; taskNdx >= 0; taskNdx--) {
            task = (TaskRecord) tasks.get(taskNdx);
            task.setPrevAffiliate(taskIdToTask(task.mPrevAffiliateTaskId, tasks));
            task.setNextAffiliate(taskIdToTask(task.mNextAffiliateTaskId, tasks));
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

    private void writeTaskIdsFiles() {
        SparseArray<SparseBooleanArray> changedTaskIdsPerUser = new SparseArray();
        synchronized (this.mService) {
            try {
                ActivityManagerService.boostPriorityForLockedSection();
                for (int userId : this.mRecentTasks.usersWithRecentsLoadedLocked()) {
                    SparseBooleanArray taskIdsToSave = (SparseBooleanArray) this.mRecentTasks.mPersistedTaskIds.get(userId);
                    SparseBooleanArray persistedIdsInFile = (SparseBooleanArray) this.mTaskIdsInFile.get(userId);
                    if (persistedIdsInFile == null || !persistedIdsInFile.equals(taskIdsToSave)) {
                        SparseBooleanArray taskIdsToSaveCopy = taskIdsToSave.clone();
                        this.mTaskIdsInFile.put(userId, taskIdsToSaveCopy);
                        changedTaskIdsPerUser.put(userId, taskIdsToSaveCopy);
                    }
                }
            } finally {
                ActivityManagerService.resetPriorityAfterLockedSection();
            }
        }
        for (int i = 0; i < changedTaskIdsPerUser.size(); i++) {
            writePersistedTaskIdsForUser((SparseBooleanArray) changedTaskIdsPerUser.valueAt(i), changedTaskIdsPerUser.keyAt(i));
        }
    }

    private void removeObsoleteFiles(ArraySet<Integer> persistentTaskIds) {
        synchronized (this.mService) {
            try {
                ActivityManagerService.boostPriorityForLockedSection();
                int[] candidateUserIds = this.mRecentTasks.usersWithRecentsLoadedLocked();
            } finally {
                ActivityManagerService.resetPriorityAfterLockedSection();
            }
        }
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
        if (!(userTaskIdsDir.exists() || (userTaskIdsDir.mkdirs() ^ 1) == 0)) {
            Slog.e(TAG, "Error while creating user directory: " + userTaskIdsDir);
        }
        return new File(userTaskIdsDir, PERSISTED_TASK_IDS_FILENAME);
    }

    static File getUserTasksDir(int userId) {
        File userTasksDir = new File(Environment.getDataSystemCeDirectory(userId), TASKS_DIRNAME);
        if (!(userTasksDir.exists() || userTasksDir.mkdir())) {
            Slog.e(TAG, "Failure creating tasks directory for user " + userId + ": " + userTasksDir);
        }
        return userTasksDir;
    }

    static File getUserImagesDir(int userId) {
        return new File(Environment.getDataSystemCeDirectory(userId), IMAGES_DIRNAME);
    }

    private static boolean createParentDirectory(String filePath) {
        File parentDir = new File(filePath).getParentFile();
        return !parentDir.exists() ? parentDir.mkdirs() : true;
    }
}
