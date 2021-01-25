package com.android.server.wm;

import android.graphics.Rect;
import android.view.SurfaceControl;

public class TaskEx {
    private Task mTask;

    public TaskEx() {
    }

    public TaskEx(Task task) {
        this.mTask = task;
    }

    public Task getTask() {
        return this.mTask;
    }

    public void setTask(Task task) {
        this.mTask = task;
    }

    public void positionChildAt(AppWindowTokenExt aToken, int position) {
        Task task = this.mTask;
        if (task != null && aToken != null) {
            task.positionChildAt(aToken.getAppWindowToken(), position);
        }
    }

    public AppWindowTokenExt getAppWindowTokenExOfIndex(int index) {
        if (this.mTask == null) {
            return null;
        }
        AppWindowTokenExt appWindowTokenEx = new AppWindowTokenExt();
        appWindowTokenEx.setAppWindowToken((AppWindowToken) this.mTask.mChildren.get(index));
        return appWindowTokenEx;
    }

    public Rect getBounds() {
        Task task = this.mTask;
        if (task != null) {
            return task.getBounds();
        }
        return null;
    }

    public void getBounds(Rect outBounds) {
        Task task = this.mTask;
        if (task != null) {
            task.getBounds(outBounds);
        }
    }

    public DisplayContentEx getDisplayContentEx() {
        if (this.mTask == null) {
            return null;
        }
        DisplayContentEx displayContentEx = new DisplayContentEx();
        displayContentEx.setDisplayContent(this.mTask.getDisplayContent());
        return displayContentEx;
    }

    public int getChildCount() {
        Task task = this.mTask;
        if (task != null) {
            return task.getChildCount();
        }
        return 0;
    }

    public SurfaceControl getSurfaceControl() {
        Task task = this.mTask;
        if (task != null) {
            return task.getSurfaceControl();
        }
        return null;
    }

    public void getDimBounds(Rect out) {
        this.mTask.getDimBounds(out);
    }

    public WindowStateEx getTopVisibleAppMainWindow() {
        if (this.mTask.getTopVisibleAppMainWindow() == null) {
            return null;
        }
        return new WindowStateEx(this.mTask.getTopVisibleAppMainWindow());
    }

    public int getTaskId() {
        return this.mTask.mTaskId;
    }

    public int setBounds(Rect bounds) {
        Task task = this.mTask;
        if (task != null) {
            return task.setBounds(bounds);
        }
        return 0;
    }

    public boolean isFloating() {
        Task task = this.mTask;
        if (task != null) {
            return task.isFloating();
        }
        return false;
    }
}
