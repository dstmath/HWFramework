package com.android.server.wm;

import com.android.server.wm.DisplayContent;

public class TaskStackBridge extends TaskStack {
    private TaskStackBridgeEx mTaskStackEx;

    public TaskStackBridge(WindowManagerService service, int stackId, ActivityStack activityStack) {
        super(service, stackId, activityStack);
    }

    public void setTaskStackEx(TaskStackBridgeEx taskStackEx) {
        this.mTaskStackEx = taskStackEx;
    }

    /* access modifiers changed from: protected */
    public void setAnimationBackground(WindowStateAnimator winAnimator, int color) {
        WindowStateAnimatorEx adapter = null;
        if (winAnimator != null) {
            adapter = new WindowStateAnimatorEx();
            adapter.setWindowStateAnimator(winAnimator);
        }
        this.mTaskStackEx.setAnimationBackground(adapter, color);
    }

    /* access modifiers changed from: protected */
    public void setAospAnimationBackground(WindowStateAnimatorEx winAnimator, int color) {
        TaskStackBridge.super.setAnimationBackground(winAnimator == null ? null : winAnimator.getWindowStateAnimator(), color);
    }

    /* access modifiers changed from: protected */
    public void findTaskForResizePoint(int pointX, int pointY, int delta, DisplayContent.TaskForResizePointSearchResult results) {
        TaskForResizePointSearchResultEx resultEx = new TaskForResizePointSearchResultEx();
        resultEx.setTaskForResizePointSearchResult(results);
        this.mTaskStackEx.findTaskForResizePoint(pointX, pointY, delta, resultEx);
    }

    /* access modifiers changed from: protected */
    public void aospFindTaskForResizePoint(int pointX, int pointY, int delta, TaskForResizePointSearchResultEx results) {
        if (results != null) {
            TaskStackBridge.super.findTaskForResizePoint(pointX, pointY, delta, results.getResult());
        }
    }
}
