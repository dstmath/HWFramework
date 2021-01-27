package com.android.server.wm;

import android.content.Context;
import android.graphics.Point;
import android.graphics.Region;
import android.hardware.display.DisplayManagerGlobal;
import android.os.Handler;
import android.os.RemoteException;
import android.os.SystemClock;
import android.pc.IHwPCManager;
import android.util.HwPCUtils;
import android.util.Slog;
import android.view.DisplayCutout;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.WindowManagerPolicyConstants;
import android.widget.OverScroller;
import com.android.server.policy.HwPolicyFactory;

/* access modifiers changed from: package-private */
public class SystemGesturesPointerEventListener implements WindowManagerPolicyConstants.PointerEventListener {
    private static final boolean DEBUG = false;
    private static final int MAX_FLING_TIME_MILLIS = 5000;
    private static final int MAX_TRACKED_POINTERS = 32;
    private static final int PC_EVENT_KEY = 5;
    private static final int SWIPE_FROM_BOTTOM = 2;
    private static final int SWIPE_FROM_LEFT = 4;
    private static final int SWIPE_FROM_RIGHT = 3;
    private static final int SWIPE_FROM_TOP = 1;
    private static final int SWIPE_NONE = 0;
    private static final long SWIPE_TIMEOUT_MS = 500;
    private static final String TAG = "SystemGestures";
    private static final int UNTRACKED_POINTER = -1;
    private final Callbacks mCallbacks;
    private final Context mContext;
    private boolean mDebugFireable;
    private DisplayContent mDisplay = null;
    private int mDisplayCutoutTouchableRegionSize;
    private final int[] mDownPointerId = new int[32];
    private int mDownPointers;
    private final long[] mDownTime = new long[32];
    private final float[] mDownX = new float[32];
    private final float[] mDownY = new float[32];
    private GestureDetector mGestureDetector;
    private final Handler mHandler;
    private long mLastFlingTime;
    private boolean mMouseHoveringAtEdge;
    IHwPCManager mPCManager;
    private int mSwipeDistanceThreshold;
    private boolean mSwipeFireable;
    private int mSwipeStartThreshold;
    int screenHeight;
    int screenWidth;

    /* access modifiers changed from: package-private */
    public interface Callbacks {
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

    SystemGesturesPointerEventListener(Context context, Handler handler, Callbacks callbacks) {
        this.mContext = (Context) checkNull("context", context);
        this.mHandler = handler;
        this.mCallbacks = (Callbacks) checkNull("callbacks", callbacks);
        onConfigurationChanged();
    }

    /* access modifiers changed from: package-private */
    public void onConfigurationChanged() {
        this.mSwipeStartThreshold = this.mContext.getResources().getDimensionPixelSize(17105445);
        DisplayCutout displayCutout = DisplayManagerGlobal.getInstance().getRealDisplay(0).getCutout();
        if (displayCutout != null && !displayCutout.getBoundingRectTop().isEmpty()) {
            this.mDisplayCutoutTouchableRegionSize = this.mContext.getResources().getDimensionPixelSize(17105143);
            this.mSwipeStartThreshold += this.mDisplayCutoutTouchableRegionSize;
        }
        this.mSwipeDistanceThreshold = this.mSwipeStartThreshold;
    }

    private static <T> T checkNull(String name, T arg) {
        if (arg != null) {
            return arg;
        }
        throw new IllegalArgumentException(name + " must not be null");
    }

    public /* synthetic */ void lambda$systemReady$0$SystemGesturesPointerEventListener() {
        this.mGestureDetector = new GestureDetector(this.mContext, new FlingGestureDetector(), this.mHandler) {
            /* class com.android.server.wm.SystemGesturesPointerEventListener.AnonymousClass1 */
        };
    }

    public void systemReady() {
        this.mHandler.post(new Runnable() {
            /* class com.android.server.wm.$$Lambda$SystemGesturesPointerEventListener$9Iw39fjTtjXO5kacgrpdxfxjuSY */

            @Override // java.lang.Runnable
            public final void run() {
                SystemGesturesPointerEventListener.this.lambda$systemReady$0$SystemGesturesPointerEventListener();
            }
        });
    }

    private boolean filterTouchPadEventForPCMode(MotionEvent event) {
        if (HwPCUtils.isPcCastModeInServer() && event.getSource() != 8194) {
            if (this.mPCManager == null) {
                this.mPCManager = HwPCUtils.getHwPCManager();
            }
            IHwPCManager iHwPCManager = this.mPCManager;
            if (iHwPCManager != null) {
                try {
                    if (iHwPCManager.injectInputEventExternal(event, -1)) {
                        return true;
                    }
                    return false;
                } catch (RemoteException e) {
                    Slog.e(TAG, "RemoteException  " + e);
                }
            }
        }
        return false;
    }

    public void onPointerEvent(MotionEvent event) {
        if (!HwPCUtils.isValidExtDisplayId(event.getDisplayId())) {
            filterTouchPadEventForPCMode(event);
        }
        if (this.mGestureDetector != null && event.isTouchEvent()) {
            this.mGestureDetector.onTouchEvent(event);
        }
        int actionMasked = event.getActionMasked();
        boolean z = false;
        if (actionMasked != 0) {
            if (actionMasked != 1) {
                if (actionMasked != 2) {
                    if (actionMasked != 3) {
                        if (actionMasked == 5) {
                            captureDown(event, event.getActionIndex());
                            if (this.mDebugFireable) {
                                if (event.getPointerCount() < 5) {
                                    z = true;
                                }
                                this.mDebugFireable = z;
                                if (!this.mDebugFireable) {
                                    this.mCallbacks.onDebug();
                                    return;
                                }
                                return;
                            }
                            return;
                        } else if (actionMasked != 7 || !event.isFromSource(8194)) {
                            return;
                        } else {
                            if (!this.mMouseHoveringAtEdge && (event.getY() == 0.0f || (HwPCUtils.isPcCastModeInServer() && event.getY() <= 5.0f && event.getY() >= 0.0f))) {
                                this.mCallbacks.onMouseHoverAtTop();
                                this.mMouseHoveringAtEdge = true;
                                return;
                            } else if (!this.mMouseHoveringAtEdge && event.getY() >= ((float) (this.screenHeight - 1))) {
                                this.mCallbacks.onMouseHoverAtBottom();
                                this.mMouseHoveringAtEdge = true;
                                return;
                            } else if (this.mMouseHoveringAtEdge && event.getY() > 0.0f && event.getY() < ((float) (this.screenHeight - 1))) {
                                this.mCallbacks.onMouseLeaveFromEdge();
                                this.mMouseHoveringAtEdge = false;
                                return;
                            } else {
                                return;
                            }
                        }
                    }
                } else if (this.mSwipeFireable) {
                    int swipe = detectSwipe(event);
                    if (swipe == 0) {
                        z = true;
                    }
                    this.mSwipeFireable = z;
                    if (swipe == 1) {
                        DisplayContent displayContent = this.mDisplay;
                        if (displayContent != null && !displayContent.mWmService.mAtmService.mHwATMSEx.blockSwipeFromTop(event, this.mDisplay)) {
                            this.mCallbacks.onSwipeFromTop();
                            return;
                        }
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
            }
            HwPolicyFactory.reportToAware(85007, 0);
            this.mSwipeFireable = false;
            this.mDebugFireable = false;
            this.mCallbacks.onUpOrCancel();
            return;
        }
        this.mSwipeFireable = true;
        this.mDebugFireable = true;
        this.mDownPointers = 0;
        captureDown(event, 0);
        if (this.mMouseHoveringAtEdge) {
            this.mMouseHoveringAtEdge = false;
            this.mCallbacks.onMouseLeaveFromEdge();
        }
        this.mCallbacks.onDown();
    }

    private void captureDown(MotionEvent event, int pointerIndex) {
        int i = findIndex(event.getPointerId(pointerIndex));
        if (i != -1) {
            this.mDownX[i] = event.getX(pointerIndex);
            this.mDownY[i] = event.getY(pointerIndex);
            this.mDownTime[i] = event.getEventTime();
        }
    }

    /* access modifiers changed from: protected */
    public boolean currentGestureStartedInRegion(Region r) {
        return r.contains((int) this.mDownX[0], (int) this.mDownY[0]);
    }

    public Point getGestureStartedPoint() {
        return new Point((int) this.mDownX[0], (int) this.mDownY[0]);
    }

    private int findIndex(int pointerId) {
        int i = 0;
        while (true) {
            int i2 = this.mDownPointers;
            if (i < i2) {
                if (this.mDownPointerId[i] == pointerId) {
                    return i;
                }
                i++;
            } else if (i2 == 32 || pointerId == -1) {
                return -1;
            } else {
                int[] iArr = this.mDownPointerId;
                this.mDownPointers = i2 + 1;
                iArr[i2] = pointerId;
                return this.mDownPointers - 1;
            }
        }
    }

    private int detectSwipe(MotionEvent move) {
        int historySize = move.getHistorySize();
        int pointerCount = move.getPointerCount();
        for (int p = 0; p < pointerCount; p++) {
            int i = findIndex(move.getPointerId(p));
            if (i != -1) {
                for (int h = 0; h < historySize; h++) {
                    int swipe = detectSwipe(i, move.getHistoricalEventTime(h), move.getHistoricalX(p, h), move.getHistoricalY(p, h));
                    if (swipe != 0) {
                        return swipe;
                    }
                }
                int swipe2 = detectSwipe(i, move.getEventTime(), move.getX(p), move.getY(p));
                if (swipe2 != 0) {
                    return swipe2;
                }
            }
        }
        return 0;
    }

    private int detectSwipe(int i, long time, float x, float y) {
        float fromX = this.mDownX[i];
        float fromY = this.mDownY[i];
        long elapsed = time - this.mDownTime[i];
        if (fromY <= ((float) this.mSwipeStartThreshold) && y > ((float) this.mSwipeDistanceThreshold) + fromY && elapsed < SWIPE_TIMEOUT_MS) {
            return 1;
        }
        if (fromY >= ((float) (this.screenHeight - this.mSwipeStartThreshold)) && y < fromY - ((float) this.mSwipeDistanceThreshold) && elapsed < SWIPE_TIMEOUT_MS) {
            return 2;
        }
        if (fromX >= ((float) (this.screenWidth - this.mSwipeStartThreshold)) && x < fromX - ((float) this.mSwipeDistanceThreshold) && elapsed < SWIPE_TIMEOUT_MS) {
            return 3;
        }
        if (fromX > ((float) this.mSwipeStartThreshold) || x <= ((float) this.mSwipeDistanceThreshold) + fromX || elapsed >= SWIPE_TIMEOUT_MS) {
            return 0;
        }
        return 4;
    }

    public void setDisplayContent(DisplayContent display) {
        this.mDisplay = display;
    }

    private final class FlingGestureDetector extends GestureDetector.SimpleOnGestureListener {
        private OverScroller mOverscroller;

        FlingGestureDetector() {
            this.mOverscroller = new OverScroller(SystemGesturesPointerEventListener.this.mContext);
        }

        @Override // android.view.GestureDetector.SimpleOnGestureListener, android.view.GestureDetector.OnGestureListener
        public boolean onSingleTapUp(MotionEvent e) {
            if (!this.mOverscroller.isFinished()) {
                this.mOverscroller.forceFinished(true);
            }
            return true;
        }

        @Override // android.view.GestureDetector.SimpleOnGestureListener, android.view.GestureDetector.OnGestureListener
        public boolean onFling(MotionEvent down, MotionEvent up, float velocityX, float velocityY) {
            this.mOverscroller.computeScrollOffset();
            long now = SystemClock.uptimeMillis();
            if (SystemGesturesPointerEventListener.this.mLastFlingTime != 0 && now > SystemGesturesPointerEventListener.this.mLastFlingTime + 5000) {
                this.mOverscroller.forceFinished(true);
            }
            this.mOverscroller.fling(0, 0, (int) velocityX, (int) velocityY, Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE);
            int duration = this.mOverscroller.getDuration();
            if (duration > 5000) {
                duration = 5000;
            }
            SystemGesturesPointerEventListener.this.mLastFlingTime = now;
            HwPolicyFactory.reportToAware(15009, duration);
            SystemGesturesPointerEventListener.this.mCallbacks.onFling(duration);
            return true;
        }

        @Override // android.view.GestureDetector.SimpleOnGestureListener, android.view.GestureDetector.OnGestureListener
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            HwPolicyFactory.reportToAware(15007, 0);
            return super.onScroll(e1, e2, distanceX, distanceY);
        }
    }
}
