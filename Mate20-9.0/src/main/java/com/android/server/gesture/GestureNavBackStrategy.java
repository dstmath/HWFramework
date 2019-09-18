package com.android.server.gesture;

import android.content.Context;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Flog;
import android.util.Log;
import android.util.MathUtils;
import com.android.server.gesture.GestureNavView;
import java.util.ArrayList;

public class GestureNavBackStrategy extends GestureNavBaseStrategy {
    private static final int MSG_CHECK_HAPTICS_VIBRATOR = 1;
    private boolean mAnimPositionSetup;
    private boolean mAnimProcessedOnce;
    private boolean mAnimProcessing;
    private Handler mBackHandler;
    private int mBackMaxDistance1;
    private int mBackMaxDistance2;
    private GestureDataTracker mGestureDataTracker;
    private GestureNavView.IGestureNavBackAnim mGestureNavBackAnim;

    private final class BackHandler extends Handler {
        public BackHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                GestureUtils.performHapticFeedbackIfNeed(GestureNavBackStrategy.this.mContext);
            }
        }
    }

    public GestureNavBackStrategy(int navId, Context context, Looper looper, GestureNavView.IGestureNavBackAnim backAnim) {
        super(navId, context, looper);
        this.mGestureNavBackAnim = backAnim;
        this.mBackHandler = new BackHandler(looper);
        this.mGestureDataTracker = GestureDataTracker.getInstance(context);
    }

    public void updateConfig(int displayWidth, int displayHeight, Rect r) {
        super.updateConfig(displayWidth, displayHeight, r);
        this.mBackMaxDistance1 = GestureNavConst.getBackMaxDistance1(this.mContext);
        this.mBackMaxDistance2 = GestureNavConst.getBackMaxDistance2(this.mContext);
        if (GestureNavConst.DEBUG_ALL) {
            Log.d(GestureNavConst.TAG_GESTURE_BACK, "distance1:" + this.mBackMaxDistance1 + ", distance2:" + this.mBackMaxDistance2);
        }
    }

    /* access modifiers changed from: protected */
    public void onGestureStarted(float rawX, float rawY) {
        super.onGestureStarted(rawX, rawY);
        boolean z = false;
        this.mAnimPositionSetup = false;
        this.mAnimProcessing = false;
        this.mAnimProcessedOnce = true;
        GestureNavView.IGestureNavBackAnim iGestureNavBackAnim = this.mGestureNavBackAnim;
        if (this.mNavId == 1) {
            z = true;
        }
        iGestureNavBackAnim.setSide(z);
    }

    /* access modifiers changed from: protected */
    public void onGestureReallyStarted() {
        super.onGestureReallyStarted();
        if (!this.mAnimPositionSetup) {
            this.mAnimPositionSetup = true;
            this.mGestureNavBackAnim.setAnimPosition(this.mTouchDownRawY);
        }
    }

    /* access modifiers changed from: protected */
    public void onGestureSlowProcessStarted(ArrayList<Float> pendingMoveDistance) {
        super.onGestureSlowProcessStarted(pendingMoveDistance);
        if (!this.mAnimProcessing) {
            this.mAnimProcessing = true;
        }
        if (pendingMoveDistance != null) {
            int size = pendingMoveDistance.size();
            int size2 = size;
            if (size > 0) {
                for (int i = 0; i < size2; i++) {
                    notifyAnimProcess(pendingMoveDistance.get(i).floatValue());
                }
                if (GestureNavConst.DEBUG != 0) {
                    Log.d(GestureNavConst.TAG_GESTURE_BACK, "interpolate " + size2 + " pending datas");
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public void onGestureSlowProcess(float distance, float offsetX, float offsetY) {
        super.onGestureSlowProcess(distance, offsetX, offsetY);
        notifyAnimProcess(distance);
    }

    /* access modifiers changed from: protected */
    public void onGestureFailed(int reason, int action) {
        super.onGestureFailed(reason, action);
        if (this.mAnimPositionSetup) {
            if (GestureNavConst.DEBUG) {
                Log.i(GestureNavConst.TAG_GESTURE_BACK, "gesture failed, disappear anim");
            }
            this.mGestureNavBackAnim.playDisappearAnim();
        }
        if (isEffectiveFailedReason(reason)) {
            this.mGestureDataTracker.gestureBackEvent(this.mNavId, false);
        }
        Flog.bdReport(this.mContext, 854, GestureNavConst.reportResultStr(false, this.mNavId, reason));
    }

    /* access modifiers changed from: protected */
    public void onGestureSuccessFinished(float distance, long durationTime, float velocity, boolean isFastSlideGesture) {
        super.onGestureSuccessFinished(distance, durationTime, velocity, isFastSlideGesture);
        checkHwHapticsVibrator();
        sendKeyEvent(4);
        if (this.mAnimProcessing && this.mAnimProcessedOnce) {
            if (GestureNavConst.DEBUG) {
                Log.i(GestureNavConst.TAG_GESTURE_BACK, "gesture finished, disappear anim");
            }
            this.mGestureNavBackAnim.playDisappearAnim();
        } else if (isFastSlideGesture) {
            if (GestureNavConst.DEBUG) {
                Log.i(GestureNavConst.TAG_GESTURE_BACK, "gesture finished, play fast anim, velocity=" + velocity);
            }
            if (!this.mAnimPositionSetup) {
                this.mAnimPositionSetup = true;
                this.mGestureNavBackAnim.setAnimPosition(this.mTouchDownRawY);
            }
            this.mGestureNavBackAnim.playFastSlidingAnim();
        } else {
            if (GestureNavConst.DEBUG) {
                Log.i(GestureNavConst.TAG_GESTURE_BACK, "velocity does not meet the threshold, disappear anim");
            }
            this.mGestureNavBackAnim.playDisappearAnim();
        }
        this.mGestureDataTracker.gestureBackEvent(this.mNavId, true);
        Flog.bdReport(this.mContext, 854, GestureNavConst.reportResultStr(true, this.mNavId, -1));
    }

    private void notifyAnimProcess(float distance) {
        float process = getRubberbandProcess(distance);
        boolean success = this.mGestureNavBackAnim.setAnimProcess(process);
        if (!this.mAnimProcessedOnce && success) {
            this.mAnimProcessedOnce = true;
        }
        if (GestureNavConst.DEBUG_ALL) {
            Log.d(GestureNavConst.TAG_GESTURE_BACK, "process=" + process + ", distance=" + distance + ", animOnce=" + this.mAnimProcessedOnce);
        }
    }

    private float getRubberbandProcess(float distance) {
        if (distance < GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO) {
            return GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
        }
        if (distance >= ((float) this.mBackMaxDistance1)) {
            return 0.88f + (MathUtils.constrain((distance - ((float) this.mBackMaxDistance1)) / ((float) (this.mBackMaxDistance2 - this.mBackMaxDistance1)), GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO, 1.0f) * 0.120000005f);
        }
        float process = (distance / ((float) this.mBackMaxDistance1)) * 0.88f;
        float f = 0.1f;
        if (process >= 0.1f) {
            f = process;
        }
        return f;
    }

    private void checkHwHapticsVibrator() {
        if (!this.mBackHandler.hasMessages(1)) {
            this.mBackHandler.sendEmptyMessage(1);
        }
    }
}
