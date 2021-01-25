package com.android.systemui.shared.system;

import android.app.ActivityManager;
import android.os.RemoteException;
import android.util.Log;
import android.view.IRecentsAnimationController;
import com.android.systemui.shared.recents.model.ThumbnailData;

public class RecentsAnimationControllerCompat {
    private static final String TAG = RecentsAnimationControllerCompat.class.getSimpleName();
    private IRecentsAnimationController mAnimationController;

    public RecentsAnimationControllerCompat() {
    }

    public RecentsAnimationControllerCompat(IRecentsAnimationController animationController) {
        this.mAnimationController = animationController;
    }

    public ThumbnailData screenshotTask(int taskId) {
        try {
            ActivityManager.TaskSnapshot snapshot = this.mAnimationController.screenshotTask(taskId);
            return snapshot != null ? new ThumbnailData(snapshot) : new ThumbnailData();
        } catch (RemoteException e) {
            Log.e(TAG, "Failed to screenshot task", e);
            return new ThumbnailData();
        }
    }

    public void setInputConsumerEnabled(boolean enabled) {
        try {
            this.mAnimationController.setInputConsumerEnabled(enabled);
        } catch (RemoteException e) {
            Log.e(TAG, "Failed to set input consumer enabled state", e);
        }
    }

    public void setAnimationTargetsBehindSystemBars(boolean behindSystemBars) {
        try {
            this.mAnimationController.setAnimationTargetsBehindSystemBars(behindSystemBars);
        } catch (RemoteException e) {
            Log.e(TAG, "Failed to set whether animation targets are behind system bars", e);
        }
    }

    public void setSplitScreenMinimized(boolean minimized) {
        try {
            this.mAnimationController.setSplitScreenMinimized(minimized);
        } catch (RemoteException e) {
            Log.e(TAG, "Failed to set minimize dock", e);
        }
    }

    public void hideCurrentInputMethod() {
        try {
            this.mAnimationController.hideCurrentInputMethod();
        } catch (RemoteException e) {
            Log.e(TAG, "Failed to set hide input method", e);
        }
    }

    public void finish(boolean toHome, boolean sendUserLeaveHint) {
        try {
            this.mAnimationController.finish(toHome, sendUserLeaveHint);
        } catch (RemoteException e) {
            Log.e(TAG, "Failed to finish recents animation", e);
        }
    }

    public void setCancelWithDeferredScreenshot(boolean screenshot) {
        try {
            this.mAnimationController.setCancelWithDeferredScreenshot(screenshot);
        } catch (RemoteException e) {
            Log.e(TAG, "Failed to set cancel with deferred screenshot", e);
        }
    }

    public void cleanupScreenshot() {
        try {
            this.mAnimationController.cleanupScreenshot();
        } catch (RemoteException e) {
            Log.e(TAG, "Failed to clean up screenshot of recents animation", e);
        }
    }
}
