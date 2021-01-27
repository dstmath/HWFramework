package com.android.server.gesture;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Rect;
import android.hardware.display.HwFoldScreenState;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.provider.Settings;
import android.util.Flog;
import android.util.HwMwUtils;
import android.util.Log;
import com.android.server.gesture.GestureNavView;
import com.android.server.policy.WindowManagerPolicyEx;
import com.android.server.wm.WindowManagerInternalEx;
import com.android.server.wm.WindowStateEx;
import com.huawei.android.app.ActivityManagerExt;
import com.huawei.android.app.ActivityTaskManagerExt;
import com.huawei.android.app.HwActivityTaskManager;
import com.huawei.android.app.WindowConfigurationEx;
import com.huawei.android.content.ContextEx;
import com.huawei.android.fsm.HwFoldScreenManagerInternal;
import com.huawei.android.hardware.display.DisplayManagerGlobalEx;
import com.huawei.android.os.UserHandleEx;
import com.huawei.android.server.LocalServicesExt;
import com.huawei.android.util.MathUtilsEx;
import com.huawei.android.view.DisplayEx;
import com.huawei.android.view.DisplayInfoEx;
import com.huawei.hwdockbar.IDockAidlInterface;
import com.huawei.hwpartbasicplatformservices.BuildConfig;
import huawei.com.android.server.policy.HwFalseTouchMonitor;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Timer;
import java.util.TimerTask;

public class GestureNavBackStrategy extends GestureNavBaseStrategy {
    private static final int BINDER_FLAG = 0;
    private static final int DOCK_CONNECT = 2;
    private static final int DOCK_DISMISS = 1;
    private static final int DOCK_EDIT = 0;
    private static final String DOCK_EDIT_RESULT = "dock_edit_result";
    private static final int MSG_CHECK_HAPTICS_VIBRATOR = 1;
    private static final int SERVICE_UNBIND_TIME = 30;
    private static final String TAG = "GestureNavBackStrategy";
    private ServiceConnection conn = new ServiceConnection() {
        /* class com.android.server.gesture.GestureNavBackStrategy.AnonymousClass1 */

        @Override // android.content.ServiceConnection
        public void onServiceConnected(ComponentName componentName, IBinder binder) {
            Log.i(GestureNavBackStrategy.TAG, "onServiceConnected");
            synchronized (GestureNavBaseStrategy.TIMER_LOCK) {
                GestureNavBaseStrategy.sDockService = IDockAidlInterface.Stub.asInterface(binder);
                GestureNavBackStrategy.this.mIsDockServiceConnected = true;
                if (GestureNavBaseStrategy.sDockService != null) {
                    try {
                        GestureNavBaseStrategy.sDockService.asBinder().linkToDeath(GestureNavBackStrategy.this.mDeathRecipient, 0);
                        GestureNavBaseStrategy.sDockService.connect(GestureNavBackStrategy.this.mNavId);
                    } catch (RemoteException e) {
                        if (GestureNavConst.DEBUG) {
                            Log.d(GestureNavBackStrategy.TAG, "onServiceConnected failed");
                        }
                    }
                }
            }
        }

        @Override // android.content.ServiceConnection
        public void onServiceDisconnected(ComponentName componentName) {
        }
    };
    private float mAnimStartPosition;
    private Handler mBackHandler;
    private int mBackMaxDistance1;
    private int mBackMaxDistance2;
    private IBinder.DeathRecipient mDeathRecipient = new IBinder.DeathRecipient() {
        /* class com.android.server.gesture.GestureNavBackStrategy.AnonymousClass2 */

        @Override // android.os.IBinder.DeathRecipient
        public void binderDied() {
            GestureNavBackStrategy.this.rmvDockDeathRecipient();
        }
    };
    private int mDockServiceUnbindTime = SERVICE_UNBIND_TIME;
    private GestureDataTracker mGestureDataTracker;
    private GestureNavView.IGestureNavBackAnim mGestureNavBackAnim;
    private boolean mIsAnimPositionSetup;
    private boolean mIsAnimProcessedOnce;
    private boolean mIsAnimProcessing;
    private boolean mIsDisLargeEnough = false;
    private boolean mIsDockServiceConnected = false;
    private boolean mIsGestureNavEnable = false;
    private boolean mIsInHomeOfLauncherTmp = false;
    private boolean mIsShouldTurnOffAnim = false;
    private boolean mIsShowDockEnable = false;
    private TimerTask mUnbindTask = null;
    private Timer mUnbindTimer = null;
    private WindowManagerInternalEx mWindowManagerInternal;

    public GestureNavBackStrategy(int navId, Context context, Looper looper, GestureNavView.IGestureNavBackAnim backAnim) {
        super(navId, context, looper);
        this.mGestureNavBackAnim = backAnim;
        this.mBackHandler = new BackHandler(looper);
        this.mGestureDataTracker = GestureDataTracker.getInstance(context);
    }

    @Override // com.android.server.gesture.GestureNavBaseStrategy
    public void updateConfig(int displayWidth, int displayHeight, Rect r, int rotation) {
        super.updateConfig(displayWidth, displayHeight, r, rotation);
        this.mBackMaxDistance1 = GestureNavConst.getBackMaxDistanceOne(this.mContext);
        this.mBackMaxDistance2 = GestureNavConst.getBackMaxDistanceTwo(this.mContext);
        if (GestureNavConst.DEBUG_ALL) {
            Log.d(GestureNavConst.TAG_GESTURE_BACK, "distance1:" + this.mBackMaxDistance1 + ", distance2:" + this.mBackMaxDistance2);
        }
    }

    @Override // com.android.server.gesture.GestureNavBaseStrategy
    public void onNavDestroy() {
        cancleTimeTask();
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.gesture.GestureNavBaseStrategy
    public void onGestureStarted(float rawX, float rawY) {
        super.onGestureStarted(rawX, rawY);
        this.mIsAnimPositionSetup = false;
        this.mIsAnimProcessing = false;
        this.mIsAnimProcessedOnce = false;
        this.mAnimStartPosition = rawY;
        this.mGestureNavBackAnim.setSide(this.mNavId == 1);
        this.mIsInHomeOfLauncherTmp = this.mIsInHomeOfLauncher;
        this.mIsDisLargeEnough = false;
        this.mIsGestureNavEnable = GestureNavConst.isGestureNavEnabled(this.mContext, -2);
        this.mIsShowDockEnable = GestureNavConst.isShowDockEnabled(this.mContext, -2);
        this.mIsShowDockPreCondSatisfied = isShowDockPreCondSatisfied();
        this.mGestureNavBackAnim.setDockIcon(isShouldShowDockAnimation());
        this.mIsShouldTurnOffAnim = isShouldTurnOffAnimation();
        switchFocusIfNeeded((int) rawX, (int) rawY);
        if (HwMwUtils.ENABLED && this.mIsGestureNavEnable) {
            HwMwUtils.performPolicy(137, new Object[]{Integer.valueOf((int) rawX), Integer.valueOf((int) rawY)});
        }
    }

    private boolean isShowDockPreCondSatisfied() {
        return this.mIsShowDockEnable && !GestureNavConst.isSimpleMode(this.mContext, -2) && !GestureNavConst.isInSuperSaveMode() && !GestureNavConst.isInScreenReaderMode(this.mContext, -2) && !isInLazyMode() && !isInSubOrCoorFoldDisplayMode() && !GestureNavConst.isKeyguardLocked(this.mContext) && !isNavBarOnRightSide() && !isDockInEditState() && !isMultiWindowDisabled() && !isInStartUpGuide();
    }

    private boolean isInLazyMode() {
        String defaultMode = Settings.Global.getString(this.mContext.getContentResolver(), "single_hand_mode");
        Log.i(GestureNavConst.TAG_GESTURE_BACK, "defaultMode:" + defaultMode);
        if (defaultMode == null || BuildConfig.FLAVOR.equals(defaultMode)) {
            return false;
        }
        return true;
    }

    private boolean isInSubOrCoorFoldDisplayMode() {
        HwFoldScreenManagerInternal hwFoldScreenManagerInternal;
        if (!(HwFoldScreenState.isFoldScreenDevice() && HwFoldScreenState.isOutFoldDevice()) || (hwFoldScreenManagerInternal = (HwFoldScreenManagerInternal) LocalServicesExt.getService(HwFoldScreenManagerInternal.class)) == null) {
            return false;
        }
        int displayMode = hwFoldScreenManagerInternal.getDisplayMode();
        return displayMode == 3 || displayMode == 4;
    }

    private boolean isShouldShowDockAnimation() {
        if (!this.mIsShowDockEnable) {
            return false;
        }
        if (!this.mIsGestureNavEnable) {
            return true;
        }
        if (this.mIsShowDockPreCondSatisfied) {
            return this.mIsInHomeOfLauncherTmp;
        }
        return false;
    }

    private boolean isShouldTurnOffAnimation() {
        if (!this.mIsShowDockEnable) {
            return false;
        }
        if (!this.mIsGestureNavEnable) {
            return !this.mIsShowDockPreCondSatisfied;
        }
        if (this.mIsShowDockPreCondSatisfied || !this.mIsInHomeOfLauncherTmp) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.gesture.GestureNavBaseStrategy
    public void onGestureReallyStarted() {
        super.onGestureReallyStarted();
        if (this.mIsSubScreenGestureNav) {
            this.mIsAnimPositionSetup = false;
        } else if (this.mIsShouldTurnOffAnim) {
            this.mIsAnimPositionSetup = false;
        } else if (!this.mIsAnimPositionSetup) {
            boolean z = true;
            this.mIsAnimPositionSetup = true;
            GestureNavView.IGestureNavBackAnim iGestureNavBackAnim = this.mGestureNavBackAnim;
            if (this.mNavId != 1) {
                z = false;
            }
            iGestureNavBackAnim.setAnimPosition(z, this.mAnimStartPosition);
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.gesture.GestureNavBaseStrategy
    public void onGestureSlowProcessStarted(ArrayList<Float> pendingMoveDistance) {
        int size;
        super.onGestureSlowProcessStarted(pendingMoveDistance);
        if (this.mIsSubScreenGestureNav) {
            this.mIsAnimProcessing = false;
        } else if (this.mIsShouldTurnOffAnim) {
            this.mIsAnimProcessing = false;
        } else {
            if (!this.mIsAnimProcessing) {
                this.mIsAnimProcessing = true;
            }
            if (pendingMoveDistance != null && (size = pendingMoveDistance.size()) > 0) {
                for (int i = 0; i < size; i++) {
                    notifyAnimProcess(pendingMoveDistance.get(i).floatValue());
                }
                if (GestureNavConst.DEBUG) {
                    Log.d(GestureNavConst.TAG_GESTURE_BACK, "interpolate " + size + " pending datas");
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.gesture.GestureNavBaseStrategy
    public void onGestureSlowProcess(float distance, float offsetX, float offsetY) {
        super.onGestureSlowProcess(distance, offsetX, offsetY);
        notifyAnimProcess(distance);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.gesture.GestureNavBaseStrategy
    public void onGestureAnimateScatterProcess(float fromProcess, float toProcess) {
        super.onGestureAnimateScatterProcess(fromProcess, toProcess);
        animteScatterProcess(fromProcess, toProcess);
    }

    private void animteScatterProcess(float fromProcess, float toProcess) {
        this.mGestureNavBackAnim.playScatterProcessAnim(fromProcess, toProcess);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.gesture.GestureNavBaseStrategy
    public void onGestureFailed(int reason, int action) {
        super.onGestureFailed(reason, action);
        if (this.mIsAnimPositionSetup) {
            if (GestureNavConst.DEBUG) {
                Log.i(GestureNavConst.TAG_GESTURE_BACK, "gesture failed, disappear anim");
            }
            this.mGestureNavBackAnim.playDisappearAnim();
        }
        if (isEffectiveFailedReason(reason)) {
            this.mGestureDataTracker.gestureBackEvent(this.mNavId, false);
        }
        Flog.bdReport(991310854, GestureNavConst.reportResultStr(false, this.mNavId, reason));
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.gesture.GestureNavBaseStrategy
    public void onGesturePreLoad() {
        super.onGesturePreLoad();
        Intent i = new Intent(GestureNavConst.DEFAULT_DOCK_AIDL_INTERFACE);
        i.setComponent(new ComponentName(GestureNavConst.DEFAULT_DOCK_PACKAGE, GestureNavConst.DEFAULT_DOCK_MAIN_CLASS));
        synchronized (TIMER_LOCK) {
            if (sDockService != null && sDockService.asBinder().isBinderAlive()) {
                if (sDockService.asBinder().pingBinder()) {
                    dockServiceFunction(2);
                }
            }
            if (GestureNavConst.DEBUG) {
                Log.d(TAG, "sDockService == null!, mNavId=" + this.mNavId);
            }
            ContextEx.bindServiceAsUser(this.mContext, i, this.conn, 1, UserHandleEx.CURRENT);
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.gesture.GestureNavBaseStrategy
    public void onGestureSuccessFinished(float distance, long durationTime, float velocity, boolean isFastSlideGesture, boolean isDockGesture) {
        super.onGestureSuccessFinished(distance, durationTime, velocity, isFastSlideGesture, isDockGesture);
        checkHwHapticsVibrator();
        boolean z = false;
        boolean isHomeOfLauncher = this.mIsShowDockEnable && this.mIsInHomeOfLauncherTmp;
        if (!isDockGesture && this.mIsGestureNavEnable && !isHomeOfLauncher) {
            sendKeyEvent(4);
        }
        if (!this.mIsSubScreenGestureNav && !this.mIsShouldTurnOffAnim) {
            if (this.mIsAnimProcessing && this.mIsAnimProcessedOnce) {
                if (GestureNavConst.DEBUG) {
                    Log.i(GestureNavConst.TAG_GESTURE_BACK, "gesture finished, disappear anim");
                }
                this.mGestureNavBackAnim.playDisappearAnim();
            } else if (isFastSlideGesture) {
                if (GestureNavConst.DEBUG) {
                    Log.i(GestureNavConst.TAG_GESTURE_BACK, "gesture finished, play fast anim, velocity=" + velocity);
                }
                if (!this.mIsAnimPositionSetup) {
                    this.mIsAnimPositionSetup = true;
                    GestureNavView.IGestureNavBackAnim iGestureNavBackAnim = this.mGestureNavBackAnim;
                    if (this.mNavId == 1) {
                        z = true;
                    }
                    iGestureNavBackAnim.setAnimPosition(z, this.mAnimStartPosition);
                }
                this.mGestureNavBackAnim.playFastSlidingAnim();
            } else {
                if (GestureNavConst.DEBUG) {
                    Log.i(GestureNavConst.TAG_GESTURE_BACK, "velocity does not meet the threshold, disappear anim");
                }
                this.mGestureNavBackAnim.playDisappearAnim();
            }
            this.mGestureDataTracker.gestureBackEvent(this.mNavId, true);
            Flog.bdReport(991310854, GestureNavConst.reportResultStr(true, this.mNavId, -1));
        }
    }

    private void notifyAnimProcess(float distance) {
        if (!this.mIsSubScreenGestureNav && !this.mIsShouldTurnOffAnim) {
            float process = getRubberbandProcess(distance);
            boolean isSuccess = this.mGestureNavBackAnim.setAnimProcess(process);
            if (!this.mIsAnimProcessedOnce && isSuccess) {
                this.mIsAnimProcessedOnce = true;
            }
            if (GestureNavConst.DEBUG_ALL) {
                Log.d(GestureNavConst.TAG_GESTURE_BACK, "process=" + process + ", distance=" + distance + ", animOnce=" + this.mIsAnimProcessedOnce);
            }
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.gesture.GestureNavBaseStrategy
    public void switchAnimationForDockIfNeed(boolean isDockShowing, boolean isDisLargeEnough) {
        if (GestureNavConst.DEBUG_ALL) {
            Log.d(TAG, "switchAnimationForDockIfNeed: isDockShowing=" + isDockShowing + "; mIsDisLargeEnough=" + this.mIsDisLargeEnough + "; isDisLargeEnough=" + isDisLargeEnough);
        }
        if (!this.mIsInHomeOfLauncherTmp) {
            if ((!isDockShowing || !isDisLargeEnough) && this.mIsDisLargeEnough != isDisLargeEnough) {
                if (this.mIsShowDockEnable && this.mIsGestureNavEnable) {
                    this.mGestureNavBackAnim.switchDockIcon(isDisLargeEnough);
                }
                this.mIsDisLargeEnough = isDisLargeEnough;
            }
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.gesture.GestureNavBaseStrategy
    public float getRubberbandProcess(float distance) {
        float rubber;
        if (distance < GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO) {
            return GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
        }
        int i = this.mBackMaxDistance1;
        if (distance < ((float) i)) {
            float process = (distance / ((float) i)) * 0.88f;
            if (process < 0.1f) {
                return 0.1f;
            }
            return process;
        }
        int backMaxDistanceDiff = this.mBackMaxDistance2 - i;
        if (backMaxDistanceDiff != 0) {
            rubber = (distance - ((float) i)) / ((float) backMaxDistanceDiff);
        } else {
            rubber = distance - ((float) i);
        }
        return 0.88f + (MathUtilsEx.constrain(rubber, (float) GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO, 1.0f) * 0.120000005f);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.gesture.GestureNavBaseStrategy
    public void rmvDockDeathRecipient() {
        synchronized (TIMER_LOCK) {
            StringBuilder sb = new StringBuilder();
            sb.append("rmvDockDeathRecipient sDockService = ");
            sb.append(sDockService == null);
            sb.append(", mNavId=");
            sb.append(this.mNavId);
            Log.d(TAG, sb.toString());
            if (sDockService != null) {
                try {
                    sDockService.asBinder().unlinkToDeath(this.mDeathRecipient, 0);
                } catch (NoSuchElementException e) {
                    Log.i(TAG, "rmvDockDeathRecipient: no such element., this=" + this);
                }
            }
        }
    }

    private final class BackHandler extends Handler {
        BackHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                GestureUtils.performHapticFeedbackIfNeed(GestureNavBackStrategy.this.mContext);
            }
        }
    }

    private void checkHwHapticsVibrator() {
        if (!this.mBackHandler.hasMessages(1)) {
            this.mBackHandler.sendEmptyMessage(1);
        }
    }

    private boolean isNavBarOnRightSide() {
        WindowManagerPolicyEx.WindowStateEx navBar;
        if (this.mIsGestureNavEnable || this.mNavId != 2 || (navBar = DeviceStateController.getInstance(this.mContext).getNavigationBar()) == null || navBar.getDisplayFrameLw() == null) {
            return false;
        }
        DisplayInfoEx displayInfoEx = new DisplayInfoEx();
        DisplayEx.getDisplayInfo(DisplayManagerGlobalEx.getRealDisplay(0), displayInfoEx);
        if (navBar.getDisplayFrameLw().top < displayInfoEx.getLogicalHeight() / 2) {
            return true;
        }
        return false;
    }

    private boolean isDockInEditState() {
        Bundle bundle;
        boolean isEditStatus = false;
        new Bundle();
        synchronized (TIMER_LOCK) {
            bundle = dockServiceFunction(0);
        }
        if (bundle != null && bundle.getBoolean(DOCK_EDIT_RESULT)) {
            isEditStatus = true;
        }
        if (GestureNavConst.DEBUG) {
            Log.d(TAG, "isDockInEditState: isEditStatus=" + isEditStatus);
        }
        return isEditStatus;
    }

    private boolean isImeWinContainsPoint(int posX, int posY) {
        WindowStateEx imeWin = WindowStateEx.getWindowStateEx(DeviceStateController.getInstance(this.mContext).getInputMethodWindow());
        int i = 0;
        if (imeWin == null || !imeWin.isVisibleLw() || imeWin.getBounds() == null || imeWin.getDisplayFrameLw() == null || imeWin.getContentFrameLw() == null || imeWin.getGivenContentInsetsLw() == null) {
            return false;
        }
        Rect inputRect = new Rect(imeWin.getBounds());
        int top = Math.max(imeWin.getDisplayFrameLw().top, imeWin.getContentFrameLw().top) + imeWin.getGivenContentInsetsLw().top;
        if (top != inputRect.bottom) {
            i = top;
        }
        inputRect.top = i;
        if (GestureNavConst.DEBUG) {
            Log.d(TAG, "isImeWinContainsPoint: (" + posX + ", " + posY + "); rect=" + inputRect);
        }
        return inputRect.contains(posX, posY);
    }

    private void switchFocusIfNeeded(int touchDownX, int touchDownY) {
        List<ActivityManager.RunningTaskInfo> visibleTaskInfoList;
        if (this.mIsShowDockEnable && this.mIsGestureNavEnable) {
            if (this.mWindowManagerInternal == null) {
                this.mWindowManagerInternal = new WindowManagerInternalEx();
            }
            int windowMode = this.mWindowManagerInternal.getFocusedAppWindowMode();
            if ((WindowConfigurationEx.isHwSplitScreenWindowingMode(windowMode) || windowMode == 103) && (visibleTaskInfoList = HwActivityTaskManager.getVisibleTasks()) != null && !visibleTaskInfoList.isEmpty()) {
                boolean isHasHwMultiWindow = false;
                Iterator<ActivityManager.RunningTaskInfo> it = visibleTaskInfoList.iterator();
                while (true) {
                    if (!it.hasNext()) {
                        break;
                    }
                    ActivityManager.RunningTaskInfo rti = it.next();
                    if (rti != null && WindowConfigurationEx.isHwMultiStackWindowingMode(ActivityManagerExt.getWindowMode(rti))) {
                        isHasHwMultiWindow = true;
                        break;
                    }
                }
                if (isHasHwMultiWindow) {
                    int touchDownX2 = getTouchDownOffsetX(touchDownX);
                    if (!isImeWinContainsPoint(touchDownX2, touchDownY)) {
                        for (ActivityManager.RunningTaskInfo rti2 : visibleTaskInfoList) {
                            if (rti2 != null && !WindowConfigurationEx.isHwFreeFormWindowingMode(ActivityManagerExt.getWindowMode(rti2)) && ActivityManagerExt.getBounds(rti2) != null && ActivityManagerExt.getBounds(rti2).contains(touchDownX2, touchDownY)) {
                                try {
                                    ActivityTaskManagerExt.setFocusedTask(ActivityManagerExt.getTaskId(rti2));
                                    return;
                                } catch (RemoteException remoteExp) {
                                    Log.d("TaskPositioningController", "switchFocusIfNeeded: ", remoteExp);
                                    return;
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private int getTouchDownOffsetX(int touchDownX) {
        if (!GestureUtils.isDisplayHasNotch()) {
            return touchDownX;
        }
        int statusBarHeight = GestureNavConst.getStatusBarHeight(this.mContext);
        int i = this.mRotation;
        if (i != 1) {
            if (i == 3 && this.mNavId == 2) {
                return touchDownX - statusBarHeight;
            }
            return touchDownX;
        } else if (this.mNavId == 1) {
            return touchDownX + statusBarHeight;
        } else {
            return touchDownX;
        }
    }

    private boolean isMultiWindowDisabled() {
        return HwActivityTaskManager.getMultiWindowDisabled();
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.gesture.GestureNavBaseStrategy
    public void dismissDockBar() {
        super.dismissDockBar();
        synchronized (TIMER_LOCK) {
            dockServiceFunction(1);
        }
    }

    private boolean isInStartUpGuide() {
        DeviceStateController deviceStateController = DeviceStateController.getInstance(this.mContext);
        return !deviceStateController.isDeviceProvisioned() || !deviceStateController.isCurrentUserSetup() || deviceStateController.isOOBEActivityEnabled() || deviceStateController.isSetupWizardEnabled();
    }

    private Bundle dockServiceFunction(int type) {
        Log.i(TAG, "dockServiceFunction " + type);
        Bundle out = new Bundle();
        if (sDockService == null) {
            return out;
        }
        if (type == 0) {
            boolean isEditStatus = false;
            if (sDockService.asBinder().isBinderAlive() && sDockService.asBinder().pingBinder()) {
                try {
                    isEditStatus = sDockService.isEditState();
                } catch (RemoteException e) {
                    Log.i(TAG, "dock service exception.");
                }
            }
            out.putBoolean(DOCK_EDIT_RESULT, isEditStatus);
        } else if (type != 1) {
            if (type == 2) {
                try {
                    if (GestureNavConst.DEBUG) {
                        Log.d(TAG, "sDockService.connect()!, mNavId=" + this.mNavId);
                    }
                    sDockService.connect(this.mNavId);
                } catch (RemoteException e2) {
                    if (GestureNavConst.DEBUG) {
                        Log.d(TAG, "sDockService connect failed");
                    }
                }
            }
        } else if (sDockService.asBinder().isBinderAlive() && sDockService.asBinder().pingBinder()) {
            try {
                sDockService.dismissWithAnimation();
            } catch (RemoteException e3) {
                Log.e(TAG, "Dock dismiss failed");
            }
        }
        return out;
    }

    private final class MyTimerTask extends TimerTask {
        private MyTimerTask() {
        }

        @Override // java.util.TimerTask, java.lang.Runnable
        public void run() {
            StringBuilder sb = new StringBuilder();
            sb.append("MyTimerTask mIsDockServiceConnected=");
            sb.append(GestureNavBackStrategy.this.mIsDockServiceConnected);
            sb.append(", sDockService=");
            sb.append(GestureNavBaseStrategy.sDockService != null);
            sb.append(",mNavId=");
            sb.append(GestureNavBackStrategy.this.mNavId);
            Log.d(GestureNavBackStrategy.TAG, sb.toString());
            synchronized (GestureNavBaseStrategy.TIMER_LOCK) {
                if (GestureNavBackStrategy.this.mIsDockServiceConnected) {
                    GestureNavBackStrategy.this.mIsDockServiceConnected = false;
                }
                if (GestureNavBackStrategy.this.mContext != null) {
                    GestureNavBackStrategy.this.mContext.unbindService(GestureNavBackStrategy.this.conn);
                }
                if (GestureNavBaseStrategy.sDockService != null && GestureNavBaseStrategy.sDockService.asBinder().isBinderAlive() && GestureNavBaseStrategy.sDockService.asBinder().pingBinder()) {
                    GestureNavBaseStrategy.sDockService = null;
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.gesture.GestureNavBaseStrategy
    public void focusOut(String packageName) {
        if (GestureNavConst.DEBUG) {
            StringBuilder sb = new StringBuilder();
            sb.append("focusOut mIsDockServiceConnected=");
            sb.append(this.mIsDockServiceConnected);
            sb.append(", packageName=");
            sb.append(packageName);
            sb.append(", mUnbindTimer=");
            sb.append(this.mUnbindTimer != null);
            sb.append(",mNavId=");
            sb.append(this.mNavId);
            Log.d(TAG, sb.toString());
        }
        if (this.mIsDockServiceConnected) {
            if (GestureNavConst.DEFAULT_DOCK_PACKAGE.equals(packageName)) {
                cancleTimeTask();
                return;
            }
            synchronized (TIMER_LOCK) {
                if (sDockService != null && sDockService.asBinder().isBinderAlive()) {
                    if (!sDockService.asBinder().pingBinder()) {
                    }
                }
                return;
            }
            if (this.mUnbindTimer == null) {
                this.mUnbindTimer = new Timer();
                this.mUnbindTask = new MyTimerTask();
                this.mUnbindTimer.schedule(this.mUnbindTask, (long) (this.mDockServiceUnbindTime * HwFalseTouchMonitor.NoEffectClickChecker.CLICK_INTERVAL_TIMEOUT));
            }
        }
    }

    private void cancleTimeTask() {
        Timer timer = this.mUnbindTimer;
        if (timer != null) {
            timer.cancel();
            this.mUnbindTimer = null;
        }
        TimerTask timerTask = this.mUnbindTask;
        if (timerTask != null) {
            timerTask.cancel();
            this.mUnbindTask = null;
        }
    }
}
