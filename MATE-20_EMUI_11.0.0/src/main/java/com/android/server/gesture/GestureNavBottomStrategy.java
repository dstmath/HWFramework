package com.android.server.gesture;

import android.content.Context;
import android.graphics.Rect;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.SystemClock;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.Toast;
import com.android.server.wm.WindowManagerInternalEx;
import com.huawei.android.app.StatusBarManagerExt;
import com.huawei.android.fsm.HwFoldScreenManagerEx;
import com.huawei.android.view.WindowManagerEx;
import com.huawei.android.widget.ToastEx;
import com.huawei.server.inputmethod.InputMethodManagerInternalEx;
import com.huawei.util.HwPartCommInterfaceWraper;
import com.huawei.utils.HwPartResourceUtils;
import java.io.PrintWriter;
import java.util.ArrayList;

public class GestureNavBottomStrategy extends GestureNavBaseStrategy {
    private static final int GESTURE_NAV_EVENT_TRANSACTION_CODE = 123;
    private static final long HIDE_RETRY_TOAST_DELAY = 300;
    private static final int MSG_HIDE_INPUTMETHOD_IF_NEED = 1;
    private static final int STARTUP_TARGET_DISMISS_SLIDE_TARGET = 6;
    private static final int STARTUP_TARGET_LEFT_CORNER = 1;
    private static final int STARTUP_TARGET_NONE = 0;
    private static final int STARTUP_TARGET_QUICK_STEP = 3;
    private static final int STARTUP_TARGET_RIGHT_CORNER = 5;
    private static final int STARTUP_TARGET_SLIDE_LEFT = 2;
    private static final int STARTUP_TARGET_SLIDE_RIGHT = 4;
    private Handler mBottomHandler;
    private boolean mFirstAftTriggered;
    private final Runnable mGoHomeRunnable = new Runnable() {
        /* class com.android.server.gesture.GestureNavBottomStrategy.AnonymousClass1 */

        @Override // java.lang.Runnable
        public void run() {
            GestureNavBottomStrategy.this.sendKeyEvent(3);
        }
    };
    private boolean mIsGestureNavTipsEnabled;
    private boolean mIsHorizontalSwitchConfigEnabled;
    private boolean mIsInTaskLockMode;
    private boolean mIsKeyguardShowing;
    private boolean mIsLandscape;
    private long mLastAftGestureTime;
    private boolean mPreConditionNotReady;
    private QuickSingleHandController mQuickSingleHandController;
    private QuickSlideOutController mQuickSlideOutController;
    private QuickStepController mQuickStepController;
    private Toast mRetryToast;
    private final Object mServiceAquireLock = new Object();
    private boolean mShouldCheckAftForThisGesture;
    private int mSideWidth;
    private int mStartupTarget = 0;
    private StatusBarManagerExt mStatusBarService;
    private WindowManagerInternalEx mWindowManagerInternal;

    public GestureNavBottomStrategy(int navId, Context context, Looper looper) {
        super(navId, context, looper);
        this.mBottomHandler = new BottomHandler(looper);
    }

    private final class BottomHandler extends Handler {
        BottomHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                GestureNavBottomStrategy.this.hideCurrentInputMethod();
            }
        }
    }

    private void notifyStart() {
        this.mIsInTaskLockMode = GestureUtils.isInLockTaskMode();
        this.mQuickStepController = new QuickStepController(this.mContext, this.mLooper);
        this.mQuickSingleHandController = new QuickSingleHandController(this.mContext, this.mLooper);
        this.mQuickSlideOutController = new QuickSlideOutController(this.mContext, this.mLooper);
        if (GestureNavConst.DEBUG) {
            Log.d(GestureNavConst.TAG_GESTURE_BOTTOM, "notifyStart mIsInTaskLockMode=" + this.mIsInTaskLockMode);
        }
    }

    private void notifyStop() {
        this.mQuickStepController = null;
        this.mQuickSingleHandController = null;
        this.mQuickSlideOutController = null;
    }

    private boolean isSingleHandOldGestureDisabled() {
        QuickSingleHandController quickSingleHandController = this.mQuickSingleHandController;
        return quickSingleHandController != null && quickSingleHandController.isSingleHandOldGestureDisabled();
    }

    private boolean isSingleHandEnableAndAvailable() {
        QuickSingleHandController quickSingleHandController = this.mQuickSingleHandController;
        return quickSingleHandController != null && quickSingleHandController.isSingleHandEnableAndAvailable();
    }

    private boolean isSlideOutEnableAndAvailable(boolean isLeft) {
        QuickSlideOutController quickSlideOutController = this.mQuickSlideOutController;
        return quickSlideOutController != null && quickSlideOutController.isSlideOutEnableAndAvailable(isLeft);
    }

    private boolean isSlideTargetShowing() {
        QuickSlideOutController quickSlideOutController = this.mQuickSlideOutController;
        return quickSlideOutController != null && quickSlideOutController.isSlideTargetShowing();
    }

    private int checkStartupTarget(int rawX, int rawY) {
        if (this.mIsKeyguardShowing) {
            return 3;
        }
        int rawAdapt = fitPoint(rawX);
        int width = getRegion().width();
        int singleHandWidth = (int) (GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO * ((float) (getRegion().height() - (GestureNavConst.getBottomQuickOutHeight(this.mContext) - GestureNavConst.getBottomWindowHeight(this.mContext)))));
        if (GestureNavConst.DEBUG) {
            Log.i(GestureNavConst.TAG_GESTURE_BOTTOM, "checkStartupTarget width=" + width + ", rawX=" + rawX + ", rawY=" + rawY + ", singleHandWidth=" + singleHandWidth + ", rawAdapt=" + rawAdapt + ", sideWidth=" + this.mSideWidth);
        }
        if (!this.mIsLandscape && !isSingleHandOldGestureDisabled()) {
            if (rawAdapt <= GestureUtils.getCurvedSideLeftDisp() + singleHandWidth && isSingleHandEnableAndAvailable()) {
                return 1;
            }
            if (rawAdapt >= (width - singleHandWidth) - GestureUtils.getCurvedSideRightDisp() && isSingleHandEnableAndAvailable()) {
                return 5;
            }
        }
        if (this.mIsInTaskLockMode || this.mIsSubScreenGestureNav) {
            return 3;
        }
        if (isSlideTargetShowing()) {
            return 6;
        }
        if (rawAdapt < this.mSideWidth && isSlideOutEnableAndAvailable(true)) {
            return 2;
        }
        int i = this.mSideWidth;
        if ((rawAdapt < i || rawAdapt > width - i) && rawAdapt > width - this.mSideWidth && isSlideOutEnableAndAvailable(false)) {
            return 4;
        }
        return 3;
    }

    public void updateLockTaskState(int lockTaskState) {
        if (GestureNavConst.DEBUG) {
            Log.d(GestureNavConst.TAG_GESTURE_BOTTOM, "lock task state changed, lockTaskState=" + lockTaskState);
        }
        this.mIsInTaskLockMode = GestureUtils.isInLockTaskMode(lockTaskState);
    }

    @Override // com.android.server.gesture.GestureNavBaseStrategy
    public void updateConfig(int displayWidth, int displayHeight, Rect rect, int rotation) {
        super.updateConfig(displayWidth, displayHeight, rect, rotation);
        this.mSideWidth = GestureNavConst.getBottomSideWidth(this.mContext);
    }

    @Override // com.android.server.gesture.GestureNavBaseStrategy
    public void updateKeyguardState(boolean isKeyguardShowing) {
        super.updateKeyguardState(isKeyguardShowing);
        this.mIsKeyguardShowing = isKeyguardShowing;
        QuickStepController quickStepController = this.mQuickStepController;
        if (quickStepController != null) {
            quickStepController.updateKeyguardState(isKeyguardShowing);
        }
    }

    @Override // com.android.server.gesture.GestureNavBaseStrategy
    public void updateScreenConfigState(boolean isLand) {
        super.updateScreenConfigState(isLand);
        this.mIsLandscape = isLand;
        this.mIsHorizontalSwitch = !isLand && this.mIsHorizontalSwitchConfigEnabled;
    }

    @Override // com.android.server.gesture.GestureNavBaseStrategy
    public void updateNavTipsState(boolean isTipsEnable) {
        super.updateNavTipsState(isTipsEnable);
        this.mIsGestureNavTipsEnabled = isTipsEnable;
    }

    @Override // com.android.server.gesture.GestureNavBaseStrategy
    public void onNavCreate(GestureNavView navView) {
        notifyStart();
        this.mQuickStepController.onNavCreate(navView);
        this.mQuickSlideOutController.onNavCreate(navView);
        this.mQuickSingleHandController.onNavCreate(navView);
    }

    @Override // com.android.server.gesture.GestureNavBaseStrategy
    public void onNavUpdate() {
        this.mQuickStepController.onNavUpdate();
        this.mQuickSlideOutController.onNavUpdate();
        this.mQuickSingleHandController.onNavUpdate();
    }

    @Override // com.android.server.gesture.GestureNavBaseStrategy
    public void onNavDestroy() {
        this.mQuickStepController.onNavDestroy();
        this.mQuickSlideOutController.onNavDestroy();
        this.mQuickSingleHandController.onNavDestroy();
        notifyStop();
    }

    @Override // com.android.server.gesture.GestureNavBaseStrategy
    public void updateHorizontalSwitch() {
        this.mIsHorizontalSwitchConfigEnabled = GestureNavConst.isHorizontalSwitchEnabled(this.mContext, -2);
        this.mIsHorizontalSwitch = this.mIsHorizontalSwitchConfigEnabled && !this.mIsLandscape;
    }

    private boolean isGesNavProxyEnable() {
        QuickStepController quickStepController = this.mQuickStepController;
        if (quickStepController != null) {
            return quickStepController.isGesNavProxyEnable();
        }
        return true;
    }

    public void updateBottomVisible(boolean isBottomEnable) {
        QuickStepController quickStepController = this.mQuickStepController;
        if (quickStepController != null) {
            quickStepController.updateBottomVisible(isBottomEnable);
        }
    }

    public void updateCheckTwiceSettings() {
        QuickStepController quickStepController = this.mQuickStepController;
        if (quickStepController != null) {
            quickStepController.updateCheckTwiceSettings();
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.gesture.GestureNavBaseStrategy
    public int moveOutAngleThreshold() {
        int i = this.mStartupTarget;
        if (i == 2 || i == 4) {
            return 45;
        }
        return super.moveOutAngleThreshold();
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.gesture.GestureNavBaseStrategy
    public int slideOutThresholdMajorAxis() {
        QuickSlideOutController quickSlideOutController;
        int i = this.mStartupTarget;
        if ((i == 2 || i == 4) && (quickSlideOutController = this.mQuickSlideOutController) != null) {
            return quickSlideOutController.slideOutThreshold(getWindowThreshold());
        }
        return super.slideOutThresholdMajorAxis();
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.gesture.GestureNavBaseStrategy
    public boolean shouldCheckAbnormalTouch() {
        if (!this.mShouldCheckAftForThisGesture || this.mFirstAftTriggered || this.mIsInTaskLockMode) {
            return false;
        }
        return super.shouldCheckAbnormalTouch();
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.gesture.GestureNavBaseStrategy
    public void onGestureStarted(float rawX, float rawY) {
        super.onGestureStarted(rawX, rawY);
        this.mPreConditionNotReady = false;
        this.mStartupTarget = checkStartupTarget((int) rawX, (int) rawY);
        int i = this.mStartupTarget;
        boolean z = true;
        if (i == 1 || i == 5 || i == 6) {
            this.mShouldCheckAftForThisGesture = false;
        } else {
            if ((!this.mIsInTaskLockMode || !isGesNavProxyEnable()) && !shouldCheckAftForThisGesture()) {
                z = false;
            }
            this.mShouldCheckAftForThisGesture = z;
        }
        long diffTime = 0;
        if (!this.mShouldCheckAftForThisGesture) {
            this.mFirstAftTriggered = false;
        } else if (this.mFirstAftTriggered) {
            diffTime = SystemClock.uptimeMillis() - this.mLastAftGestureTime;
            if (diffTime > 2500) {
                this.mFirstAftTriggered = false;
            }
        }
        if (GestureNavConst.DEBUG) {
            Log.i(GestureNavConst.TAG_GESTURE_BOTTOM, "StartupTarget=" + this.mStartupTarget + ", checkAft=" + this.mShouldCheckAftForThisGesture + ", firstAftTriggered=" + this.mFirstAftTriggered + ", diffTime=" + diffTime);
        }
        if (!this.mShouldCheckAftForThisGesture || this.mFirstAftTriggered || this.mIsInTaskLockMode) {
            int i2 = this.mStartupTarget;
            if (i2 == 2 || i2 == 4) {
                this.mQuickSlideOutController.onGestureStarted();
            } else if (i2 == 3) {
                this.mQuickStepController.onGestureStarted();
            }
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.gesture.GestureNavBaseStrategy
    public void onGestureReallyStarted() {
        super.onGestureReallyStarted();
        dismissDock();
        if (!this.mPreConditionNotReady) {
            if (!this.mShouldCheckAftForThisGesture || this.mFirstAftTriggered || this.mIsInTaskLockMode) {
                hideRetryToast();
                int i = this.mStartupTarget;
                if (i == 2 || i == 4) {
                    this.mQuickSlideOutController.onGestureReallyStarted();
                } else if (i == 3) {
                    this.mQuickStepController.onGestureReallyStarted();
                }
            } else if (isGesNavProxyEnable() || this.mStartupTarget != 3) {
                showReTryToast();
            }
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.gesture.GestureNavBaseStrategy
    public void onGestureSlowProcessStarted(ArrayList<Float> pendingMoveDistance) {
        super.onGestureSlowProcessStarted(pendingMoveDistance);
        if (!this.mPreConditionNotReady) {
            if (!this.mShouldCheckAftForThisGesture || this.mFirstAftTriggered || this.mIsInTaskLockMode) {
                int i = this.mStartupTarget;
                if (i == 2 || i == 4) {
                    this.mQuickSlideOutController.onGestureSlowProcessStarted();
                } else if (i == 3) {
                    this.mQuickStepController.onGestureSlowProcessStarted();
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.gesture.GestureNavBaseStrategy
    public void onGestureSlowProcess(float distance, float offsetX, float offsetY) {
        super.onGestureSlowProcess(distance, offsetX, offsetY);
        if (this.mShouldCheckAftForThisGesture && !this.mFirstAftTriggered && this.mIsInTaskLockMode) {
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.gesture.GestureNavBaseStrategy
    public void onGestureFailed(int reason, int action) {
        super.onGestureFailed(reason, action);
        if (!this.mPreConditionNotReady) {
            if (!this.mShouldCheckAftForThisGesture || this.mFirstAftTriggered || this.mIsInTaskLockMode) {
                int i = this.mStartupTarget;
                if (i == 2 || i == 4) {
                    this.mQuickSlideOutController.onGestureFailed(reason, action);
                } else if (i == 3) {
                    this.mQuickStepController.onGestureFailed(reason, action);
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.gesture.GestureNavBaseStrategy
    public void onGestureSuccessFinished(float distance, long durationTime, float velocity, boolean isFastSlideGesture, boolean isDockGesture) {
        super.onGestureSuccessFinished(distance, durationTime, velocity, isFastSlideGesture, isDockGesture);
        if (!this.mPreConditionNotReady) {
            if (!this.mShouldCheckAftForThisGesture || this.mFirstAftTriggered || this.mIsInTaskLockMode) {
                int i = this.mStartupTarget;
                if (i == 2 || i == 4) {
                    this.mQuickSlideOutController.onGestureSuccessFinished(distance, durationTime, velocity, isFastSlideGesture, null);
                } else if (i == 3) {
                    this.mQuickStepController.onGestureSuccessFinished(distance, durationTime, velocity, isFastSlideGesture, this.mGoHomeRunnable);
                } else if (i == 6) {
                    this.mQuickSlideOutController.dismissCtrlCenter(true);
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.gesture.GestureNavBaseStrategy
    public void onGestureEnd(int action) {
        super.onGestureEnd(action);
    }

    @Override // com.android.server.gesture.GestureNavBaseStrategy
    public boolean onTouchEvent(MotionEvent event, boolean isFromSubScreenView) {
        boolean result = super.onTouchEvent(event, isFromSubScreenView);
        boolean isFirstAftChecking = this.mShouldCheckAftForThisGesture && !this.mFirstAftTriggered;
        if (isFirstAftChecking) {
            if (this.mIsGestureEnd && !this.mIsGestureFailed) {
                this.mFirstAftTriggered = true;
                this.mLastAftGestureTime = SystemClock.uptimeMillis();
                if (GestureNavConst.DEBUG) {
                    Log.i(GestureNavConst.TAG_GESTURE_BOTTOM, "gesture end, mLastAftGestureTime=" + this.mLastAftGestureTime);
                }
            }
            if (!this.mIsInTaskLockMode) {
                return result;
            }
        }
        handleTouchEvent(event);
        if (this.mIsGestureNavTipsEnabled) {
            transactGestureNavEvent(event);
        }
        if (!isFirstAftChecking && this.mIsGestureEnd && !this.mIsGestureFailed && this.mFirstAftTriggered) {
            this.mFirstAftTriggered = false;
            if (this.mIsInTaskLockMode) {
                exitLockTaskMode();
            }
        }
        return result;
    }

    private void handleTouchEvent(MotionEvent event) {
        int i = this.mStartupTarget;
        if (i != 1) {
            if (i != 2) {
                if (i == 3) {
                    handleQuickStep(event);
                    return;
                } else if (i != 4) {
                    if (i != 5) {
                        return;
                    }
                }
            }
            handleQuickSlideOut(event);
            return;
        }
        handleSingleHand(event);
    }

    private void dismissDock() {
        synchronized (TIMER_LOCK) {
            if (sDockService != null) {
                Log.d(GestureNavConst.TAG_GESTURE_BOTTOM, "dismissDock");
                if (sDockService.asBinder().isBinderAlive() && sDockService.asBinder().pingBinder()) {
                    try {
                        sDockService.dismiss();
                    } catch (RemoteException e) {
                        if (GestureNavConst.DEBUG) {
                            Log.d(GestureNavConst.TAG_GESTURE_BOTTOM, "Dock dismiss failed");
                        }
                    }
                }
            } else if (GestureNavConst.DEBUG) {
                StringBuilder sb = new StringBuilder();
                sb.append("sDockService != null:");
                sb.append(sDockService != null);
                Log.d(GestureNavConst.TAG_GESTURE_BOTTOM, sb.toString());
            }
        }
    }

    private void handleSingleHand(MotionEvent event) {
        if (!this.mPreConditionNotReady) {
            int actionMasked = event.getActionMasked();
            if (actionMasked == 0) {
                setUseProxyAngleStrategy(true);
                if (this.mQuickSingleHandController.isPreConditionNotReady(false)) {
                    if (GestureNavConst.DEBUG) {
                        Log.i(GestureNavConst.TAG_GESTURE_BOTTOM, "QuickSingleHand not ready at down");
                    }
                    this.mPreConditionNotReady = true;
                    return;
                }
            } else if (actionMasked == 1 || actionMasked == 3) {
                setUseProxyAngleStrategy(false);
                this.mQuickSingleHandController.setGestureResultAtUp(!this.mIsGestureFailed, this.mGestureFailedReason);
            }
            this.mQuickSingleHandController.handleTouchEvent(event);
            if (this.mQuickSingleHandController.isBeginFailedAsExceedDegree()) {
                Log.i(GestureNavConst.TAG_GESTURE_BOTTOM, "start transfer target to slide out");
                if (transferTargetToSlideOut()) {
                    this.mQuickSingleHandController.interrupt();
                }
            }
        }
    }

    private void handleQuickSlideOut(MotionEvent event) {
        if (!this.mPreConditionNotReady) {
            int actionMasked = event.getActionMasked();
            if (actionMasked == 0) {
                boolean isOnLeft = this.mStartupTarget == 2;
                if (this.mQuickSlideOutController.isPreConditionNotReady(isOnLeft)) {
                    if (GestureNavConst.DEBUG) {
                        StringBuilder sb = new StringBuilder();
                        sb.append("QuickSlideOut not ready at ");
                        sb.append(isOnLeft ? "left" : "right");
                        sb.append(" down");
                        Log.i(GestureNavConst.TAG_GESTURE_BOTTOM, sb.toString());
                    }
                    this.mPreConditionNotReady = true;
                    return;
                }
                this.mQuickSlideOutController.setSlidingSide(isOnLeft);
            } else if (actionMasked == 1 || actionMasked == 3) {
                this.mQuickSlideOutController.setGestureResultAtUp(true ^ this.mIsGestureFailed, this.mGestureFailedReason);
            }
            this.mQuickSlideOutController.handleTouchEvent(event);
        }
    }

    private void handleQuickStep(MotionEvent event) {
        showOrHideInsetSurface(event);
        this.mQuickStepController.handleTouchEvent(event);
    }

    private boolean transferTargetToSlideOut() {
        int toTarget;
        int fromTarget = this.mStartupTarget;
        if (fromTarget == 1) {
            toTarget = 2;
        } else if (fromTarget != 5) {
            return false;
        } else {
            toTarget = 4;
        }
        boolean isOnLeft = toTarget == 2;
        if (!isSlideOutEnableAndAvailable(isOnLeft) || this.mQuickSlideOutController.isPreConditionNotReady(isOnLeft)) {
            return false;
        }
        if (isSlideTargetShowing()) {
            this.mStartupTarget = 6;
            return true;
        }
        this.mPreConditionNotReady = false;
        this.mStartupTarget = toTarget;
        this.mQuickSlideOutController.onGestureStarted();
        this.mQuickSlideOutController.setSlidingSide(isOnLeft);
        this.mQuickSlideOutController.resetState((float) (getRegion().height() - 1));
        this.mQuickSlideOutController.onGestureReallyStarted();
        if (isSlowProcessStarted()) {
            this.mQuickSlideOutController.onGestureSlowProcessStarted();
        }
        return true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void hideCurrentInputMethod() {
        InputMethodManagerInternalEx.hideCurrentInputMethod();
    }

    private void showOrHideInsetSurface(MotionEvent event) {
        if (this.mWindowManagerInternal == null) {
            this.mWindowManagerInternal = new WindowManagerInternalEx();
        }
        WindowManagerInternalEx windowManagerInternalEx = this.mWindowManagerInternal;
        if (windowManagerInternalEx != null) {
            windowManagerInternalEx.showOrHideInsetSurface(event);
        }
    }

    private boolean shouldCheckAftForThisGesture() {
        if (isGesNavProxyEnable() || !GestureNavConst.IS_SUPPORT_GAME_ASSIST) {
            return HwGestureNavWhiteConfig.getInstance().isEnable();
        }
        return GestureUtils.isGameAppForeground() && this.mIsLandscape;
    }

    private void showReTryToast() {
        if (GestureNavConst.DEBUG) {
            Log.d(GestureNavConst.TAG_GESTURE_BOTTOM, "showReTryToast");
        }
        this.mBottomHandler.post(new Runnable() {
            /* class com.android.server.gesture.GestureNavBottomStrategy.AnonymousClass2 */

            @Override // java.lang.Runnable
            public void run() {
                if (GestureNavBottomStrategy.this.mRetryToast != null) {
                    GestureNavBottomStrategy.this.mRetryToast.cancel();
                    GestureNavBottomStrategy.this.mRetryToast = null;
                }
                Toast toast = Toast.makeText(new ContextThemeWrapper(GestureNavBottomStrategy.this.mContext, 33947656), HwPartResourceUtils.getResourceId("toast_gesture_retry"), 0);
                WindowManager.LayoutParams params = ToastEx.getWindowParams(toast);
                params.type = 2010;
                new WindowManagerEx.LayoutParamsEx(params).addPrivateFlags(WindowManagerEx.LayoutParamsEx.getPrivateFlagShowForAllUsers());
                toast.show();
                GestureNavBottomStrategy.this.mRetryToast = toast;
            }
        });
    }

    private void hideRetryToast() {
        if (this.mRetryToast != null && this.mIsLandscape) {
            this.mBottomHandler.postDelayed(new Runnable() {
                /* class com.android.server.gesture.GestureNavBottomStrategy.AnonymousClass3 */

                @Override // java.lang.Runnable
                public void run() {
                    if (GestureNavBottomStrategy.this.mRetryToast != null) {
                        GestureNavBottomStrategy.this.mRetryToast.cancel();
                        GestureNavBottomStrategy.this.mRetryToast = null;
                    }
                }
            }, HIDE_RETRY_TOAST_DELAY);
        }
    }

    private void exitLockTaskMode() {
        Log.i(GestureNavConst.TAG_GESTURE_BOTTOM, "start exit lock task mode");
        this.mBottomHandler.post(new Runnable() {
            /* class com.android.server.gesture.GestureNavBottomStrategy.AnonymousClass4 */

            @Override // java.lang.Runnable
            public void run() {
                GestureUtils.exitLockTaskMode();
            }
        });
    }

    private StatusBarManagerExt getHwStatusBarService() {
        StatusBarManagerExt statusBarManagerExt;
        synchronized (this.mServiceAquireLock) {
            if (this.mStatusBarService == null) {
                this.mStatusBarService = new StatusBarManagerExt();
            }
            statusBarManagerExt = this.mStatusBarService;
        }
        return statusBarManagerExt;
    }

    private void transactGestureNavEvent(MotionEvent event) {
        Parcel data = Parcel.obtain();
        try {
            IBinder statusBarServiceBinder = getHwStatusBarService().asBinder();
            if (statusBarServiceBinder != null) {
                data.writeInterfaceToken("com.android.internal.statusbar.IStatusBarService");
                data.writeParcelable(event, 0);
                statusBarServiceBinder.transact(GESTURE_NAV_EVENT_TRANSACTION_CODE, data, null, 0);
            }
        } catch (RemoteException e) {
            Log.e(GestureNavConst.TAG_GESTURE_BOTTOM, "exception occured" + e.getMessage());
        } catch (Throwable th) {
            data.recycle();
            throw th;
        }
        data.recycle();
    }

    private int fitPoint(int rawX) {
        if (!HwFoldScreenManagerEx.isFoldable() || HwFoldScreenManagerEx.getDisplayMode() != 4) {
            return rawX;
        }
        return rawX - (HwPartCommInterfaceWraper.getFoldScreenFullWidth() - HwPartCommInterfaceWraper.getFoldScreenMainWidth());
    }

    public void dump(String prefix, PrintWriter pw, String[] args) {
        pw.print(prefix);
        pw.print("mShouldCheckAft=" + this.mShouldCheckAftForThisGesture);
        pw.print(" mFirstAftTriggered=" + this.mFirstAftTriggered);
        pw.print(" mLastAftGestureTime=" + this.mLastAftGestureTime);
        pw.print(" mIsInTaskLockMode=" + this.mIsInTaskLockMode);
        pw.println();
        QuickSlideOutController quickSlideOutController = this.mQuickSlideOutController;
        if (quickSlideOutController != null) {
            quickSlideOutController.dump(prefix, pw, args);
        }
        QuickStepController quickStepController = this.mQuickStepController;
        if (quickStepController != null) {
            quickStepController.dump(prefix, pw, args);
        }
    }
}
