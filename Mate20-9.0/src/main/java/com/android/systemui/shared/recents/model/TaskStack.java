package com.android.systemui.shared.recents.model;

import android.content.ComponentName;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.Log;
import com.android.systemui.shared.recents.model.Task;
import com.android.systemui.shared.recents.utilities.AnimationProps;
import com.android.systemui.shared.system.PackageManagerWrapper;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class TaskStack {
    private static final String TAG = "TaskStack";
    private TaskStackCallbacks mCb;
    private final ArrayList<Task> mRawTaskList = new ArrayList<>();
    private final FilteredTaskList mStackTaskList = new FilteredTaskList();

    public interface TaskStackCallbacks {
        void onStackTaskAdded(TaskStack taskStack, Task task);

        void onStackTaskRemoved(TaskStack taskStack, Task task, Task task2, AnimationProps animationProps, boolean z, boolean z2);

        void onStackTasksRemoved(TaskStack taskStack);

        void onStackTasksUpdated(TaskStack taskStack);
    }

    public TaskStack() {
        this.mStackTaskList.setFilter($$Lambda$TaskStack$gkuBLLtJ6FV7PDAxT_KECDzTOI.INSTANCE);
    }

    public void setCallbacks(TaskStackCallbacks cb) {
        this.mCb = cb;
    }

    public void removeTask(Task t, AnimationProps animation, boolean fromDockGesture) {
        removeTask(t, animation, fromDockGesture, true);
    }

    public void removeTask(Task t, AnimationProps animation, boolean fromDockGesture, boolean dismissRecentsIfAllRemoved) {
        if (this.mStackTaskList.contains(t)) {
            this.mStackTaskList.remove(t);
            Task newFrontMostTask = getFrontMostTask();
            if (this.mCb != null) {
                this.mCb.onStackTaskRemoved(this, t, newFrontMostTask, animation, fromDockGesture, dismissRecentsIfAllRemoved);
            }
        }
        this.mRawTaskList.remove(t);
    }

    public void removeAllTasks(boolean notifyStackChanges) {
        ArrayList<Task> tasks = this.mStackTaskList.getTasks();
        for (int i = tasks.size() - 1; i >= 0; i--) {
            Task t = tasks.get(i);
            this.mStackTaskList.remove(t);
            this.mRawTaskList.remove(t);
        }
        if (this.mCb != null && notifyStackChanges) {
            this.mCb.onStackTasksRemoved(this);
        }
    }

    public void setTasks(TaskStack stack, boolean notifyStackChanges) {
        setTasks((List<Task>) stack.mRawTaskList, notifyStackChanges);
    }

    public void setTasks(List<Task> tasks, boolean notifyStackChanges) {
        ArrayMap<Task.TaskKey, Task> currentTasksMap = createTaskKeyMapFromList(this.mRawTaskList);
        ArrayMap<Task.TaskKey, Task> newTasksMap = createTaskKeyMapFromList(tasks);
        ArrayList arrayList = new ArrayList();
        ArrayList arrayList2 = new ArrayList();
        ArrayList arrayList3 = new ArrayList();
        boolean notifyStackChanges2 = this.mCb == null ? false : notifyStackChanges;
        for (int i = this.mRawTaskList.size() - 1; i >= 0; i--) {
            Task task = this.mRawTaskList.get(i);
            if (task == null) {
                Log.e(TAG, "setTasks,task is null here");
            } else if (!newTasksMap.containsKey(task.key) && notifyStackChanges2) {
                arrayList2.add(task);
            }
        }
        int taskCount = tasks.size();
        int i2 = 0;
        for (int i3 = 0; i3 < taskCount; i3++) {
            Task newTask = tasks.get(i3);
            Task currentTask = currentTasksMap.get(newTask.key);
            if (currentTask == null && notifyStackChanges2) {
                arrayList.add(newTask);
            } else if (currentTask != null) {
                currentTask.copyFrom(newTask);
                newTask = currentTask;
            }
            arrayList3.add(newTask);
        }
        List<Task> list = tasks;
        for (int i4 = arrayList3.size() - 1; i4 >= 0; i4--) {
            ((Task) arrayList3.get(i4)).temporarySortIndexInStack = i4;
        }
        this.mStackTaskList.set(arrayList3);
        this.mRawTaskList.clear();
        this.mRawTaskList.addAll(arrayList3);
        int removedTaskCount = arrayList2.size();
        Task newFrontMostTask = getFrontMostTask();
        int i5 = 0;
        while (true) {
            int i6 = i5;
            if (i6 >= removedTaskCount) {
                break;
            }
            this.mCb.onStackTaskRemoved(this, (Task) arrayList2.get(i6), newFrontMostTask, AnimationProps.IMMEDIATE, false, true);
            i5 = i6 + 1;
            List<Task> list2 = tasks;
            removedTaskCount = removedTaskCount;
        }
        int addedTaskCount = arrayList.size();
        while (true) {
            int i7 = i2;
            if (i7 >= addedTaskCount) {
                break;
            }
            this.mCb.onStackTaskAdded(this, (Task) arrayList.get(i7));
            i2 = i7 + 1;
        }
        if (notifyStackChanges2) {
            this.mCb.onStackTasksUpdated(this);
        }
    }

    public Task getFrontMostTask() {
        ArrayList<Task> stackTasks = this.mStackTaskList.getTasks();
        if (stackTasks.isEmpty()) {
            return null;
        }
        return stackTasks.get(stackTasks.size() - 1);
    }

    public ArrayList<Task.TaskKey> getTaskKeys() {
        ArrayList<Task.TaskKey> taskKeys = new ArrayList<>();
        ArrayList<Task> tasks = computeAllTasksList();
        int taskCount = tasks.size();
        for (int i = 0; i < taskCount; i++) {
            taskKeys.add(tasks.get(i).key);
        }
        return taskKeys;
    }

    public ArrayList<Task> getTasks() {
        return this.mStackTaskList.getTasks();
    }

    public ArrayList<Task> computeAllTasksList() {
        ArrayList<Task> tasks = new ArrayList<>();
        tasks.addAll(this.mStackTaskList.getTasks());
        return tasks;
    }

    public int getTaskCount() {
        return this.mStackTaskList.size();
    }

    public Task getLaunchTarget() {
        ArrayList<Task> tasks = this.mStackTaskList.getTasks();
        int taskCount = tasks.size();
        for (int i = 0; i < taskCount; i++) {
            Task task = tasks.get(i);
            if (task != null && task.isLaunchTarget) {
                return task;
            }
        }
        return null;
    }

    public boolean isNextLaunchTargetPip(long lastPipTime) {
        Task launchTarget = getLaunchTarget();
        Task nextLaunchTarget = getNextLaunchTargetRaw();
        boolean z = false;
        if (nextLaunchTarget == null || lastPipTime <= 0) {
            return launchTarget != null && lastPipTime > 0 && getTaskCount() == 1;
        }
        if (lastPipTime > nextLaunchTarget.key.lastActiveTime) {
            z = true;
        }
        return z;
    }

    public Task getNextLaunchTarget() {
        Task nextLaunchTarget = getNextLaunchTargetRaw();
        if (nextLaunchTarget != null) {
            return nextLaunchTarget;
        }
        return getTasks().get(getTaskCount() - 1);
    }

    private Task getNextLaunchTargetRaw() {
        if (getTaskCount() == 0) {
            return null;
        }
        int launchTaskIndex = indexOfTask(getLaunchTarget());
        if (launchTaskIndex == -1 || launchTaskIndex <= 0) {
            return null;
        }
        return getTasks().get(launchTaskIndex - 1);
    }

    public int indexOfTask(Task t) {
        return this.mStackTaskList.indexOf(t);
    }

    public Task findTaskWithId(int taskId) {
        ArrayList<Task> tasks = computeAllTasksList();
        int taskCount = tasks.size();
        for (int i = 0; i < taskCount; i++) {
            Task task = tasks.get(i);
            if (task != null && task.key != null && task.key.id == taskId) {
                return task;
            }
        }
        return null;
    }

    public ArraySet<ComponentName> computeComponentsRemoved(String packageName, int userId) {
        ArraySet<ComponentName> existingComponents = new ArraySet<>();
        ArraySet<ComponentName> removedComponents = new ArraySet<>();
        ArrayList<Task.TaskKey> taskKeys = getTaskKeys();
        int taskKeyCount = taskKeys.size();
        for (int i = 0; i < taskKeyCount; i++) {
            Task.TaskKey t = taskKeys.get(i);
            if (t.userId == userId) {
                ComponentName cn = t.getComponent();
                if (cn.getPackageName().equals(packageName) && !existingComponents.contains(cn)) {
                    if (PackageManagerWrapper.getInstance().getActivityInfo(cn, userId) != null) {
                        existingComponents.add(cn);
                    } else {
                        removedComponents.add(cn);
                    }
                }
            }
        }
        return removedComponents;
    }

    public String toString() {
        String str = "Stack Tasks (" + this.mStackTaskList.size() + "):\n";
        for (int i = 0; i < this.mStackTaskList.getTasks().size(); i++) {
            str = str + "    " + tasks.get(i).toString() + "\n";
        }
        return str;
    }

    private ArrayMap<Task.TaskKey, Task> createTaskKeyMapFromList(List<Task> tasks) {
        ArrayMap<Task.TaskKey, Task> map = new ArrayMap<>(tasks.size());
        int taskCount = tasks.size();
        for (int i = 0; i < taskCount; i++) {
            Task task = tasks.get(i);
            map.put(task.key, task);
        }
        return map;
    }

    public void dump(String prefix, PrintWriter writer) {
        String innerPrefix = prefix + "  ";
        writer.print(prefix);
        writer.print(TAG);
        writer.print(" numStackTasks=");
        writer.print(this.mStackTaskList.size());
        writer.println();
        ArrayList<Task> tasks = this.mStackTaskList.getTasks();
        int taskCount = tasks.size();
        for (int i = 0; i < taskCount; i++) {
            tasks.get(i).dump(innerPrefix, writer);
        }
    }
}
