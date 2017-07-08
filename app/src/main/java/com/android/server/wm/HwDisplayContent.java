package com.android.server.wm;

import android.view.Display;

public class HwDisplayContent extends DisplayContent {
    public HwDisplayContent(Display display, WindowManagerService service) {
        super(display, service);
    }

    public void addTask(Task task, int aIndex) {
        this.mTmpTaskHistory.remove(task);
        int userId = task.mUserId;
        int taskNdx = this.mTmpTaskHistory.size() - 1;
        while (taskNdx >= 0 && ((Task) this.mTmpTaskHistory.get(taskNdx)).mUserId != userId) {
            taskNdx--;
        }
        this.mTmpTaskHistory.add((taskNdx + 1) - aIndex, task);
    }
}
