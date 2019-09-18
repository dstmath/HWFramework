package com.android.systemui.shared.recents.model;

import android.util.ArrayMap;
import android.util.SparseArray;
import com.android.systemui.shared.recents.model.Task;
import java.util.ArrayList;
import java.util.List;

class FilteredTaskList {
    private TaskFilter mFilter;
    private final ArrayMap<Task.TaskKey, Integer> mFilteredTaskIndices = new ArrayMap<>();
    private final ArrayList<Task> mFilteredTasks = new ArrayList<>();
    private final ArrayList<Task> mTasks = new ArrayList<>();

    FilteredTaskList() {
    }

    /* access modifiers changed from: package-private */
    public boolean setFilter(TaskFilter filter) {
        ArrayList<Task> prevFilteredTasks = new ArrayList<>(this.mFilteredTasks);
        this.mFilter = filter;
        updateFilteredTasks();
        synchronized (this.mFilteredTasks) {
            if (!prevFilteredTasks.equals(this.mFilteredTasks)) {
                return true;
            }
            return false;
        }
    }

    /* access modifiers changed from: package-private */
    public void add(Task t) {
        this.mTasks.add(t);
        updateFilteredTasks();
    }

    /* access modifiers changed from: package-private */
    public void set(List<Task> tasks) {
        this.mTasks.clear();
        this.mTasks.addAll(tasks);
        updateFilteredTasks();
    }

    /* access modifiers changed from: package-private */
    public boolean remove(Task t) {
        synchronized (this.mFilteredTasks) {
            if (!this.mFilteredTasks.contains(t)) {
                return false;
            }
            boolean removed = this.mTasks.remove(t);
            updateFilteredTasks();
            return removed;
        }
    }

    /* access modifiers changed from: package-private */
    public int indexOf(Task t) {
        if (t == null || !this.mFilteredTaskIndices.containsKey(t.key)) {
            return -1;
        }
        return this.mFilteredTaskIndices.get(t.key).intValue();
    }

    /* access modifiers changed from: package-private */
    public int size() {
        int size;
        synchronized (this.mFilteredTasks) {
            size = this.mFilteredTasks.size();
        }
        return size;
    }

    /* access modifiers changed from: package-private */
    public boolean contains(Task t) {
        return this.mFilteredTaskIndices.containsKey(t.key);
    }

    private void updateFilteredTasks() {
        ArrayList<Task> mFilteredTasksTemp = new ArrayList<>();
        if (this.mFilter != null) {
            SparseArray<Task> taskIdMap = new SparseArray<>();
            int taskCount = this.mTasks.size();
            for (int i = 0; i < taskCount; i++) {
                Task t = this.mTasks.get(i);
                taskIdMap.put(t.key.id, t);
            }
            for (int i2 = 0; i2 < taskCount; i2++) {
                Task t2 = this.mTasks.get(i2);
                if (this.mFilter.acceptTask(taskIdMap, t2, i2)) {
                    mFilteredTasksTemp.add(t2);
                }
            }
        } else {
            mFilteredTasksTemp.addAll(this.mTasks);
        }
        synchronized (this.mFilteredTasks) {
            this.mFilteredTasks.clear();
            this.mFilteredTasks.addAll(mFilteredTasksTemp);
        }
        updateFilteredTaskIndices();
    }

    private void updateFilteredTaskIndices() {
        ArrayList<Task> mFilteredTasksTemp = new ArrayList<>();
        synchronized (this.mFilteredTasks) {
            mFilteredTasksTemp.addAll(this.mFilteredTasks);
        }
        int taskCount = mFilteredTasksTemp.size();
        this.mFilteredTaskIndices.clear();
        for (int i = 0; i < taskCount; i++) {
            this.mFilteredTaskIndices.put(mFilteredTasksTemp.get(i).key, Integer.valueOf(i));
        }
    }

    /* access modifiers changed from: package-private */
    public ArrayList<Task> getTasks() {
        ArrayList<Task> arrayList;
        synchronized (this.mFilteredTasks) {
            arrayList = this.mFilteredTasks;
        }
        return arrayList;
    }
}
