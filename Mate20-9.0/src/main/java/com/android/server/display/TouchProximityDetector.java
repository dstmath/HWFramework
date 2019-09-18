package com.android.server.display;

import android.hardware.display.IDisplayManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import android.util.Slog;
import android.view.IHwRotateObserver;
import android.view.MotionEvent;
import android.view.WindowManagerPolicyConstants;
import com.android.server.display.HwBrightnessXmlLoader;
import com.android.server.wm.WindowManagerService;

class TouchProximityDetector {
    private static final int CONTINUE_INVALID_PRINT_PERIOD_MS = 30000;
    /* access modifiers changed from: private */
    public static final boolean HWDEBUG = (Log.HWLog || (Log.HWModuleLog && Log.isLoggable(TAG, 3)));
    /* access modifiers changed from: private */
    public static final boolean HWFLOW;
    private static final String TAG = "TouchProximityDetector";
    private int mContinueInvalidCnt;
    private final int mContinueInvalidPrintPeriod;
    private volatile boolean mCurrentLuxValid = true;
    /* access modifiers changed from: private */
    public boolean mEnable;
    private WindowManagerService.HwInnerWindowManagerService mHwInnerWindowManagerService;
    /* access modifiers changed from: private */
    public MyRotateObserver mIHwRotateObserver;
    private boolean mInited;
    private MyPointerEventListener mPointerEventListener;
    private WindowManagerService mWindowManagerService;
    /* access modifiers changed from: private */
    public int mXThreshold270Max;
    /* access modifiers changed from: private */
    public int mXThreshold270Min;
    /* access modifiers changed from: private */
    public int mXThreshold90Max;
    /* access modifiers changed from: private */
    public int mXThreshold90Min;
    /* access modifiers changed from: private */
    public int mYThreshold0Max;
    /* access modifiers changed from: private */
    public int mYThreshold0Min;
    /* access modifiers changed from: private */
    public int mYThreshold180Max;
    /* access modifiers changed from: private */
    public int mYThreshold180Min;

    private class MyPointerEventListener implements WindowManagerPolicyConstants.PointerEventListener {
        private boolean mIsCovered;
        private int mNearbyPointBits;
        private int mUsedOrientation;

        private MyPointerEventListener() {
            this.mIsCovered = false;
            this.mNearbyPointBits = 0;
            this.mUsedOrientation = 0;
        }

        public void onPointerEvent(MotionEvent motionEvent) {
            updatePointState(motionEvent);
        }

        private void updatePointState(MotionEvent motionEvent) {
            if (TouchProximityDetector.this.mEnable) {
                int action = motionEvent.getActionMasked();
                boolean stateChanged = false;
                switch (action) {
                    case 0:
                    case 5:
                    case 6:
                        int index = motionEvent.getActionIndex();
                        if (motionEvent.getToolType(index) == 1) {
                            if (action == 0) {
                                this.mNearbyPointBits = 0;
                                if (TouchProximityDetector.this.mIHwRotateObserver != null) {
                                    this.mUsedOrientation = TouchProximityDetector.this.mIHwRotateObserver.getOrientation();
                                }
                            }
                            float x = motionEvent.getX(index);
                            float y = motionEvent.getY(index);
                            int id = motionEvent.getPointerId(index);
                            float realX = x * motionEvent.getXPrecision();
                            float realY = y * motionEvent.getYPrecision();
                            if (action != 6) {
                                if (nearbyProximity(realX, realY)) {
                                    this.mNearbyPointBits |= 1 << id;
                                    break;
                                }
                            } else {
                                this.mNearbyPointBits &= ~(1 << id);
                                break;
                            }
                        }
                        break;
                    case 1:
                    case 3:
                        if (motionEvent.getToolType(motionEvent.getActionIndex()) == 1) {
                            this.mNearbyPointBits = 0;
                            break;
                        }
                        break;
                    case 2:
                        int pointCount = motionEvent.getPointerCount();
                        float realY2 = 0.0f;
                        float realY3 = 0.0f;
                        float y2 = 0.0f;
                        float x2 = 0.0f;
                        for (int i = 0; i < pointCount; i++) {
                            if (motionEvent.getToolType(i) == 1) {
                                x2 = motionEvent.getX(i);
                                y2 = motionEvent.getY(i);
                                int id2 = motionEvent.getPointerId(i);
                                float realX2 = motionEvent.getXPrecision() * x2;
                                float realY4 = motionEvent.getYPrecision() * y2;
                                if (nearbyProximity(realX2, realY4)) {
                                    this.mNearbyPointBits |= 1 << id2;
                                } else {
                                    this.mNearbyPointBits &= ~(1 << id2);
                                }
                                realY2 = realY4;
                                realY3 = realX2;
                            }
                        }
                        float f = x2;
                        float x3 = y2;
                        float y3 = realY3;
                        float realX3 = realY2;
                        break;
                }
                if (this.mIsCovered != (this.mNearbyPointBits != 0)) {
                    stateChanged = true;
                }
                if (stateChanged) {
                    this.mIsCovered = true ^ this.mIsCovered;
                    if (this.mIsCovered) {
                        TouchProximityDetector.this.setCurrentLuxInvalid();
                    }
                    if (TouchProximityDetector.HWDEBUG) {
                        StringBuilder sb = new StringBuilder();
                        sb.append(this.mIsCovered ? "one more" : "no");
                        sb.append(" finger(s) nearby the proximity sensor");
                        Slog.d(TouchProximityDetector.TAG, sb.toString());
                    }
                }
            }
        }

        private boolean nearbyProximity(float x, float y) {
            boolean z = true;
            switch (this.mUsedOrientation) {
                case 0:
                    if (y < ((float) TouchProximityDetector.this.mYThreshold0Min) || y > ((float) TouchProximityDetector.this.mYThreshold0Max)) {
                        z = false;
                    }
                    return z;
                case 1:
                    if (x < ((float) TouchProximityDetector.this.mXThreshold90Min) || x > ((float) TouchProximityDetector.this.mXThreshold90Max)) {
                        z = false;
                    }
                    return z;
                case 2:
                    if (y < ((float) TouchProximityDetector.this.mYThreshold180Min) || y > ((float) TouchProximityDetector.this.mYThreshold180Max)) {
                        z = false;
                    }
                    return z;
                case 3:
                    if (x < ((float) TouchProximityDetector.this.mXThreshold270Min) || x > ((float) TouchProximityDetector.this.mXThreshold270Max)) {
                        z = false;
                    }
                    return z;
                default:
                    if (TouchProximityDetector.HWFLOW) {
                        Slog.i(TouchProximityDetector.TAG, "unknow mOrientation=" + this.mUsedOrientation);
                    }
                    return false;
            }
        }

        public boolean isCovered() {
            return this.mIsCovered;
        }

        public void clear() {
            this.mIsCovered = false;
        }
    }

    private class MyRotateObserver extends IHwRotateObserver.Stub {
        private int mOrientation;

        private MyRotateObserver() {
            this.mOrientation = 0;
        }

        public int getOrientation() {
            return this.mOrientation;
        }

        public void onRotate(int oldRotation, int newRotation) {
            if (TouchProximityDetector.HWFLOW) {
                Slog.i(TouchProximityDetector.TAG, "RotateObserver onRotate " + oldRotation + "->" + newRotation);
            }
            this.mOrientation = newRotation;
        }
    }

    static {
        boolean z = true;
        if (!Log.HWINFO && (!Log.HWModuleLog || !Log.isLoggable(TAG, 4))) {
            z = false;
        }
        HWFLOW = z;
    }

    public TouchProximityDetector(HwBrightnessXmlLoader.Data data) {
        this.mContinueInvalidPrintPeriod = 30000 / (data.lightSensorRateMills > 0 ? data.lightSensorRateMills : 300);
        IDisplayManager iDisplayManager = ServiceManager.getService("display");
        if (iDisplayManager == null) {
            Slog.e(TAG, "init failed, display manager is null");
            return;
        }
        this.mWindowManagerService = ServiceManager.getService("window");
        if (this.mWindowManagerService == null) {
            Slog.e(TAG, "init failed, window manager is null");
            return;
        }
        this.mHwInnerWindowManagerService = this.mWindowManagerService.getHwInnerService();
        if (this.mHwInnerWindowManagerService == null) {
            Slog.e(TAG, "init failed, inner window manager is null");
            return;
        }
        this.mPointerEventListener = new MyPointerEventListener();
        this.mIHwRotateObserver = new MyRotateObserver();
        try {
            int yMaxPixels = iDisplayManager.getStableDisplaySize().y;
            this.mYThreshold0Min = (int) (((float) yMaxPixels) * data.touchProximityYNearbyRatioMin);
            this.mYThreshold0Max = (int) (((float) yMaxPixels) * data.touchProximityYNearbyRatioMax);
            this.mXThreshold90Min = this.mYThreshold0Min;
            this.mXThreshold90Max = this.mYThreshold0Max;
            this.mYThreshold180Min = yMaxPixels - this.mYThreshold0Max;
            this.mYThreshold180Max = yMaxPixels - this.mYThreshold0Min;
            this.mXThreshold270Min = this.mYThreshold180Min;
            this.mXThreshold270Max = this.mYThreshold180Max;
            if (yMaxPixels <= 0) {
                Slog.e(TAG, "init the threeshold failed, invalid parameter value");
                this.mInited = false;
            } else {
                this.mInited = true;
            }
        } catch (RemoteException e) {
            Slog.e(TAG, "getStableDisplaySize failed ");
        }
    }

    public void enable() {
        if (this.mInited && !this.mEnable) {
            this.mCurrentLuxValid = true;
            this.mEnable = true;
            this.mPointerEventListener.clear();
            this.mWindowManagerService.registerPointerEventListener(this.mPointerEventListener);
            this.mHwInnerWindowManagerService.registerRotateObserver(this.mIHwRotateObserver);
            if (HWFLOW) {
                Slog.i(TAG, "enable()");
            }
        }
    }

    public void disable() {
        if (this.mInited && this.mEnable) {
            this.mWindowManagerService.unregisterPointerEventListener(this.mPointerEventListener);
            this.mHwInnerWindowManagerService.unregisterRotateObserver(this.mIHwRotateObserver);
            this.mPointerEventListener.clear();
            this.mEnable = false;
            if (HWFLOW) {
                Slog.i(TAG, "disable()");
            }
        }
    }

    public boolean isCurrentLuxValid() {
        if (!this.mInited) {
            return true;
        }
        continueInvalidPrint(!this.mCurrentLuxValid);
        return this.mCurrentLuxValid;
    }

    public void startNextLux() {
        if (this.mInited) {
            this.mCurrentLuxValid = !this.mPointerEventListener.isCovered();
        }
    }

    /* access modifiers changed from: private */
    public void setCurrentLuxInvalid() {
        this.mCurrentLuxValid = false;
    }

    private void continueInvalidPrint(boolean isInvalid) {
        if (isInvalid) {
            int i = this.mContinueInvalidCnt + 1;
            this.mContinueInvalidCnt = i;
            if (i % this.mContinueInvalidPrintPeriod == 0 && HWFLOW) {
                Slog.i(TAG, "lux continue invalid for " + this.mContinueInvalidCnt + " times");
                return;
            }
            return;
        }
        this.mContinueInvalidCnt = 0;
    }
}
