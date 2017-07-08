package com.android.server.wm;

public abstract class AbsTaskStack {
    public boolean moveMwTask(Task task, int aIndex) {
        return false;
    }

    public boolean addTask(Task task, int aIndex) {
        return false;
    }
}
