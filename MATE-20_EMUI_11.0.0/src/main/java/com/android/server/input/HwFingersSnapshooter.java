package com.android.server.input;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.UserManager;
import android.provider.Settings;
import android.util.Flog;
import android.util.Log;
import android.util.SparseArray;
import android.view.InputEvent;
import android.view.MotionEvent;
import com.android.server.gesture.GestureNavConst;
import com.android.server.policy.DecisionUtil;
import com.android.server.policy.HwPhoneWindowManager;
import com.android.server.policy.WindowManagerPolicyEx;
import com.huawei.android.app.ActivityManagerEx;
import com.huawei.android.content.ContentResolverExt;
import com.huawei.android.content.ContextEx;
import com.huawei.android.content.IntentExEx;
import com.huawei.android.hardware.input.InputManagerEx;
import com.huawei.android.os.HandlerAdapter;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.os.UserHandleEx;
import com.huawei.android.os.UserManagerExt;
import com.huawei.android.provider.SettingsEx;
import com.huawei.android.view.MotionEventEx;
import com.huawei.hiai.awareness.AwarenessInnerConstants;
import com.huawei.utils.HwPartResourceUtils;

public class HwFingersSnapshooter {
    private static final String ACCESSIBILITY_SCREENREADER_ENABLED = "accessibility_screenreader_enabled";
    private static final String APS_RESOLUTION_CHANGE_ACTION = "huawei.intent.action.APS_RESOLUTION_CHANGE_ACTION";
    private static final String APS_RESOLUTION_CHANGE_PERSISSIONS = "huawei.intent.permissions.APS_RESOLUTION_CHANGE_ACTION";
    private static final String GAME_TRIPLE_FINGER = "game_triple_finger";
    private static final int GAME_TRIPLE_MODE_CLOSE = 2;
    private static final int GAME_TRIPLE_MODE_DEFAULT = 2;
    private static final int GAME_TRIPLE_MODE_OPEN = 1;
    private static final long INIT_DELAY_TIME = 10000;
    private static final float INIT_DENSITY = 2.0f;
    private static final String KEY_TRIPLE_FINGER_MOTION = "motion_triple_finger_shot";
    private static final float MAX_FINGER_DOWN_INTERVAL = 1000.0f;
    private static final float MAX_FINGER_DOWN_X_DISTANCE = 400.0f;
    private static final float MAX_FINGER_DOWN_Y_DISTANCE = 145.0f;
    private static final float MIN_Y_TRIGGER_LANDSCAPE_DISTANCE = 90.0f;
    private static final float MIN_Y_TRIGGER_PORTRAIT_DISTANCE = 120.0f;
    public static final int MSG_HANDLE_FINGER_SNAP_SHOOTER_INIT = 0;
    public static final int MSG_HANDLE_USER_SWITCH = 1;
    private static final int MSG_KEY_SCREEN_REMIND = 2;
    private static final int MSG_MENU_SCREEN_DELAY_TME = 15000;
    private static final String RIGHT_BRACKETS = "}";
    private static final String SCREEN_SHOT_EVENT_NAME = "com.huawei.screenshot.intent.action.TripleFingersScreenshot";
    private static final String TAG = "HwFingersSnapshooter";
    private static final String TALKBACK_CONFIG = "ro.config.hw_talkback_btn";
    private static final int TRIGGER_MIN_FINGERS = 3;
    private static final int TRIPLE_FINGER_MOTION_OFF = 0;
    private static final int TRIPLE_FINGER_MOTION_ON = 1;
    private static final float UPWARD_DIFF = 0.5f;
    private Context mContext;
    private float mDensity = INIT_DENSITY;
    private int mEnabled;
    private BroadcastReceiver mFingerSnapReceiver = new BroadcastReceiver() {
        /* class com.android.server.input.HwFingersSnapshooter.AnonymousClass3 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                Log.i(HwFingersSnapshooter.TAG, "onReceive intent action = " + intent.getAction());
                if (intent.getAction() != null) {
                    if (HwFingersSnapshooter.APS_RESOLUTION_CHANGE_ACTION.equals(intent.getAction())) {
                        HwFingersSnapshooter hwFingersSnapshooter = HwFingersSnapshooter.this;
                        hwFingersSnapshooter.mDensity = hwFingersSnapshooter.mContext.getResources().getDisplayMetrics().density;
                        HwFingersSnapshooter hwFingersSnapshooter2 = HwFingersSnapshooter.this;
                        hwFingersSnapshooter2.mMaxFingerDownyDistance = hwFingersSnapshooter2.dipsToPixels(HwFingersSnapshooter.MAX_FINGER_DOWN_Y_DISTANCE);
                        HwFingersSnapshooter hwFingersSnapshooter3 = HwFingersSnapshooter.this;
                        hwFingersSnapshooter3.mMaxFingerDownxDistance = hwFingersSnapshooter3.dipsToPixels(HwFingersSnapshooter.MAX_FINGER_DOWN_X_DISTANCE);
                        HwFingersSnapshooter hwFingersSnapshooter4 = HwFingersSnapshooter.this;
                        hwFingersSnapshooter4.mMinyTriggerLandscapeDistance = hwFingersSnapshooter4.dipsToPixels(HwFingersSnapshooter.MIN_Y_TRIGGER_LANDSCAPE_DISTANCE);
                        HwFingersSnapshooter hwFingersSnapshooter5 = HwFingersSnapshooter.this;
                        hwFingersSnapshooter5.mMinyTriggerPortraitDistance = hwFingersSnapshooter5.dipsToPixels(HwFingersSnapshooter.MIN_Y_TRIGGER_PORTRAIT_DISTANCE);
                        Log.i(HwFingersSnapshooter.TAG, "mDisplayResolutionModeObserver mMaxFingerDownyDistance = " + HwFingersSnapshooter.this.mMaxFingerDownyDistance + ",mMinyTriggerLandscapeDistance:" + HwFingersSnapshooter.this.mMinyTriggerLandscapeDistance + ",mMinyTriggerPortraitDistance:" + HwFingersSnapshooter.this.mMinyTriggerPortraitDistance);
                    } else if (IntentExEx.getActionUserSwitched().equals(intent.getAction())) {
                        Message message = Message.obtain();
                        message.what = 1;
                        message.arg1 = intent.getIntExtra("android.intent.extra.user_handle", 0);
                        HwFingersSnapshooter.this.mHandler.sendMessage(message);
                    }
                }
            }
        }
    };
    private Handler mHandler;
    private float mHeight = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
    private HwInputManagerService mIm = null;
    private boolean mIsCanFilter = false;
    private boolean mIsDeviceProvisioned = false;
    private boolean mIsFilterCurrentTouch = false;
    private float mMaxFingerDownxDistance = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
    private float mMaxFingerDownyDistance = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
    private float mMaxY = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
    private float mMinyTriggerDistance = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
    private float mMinyTriggerLandscapeDistance = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
    private float mMinyTriggerPortraitDistance = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
    private HwPhoneWindowManager mPolicy;
    ServiceConnection mScreenshotConnection = null;
    private final Object mScreenshotLock = new Object();
    private final Runnable mScreenshotRunnable = new Runnable() {
        /* class com.android.server.input.HwFingersSnapshooter.AnonymousClass2 */

        @Override // java.lang.Runnable
        public void run() {
            HwFingersSnapshooter.this.takeScreenshot();
            HwFingersSnapshooter.this.mHandler.sendEmptyMessageDelayed(2, 15000);
        }
    };
    final Runnable mScreenshotTimeout = new Runnable() {
        /* class com.android.server.input.HwFingersSnapshooter.AnonymousClass1 */

        @Override // java.lang.Runnable
        public void run() {
            synchronized (HwFingersSnapshooter.this.mScreenshotLock) {
                if (HwFingersSnapshooter.this.mScreenshotConnection != null) {
                    HwFingersSnapshooter.this.mContext.unbindService(HwFingersSnapshooter.this.mScreenshotConnection);
                    HwFingersSnapshooter.this.mScreenshotConnection = null;
                }
            }
        }
    };
    private SparseArray<Point> mTouchingFingers = new SparseArray<>();
    private float mTrigerStartThreshold = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
    private ContentObserver mTripleFingerMotionModeObserver = new ContentObserver(new Handler()) {
        /* class com.android.server.input.HwFingersSnapshooter.AnonymousClass4 */

        @Override // android.database.ContentObserver
        public void onChange(boolean isSelfChange) {
            HwFingersSnapshooter hwFingersSnapshooter = HwFingersSnapshooter.this;
            hwFingersSnapshooter.mEnabled = SettingsEx.System.getIntForUser(hwFingersSnapshooter.mContext.getContentResolver(), HwFingersSnapshooter.KEY_TRIPLE_FINGER_MOTION, 0, -2);
            Log.i(HwFingersSnapshooter.TAG, "mTripleFingerMotionModeObserver mEnabled = " + HwFingersSnapshooter.this.mEnabled);
        }
    };

    /* access modifiers changed from: private */
    public static class Point {
        private float mMoveY = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
        private long mTime;
        private float mX;
        private float mY;

        Point(float px, float py, long time) {
            this.mX = px;
            this.mY = py;
            this.mTime = time;
        }

        public void updateMoveDistance(float px, float py) {
            this.mMoveY = py - this.mY;
        }

        public String toString() {
            return "(" + this.mX + AwarenessInnerConstants.COMMA_KEY + this.mY + ")";
        }
    }

    /* JADX WARN: Type inference failed for: r2v4, types: [android.os.Handler, com.android.server.input.HwFingersSnapshooter$FingerSnapHandler] */
    /* JADX WARNING: Unknown variable types count: 1 */
    public HwFingersSnapshooter(Context context, HwInputManagerService inputManager) {
        Log.i(TAG, "HwFingersSnapshooter constructor");
        this.mContext = context;
        this.mIm = inputManager;
        HandlerThread handlerThread = new HandlerThread(TAG);
        handlerThread.start();
        this.mHandler = new FingerSnapHandler(handlerThread.getLooper());
        this.mHandler.sendEmptyMessage(0);
        this.mHeight = (float) this.mContext.getResources().getDisplayMetrics().heightPixels;
    }

    public boolean handleMotionEvent(InputEvent event) {
        if (this.mEnabled == 0) {
            return true;
        }
        if (event instanceof MotionEvent) {
            MotionEvent motionEvent = (MotionEvent) event;
            int action = motionEvent.getActionMasked();
            int id = motionEvent.getPointerId(motionEvent.getActionIndex());
            long time = motionEvent.getEventTime();
            if (action == 0) {
                resetState();
                Point down = new Point(motionEvent.getRawX(), motionEvent.getRawY(), time);
                this.mTouchingFingers.put(id, down);
                Log.i(TAG, "handleMotionEvent first finger(" + id + ") touch down at " + down);
            } else if (action == 1) {
                Log.i(TAG, "handleMotionEvent last finger(" + id + ") up.");
                resetState();
            } else if (action != 2) {
                if (action == 5) {
                    this.mIsCanFilter = handleFingerDown(motionEvent);
                } else if (action != 6) {
                    Log.e(TAG, "Invalid motionevent");
                } else {
                    handleFingerUp(motionEvent);
                    this.mTouchingFingers.delete(id);
                    this.mIsFilterCurrentTouch = false;
                }
            } else if (this.mIsCanFilter) {
                handleFingerMove(motionEvent);
            }
            return true ^ this.mIsFilterCurrentTouch;
        }
        Log.i(TAG, "handleMotionEvent not a motionEvent");
        return true;
    }

    private void resetState() {
        this.mIsFilterCurrentTouch = false;
        this.mIsCanFilter = false;
        this.mMaxY = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
        this.mTouchingFingers.clear();
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
        if (fingerSize == 3) {
            return handleTriggerMinFingers(motionEvent);
        }
        Log.i(TAG, "handleFingerDown " + fingerSize + " fingers touching down");
        return false;
    }

    private boolean handleTriggerMinFingers(MotionEvent motionEvent) {
        float distanceY = getDistanceY();
        if (this.mMaxFingerDownyDistance < distanceY) {
            Flog.bdReport(991310143, "{distance:" + pixelsToDips(distanceY) + RIGHT_BRACKETS);
            Log.i(TAG, "errorState:the fingers' position faraway on Y dimension! EventId:991310143");
            return false;
        }
        if (this.mMaxFingerDownxDistance < getDistanceX()) {
            Log.i(TAG, "errorState:the fingers' position faraway on X dimension!");
            return false;
        }
        long interval = getInterval();
        if (((float) interval) > MAX_FINGER_DOWN_INTERVAL) {
            Flog.bdReport(991310144, "{interval:" + interval + RIGHT_BRACKETS);
            Log.i(TAG, "errorState:fingers'interval longer than except time! EventId:991310144");
            return false;
        } else if (canDisableGesture()) {
            return false;
        } else {
            if (SystemPropertiesEx.getBoolean(GestureNavConst.KEY_SUPER_SAVE_MODE, false)) {
                Log.i(TAG, "can not take screen shot in super power mode!");
                return false;
            } else if (!isUserUnlocked()) {
                Log.i(TAG, "now isRestrictAsEncrypt, do not allow to take screenshot");
                return false;
            } else {
                startHandleTripleFingerSnap(motionEvent);
                if (this.mMaxY + this.mMinyTriggerDistance > this.mHeight) {
                    Log.i(TAG, "errorState:the remaining distance is not enough on Y dimension!");
                    this.mMaxY = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
                    return false;
                }
                Flog.bdReport(991310140);
                Log.i(TAG, "handleFingerDown, this finger may trigger the 3-finger snapshot. EventId:991310140");
                return true;
            }
        }
    }

    private boolean isUserUnlocked() {
        return UserManagerExt.isUserUnlocked((UserManager) this.mContext.getSystemService("user"), ActivityManagerEx.getCurrentUser());
    }

    private float getDistanceY() {
        float maxY = Float.MIN_VALUE;
        float minY = Float.MAX_VALUE;
        int fingerSize = this.mTouchingFingers.size();
        for (int i = 0; i < fingerSize; i++) {
            Point point = this.mTouchingFingers.valueAt(i);
            if (point.mY < minY) {
                minY = point.mY;
            }
            if (point.mY > maxY) {
                maxY = point.mY;
            }
        }
        this.mMaxY = maxY;
        Log.i(TAG, "getDistance maxY = " + maxY + ", minY = " + minY);
        return maxY - minY;
    }

    private float getDistanceX() {
        float maxX = Float.MIN_VALUE;
        float minX = Float.MAX_VALUE;
        int fingerSize = this.mTouchingFingers.size();
        for (int i = 0; i < fingerSize; i++) {
            Point point = this.mTouchingFingers.valueAt(i);
            if (point.mX < minX) {
                minX = point.mX;
            }
            if (point.mX > maxX) {
                maxX = point.mX;
            }
        }
        Log.i(TAG, "getDistance maxX = " + maxX + ", minX = " + minX);
        return maxX - minX;
    }

    private long getInterval() {
        long startTime = Long.MAX_VALUE;
        long latestTime = Long.MIN_VALUE;
        int fingerSize = this.mTouchingFingers.size();
        for (int i = 0; i < fingerSize; i++) {
            Point point = this.mTouchingFingers.valueAt(i);
            if (point.mTime < startTime) {
                startTime = point.mTime;
            }
            if (point.mTime > latestTime) {
                latestTime = point.mTime;
            }
        }
        Log.i(TAG, "getInterval interval = " + (latestTime - startTime));
        return latestTime - startTime;
    }

    private void startHandleTripleFingerSnap(MotionEvent motionEvent) {
        this.mPolicy = WindowManagerPolicyEx.getInstance().getHwPhoneWindowManager();
        HwPhoneWindowManager hwPhoneWindowManager = this.mPolicy;
        if (hwPhoneWindowManager != null) {
            if (hwPhoneWindowManager.isLandscape()) {
                this.mMinyTriggerDistance = this.mMinyTriggerLandscapeDistance;
            } else {
                this.mMinyTriggerDistance = this.mMinyTriggerPortraitDistance;
            }
            this.mHeight = (float) this.mContext.getResources().getDisplayMetrics().heightPixels;
            Log.i(TAG, "handleFingerDown mMinyTriggerDistance:" + this.mMinyTriggerDistance + ", mHeight:" + this.mHeight);
        }
    }

    private void handleFingerMove(final MotionEvent motionEvent) {
        float offsetY = motionEvent.getY() - motionEvent.getRawY();
        float offsetX = motionEvent.getX() - motionEvent.getRawX();
        int pointerCount = motionEvent.getPointerCount();
        int moveCount = 0;
        for (int i = 0; i < pointerCount; i++) {
            float curY = motionEvent.getY(i) + offsetY;
            float curX = motionEvent.getX(i) + offsetX;
            int id = motionEvent.getPointerId(i);
            Point point = this.mTouchingFingers.get(id);
            if (point != null) {
                point.updateMoveDistance(curX, curY);
                if (!this.mIsFilterCurrentTouch && point.mY < curY) {
                    moveCount++;
                    Log.i(TAG, "handleFingerMove finger(" + id + ") moveCount:" + moveCount);
                    if (moveCount == 3) {
                        Log.i(TAG, "handleFingerMove cancel");
                        this.mHandler.post(new Runnable() {
                            /* class com.android.server.input.HwFingersSnapshooter.AnonymousClass5 */

                            @Override // java.lang.Runnable
                            public void run() {
                                MotionEvent cancelEvent = MotionEventEx.copy(motionEvent);
                                cancelEvent.setAction(3);
                                HwFingersSnapshooter.this.mIm.injectInputEvent(cancelEvent, InputManagerEx.getInjectInputEventModeWaitForFinish());
                            }
                        });
                        this.mIsFilterCurrentTouch = true;
                    }
                }
            } else {
                Log.i(TAG, "handleFingerMove point(" + id + ") not tracked!");
            }
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
            Log.i(TAG, "handleFingerUp, current touch has " + fingerSize + " fingers");
            return;
        }
        boolean isTriggerSnapshot = true;
        int i = 0;
        while (true) {
            if (i >= fingerSize) {
                break;
            }
            float moveDistance = this.mTouchingFingers.get(i).mMoveY;
            if (moveDistance < this.mMinyTriggerDistance) {
                Flog.bdReport(991310142, "{moveDistance:" + pixelsToDips(moveDistance) + RIGHT_BRACKETS);
                Log.i(TAG, "errorState:finger(" + i + ") move " + pixelsToDips(moveDistance) + "dp, less than except distance! EventId:991310142");
                isTriggerSnapshot = false;
                break;
            }
            i++;
        }
        if (isTriggerSnapshot) {
            resetState();
            Flog.bdReport(this.mContext, 991310141);
            Log.i(TAG, "trigger the snapshot! EventId:991310141");
            this.mHandler.post(this.mScreenshotRunnable);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleFingerSnapShooterInit() {
        this.mDensity = this.mContext.getResources().getDisplayMetrics().density;
        this.mTrigerStartThreshold = (float) ((Context) checkNull("context", this.mContext)).getResources().getDimensionPixelSize(HwPartResourceUtils.getResourceId("status_bar_height"));
        this.mEnabled = SettingsEx.System.getIntForUser(this.mContext.getContentResolver(), KEY_TRIPLE_FINGER_MOTION, 0, -2);
        ContentResolverExt.registerContentObserver(this.mContext.getContentResolver(), Settings.System.getUriFor(KEY_TRIPLE_FINGER_MOTION), true, this.mTripleFingerMotionModeObserver, -1);
        ContextEx.registerReceiverAsUser(this.mContext, this.mFingerSnapReceiver, UserHandleEx.ALL, new IntentFilter(APS_RESOLUTION_CHANGE_ACTION), APS_RESOLUTION_CHANGE_PERSISSIONS, (Handler) null);
        ContextEx.registerReceiverAsUser(this.mContext, this.mFingerSnapReceiver, UserHandleEx.ALL, new IntentFilter(IntentExEx.getActionUserSwitched()), (String) null, (Handler) null);
        this.mPolicy = WindowManagerPolicyEx.getInstance().getHwPhoneWindowManager();
        this.mMaxFingerDownyDistance = dipsToPixels(MAX_FINGER_DOWN_Y_DISTANCE);
        this.mMaxFingerDownxDistance = dipsToPixels(MAX_FINGER_DOWN_X_DISTANCE);
        this.mMinyTriggerLandscapeDistance = dipsToPixels(MIN_Y_TRIGGER_LANDSCAPE_DISTANCE);
        this.mMinyTriggerPortraitDistance = dipsToPixels(MIN_Y_TRIGGER_PORTRAIT_DISTANCE);
        Log.i(TAG, "mTrigerStartThreshold:" + this.mTrigerStartThreshold + ", mEnabled:" + this.mEnabled);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleUserSwitch(int userId) {
        this.mEnabled = SettingsEx.System.getIntForUser(this.mContext.getContentResolver(), KEY_TRIPLE_FINGER_MOTION, 0, -2);
        Log.i(TAG, "onReceive ACTION_USER_SWITCHED currentUserId= " + userId + ", mEnabled:" + this.mEnabled);
    }

    private final class FingerSnapHandler extends HandlerAdapter {
        FingerSnapHandler(Looper looper) {
            super(looper, (Handler.Callback) null, true);
        }

        public void handleMessage(Message msg) {
            int i = msg.what;
            if (i == 0) {
                HwFingersSnapshooter.this.handleFingerSnapShooterInit();
            } else if (i == 1) {
                HwFingersSnapshooter.this.handleUserSwitch(msg.arg1);
            } else if (i != 2) {
                Log.e(HwFingersSnapshooter.TAG, "Invalid message");
            } else {
                removeMessages(2);
                if (!DecisionUtil.bindServiceToAidsEngine(HwFingersSnapshooter.this.mContext, HwFingersSnapshooter.SCREEN_SHOT_EVENT_NAME)) {
                    Log.i(HwFingersSnapshooter.TAG, "bindServiceToAidsEngine error");
                }
            }
        }
    }

    private static <T> T checkNull(String name, T arg) {
        if (arg != null) {
            return arg;
        }
        throw new IllegalArgumentException(name + " must not be null");
    }

    /* access modifiers changed from: package-private */
    public final float dipsToPixels(float dips) {
        return (this.mDensity * dips) + UPWARD_DIFF;
    }

    /* access modifiers changed from: package-private */
    public final float pixelsToDips(float pixels) {
        return (pixels / this.mDensity) + UPWARD_DIFF;
    }

    private boolean canDisableGesture() {
        if (inInValidArea()) {
            Log.i(TAG, "inInvalidarea");
            return true;
        } else if (SystemPropertiesEx.getBoolean(TALKBACK_CONFIG, true) && isTalkBackServicesOn()) {
            Log.i(TAG, "in talkback mode");
            return true;
        } else if (SystemPropertiesEx.getBoolean("runtime.mmitest.isrunning", false)) {
            Log.i(TAG, "in MMI test");
            return true;
        } else if (isTripleGameDisabled()) {
            Log.i(TAG, "game space disable triple finger");
            return true;
        } else if (isDeviceProvisioned()) {
            return false;
        } else {
            Log.i(TAG, "Device has not been provisioned");
            return true;
        }
    }

    private boolean isTripleGameDisabled() {
        return SettingsEx.Secure.getIntForUser(this.mContext.getContentResolver(), GAME_TRIPLE_FINGER, 2, ActivityManagerEx.getCurrentUser()) == 1 && ActivityManagerEx.isGameDndOn();
    }

    private boolean isTalkBackServicesOn() {
        Context context = this.mContext;
        boolean isScreenReaderEnabled = false;
        if (context == null) {
            return false;
        }
        if (SettingsEx.Secure.getIntForUser(context.getContentResolver(), ACCESSIBILITY_SCREENREADER_ENABLED, 0, -2) == 1) {
            isScreenReaderEnabled = true;
        }
        Log.i(TAG, "isScreenReaderEnabled : " + isScreenReaderEnabled);
        return isScreenReaderEnabled;
    }

    private boolean isDeviceProvisioned() {
        if (!this.mIsDeviceProvisioned) {
            boolean z = false;
            if (Settings.Global.getInt(this.mContext.getContentResolver(), "device_provisioned", 0) != 0) {
                z = true;
            }
            this.mIsDeviceProvisioned = z;
        }
        return this.mIsDeviceProvisioned;
    }

    private boolean inInValidArea() {
        int fingerSize = this.mTouchingFingers.size();
        for (int i = 0; i < fingerSize; i++) {
            if (this.mTrigerStartThreshold >= this.mTouchingFingers.valueAt(i).mY) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void takeScreenshot() {
        synchronized (this.mScreenshotLock) {
            if (this.mScreenshotConnection == null) {
                ComponentName cn = new ComponentName("com.android.systemui", "com.android.systemui.screenshot.TakeScreenshotService");
                Intent intent = new Intent();
                intent.setComponent(cn);
                ServiceConnection conn = new ServiceConnection() {
                    /* class com.android.server.input.HwFingersSnapshooter.AnonymousClass6 */

                    @Override // android.content.ServiceConnection
                    public void onServiceConnected(ComponentName name, IBinder service) {
                        synchronized (HwFingersSnapshooter.this.mScreenshotLock) {
                            if (HwFingersSnapshooter.this.mScreenshotConnection == this) {
                                Messenger messenger = new Messenger(service);
                                Message msg = Message.obtain((Handler) null, 1);
                                msg.replyTo = new Messenger(new Handler(HwFingersSnapshooter.this.mHandler.getLooper()) {
                                    /* class com.android.server.input.HwFingersSnapshooter.AnonymousClass6.AnonymousClass1 */

                                    @Override // android.os.Handler
                                    public void handleMessage(Message msg) {
                                        synchronized (HwFingersSnapshooter.this.mScreenshotLock) {
                                            if (HwFingersSnapshooter.this.mScreenshotConnection == this) {
                                                HwFingersSnapshooter.this.mContext.unbindService(HwFingersSnapshooter.this.mScreenshotConnection);
                                                HwFingersSnapshooter.this.mScreenshotConnection = null;
                                                HwFingersSnapshooter.this.mHandler.removeCallbacks(HwFingersSnapshooter.this.mScreenshotTimeout);
                                            }
                                        }
                                    }
                                });
                                msg.arg1 = 0;
                                msg.arg2 = 0;
                                try {
                                    messenger.send(msg);
                                } catch (RemoteException e) {
                                    Log.e(HwFingersSnapshooter.TAG, "takeScreenshot: sending msg occured an error");
                                }
                            }
                        }
                    }

                    @Override // android.content.ServiceConnection
                    public void onServiceDisconnected(ComponentName name) {
                    }
                };
                if (ContextEx.bindServiceAsUser(this.mContext, intent, conn, 1, UserHandleEx.CURRENT)) {
                    this.mScreenshotConnection = conn;
                    this.mHandler.postDelayed(this.mScreenshotTimeout, INIT_DELAY_TIME);
                }
            }
        }
    }
}
