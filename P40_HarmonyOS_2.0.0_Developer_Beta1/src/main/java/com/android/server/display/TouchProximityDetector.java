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

/* access modifiers changed from: package-private */
public class TouchProximityDetector {
    private static final int CONTINUE_INVALID_PRINT_PERIOD_MS = 30000;
    private static final int DEFAULT_SENSOR_RATIO_MS = 300;
    private static final boolean HWDEBUG = (Log.HWLog || (Log.HWModuleLog && Log.isLoggable(TAG, 3)));
    private static final boolean HWFLOW;
    private static final String TAG = "TouchProximityDetector";
    private int mContinueInvalidCnt;
    private final int mContinueInvalidPrintPeriod;
    private volatile boolean mCurrentLuxValid = true;
    private boolean mEnable;
    private WindowManagerService.HwInnerWindowManagerService mHwInnerWindowManagerService;
    private MyRotateObserver mIHwRotateObserver;
    private boolean mInited;
    private MyPointerEventListener mPointerEventListener;
    private WindowManagerService mWindowManagerService;
    private int mXThreshold270Max;
    private int mXThreshold270Min;
    private int mXThreshold90Max;
    private int mXThreshold90Min;
    private int mYThreshold0Max;
    private int mYThreshold0Min;
    private int mYThreshold180Max;
    private int mYThreshold180Min;

    static {
        boolean z = false;
        if (Log.HWINFO || (Log.HWModuleLog && Log.isLoggable(TAG, 4))) {
            z = true;
        }
        HWFLOW = z;
    }

    TouchProximityDetector(HwBrightnessXmlLoader.Data data) {
        this.mContinueInvalidPrintPeriod = 30000 / (data.lightSensorRateMills > 0 ? data.lightSensorRateMills : 300);
        IDisplayManager iDisplayManager = ServiceManager.getService("display");
        if (iDisplayManager == null) {
            Slog.e(TAG, "init failed, display manager is null");
            return;
        }
        this.mWindowManagerService = ServiceManager.getService("window");
        WindowManagerService windowManagerService = this.mWindowManagerService;
        if (windowManagerService == null) {
            Slog.e(TAG, "init failed, window manager is null");
            return;
        }
        this.mHwInnerWindowManagerService = windowManagerService.getHwInnerService();
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
                return;
            }
            this.mInited = true;
        } catch (RemoteException e) {
            Slog.e(TAG, "getStableDisplaySize failed ");
        }
    }

    /* access modifiers changed from: package-private */
    public void enable() {
        if (this.mInited && !this.mEnable) {
            this.mCurrentLuxValid = true;
            this.mEnable = true;
            this.mPointerEventListener.clear();
            this.mWindowManagerService.registerPointerEventListener(this.mPointerEventListener, 0);
            this.mHwInnerWindowManagerService.registerRotateObserver(this.mIHwRotateObserver);
            if (HWFLOW) {
                Slog.i(TAG, "enable()");
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void disable() {
        if (this.mInited && this.mEnable) {
            this.mWindowManagerService.unregisterPointerEventListener(this.mPointerEventListener, 0);
            this.mHwInnerWindowManagerService.unregisterRotateObserver(this.mIHwRotateObserver);
            this.mPointerEventListener.clear();
            this.mEnable = false;
            if (HWFLOW) {
                Slog.i(TAG, "disable()");
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isCurrentLuxValid() {
        if (!this.mInited) {
            return true;
        }
        continueInvalidPrint(!this.mCurrentLuxValid);
        return this.mCurrentLuxValid;
    }

    /* access modifiers changed from: package-private */
    public void startNextLux() {
        if (this.mInited) {
            this.mCurrentLuxValid = !this.mPointerEventListener.isCovered();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setCurrentLuxInvalid() {
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

    /* access modifiers changed from: private */
    public class MyPointerEventListener implements WindowManagerPolicyConstants.PointerEventListener {
        private boolean mIsCovered;
        private int mNearbyPointBits;
        private int mUsedOrientation;

        private MyPointerEventListener() {
            this.mUsedOrientation = 0;
        }

        public void onPointerEvent(MotionEvent motionEvent) {
            updatePointState(motionEvent);
        }

        /* JADX WARNING: Code restructure failed: missing block: B:13:0x001d, code lost:
            if (r0 != 6) goto L_0x0035;
         */
        /* JADX WARNING: Removed duplicated region for block: B:21:0x003b  */
        /* JADX WARNING: Removed duplicated region for block: B:22:0x003d  */
        /* JADX WARNING: Removed duplicated region for block: B:24:0x0040  */
        /* JADX WARNING: Removed duplicated region for block: B:26:0x0043  */
        /* JADX WARNING: Removed duplicated region for block: B:37:? A[RETURN, SYNTHETIC] */
        private void updatePointState(MotionEvent motionEvent) {
            if (TouchProximityDetector.this.mEnable) {
                int action = motionEvent.getActionMasked();
                boolean isStateChanged = false;
                if (action != 0) {
                    if (action != 1) {
                        if (action == 2) {
                            doMoveAction(motionEvent);
                        } else if (action != 3) {
                            if (action != 5) {
                            }
                        }
                        if (this.mIsCovered != (this.mNearbyPointBits != 0)) {
                            isStateChanged = true;
                        }
                        if (isStateChanged) {
                            this.mIsCovered = true ^ this.mIsCovered;
                            if (this.mIsCovered) {
                                TouchProximityDetector.this.setCurrentLuxInvalid();
                            }
                            if (TouchProximityDetector.HWDEBUG) {
                                StringBuilder sb = new StringBuilder();
                                sb.append(this.mIsCovered ? "one more" : "no");
                                sb.append(" finger(s) nearby the proximity sensor");
                                Slog.d(TouchProximityDetector.TAG, sb.toString());
                                return;
                            }
                            return;
                        }
                        return;
                    }
                    if (motionEvent.getToolType(motionEvent.getActionIndex()) == 1) {
                        this.mNearbyPointBits = 0;
                    }
                    if (this.mIsCovered != (this.mNearbyPointBits != 0)) {
                    }
                    if (isStateChanged) {
                    }
                }
                doClickAction(action, motionEvent);
                if (this.mIsCovered != (this.mNearbyPointBits != 0)) {
                }
                if (isStateChanged) {
                }
            }
        }

        private void doClickAction(int action, MotionEvent motionEvent) {
            int index = motionEvent.getActionIndex();
            if (motionEvent.getToolType(index) == 1) {
                if (action == 0) {
                    this.mNearbyPointBits = 0;
                    if (TouchProximityDetector.this.mIHwRotateObserver != null) {
                        this.mUsedOrientation = TouchProximityDetector.this.mIHwRotateObserver.getOrientation();
                    }
                }
                int id = motionEvent.getPointerId(index);
                float realX = motionEvent.getX(index) * motionEvent.getXPrecision();
                float realY = motionEvent.getY(index) * motionEvent.getYPrecision();
                if (action == 6) {
                    this.mNearbyPointBits = (~(1 << id)) & this.mNearbyPointBits;
                } else if (isNearbyProximity(realX, realY)) {
                    this.mNearbyPointBits = (1 << id) | this.mNearbyPointBits;
                }
            }
        }

        private void doMoveAction(MotionEvent motionEvent) {
            int pointCount = motionEvent.getPointerCount();
            for (int i = 0; i < pointCount; i++) {
                if (motionEvent.getToolType(i) == 1) {
                    int id = motionEvent.getPointerId(i);
                    if (isNearbyProximity(motionEvent.getX(i) * motionEvent.getXPrecision(), motionEvent.getY(i) * motionEvent.getYPrecision())) {
                        this.mNearbyPointBits = (1 << id) | this.mNearbyPointBits;
                    } else {
                        this.mNearbyPointBits = (~(1 << id)) & this.mNearbyPointBits;
                    }
                }
            }
        }

        private boolean isNearbyProximity(float xValue, float yValue) {
            int i = this.mUsedOrientation;
            if (i == 0) {
                return yValue >= ((float) TouchProximityDetector.this.mYThreshold0Min) && yValue <= ((float) TouchProximityDetector.this.mYThreshold0Max);
            }
            if (i == 1) {
                return xValue >= ((float) TouchProximityDetector.this.mXThreshold90Min) && xValue <= ((float) TouchProximityDetector.this.mXThreshold90Max);
            }
            if (i == 2) {
                return yValue >= ((float) TouchProximityDetector.this.mYThreshold180Min) && yValue <= ((float) TouchProximityDetector.this.mYThreshold180Max);
            }
            if (i == 3) {
                return xValue >= ((float) TouchProximityDetector.this.mXThreshold270Min) && xValue <= ((float) TouchProximityDetector.this.mXThreshold270Max);
            }
            if (TouchProximityDetector.HWFLOW) {
                Slog.i(TouchProximityDetector.TAG, "unknow mOrientation=" + this.mUsedOrientation);
            }
            return false;
        }

        /* access modifiers changed from: package-private */
        public boolean isCovered() {
            return this.mIsCovered;
        }

        /* access modifiers changed from: package-private */
        public void clear() {
            this.mIsCovered = false;
        }
    }

    /* access modifiers changed from: private */
    public class MyRotateObserver extends IHwRotateObserver.Stub {
        private int mOrientation;

        private MyRotateObserver() {
            this.mOrientation = 0;
        }

        /* access modifiers changed from: package-private */
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
}
