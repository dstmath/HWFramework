package com.android.server.gesture;

import android.content.Context;
import android.view.MotionEvent;
import com.android.server.gesture.DefaultDeviceStateController;

public class QuickStartupStub {
    protected Context mContext;
    private final DefaultDeviceStateController.DeviceChangedListener mDeviceChangedCallback = new DefaultDeviceStateController.DeviceChangedListener() {
        /* class com.android.server.gesture.QuickStartupStub.AnonymousClass1 */

        public void onUserSwitched(int newUserId) {
            QuickStartupStub.this.updateSettings();
        }

        public void onConfigurationChanged() {
            QuickStartupStub.this.updateConfig();
        }
    };
    protected DeviceStateController mDeviceStateController;
    protected int mGestureFailedReason;
    protected boolean mIsFastSlideGesture;
    protected boolean mIsGestureFailed;
    protected boolean mIsGestureReallyStarted;
    protected boolean mIsGestureSlowProcessStarted;
    protected boolean mIsValidGuesture = true;

    public QuickStartupStub(Context context) {
        this.mContext = context;
        this.mDeviceStateController = DeviceStateController.getInstance(context);
    }

    public void setGestureResultAtUp(boolean isSuccess, int failedReason) {
        this.mIsValidGuesture = this.mIsValidGuesture && isSuccess;
    }

    public void onGestureStarted() {
    }

    public void onGestureReallyStarted() {
        this.mIsGestureReallyStarted = true;
    }

    public void onGestureSlowProcessStarted() {
        this.mIsGestureSlowProcessStarted = true;
    }

    public void onGestureSlowProcess(float distance, float offsetX, float offsetY) {
    }

    public void onGestureFailed(int reason, int action) {
        this.mIsGestureFailed = true;
        this.mGestureFailedReason = reason;
    }

    public void onGestureSuccessFinished(float distance, long durationTime, float velocity, boolean isFastSlideGesture, Runnable runnable) {
        this.mIsFastSlideGesture = isFastSlideGesture;
    }

    public void onGestureEnd(int action) {
    }

    /* access modifiers changed from: protected */
    public void resetAtDown() {
        this.mIsValidGuesture = true;
        this.mIsGestureReallyStarted = false;
        this.mIsGestureSlowProcessStarted = false;
        this.mIsGestureFailed = false;
        this.mIsFastSlideGesture = false;
        this.mGestureFailedReason = 0;
    }

    public void handleTouchEvent(MotionEvent event) {
        int actionMasked = event.getActionMasked();
        if (actionMasked == 0) {
            resetAtDown();
        } else if ((actionMasked == 1 || actionMasked == 3) && this.mIsValidGuesture) {
            quickStartup();
        }
    }

    public void onNavCreate(GestureNavView navView) {
        this.mDeviceStateController.addCallback(this.mDeviceChangedCallback);
    }

    public void onNavUpdate() {
    }

    public void onNavDestroy() {
        this.mDeviceStateController.removeCallback(this.mDeviceChangedCallback);
    }

    public boolean isPreConditionNotReady(boolean isLeft) {
        return false;
    }

    public void quickStartup() {
    }

    public void updateSettings() {
    }

    public void updateConfig() {
    }
}
