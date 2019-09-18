package com.android.server.gesture;

import android.content.Context;
import android.os.Looper;
import android.util.Flog;
import android.util.Log;
import android.view.MotionEvent;
import com.android.server.policy.SlideTouchEvent;

public class QuickSingleHandController extends QuickStartupStub {
    private SlideTouchEvent mSlideTouchEvent;

    public QuickSingleHandController(Context context, Looper looper) {
        super(context);
        this.mSlideTouchEvent = new SlideTouchEvent(context);
    }

    public boolean isPreConditionNotReady(boolean onLeft) {
        if (this.mDeviceStateController.isNavBarAtBottom() && !this.mDeviceStateController.isKeyguardLocked()) {
            return false;
        }
        if (GestureNavConst.DEBUG) {
            Log.i(GestureNavConst.TAG_GESTURE_QSH, "nav bar not in bottom or keygaurdLocked");
        }
        return true;
    }

    public void updateSettings() {
        super.updateSettings();
        this.mSlideTouchEvent.updateSettings();
    }

    public void setGestureResultAtUp(boolean success, int failedReason) {
        this.mSlideTouchEvent.setGestureResultAtUp(success);
        Flog.bdReport(this.mContext, 855, GestureNavConst.reportResultStr(success, failedReason));
    }

    public void handleTouchEvent(MotionEvent event) {
        this.mSlideTouchEvent.handleTouchEvent(event);
    }
}
