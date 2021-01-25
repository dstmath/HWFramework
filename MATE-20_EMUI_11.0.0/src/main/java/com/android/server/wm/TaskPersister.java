package com.android.server.wm;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.FileUtils;
import android.util.ArraySet;
import android.util.AtomicFile;
import android.util.Slog;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.util.Xml;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.util.FastXmlSerializer;
import com.android.internal.util.XmlUtils;
import com.android.server.wm.PersisterQueue;
import com.android.server.wm.TaskPersister;
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
import java.util.function.Predicate;
import libcore.io.IoUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public class TaskPersister implements PersisterQueue.Listener {
    static final boolean DEBUG = false;
    private static final String IMAGES_DIRNAME = "recent_images";
    static final String IMAGE_EXTENSION = ".png";
    private static final String PERSISTED_TASK_IDS_FILENAME = "persisted_taskIds.txt";
    static final String TAG = "TaskPersister";
    private static final String TAG_TASK = "task";
    private static final String TASKS_DIRNAME = "recent_tasks";
    private static final String TASK_FILENAME_SUFFIX = "_task.xml";
    private final Object mIoLock = new Object();
    private final PersisterQueue mPersisterQueue;
    private final RecentTasks mRecentTasks;
    private final ActivityTaskManagerService mService;
    private final ActivityStackSupervisor mStackSupervisor;
    private final File mTaskIdsDir;
    private final SparseArray<SparseBooleanArray> mTaskIdsInFile = new SparseArray<>();
    private final ArraySet<Integer> mTmpTaskIds = new ArraySet<>();

    TaskPersister(File systemDir, ActivityStackSupervisor stackSupervisor, ActivityTaskManagerService service, RecentTasks recentTasks, PersisterQueue persisterQueue) {
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
        this.mPersisterQueue = persisterQueue;
        this.mPersisterQueue.addListener(this);
    }

    @VisibleForTesting
    TaskPersister(File workingDir) {
        this.mTaskIdsDir = workingDir;
        this.mStackSupervisor = null;
        this.mService = null;
        this.mRecentTasks = null;
        this.mPersisterQueue = new PersisterQueue();
        this.mPersisterQueue.addListener(this);
    }

    private void removeThumbnails(TaskRecord task) {
        this.mPersisterQueue.removeItems(new Predicate() {
            /* class com.android.server.wm.$$Lambda$TaskPersister$8TcnoL7JFvpj8NzBRg91ns5JOBw */

            @Override // java.util.function.Predicate
            public final boolean test(Object obj) {
                return new File(((TaskPersister.ImageWriteQueueItem) obj).mFilePath).getName().startsWith(Integer.toString(TaskRecord.this.taskId));
            }
        }, ImageWriteQueueItem.class);
    }

    /* access modifiers changed from: package-private */
    public SparseBooleanArray loadPersistedTaskIdsForUser(int userId) {
        if (this.mTaskIdsInFile.get(userId) != null) {
            return this.mTaskIdsInFile.get(userId).clone();
        }
        SparseBooleanArray persistedTaskIds = new SparseBooleanArray();
        synchronized (this.mIoLock) {
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new FileReader(getUserPersistedTaskIdsFile(userId)));
                while (true) {
                    String line = reader.readLine();
                    if (line == null) {
                        break;
                    }
                    for (String taskIdString : line.split("\\s+")) {
                        persistedTaskIds.put(Integer.parseInt(taskIdString), true);
                    }
                }
            } catch (FileNotFoundException e) {
            } catch (Exception e2) {
                Slog.e(TAG, "Error while reading taskIds file for user " + userId, e2);
            } finally {
                IoUtils.closeQuietly(reader);
            }
        }
        this.mTaskIdsInFile.put(userId, persistedTaskIds);
        return persistedTaskIds.clone();
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void writePersistedTaskIdsForUser(SparseBooleanArray taskIds, int userId) {
        if (userId >= 0) {
            File persistedTaskIdsFile = getUserPersistedTaskIdsFile(userId);
            synchronized (this.mIoLock) {
                BufferedWriter writer = null;
                try {
                    writer = new BufferedWriter(new FileWriter(persistedTaskIdsFile));
                    for (int i = 0; i < taskIds.size(); i++) {
                        if (taskIds.valueAt(i)) {
                            writer.write(String.valueOf(taskIds.keyAt(i)));
                            writer.newLine();
                        }
                    }
                } catch (Exception e) {
                    Slog.e(TAG, "Error while writing taskIds file for user " + userId, e);
                } finally {
                    IoUtils.closeQuietly(writer);
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
        synchronized (this.mPersisterQueue) {
            if (task != null) {
                TaskWriteQueueItem item = (TaskWriteQueueItem) this.mPersisterQueue.findLastItem(new Predicate() {
                    /* class com.android.server.wm.$$Lambda$TaskPersister$xdLXwftXa6l84QTg1zpxMnmtQ0g */

                    @Override // java.util.function.Predicate
                    public final boolean test(Object obj) {
                        return TaskPersister.lambda$wakeup$1(TaskRecord.this, (TaskPersister.TaskWriteQueueItem) obj);
                    }
                }, TaskWriteQueueItem.class);
                if (item != null && !task.inRecents) {
                    removeThumbnails(task);
                }
                if (item == null && task.isPersistable) {
                    this.mPersisterQueue.addItem(new TaskWriteQueueItem(task, this.mService), flush);
                }
            } else {
                this.mPersisterQueue.addItem(PersisterQueue.EMPTY_ITEM, flush);
            }
        }
        this.mPersisterQueue.yieldIfQueueTooDeep();
    }

    static /* synthetic */ boolean lambda$wakeup$1(TaskRecord task, TaskWriteQueueItem queueItem) {
        return task == queueItem.mTask;
    }

    /* access modifiers changed from: package-private */
    public void flush() {
        this.mPersisterQueue.flush();
    }

    /* access modifiers changed from: package-private */
    public void saveImage(Bitmap image, String filePath) {
        this.mPersisterQueue.updateLastOrAddItem(new ImageWriteQueueItem(filePath, image), false);
    }

    /* access modifiers changed from: package-private */
    public Bitmap getTaskDescriptionIcon(String filePath) {
        Bitmap icon = getImageFromWriteQueue(filePath);
        if (icon != null) {
            return icon;
        }
        return restoreImage(filePath);
    }

    private Bitmap getImageFromWriteQueue(String filePath) {
        ImageWriteQueueItem item = (ImageWriteQueueItem) this.mPersisterQueue.findLastItem(new Predicate(filePath) {
            /* class com.android.server.wm.$$Lambda$TaskPersister$mW0HULrR8EtZ9LapL9kLTnHSzk */
            private final /* synthetic */ String f$0;

            {
                this.f$0 = r1;
            }

            @Override // java.util.function.Predicate
            public final boolean test(Object obj) {
                return ((TaskPersister.ImageWriteQueueItem) obj).mFilePath.equals(this.f$0);
            }
        }, ImageWriteQueueItem.class);
        if (item != null) {
            return item.mImage;
        }
        return null;
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
            TaskRecord task = tasks.get(taskNdx);
            if (task.taskId == taskId) {
                return task;
            }
        }
        Slog.e(TAG, "Restore affiliation error looking for taskId=" + taskId);
        return null;
    }

    /* JADX INFO: Multiple debug info for r6v17 int: [D('taskId' int), D('recentFiles' java.io.File[])] */
    /* access modifiers changed from: package-private */
    /* JADX WARNING: Removed duplicated region for block: B:66:0x01df  */
    /* JADX WARNING: Removed duplicated region for block: B:70:0x01e9  */
    /* JADX WARNING: Removed duplicated region for block: B:87:0x01f8 A[SYNTHETIC] */
    public List<TaskRecord> restoreTasksForUserLocked(int userId, SparseBooleanArray preaddedTasks) {
        int taskNdx;
        File[] recentFiles;
        NumberFormatException e;
        int taskId;
        Throwable th;
        Exception e2;
        XmlPullParser in;
        ArrayList<TaskRecord> tasks = new ArrayList<>();
        ArraySet<Integer> recoveredTaskIds = new ArraySet<>();
        File userTasksDir = getUserTasksDir(userId);
        File[] recentFiles2 = userTasksDir.listFiles();
        if (recentFiles2 == null) {
            Slog.e(TAG, "restoreTasksForUserLocked: Unable to list files from " + userTasksDir);
            return tasks;
        }
        int taskNdx2 = 0;
        while (true) {
            int i = 1;
            if (taskNdx2 >= recentFiles2.length) {
                break;
            }
            File taskFile = recentFiles2[taskNdx2];
            if (!taskFile.getName().endsWith(TASK_FILENAME_SUFFIX)) {
                recentFiles = recentFiles2;
                taskNdx = taskNdx2;
            } else {
                try {
                    taskId = Integer.parseInt(taskFile.getName().substring(0, taskFile.getName().length() - TASK_FILENAME_SUFFIX.length()));
                } catch (NumberFormatException e3) {
                    e = e3;
                    recentFiles = recentFiles2;
                    taskNdx = taskNdx2;
                    Slog.w(TAG, "Unexpected task file name", e);
                    taskNdx2 = taskNdx + 1;
                    recentFiles2 = recentFiles;
                }
                if (preaddedTasks.get(taskId, false)) {
                    try {
                        Slog.w(TAG, "Task #" + taskId + " has already been created so we don't restore again");
                        recentFiles = recentFiles2;
                        taskNdx = taskNdx2;
                    } catch (NumberFormatException e4) {
                        e = e4;
                        recentFiles = recentFiles2;
                        taskNdx = taskNdx2;
                        Slog.w(TAG, "Unexpected task file name", e);
                        taskNdx2 = taskNdx + 1;
                        recentFiles2 = recentFiles;
                    }
                } else {
                    BufferedReader reader = null;
                    try {
                        reader = new BufferedReader(new FileReader(taskFile));
                        in = Xml.newPullParser();
                        in.setInput(reader);
                        recentFiles = recentFiles2;
                        taskNdx = taskNdx2;
                        IoUtils.closeQuietly(reader);
                        if (0 != 0) {
                            taskFile.delete();
                        }
                    } catch (Exception e5) {
                        e2 = e5;
                        recentFiles = recentFiles2;
                        taskNdx = taskNdx2;
                        try {
                            Slog.wtf(TAG, "Unable to parse " + taskFile + ". Error ", e2);
                            StringBuilder sb = new StringBuilder();
                            sb.append("Failing file: ");
                            sb.append(fileToString(taskFile));
                            Slog.e(TAG, sb.toString());
                            IoUtils.closeQuietly(reader);
                            if (1 != 0) {
                            }
                            taskNdx2 = taskNdx + 1;
                            recentFiles2 = recentFiles;
                        } catch (Throwable th2) {
                            th = th2;
                            IoUtils.closeQuietly(reader);
                            if (0 != 0) {
                                taskFile.delete();
                            }
                            throw th;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        IoUtils.closeQuietly(reader);
                        if (0 != 0) {
                        }
                        throw th;
                    }
                    while (true) {
                        int event = in.next();
                        if (event == i || event == 3) {
                            break;
                        }
                        String name = in.getName();
                        if (event != 2) {
                            recentFiles = recentFiles2;
                            taskNdx = taskNdx2;
                        } else if (TAG_TASK.equals(name)) {
                            TaskRecord task = TaskRecord.restoreFromXml(in, this.mStackSupervisor);
                            if (task != null) {
                                recentFiles = recentFiles2;
                                try {
                                    int taskId2 = task.taskId;
                                    taskNdx = taskNdx2;
                                    try {
                                        if (this.mService.mRootActivityContainer.anyTaskForId(taskId2, 1) != null) {
                                            Slog.wtf(TAG, "Existing task with taskId " + taskId2 + "found");
                                        } else if (userId != task.userId) {
                                            Slog.wtf(TAG, "Task with userId " + task.userId + " found in " + userTasksDir.getAbsolutePath());
                                        } else {
                                            this.mStackSupervisor.setNextTaskIdForUserLocked(taskId2, userId);
                                            task.isPersistable = true;
                                            tasks.add(task);
                                            recoveredTaskIds.add(Integer.valueOf(taskId2));
                                        }
                                    } catch (Exception e6) {
                                        e2 = e6;
                                        Slog.wtf(TAG, "Unable to parse " + taskFile + ". Error ", e2);
                                        StringBuilder sb2 = new StringBuilder();
                                        sb2.append("Failing file: ");
                                        sb2.append(fileToString(taskFile));
                                        Slog.e(TAG, sb2.toString());
                                        IoUtils.closeQuietly(reader);
                                        if (1 != 0) {
                                            taskFile.delete();
                                        }
                                        taskNdx2 = taskNdx + 1;
                                        recentFiles2 = recentFiles;
                                    }
                                } catch (Exception e7) {
                                    e2 = e7;
                                    taskNdx = taskNdx2;
                                    Slog.wtf(TAG, "Unable to parse " + taskFile + ". Error ", e2);
                                    StringBuilder sb22 = new StringBuilder();
                                    sb22.append("Failing file: ");
                                    sb22.append(fileToString(taskFile));
                                    Slog.e(TAG, sb22.toString());
                                    IoUtils.closeQuietly(reader);
                                    if (1 != 0) {
                                    }
                                    taskNdx2 = taskNdx + 1;
                                    recentFiles2 = recentFiles;
                                } catch (Throwable th4) {
                                    th = th4;
                                    IoUtils.closeQuietly(reader);
                                    if (0 != 0) {
                                    }
                                    throw th;
                                }
                            } else {
                                recentFiles = recentFiles2;
                                taskNdx = taskNdx2;
                                Slog.e(TAG, "restoreTasksForUserLocked: Unable to restore taskFile=" + taskFile + ": " + fileToString(taskFile));
                            }
                        } else {
                            recentFiles = recentFiles2;
                            taskNdx = taskNdx2;
                            Slog.wtf(TAG, "restoreTasksForUserLocked: Unknown xml event=" + event + " name=" + name);
                        }
                        XmlUtils.skipCurrentTag(in);
                        recentFiles2 = recentFiles;
                        taskNdx2 = taskNdx;
                        i = 1;
                    }
                }
            }
            taskNdx2 = taskNdx + 1;
            recentFiles2 = recentFiles;
        }
        removeObsoleteFiles(recoveredTaskIds, userTasksDir.listFiles());
        for (int taskNdx3 = tasks.size() - 1; taskNdx3 >= 0; taskNdx3--) {
            TaskRecord task2 = tasks.get(taskNdx3);
            task2.setPrevAffiliate(taskIdToTask(task2.mPrevAffiliateTaskId, tasks));
            task2.setNextAffiliate(taskIdToTask(task2.mNextAffiliateTaskId, tasks));
        }
        Collections.sort(tasks, new Comparator<TaskRecord>() {
            /* class com.android.server.wm.TaskPersister.AnonymousClass1 */

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

    /* JADX INFO: finally extract failed */
    @Override // com.android.server.wm.PersisterQueue.Listener
    public void onPreProcessItem(boolean queueEmpty) {
        if (queueEmpty) {
            this.mTmpTaskIds.clear();
            synchronized (this.mService.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    this.mRecentTasks.getPersistableTaskIds(this.mTmpTaskIds);
                    this.mService.mWindowManager.removeObsoleteTaskFiles(this.mTmpTaskIds, this.mRecentTasks.usersWithRecentsLoadedLocked());
                } catch (Throwable th) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
            WindowManagerService.resetPriorityAfterLockedSection();
            removeObsoleteFiles(this.mTmpTaskIds);
        }
        writeTaskIdsFiles();
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

    /* JADX INFO: finally extract failed */
    private void writeTaskIdsFiles() {
        SparseArray<SparseBooleanArray> changedTaskIdsPerUser = new SparseArray<>();
        synchronized (this.mService.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                int[] usersWithRecentsLoadedLocked = this.mRecentTasks.usersWithRecentsLoadedLocked();
                for (int userId : usersWithRecentsLoadedLocked) {
                    SparseBooleanArray taskIdsToSave = this.mRecentTasks.getTaskIdsForUser(userId);
                    SparseBooleanArray persistedIdsInFile = this.mTaskIdsInFile.get(userId);
                    if (persistedIdsInFile == null || !persistedIdsInFile.equals(taskIdsToSave)) {
                        SparseBooleanArray taskIdsToSaveCopy = taskIdsToSave.clone();
                        this.mTaskIdsInFile.put(userId, taskIdsToSaveCopy);
                        changedTaskIdsPerUser.put(userId, taskIdsToSaveCopy);
                    }
                }
            } catch (Throwable th) {
                WindowManagerService.resetPriorityAfterLockedSection();
                throw th;
            }
        }
        WindowManagerService.resetPriorityAfterLockedSection();
        for (int i = 0; i < changedTaskIdsPerUser.size(); i++) {
            writePersistedTaskIdsForUser(changedTaskIdsPerUser.valueAt(i), changedTaskIdsPerUser.keyAt(i));
        }
    }

    /* JADX INFO: finally extract failed */
    private void removeObsoleteFiles(ArraySet<Integer> persistentTaskIds) {
        int[] candidateUserIds;
        synchronized (this.mService.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                candidateUserIds = this.mRecentTasks.usersWithRecentsLoadedLocked();
            } catch (Throwable th) {
                WindowManagerService.resetPriorityAfterLockedSection();
                throw th;
            }
        }
        WindowManagerService.resetPriorityAfterLockedSection();
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

    /* access modifiers changed from: private */
    public static File getUserTasksDir(int userId) {
        return new File(Environment.getDataSystemCeDirectory(userId), TASKS_DIRNAME);
    }

    static File getUserImagesDir(int userId) {
        return new File(Environment.getDataSystemCeDirectory(userId), IMAGES_DIRNAME);
    }

    /* access modifiers changed from: private */
    public static boolean createParentDirectory(String filePath) {
        File parentDir = new File(filePath).getParentFile();
        return parentDir.exists() || parentDir.mkdirs();
    }

    /* access modifiers changed from: private */
    public static class TaskWriteQueueItem implements PersisterQueue.WriteQueueItem {
        private final ActivityTaskManagerService mService;
        private final TaskRecord mTask;

        TaskWriteQueueItem(TaskRecord task, ActivityTaskManagerService service) {
            this.mTask = task;
            this.mService = service;
        }

        private StringWriter saveToXml(TaskRecord task) throws IOException, XmlPullParserException {
            XmlSerializer xmlSerializer = new FastXmlSerializer();
            StringWriter stringWriter = new StringWriter();
            xmlSerializer.setOutput(stringWriter);
            xmlSerializer.startDocument(null, true);
            xmlSerializer.startTag(null, TaskPersister.TAG_TASK);
            task.saveToXml(xmlSerializer);
            xmlSerializer.endTag(null, TaskPersister.TAG_TASK);
            xmlSerializer.endDocument();
            xmlSerializer.flush();
            return stringWriter;
        }

        /* JADX INFO: finally extract failed */
        @Override // com.android.server.wm.PersisterQueue.WriteQueueItem
        public void process() {
            StringWriter stringWriter = null;
            TaskRecord task = this.mTask;
            synchronized (this.mService.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    if (task.inRecents && !task.inHwFreeFormWindowingMode()) {
                        try {
                            stringWriter = saveToXml(task);
                        } catch (IOException | XmlPullParserException e) {
                        }
                    }
                } catch (Throwable th) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
            WindowManagerService.resetPriorityAfterLockedSection();
            if (task.inHwFreeFormWindowingMode()) {
                removeObsoleteTaskFiles(task.taskId, TaskPersister.getUserTasksDir(task.userId).listFiles());
            }
            if (stringWriter != null) {
                AtomicFile atomicFile = null;
                try {
                    File userTasksDir = TaskPersister.getUserTasksDir(task.userId);
                    if (userTasksDir.isDirectory() || userTasksDir.mkdirs()) {
                        AtomicFile atomicFile2 = new AtomicFile(new File(userTasksDir, String.valueOf(task.taskId) + TaskPersister.TASK_FILENAME_SUFFIX));
                        FileOutputStream file = atomicFile2.startWrite();
                        file.write(stringWriter.toString().getBytes());
                        file.write(10);
                        atomicFile2.finishWrite(file);
                        return;
                    }
                    Slog.e(TaskPersister.TAG, "Failure creating tasks directory for user " + task.userId + ": " + userTasksDir + " Dropping persistence for task " + task);
                } catch (IOException e2) {
                    if (0 != 0) {
                        atomicFile.failWrite(null);
                    }
                    Slog.e(TaskPersister.TAG, "Unable to open " + ((Object) null) + " for persisting. " + e2);
                }
            }
        }

        public String toString() {
            return "TaskWriteQueueItem{task=" + this.mTask + "}";
        }

        private void removeObsoleteTaskFiles(int inTaskId, File[] files) {
            if (files == null) {
                Slog.e(TaskPersister.TAG, "File error accessing recents directory (directory doesn't exist?).");
                return;
            }
            for (File file : files) {
                try {
                    String filename = file.getName();
                    int taskIdEnd = filename.indexOf(95);
                    if (taskIdEnd > 0 && Integer.parseInt(filename.substring(0, taskIdEnd)) == inTaskId) {
                        Slog.d(TaskPersister.TAG, "removeObsoleteTaskFiles: deleting task id=" + inTaskId);
                        file.delete();
                    }
                } catch (NumberFormatException e) {
                    Slog.w(TaskPersister.TAG, "removeObsoleteTaskFiles: Can't parse task id=" + inTaskId);
                    return;
                } catch (Exception e2) {
                    Slog.wtf(TaskPersister.TAG, "removeObsoleteTaskFiles: Can't parse task id=" + inTaskId);
                    return;
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public static class ImageWriteQueueItem implements PersisterQueue.WriteQueueItem<ImageWriteQueueItem> {
        final String mFilePath;
        Bitmap mImage;

        ImageWriteQueueItem(String filePath, Bitmap image) {
            this.mFilePath = filePath;
            this.mImage = image;
        }

        @Override // com.android.server.wm.PersisterQueue.WriteQueueItem
        public void process() {
            String filePath = this.mFilePath;
            if (!TaskPersister.createParentDirectory(filePath)) {
                Slog.e(TaskPersister.TAG, "Error while creating images directory for file: " + filePath);
                return;
            }
            Bitmap bitmap = this.mImage;
            FileOutputStream imageFile = null;
            try {
                imageFile = new FileOutputStream(new File(filePath));
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, imageFile);
            } catch (Exception e) {
                Slog.e(TaskPersister.TAG, "saveImage: unable to save " + filePath, e);
            } catch (Throwable th) {
                IoUtils.closeQuietly(imageFile);
                throw th;
            }
            IoUtils.closeQuietly(imageFile);
        }

        public boolean matches(ImageWriteQueueItem item) {
            return this.mFilePath.equals(item.mFilePath);
        }

        public void updateFrom(ImageWriteQueueItem item) {
            this.mImage = item.mImage;
        }

        public String toString() {
            return "ImageWriteQueueItem{path=" + this.mFilePath + ", image=(" + this.mImage.getWidth() + "x" + this.mImage.getHeight() + ")}";
        }
    }
}
