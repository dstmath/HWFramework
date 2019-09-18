package com.android.server.input;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.freeform.HwFreeFormManager;
import android.freeform.HwFreeFormUtils;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings;
import android.util.SparseArray;
import android.util.SplitNotificationUtils;
import android.view.InputEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.WindowManager;
import com.android.server.LocalServices;
import com.android.server.gesture.GestureNavConst;
import com.android.server.policy.HwPhoneWindowManager;
import com.android.server.policy.HwSplitScreenArrowView;
import com.android.server.policy.WindowManagerPolicy;

public class HwTripleFingersFreeForm {
    private static final String APS_RESOLUTION_CHANGE_ACTION = "huawei.intent.action.APS_RESOLUTION_CHANGE_ACTION";
    private static final String APS_RESOLUTION_CHANGE_PERSISSIONS = "huawei.intent.permissions.APS_RESOLUTION_CHANGE_ACTION";
    private static final boolean DISABLE_MULTIWIN = SystemProperties.getBoolean("ro.huawei.disable_multiwindow", false);
    private static final float MAX_FINGER_DOWN_INTERVAL = 1000.0f;
    private static final float MAX_FINGER_DOWN_Y_DISTANCE = 240.0f;
    private static final float MIN_Y_TRIGGER_LANDSCAPE_DISTANCE = 90.0f;
    private static final float MIN_Y_TRIGGER_PORTRAIT_DISTANCE = 120.0f;
    public static final int MSG_HANDLE_FREEFORM_INIT = 0;
    public static final int SCREEN_STATE_DEFAULT = 0;
    public static final int SCREEN_STATE_IS_FULL_SCREEN = 1;
    public static final int SCREEN_STATE_NOT_FULL_SCREEN = 2;
    private static final String TAG = "HwTripleFingersFreeForm";
    private static final String TALKBACK_COMPONENT_NAME = "com.google.android.marvin.talkback/com.google.android.marvin.talkback.TalkBackService";
    private static final String TALKBACK_CONFIG = "ro.config.hw_talkback_btn";
    private static final int TRIGGER_MIN_FINGERS = 3;
    private static final boolean mEnableFingerFreeForm = SystemProperties.getBoolean("ro.config.hw_freeform_enable", false);
    private static int mScreenState = 0;
    /* access modifiers changed from: private */
    public Context mContext;
    /* access modifiers changed from: private */
    public float mDensity = 2.0f;
    private boolean mFilterCurrentTouch = false;
    private Handler mHandler;
    /* access modifiers changed from: private */
    public HwInputManagerService mIm = null;
    private boolean mIsChange = false;
    private HwSplitScreenArrowView mLandMultiWinArrowView = null;
    /* access modifiers changed from: private */
    public float mMaxFingerDownYDistance = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
    private float mMinYTriggerDistance = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
    /* access modifiers changed from: private */
    public float mMinYTriggerLandscapeDistance = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
    /* access modifiers changed from: private */
    public float mMinYTriggerPortraitDistance = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
    private HwSplitScreenArrowView mMultiWinArrowView = null;
    private HwPhoneWindowManager mPolicy;
    private HwSplitScreenArrowView mPortMultiWinArrowView = null;
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (!(intent == null || intent.getAction() == null || !HwTripleFingersFreeForm.APS_RESOLUTION_CHANGE_ACTION.equals(intent.getAction()))) {
                float unused = HwTripleFingersFreeForm.this.mDensity = HwTripleFingersFreeForm.this.mContext.getResources().getDisplayMetrics().density;
                float unused2 = HwTripleFingersFreeForm.this.mMaxFingerDownYDistance = HwTripleFingersFreeForm.this.dipsToPixels(HwTripleFingersFreeForm.MAX_FINGER_DOWN_Y_DISTANCE);
                float unused3 = HwTripleFingersFreeForm.this.mMinYTriggerLandscapeDistance = HwTripleFingersFreeForm.this.dipsToPixels(HwTripleFingersFreeForm.MIN_Y_TRIGGER_LANDSCAPE_DISTANCE);
                float unused4 = HwTripleFingersFreeForm.this.mMinYTriggerPortraitDistance = HwTripleFingersFreeForm.this.dipsToPixels(HwTripleFingersFreeForm.MIN_Y_TRIGGER_PORTRAIT_DISTANCE);
                HwFreeFormUtils.log("input", "MaxYDistance:" + HwTripleFingersFreeForm.this.mMaxFingerDownYDistance + ",MinLandDistance:" + HwTripleFingersFreeForm.this.mMinYTriggerLandscapeDistance + ",MinPortraitDistance:" + HwTripleFingersFreeForm.this.mMinYTriggerPortraitDistance);
            }
        }
    };
    private SparseArray<Point> mTouchingFingers = new SparseArray<>();
    private float mTrigerStartThreshold = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
    private float mTrigerStartThresholdAbove = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
    private WindowManager mWindowManager;

    private final class FreeFormHandler extends Handler {
        public FreeFormHandler(Looper looper) {
            super(looper, null, true);
        }

        public void handleMessage(Message msg) {
            if (msg.what == 0) {
                HwTripleFingersFreeForm.this.handleFreeFormInit();
            }
        }
    }

    private static class Point {
        public float moveY = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
        public long time;
        public float x;
        public float y;

        public Point(float x_, float y_, long time_) {
            this.x = x_;
            this.y = y_;
            this.time = time_;
        }

        public void updateMoveDistance(float x_, float y_) {
            this.moveY = y_ - this.y;
        }

        public String toString() {
            return "(" + this.x + "," + this.y + ")";
        }
    }

    public HwTripleFingersFreeForm(Context context, HwInputManagerService inputManager) {
        this.mContext = context;
        this.mIm = inputManager;
        HandlerThread handlerThread = new HandlerThread(TAG);
        handlerThread.start();
        this.mHandler = new FreeFormHandler(handlerThread.getLooper());
        this.mHandler.sendEmptyMessage(0);
    }

    /* access modifiers changed from: private */
    public void handleFreeFormInit() {
        if (mEnableFingerFreeForm) {
            this.mDensity = this.mContext.getResources().getDisplayMetrics().density;
            this.mTrigerStartThreshold = (float) this.mContext.getResources().getDimensionPixelSize(17105318);
            this.mTrigerStartThresholdAbove = (float) this.mContext.getResources().getDimensionPixelSize(34472334);
            this.mPolicy = (HwPhoneWindowManager) LocalServices.getService(WindowManagerPolicy.class);
            this.mMaxFingerDownYDistance = dipsToPixels(MAX_FINGER_DOWN_Y_DISTANCE);
            this.mMinYTriggerLandscapeDistance = dipsToPixels(MIN_Y_TRIGGER_LANDSCAPE_DISTANCE);
            this.mMinYTriggerPortraitDistance = dipsToPixels(MIN_Y_TRIGGER_PORTRAIT_DISTANCE);
            this.mContext.registerReceiverAsUser(this.mReceiver, UserHandle.ALL, new IntentFilter(APS_RESOLUTION_CHANGE_ACTION), APS_RESOLUTION_CHANGE_PERSISSIONS, null);
        }
        initView();
        createMultiWinArrowView();
    }

    public void createMultiWinArrowView() {
        if (ActivityManager.supportsMultiWindow(this.mContext)) {
            if (this.mMultiWinArrowView != null) {
                this.mMultiWinArrowView.removeViewToWindow();
            }
            HwFreeFormUtils.log("testFreeForm", "getConfiguration orientation=" + this.mContext.getResources().getConfiguration().orientation);
            if (1 == this.mContext.getResources().getConfiguration().orientation) {
                this.mMultiWinArrowView = this.mPortMultiWinArrowView;
            } else {
                this.mMultiWinArrowView = this.mLandMultiWinArrowView;
            }
            if (this.mMultiWinArrowView != null) {
                this.mMultiWinArrowView.addViewToWindow();
            }
        }
    }

    private void initView() {
        this.mTrigerStartThresholdAbove = (float) this.mContext.getResources().getDimensionPixelSize(34472334);
        android.graphics.Point screenDims = new android.graphics.Point();
        this.mWindowManager = (WindowManager) this.mContext.getSystemService("window");
        this.mWindowManager.getDefaultDisplay().getRealSize(screenDims);
        this.mPortMultiWinArrowView = (HwSplitScreenArrowView) LayoutInflater.from(this.mContext).inflate(34013263, null);
        if (this.mPortMultiWinArrowView != null) {
            this.mPortMultiWinArrowView.initViewParams(1, screenDims);
        }
        this.mLandMultiWinArrowView = (HwSplitScreenArrowView) LayoutInflater.from(this.mContext).inflate(34013264, null);
        if (this.mLandMultiWinArrowView != null) {
            this.mLandMultiWinArrowView.initViewParams(2, new android.graphics.Point(screenDims.y, screenDims.x));
        }
    }

    public boolean handleMotionEvent(InputEvent event) {
        if (!(event instanceof MotionEvent) || HwFreeFormUtils.getFreeFormStackVisible()) {
            return true;
        }
        MotionEvent motionEvent = (MotionEvent) event;
        int action = motionEvent.getActionMasked();
        int id = motionEvent.getPointerId(motionEvent.getActionIndex());
        long time = motionEvent.getEventTime();
        int pointerCount = motionEvent.getPointerCount();
        switch (action) {
            case 0:
                resetState();
                this.mTouchingFingers.put(id, new Point(motionEvent.getRawX(), motionEvent.getRawY(), time));
                break;
            case 1:
                mScreenState = 0;
                resetState();
                break;
            case 2:
                if (this.mFilterCurrentTouch) {
                    handleFingerMove(motionEvent);
                    break;
                }
                break;
            case 5:
                if (!this.mFilterCurrentTouch) {
                    this.mFilterCurrentTouch = handleFingerDown(motionEvent);
                    break;
                }
                break;
            case 6:
                handleFingerUp(motionEvent);
                this.mTouchingFingers.delete(id);
                return true ^ this.mFilterCurrentTouch;
        }
        if (mScreenState == 1 || ((action == 2 && (pointerCount != 3 || action != 2)) || inInValidArea() || DISABLE_MULTIWIN)) {
            return true ^ this.mFilterCurrentTouch;
        }
        this.mMultiWinArrowView.handleSplitScreenGesture(motionEvent);
        return true ^ this.mFilterCurrentTouch;
    }

    private boolean handleFingerDown(MotionEvent motionEvent) {
        float offsetX = motionEvent.getRawX() - motionEvent.getX();
        float offsetY = motionEvent.getRawY() - motionEvent.getY();
        int actionIndex = motionEvent.getActionIndex();
        this.mTouchingFingers.put(motionEvent.getPointerId(actionIndex), new Point(motionEvent.getX(actionIndex) + offsetX, motionEvent.getY(actionIndex) + offsetY, motionEvent.getEventTime()));
        int fingerSize = this.mTouchingFingers.size();
        if (fingerSize < 3 || fingerSize != 3 || getDistance() > this.mMaxFingerDownYDistance || ((float) getInterval()) > MAX_FINGER_DOWN_INTERVAL || shouldDisableGesture()) {
            return false;
        }
        startHandleTripleFinger(motionEvent);
        return true;
    }

    private void startHandleTripleFinger(final MotionEvent motionEvent) {
        if (this.mPolicy == null) {
            this.mPolicy = (HwPhoneWindowManager) LocalServices.getService(WindowManagerPolicy.class);
        }
        if (this.mPolicy.isLandscape()) {
            this.mMinYTriggerDistance = -this.mMinYTriggerLandscapeDistance;
        } else {
            this.mMinYTriggerDistance = -this.mMinYTriggerPortraitDistance;
        }
        this.mHandler.post(new Runnable() {
            public void run() {
                MotionEvent cancelEvent = motionEvent.copy();
                cancelEvent.setAction(3);
                HwTripleFingersFreeForm.this.mIm.injectInputEvent(cancelEvent, 2);
            }
        });
    }

    private void handleFingerMove(MotionEvent motionEvent) {
        float offsetY = motionEvent.getY() - motionEvent.getRawY();
        float offsetX = motionEvent.getX() - motionEvent.getRawX();
        int pointerCount = motionEvent.getPointerCount();
        if (pointerCount > 3) {
            resetState();
            return;
        }
        for (int i = 0; i < pointerCount; i++) {
            float curY = motionEvent.getY(i) + offsetY;
            float curX = motionEvent.getX(i) + offsetX;
            Point p = this.mTouchingFingers.get(motionEvent.getPointerId(i));
            if (p != null) {
                p.updateMoveDistance(curX, curY);
            }
        }
    }

    private void handleFingerUp(MotionEvent motionEvent) {
        int pointerId = motionEvent.getPointerId(motionEvent.getActionIndex());
        if (this.mFilterCurrentTouch) {
            int fingerSize = this.mTouchingFingers.size();
            if (fingerSize == 3) {
                boolean isDistanceLongEnough = true;
                int i = 0;
                while (true) {
                    if (i >= fingerSize) {
                        break;
                    }
                    float moveDistance = this.mTouchingFingers.get(i).moveY;
                    if (moveDistance > this.mMinYTriggerDistance) {
                        HwFreeFormUtils.log("input", "move " + pixelsToDips(moveDistance) + "dp, less than except distance!");
                        isDistanceLongEnough = false;
                        break;
                    }
                    i++;
                }
                if (isDistanceLongEnough) {
                    resetState();
                    if (setScreenState()) {
                        HwFreeFormUtils.log("input", "addFloatListView");
                        HwFreeFormManager.getInstance(this.mContext).addFloatListView();
                    } else {
                        HwFreeFormUtils.log("input", "showUnsupportedToast");
                    }
                }
            }
        }
    }

    private boolean shouldTriggerFreeForm() {
        return SplitNotificationUtils.getInstance(this.mContext).getNotificationType("", 2).equals("floating_window");
    }

    private boolean shouldSplit() {
        if (this.mMultiWinArrowView == null || !this.mMultiWinArrowView.isTopTaskSupportMultiWindow()) {
            return false;
        }
        return true;
    }

    private void resetState() {
        this.mFilterCurrentTouch = false;
        this.mTouchingFingers.clear();
    }

    private boolean isUserUnlocked() {
        return ((UserManager) this.mContext.getSystemService("user")).isUserUnlocked(ActivityManager.getCurrentUser());
    }

    private boolean shouldDisableGesture() {
        if (inInValidArea()) {
            return true;
        }
        if ((SystemProperties.getBoolean(TALKBACK_CONFIG, true) && isTalkBackServicesOn()) || SystemProperties.getBoolean("runtime.mmitest.isrunning", false) || SystemProperties.getBoolean("sys.super_power_save", false)) {
            return true;
        }
        if (isUserUnlocked()) {
            return false;
        }
        HwFreeFormUtils.log("input", "Do not allow to triple freeform if user is unlocked");
        return true;
    }

    private boolean isTalkBackServicesOn() {
        boolean z = false;
        if (this.mContext == null) {
            return false;
        }
        boolean accessibilityEnabled = Settings.Secure.getIntForUser(this.mContext.getContentResolver(), "accessibility_enabled", 0, -2) == 1;
        String enabledSerices = Settings.Secure.getStringForUser(this.mContext.getContentResolver(), "enabled_accessibility_services", -2);
        boolean isContainsTalkBackService = enabledSerices != null && enabledSerices.contains(TALKBACK_COMPONENT_NAME);
        if (accessibilityEnabled && isContainsTalkBackService) {
            z = true;
        }
        return z;
    }

    private boolean inInValidArea() {
        int fingerSize = this.mTouchingFingers.size();
        float height = (float) this.mContext.getResources().getDisplayMetrics().heightPixels;
        for (int i = 0; i < fingerSize; i++) {
            float fromY = this.mTouchingFingers.valueAt(i).y;
            if (fromY <= this.mTrigerStartThreshold || fromY >= height - this.mTrigerStartThresholdAbove) {
                return true;
            }
        }
        return false;
    }

    private float getDistance() {
        float maxY = Float.MIN_VALUE;
        float minY = Float.MAX_VALUE;
        int fingerSize = this.mTouchingFingers.size();
        for (int i = 0; i < fingerSize; i++) {
            Point p = this.mTouchingFingers.valueAt(i);
            if (p.y < minY) {
                minY = p.y;
            }
            if (p.y > maxY) {
                maxY = p.y;
            }
        }
        return maxY - minY;
    }

    private long getInterval() {
        long startTime = Long.MAX_VALUE;
        long latestTime = Long.MIN_VALUE;
        int fingerSize = this.mTouchingFingers.size();
        for (int i = 0; i < fingerSize; i++) {
            Point p = this.mTouchingFingers.valueAt(i);
            if (p.time < startTime) {
                startTime = p.time;
            }
            if (p.time > latestTime) {
                latestTime = p.time;
            }
        }
        return latestTime - startTime;
    }

    private boolean setScreenState() {
        if (mScreenState == 0) {
            if (shouldTriggerFreeForm()) {
                mScreenState = 1;
                return true;
            }
            mScreenState = 2;
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public final float dipsToPixels(float dips) {
        return (this.mDensity * dips) + 0.5f;
    }

    /* access modifiers changed from: package-private */
    public final float pixelsToDips(float pixels) {
        return (pixels / this.mDensity) + 0.5f;
    }
}
