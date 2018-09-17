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
import android.provider.Settings.Secure;
import android.provider.Settings.System;
import android.util.Flog;
import android.util.Log;
import android.util.SparseArray;
import android.view.InputEvent;
import android.view.MotionEvent;
import android.view.WindowManagerPolicy;
import com.android.server.LocalServices;
import com.android.server.policy.HwPhoneWindowManager;
import com.android.server.rms.iaware.cpu.CPUFeature;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;

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
    private static final String TAG = "HwFingersSnapshooter";
    private static final String TALKBACK_COMPONENT_NAME = "com.google.android.marvin.talkback/com.google.android.marvin.talkback.TalkBackService";
    private static final String TALKBACK_CONFIG = "ro.config.hw_talkback_btn";
    private static final int TRIGGER_MIN_FINGERS = 3;
    private static final int TRIPLE_FINGER_MOTION_OFF = 0;
    private static final int TRIPLE_FINGER_MOTION_ON = 1;
    private boolean mCanFilter = false;
    private Context mContext;
    private float mDensity = 2.0f;
    private int mEnabled;
    private boolean mFilterCurrentTouch = false;
    private BroadcastReceiver mFingerSnapReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                Log.i(HwFingersSnapshooter.TAG, "onReceive intent action = " + intent.getAction());
                if (intent.getAction() != null) {
                    if (HwFingersSnapshooter.APS_RESOLUTION_CHANGE_ACTION.equals(intent.getAction())) {
                        HwFingersSnapshooter.this.mDensity = HwFingersSnapshooter.this.mContext.getResources().getDisplayMetrics().density;
                        HwFingersSnapshooter.this.mMaxFingerDownYDistance = HwFingersSnapshooter.this.dipsToPixels(HwFingersSnapshooter.MAX_FINGER_DOWN_Y_DISTANCE);
                        HwFingersSnapshooter.this.mMaxFingerDownXDistance = HwFingersSnapshooter.this.dipsToPixels(HwFingersSnapshooter.MAX_FINGER_DOWN_X_DISTANCE);
                        HwFingersSnapshooter.this.mMinYTriggerLandscapeDistance = HwFingersSnapshooter.this.dipsToPixels(HwFingersSnapshooter.MIN_Y_TRIGGER_LANDSCAPE_DISTANCE);
                        HwFingersSnapshooter.this.mMinYTriggerPortraitDistance = HwFingersSnapshooter.this.dipsToPixels(HwFingersSnapshooter.MIN_Y_TRIGGER_PORTRAIT_DISTANCE);
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
    private Handler mHandler;
    private float mHeight = 0.0f;
    private HwInputManagerService mIm = null;
    private float mMaxFingerDownXDistance = 0.0f;
    private float mMaxFingerDownYDistance = 0.0f;
    private float mMaxY = 0.0f;
    private float mMinYTriggerDistance = 0.0f;
    private float mMinYTriggerLandscapeDistance = 0.0f;
    private float mMinYTriggerPortraitDistance = 0.0f;
    private HwPhoneWindowManager mPolicy;
    ServiceConnection mScreenshotConnection = null;
    final Object mScreenshotLock = new Object();
    private final Runnable mScreenshotRunnable = new Runnable() {
        public void run() {
            HwFingersSnapshooter.this.takeScreenshot();
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
    private SparseArray<Point> mTouchingFingers = new SparseArray();
    private float mTrigerStartThreshold = 0.0f;
    private ContentObserver mTripleFingerMotionModeObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean selfChange) {
            HwFingersSnapshooter.this.mEnabled = System.getIntForUser(HwFingersSnapshooter.this.mContext.getContentResolver(), HwFingersSnapshooter.KEY_TRIPLE_FINGER_MOTION, 0, -2);
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
                default:
                    Log.e(HwFingersSnapshooter.TAG, "Invalid message");
                    return;
            }
        }
    }

    private static class Point {
        public float moveY = 0.0f;
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
            return this.mFilterCurrentTouch ^ 1;
        }
        Log.i(TAG, "handleMotionEvent not a motionEvent");
        return true;
    }

    private void resetState() {
        this.mFilterCurrentTouch = false;
        this.mCanFilter = false;
        this.mMaxY = 0.0f;
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
        if (fingerSize != 3) {
            Log.i(TAG, "handleFingerDown " + fingerSize + " fingers touching down");
            return false;
        }
        float distanceY = getDistanceY();
        if (distanceY > this.mMaxFingerDownYDistance) {
            Log.i(TAG, "errorState:the fingers' position faraway on Y dimension! EventId:143 ret:" + Flog.bdReport(this.mContext, CPUFeature.MSG_SET_VIP_THREAD, "{distance:" + pixelsToDips(distanceY) + "}"));
            return false;
        } else if (getDistanceX() > this.mMaxFingerDownXDistance) {
            Log.i(TAG, "errorState:the fingers' position faraway on X dimension!");
            return false;
        } else {
            long interval = getInterval();
            if (((float) interval) > MAX_FINGER_DOWN_INTERVAL) {
                Log.i(TAG, "errorState:fingers'interval longer than except time! EventId:144 ret:" + Flog.bdReport(this.mContext, CPUFeature.MSG_RESET_VIP_THREAD, "{interval:" + interval + "}"));
                return false;
            } else if (canDisableGesture()) {
                return false;
            } else {
                if (SystemProperties.getBoolean("sys.super_power_save", false)) {
                    Log.i(TAG, "can not take screen shot in super power mode!");
                    return false;
                } else if (isUserUnlocked()) {
                    startHandleTripleFingerSnap(motionEvent);
                    if (this.mMaxY + this.mMinYTriggerDistance > this.mHeight) {
                        Log.i(TAG, "errorState:the remaining distance is not enough on Y dimension!");
                        this.mMaxY = 0.0f;
                        return false;
                    }
                    Log.i(TAG, "handleFingerDown, this finger may trigger the 3-finger snapshot. EventId:140 ret:" + Flog.bdReport(this.mContext, CPUFeature.MSG_CPUCTL_SUBSWITCH));
                    return true;
                } else {
                    Log.i(TAG, "now isRestrictAsEncrypt, do not allow to take screenshot");
                    return false;
                }
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
            Point p = (Point) this.mTouchingFingers.valueAt(i);
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
            Point p = (Point) this.mTouchingFingers.valueAt(i);
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
            Point p = (Point) this.mTouchingFingers.valueAt(i);
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
            Point p = (Point) this.mTouchingFingers.get(id);
            if (p != null) {
                p.updateMoveDistance(curX, curY);
                if (!(this.mFilterCurrentTouch || curY == p.y)) {
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
        Log.i(TAG, "handleFingerUp finger(" + motionEvent.getPointerId(motionEvent.getActionIndex()) + ") up");
        if (this.mFilterCurrentTouch) {
            int fingerSize = this.mTouchingFingers.size();
            if (fingerSize != 3) {
                Log.i(TAG, "handleFingerUp, current touch has " + fingerSize + " fingers");
                return;
            }
            boolean triggerSnapshot = true;
            for (int i = 0; i < fingerSize; i++) {
                float moveDistance = ((Point) this.mTouchingFingers.get(i)).moveY;
                if (moveDistance < this.mMinYTriggerDistance) {
                    Log.i(TAG, "errorState:finger(" + i + ") move " + pixelsToDips(moveDistance) + "dp, less than except distance! EventId:" + CPUFeature.MSG_SET_BG_UIDS + " ret:" + Flog.bdReport(this.mContext, CPUFeature.MSG_SET_BG_UIDS, "{moveDistance:" + pixelsToDips(moveDistance) + "}"));
                    triggerSnapshot = false;
                    break;
                }
            }
            if (triggerSnapshot) {
                resetState();
                Log.i(TAG, "trigger the snapshot! EventId:141 ret:" + Flog.bdReport(this.mContext, CPUFeature.MSG_SET_FG_UIDS));
                this.mHandler.post(this.mScreenshotRunnable);
            }
            return;
        }
        Log.i(TAG, "handleFingerUp, current touch not marked as filter");
    }

    private void handleFingerSnapShooterInit() {
        this.mDensity = this.mContext.getResources().getDisplayMetrics().density;
        this.mTrigerStartThreshold = (float) ((Context) checkNull("context", this.mContext)).getResources().getDimensionPixelSize(17105234);
        this.mEnabled = System.getIntForUser(this.mContext.getContentResolver(), KEY_TRIPLE_FINGER_MOTION, 0, -2);
        this.mContext.getContentResolver().registerContentObserver(System.getUriFor(KEY_TRIPLE_FINGER_MOTION), true, this.mTripleFingerMotionModeObserver, -1);
        this.mContext.registerReceiverAsUser(this.mFingerSnapReceiver, UserHandle.ALL, new IntentFilter(APS_RESOLUTION_CHANGE_ACTION), APS_RESOLUTION_CHANGE_PERSISSIONS, null);
        this.mContext.registerReceiverAsUser(this.mFingerSnapReceiver, UserHandle.ALL, new IntentFilter("android.intent.action.USER_SWITCHED"), null, null);
        this.mPolicy = (HwPhoneWindowManager) LocalServices.getService(WindowManagerPolicy.class);
        this.mMaxFingerDownYDistance = dipsToPixels(MAX_FINGER_DOWN_Y_DISTANCE);
        this.mMaxFingerDownXDistance = dipsToPixels(MAX_FINGER_DOWN_X_DISTANCE);
        this.mMinYTriggerLandscapeDistance = dipsToPixels(MIN_Y_TRIGGER_LANDSCAPE_DISTANCE);
        this.mMinYTriggerPortraitDistance = dipsToPixels(MIN_Y_TRIGGER_PORTRAIT_DISTANCE);
        Log.i(TAG, "mTrigerStartThreshold:" + this.mTrigerStartThreshold + ", mEnabled:" + this.mEnabled);
    }

    private void handleUserSwitch(int userId) {
        this.mEnabled = System.getIntForUser(this.mContext.getContentResolver(), KEY_TRIPLE_FINGER_MOTION, 0, -2);
        Log.i(TAG, "onReceive ACTION_USER_SWITCHED  currentUserId= " + userId + ", mEnabled:" + this.mEnabled);
    }

    private static <T> T checkNull(String name, T arg) {
        if (arg != null) {
            return arg;
        }
        throw new IllegalArgumentException(name + " must not be null");
    }

    final float dipsToPixels(float dips) {
        return (this.mDensity * dips) + 0.5f;
    }

    final float pixelsToDips(float pixels) {
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
        if (this.mContext == null) {
            return false;
        }
        boolean accessibilityEnabled = Secure.getIntForUser(this.mContext.getContentResolver(), "accessibility_enabled", 0, -2) == 1;
        String enabledSerices = Secure.getStringForUser(this.mContext.getContentResolver(), "enabled_accessibility_services", -2);
        boolean isContainsTalkBackService = enabledSerices != null ? enabledSerices.contains(TALKBACK_COMPONENT_NAME) : false;
        Log.i(TAG, "accessibilityEnabled:" + accessibilityEnabled + ",isContainsTalkBackService:" + isContainsTalkBackService);
        if (!accessibilityEnabled) {
            isContainsTalkBackService = false;
        }
        return isContainsTalkBackService;
    }

    private boolean inInValidArea() {
        int fingerSize = this.mTouchingFingers.size();
        for (int i = 0; i < fingerSize; i++) {
            if (((Point) this.mTouchingFingers.valueAt(i)).y <= this.mTrigerStartThreshold) {
                return true;
            }
        }
        return false;
    }

    /* JADX WARNING: Missing block: B:12:0x0038, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void takeScreenshot() {
        synchronized (this.mScreenshotLock) {
            if (this.mScreenshotConnection != null) {
                return;
            }
            ComponentName cn = new ComponentName("com.android.systemui", "com.android.systemui.screenshot.TakeScreenshotService");
            Intent intent = new Intent();
            intent.setComponent(cn);
            ServiceConnection conn = new ServiceConnection() {
                public void onServiceConnected(ComponentName name, IBinder service) {
                    synchronized (HwFingersSnapshooter.this.mScreenshotLock) {
                        if (HwFingersSnapshooter.this.mScreenshotConnection != this) {
                            return;
                        }
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
