package com.android.server.policy;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.pc.IHwPCManager;
import android.util.HwPCUtils;
import android.util.Slog;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.WindowManagerPolicyConstants;
import android.widget.OverScroller;
import com.android.server.NetworkManagementService;
import com.android.server.input.InputManagerService;
import com.android.server.os.HwBootFail;
import com.android.server.usb.descriptors.UsbACInterface;
import com.huawei.pgmng.log.LogPower;

public class SystemGesturesPointerEventListener implements WindowManagerPolicyConstants.PointerEventListener {
    private static final boolean DEBUG = false;
    /* access modifiers changed from: private */
    public static final boolean IS_USER_DOMESTIC_BETA;
    private static final int MAX_FLING_TIME_MILLIS = 5000;
    private static final int MAX_TRACKED_POINTERS = 32;
    private static final int PC_EVENT_KEY = 3;
    private static final int SWIPE_FROM_BOTTOM = 2;
    private static final int SWIPE_FROM_LEFT = 4;
    private static final int SWIPE_FROM_RIGHT = 3;
    private static final int SWIPE_FROM_TOP = 1;
    private static final int SWIPE_NONE = 0;
    private static final long SWIPE_TIMEOUT_MS = 500;
    private static final String TAG = "SystemGestures";
    private static final int UNTRACKED_POINTER = -1;
    /* access modifiers changed from: private */
    public final Callbacks mCallbacks;
    private final Context mContext;
    private boolean mDebugFireable;
    private final int[] mDownPointerId = new int[32];
    private int mDownPointers;
    private final long[] mDownTime = new long[32];
    private final float[] mDownX = new float[32];
    private final float[] mDownY = new float[32];
    private GestureDetector mGestureDetector;
    /* access modifiers changed from: private */
    public long mLastFlingTime;
    private boolean mMouseHoveringAtEdge;
    /* access modifiers changed from: private */
    public OverScroller mOverscroller;
    IHwPCManager mPCManager;
    private ScaleGestureDetector mScaleGestureDetector;
    private final int mSwipeDistanceThreshold;
    private boolean mSwipeFireable;
    private final int mSwipeStartThreshold;
    int screenHeight;
    int screenWidth;

    interface Callbacks {
        void onDebug();

        void onDown();

        void onFling(int i);

        void onMouseHoverAtBottom();

        void onMouseHoverAtTop();

        void onMouseLeaveFromEdge();

        void onSwipeFromBottom();

        void onSwipeFromLeft();

        void onSwipeFromRight();

        void onSwipeFromTop();

        void onUpOrCancel();
    }

    private final class FlingGestureDetector extends GestureDetector.SimpleOnGestureListener {
        private FlingGestureDetector() {
        }

        public boolean onSingleTapUp(MotionEvent e) {
            if (!SystemGesturesPointerEventListener.this.mOverscroller.isFinished()) {
                SystemGesturesPointerEventListener.this.mOverscroller.forceFinished(true);
            }
            return true;
        }

        public boolean onFling(MotionEvent down, MotionEvent up, float velocityX, float velocityY) {
            SystemGesturesPointerEventListener.this.mOverscroller.computeScrollOffset();
            long now = SystemClock.uptimeMillis();
            if (SystemGesturesPointerEventListener.this.mLastFlingTime != 0 && now > SystemGesturesPointerEventListener.this.mLastFlingTime + 5000) {
                SystemGesturesPointerEventListener.this.mOverscroller.forceFinished(true);
            }
            SystemGesturesPointerEventListener.this.mOverscroller.fling(0, 0, (int) velocityX, (int) velocityY, Integer.MIN_VALUE, HwBootFail.STAGE_BOOT_SUCCESS, Integer.MIN_VALUE, HwBootFail.STAGE_BOOT_SUCCESS);
            int duration = SystemGesturesPointerEventListener.this.mOverscroller.getDuration();
            if (duration > SystemGesturesPointerEventListener.MAX_FLING_TIME_MILLIS) {
                duration = SystemGesturesPointerEventListener.MAX_FLING_TIME_MILLIS;
            }
            long unused = SystemGesturesPointerEventListener.this.mLastFlingTime = now;
            HwPolicyFactory.reportToAware(15009, duration);
            SystemGesturesPointerEventListener.this.mCallbacks.onFling(duration);
            if (SystemGesturesPointerEventListener.IS_USER_DOMESTIC_BETA) {
                LogPower.push(NetworkManagementService.NetdResponseCode.IpFwdStatusResult, String.valueOf(velocityX), String.valueOf(velocityY));
            }
            return true;
        }

        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            HwPolicyFactory.reportToAware(15007, 0);
            return super.onScroll(e1, e2, distanceX, distanceY);
        }

        public boolean onSingleTapConfirmed(MotionEvent e) {
            if (SystemGesturesPointerEventListener.IS_USER_DOMESTIC_BETA) {
                LogPower.push(208);
            }
            return super.onSingleTapConfirmed(e);
        }

        public boolean onDoubleTap(MotionEvent e) {
            if (SystemGesturesPointerEventListener.IS_USER_DOMESTIC_BETA) {
                LogPower.push(209);
            }
            return super.onDoubleTap(e);
        }

        public void onLongPress(MotionEvent e) {
            if (SystemGesturesPointerEventListener.IS_USER_DOMESTIC_BETA) {
                LogPower.push(InputManagerService.SW_JACK_BITS);
            }
            super.onLongPress(e);
        }
    }

    private final class ScaleDetector extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        private ScaleDetector() {
        }

        public void onScaleEnd(ScaleGestureDetector detector) {
            if (SystemGesturesPointerEventListener.IS_USER_DOMESTIC_BETA) {
                LogPower.push(NetworkManagementService.NetdResponseCode.TetherStatusResult);
            }
        }
    }

    static {
        boolean z = true;
        if (SystemProperties.getInt("ro.logsystem.usertype", 1) != 3) {
            z = false;
        }
        IS_USER_DOMESTIC_BETA = z;
    }

    public SystemGesturesPointerEventListener(Context context, Callbacks callbacks) {
        this.mContext = context;
        this.mCallbacks = (Callbacks) checkNull("callbacks", callbacks);
        this.mSwipeStartThreshold = ((Context) checkNull("context", context)).getResources().getDimensionPixelSize(17105318);
        this.mSwipeDistanceThreshold = this.mSwipeStartThreshold;
    }

    private static <T> T checkNull(String name, T arg) {
        if (arg != null) {
            return arg;
        }
        throw new IllegalArgumentException(name + " must not be null");
    }

    public void systemReady() {
        Handler h = new Handler(Looper.myLooper());
        this.mGestureDetector = new GestureDetector(this.mContext, new FlingGestureDetector(), h);
        if (IS_USER_DOMESTIC_BETA) {
            this.mScaleGestureDetector = new ScaleGestureDetector(this.mContext, new ScaleDetector(), h);
        }
        this.mOverscroller = new OverScroller(this.mContext);
    }

    private boolean filterTouchPadEventForPCMode(MotionEvent event) {
        if (HwPCUtils.isPcCastModeInServer() && event.getSource() != 8194) {
            if (this.mPCManager == null) {
                this.mPCManager = HwPCUtils.getHwPCManager();
            }
            if (this.mPCManager != null) {
                try {
                    if (this.mPCManager.injectInputEventExternal(event, -1)) {
                        return true;
                    }
                } catch (RemoteException e) {
                    Slog.e(TAG, "RemoteException  " + e);
                }
                return false;
            }
        }
        return false;
    }

    public void onPointerEvent(MotionEvent event, int displayId) {
        if (!HwPCUtils.isValidExtDisplayId(displayId)) {
            filterTouchPadEventForPCMode(event);
        }
        SystemGesturesPointerEventListener.super.onPointerEvent(event, displayId);
    }

    public void onPointerEvent(MotionEvent event) {
        if (this.mGestureDetector != null && event.isTouchEvent()) {
            this.mGestureDetector.onTouchEvent(event);
        }
        if (IS_USER_DOMESTIC_BETA && this.mScaleGestureDetector != null && event.isTouchEvent()) {
            this.mScaleGestureDetector.onTouchEvent(event);
        }
        int actionMasked = event.getActionMasked();
        boolean z = false;
        if (actionMasked == 5) {
            captureDown(event, event.getActionIndex());
            if (this.mDebugFireable) {
                if (event.getPointerCount() < 5) {
                    z = true;
                }
                this.mDebugFireable = z;
                if (!this.mDebugFireable) {
                    this.mCallbacks.onDebug();
                }
            }
        } else if (actionMasked != 7) {
            switch (actionMasked) {
                case 0:
                    this.mSwipeFireable = true;
                    this.mDebugFireable = true;
                    this.mDownPointers = 0;
                    captureDown(event, 0);
                    if (this.mMouseHoveringAtEdge) {
                        this.mMouseHoveringAtEdge = false;
                        this.mCallbacks.onMouseLeaveFromEdge();
                    }
                    this.mCallbacks.onDown();
                    return;
                case 1:
                case 3:
                    HwPolicyFactory.reportToAware(85007, 0);
                    this.mSwipeFireable = false;
                    this.mDebugFireable = false;
                    this.mCallbacks.onUpOrCancel();
                    return;
                case 2:
                    if (this.mSwipeFireable) {
                        int swipe = detectSwipe(event);
                        if (swipe == 0) {
                            z = true;
                        }
                        this.mSwipeFireable = z;
                        if (swipe == 1) {
                            this.mCallbacks.onSwipeFromTop();
                            return;
                        } else if (swipe == 2) {
                            this.mCallbacks.onSwipeFromBottom();
                            return;
                        } else if (swipe == 3) {
                            this.mCallbacks.onSwipeFromRight();
                            return;
                        } else if (swipe == 4) {
                            this.mCallbacks.onSwipeFromLeft();
                            return;
                        } else {
                            return;
                        }
                    } else {
                        return;
                    }
                default:
                    return;
            }
        } else if (!event.isFromSource(UsbACInterface.FORMAT_III_IEC1937_MPEG1_Layer1)) {
        } else {
            if (!this.mMouseHoveringAtEdge && (event.getY() == 0.0f || (HwPCUtils.isPcCastModeInServer() && event.getY() <= 3.0f && event.getY() >= 0.0f))) {
                this.mCallbacks.onMouseHoverAtTop();
                this.mMouseHoveringAtEdge = true;
            } else if (!this.mMouseHoveringAtEdge && event.getY() >= ((float) (this.screenHeight - 1))) {
                this.mCallbacks.onMouseHoverAtBottom();
                this.mMouseHoveringAtEdge = true;
            } else if (this.mMouseHoveringAtEdge && event.getY() > 0.0f && event.getY() < ((float) (this.screenHeight - 1))) {
                this.mCallbacks.onMouseLeaveFromEdge();
                this.mMouseHoveringAtEdge = false;
            }
        }
    }

    private void captureDown(MotionEvent event, int pointerIndex) {
        int i = findIndex(event.getPointerId(pointerIndex));
        if (i != -1) {
            this.mDownX[i] = event.getX(pointerIndex);
            this.mDownY[i] = event.getY(pointerIndex);
            this.mDownTime[i] = event.getEventTime();
        }
    }

    private int findIndex(int pointerId) {
        for (int i = 0; i < this.mDownPointers; i++) {
            if (this.mDownPointerId[i] == pointerId) {
                return i;
            }
        }
        if (this.mDownPointers == 32 || pointerId == -1) {
            return -1;
        }
        int[] iArr = this.mDownPointerId;
        int i2 = this.mDownPointers;
        this.mDownPointers = i2 + 1;
        iArr[i2] = pointerId;
        return this.mDownPointers - 1;
    }

    private int detectSwipe(MotionEvent move) {
        MotionEvent motionEvent = move;
        int historySize = move.getHistorySize();
        int pointerCount = move.getPointerCount();
        for (int p = 0; p < pointerCount; p++) {
            int i = findIndex(motionEvent.getPointerId(p));
            if (i != -1) {
                int swipe = 0;
                while (true) {
                    int h = swipe;
                    if (h < historySize) {
                        int swipe2 = detectSwipe(i, motionEvent.getHistoricalEventTime(h), motionEvent.getHistoricalX(p, h), motionEvent.getHistoricalY(p, h));
                        if (swipe2 != 0) {
                            return swipe2;
                        }
                        swipe = h + 1;
                    } else {
                        int swipe3 = detectSwipe(i, move.getEventTime(), motionEvent.getX(p), motionEvent.getY(p));
                        if (swipe3 != 0) {
                            return swipe3;
                        }
                    }
                }
            }
        }
        return 0;
    }

    private int detectSwipe(int i, long time, float x, float y) {
        float fromX = this.mDownX[i];
        float fromY = this.mDownY[i];
        long elapsed = time - this.mDownTime[i];
        if (fromY <= ((float) this.mSwipeStartThreshold) && y > ((float) this.mSwipeDistanceThreshold) + fromY && elapsed < 500) {
            return 1;
        }
        if (fromY >= ((float) (this.screenHeight - this.mSwipeStartThreshold)) && y < fromY - ((float) this.mSwipeDistanceThreshold) && elapsed < 500) {
            return 2;
        }
        if (fromX >= ((float) (this.screenWidth - this.mSwipeStartThreshold)) && x < fromX - ((float) this.mSwipeDistanceThreshold) && elapsed < 500) {
            return 3;
        }
        if (fromX > ((float) this.mSwipeStartThreshold) || x <= ((float) this.mSwipeDistanceThreshold) + fromX || elapsed >= 500) {
            return 0;
        }
        return 4;
    }
}
