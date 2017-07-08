package com.google.android.maps;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.ViewConfiguration;
import com.google.android.maps.MapView.LayoutParams;

class GestureDetector {
    private static final int DOUBLE_TAP_TIMEOUT = 0;
    private static final int LONGPRESS_TIMEOUT = 0;
    private static final int TAP_TIMEOUT = 0;
    private boolean mAlwaysInBiggerTapRegion;
    private boolean mAlwaysInTapRegion;
    private MotionEvent mCurrentDownEvent;
    private OnDoubleTapListener mDoubleTapListener;
    private int mDoubleTapSlopSquare;
    private int mDoubleTapTouchSlopSquare;
    private float mDownFocusX;
    private float mDownFocusY;
    private final Handler mHandler;
    private boolean mInLongPress;
    private boolean mIsDoubleTapping;
    private boolean mIsLongpressEnabled;
    private float mLastFocusX;
    private float mLastFocusY;
    private final OnGestureListener mListener;
    private int mMaximumFlingVelocity;
    private int mMinimumFlingVelocity;
    private MotionEvent mPreviousUpEvent;
    private boolean mStillDown;
    private int mTouchSlopSquare;
    private VelocityTracker mVelocityTracker;

    private class GestureHandler extends Handler {
        GestureHandler() {
        }

        GestureHandler(Handler handler) {
            super(handler.getLooper());
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case OverlayItem.ITEM_STATE_PRESSED_MASK /*1*/:
                    GestureDetector.this.mListener.onShowPress(GestureDetector.this.mCurrentDownEvent);
                case OverlayItem.ITEM_STATE_SELECTED_MASK /*2*/:
                    GestureDetector.this.dispatchLongPress();
                case LayoutParams.LEFT /*3*/:
                    if (GestureDetector.this.mDoubleTapListener != null && !GestureDetector.this.mStillDown) {
                        GestureDetector.this.mDoubleTapListener.onSingleTapConfirmed(GestureDetector.this.mCurrentDownEvent);
                    }
                default:
                    throw new RuntimeException("Unknown message " + msg);
            }
        }
    }

    public interface OnDoubleTapListener {
        boolean onDoubleTap(MotionEvent motionEvent);

        boolean onDoubleTapEvent(MotionEvent motionEvent);

        boolean onSingleTapConfirmed(MotionEvent motionEvent);
    }

    public interface OnGestureListener {
        boolean onDown(MotionEvent motionEvent);

        boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent2, float f, float f2);

        void onLongPress(MotionEvent motionEvent);

        boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent2, float f, float f2);

        void onShowPress(MotionEvent motionEvent);

        boolean onSingleTapUp(MotionEvent motionEvent);
    }

    public static class SimpleOnGestureListener implements OnGestureListener, OnDoubleTapListener {
        public boolean onSingleTapUp(MotionEvent e) {
            return false;
        }

        public void onLongPress(MotionEvent e) {
        }

        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            return false;
        }

        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            return false;
        }

        public void onShowPress(MotionEvent e) {
        }

        public boolean onDown(MotionEvent e) {
            return false;
        }

        public boolean onDoubleTap(MotionEvent e) {
            return false;
        }

        public boolean onDoubleTapEvent(MotionEvent e) {
            return false;
        }

        public boolean onSingleTapConfirmed(MotionEvent e) {
            return false;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.google.android.maps.GestureDetector.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.google.android.maps.GestureDetector.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.google.android.maps.GestureDetector.<clinit>():void");
    }

    public GestureDetector(Context context, OnGestureListener listener) {
        this(context, listener, null);
    }

    public GestureDetector(Context context, OnGestureListener listener, Handler handler) {
        if (handler != null) {
            this.mHandler = new GestureHandler(handler);
        } else {
            this.mHandler = new GestureHandler();
        }
        this.mListener = listener;
        if (listener instanceof OnDoubleTapListener) {
            setOnDoubleTapListener((OnDoubleTapListener) listener);
        }
        init(context);
    }

    private void init(Context context) {
        if (this.mListener == null) {
            throw new NullPointerException("OnGestureListener must not be null");
        }
        int touchSlop;
        int doubleTapTouchSlop;
        int doubleTapSlop;
        this.mIsLongpressEnabled = true;
        if (context == null) {
            touchSlop = ViewConfiguration.getTouchSlop();
            doubleTapTouchSlop = touchSlop;
            doubleTapSlop = ViewConfiguration.getDoubleTapSlop();
            this.mMinimumFlingVelocity = ViewConfiguration.getMinimumFlingVelocity();
            this.mMaximumFlingVelocity = ViewConfiguration.getMaximumFlingVelocity();
        } else {
            ViewConfiguration configuration = ViewConfiguration.get(context);
            touchSlop = configuration.getScaledTouchSlop();
            doubleTapTouchSlop = configuration.getScaledDoubleTapTouchSlop();
            doubleTapSlop = configuration.getScaledDoubleTapSlop();
            this.mMinimumFlingVelocity = configuration.getScaledMinimumFlingVelocity();
            this.mMaximumFlingVelocity = configuration.getScaledMaximumFlingVelocity();
        }
        this.mTouchSlopSquare = touchSlop * touchSlop;
        this.mDoubleTapTouchSlopSquare = doubleTapTouchSlop * doubleTapTouchSlop;
        this.mDoubleTapSlopSquare = doubleTapSlop * doubleTapSlop;
    }

    public void setOnDoubleTapListener(OnDoubleTapListener onDoubleTapListener) {
        this.mDoubleTapListener = onDoubleTapListener;
    }

    public void setIsLongpressEnabled(boolean isLongpressEnabled) {
        this.mIsLongpressEnabled = isLongpressEnabled;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean onTouchEvent(MotionEvent ev) {
        int div;
        int action = ev.getAction();
        if (this.mVelocityTracker == null) {
            this.mVelocityTracker = VelocityTracker.obtain();
        }
        this.mVelocityTracker.addMovement(ev);
        int i = action & 255;
        boolean pointerUp = r0 == 6;
        int skipIndex = pointerUp ? ev.getActionIndex() : -1;
        float sumX = 0.0f;
        float sumY = 0.0f;
        int count = ev.getPointerCount();
        for (int i2 = 0; i2 < count; i2++) {
            if (skipIndex != i2) {
                sumX += ev.getX(i2);
                sumY += ev.getY(i2);
            }
        }
        if (pointerUp) {
            div = count - 1;
        } else {
            div = count;
        }
        float focusX = sumX / ((float) div);
        float focusY = sumY / ((float) div);
        boolean handled = false;
        switch (action & 255) {
            case LayoutParams.MODE_MAP /*0*/:
                if (this.mDoubleTapListener != null) {
                    boolean hadTapMessage = this.mHandler.hasMessages(3);
                    if (hadTapMessage) {
                        this.mHandler.removeMessages(3);
                    }
                    if (!(this.mCurrentDownEvent == null || this.mPreviousUpEvent == null || !hadTapMessage)) {
                        if (isConsideredDoubleTap(this.mCurrentDownEvent, this.mPreviousUpEvent, ev)) {
                            this.mIsDoubleTapping = true;
                            handled = this.mDoubleTapListener.onDoubleTap(this.mCurrentDownEvent) | this.mDoubleTapListener.onDoubleTapEvent(ev);
                        }
                    }
                    this.mHandler.sendEmptyMessageDelayed(3, (long) DOUBLE_TAP_TIMEOUT);
                }
                this.mLastFocusX = focusX;
                this.mDownFocusX = focusX;
                this.mLastFocusY = focusY;
                this.mDownFocusY = focusY;
                if (this.mCurrentDownEvent != null) {
                    this.mCurrentDownEvent.recycle();
                }
                this.mCurrentDownEvent = MotionEvent.obtain(ev);
                this.mAlwaysInTapRegion = true;
                this.mAlwaysInBiggerTapRegion = true;
                this.mStillDown = true;
                this.mInLongPress = false;
                if (this.mIsLongpressEnabled) {
                    this.mHandler.removeMessages(2);
                    this.mHandler.sendEmptyMessageAtTime(2, (this.mCurrentDownEvent.getDownTime() + ((long) TAP_TIMEOUT)) + ((long) LONGPRESS_TIMEOUT));
                }
                this.mHandler.sendEmptyMessageAtTime(1, this.mCurrentDownEvent.getDownTime() + ((long) TAP_TIMEOUT));
                return handled | this.mListener.onDown(ev);
            case OverlayItem.ITEM_STATE_PRESSED_MASK /*1*/:
                this.mStillDown = false;
                MotionEvent currentUpEvent = MotionEvent.obtain(ev);
                if (this.mIsDoubleTapping) {
                    handled = this.mDoubleTapListener.onDoubleTapEvent(ev);
                } else if (this.mInLongPress) {
                    this.mHandler.removeMessages(3);
                    this.mInLongPress = false;
                } else if (this.mAlwaysInTapRegion) {
                    handled = this.mListener.onSingleTapUp(ev);
                } else {
                    VelocityTracker velocityTracker = this.mVelocityTracker;
                    velocityTracker.computeCurrentVelocity(1000, (float) this.mMaximumFlingVelocity);
                    float velocityY = velocityTracker.getYVelocity();
                    float velocityX = velocityTracker.getXVelocity();
                    if (Math.abs(velocityY) <= ((float) this.mMinimumFlingVelocity)) {
                        break;
                    }
                    handled = this.mListener.onFling(this.mCurrentDownEvent, ev, velocityX, velocityY);
                }
                if (this.mPreviousUpEvent != null) {
                    this.mPreviousUpEvent.recycle();
                }
                this.mPreviousUpEvent = currentUpEvent;
                if (this.mVelocityTracker != null) {
                    this.mVelocityTracker.recycle();
                    this.mVelocityTracker = null;
                }
                this.mIsDoubleTapping = false;
                this.mHandler.removeMessages(1);
                this.mHandler.removeMessages(2);
                return handled;
            case OverlayItem.ITEM_STATE_SELECTED_MASK /*2*/:
                if (this.mInLongPress) {
                    return false;
                }
                float scrollX = this.mLastFocusX - focusX;
                float scrollY = this.mLastFocusY - focusY;
                if (this.mIsDoubleTapping) {
                    return this.mDoubleTapListener.onDoubleTapEvent(ev);
                } else if (this.mAlwaysInTapRegion) {
                    int deltaX = (int) (focusX - this.mDownFocusX);
                    int deltaY = (int) (focusY - this.mDownFocusY);
                    int distance = (deltaX * deltaX) + (deltaY * deltaY);
                    i = this.mTouchSlopSquare;
                    if (distance > r0) {
                        handled = this.mListener.onScroll(this.mCurrentDownEvent, ev, scrollX, scrollY);
                        this.mLastFocusX = focusX;
                        this.mLastFocusY = focusY;
                        this.mAlwaysInTapRegion = false;
                        this.mHandler.removeMessages(3);
                        this.mHandler.removeMessages(1);
                        this.mHandler.removeMessages(2);
                    }
                    i = this.mDoubleTapTouchSlopSquare;
                    if (distance <= r0) {
                        return handled;
                    }
                    this.mAlwaysInBiggerTapRegion = false;
                    return handled;
                } else if (Math.abs(scrollX) < 1.0f && Math.abs(scrollY) < 1.0f) {
                    return false;
                } else {
                    handled = this.mListener.onScroll(this.mCurrentDownEvent, ev, scrollX, scrollY);
                    this.mLastFocusX = focusX;
                    this.mLastFocusY = focusY;
                    return handled;
                }
            case LayoutParams.LEFT /*3*/:
                cancel();
                return false;
            case LayoutParams.RIGHT /*5*/:
                this.mLastFocusX = focusX;
                this.mDownFocusX = focusX;
                this.mLastFocusY = focusY;
                this.mDownFocusY = focusY;
                cancelTaps();
                return false;
            case 6:
                this.mLastFocusX = focusX;
                this.mDownFocusX = focusX;
                this.mLastFocusY = focusY;
                this.mDownFocusY = focusY;
                this.mVelocityTracker.clear();
                return false;
            default:
                return false;
        }
    }

    private void cancel() {
        this.mHandler.removeMessages(1);
        this.mHandler.removeMessages(2);
        this.mHandler.removeMessages(3);
        this.mVelocityTracker.recycle();
        this.mVelocityTracker = null;
        this.mIsDoubleTapping = false;
        this.mStillDown = false;
        this.mAlwaysInTapRegion = false;
        this.mAlwaysInBiggerTapRegion = false;
        if (this.mInLongPress) {
            this.mInLongPress = false;
        }
    }

    private void cancelTaps() {
        this.mHandler.removeMessages(1);
        this.mHandler.removeMessages(2);
        this.mHandler.removeMessages(3);
        this.mIsDoubleTapping = false;
        this.mAlwaysInTapRegion = false;
        this.mAlwaysInBiggerTapRegion = false;
        if (this.mInLongPress) {
            this.mInLongPress = false;
        }
    }

    private boolean isConsideredDoubleTap(MotionEvent firstDown, MotionEvent firstUp, MotionEvent secondDown) {
        boolean z = false;
        if (!this.mAlwaysInBiggerTapRegion || secondDown.getEventTime() - firstUp.getEventTime() > ((long) DOUBLE_TAP_TIMEOUT)) {
            return false;
        }
        int deltaX = ((int) firstDown.getX()) - ((int) secondDown.getX());
        int deltaY = ((int) firstDown.getY()) - ((int) secondDown.getY());
        if ((deltaX * deltaX) + (deltaY * deltaY) < this.mDoubleTapSlopSquare) {
            z = true;
        }
        return z;
    }

    private void dispatchLongPress() {
        this.mHandler.removeMessages(3);
        this.mInLongPress = true;
        this.mListener.onLongPress(this.mCurrentDownEvent);
    }
}
