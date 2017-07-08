package com.android.server.policy;

import android.content.Context;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.util.Slog;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.WindowManagerPolicy.PointerEventListener;
import android.widget.OverScroller;
import com.android.server.usb.UsbAudioDevice;
import com.android.server.wm.WindowManagerService.H;

public class SystemGesturesPointerEventListener implements PointerEventListener {
    private static final boolean DEBUG = false;
    private static final int MAX_FLING_TIME_MILLIS = 5000;
    private static final int MAX_TRACKED_POINTERS = 32;
    private static final int SWIPE_FROM_BOTTOM = 2;
    private static final int SWIPE_FROM_RIGHT = 3;
    private static final int SWIPE_FROM_TOP = 1;
    private static final int SWIPE_NONE = 0;
    private static final long SWIPE_TIMEOUT_MS = 500;
    private static final String TAG = "SystemGestures";
    private static final int UNTRACKED_POINTER = -1;
    private IBinder mAwareService;
    private final Callbacks mCallbacks;
    private final Context mContext;
    private boolean mDebugFireable;
    private final int[] mDownPointerId;
    private int mDownPointers;
    private final long[] mDownTime;
    private final float[] mDownX;
    private final float[] mDownY;
    private GestureDetector mGestureDetector;
    private long mLastFlingTime;
    private boolean mMouseHoveringAtEdge;
    private OverScroller mOverscroller;
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

        void onSwipeFromRight();

        void onSwipeFromTop();

        void onUpOrCancel();
    }

    private final class FlingGestureDetector extends SimpleOnGestureListener {
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
            SystemGesturesPointerEventListener.this.mOverscroller.fling(SystemGesturesPointerEventListener.SWIPE_NONE, SystemGesturesPointerEventListener.SWIPE_NONE, (int) velocityX, (int) velocityY, UsbAudioDevice.kAudioDeviceMeta_Alsa, Integer.MAX_VALUE, UsbAudioDevice.kAudioDeviceMeta_Alsa, Integer.MAX_VALUE);
            int duration = SystemGesturesPointerEventListener.this.mOverscroller.getDuration();
            if (duration > SystemGesturesPointerEventListener.MAX_FLING_TIME_MILLIS) {
                duration = SystemGesturesPointerEventListener.MAX_FLING_TIME_MILLIS;
            }
            SystemGesturesPointerEventListener.this.mLastFlingTime = now;
            SystemGesturesPointerEventListener.this.reportScrollToAware(15009, duration);
            SystemGesturesPointerEventListener.this.mCallbacks.onFling(duration);
            return true;
        }

        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            SystemGesturesPointerEventListener.this.reportScrollToAware(15007, SystemGesturesPointerEventListener.SWIPE_NONE);
            return super.onScroll(e1, e2, distanceX, distanceY);
        }
    }

    public SystemGesturesPointerEventListener(Context context, Callbacks callbacks) {
        this.mDownPointerId = new int[MAX_TRACKED_POINTERS];
        this.mDownX = new float[MAX_TRACKED_POINTERS];
        this.mDownY = new float[MAX_TRACKED_POINTERS];
        this.mDownTime = new long[MAX_TRACKED_POINTERS];
        this.mAwareService = null;
        this.mContext = context;
        this.mCallbacks = (Callbacks) checkNull("callbacks", callbacks);
        this.mSwipeStartThreshold = ((Context) checkNull("context", context)).getResources().getDimensionPixelSize(17104919);
        this.mSwipeDistanceThreshold = this.mSwipeStartThreshold;
    }

    private static <T> T checkNull(String name, T arg) {
        if (arg != null) {
            return arg;
        }
        throw new IllegalArgumentException(name + " must not be null");
    }

    public void systemReady() {
        this.mGestureDetector = new GestureDetector(this.mContext, new FlingGestureDetector(), new Handler(Looper.myLooper()));
        this.mOverscroller = new OverScroller(this.mContext);
    }

    public void onPointerEvent(MotionEvent event) {
        boolean z = true;
        boolean z2 = DEBUG;
        if (this.mGestureDetector != null && event.isTouchEvent()) {
            this.mGestureDetector.onTouchEvent(event);
        }
        switch (event.getActionMasked()) {
            case SWIPE_NONE /*0*/:
                this.mSwipeFireable = true;
                this.mDebugFireable = true;
                this.mDownPointers = SWIPE_NONE;
                captureDown(event, SWIPE_NONE);
                if (this.mMouseHoveringAtEdge) {
                    this.mMouseHoveringAtEdge = DEBUG;
                    this.mCallbacks.onMouseLeaveFromEdge();
                }
                this.mCallbacks.onDown();
            case SWIPE_FROM_TOP /*1*/:
            case SWIPE_FROM_RIGHT /*3*/:
                reportScrollToAware(85007, SWIPE_NONE);
                this.mSwipeFireable = DEBUG;
                this.mDebugFireable = DEBUG;
                this.mCallbacks.onUpOrCancel();
            case SWIPE_FROM_BOTTOM /*2*/:
                if (this.mSwipeFireable) {
                    int swipe = detectSwipe(event);
                    if (swipe == 0) {
                        z2 = true;
                    }
                    this.mSwipeFireable = z2;
                    if (swipe == SWIPE_FROM_TOP) {
                        this.mCallbacks.onSwipeFromTop();
                    } else if (swipe == SWIPE_FROM_BOTTOM) {
                        this.mCallbacks.onSwipeFromBottom();
                    } else if (swipe == SWIPE_FROM_RIGHT) {
                        this.mCallbacks.onSwipeFromRight();
                    }
                }
            case H.ADD_STARTING /*5*/:
                captureDown(event, event.getActionIndex());
                if (this.mDebugFireable) {
                    if (event.getPointerCount() >= 5) {
                        z = DEBUG;
                    }
                    this.mDebugFireable = z;
                    if (!this.mDebugFireable) {
                        this.mCallbacks.onDebug();
                    }
                }
            case H.FINISHED_STARTING /*7*/:
                if (!event.isFromSource(8194)) {
                    return;
                }
                if (!this.mMouseHoveringAtEdge && event.getY() == 0.0f) {
                    this.mCallbacks.onMouseHoverAtTop();
                    this.mMouseHoveringAtEdge = true;
                } else if (!this.mMouseHoveringAtEdge && event.getY() >= ((float) (this.screenHeight + UNTRACKED_POINTER))) {
                    this.mCallbacks.onMouseHoverAtBottom();
                    this.mMouseHoveringAtEdge = true;
                } else if (this.mMouseHoveringAtEdge && event.getY() > 0.0f && event.getY() < ((float) (this.screenHeight + UNTRACKED_POINTER))) {
                    this.mCallbacks.onMouseLeaveFromEdge();
                    this.mMouseHoveringAtEdge = DEBUG;
                }
            default:
        }
    }

    private void captureDown(MotionEvent event, int pointerIndex) {
        int i = findIndex(event.getPointerId(pointerIndex));
        if (i != UNTRACKED_POINTER) {
            this.mDownX[i] = event.getX(pointerIndex);
            this.mDownY[i] = event.getY(pointerIndex);
            this.mDownTime[i] = event.getEventTime();
        }
    }

    private int findIndex(int pointerId) {
        for (int i = SWIPE_NONE; i < this.mDownPointers; i += SWIPE_FROM_TOP) {
            if (this.mDownPointerId[i] == pointerId) {
                return i;
            }
        }
        if (this.mDownPointers == MAX_TRACKED_POINTERS || pointerId == UNTRACKED_POINTER) {
            return UNTRACKED_POINTER;
        }
        int[] iArr = this.mDownPointerId;
        int i2 = this.mDownPointers;
        this.mDownPointers = i2 + SWIPE_FROM_TOP;
        iArr[i2] = pointerId;
        return this.mDownPointers + UNTRACKED_POINTER;
    }

    private int detectSwipe(MotionEvent move) {
        int historySize = move.getHistorySize();
        int pointerCount = move.getPointerCount();
        for (int p = SWIPE_NONE; p < pointerCount; p += SWIPE_FROM_TOP) {
            int i = findIndex(move.getPointerId(p));
            if (i != UNTRACKED_POINTER) {
                int swipe;
                for (int h = SWIPE_NONE; h < historySize; h += SWIPE_FROM_TOP) {
                    swipe = detectSwipe(i, move.getHistoricalEventTime(h), move.getHistoricalX(p, h), move.getHistoricalY(p, h));
                    if (swipe != 0) {
                        return swipe;
                    }
                }
                swipe = detectSwipe(i, move.getEventTime(), move.getX(p), move.getY(p));
                if (swipe != 0) {
                    return swipe;
                }
            }
        }
        return SWIPE_NONE;
    }

    private int detectSwipe(int i, long time, float x, float y) {
        float fromX = this.mDownX[i];
        float fromY = this.mDownY[i];
        long elapsed = time - this.mDownTime[i];
        if (fromY <= ((float) this.mSwipeStartThreshold) && y > ((float) this.mSwipeDistanceThreshold) + fromY && elapsed < SWIPE_TIMEOUT_MS) {
            return SWIPE_FROM_TOP;
        }
        if (fromY >= ((float) (this.screenHeight - this.mSwipeStartThreshold)) && y < fromY - ((float) this.mSwipeDistanceThreshold) && elapsed < SWIPE_TIMEOUT_MS) {
            return SWIPE_FROM_BOTTOM;
        }
        if (fromX < ((float) (this.screenWidth - this.mSwipeStartThreshold)) || x >= fromX - ((float) this.mSwipeDistanceThreshold) || elapsed >= SWIPE_TIMEOUT_MS) {
            return SWIPE_NONE;
        }
        return SWIPE_FROM_RIGHT;
    }

    private void reportScrollToAware(int code, int duration) {
        if (this.mAwareService == null) {
            this.mAwareService = ServiceManager.getService("hwsysresmanager");
        }
        if (this.mAwareService != null) {
            Parcel data = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            try {
                data.writeInterfaceToken("android.rms.IHwSysResManager");
                data.writeInt(duration);
                this.mAwareService.transact(code, data, reply, SWIPE_NONE);
                reply.readException();
            } catch (RemoteException e) {
                Slog.e(TAG, "mAwareService ontransact " + e.getMessage());
            } finally {
                data.recycle();
                reply.recycle();
            }
            return;
        }
        Slog.e(TAG, "mAwareService is not start");
    }
}
