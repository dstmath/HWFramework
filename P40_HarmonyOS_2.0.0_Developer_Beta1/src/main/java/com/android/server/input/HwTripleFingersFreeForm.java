package com.android.server.input;

import android.app.ActivityManager;
import android.app.KeyguardManager;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.freeform.HwFreeFormUtils;
import android.hardware.display.HwFoldScreenState;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.UserManager;
import android.provider.Settings;
import android.util.Log;
import android.util.SparseArray;
import android.view.InputEvent;
import android.view.MotionEvent;
import android.widget.Toast;
import com.android.server.gesture.GestureNavConst;
import com.android.server.policy.HwPhoneWindowManager;
import com.android.server.policy.WindowManagerPolicyEx;
import com.huawei.android.app.ActivityManagerEx;
import com.huawei.android.app.ActivityManagerExt;
import com.huawei.android.app.HwActivityTaskManager;
import com.huawei.android.app.WindowConfigurationEx;
import com.huawei.android.app.WindowManagerExt;
import com.huawei.android.content.ContextEx;
import com.huawei.android.fsm.HwFoldScreenManagerInternal;
import com.huawei.android.hardware.input.InputManagerEx;
import com.huawei.android.os.HandlerAdapter;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.os.UserHandleEx;
import com.huawei.android.os.UserManagerExt;
import com.huawei.android.provider.SettingsEx;
import com.huawei.android.server.LocalServicesExt;
import com.huawei.android.view.IDockedStackListenerEx;
import com.huawei.android.view.MotionEventEx;
import com.huawei.android.view.WindowManagerEx;
import com.huawei.android.widget.ToastEx;
import com.huawei.hwpartbasicplatformservices.BuildConfig;
import com.huawei.server.statusbar.StatusBarManagerInternalEx;
import com.huawei.utils.HwPartResourceUtils;
import java.util.List;

public class HwTripleFingersFreeForm {
    private static final String ACCESSIBILITY_SCREENREADER_ENABLED = "accessibility_screenreader_enabled";
    private static final String APS_RESOLUTION_CHANGE_ACTION = "huawei.intent.action.APS_RESOLUTION_CHANGE_ACTION";
    private static final String APS_RESOLUTION_CHANGE_PERSISSIONS = "huawei.intent.permissions.APS_RESOLUTION_CHANGE_ACTION";
    private static final float DENSITY_INIT = 2.0f;
    private static final float DIP_TO_PIXEL_OFFSET = 0.5f;
    private static final boolean IS_DISABLE_MULTIWIN = SystemPropertiesEx.getBoolean("ro.huawei.disable_multiwindow", false);
    private static final boolean IS_MULTIWINDOW_OPTIMIZATION = SystemPropertiesEx.getBoolean("ro.config.hw_multiwindow_optimization", false);
    private static final float MAX_FINGER_DOWN_INTERVAL = 200.0f;
    private static final float MAX_FINGER_DOWN_X_DISTANCE = 400.0f;
    private static final float MAX_FINGER_DOWN_Y_DISTANCE = 145.0f;
    private static final float MIN_Y_TRIGGER_LANDSCAPE_DISTANCE = 90.0f;
    private static final float MIN_Y_TRIGGER_PORTRAIT_DISTANCE = 120.0f;
    public static final int MSG_HANDLE_FREEFORM_INIT = 0;
    public static final int MSG_SHOW_SPLIT_TOAST = 1;
    private static final String TAG = "HwTripleFingersFreeForm";
    private static final String TALKBACK_CONFIG = "ro.config.hw_talkback_btn";
    private static final int TRIGGER_MIN_FINGERS = 3;
    ActivityManager mAm = null;
    private Context mContext;
    private float mDensity = DENSITY_INIT;
    private Handler mHandler;
    private HwInputManagerService mIm = null;
    private boolean mIsDockedStackExists = false;
    private boolean mIsFilter = false;
    private boolean mIsFilterCurrentTouch = false;
    private KeyguardManager mKeyguardManager;
    private float mMaxFingerDownDistanceX = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
    private float mMaxFingerDownDistanceY = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
    private float mMinTriggerDistanceY = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
    private float mMinTriggerLandscapeDistanceY = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
    private float mMinTriggerPortraitDistanceY = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
    private HwPhoneWindowManager mPolicy;
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        /* class com.android.server.input.HwTripleFingersFreeForm.AnonymousClass1 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (intent != null && intent.getAction() != null && HwTripleFingersFreeForm.APS_RESOLUTION_CHANGE_ACTION.equals(intent.getAction())) {
                HwTripleFingersFreeForm hwTripleFingersFreeForm = HwTripleFingersFreeForm.this;
                hwTripleFingersFreeForm.mDensity = hwTripleFingersFreeForm.mContext.getResources().getDisplayMetrics().density;
                HwTripleFingersFreeForm hwTripleFingersFreeForm2 = HwTripleFingersFreeForm.this;
                hwTripleFingersFreeForm2.mMaxFingerDownDistanceY = hwTripleFingersFreeForm2.dipsToPixels(HwTripleFingersFreeForm.MAX_FINGER_DOWN_Y_DISTANCE);
                HwTripleFingersFreeForm hwTripleFingersFreeForm3 = HwTripleFingersFreeForm.this;
                hwTripleFingersFreeForm3.mMaxFingerDownDistanceX = hwTripleFingersFreeForm3.dipsToPixels(HwTripleFingersFreeForm.MAX_FINGER_DOWN_X_DISTANCE);
                HwTripleFingersFreeForm hwTripleFingersFreeForm4 = HwTripleFingersFreeForm.this;
                hwTripleFingersFreeForm4.mMinTriggerLandscapeDistanceY = hwTripleFingersFreeForm4.dipsToPixels(HwTripleFingersFreeForm.MIN_Y_TRIGGER_LANDSCAPE_DISTANCE);
                HwTripleFingersFreeForm hwTripleFingersFreeForm5 = HwTripleFingersFreeForm.this;
                hwTripleFingersFreeForm5.mMinTriggerPortraitDistanceY = hwTripleFingersFreeForm5.dipsToPixels(HwTripleFingersFreeForm.MIN_Y_TRIGGER_PORTRAIT_DISTANCE);
                Log.i(HwTripleFingersFreeForm.TAG, "MaxYDistance:" + HwTripleFingersFreeForm.this.mMaxFingerDownDistanceY + "MaxXDistance:" + HwTripleFingersFreeForm.this.mMaxFingerDownDistanceX + ",MinLandDistance:" + HwTripleFingersFreeForm.this.mMinTriggerLandscapeDistanceY + ",MinPortraitDistance:" + HwTripleFingersFreeForm.this.mMinTriggerPortraitDistanceY);
            }
        }
    };
    private SparseArray<Point> mTouchingFingers = new SparseArray<>();
    private float mTrigerStartThreshold = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
    private float mTrigerStartThresholdAbove = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;

    private final class FreeFormHandler extends HandlerAdapter {
        private FreeFormHandler(Looper looper) {
            super(looper, (Handler.Callback) null, true);
        }

        public void handleMessage(Message msg) {
            int i = msg.what;
            if (i == 0) {
                HwTripleFingersFreeForm.this.handleFreeFormInit();
            } else if (i == 1) {
                if (HwTripleFingersFreeForm.this.isTopTaskHome()) {
                    HwTripleFingersFreeForm hwTripleFingersFreeForm = HwTripleFingersFreeForm.this;
                    hwTripleFingersFreeForm.showToastForAllUser(hwTripleFingersFreeForm.mContext, HwPartResourceUtils.getResourceId("split_app_three_finger_slide_message"));
                    return;
                }
                HwTripleFingersFreeForm hwTripleFingersFreeForm2 = HwTripleFingersFreeForm.this;
                hwTripleFingersFreeForm2.showToastForAllUser(hwTripleFingersFreeForm2.mContext, 33685924);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isTopTaskHome() {
        List<ActivityManager.RunningTaskInfo> tasks;
        ActivityManager.RunningTaskInfo topTask;
        ActivityManager activityManager = this.mAm;
        if (activityManager == null || (tasks = activityManager.getRunningTasks(1)) == null || tasks.isEmpty() || (topTask = tasks.get(0)) == null || !isInHomeStack(topTask)) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: private */
    public static class Point {
        private float moveY;
        private float pointX;
        private float pointY;
        private long time;

        private Point(float ax, float ay, long inTime) {
            this.pointX = ax;
            this.pointY = ay;
            this.time = inTime;
            this.moveY = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void updateMoveDistance(float ay) {
            this.moveY = ay - this.pointY;
        }
    }

    /* JADX WARN: Type inference failed for: r3v2, types: [com.android.server.input.HwTripleFingersFreeForm$FreeFormHandler, android.os.Handler] */
    /* JADX WARNING: Unknown variable types count: 1 */
    public HwTripleFingersFreeForm(Context context, HwInputManagerService inputManager) {
        this.mContext = context;
        this.mIm = inputManager;
        this.mAm = (ActivityManager) this.mContext.getSystemService("activity");
        if (!IS_DISABLE_MULTIWIN) {
            HandlerThread handlerThread = new HandlerThread(TAG);
            handlerThread.start();
            this.mHandler = new FreeFormHandler(handlerThread.getLooper());
            this.mHandler.sendEmptyMessage(0);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleFreeFormInit() {
        this.mDensity = this.mContext.getResources().getDisplayMetrics().density;
        this.mTrigerStartThreshold = (float) this.mContext.getResources().getDimensionPixelSize(HwPartResourceUtils.getResourceId("status_bar_height"));
        this.mTrigerStartThresholdAbove = (float) this.mContext.getResources().getDimensionPixelSize(HwPartResourceUtils.getResourceId("gesture_nav_bottom_window_height"));
        this.mPolicy = WindowManagerPolicyEx.getInstance().getHwPhoneWindowManager();
        this.mMaxFingerDownDistanceY = dipsToPixels(MAX_FINGER_DOWN_Y_DISTANCE);
        this.mMaxFingerDownDistanceX = dipsToPixels(MAX_FINGER_DOWN_X_DISTANCE);
        this.mMinTriggerLandscapeDistanceY = dipsToPixels(MIN_Y_TRIGGER_LANDSCAPE_DISTANCE);
        this.mMinTriggerPortraitDistanceY = dipsToPixels(MIN_Y_TRIGGER_PORTRAIT_DISTANCE);
        ContextEx.registerReceiverAsUser(this.mContext, this.mReceiver, UserHandleEx.ALL, new IntentFilter(APS_RESOLUTION_CHANGE_ACTION), APS_RESOLUTION_CHANGE_PERSISSIONS, (Handler) null);
    }

    public boolean handleMotionEvent(InputEvent event) {
        if (HwFreeFormUtils.getFreeFormStackVisible()) {
            return true;
        }
        if (event instanceof MotionEvent) {
            MotionEvent motionEvent = (MotionEvent) event;
            int action = motionEvent.getActionMasked();
            int id = motionEvent.getPointerId(motionEvent.getActionIndex());
            long time = motionEvent.getEventTime();
            if (action == 0) {
                resetState();
                this.mTouchingFingers.put(id, new Point(motionEvent.getRawX(), motionEvent.getRawY(), time));
            } else if (action == 1) {
                resetState();
            } else if (action != 2) {
                if (action == 5) {
                    this.mIsFilter = handleFingerDown(motionEvent);
                } else if (action == 6) {
                    handleFingerUp(motionEvent);
                    this.mTouchingFingers.delete(id);
                    this.mIsFilterCurrentTouch = false;
                }
            } else if (this.mIsFilter) {
                handleFingerMove(motionEvent);
            }
            return true ^ this.mIsFilterCurrentTouch;
        }
        Log.i(TAG, "handleMotionEvent not a motionEvent");
        return true;
    }

    private boolean handleFingerDown(MotionEvent motionEvent) {
        float offsetX = motionEvent.getRawX() - motionEvent.getX();
        float offsetY = motionEvent.getRawY() - motionEvent.getY();
        int actionIndex = motionEvent.getActionIndex();
        int id = motionEvent.getPointerId(actionIndex);
        Point pointerDown = new Point(motionEvent.getX(actionIndex) + offsetX, motionEvent.getY(actionIndex) + offsetY, motionEvent.getEventTime());
        this.mTouchingFingers.put(id, pointerDown);
        int fingerSize = this.mTouchingFingers.size();
        Log.i(TAG, "handleFingerDown new finger(" + id + ") touch down at " + pointerDown + ",size:" + fingerSize);
        if (fingerSize != 3) {
            Log.i(TAG, "handleFingerDown " + fingerSize + " fingers touching down");
            return false;
        } else if (getDistanceY() > this.mMaxFingerDownDistanceY) {
            Log.i(TAG, "fingers touch down on the screen exceeds to much on Y");
            return false;
        } else if (getDistanceX() > this.mMaxFingerDownDistanceX) {
            Log.i(TAG, "fingers touch down on the screen exceeds to much on X");
            return false;
        } else if (((float) getInterval()) > MAX_FINGER_DOWN_INTERVAL) {
            Log.i(TAG, "fingers'interval longer than except time!");
            return false;
        } else if (shouldDisableGesture()) {
            return false;
        } else {
            if (this.mIsDockedStackExists) {
                Log.i(TAG, "DockedStackExist!");
                return false;
            }
            startHandleTripleFinger(motionEvent);
            return true;
        }
    }

    private void handleSplitScreenGesture() {
        Handler handler = this.mHandler;
        if (handler != null) {
            handler.post(new Runnable() {
                /* class com.android.server.input.HwTripleFingersFreeForm.AnonymousClass2 */

                @Override // java.lang.Runnable
                public void run() {
                    Log.i(HwTripleFingersFreeForm.TAG, "handleFingerUp toggle split to systemUI");
                    StatusBarManagerInternalEx.toggleSplitScreen();
                }
            });
        }
    }

    private boolean isNewSimpleModeOn(Context context) {
        int simpleModeValue = SettingsEx.System.getIntForUser(context.getContentResolver(), GestureNavConst.SIMPLE_MODE_DB_KEY, 0, ActivityManagerEx.getCurrentUser());
        Log.i(TAG, " isNewSimpleModeOn simpleModeValue = " + simpleModeValue);
        if (simpleModeValue == 1) {
            return true;
        }
        return false;
    }

    private boolean isOldSimpleModeOn(Context context) {
        int simpleModeValue = SettingsEx.System.getIntForUser(context.getContentResolver(), "Simple", 0, ActivityManagerEx.getCurrentUser());
        Log.i(TAG, " isOldSimpleModeOn simpleModeValue = " + simpleModeValue);
        if (simpleModeValue == 1) {
            return true;
        }
        return false;
    }

    private boolean isDeviceProvisioning() {
        int deviceProvisioned = SettingsEx.Secure.getIntForUser(this.mContext.getContentResolver(), "device_provisioned", 0, ActivityManagerEx.getCurrentUser());
        Log.i(TAG, " isDeviceProvisioned deviceProvisioned = " + deviceProvisioned);
        if (deviceProvisioned == 0) {
            return true;
        }
        return false;
    }

    private KeyguardManager getKeyguardService() {
        if (this.mKeyguardManager == null) {
            this.mKeyguardManager = (KeyguardManager) this.mContext.getSystemService("keyguard");
        }
        return this.mKeyguardManager;
    }

    private boolean isScreenReaderEnabled() {
        if (SettingsEx.Secure.getIntForUser(this.mContext.getContentResolver(), ACCESSIBILITY_SCREENREADER_ENABLED, 0, ActivityManagerEx.getCurrentUser()) == 1) {
            return true;
        }
        return false;
    }

    private boolean isInLazyMode() {
        String defaultMode = Settings.Global.getString(this.mContext.getContentResolver(), "single_hand_mode");
        Log.i(TAG, "defaultMode:" + defaultMode);
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

    private boolean isDockDisabled() {
        if (isNewSimpleModeOn(this.mContext) || isOldSimpleModeOn(this.mContext)) {
            return true;
        }
        KeyguardManager keyguardManager = getKeyguardService();
        if (keyguardManager != null && keyguardManager.isKeyguardLocked()) {
            Log.i(TAG, "dock is disabled in keyguard lock state");
            return true;
        } else if (isScreenReaderEnabled()) {
            Log.i(TAG, "dock is disabled in talkback mode");
            return true;
        } else if (isDeviceProvisioning()) {
            Log.i(TAG, "dock is disabled in device provisioning mode");
            return true;
        } else if (isInSubOrCoorFoldDisplayMode()) {
            Log.i(TAG, "dock is disabled in sub fold display mode");
            return true;
        } else if (!isInLazyMode()) {
            return false;
        } else {
            Log.i(TAG, "dock is disabled in lazy mode");
            return true;
        }
    }

    private void startDockTipDialogActivity() {
        if (isDockDisabled()) {
            Log.w(TAG, "not startDockTipDialogActivity when dock is disabled");
            return;
        }
        Log.i(TAG, "startDockTipDialogActivity");
        Intent intent = new Intent("com.huawei.hwdockbar.action");
        intent.putExtra("AROUSAL_MODE", "triple");
        intent.addFlags(67108864);
        try {
            ContextEx.startActivityAsUser(this.mContext, intent, (Bundle) null, UserHandleEx.CURRENT_OR_SELF);
        } catch (ActivityNotFoundException e) {
            Log.e(TAG, "start hwdockbar ActivityNotFoundException");
        }
    }

    private void startHandleTripleFinger(MotionEvent motionEvent) {
        if (this.mPolicy == null) {
            this.mPolicy = WindowManagerPolicyEx.getInstance().getHwPhoneWindowManager();
        }
        HwPhoneWindowManager hwPhoneWindowManager = this.mPolicy;
        if (hwPhoneWindowManager == null || !hwPhoneWindowManager.isLandscape()) {
            this.mMinTriggerDistanceY = -this.mMinTriggerPortraitDistanceY;
        } else {
            this.mMinTriggerDistanceY = -this.mMinTriggerLandscapeDistanceY;
        }
    }

    private void handleFingerMove(MotionEvent motionEvent) {
        float offsetY = motionEvent.getY() - motionEvent.getRawY();
        int pointerCount = motionEvent.getPointerCount();
        int moveCount = 0;
        for (int i = 0; i < pointerCount; i++) {
            float curY = motionEvent.getY(i) + offsetY;
            int id = motionEvent.getPointerId(i);
            Point point = this.mTouchingFingers.get(id);
            if (point == null) {
                Log.i(TAG, "handleFingerMove point(" + id + ") not tracked!");
            } else {
                point.updateMoveDistance(curY);
                if (!this.mIsFilterCurrentTouch && curY < point.pointY) {
                    moveCount++;
                    Log.i(TAG, "handleFingerMove finger(" + id + ") moveCount:" + moveCount);
                    if (moveCount == 3) {
                        postCancelEvent(motionEvent);
                        this.mIsFilterCurrentTouch = true;
                    }
                }
            }
        }
    }

    private void postCancelEvent(final MotionEvent motionEvent) {
        Log.i(TAG, "handleFingerMove cancel");
        Handler handler = this.mHandler;
        if (handler != null) {
            handler.post(new Runnable() {
                /* class com.android.server.input.HwTripleFingersFreeForm.AnonymousClass3 */

                @Override // java.lang.Runnable
                public void run() {
                    MotionEvent cancelEvent = MotionEventEx.copy(motionEvent);
                    cancelEvent.setAction(3);
                    HwTripleFingersFreeForm.this.mIm.injectInputEvent(cancelEvent, InputManagerEx.getInjectInputEventModeWaitForFinish());
                }
            });
        }
    }

    private void handleFingerUp(MotionEvent motionEvent) {
        int id = motionEvent.getPointerId(motionEvent.getActionIndex());
        Log.i(TAG, "handleFingerUp finger(" + id + ") up");
        if (!this.mIsFilterCurrentTouch) {
            Log.i(TAG, "handleFingerUp, current touch not marked as filter");
            return;
        }
        int fingerSize = this.mTouchingFingers.size();
        if (fingerSize != 3) {
            Log.i(TAG, "handleFingerUp fingerSize = " + fingerSize);
        } else if (SystemPropertiesEx.getInt("sys.ride_mode", 0) == 1) {
            Log.i(TAG, "can not split in Ride mode");
        } else {
            boolean isDistanceLongEnough = true;
            int i = 0;
            while (true) {
                if (i >= fingerSize) {
                    break;
                }
                float moveDistance = this.mTouchingFingers.get(i).moveY;
                if (moveDistance > this.mMinTriggerDistanceY) {
                    Log.i(TAG, "move " + pixelsToDips(moveDistance) + "dp, less than except distance!");
                    isDistanceLongEnough = false;
                    break;
                }
                i++;
            }
            if (isDistanceLongEnough) {
                resetState();
                if (IS_MULTIWINDOW_OPTIMIZATION) {
                    startDockTipDialogActivity();
                } else if (!isMultiWindowDisabled()) {
                    if (shouldSplit() || isSimpleUi()) {
                        handleSplitScreenGesture();
                        updateDockedStackFlag();
                    } else if (this.mHandler != null) {
                        Log.i(TAG, "app do not surpot split show toast");
                        this.mHandler.sendEmptyMessage(1);
                    }
                }
            }
        }
    }

    private boolean shouldSplit() {
        ActivityManager.RunningTaskInfo topTask = getTopMostTask();
        if (topTask == null || isInHomeStack(topTask) || !ActivityManagerExt.getSupportsSplitScreenMultiWindow(topTask)) {
            return false;
        }
        return true;
    }

    private void resetState() {
        this.mIsFilterCurrentTouch = false;
        this.mIsFilter = false;
        this.mTouchingFingers.clear();
    }

    private boolean isUserUnlocked() {
        return UserManagerExt.isUserUnlocked((UserManager) this.mContext.getSystemService("user"), ActivityManagerEx.getCurrentUser());
    }

    private boolean shouldDisableGesture() {
        if (IS_DISABLE_MULTIWIN) {
            Log.i(TAG, "product is not support split");
            return true;
        } else if (inInValidArea()) {
            Log.i(TAG, "finger is too close to navigation");
            return true;
        } else if (isGameMode()) {
            Log.i(TAG, "can not split in gaming");
            return true;
        } else if (SystemPropertiesEx.getBoolean(TALKBACK_CONFIG, true) && isTalkBackServicesOn()) {
            Log.i(TAG, "can not split in talkback mode");
            return true;
        } else if (SystemPropertiesEx.getBoolean("runtime.mmitest.isrunning", false)) {
            Log.i(TAG, "can not split in MMI test");
            return true;
        } else if (SystemPropertiesEx.getBoolean(GestureNavConst.KEY_SUPER_SAVE_MODE, false)) {
            Log.i(TAG, "can not split in superpower");
            return true;
        } else if (isUserUnlocked()) {
            return false;
        } else {
            Log.i(TAG, "Do not allow split if user is unlocked");
            return true;
        }
    }

    private boolean isTalkBackServicesOn() {
        Context context = this.mContext;
        if (context == null) {
            return false;
        }
        boolean isAccessibilityEnabled = SettingsEx.Secure.getIntForUser(context.getContentResolver(), "accessibility_enabled", 0, -2) == 1;
        boolean isContainsTalkBackService = Settings.Secure.getInt(this.mContext.getContentResolver(), ACCESSIBILITY_SCREENREADER_ENABLED, 0) == 1;
        if (!isAccessibilityEnabled || !isContainsTalkBackService) {
            return false;
        }
        return true;
    }

    private boolean inInValidArea() {
        int fingerSize = this.mTouchingFingers.size();
        float height = (float) this.mContext.getResources().getDisplayMetrics().heightPixels;
        for (int i = 0; i < fingerSize; i++) {
            float fromY = this.mTouchingFingers.valueAt(i).pointY;
            if (fromY <= this.mTrigerStartThreshold || fromY >= height - this.mTrigerStartThresholdAbove) {
                return true;
            }
        }
        return false;
    }

    /* JADX INFO: Multiple debug info for r5v2 long: [D('i' int), D('interval' long)] */
    private long getInterval() {
        long startTime = Long.MAX_VALUE;
        long latestTime = Long.MIN_VALUE;
        int fingerSize = this.mTouchingFingers.size();
        for (int i = 0; i < fingerSize; i++) {
            Point point = this.mTouchingFingers.valueAt(i);
            if (point.time < startTime) {
                startTime = point.time;
            }
            if (point.time > latestTime) {
                latestTime = point.time;
            }
        }
        return latestTime - startTime;
    }

    /* access modifiers changed from: package-private */
    public final float dipsToPixels(float dips) {
        return (this.mDensity * dips) + DIP_TO_PIXEL_OFFSET;
    }

    /* access modifiers changed from: package-private */
    public final float pixelsToDips(float pixels) {
        return (pixels / this.mDensity) + DIP_TO_PIXEL_OFFSET;
    }

    private ActivityManager.RunningTaskInfo getTopMostTask() {
        List<ActivityManager.RunningTaskInfo> tasks = getRunningTasks(1);
        if (tasks == null || tasks.isEmpty()) {
            return null;
        }
        return tasks.get(0);
    }

    private List<ActivityManager.RunningTaskInfo> getRunningTasks(int numTasks) {
        ActivityManager activityManager = this.mAm;
        if (activityManager == null) {
            return null;
        }
        return activityManager.getRunningTasks(numTasks);
    }

    private boolean isInHomeStack(ActivityManager.RunningTaskInfo runningTask) {
        if (runningTask != null && WindowConfigurationEx.getActivityType(runningTask) == 2) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void showToastForAllUser(final Context context, final int message) {
        if (context != null) {
            runOnUiThread(new Runnable() {
                /* class com.android.server.input.HwTripleFingersFreeForm.AnonymousClass4 */

                @Override // java.lang.Runnable
                public void run() {
                    Toast toast = Toast.makeText(context, message, 0);
                    new WindowManagerEx.LayoutParamsEx(ToastEx.getWindowParams(toast)).addPrivateFlags(WindowManagerEx.LayoutParamsEx.getPrivateFlagShowForAllUsers());
                    toast.show();
                }
            });
        }
    }

    private void runOnUiThread(Runnable runnable) {
        Handler handler = new Handler();
        if (handler.getLooper() != Looper.myLooper()) {
            handler.post(runnable);
        } else {
            runnable.run();
        }
    }

    private boolean isSimpleUi() {
        int simpleuiVal = SettingsEx.System.getIntForUser(this.mContext.getContentResolver(), SettingsEx.System.SIMPLEUI_MODE, 0, ActivityManagerEx.getCurrentUser());
        if (simpleuiVal == 2 || simpleuiVal == 5) {
            return true;
        }
        return false;
    }

    private boolean isGameMode() {
        return ActivityManagerEx.isGameDndOn();
    }

    private float getDistanceY() {
        float maxY = Float.MIN_VALUE;
        float minY = Float.MAX_VALUE;
        int fingerSize = this.mTouchingFingers.size();
        for (int i = 0; i < fingerSize; i++) {
            Point point = this.mTouchingFingers.valueAt(i);
            if (point.pointY < minY) {
                minY = point.pointY;
            }
            if (point.pointY > maxY) {
                maxY = point.pointY;
            }
        }
        float distanceY = maxY - minY;
        Log.i(TAG, "getDistance maxY = " + maxY + ", minY = " + minY);
        return distanceY;
    }

    private float getDistanceX() {
        float maxX = Float.MIN_VALUE;
        float minX = Float.MAX_VALUE;
        int fingerSize = this.mTouchingFingers.size();
        for (int i = 0; i < fingerSize; i++) {
            Point point = this.mTouchingFingers.valueAt(i);
            if (point.pointX < minX) {
                minX = point.pointX;
            }
            if (point.pointX > maxX) {
                maxX = point.pointX;
            }
        }
        float distanceX = maxX - minX;
        Log.i(TAG, "getDistance maxX = " + maxX + ", minX = " + minX);
        return distanceX;
    }

    private void updateDockedStackFlag() {
        try {
            WindowManagerExt.registerDockedStackListener(new IDockedStackListenerEx() {
                /* class com.android.server.input.HwTripleFingersFreeForm.AnonymousClass5 */

                public void onDividerVisibilityChanged(boolean isVisible) throws RemoteException {
                }

                public void onDockedStackExistsChanged(boolean isExist) throws RemoteException {
                    HwTripleFingersFreeForm.this.mIsDockedStackExists = isExist;
                }

                public void onDockedStackMinimizedChanged(boolean isMinimized, long animDuration, boolean isHomeStackResizable) throws RemoteException {
                }

                public void onAdjustedForImeChanged(boolean isAdjustedForIme, long animDuration) throws RemoteException {
                }

                public void onDockSideChanged(int newDockSide) throws RemoteException {
                }
            });
        } catch (RemoteException e) {
            Log.e(TAG, "Failed registering docked stack exists listener");
        }
    }

    private boolean isMultiWindowDisabled() {
        return HwActivityTaskManager.getMultiWindowDisabled();
    }
}
