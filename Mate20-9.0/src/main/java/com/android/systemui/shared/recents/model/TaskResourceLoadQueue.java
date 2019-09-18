package com.android.systemui.shared.recents.model;

import java.util.concurrent.ConcurrentLinkedQueue;

class TaskResourceLoadQueue {
    private final ConcurrentLinkedQueue<Task> mQueue = new ConcurrentLinkedQueue<>();

    TaskResourceLoadQueue() {
    }

    /* access modifiers changed from: package-private */
    public void addTask(Task t) {
        if (!this.mQueue.contains(t)) {
            this.mQueue.add(t);
        }
        synchronized (this) {
            notifyAll();
        }
    }

    /* access modifiers changed from: package-private */
    public Task nextTask() {
        return this.mQueue.poll();
    }

    /* access modifiers changed from: package-private */
    public void removeTask(Task t) {
        this.mQueue.remove(t);
    }

    /* access modifiers changed from: package-private */
    public void clearTasks() {
        this.mQueue.clear();
    }

    /* access modifiers changed from: package-private */
    public boolean isEmpty() {
        return this.mQueue.isEmpty();
    }
}
