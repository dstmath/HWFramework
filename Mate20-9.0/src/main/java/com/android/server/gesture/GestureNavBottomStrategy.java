package com.android.server.gesture;

import android.content.Context;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.util.Log;
import android.view.MotionEvent;
import android.view.inputmethod.InputMethodManagerInternal;
import android.widget.Toast;
import com.android.internal.statusbar.IStatusBarService;
import com.android.server.LocalServices;
import com.android.server.hidata.arbitration.HwArbitrationDEFS;
import com.android.server.wm.HwGestureNavWhiteConfig;
import java.io.PrintWriter;
import java.util.ArrayList;

public class GestureNavBottomStrategy extends GestureNavBaseStrategy {
    private static final int GESTURE_NAV_EVENT_TRANSACTION_CODE = 123;
    private static final int MSG_HIDE_INPUTMETHOD_IF_NEED = 1;
    private static final int STARTUP_TARGET_HIVISION = 4;
    private static final int STARTUP_TARGET_NONE = 0;
    private static final int STARTUP_TARGET_QUICK_STEP = 3;
    private static final int STARTUP_TARGET_SINGLE_HAND = 1;
    private static final int STARTUP_TARGET_VOICE_ASSIST = 2;
    private Handler mBottomHandler;
    private boolean mFirstAftTriggered;
    private boolean mGestureNavTipsEnabled;
    private final Runnable mGoHomeRunnable = new Runnable() {
        public void run() {
            GestureNavBottomStrategy.this.sendKeyEvent(3);
        }
    };
    private boolean mInTaskLockMode;
    private InputMethodManagerInternal mInputMethodManagerInternal;
    private boolean mKeyguardShowing;
    private boolean mLandscape;
    private long mLastAftGestureTime;
    private boolean mPreConditionNotReady;
    private QuickSingleHandController mQuickSingleHandController;
    private QuickSlideOutController mQuickSlideOutController;
    private QuickStepController mQuickStepController;
    private final Object mServiceAquireLock = new Object();
    private boolean mShouldCheckAftForThisGesture;
    private int mStartupTarget = 0;
    private IStatusBarService mStatusBarService;

    private final class BottomHandler extends Handler {
        public BottomHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                GestureNavBottomStrategy.this.hideCurrentInputMethod();
            }
        }
    }

    public GestureNavBottomStrategy(int navId, Context context, Looper looper) {
        super(navId, context, looper);
        this.mBottomHandler = new BottomHandler(looper);
    }

    private void notifyStart() {
        this.mQuickStepController = new QuickStepController(this.mContext, this.mLooper);
        this.mQuickSingleHandController = new QuickSingleHandController(this.mContext, this.mLooper);
        this.mQuickSlideOutController = new QuickSlideOutController(this.mContext, this.mLooper);
    }

    private void notifyStop() {
        this.mQuickStepController = null;
        this.mQuickSingleHandController = null;
        this.mQuickSlideOutController = null;
    }

    private boolean isSidleOutEnabled() {
        return this.mQuickSlideOutController != null && this.mQuickSlideOutController.isSlideOutEnabled();
    }

    private int checkStartupTarget(int rawX, int rawY) {
        if (this.mKeyguardShowing) {
            return 3;
        }
        int width = getRegion().width();
        int singleHandWidth = (int) (GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO * ((float) getRegion().height()));
        float sideRatio = (1.0f - (this.mLandscape ? 0.75f : 0.6f)) / 2.0f;
        if (GestureNavConst.DEBUG) {
            Log.d(GestureNavConst.TAG_GESTURE_BOTTOM, "checkStartupTarget width=" + width + ", rawX=" + rawX + ", rawY=" + rawY + ", singleHandWidth=" + singleHandWidth);
        }
        if (!this.mLandscape && (rawX <= singleHandWidth || rawX >= width - singleHandWidth)) {
            return 1;
        }
        if (!isSidleOutEnabled() || this.mInTaskLockMode) {
            return 3;
        }
        if (((float) rawX) < ((float) width) * sideRatio) {
            return 2;
        }
        if (((float) rawX) >= ((float) width) * sideRatio && ((float) rawX) <= ((float) width) - (((float) width) * sideRatio)) {
            return 3;
        }
        if (((float) rawX) > ((float) width) - (((float) width) * sideRatio)) {
            return 4;
        }
        return 0;
    }

    public void updateKeyguardState(boolean keyguardShowing) {
        super.updateKeyguardState(keyguardShowing);
        this.mKeyguardShowing = keyguardShowing;
        if (this.mQuickStepController != null) {
            this.mQuickStepController.updateKeyguardState(keyguardShowing);
        }
    }

    public void updateScreenConfigState(boolean isLand) {
        super.updateScreenConfigState(isLand);
        this.mLandscape = isLand;
    }

    public void updateNavTipsState(boolean tipsEnable) {
        super.updateNavTipsState(tipsEnable);
        this.mGestureNavTipsEnabled = tipsEnable;
    }

    public void onNavCreate(GestureNavView navView) {
        notifyStart();
        this.mQuickStepController.onNavCreate(navView);
        this.mQuickSlideOutController.onNavCreate(navView);
        this.mQuickSingleHandController.onNavCreate(navView);
    }

    public void onNavUpdate() {
        this.mQuickStepController.onNavUpdate();
        this.mQuickSlideOutController.onNavUpdate();
        this.mQuickSingleHandController.onNavUpdate();
    }

    public void onNavDestroy() {
        this.mQuickStepController.onNavDestroy();
        this.mQuickSlideOutController.onNavDestroy();
        this.mQuickSingleHandController.onNavDestroy();
        notifyStop();
    }

    /* access modifiers changed from: protected */
    public int moveOutAngleThreshold() {
        if (this.mStartupTarget == 2 || this.mStartupTarget == 4) {
            return 45;
        }
        return super.moveOutAngleThreshold();
    }

    /* access modifiers changed from: protected */
    public void onGestureStarted(float rawX, float rawY) {
        super.onGestureStarted(rawX, rawY);
        this.mInTaskLockMode = GestureUtils.isInLockTaskMode();
        this.mPreConditionNotReady = false;
        this.mStartupTarget = checkStartupTarget((int) rawX, (int) rawY);
        boolean z = true;
        if (this.mStartupTarget == 1) {
            this.mShouldCheckAftForThisGesture = false;
        } else {
            if (!this.mInTaskLockMode && !shouldCheckAftForThisGesture()) {
                z = false;
            }
            this.mShouldCheckAftForThisGesture = z;
        }
        long diffTime = 0;
        if (!this.mShouldCheckAftForThisGesture) {
            this.mFirstAftTriggered = false;
        } else if (this.mFirstAftTriggered) {
            long uptimeMillis = SystemClock.uptimeMillis() - this.mLastAftGestureTime;
            diffTime = uptimeMillis;
            if (uptimeMillis > 2500) {
                this.mFirstAftTriggered = false;
            }
        }
        if (GestureNavConst.DEBUG) {
            Log.i(GestureNavConst.TAG_GESTURE_BOTTOM, "StartupTarget=" + this.mStartupTarget + ", checkAft=" + this.mShouldCheckAftForThisGesture + ", firstAftTriggered=" + this.mFirstAftTriggered + ", diffTime=" + diffTime);
        }
        if (!this.mShouldCheckAftForThisGesture || this.mFirstAftTriggered || this.mInTaskLockMode) {
            if (this.mStartupTarget == 2 || this.mStartupTarget == 4) {
                this.mQuickSlideOutController.onGestureStarted();
            } else if (this.mStartupTarget == 3) {
                this.mQuickStepController.onGestureStarted();
            }
        }
    }

    /* access modifiers changed from: protected */
    public void onGestureReallyStarted() {
        super.onGestureReallyStarted();
        if (!this.mShouldCheckAftForThisGesture || this.mFirstAftTriggered || this.mInTaskLockMode) {
            this.mBottomHandler.sendEmptyMessage(1);
            if (this.mStartupTarget == 2 || this.mStartupTarget == 4) {
                this.mQuickSlideOutController.onGestureReallyStarted();
            } else if (this.mStartupTarget == 3) {
                this.mQuickStepController.onGestureReallyStarted();
            }
            return;
        }
        showReTryToast();
    }

    /* access modifiers changed from: protected */
    public void onGestureSlowProcessStarted(ArrayList<Float> pendingMoveDistance) {
        super.onGestureSlowProcessStarted(pendingMoveDistance);
        if (!this.mShouldCheckAftForThisGesture || this.mFirstAftTriggered || this.mInTaskLockMode) {
            if (this.mStartupTarget == 2 || this.mStartupTarget == 4) {
                this.mQuickSlideOutController.onGestureSlowProcessStarted();
            } else if (this.mStartupTarget == 3) {
                this.mQuickStepController.onGestureSlowProcessStarted();
            }
        }
    }

    /* access modifiers changed from: protected */
    public void onGestureSlowProcess(float distance, float offsetX, float offsetY) {
        super.onGestureSlowProcess(distance, offsetX, offsetY);
        if (this.mShouldCheckAftForThisGesture && !this.mFirstAftTriggered && this.mInTaskLockMode) {
        }
    }

    /* access modifiers changed from: protected */
    public void onGestureFailed(int reason, int action) {
        super.onGestureFailed(reason, action);
        if (!this.mShouldCheckAftForThisGesture || this.mFirstAftTriggered || this.mInTaskLockMode) {
            if (this.mStartupTarget == 2 || this.mStartupTarget == 4) {
                this.mQuickSlideOutController.onGestureFailed(reason, action);
            } else if (this.mStartupTarget == 3) {
                this.mQuickStepController.onGestureFailed(reason, action);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void onGestureSuccessFinished(float distance, long durationTime, float velocity, boolean isFastSlideGesture) {
        super.onGestureSuccessFinished(distance, durationTime, velocity, isFastSlideGesture);
        if (!this.mShouldCheckAftForThisGesture || this.mFirstAftTriggered || this.mInTaskLockMode) {
            if (this.mStartupTarget == 2 || this.mStartupTarget == 4) {
                this.mQuickSlideOutController.onGestureSuccessFinished(distance, durationTime, velocity, isFastSlideGesture, null);
            } else if (this.mStartupTarget == 3) {
                this.mQuickStepController.onGestureSuccessFinished(distance, durationTime, velocity, isFastSlideGesture, this.mGoHomeRunnable);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void onGestureEnd(int action) {
        super.onGestureEnd(action);
    }

    public boolean onTouchEvent(MotionEvent event) {
        boolean result = super.onTouchEvent(event);
        boolean firstAftChecking = this.mShouldCheckAftForThisGesture && !this.mFirstAftTriggered;
        if (firstAftChecking) {
            if (this.mGestureEnd && !this.mGestureFailed) {
                this.mFirstAftTriggered = true;
                this.mLastAftGestureTime = SystemClock.uptimeMillis();
                if (GestureNavConst.DEBUG) {
                    Log.i(GestureNavConst.TAG_GESTURE_BOTTOM, "gesture end, mLastAftGestureTime=" + this.mLastAftGestureTime);
                }
            }
            if (!this.mInTaskLockMode) {
                return result;
            }
        }
        handleTouchEvent(event);
        if (this.mGestureNavTipsEnabled) {
            transactGestureNavEvent(event);
        }
        if (!firstAftChecking && this.mGestureEnd && !this.mGestureFailed && this.mFirstAftTriggered) {
            this.mFirstAftTriggered = false;
            if (this.mInTaskLockMode) {
                exitLockTaskMode();
            }
        }
        return result;
    }

    private void handleTouchEvent(MotionEvent event) {
        switch (this.mStartupTarget) {
            case 1:
                handleSingleHand(event);
                return;
            case 2:
            case 4:
                handleQuickSlideOut(event);
                return;
            case 3:
                handleQuickStep(event);
                return;
            default:
                return;
        }
    }

    private void handleSingleHand(MotionEvent event) {
        if (!this.mPreConditionNotReady) {
            int actionMasked = event.getActionMasked();
            if (actionMasked != 3) {
                switch (actionMasked) {
                    case 0:
                        setUseProxyAngleStrategy(true);
                        if (this.mQuickSingleHandController.isPreConditionNotReady(false)) {
                            if (GestureNavConst.DEBUG) {
                                Log.i(GestureNavConst.TAG_GESTURE_BOTTOM, "QuickSingleHand not ready at down");
                            }
                            this.mPreConditionNotReady = true;
                            return;
                        }
                        break;
                    case 1:
                        break;
                }
            }
            setUseProxyAngleStrategy(false);
            this.mQuickSingleHandController.setGestureResultAtUp(!this.mGestureFailed, this.mGestureFailedReason);
            this.mQuickSingleHandController.handleTouchEvent(event);
        }
    }

    private void handleQuickSlideOut(MotionEvent event) {
        if (!this.mPreConditionNotReady) {
            int actionMasked = event.getActionMasked();
            if (actionMasked != 3) {
                switch (actionMasked) {
                    case 0:
                        boolean onLeft = this.mStartupTarget == 2;
                        if (!this.mQuickSlideOutController.isPreConditionNotReady(onLeft)) {
                            this.mQuickSlideOutController.setSlidingSide(onLeft);
                            break;
                        } else {
                            if (GestureNavConst.DEBUG) {
                                StringBuilder sb = new StringBuilder();
                                sb.append("QuickSlideOut not ready at ");
                                sb.append(onLeft ? "left" : "right");
                                sb.append(" down");
                                Log.i(GestureNavConst.TAG_GESTURE_BOTTOM, sb.toString());
                            }
                            this.mPreConditionNotReady = true;
                            return;
                        }
                    case 1:
                        break;
                }
            }
            this.mQuickSlideOutController.setGestureResultAtUp(!this.mGestureFailed, this.mGestureFailedReason);
            this.mQuickSlideOutController.handleTouchEvent(event);
        }
    }

    private void handleQuickStep(MotionEvent event) {
        this.mQuickStepController.handleTouchEvent(event);
    }

    /* access modifiers changed from: private */
    public void hideCurrentInputMethod() {
        if (this.mInputMethodManagerInternal == null) {
            this.mInputMethodManagerInternal = (InputMethodManagerInternal) LocalServices.getService(InputMethodManagerInternal.class);
        }
        if (this.mInputMethodManagerInternal != null) {
            if (GestureNavConst.DEBUG) {
                Log.d(GestureNavConst.TAG_GESTURE_BOTTOM, "hide input method if need");
            }
            this.mInputMethodManagerInternal.hideCurrentInputMethod();
        }
    }

    private boolean shouldCheckAftForThisGesture() {
        return HwGestureNavWhiteConfig.getInstance().isEnable();
    }

    private void showReTryToast() {
        if (GestureNavConst.DEBUG) {
            Log.d(GestureNavConst.TAG_GESTURE_BOTTOM, "showReTryToast");
        }
        this.mBottomHandler.post(new Runnable() {
            public void run() {
                Toast toast = Toast.makeText(GestureNavBottomStrategy.this.mContext, 33686241, 0);
                toast.getWindowParams().type = HwArbitrationDEFS.MSG_MPLINK_UNBIND_FAIL;
                toast.getWindowParams().privateFlags |= 16;
                toast.show();
            }
        });
    }

    private void exitLockTaskMode() {
        Log.i(GestureNavConst.TAG_GESTURE_BOTTOM, "start exit lock task mode");
        this.mBottomHandler.post(new Runnable() {
            public void run() {
                GestureUtils.exitLockTaskMode();
            }
        });
    }

    private IStatusBarService getHWStatusBarService() {
        IStatusBarService iStatusBarService;
        synchronized (this.mServiceAquireLock) {
            if (this.mStatusBarService == null) {
                this.mStatusBarService = IStatusBarService.Stub.asInterface(ServiceManager.getService("statusbar"));
            }
            iStatusBarService = this.mStatusBarService;
        }
        return iStatusBarService;
    }

    private void transactGestureNavEvent(MotionEvent event) {
        Parcel data = Parcel.obtain();
        try {
            IBinder statusBarServiceBinder = getHWStatusBarService().asBinder();
            if (statusBarServiceBinder != null) {
                data.writeInterfaceToken("com.android.internal.statusbar.IStatusBarService");
                data.writeParcelable(event, 0);
                statusBarServiceBinder.transact(123, data, null, 0);
            }
        } catch (RemoteException e) {
            Log.e(GestureNavConst.TAG_GESTURE_BOTTOM, "exception occured", e);
        } catch (Throwable th) {
            data.recycle();
            throw th;
        }
        data.recycle();
    }

    public void dump(String prefix, PrintWriter pw, String[] args) {
        pw.print(prefix);
        pw.print("mShouldCheckAftForThisGesture=" + this.mShouldCheckAftForThisGesture);
        pw.print(" mFirstAftTriggered=" + this.mFirstAftTriggered);
        pw.print(" mLastAftGestureTime=" + this.mLastAftGestureTime);
        pw.println();
        if (this.mQuickSlideOutController != null) {
            this.mQuickSlideOutController.dump(prefix, pw, args);
        }
        if (this.mQuickStepController != null) {
            this.mQuickStepController.dump(prefix, pw, args);
        }
    }
}
