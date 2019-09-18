package com.android.server.gesture;

import android.content.Context;
import android.view.MotionEvent;
import com.android.server.gesture.DeviceStateController;

public class QuickStartupStub {
    protected Context mContext;
    private final DeviceStateController.DeviceChangedListener mDeviceChangedCallback = new DeviceStateController.DeviceChangedListener() {
        public void onUserSwitched(int newUserId) {
            QuickStartupStub.this.updateSettings();
        }

        public void onConfigurationChanged() {
            QuickStartupStub.this.updateConfig();
        }
    };
    protected DeviceStateController mDeviceStateController;
    protected boolean mGestureFailed;
    protected int mGestureFailedReason;
    protected boolean mGestureReallyStarted;
    protected boolean mGestureSlowProcessStarted;
    protected boolean mIsFastSlideGesture;
    protected boolean mIsValidGuesture = true;

    public QuickStartupStub(Context context) {
        this.mContext = context;
        this.mDeviceStateController = DeviceStateController.getInstance(context);
    }

    public void setGestureResultAtUp(boolean success, int failedReason) {
        this.mIsValidGuesture = this.mIsValidGuesture && success;
    }

    public void onGestureStarted() {
    }

    public void onGestureReallyStarted() {
        this.mGestureReallyStarted = true;
    }

    public void onGestureSlowProcessStarted() {
        this.mGestureSlowProcessStarted = true;
    }

    public void onGestureSlowProcess(float distance, float offsetX, float offsetY) {
    }

    public void onGestureFailed(int reason, int action) {
        this.mGestureFailed = true;
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
        this.mGestureReallyStarted = false;
        this.mGestureSlowProcessStarted = false;
        this.mGestureFailed = false;
        this.mIsFastSlideGesture = false;
        this.mGestureFailedReason = 0;
    }

    public void handleTouchEvent(MotionEvent event) {
        int actionMasked = event.getActionMasked();
        if (actionMasked != 3) {
            switch (actionMasked) {
                case 0:
                    resetAtDown();
                    return;
                case 1:
                    break;
                default:
                    return;
            }
        }
        if (this.mIsValidGuesture) {
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

    public boolean isPreConditionNotReady(boolean left) {
        return false;
    }

    public void quickStartup() {
    }

    public void updateSettings() {
    }

    public void updateConfig() {
    }
}
