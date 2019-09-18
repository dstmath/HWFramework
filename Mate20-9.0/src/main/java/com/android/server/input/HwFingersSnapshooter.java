package com.android.server.input;

import android.app.ActivityManager;
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
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings;
import android.util.Flog;
import android.util.Log;
import android.util.SparseArray;
import android.view.InputEvent;
import android.view.MotionEvent;
import com.android.server.LocalServices;
import com.android.server.gesture.GestureNavConst;
import com.android.server.policy.DecisionUtil;
import com.android.server.policy.HwPhoneWindowManager;
import com.android.server.policy.WindowManagerPolicy;
import com.android.server.rms.iaware.cpu.CPUFeature;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import huawei.com.android.server.fingerprint.FingerViewController;

public class HwFingersSnapshooter {
    private static final String APS_RESOLUTION_CHANGE_ACTION = "huawei.intent.action.APS_RESOLUTION_CHANGE_ACTION";
    private static final String APS_RESOLUTION_CHANGE_PERSISSIONS = "huawei.intent.permissions.APS_RESOLUTION_CHANGE_ACTION";
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
    private static final String SCREEN_SHOT_EVENT_NAME = "com.huawei.screenshot.intent.action.TripleFingersScreenshot";
    private static final String TAG = "HwFingersSnapshooter";
    private static final String TALKBACK_COMPONENT_NAME = "com.google.android.marvin.talkback/com.google.android.marvin.talkback.TalkBackService";
    private static final String TALKBACK_CONFIG = "ro.config.hw_talkback_btn";
    private static final int TRIGGER_MIN_FINGERS = 3;
    private static final int TRIPLE_FINGER_MOTION_OFF = 0;
    private static final int TRIPLE_FINGER_MOTION_ON = 1;
    private boolean mCanFilter = false;
    /* access modifiers changed from: private */
    public Context mContext;
    /* access modifiers changed from: private */
    public float mDensity = 2.0f;
    /* access modifiers changed from: private */
    public int mEnabled;
    private boolean mFilterCurrentTouch = false;
    private BroadcastReceiver mFingerSnapReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                Log.i(HwFingersSnapshooter.TAG, "onReceive intent action = " + intent.getAction());
                if (intent.getAction() != null) {
                    if (HwFingersSnapshooter.APS_RESOLUTION_CHANGE_ACTION.equals(intent.getAction())) {
                        float unused = HwFingersSnapshooter.this.mDensity = HwFingersSnapshooter.this.mContext.getResources().getDisplayMetrics().density;
                        float unused2 = HwFingersSnapshooter.this.mMaxFingerDownYDistance = HwFingersSnapshooter.this.dipsToPixels(HwFingersSnapshooter.MAX_FINGER_DOWN_Y_DISTANCE);
                        float unused3 = HwFingersSnapshooter.this.mMaxFingerDownXDistance = HwFingersSnapshooter.this.dipsToPixels(HwFingersSnapshooter.MAX_FINGER_DOWN_X_DISTANCE);
                        float unused4 = HwFingersSnapshooter.this.mMinYTriggerLandscapeDistance = HwFingersSnapshooter.this.dipsToPixels(HwFingersSnapshooter.MIN_Y_TRIGGER_LANDSCAPE_DISTANCE);
                        float unused5 = HwFingersSnapshooter.this.mMinYTriggerPortraitDistance = HwFingersSnapshooter.this.dipsToPixels(HwFingersSnapshooter.MIN_Y_TRIGGER_PORTRAIT_DISTANCE);
                        Log.i(HwFingersSnapshooter.TAG, "mDisplayResolutionModeObserver mMaxFingerDownYDistance = " + HwFingersSnapshooter.this.mMaxFingerDownYDistance + ",mMinYTriggerLandscapeDistance:" + HwFingersSnapshooter.this.mMinYTriggerLandscapeDistance + ",mMinYTriggerPortraitDistance:" + HwFingersSnapshooter.this.mMinYTriggerPortraitDistance);
                    } else if ("android.intent.action.USER_SWITCHED".equals(intent.getAction())) {
                        Message message = new Message();
                        message.what = 1;
                        message.arg1 = intent.getIntExtra("android.intent.extra.user_handle", 0);
                        HwFingersSnapshooter.this.mHandler.sendMessage(message);
                    }
                }
            }
        }
    };
    /* access modifiers changed from: private */
    public Handler mHandler;
    private float mHeight = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
    /* access modifiers changed from: private */
    public HwInputManagerService mIm = null;
    /* access modifiers changed from: private */
    public float mMaxFingerDownXDistance = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
    /* access modifiers changed from: private */
    public float mMaxFingerDownYDistance = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
    private float mMaxY = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
    private float mMinYTriggerDistance = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
    /* access modifiers changed from: private */
    public float mMinYTriggerLandscapeDistance = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
    /* access modifiers changed from: private */
    public float mMinYTriggerPortraitDistance = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
    private HwPhoneWindowManager mPolicy;
    ServiceConnection mScreenshotConnection = null;
    final Object mScreenshotLock = new Object();
    private final Runnable mScreenshotRunnable = new Runnable() {
        public void run() {
            HwFingersSnapshooter.this.takeScreenshot();
            HwFingersSnapshooter.this.mHandler.sendEmptyMessageDelayed(2, 15000);
        }
    };
    final Runnable mScreenshotTimeout = new Runnable() {
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
        public void onChange(boolean selfChange) {
            int unused = HwFingersSnapshooter.this.mEnabled = Settings.System.getIntForUser(HwFingersSnapshooter.this.mContext.getContentResolver(), HwFingersSnapshooter.KEY_TRIPLE_FINGER_MOTION, 0, -2);
            Log.i(HwFingersSnapshooter.TAG, "mTripleFingerMotionModeObserver mEnabled = " + HwFingersSnapshooter.this.mEnabled);
        }
    };

    private final class FingerSnapHandler extends Handler {
        public FingerSnapHandler(Looper looper) {
            super(looper, null, true);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    HwFingersSnapshooter.this.handleFingerSnapShooterInit();
                    return;
                case 1:
                    HwFingersSnapshooter.this.handleUserSwitch(msg.arg1);
                    return;
                case 2:
                    removeMessages(2);
                    if (!DecisionUtil.bindServiceToAidsEngine(HwFingersSnapshooter.this.mContext, HwFingersSnapshooter.SCREEN_SHOT_EVENT_NAME)) {
                        Log.i(HwFingersSnapshooter.TAG, "bindServiceToAidsEngine error");
                        return;
                    }
                    return;
                default:
                    Log.e(HwFingersSnapshooter.TAG, "Invalid message");
                    return;
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
            switch (action) {
                case 0:
                    resetState();
                    Point down = new Point(motionEvent.getRawX(), motionEvent.getRawY(), time);
                    this.mTouchingFingers.put(id, down);
                    Log.i(TAG, "handleMotionEvent first finger(" + id + ") touch down at " + down);
                    break;
                case 1:
                    Log.i(TAG, "handleMotionEvent last finger(" + id + ") up ");
                    resetState();
                    break;
                case 2:
                    if (this.mCanFilter) {
                        handleFingerMove(motionEvent);
                        break;
                    }
                    break;
                case 5:
                    this.mCanFilter = handleFingerDown(motionEvent);
                    break;
                case 6:
                    handleFingerUp(motionEvent);
                    this.mTouchingFingers.delete(id);
                    this.mFilterCurrentTouch = false;
                    break;
                default:
                    Log.e(TAG, "Invalid motionevent");
                    break;
            }
            return true ^ this.mFilterCurrentTouch;
        }
        Log.i(TAG, "handleMotionEvent not a motionEvent");
        return true;
    }

    private void resetState() {
        this.mFilterCurrentTouch = false;
        this.mCanFilter = false;
        this.mMaxY = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
        this.mTouchingFingers.clear();
    }

    private boolean handleFingerDown(MotionEvent motionEvent) {
        MotionEvent motionEvent2 = motionEvent;
        float offsetX = motionEvent.getRawX() - motionEvent.getX();
        float offsetY = motionEvent.getRawY() - motionEvent.getY();
        int actionIndex = motionEvent.getActionIndex();
        int id = motionEvent2.getPointerId(actionIndex);
        Point pointerDown = new Point(motionEvent2.getX(actionIndex) + offsetX, motionEvent2.getY(actionIndex) + offsetY, motionEvent.getEventTime());
        this.mTouchingFingers.put(id, pointerDown);
        int fingerSize = this.mTouchingFingers.size();
        Log.i(TAG, "handleFingerDown new finger(" + id + ") touch down at " + pointerDown + ",size:" + fingerSize);
        if (fingerSize != 3) {
            Log.i(TAG, "handleFingerDown " + fingerSize + " fingers touching down");
            return false;
        }
        float distanceY = getDistanceY();
        if (distanceY > this.mMaxFingerDownYDistance) {
            Context context = this.mContext;
            boolean ret = Flog.bdReport(context, CPUFeature.MSG_SET_VIP_THREAD, "{distance:" + pixelsToDips(distanceY) + "}");
            StringBuilder sb = new StringBuilder();
            sb.append("errorState:the fingers' position faraway on Y dimension! EventId:143 ret:");
            sb.append(ret);
            Log.i(TAG, sb.toString());
            return false;
        } else if (getDistanceX() > this.mMaxFingerDownXDistance) {
            Log.i(TAG, "errorState:the fingers' position faraway on X dimension!");
            return false;
        } else {
            long interval = getInterval();
            if (((float) interval) > MAX_FINGER_DOWN_INTERVAL) {
                Context context2 = this.mContext;
                StringBuilder sb2 = new StringBuilder();
                float f = offsetX;
                sb2.append("{interval:");
                sb2.append(interval);
                sb2.append("}");
                boolean ret2 = Flog.bdReport(context2, CPUFeature.MSG_RESET_VIP_THREAD, sb2.toString());
                StringBuilder sb3 = new StringBuilder();
                float f2 = offsetY;
                sb3.append("errorState:fingers'interval longer than except time! EventId:144 ret:");
                sb3.append(ret2);
                Log.i(TAG, sb3.toString());
                return false;
            }
            float f3 = offsetY;
            if (canDisableGesture()) {
                return false;
            }
            if (SystemProperties.getBoolean("sys.super_power_save", false)) {
                Log.i(TAG, "can not take screen shot in super power mode!");
                return false;
            } else if (!isUserUnlocked()) {
                Log.i(TAG, "now isRestrictAsEncrypt, do not allow to take screenshot");
                return false;
            } else {
                startHandleTripleFingerSnap(motionEvent);
                if (this.mMaxY + this.mMinYTriggerDistance > this.mHeight) {
                    Log.i(TAG, "errorState:the remaining distance is not enough on Y dimension!");
                    this.mMaxY = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
                    return false;
                }
                boolean ret3 = Flog.bdReport(this.mContext, 140);
                Log.i(TAG, "handleFingerDown, this finger may trigger the 3-finger snapshot. EventId:140 ret:" + ret3);
                return true;
            }
        }
    }

    private boolean isUserUnlocked() {
        return ((UserManager) this.mContext.getSystemService("user")).isUserUnlocked(ActivityManager.getCurrentUser());
    }

    private float getDistanceY() {
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
        this.mMaxY = maxY;
        Log.i(TAG, "getDistance maxY = " + maxY + ", minY = " + minY);
        return maxY - minY;
    }

    private float getDistanceX() {
        float maxX = Float.MIN_VALUE;
        float minX = Float.MAX_VALUE;
        int fingerSize = this.mTouchingFingers.size();
        for (int i = 0; i < fingerSize; i++) {
            Point p = this.mTouchingFingers.valueAt(i);
            if (p.x < minX) {
                minX = p.x;
            }
            if (p.x > maxX) {
                maxX = p.x;
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
            Point p = this.mTouchingFingers.valueAt(i);
            if (p.time < startTime) {
                startTime = p.time;
            }
            if (p.time > latestTime) {
                latestTime = p.time;
            }
        }
        Log.i(TAG, "getInterval interval = " + (latestTime - startTime));
        return latestTime - startTime;
    }

    private void startHandleTripleFingerSnap(MotionEvent motionEvent) {
        if (this.mPolicy == null) {
            this.mPolicy = (HwPhoneWindowManager) LocalServices.getService(WindowManagerPolicy.class);
        }
        if (this.mPolicy.isLandscape()) {
            this.mMinYTriggerDistance = this.mMinYTriggerLandscapeDistance;
        } else {
            this.mMinYTriggerDistance = this.mMinYTriggerPortraitDistance;
        }
        this.mHeight = (float) this.mContext.getResources().getDisplayMetrics().heightPixels;
        Log.i(TAG, "handleFingerDown mMinYTriggerDistance:" + this.mMinYTriggerDistance + ", mHeight:" + this.mHeight);
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
            Point p = this.mTouchingFingers.get(id);
            if (p != null) {
                p.updateMoveDistance(curX, curY);
                if (!this.mFilterCurrentTouch && curY != p.y) {
                    moveCount++;
                    Log.i(TAG, "handleFingerMove finger(" + id + ") moveCount:" + moveCount);
                    if (moveCount == 3) {
                        Log.i(TAG, "handleFingerMove cancel");
                        this.mHandler.post(new Runnable() {
                            public void run() {
                                MotionEvent cancelEvent = motionEvent.copy();
                                cancelEvent.setAction(3);
                                HwFingersSnapshooter.this.mIm.injectInputEvent(cancelEvent, 2);
                            }
                        });
                        this.mFilterCurrentTouch = true;
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
        if (!this.mFilterCurrentTouch) {
            Log.i(TAG, "handleFingerUp, current touch not marked as filter");
            return;
        }
        int fingerSize = this.mTouchingFingers.size();
        if (fingerSize != 3) {
            Log.i(TAG, "handleFingerUp, current touch has " + fingerSize + " fingers");
            return;
        }
        boolean triggerSnapshot = true;
        int i = 0;
        while (true) {
            if (i >= fingerSize) {
                break;
            }
            float moveDistance = this.mTouchingFingers.get(i).moveY;
            if (moveDistance < this.mMinYTriggerDistance) {
                Context context = this.mContext;
                boolean ret = Flog.bdReport(context, CPUFeature.MSG_SET_BG_UIDS, "{moveDistance:" + pixelsToDips(moveDistance) + "}");
                Log.i(TAG, "errorState:finger(" + i + ") move " + pixelsToDips(moveDistance) + "dp, less than except distance! EventId:" + CPUFeature.MSG_SET_BG_UIDS + " ret:" + ret);
                triggerSnapshot = false;
                break;
            }
            i++;
        }
        if (triggerSnapshot) {
            resetState();
            boolean ret2 = Flog.bdReport(this.mContext, 141);
            Log.i(TAG, "trigger the snapshot! EventId:141 ret:" + ret2);
            this.mHandler.post(this.mScreenshotRunnable);
        }
    }

    /* access modifiers changed from: private */
    public void handleFingerSnapShooterInit() {
        this.mDensity = this.mContext.getResources().getDisplayMetrics().density;
        this.mTrigerStartThreshold = (float) ((Context) checkNull("context", this.mContext)).getResources().getDimensionPixelSize(17105318);
        this.mEnabled = Settings.System.getIntForUser(this.mContext.getContentResolver(), KEY_TRIPLE_FINGER_MOTION, 0, -2);
        this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor(KEY_TRIPLE_FINGER_MOTION), true, this.mTripleFingerMotionModeObserver, -1);
        this.mContext.registerReceiverAsUser(this.mFingerSnapReceiver, UserHandle.ALL, new IntentFilter(APS_RESOLUTION_CHANGE_ACTION), APS_RESOLUTION_CHANGE_PERSISSIONS, null);
        this.mContext.registerReceiverAsUser(this.mFingerSnapReceiver, UserHandle.ALL, new IntentFilter("android.intent.action.USER_SWITCHED"), null, null);
        this.mPolicy = (HwPhoneWindowManager) LocalServices.getService(WindowManagerPolicy.class);
        this.mMaxFingerDownYDistance = dipsToPixels(MAX_FINGER_DOWN_Y_DISTANCE);
        this.mMaxFingerDownXDistance = dipsToPixels(MAX_FINGER_DOWN_X_DISTANCE);
        this.mMinYTriggerLandscapeDistance = dipsToPixels(MIN_Y_TRIGGER_LANDSCAPE_DISTANCE);
        this.mMinYTriggerPortraitDistance = dipsToPixels(MIN_Y_TRIGGER_PORTRAIT_DISTANCE);
        Log.i(TAG, "mTrigerStartThreshold:" + this.mTrigerStartThreshold + ", mEnabled:" + this.mEnabled);
    }

    /* access modifiers changed from: private */
    public void handleUserSwitch(int userId) {
        this.mEnabled = Settings.System.getIntForUser(this.mContext.getContentResolver(), KEY_TRIPLE_FINGER_MOTION, 0, -2);
        Log.i(TAG, "onReceive ACTION_USER_SWITCHED  currentUserId= " + userId + ", mEnabled:" + this.mEnabled);
    }

    private static <T> T checkNull(String name, T arg) {
        if (arg != null) {
            return arg;
        }
        throw new IllegalArgumentException(name + " must not be null");
    }

    /* access modifiers changed from: package-private */
    public final float dipsToPixels(float dips) {
        return (this.mDensity * dips) + 0.5f;
    }

    /* access modifiers changed from: package-private */
    public final float pixelsToDips(float pixels) {
        return (pixels / this.mDensity) + 0.5f;
    }

    private boolean canDisableGesture() {
        if (inInValidArea()) {
            Log.i(TAG, "inInvalidarea");
            return true;
        } else if (SystemProperties.getBoolean(TALKBACK_CONFIG, true) && isTalkBackServicesOn()) {
            Log.i(TAG, "in talkback mode");
            return true;
        } else if (!SystemProperties.getBoolean("runtime.mmitest.isrunning", false)) {
            return false;
        } else {
            Log.i(TAG, "in MMI test");
            return true;
        }
    }

    private boolean isTalkBackServicesOn() {
        boolean isContainsTalkBackService;
        boolean z = false;
        if (this.mContext == null) {
            return false;
        }
        boolean accessibilityEnabled = Settings.Secure.getIntForUser(this.mContext.getContentResolver(), "accessibility_enabled", 0, -2) == 1;
        String enabledSerices = Settings.Secure.getStringForUser(this.mContext.getContentResolver(), "enabled_accessibility_services", -2);
        Log.i(TAG, "accessibilityEnabled:" + accessibilityEnabled + ",isContainsTalkBackService:" + isContainsTalkBackService);
        if (accessibilityEnabled && isContainsTalkBackService) {
            z = true;
        }
        return z;
    }

    private boolean inInValidArea() {
        int fingerSize = this.mTouchingFingers.size();
        for (int i = 0; i < fingerSize; i++) {
            if (this.mTouchingFingers.valueAt(i).y <= this.mTrigerStartThreshold) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0036, code lost:
        return;
     */
    public void takeScreenshot() {
        synchronized (this.mScreenshotLock) {
            if (this.mScreenshotConnection == null) {
                ComponentName cn = new ComponentName(FingerViewController.PKGNAME_OF_KEYGUARD, "com.android.systemui.screenshot.TakeScreenshotService");
                Intent intent = new Intent();
                intent.setComponent(cn);
                ServiceConnection conn = new ServiceConnection() {
                    public void onServiceConnected(ComponentName name, IBinder service) {
                        synchronized (HwFingersSnapshooter.this.mScreenshotLock) {
                            if (HwFingersSnapshooter.this.mScreenshotConnection == this) {
                                Messenger messenger = new Messenger(service);
                                Message msg = Message.obtain(null, 1);
                                msg.replyTo = new Messenger(new Handler(HwFingersSnapshooter.this.mHandler.getLooper()) {
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
                                msg.arg2 = 0;
                                msg.arg1 = 0;
                                try {
                                    messenger.send(msg);
                                } catch (RemoteException e) {
                                    Log.e(HwFingersSnapshooter.TAG, "takeScreenshot: sending msg occured an error");
                                }
                            }
                        }
                    }

                    public void onServiceDisconnected(ComponentName name) {
                    }
                };
                if (this.mContext.bindServiceAsUser(intent, conn, 1, UserHandle.CURRENT)) {
                    this.mScreenshotConnection = conn;
                    this.mHandler.postDelayed(this.mScreenshotTimeout, MemoryConstant.MIN_INTERVAL_OP_TIMEOUT);
                }
            }
        }
    }
}
