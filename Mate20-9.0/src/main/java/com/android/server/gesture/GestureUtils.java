package com.android.server.gesture;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.PointF;
import android.hardware.input.InputManager;
import android.hardware.input.InputManagerInternal;
import android.os.Process;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.provider.Settings;
import android.util.Log;
import android.util.SparseArray;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import com.android.server.LocalServices;
import com.android.server.am.HwActivityManagerService;
import com.android.server.policy.WindowManagerPolicy;
import com.huawei.android.os.HwVibrator;
import java.util.ArrayList;
import java.util.BitSet;
import vendor.huawei.hardware.hwdisplay.displayengine.V1_0.HighBitsDetailModeID;

public class GestureUtils {
    private static final int DEFAULT_BUTTON_STATE = 0;
    public static final int DEFAULT_DEVICE_ID = 0;
    private static final int DEFAULT_EDGE_FLAGS = 0;
    private static final int DEFAULT_EVENT_FLAGS = 0;
    private static final int DEFAULT_META_STATE = 0;
    private static final float DEFAULT_PRECISION_X = 1.0f;
    private static final float DEFAULT_PRECISION_Y = 1.0f;
    private static final float DEFAULT_PRESSURE_DOWN = 1.0f;
    private static final float DEFAULT_PRESSURE_UP = 0.0f;
    private static final float DEFAULT_SIZE = 1.0f;
    private static final int MOTION_EVENT_INJECTION_DELAY_MILLIS = 5;
    private static final Object gSharedTempLock = new Object();
    private static boolean mHasInit = false;
    private static boolean mHasNotch = false;
    private static boolean mSupportEffectVb = false;
    private static MotionEvent.PointerCoords[] sPointerCoords;
    private static MotionEvent.PointerProperties[] sPointerProps;

    public static final class PointerState {
        public int action;
        public int activePointerId;
        public float x;
        public float y;

        public PointerState(int _activePointerId, int _action, float _x, float _y) {
            this.activePointerId = _activePointerId;
            this.action = _action;
            this.x = _x;
            this.y = _y;
        }
    }

    public static void systemReady() {
        if (!mHasInit) {
            mHasNotch = parseHole();
            mSupportEffectVb = HwVibrator.isSupportHwVibrator("haptic.virtual_navigation.click_back");
            if (GestureNavConst.DEBUG) {
                Log.i(GestureNavConst.TAG_GESTURE_UTILS, "systemReady hasNotch=" + mHasNotch + ", effectVb=" + mSupportEffectVb);
            }
            mHasInit = true;
        }
    }

    public static boolean parseHole() {
        String[] props = SystemProperties.get("ro.config.hw_notch_size", "").split(",");
        if (props == null || props.length != 4) {
            return false;
        }
        Log.d(GestureNavConst.TAG_GESTURE_UTILS, "prop hole height:" + Integer.parseInt(props[1]));
        return true;
    }

    public static boolean hasNotch() {
        return mHasNotch;
    }

    public static boolean isSupportEffectVibrator() {
        return mSupportEffectVb;
    }

    public static int getInputDeviceId(int inputSource) {
        for (int devId : InputDevice.getDeviceIds()) {
            if (InputDevice.getDevice(devId).supportsSource(inputSource)) {
                return devId;
            }
        }
        return 0;
    }

    public static final int getActiveActionIndex(int action) {
        return (65280 & action) >> 8;
    }

    public static final int getActivePointerId(MotionEvent event, int action) {
        return event.getPointerId(getActiveActionIndex(action));
    }

    public static void sendKeyEvent(int keycode) {
        int i;
        if (GestureNavConst.DEBUG) {
            StringBuilder sb = new StringBuilder();
            sb.append("sendKeyEvent keycode=");
            i = keycode;
            sb.append(i);
            Log.i(GestureNavConst.TAG_GESTURE_UTILS, sb.toString());
        } else {
            i = keycode;
        }
        long now = SystemClock.uptimeMillis();
        int[] actions = {0, 1};
        int i2 = 0;
        while (true) {
            int i3 = i2;
            if (i3 < actions.length) {
                KeyEvent ev = new KeyEvent(now, now, actions[i3], i, 0, 0, -1, 0, 8, 257);
                InputManager.getInstance().injectInputEvent(ev, 0);
                i2 = i3 + 1;
                actions = actions;
            } else {
                return;
            }
        }
    }

    public static void sendTap(float x, float y, int deviceId, int source, int toolType) {
        float f;
        float f2;
        if (GestureNavConst.DEBUG) {
            StringBuilder sb = new StringBuilder();
            sb.append("sendTap (");
            f2 = x;
            sb.append(f2);
            sb.append(", ");
            f = y;
            sb.append(f);
            sb.append(")");
            Log.i(GestureNavConst.TAG_GESTURE_UTILS, sb.toString());
        } else {
            f2 = x;
            f = y;
        }
        long downTime = SystemClock.uptimeMillis();
        long j = downTime;
        float f3 = f2;
        float f4 = f;
        int i = deviceId;
        int i2 = source;
        int i3 = toolType;
        injectMotionEvent(0, j, downTime, f3, f4, 1.0f, i, i2, i3);
        injectMotionEvent(1, j, SystemClock.uptimeMillis(), f3, f4, 0.0f, i, i2, i3);
    }

    public static void sendSwipe(float x1, float y1, float x2, float y2, int duration, int deviceId, int source, int toolType, ArrayList<PointF> pendingMovePoints, boolean hasMultiTouched) {
        float f = x1;
        float f2 = y1;
        float f3 = x2;
        float f4 = y2;
        int i = duration;
        ArrayList<PointF> arrayList = pendingMovePoints;
        if (GestureNavConst.DEBUG) {
            Log.i(GestureNavConst.TAG_GESTURE_UTILS, "sendSwipe (" + f + ", " + f2 + ") to (" + f3 + ", " + f4 + "), duration:" + i);
        }
        if (i < 80) {
            i = 80;
        } else if (i > 500) {
            i = 500;
        }
        int duration2 = i;
        long now = SystemClock.uptimeMillis();
        long downTime = now;
        int duration3 = duration2;
        injectMotionEvent(0, downTime, now, f, f2, 1.0f, deviceId, source, toolType);
        long size = 0;
        if (!hasMultiTouched && arrayList != null) {
            long size2 = (long) pendingMovePoints.size();
            size = size2;
            if (size2 > 0) {
                if (GestureNavConst.DEBUG) {
                    Log.d(GestureNavConst.TAG_GESTURE_UTILS, "inject " + size + " pending move points");
                }
                for (int i2 = 0; ((long) i2) < size; i2++) {
                    injectMotionEvent(2, downTime, now, arrayList.get(i2).x, arrayList.get(i2).y, 1.0f, deviceId, source, toolType);
                    SystemClock.sleep(5);
                    now = SystemClock.uptimeMillis();
                }
                long j = size;
                int i3 = duration3;
                float f5 = y1;
                injectMotionEvent(1, downTime, now, f3, f4, 0.0f, deviceId, source, toolType);
            }
        }
        long endTime = downTime + ((long) duration3);
        while (now < endTime) {
            float alpha = ((float) (now - downTime)) / ((float) duration3);
            injectMotionEvent(2, downTime, now, lerp(f, f3, alpha), lerp(y1, f4, alpha), 1.0f, deviceId, source, toolType);
            SystemClock.sleep(5);
            now = SystemClock.uptimeMillis();
            duration3 = duration3;
        }
        float f6 = y1;
        long j2 = size;
        injectMotionEvent(1, downTime, now, f3, f4, 0.0f, deviceId, source, toolType);
    }

    public static void injectMotionEvent(int action, long downTime, long eventTime, float x, float y, int deviceId, int source, int toolType) {
        int i = action;
        injectMotionEvent(i, downTime, eventTime, x, y, i == 1 ? 0.0f : 1.0f, deviceId, source, toolType);
    }

    public static void injectMotionEvent(int action, long downTime, long eventTime, float x, float y, float pressure, int deviceId, int source, int toolType) {
        injectTransferMotionEvent(obtainMotionEvent(downTime, eventTime, action, x, y, pressure, deviceId, source, toolType));
    }

    public static void injectTransferMotionEvent(MotionEvent event) {
        injectMotionEvent(event, 524288);
    }

    public static void injectMotionEvent(MotionEvent event, int appendPolicyFlag) {
        ((InputManagerInternal) LocalServices.getService(InputManagerInternal.class)).injectInputEvent(event, 0, 0, appendPolicyFlag);
    }

    public static void injectDownWithBatchMoveEvent(long downTime, float downX, float downY, ArrayList<PointF> batchMovePoints, long durationTime, int deviceId, int source, int toolType) {
        ArrayList<PointF> arrayList = batchMovePoints;
        MotionEvent event = obtainMotionEvent(downTime, downTime, 0, downX, downY, 1.0f, deviceId, source, toolType);
        int i = 524288;
        if (arrayList != null) {
            int size = batchMovePoints.size();
            int size2 = size;
            if (size > 0) {
                int appendPolicyFlag = 524288 | HighBitsDetailModeID.MODE_FOLIAGE;
                if (GestureNavConst.DEBUG != 0) {
                    Log.d(GestureNavConst.TAG_GESTURE_UTILS, "inject down with " + size2 + " batch move points");
                }
                int i2 = 0;
                while (true) {
                    int i3 = i2;
                    if (i3 >= size2) {
                        break;
                    }
                    event.addBatch(downTime + ((long) (((1.0f * ((float) (i3 + 1))) / ((float) size2)) * ((float) durationTime))), arrayList.get(i3).x, arrayList.get(i3).y, 1.0f, 1.0f, 0);
                    i2 = i3 + 1;
                }
                i = appendPolicyFlag;
            }
        }
        injectMotionEvent(event, i);
    }

    public static MotionEvent obtainMotionEvent(long downTime, long eventTime, int action, float x, float y, float pressure, int deviceId, int source, int toolType) {
        MotionEvent obtain;
        synchronized (gSharedTempLock) {
            if (sPointerProps == null) {
                sPointerProps = new MotionEvent.PointerProperties[1];
                sPointerProps[0] = new MotionEvent.PointerProperties();
            }
            MotionEvent.PointerProperties[] pp = sPointerProps;
            pp[0].clear();
            pp[0].id = 0;
            pp[0].toolType = toolType;
            if (sPointerCoords == null) {
                sPointerCoords = new MotionEvent.PointerCoords[1];
                sPointerCoords[0] = new MotionEvent.PointerCoords();
            }
            MotionEvent.PointerCoords[] pc = sPointerCoords;
            pc[0].clear();
            pc[0].x = x;
            pc[0].y = y;
            pc[0].pressure = pressure;
            pc[0].size = 1.0f;
            obtain = MotionEvent.obtain(downTime, eventTime, action, 1, pp, pc, 0, 0, 1.0f, 1.0f, deviceId, 0, source, 0);
        }
        return obtain;
    }

    public static void sendMultiPointerDown(ArrayList<PointerState> pendingPointerStates, int maxPointerCount, int deviceId, int source, int toolType, long firstDownTime, long durationTime) {
        sendMultiPointerGesture(pendingPointerStates, maxPointerCount, deviceId, source, toolType, true, true, firstDownTime, durationTime);
    }

    public static void sendMultiPointerTap(ArrayList<PointerState> pendingPointerStates, int maxPointerCount, int deviceId, int source, int toolType) {
        sendMultiPointerGesture(pendingPointerStates, maxPointerCount, deviceId, source, toolType, true, false, 0, 0);
    }

    public static void sendMultiPointerGesture(ArrayList<PointerState> pendingPointerStates, int maxPointerCount, int deviceId, int source, int toolType, boolean skipMove, boolean skipUp, long firstDownTime, long durationTime) {
        BitSet idBits;
        MotionEvent.PointerProperties[] pp;
        MotionEvent.PointerCoords[] pc;
        int i;
        SparseArray<PointF> idToPointer;
        long eventTime;
        ArrayList<PointerState> arrayList = pendingPointerStates;
        int i2 = maxPointerCount;
        boolean z = skipMove;
        boolean z2 = skipUp;
        long j = durationTime;
        if (GestureNavConst.DEBUG) {
            Log.i(GestureNavConst.TAG_GESTURE_UTILS, "sendMultiPointerGesture count:" + i2 + ", skipMove:" + z + ", skipUp:" + z2);
        }
        if (arrayList != null) {
            int size = pendingPointerStates.size();
            int size2 = size;
            if (size >= 2 && i2 >= 2 && arrayList.get(0).action == 0 && (z2 || arrayList.get(size2 - 1).action == 1)) {
                long downTime = firstDownTime > 0 ? firstDownTime : SystemClock.uptimeMillis();
                MotionEvent.PointerProperties[] pp2 = new MotionEvent.PointerProperties[i2];
                MotionEvent.PointerCoords[] pc2 = new MotionEvent.PointerCoords[i2];
                BitSet idBits2 = new BitSet(i2);
                SparseArray<PointF> idToPointer2 = new SparseArray<>();
                long eventTime2 = downTime;
                int currentPointerCount = 0;
                int i3 = 0;
                while (true) {
                    int i4 = i3;
                    if (i4 < size2) {
                        MotionEvent event = null;
                        PointerState ps = arrayList.get(i4);
                        switch (ps.action & 255) {
                            case 0:
                            case 5:
                                idToPointer = idToPointer2;
                                PointerState ps2 = ps;
                                i = i4;
                                idBits = idBits2;
                                pc = pc2;
                                pp = pp2;
                                BitSet idBits3 = idBits;
                                idBits3.set(ps2.activePointerId);
                                idToPointer.put(ps2.activePointerId, new PointF(ps2.x, ps2.y));
                                int currentPointerCount2 = fillPointerEvent(i2, pp, pc, toolType, idBits3, idToPointer);
                                event = MotionEvent.obtain(downTime, eventTime2, ps2.action, currentPointerCount2, pp, pc, 0, 0, 1.0f, 1.0f, deviceId, 0, source, 0);
                                currentPointerCount = currentPointerCount2;
                                break;
                            case 1:
                            case 6:
                                if (!z2) {
                                    event = MotionEvent.obtain(downTime, eventTime2, ps.action, currentPointerCount, pp2, pc2, 0, 0, 1.0f, 1.0f, deviceId, 0, source, 0);
                                }
                                idBits2.clear(ps.activePointerId);
                                idToPointer2.put(ps.activePointerId, new PointF(ps.x, ps.y));
                                idToPointer = idToPointer2;
                                PointerState pointerState = ps;
                                i = i4;
                                idBits = idBits2;
                                pc = pc2;
                                pp = pp2;
                                currentPointerCount = fillPointerEvent(i2, pp2, pc2, toolType, idBits, idToPointer);
                                break;
                            case 2:
                                if (!z) {
                                    event = MotionEvent.obtain(downTime, eventTime2, ps.action, currentPointerCount, pp2, pc2, 0, 0, 1.0f, 1.0f, deviceId, 0, source, 0);
                                }
                                idToPointer = idToPointer2;
                                i = i4;
                                idBits = idBits2;
                                pc = pc2;
                                pp = pp2;
                                break;
                            default:
                                idToPointer = idToPointer2;
                                PointerState pointerState2 = ps;
                                i = i4;
                                idBits = idBits2;
                                pc = pc2;
                                pp = pp2;
                                break;
                        }
                        MotionEvent event2 = event;
                        if (event2 != null) {
                            injectTransferMotionEvent(event2);
                        }
                        if (firstDownTime <= 0 || j <= 0) {
                            eventTime = SystemClock.uptimeMillis();
                        } else {
                            eventTime = firstDownTime + ((long) (((1.0f * ((float) (i + 1))) / ((float) size2)) * ((float) j)));
                        }
                        eventTime2 = eventTime;
                        i3 = i + 1;
                        idToPointer2 = idToPointer;
                        pc2 = pc;
                        pp2 = pp;
                        idBits2 = idBits;
                        arrayList = pendingPointerStates;
                        z = skipMove;
                    } else {
                        BitSet bitSet = idBits2;
                        MotionEvent.PointerCoords[] pointerCoordsArr = pc2;
                        MotionEvent.PointerProperties[] pointerPropertiesArr = pp2;
                        return;
                    }
                }
            }
        }
    }

    public static int fillPointerEvent(int maxPointerCount, MotionEvent.PointerProperties[] pp, MotionEvent.PointerCoords[] pc, int toolType, BitSet idBits, SparseArray<PointF> idToPointer) {
        int currentPointerCount = 0;
        int idSize = idBits.size();
        for (int j = 0; j < idSize; j++) {
            if (idBits.get(j)) {
                if (currentPointerCount >= maxPointerCount) {
                    return maxPointerCount;
                }
                pp[currentPointerCount] = new MotionEvent.PointerProperties();
                pp[currentPointerCount].id = j;
                pp[currentPointerCount].toolType = toolType;
                pc[currentPointerCount] = new MotionEvent.PointerCoords();
                pc[currentPointerCount].clear();
                pc[currentPointerCount].x = idToPointer.get(j).x;
                pc[currentPointerCount].y = idToPointer.get(j).y;
                pc[currentPointerCount].pressure = 1.0f;
                pc[currentPointerCount].size = 1.0f;
                currentPointerCount++;
            }
        }
        return currentPointerCount;
    }

    private static final float lerp(float a, float b, float alpha) {
        return ((b - a) * alpha) + a;
    }

    public static double angle(float distanceX, float distanceY, boolean divY) {
        if ((divY ? distanceY : distanceX) == 0.0f) {
            return 90.0d;
        }
        return (Math.atan((double) (divY ? distanceX / distanceY : distanceY / distanceX)) / 3.141592653589793d) * 180.0d;
    }

    public static void addWindowView(WindowManager mWindowManager, View view, WindowManager.LayoutParams params) {
        if (view != null) {
            try {
                mWindowManager.addView(view, params);
            } catch (Exception e) {
                Log.e(GestureNavConst.TAG_GESTURE_UTILS, "addWindowView fail." + e);
            }
        }
    }

    public static void updateViewLayout(WindowManager mWindowManager, View view, WindowManager.LayoutParams params) {
        if (view != null) {
            try {
                mWindowManager.updateViewLayout(view, params);
            } catch (Exception e) {
                Log.e(GestureNavConst.TAG_GESTURE_UTILS, "updateViewLayout fail." + e);
            }
        }
    }

    public static void removeWindowView(WindowManager mWindowManager, View view, boolean immediate) {
        if (view != null) {
            if (immediate) {
                try {
                    mWindowManager.removeViewImmediate(view);
                } catch (IllegalArgumentException e) {
                    Log.e(GestureNavConst.TAG_GESTURE_UTILS, "removeWindowView fail." + e);
                } catch (Exception e2) {
                    Log.e(GestureNavConst.TAG_GESTURE_UTILS, "removeWindowView fail." + e2);
                }
            } else {
                mWindowManager.removeView(view);
            }
        }
    }

    public static boolean isInLockTaskMode() {
        boolean z = false;
        try {
            if (ActivityManager.getService().getLockTaskModeState() != 0) {
                z = true;
            }
            return z;
        } catch (RemoteException e) {
            Log.e(GestureNavConst.TAG_GESTURE_UTILS, "Check lock task mode fail.", e);
            return false;
        }
    }

    public static void exitLockTaskMode() {
        try {
            ActivityManager.getService().stopSystemLockTaskMode();
        } catch (RemoteException e) {
            Log.e(GestureNavConst.TAG_GESTURE_UTILS, "Exit lock task mode fail.", e);
        }
    }

    public static boolean isSystemApp(Context context, String packageName) {
        if (context == null || packageName == null) {
            return false;
        }
        boolean systemApp = false;
        try {
            ApplicationInfo appInfo = context.getPackageManager().getApplicationInfo(packageName, 0);
            if (!(appInfo == null || (appInfo.flags & 1) == 0)) {
                systemApp = true;
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(GestureNavConst.TAG_GESTURE_UTILS, packageName + " not found.");
        }
        return systemApp;
    }

    public static boolean isSuperPowerSaveMode() {
        return SystemProperties.getBoolean("sys.super_power_save", false);
    }

    public static boolean performHapticFeedbackIfNeed(Context context) {
        if (!isHapticFedbackEnabled(context)) {
            return false;
        }
        if (mSupportEffectVb) {
            HwVibrator.setHwVibrator(Process.myUid(), context.getOpPackageName(), "haptic.virtual_navigation.click_back");
        } else {
            WindowManagerPolicy policy = (WindowManagerPolicy) LocalServices.getService(WindowManagerPolicy.class);
            if (policy != null) {
                policy.performHapticFeedbackLw(null, 1, false);
            }
        }
        return true;
    }

    public static boolean isHapticFedbackEnabled(Context context) {
        return Settings.System.getIntForUser(context.getContentResolver(), "haptic_feedback_enabled", 0, -2) != 0;
    }

    public static boolean isGameAppForeground() {
        HwActivityManagerService hwAms = HwActivityManagerService.self();
        if (hwAms != null) {
            return hwAms.isGameDndOn();
        }
        return false;
    }
}
