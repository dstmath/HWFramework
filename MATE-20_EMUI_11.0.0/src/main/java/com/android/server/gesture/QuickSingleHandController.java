package com.android.server.gesture;

import android.content.Context;
import android.hardware.display.HwFoldScreenState;
import android.os.Looper;
import android.provider.Settings;
import android.util.Flog;
import android.util.Log;
import android.view.MotionEvent;
import com.android.server.policy.SlideTouchEvent;
import com.huawei.android.fsm.HwFoldScreenManagerInternal;
import com.huawei.android.server.LocalServicesExt;

public class QuickSingleHandController extends QuickStartupStub {
    private HwFoldScreenManagerInternal mFsmInternal;
    private boolean mIsFoldDevice;
    private SlideTouchEvent.SlideGestureListener mSlideGestureListener = new SlideTouchEvent.SlideGestureListener() {
        /* class com.android.server.gesture.QuickSingleHandController.AnonymousClass1 */

        @Override // com.android.server.policy.SlideTouchEvent.SlideGestureListener
        public boolean onSlideGestureSuccess() {
            return !QuickSingleHandController.this.isSingleHandOldGestureDisabled();
        }
    };
    private SlideTouchEvent mSlideTouchEvent;

    public QuickSingleHandController(Context context, Looper looper) {
        super(context);
        this.mSlideTouchEvent = new SlideTouchEvent(context, this.mSlideGestureListener);
        this.mIsFoldDevice = HwFoldScreenState.isFoldScreenDevice();
        if (this.mIsFoldDevice) {
            this.mFsmInternal = (HwFoldScreenManagerInternal) LocalServicesExt.getService(HwFoldScreenManagerInternal.class);
        }
    }

    @Override // com.android.server.gesture.QuickStartupStub
    public boolean isPreConditionNotReady(boolean isOnLeft) {
        if (!this.mDeviceStateController.isKeyguardLocked()) {
            return false;
        }
        if (!GestureNavConst.DEBUG) {
            return true;
        }
        Log.i(GestureNavConst.TAG_GESTURE_QSH, "not ready as keygaurd locked or not fold main screen");
        return true;
    }

    @Override // com.android.server.gesture.QuickStartupStub
    public void updateSettings() {
        super.updateSettings();
        this.mSlideTouchEvent.updateSettings();
    }

    @Override // com.android.server.gesture.QuickStartupStub
    public void setGestureResultAtUp(boolean isSuccess, int failedReason) {
        this.mSlideTouchEvent.setGestureResultAtUp(isSuccess);
        Flog.bdReport(991310855, GestureNavConst.reportResultStr(isSuccess, failedReason));
    }

    @Override // com.android.server.gesture.QuickStartupStub
    public void handleTouchEvent(MotionEvent event) {
        this.mSlideTouchEvent.handleTouchEvent(event);
    }

    public boolean isSingleHandOldGestureDisabled() {
        return Settings.Global.getInt(this.mContext.getContentResolver(), "single_hand_old_gesture_disable", 0) == 1;
    }

    public boolean isSingleHandEnableAndAvailable() {
        return this.mSlideTouchEvent.isSingleHandEnableAndAvailable() && !isFoldDisplaySingleHandDisabled();
    }

    public boolean isBeginFailedAsExceedDegree() {
        return this.mSlideTouchEvent.isBeginFailedAsExceedDegree();
    }

    public void interrupt() {
        if (GestureNavConst.DEBUG) {
            Log.i(GestureNavConst.TAG_GESTURE_QSH, "single hand event is interrupted");
        }
    }

    private boolean isFoldDisplaySingleHandDisabled() {
        HwFoldScreenManagerInternal hwFoldScreenManagerInternal;
        if (!this.mIsFoldDevice || (hwFoldScreenManagerInternal = this.mFsmInternal) == null || hwFoldScreenManagerInternal.getDisplayMode() == 2) {
            return false;
        }
        return true;
    }
}
