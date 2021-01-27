package com.android.server.gesture;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.PointF;
import android.hardware.display.HwFoldScreenState;
import android.os.Bundle;
import android.os.Process;
import android.os.RemoteException;
import android.os.SystemClock;
import android.util.Log;
import android.util.SparseArray;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import com.android.server.input.InputManagerServiceEx;
import com.android.server.pm.HwPackageManagerServiceEx;
import com.android.server.policy.WindowManagerPolicyEx;
import com.huawei.android.app.ActivityManagerEx;
import com.huawei.android.app.ActivityTaskManagerExt;
import com.huawei.android.content.ContextEx;
import com.huawei.android.fsm.HwFoldScreenManagerInternal;
import com.huawei.android.hardware.input.InputManagerEx;
import com.huawei.android.os.HwVibrator;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.provider.SettingsEx;
import com.huawei.android.server.LocalServicesExt;
import com.huawei.android.view.KeyEventEx;
import com.huawei.android.view.MotionEventEx;
import com.huawei.hwpartbasicplatformservices.BuildConfig;
import java.util.ArrayList;
import java.util.BitSet;

public class GestureUtils extends DefaultGestureUtils {
    private static final int CURVED_SIDE_DISP_LENGTH = 4;
    private static final int DEFAULT_ACTION_BUTTON = 0;
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
    private static final boolean IS_BACK_GESTURE_HAPTIC_FEEDBACK_EN = SystemPropertiesEx.getBoolean("ro.config.backgesture_haptic_feedback", false);
    private static final String KEY_CURVED_SIDE_DISP = "ro.config.hw_curved_side_disp";
    private static final int MOTION_EVENT_INJECTION_DELAY_MILLIS = 5;
    private static final Object SHARED_TEMP_LOCK = new Object();
    private static boolean sHasInit = false;
    private static boolean sHasNotchProp = false;
    private static boolean sIsCurvedSide = false;
    private static boolean sIsSupportEffectVb = false;
    private static int sLeftCurvedSideDisp = 0;
    private static MotionEvent.PointerCoords[] sPointerCoords;
    private static MotionEvent.PointerProperties[] sPointerProps;
    private static int sRightCurvedSideDisp = 0;

    public static void getCurvedSideDisp() {
        String[] curvedSideDisps = SystemPropertiesEx.get(KEY_CURVED_SIDE_DISP).split(",");
        if (curvedSideDisps.length != 4) {
            sIsCurvedSide = false;
            sLeftCurvedSideDisp = 0;
            sRightCurvedSideDisp = 0;
            return;
        }
        try {
            sLeftCurvedSideDisp = Integer.parseInt(curvedSideDisps[0]);
            sRightCurvedSideDisp = Integer.parseInt(curvedSideDisps[2]);
        } catch (NumberFormatException e) {
            Log.e(GestureNavConst.TAG_GESTURE_UTILS, "parseInt fail with NumberFormatException");
        }
        sIsCurvedSide = true;
    }

    public static int getCurvedSideLeftDisp() {
        return sLeftCurvedSideDisp;
    }

    public static int getCurvedSideRightDisp() {
        return sRightCurvedSideDisp;
    }

    public static boolean isCurvedSideDisp() {
        return sIsCurvedSide;
    }

    public static void systemReady() {
        if (!sHasInit) {
            sHasNotchProp = parseHole();
            sIsSupportEffectVb = HwVibrator.isSupportHwVibrator("haptic.virtual_navigation.click_back");
            if (GestureNavConst.DEBUG) {
                Log.i(GestureNavConst.TAG_GESTURE_UTILS, "systemReady hasNotch=" + sHasNotchProp + ", effectVb=" + sIsSupportEffectVb);
            }
            getCurvedSideDisp();
            sHasInit = true;
        }
    }

    public static boolean parseHole() {
        String[] props = SystemPropertiesEx.get("ro.config.hw_notch_size", BuildConfig.FLAVOR).split(",");
        if (props == null || props.length != 4) {
            return false;
        }
        Log.d(GestureNavConst.TAG_GESTURE_UTILS, "prop hole height:" + Integer.parseInt(props[1]));
        return true;
    }

    public static boolean hasNotchProp() {
        return sHasNotchProp;
    }

    public static boolean isDisplayHasNotch() {
        HwFoldScreenManagerInternal fsm;
        if (!sHasNotchProp) {
            return false;
        }
        if (!HwFoldScreenState.isFoldScreenDevice() || !HwFoldScreenState.isInwardFoldDevice() || (fsm = (HwFoldScreenManagerInternal) LocalServicesExt.getService(HwFoldScreenManagerInternal.class)) == null || fsm.getDisplayMode() == 2) {
            return true;
        }
        return false;
    }

    public static boolean isSupportEffectVibrator() {
        return sIsSupportEffectVb;
    }

    public static int getInputDeviceId(int inputSource) {
        int[] devIds = InputDevice.getDeviceIds();
        for (int devId : devIds) {
            InputDevice inputDev = InputDevice.getDevice(devId);
            if (inputDev != null && inputDev.supportsSource(inputSource)) {
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

    public static final class PointerState {
        public int action;
        public int activePointerId;
        public PointF point = new PointF();

        public PointerState(int activePointerId2, int action2, PointF point2) {
            this.activePointerId = activePointerId2;
            this.action = action2;
            this.point.set(point2);
        }
    }

    public static void sendKeyEvent(int keycode) {
        int[] actions;
        if (GestureNavConst.DEBUG) {
            Log.i(GestureNavConst.TAG_GESTURE_UTILS, "sendKeyEvent keycode=" + keycode);
        }
        long now = SystemClock.uptimeMillis();
        for (int i : new int[]{0, 1}) {
            KeyEvent ev = new KeyEvent(now, now, i, keycode, 0, 0, -1, 0, 8, 257);
            KeyEventEx.setDisplayId(ev, 0);
            InputManagerEx.injectInputEvent(InputManagerEx.getInstance(), ev, InputManagerEx.getInjectInputEventModeAsync());
        }
    }

    public static void sendTap(float posX, float posY, int deviceId, int source, int toolType, int buttonState, int metaState) {
        if (GestureNavConst.DEBUG) {
            Log.i(GestureNavConst.TAG_GESTURE_UTILS, "sendTap(" + posX + ", " + posY + "), source:0x" + Integer.toHexString(source) + ", buttonState:" + buttonState + ", metaState:" + metaState);
        }
        long downTime = SystemClock.uptimeMillis();
        injectMotionEvent(0, downTime, downTime, posX, posY, 1.0f, deviceId, source, toolType, buttonState, metaState);
        if ((source & 8194) == 8194) {
            injectMotionEvent(11, downTime, downTime, posX, posY, 1.0f, deviceId, source, toolType, buttonState, metaState, buttonState);
            injectMotionEvent(12, downTime, downTime, posX, posY, 1.0f, deviceId, source, toolType, 0, metaState, buttonState);
        }
        injectMotionEvent(1, downTime, SystemClock.uptimeMillis(), posX, posY, 0.0f, deviceId, source, toolType, 0, metaState);
    }

    /* JADX WARNING: Removed duplicated region for block: B:40:0x0164  */
    /* JADX WARNING: Removed duplicated region for block: B:41:0x0183  */
    public static void sendSwipe(float x1, float y1, float x2, float y2, int duration, int deviceId, int source, int toolType, int buttonState, int metaState, ArrayList<PointF> pendingMovePoints, boolean hasMultiTouched) {
        int adjustedDuration;
        float f;
        int adjustedDuration2;
        if (GestureNavConst.DEBUG) {
            Log.i(GestureNavConst.TAG_GESTURE_UTILS, "sendSwipe (" + x1 + ", " + y1 + ") to (" + x2 + ", " + y2 + "), duration:" + duration);
        }
        if (duration < 80) {
            adjustedDuration = 80;
        } else if (duration > 500) {
            adjustedDuration = 500;
        } else {
            adjustedDuration = duration;
        }
        long now = SystemClock.uptimeMillis();
        boolean isFromMouse = (source & 8194) == 8194;
        injectMotionEvent(0, now, now, x1, y1, 1.0f, deviceId, source, toolType, buttonState, metaState);
        if (isFromMouse) {
            f = x2;
            injectMotionEvent(11, now, now, x1, y1, 1.0f, deviceId, source, toolType, buttonState, metaState, buttonState);
        } else {
            f = x2;
        }
        long size = 0;
        if (!hasMultiTouched) {
            if (pendingMovePoints != null) {
                long size2 = (long) pendingMovePoints.size();
                size = size2;
                if (size2 > 0) {
                    if (GestureNavConst.DEBUG) {
                        Log.d(GestureNavConst.TAG_GESTURE_UTILS, "inject " + size + " pending move points");
                    }
                    for (int i = 0; ((long) i) < size; i++) {
                        injectMotionEvent(2, now, now, pendingMovePoints.get(i).x, pendingMovePoints.get(i).y, 1.0f, deviceId, source, toolType, buttonState, metaState);
                        SystemClock.sleep(5);
                        now = SystemClock.uptimeMillis();
                    }
                    adjustedDuration2 = adjustedDuration;
                    if (!isFromMouse) {
                        injectMotionEvent(12, now, now, x2, y2, 0.0f, deviceId, source, toolType, 0, metaState, buttonState);
                    }
                    injectMotionEvent(1, now, now, x2, y2, 0.0f, deviceId, source, toolType, 0, metaState);
                }
            }
        }
        adjustedDuration2 = adjustedDuration;
        long endTime = now + ((long) adjustedDuration2);
        while (now < endTime) {
            float alpha = ((float) (now - now)) / ((float) adjustedDuration2);
            injectMotionEvent(2, now, now, lerp(x1, f, alpha), lerp(y1, y2, alpha), 1.0f, deviceId, source, toolType, buttonState, metaState);
            SystemClock.sleep(5);
            now = SystemClock.uptimeMillis();
        }
        if (!isFromMouse) {
        }
        injectMotionEvent(1, now, now, x2, y2, 0.0f, deviceId, source, toolType, 0, metaState);
    }

    public static void injectMotionEvent(int action, long downTime, long eventTime, float posX, float posY, int deviceId, int source, int toolType, int buttonState, int metaState) {
        boolean isUp = true;
        if (action != 1) {
            isUp = false;
        }
        injectMotionEvent(action, downTime, eventTime, posX, posY, isUp ? 0.0f : 1.0f, deviceId, source, toolType, isUp ? 0 : buttonState, metaState);
    }

    private static void injectMotionEvent(int action, long downTime, long eventTime, float posX, float posY, float pressure, int deviceId, int source, int toolType, int buttonState, int metaState) {
        injectMotionEvent(action, downTime, eventTime, posX, posY, pressure, deviceId, source, toolType, buttonState, metaState, 0);
    }

    public static void injectMotionEvent(int action, long downTime, long eventTime, float posX, float posY, float pressure, int deviceId, int source, int toolType, int buttonState, int metaState, int actionButton) {
        MotionEvent event = obtainMotionEvent(downTime, eventTime, action, posX, posY, pressure, deviceId, source, toolType, buttonState, metaState);
        if (actionButton != 0) {
            MotionEventEx.setActionButton(event, actionButton);
        }
        injectTransferMotionEvent(event);
    }

    public static void injectMotionEvent(MotionEvent event, int appendPolicyFlag) {
        InputManagerServiceEx.DefaultHwInputManagerLocalService hwInputManagerInternal = (InputManagerServiceEx.DefaultHwInputManagerLocalService) LocalServicesExt.getService(InputManagerServiceEx.DefaultHwInputManagerLocalService.class);
        if (hwInputManagerInternal != null) {
            hwInputManagerInternal.injectInputEvent(event, InputManagerEx.getInjectInputEventModeAsync(), appendPolicyFlag);
        }
    }

    public static void injectTransferMotionEvent(MotionEvent event) {
        injectMotionEvent(event, WindowManagerPolicyEx.FLAG_TRANSFER_EVENT);
    }

    public static void injectDownWithBatchMoveEvent(long downTime, float downX, float downY, ArrayList<PointF> batchMovePoints, long durationTime, int deviceId, int source, int toolType) {
        int appendPolicyFlag;
        int size;
        MotionEvent event = obtainMotionEvent(downTime, downTime, 0, downX, downY, 1.0f, deviceId, source, toolType, 0, 0);
        int appendPolicyFlag2 = WindowManagerPolicyEx.FLAG_TRANSFER_EVENT;
        if (batchMovePoints == null || (size = batchMovePoints.size()) <= 0) {
            appendPolicyFlag = appendPolicyFlag2;
        } else {
            appendPolicyFlag = appendPolicyFlag2 | WindowManagerPolicyEx.FLAG_INJECT_DOWN_WITH_BATCH_MOVE;
            if (GestureNavConst.DEBUG) {
                Log.d(GestureNavConst.TAG_GESTURE_UTILS, "inject down with " + size + " batch move points");
            }
            for (int i = 0; i < size; i++) {
                event.addBatch(downTime + ((long) (((((float) (i + 1)) * 1.0f) / ((float) size)) * ((float) durationTime))), batchMovePoints.get(i).x, batchMovePoints.get(i).y, 1.0f, 1.0f, 0);
            }
        }
        injectMotionEvent(event, appendPolicyFlag);
    }

    public static MotionEvent obtainMotionEvent(long downTime, long eventTime, int action, float posX, float posY, float pressure, int deviceId, int source, int toolType, int buttonState, int metaState) {
        MotionEvent obtain;
        synchronized (SHARED_TEMP_LOCK) {
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
            pc[0].x = posX;
            pc[0].y = posY;
            pc[0].pressure = pressure;
            pc[0].size = 1.0f;
            obtain = MotionEvent.obtain(downTime, eventTime, action, 1, pp, pc, metaState, buttonState, 1.0f, 1.0f, deviceId, 0, source, 0);
        }
        return obtain;
    }

    public static void sendMultiPointerDown(ArrayList<PointerState> pendingPointerStates, int maxPointerCount, int deviceId, int source, int toolType, long firstDownTime, long durationTime) {
        sendMultiPointerGesture(pendingPointerStates, maxPointerCount, deviceId, source, toolType, true, true, firstDownTime, durationTime);
    }

    public static void sendMultiPointerTap(ArrayList<PointerState> pendingPointerStates, int maxPointerCount, int deviceId, int source, int toolType) {
        sendMultiPointerGesture(pendingPointerStates, maxPointerCount, deviceId, source, toolType, true, false, 0, 0);
    }

    public static void sendPointerUp(MotionEvent event) {
        if (GestureNavConst.DEBUG) {
            Log.i(GestureNavConst.TAG_GESTURE_UTILS, "sendPointerUp");
        }
        int pointerCount = event.getPointerCount();
        MotionEvent.PointerProperties[] props = MotionEventEx.PointerPropertiesEx.createArray(pointerCount);
        MotionEvent.PointerCoords[] coords = MotionEventEx.PointerCoordsEx.createArray(pointerCount);
        for (int i = 0; i < pointerCount; i++) {
            event.getPointerProperties(i, props[i]);
            coords[i].clear();
            coords[i].x = MotionEventEx.getRawX(event, i);
            coords[i].y = MotionEventEx.getRawY(event, i);
            coords[i].pressure = event.getPressure(i);
            coords[i].size = event.getSize(i);
        }
        MotionEvent motionEvent = MotionEvent.obtain(event.getDownTime(), event.getEventTime(), event.getAction(), pointerCount, props, coords, event.getMetaState(), event.getButtonState(), 1.0f, 1.0f, event.getDeviceId(), event.getEdgeFlags(), event.getSource(), event.getFlags());
        if (motionEvent != null) {
            injectTransferMotionEvent(motionEvent);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:27:0x00a1, code lost:
        if (r15 != 6) goto L_0x00de;
     */
    /* JADX WARNING: Removed duplicated region for block: B:43:0x019b  */
    /* JADX WARNING: Removed duplicated region for block: B:46:0x01a4  */
    public static void sendMultiPointerGesture(ArrayList<PointerState> pendingPointerStates, int maxPointerCount, int deviceId, int source, int toolType, boolean skipMove, boolean skipUp, long firstDownTime, long durationTime) {
        MotionEvent.PointerProperties[] pp;
        MotionEvent.PointerCoords[] pc;
        BitSet idBits;
        SparseArray<PointF> idToPointer;
        int i;
        long eventTime;
        PointerState ps;
        ArrayList<PointerState> arrayList = pendingPointerStates;
        if (GestureNavConst.DEBUG) {
            Log.i(GestureNavConst.TAG_GESTURE_UTILS, "sendMultiPointerGesture count:" + maxPointerCount + ", skipMove:" + skipMove + ", skipUp:" + skipUp);
        }
        if (arrayList != null) {
            int size = pendingPointerStates.size();
            int i2 = 2;
            if (size >= 2 && maxPointerCount >= 2 && arrayList.get(0).action == 0) {
                int i3 = 1;
                if (skipUp || arrayList.get(size - 1).action == 1) {
                    long downTime = firstDownTime > 0 ? firstDownTime : SystemClock.uptimeMillis();
                    MotionEvent.PointerProperties[] pp2 = new MotionEvent.PointerProperties[maxPointerCount];
                    MotionEvent.PointerCoords[] pc2 = new MotionEvent.PointerCoords[maxPointerCount];
                    BitSet idBits2 = new BitSet(maxPointerCount);
                    SparseArray<PointF> idToPointer2 = new SparseArray<>();
                    long eventTime2 = downTime;
                    int currentPointerCount = 0;
                    int i4 = 0;
                    while (i4 < size) {
                        MotionEvent event = null;
                        PointerState ps2 = arrayList.get(i4);
                        int maskAction = ps2.action & GestureNavConst.VIBRATION_AMPLITUDE;
                        if (maskAction != 0) {
                            if (maskAction != i3) {
                                if (maskAction != i2) {
                                    if (maskAction == 5) {
                                        ps = ps2;
                                        i = i4;
                                        idToPointer = idToPointer2;
                                        idBits = idBits2;
                                        pc = pc2;
                                        pp = pp2;
                                    }
                                } else if (currentPointerCount > 0 && !skipMove) {
                                    event = MotionEvent.obtain(downTime, eventTime2, ps2.action, currentPointerCount, pp2, pc2, 0, 0, 1.0f, 1.0f, deviceId, 0, source, 0);
                                    i = i4;
                                    idToPointer = idToPointer2;
                                    idBits = idBits2;
                                    pc = pc2;
                                    pp = pp2;
                                    if (event != null) {
                                        injectTransferMotionEvent(event);
                                    }
                                    if (firstDownTime > 0 || durationTime <= 0) {
                                        eventTime = SystemClock.uptimeMillis();
                                    } else {
                                        eventTime = firstDownTime + ((long) (((((float) (i + 1)) * 1.0f) / ((float) size)) * ((float) durationTime)));
                                    }
                                    eventTime2 = eventTime;
                                    arrayList = pendingPointerStates;
                                    i4 = i + 1;
                                    idToPointer2 = idToPointer;
                                    idBits2 = idBits;
                                    pc2 = pc;
                                    pp2 = pp;
                                    i2 = 2;
                                    i3 = 1;
                                }
                                i = i4;
                                idToPointer = idToPointer2;
                                idBits = idBits2;
                                pc = pc2;
                                pp = pp2;
                                if (event != null) {
                                }
                                if (firstDownTime > 0) {
                                }
                                eventTime = SystemClock.uptimeMillis();
                                eventTime2 = eventTime;
                                arrayList = pendingPointerStates;
                                i4 = i + 1;
                                idToPointer2 = idToPointer;
                                idBits2 = idBits;
                                pc2 = pc;
                                pp2 = pp;
                                i2 = 2;
                                i3 = 1;
                            }
                            if (currentPointerCount > 0 && !skipUp) {
                                event = MotionEvent.obtain(downTime, eventTime2, ps2.action, currentPointerCount, pp2, pc2, 0, 0, 1.0f, 1.0f, deviceId, 0, source, 0);
                            }
                            idBits2.clear(ps2.activePointerId);
                            idToPointer2.put(ps2.activePointerId, new PointF(ps2.point.x, ps2.point.y));
                            i = i4;
                            idToPointer = idToPointer2;
                            idBits = idBits2;
                            pc = pc2;
                            pp = pp2;
                            currentPointerCount = fillPointerEvent(maxPointerCount, pp2, pc2, toolType, idBits, idToPointer);
                            if (event != null) {
                            }
                            if (firstDownTime > 0) {
                            }
                            eventTime = SystemClock.uptimeMillis();
                            eventTime2 = eventTime;
                            arrayList = pendingPointerStates;
                            i4 = i + 1;
                            idToPointer2 = idToPointer;
                            idBits2 = idBits;
                            pc2 = pc;
                            pp2 = pp;
                            i2 = 2;
                            i3 = 1;
                        } else {
                            ps = ps2;
                            i = i4;
                            idToPointer = idToPointer2;
                            idBits = idBits2;
                            pc = pc2;
                            pp = pp2;
                        }
                        idBits.set(ps.activePointerId);
                        idToPointer.put(ps.activePointerId, new PointF(ps.point.x, ps.point.y));
                        int currentPointerCount2 = fillPointerEvent(maxPointerCount, pp, pc, toolType, idBits, idToPointer);
                        if (currentPointerCount2 > 0) {
                            event = MotionEvent.obtain(downTime, eventTime2, ps.action, currentPointerCount2, pp, pc, 0, 0, 1.0f, 1.0f, deviceId, 0, source, 0);
                            currentPointerCount = currentPointerCount2;
                        } else {
                            currentPointerCount = currentPointerCount2;
                        }
                        if (event != null) {
                        }
                        if (firstDownTime > 0) {
                        }
                        eventTime = SystemClock.uptimeMillis();
                        eventTime2 = eventTime;
                        arrayList = pendingPointerStates;
                        i4 = i + 1;
                        idToPointer2 = idToPointer;
                        idBits2 = idBits;
                        pc2 = pc;
                        pp2 = pp;
                        i2 = 2;
                        i3 = 1;
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

    private static float lerp(float a, float b, float alpha) {
        return ((b - a) * alpha) + a;
    }

    public static double angle(float distanceX, float distanceY, boolean isDivY) {
        if ((isDivY ? distanceY : distanceX) == 0.0f) {
            return 90.0d;
        }
        return (Math.atan((double) (isDivY ? distanceX / distanceY : distanceY / distanceX)) / 3.141592653589793d) * 180.0d;
    }

    public static void addWindowView(WindowManager windowManager, View view, WindowManager.LayoutParams params) {
        if (view != null) {
            try {
                windowManager.addView(view, params);
            } catch (IllegalArgumentException e) {
                Log.e(GestureNavConst.TAG_GESTURE_UTILS, "addWindowView fail, catch IllegalArgumentException");
            } catch (Exception e2) {
                Log.e(GestureNavConst.TAG_GESTURE_UTILS, "addWindowView fail, catch Exception");
            }
        }
    }

    public static void updateViewLayout(WindowManager windowManager, View view, WindowManager.LayoutParams params) {
        if (view != null) {
            try {
                windowManager.updateViewLayout(view, params);
            } catch (IllegalArgumentException e) {
                Log.e(GestureNavConst.TAG_GESTURE_UTILS, "updateViewLayout fail, catch IllegalArgumentException");
            } catch (Exception e2) {
                Log.e(GestureNavConst.TAG_GESTURE_UTILS, "updateViewLayout fail, catch Exception");
            }
        }
    }

    public static void removeWindowView(WindowManager windowManager, View view, boolean isImmediate) {
        if (view != null) {
            if (isImmediate) {
                try {
                    windowManager.removeViewImmediate(view);
                } catch (IllegalArgumentException e) {
                    Log.e(GestureNavConst.TAG_GESTURE_UTILS, "removeWindowView fail." + e);
                } catch (Exception e2) {
                    Log.e(GestureNavConst.TAG_GESTURE_UTILS, "removeWindowView fail, catch Exception");
                }
            } else {
                windowManager.removeView(view);
            }
        }
    }

    public static boolean isInLockTaskMode() {
        try {
            return isInLockTaskMode(ActivityTaskManagerExt.getLockTaskModeState());
        } catch (RemoteException e) {
            Log.e(GestureNavConst.TAG_GESTURE_UTILS, "Check lock task mode fail.", e);
            return false;
        }
    }

    public static boolean isInLockTaskMode(int lockTaskState) {
        return lockTaskState != 0;
    }

    public static void exitLockTaskMode() {
        try {
            ActivityTaskManagerExt.stopSystemLockTaskMode();
        } catch (RemoteException e) {
            Log.e(GestureNavConst.TAG_GESTURE_UTILS, "Exit lock task mode fail.", e);
        }
    }

    public static boolean isSystemOrSignature(Context context, String packageName) {
        if (context == null || packageName == null) {
            return false;
        }
        boolean isTrust = false;
        try {
            PackageManager pm = context.getPackageManager();
            ApplicationInfo appInfo = pm.getApplicationInfo(packageName, 0);
            if (!(appInfo == null || (appInfo.flags & 1) == 0)) {
                isTrust = true;
            }
            if (isTrust || pm.checkSignatures(packageName, "android") != 0) {
                return isTrust;
            }
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(GestureNavConst.TAG_GESTURE_UTILS, packageName + " not found.");
            return false;
        }
    }

    public static boolean isSuperPowerSaveMode() {
        return SystemPropertiesEx.getBoolean(GestureNavConst.KEY_SUPER_SAVE_MODE, false);
    }

    public static boolean performHapticFeedbackIfNeed(Context context) {
        if (!IS_BACK_GESTURE_HAPTIC_FEEDBACK_EN) {
            return false;
        }
        if (sIsSupportEffectVb) {
            HwVibrator.setHwVibrator(Process.myUid(), ContextEx.getOpPackageName(context), "haptic.virtual_navigation.click_back");
            return true;
        }
        WindowManagerPolicyEx policy = WindowManagerPolicyEx.getInstance();
        if (policy == null) {
            return true;
        }
        policy.performHapticFeedback(Process.myUid(), ContextEx.getOpPackageName(context), 1, false, "Gesture Nav Back");
        return true;
    }

    public static boolean isHapticFedbackEnabled(Context context) {
        return SettingsEx.System.getIntForUser(context.getContentResolver(), "haptic_feedback_enabled", 0, -2) != 0;
    }

    public static boolean isGameAppForeground() {
        return ActivityManagerEx.isGameDndOn();
    }

    public static boolean isLauncherGesNavProxyEnable(Context context) {
        if (context == null) {
            Log.e(GestureNavConst.TAG_GESTURE_UTILS, " isLauncherGesNavProxyEnable context null");
            return true;
        }
        try {
            Bundle bundle = context.getPackageManager().getApplicationInfo("com.huawei.android.launcher", HwPackageManagerServiceEx.APP_FORCE_DARK_USER_SET_FLAG).metaData;
            if (bundle == null) {
                return true;
            }
            boolean isEnable = bundle.getBoolean("huawei.gesnav_proxy", true);
            if (!GestureNavConst.DEBUG) {
                return isEnable;
            }
            Log.i(GestureNavConst.TAG_GESTURE_UTILS, "isLauncherGesNavProxyEnable isEnable=" + isEnable);
            return isEnable;
        } catch (PackageManager.NameNotFoundException | NumberFormatException e) {
            Log.e(GestureNavConst.TAG_GESTURE_UTILS, " isLauncherGesNavProxyEnable exception");
            return true;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:12:0x0033  */
    /* JADX WARNING: Removed duplicated region for block: B:14:0x0039  */
    public static String getDeviceType() {
        char c;
        String characteristics = SystemPropertiesEx.get("ro.build.characteristics", GestureNavConst.DEVICE_TYPE_DEFAULT);
        boolean isFoldScreen = HwFoldScreenState.isFoldScreenDevice();
        int hashCode = characteristics.hashCode();
        if (hashCode != -881377690) {
            if (hashCode == 1544803905 && characteristics.equals(GestureNavConst.DEVICE_TYPE_DEFAULT)) {
                c = 0;
                if (c != 0) {
                    if (c != 1) {
                        return GestureNavConst.DEVICE_TYPE_DEFAULT;
                    }
                    return GestureNavConst.DEVICE_TYPE_TABLET;
                } else if (isFoldScreen) {
                    return GestureNavConst.DEVICE_TYPE_FOLD_PHONE;
                } else {
                    return GestureNavConst.DEVICE_TYPE_DEFAULT;
                }
            }
        } else if (characteristics.equals(GestureNavConst.DEVICE_TYPE_TABLET)) {
            c = 1;
            if (c != 0) {
            }
        }
        c = 65535;
        if (c != 0) {
        }
    }
}
