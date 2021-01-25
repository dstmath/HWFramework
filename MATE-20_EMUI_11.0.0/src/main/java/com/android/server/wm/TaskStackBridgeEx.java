package com.android.server.wm;

public class TaskStackBridgeEx {
    protected final int mStackId;
    private TaskStackBridge mTaskStackBridge;

    public TaskStackBridgeEx(WindowManagerServiceEx serviceEx, int stackId, ActivityStackEx activityStackEx) {
        this.mTaskStackBridge = new TaskStackBridge(serviceEx.getWindowManagerService(), stackId, activityStackEx.getActivityStack());
        this.mTaskStackBridge.setTaskStackEx(this);
        this.mStackId = stackId;
    }

    public TaskStackBridge getTaskStackBridge() {
        return this.mTaskStackBridge;
    }

    /* access modifiers changed from: protected */
    public void setAnimationBackground(WindowStateAnimatorEx winAnimator, int color) {
    }

    /* access modifiers changed from: protected */
    public void setAospAnimationBackground(WindowStateAnimatorEx winAnimator, int color) {
        this.mTaskStackBridge.setAospAnimationBackground(winAnimator, color);
    }

    /* access modifiers changed from: protected */
    public void findTaskForResizePoint(int x, int y, int delta, TaskForResizePointSearchResultEx results) {
    }

    /* access modifiers changed from: protected */
    public void aospFindTaskForResizePoint(int pointX, int pointY, int delta, TaskForResizePointSearchResultEx results) {
        this.mTaskStackBridge.aospFindTaskForResizePoint(pointX, pointY, delta, results);
    }

    /* access modifiers changed from: protected */
    public boolean isVisible() {
        return this.mTaskStackBridge.isVisible();
    }
}
