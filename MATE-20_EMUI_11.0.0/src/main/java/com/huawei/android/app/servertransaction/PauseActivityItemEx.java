package com.huawei.android.app.servertransaction;

import android.app.servertransaction.PauseActivityItem;

public class PauseActivityItemEx {
    private PauseActivityItem mPauseActivityItem;

    public PauseActivityItem getPauseActivityItem() {
        return this.mPauseActivityItem;
    }

    public void setPauseActivityItem(PauseActivityItem pauseActivityItem) {
        this.mPauseActivityItem = pauseActivityItem;
    }

    public static PauseActivityItemEx obtain(boolean finished, boolean userLeaving, int configChanges, boolean dontReport) {
        PauseActivityItemEx pauseActivityItemEx = new PauseActivityItemEx();
        pauseActivityItemEx.setPauseActivityItem(PauseActivityItem.obtain(finished, userLeaving, configChanges, dontReport));
        return pauseActivityItemEx;
    }
}
