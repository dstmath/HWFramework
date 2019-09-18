package android.view;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class GestureDetector {
    private static final int DOUBLE_TAP_MIN_TIME = ViewConfiguration.getDoubleTapMinTime();
    private static final int DOUBLE_TAP_TIMEOUT = ViewConfiguration.getDoubleTapTimeout();
    private static final int LONGPRESS_TIMEOUT = ViewConfiguration.getLongPressTimeout();
    private static final int LONG_PRESS = 2;
    private static final int SHOW_PRESS = 1;
    private static final int TAP = 3;
    private static final int TAP_TIMEOUT = ViewConfiguration.getTapTimeout();
    private boolean mAlwaysInBiggerTapRegion;
    private boolean mAlwaysInTapRegion;
    private OnContextClickListener mContextClickListener;
    /* access modifiers changed from: private */
    public MotionEvent mCurrentDownEvent;
    private int mCustomLongpressTimeout;
    /* access modifiers changed from: private */
    public boolean mDeferConfirmSingleTap;
    /* access modifiers changed from: private */
    public OnDoubleTapListener mDoubleTapListener;
    private int mDoubleTapSlopSquare;
    private int mDoubleTapTouchSlopSquare;
    private float mDownFocusX;
    private float mDownFocusY;
    private final Handler mHandler;
    private boolean mIgnoreNextUpEvent;
    private boolean mInContextClick;
    private boolean mInLongPress;
    private final InputEventConsistencyVerifier mInputEventConsistencyVerifier;
    private boolean mIsDoubleTapping;
    private boolean mIsLongpressEnabled;
    private float mLastFocusX;
    private float mLastFocusY;
    /* access modifiers changed from: private */
    public final OnGestureListener mListener;
    private int mMaximumFlingVelocity;
    private int mMinimumFlingVelocity;
    private MotionEvent mPreviousUpEvent;
    /* access modifiers changed from: private */
    public boolean mStillDown;
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
                case 1:
                    GestureDetector.this.mListener.onShowPress(GestureDetector.this.mCurrentDownEvent);
                    return;
                case 2:
                    GestureDetector.this.dispatchLongPress();
                    return;
                case 3:
                    if (GestureDetector.this.mDoubleTapListener == null) {
                        return;
                    }
                    if (!GestureDetector.this.mStillDown) {
                        GestureDetector.this.mDoubleTapListener.onSingleTapConfirmed(GestureDetector.this.mCurrentDownEvent);
                        return;
                    } else {
                        boolean unused = GestureDetector.this.mDeferConfirmSingleTap = true;
                        return;
                    }
                default:
                    throw new RuntimeException("Unknown message " + msg);
            }
        }
    }

    public interface OnContextClickListener {
        boolean onContextClick(MotionEvent motionEvent);
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

    public static class SimpleOnGestureListener implements OnGestureListener, OnDoubleTapListener, OnContextClickListener {
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

        public boolean onContextClick(MotionEvent e) {
            return false;
        }
    }

    @Deprecated
    public GestureDetector(OnGestureListener listener, Handler handler) {
        this(null, listener, handler);
    }

    @Deprecated
    public GestureDetector(OnGestureListener listener) {
        this(null, listener, null);
    }

    public GestureDetector(Context context, OnGestureListener listener) {
        this(context, listener, null);
    }

    public GestureDetector(Context context, OnGestureListener listener, Handler handler) {
        this.mInputEventConsistencyVerifier = InputEventConsistencyVerifier.isInstrumentationEnabled() ? new InputEventConsistencyVerifier(this, 0) : null;
        if (handler != null) {
            this.mHandler = new GestureHandler(handler);
        } else {
            this.mHandler = new GestureHandler();
        }
        this.mListener = listener;
        if (listener instanceof OnDoubleTapListener) {
            setOnDoubleTapListener((OnDoubleTapListener) listener);
        }
        if (listener instanceof OnContextClickListener) {
            setContextClickListener((OnContextClickListener) listener);
        }
        init(context);
    }

    public GestureDetector(Context context, OnGestureListener listener, Handler handler, boolean unused) {
        this(context, listener, handler);
    }

    private void init(Context context) {
        int doubleTapTouchSlop;
        int touchSlop;
        int touchSlop2;
        if (this.mListener != null) {
            this.mIsLongpressEnabled = true;
            if (context == null) {
                touchSlop2 = ViewConfiguration.getTouchSlop();
                touchSlop = touchSlop2;
                doubleTapTouchSlop = ViewConfiguration.getDoubleTapSlop();
                this.mMinimumFlingVelocity = ViewConfiguration.getMinimumFlingVelocity();
                this.mMaximumFlingVelocity = ViewConfiguration.getMaximumFlingVelocity();
            } else {
                ViewConfiguration configuration = ViewConfiguration.get(context);
                int touchSlop3 = configuration.getScaledTouchSlop();
                int doubleTapTouchSlop2 = configuration.getScaledDoubleTapTouchSlop();
                int doubleTapSlop = configuration.getScaledDoubleTapSlop();
                this.mMinimumFlingVelocity = configuration.getScaledMinimumFlingVelocity();
                this.mMaximumFlingVelocity = configuration.getScaledMaximumFlingVelocity();
                touchSlop2 = touchSlop3;
                touchSlop = doubleTapTouchSlop2;
                doubleTapTouchSlop = doubleTapSlop;
            }
            this.mTouchSlopSquare = touchSlop2 * touchSlop2;
            this.mDoubleTapTouchSlopSquare = touchSlop * touchSlop;
            this.mDoubleTapSlopSquare = doubleTapTouchSlop * doubleTapTouchSlop;
            this.mCustomLongpressTimeout = 0;
            return;
        }
        throw new NullPointerException("OnGestureListener must not be null");
    }

    public void setOnDoubleTapListener(OnDoubleTapListener onDoubleTapListener) {
        this.mDoubleTapListener = onDoubleTapListener;
    }

    public void setContextClickListener(OnContextClickListener onContextClickListener) {
        this.mContextClickListener = onContextClickListener;
    }

    public void setIsLongpressEnabled(boolean isLongpressEnabled) {
        this.mIsLongpressEnabled = isLongpressEnabled;
    }

    public void setCustomLongpressTimeout(int customLongpressTimeout) {
        this.mCustomLongpressTimeout = customLongpressTimeout;
    }

    public boolean isLongpressEnabled() {
        return this.mIsLongpressEnabled;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:130:0x0306, code lost:
        if (r15 != false) goto L_0x0312;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:132:0x030a, code lost:
        if (r1.mInputEventConsistencyVerifier == null) goto L_0x0312;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:133:0x030c, code lost:
        r1.mInputEventConsistencyVerifier.onUnhandledEvent(r2, 0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:134:0x0312, code lost:
        return r15;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:48:0x0123, code lost:
        r28 = r7;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:49:0x0125, code lost:
        r31 = r8;
     */
    /* JADX WARNING: Removed duplicated region for block: B:102:0x023f  */
    /* JADX WARNING: Removed duplicated region for block: B:99:0x0234  */
    public boolean onTouchEvent(MotionEvent ev) {
        int longPressTimeout;
        int deltaX;
        float x1;
        boolean pointerUp;
        int upIndex;
        MotionEvent motionEvent = ev;
        if (this.mInputEventConsistencyVerifier != null) {
            this.mInputEventConsistencyVerifier.onTouchEvent(motionEvent, 0);
        }
        int action = ev.getAction();
        if (this.mVelocityTracker == null) {
            this.mVelocityTracker = VelocityTracker.obtain();
        }
        this.mVelocityTracker.addMovement(motionEvent);
        boolean pointerUp2 = (action & 255) == 6;
        int skipIndex = pointerUp2 ? ev.getActionIndex() : -1;
        boolean isGeneratedGesture = (ev.getFlags() & 8) != 0;
        int count = ev.getPointerCount();
        float sumY = 0.0f;
        float sumX = 0.0f;
        for (int i = 0; i < count; i++) {
            if (skipIndex != i) {
                sumX += motionEvent.getX(i);
                sumY += motionEvent.getY(i);
            }
        }
        int div = pointerUp2 ? count - 1 : count;
        float focusX = sumX / ((float) div);
        float focusY = sumY / ((float) div);
        boolean handled = false;
        switch (action & 255) {
            case 0:
                boolean z = pointerUp2;
                int i2 = skipIndex;
                if (this.mDoubleTapListener != null) {
                    boolean hadTapMessage = this.mHandler.hasMessages(3);
                    if (hadTapMessage) {
                        this.mHandler.removeMessages(3);
                    }
                    if (this.mCurrentDownEvent == null || this.mPreviousUpEvent == null || !hadTapMessage || !isConsideredDoubleTap(this.mCurrentDownEvent, this.mPreviousUpEvent, motionEvent)) {
                        this.mHandler.sendEmptyMessageDelayed(3, (long) DOUBLE_TAP_TIMEOUT);
                    } else {
                        this.mIsDoubleTapping = true;
                        handled = this.mDoubleTapListener.onDoubleTap(this.mCurrentDownEvent) | false | this.mDoubleTapListener.onDoubleTapEvent(motionEvent);
                    }
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
                this.mDeferConfirmSingleTap = false;
                if (this.mIsLongpressEnabled) {
                    if (this.mCustomLongpressTimeout > 0) {
                        longPressTimeout = this.mCustomLongpressTimeout;
                    } else {
                        longPressTimeout = LONGPRESS_TIMEOUT;
                    }
                    this.mHandler.removeMessages(2);
                    boolean z2 = isGeneratedGesture;
                    this.mHandler.sendEmptyMessageAtTime(2, this.mCurrentDownEvent.getDownTime() + ((long) longPressTimeout));
                }
                this.mHandler.sendEmptyMessageAtTime(1, this.mCurrentDownEvent.getDownTime() + ((long) TAP_TIMEOUT));
                handled |= this.mListener.onDown(motionEvent);
                break;
            case 1:
                boolean z3 = pointerUp2;
                this.mStillDown = false;
                MotionEvent currentUpEvent = MotionEvent.obtain(ev);
                if (this.mIsDoubleTapping) {
                    handled = false | this.mDoubleTapListener.onDoubleTapEvent(motionEvent);
                } else if (this.mInLongPress) {
                    this.mHandler.removeMessages(3);
                    this.mInLongPress = false;
                } else if (!this.mAlwaysInTapRegion || this.mIgnoreNextUpEvent) {
                    if (!this.mIgnoreNextUpEvent) {
                        VelocityTracker velocityTracker = this.mVelocityTracker;
                        int pointerId = motionEvent.getPointerId(0);
                        velocityTracker.computeCurrentVelocity(1000, (float) this.mMaximumFlingVelocity);
                        float velocityY = velocityTracker.getYVelocity(pointerId);
                        int i3 = skipIndex;
                        float velocityX = velocityTracker.getXVelocity(pointerId);
                        VelocityTracker velocityTracker2 = velocityTracker;
                        if (Math.abs(velocityY) > ((float) this.mMinimumFlingVelocity) || Math.abs(velocityX) > ((float) this.mMinimumFlingVelocity)) {
                            try {
                                handled = this.mListener.onFling(this.mCurrentDownEvent, motionEvent, velocityX, velocityY);
                            } catch (Exception e) {
                                Exception exc = e;
                                Log.e("GestureDetector", "onFling Exception");
                            }
                        }
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
                    this.mDeferConfirmSingleTap = false;
                    this.mIgnoreNextUpEvent = false;
                    this.mHandler.removeMessages(1);
                    this.mHandler.removeMessages(2);
                    break;
                } else {
                    handled = this.mListener.onSingleTapUp(motionEvent);
                    if (this.mDeferConfirmSingleTap && this.mDoubleTapListener != null) {
                        this.mDoubleTapListener.onSingleTapConfirmed(motionEvent);
                    }
                }
                if (this.mPreviousUpEvent != null) {
                }
                this.mPreviousUpEvent = currentUpEvent;
                if (this.mVelocityTracker != null) {
                }
                this.mIsDoubleTapping = false;
                this.mDeferConfirmSingleTap = false;
                this.mIgnoreNextUpEvent = false;
                this.mHandler.removeMessages(1);
                this.mHandler.removeMessages(2);
                break;
            case 2:
                boolean z4 = pointerUp2;
                if (!this.mInLongPress && !this.mInContextClick) {
                    float scrollX = this.mLastFocusX - focusX;
                    float scrollY = this.mLastFocusY - focusY;
                    if (!this.mIsDoubleTapping) {
                        if (!this.mAlwaysInTapRegion) {
                            if (Math.abs(scrollX) >= 1.0f || Math.abs(scrollY) >= 1.0f) {
                                handled = this.mListener.onScroll(this.mCurrentDownEvent, motionEvent, scrollX, scrollY);
                                this.mLastFocusX = focusX;
                                this.mLastFocusY = focusY;
                                break;
                            }
                        } else {
                            int deltaX2 = (int) (focusX - this.mDownFocusX);
                            int deltaY = (int) (focusY - this.mDownFocusY);
                            int distance = (deltaX2 * deltaX2) + (deltaY * deltaY);
                            if (isGeneratedGesture) {
                                int i4 = deltaX2;
                                deltaX = 0;
                            } else {
                                int i5 = deltaX2;
                                deltaX = this.mTouchSlopSquare;
                            }
                            if (distance > deltaX) {
                                int i6 = deltaX;
                                int i7 = deltaY;
                                boolean handled2 = this.mListener.onScroll(this.mCurrentDownEvent, motionEvent, scrollX, scrollY);
                                this.mLastFocusX = focusX;
                                this.mLastFocusY = focusY;
                                this.mAlwaysInTapRegion = false;
                                this.mHandler.removeMessages(3);
                                this.mHandler.removeMessages(1);
                                this.mHandler.removeMessages(2);
                                handled = handled2;
                            } else {
                                int slopSquare = deltaX;
                                int i8 = deltaY;
                            }
                            if (distance > (isGeneratedGesture ? 0 : this.mDoubleTapTouchSlopSquare)) {
                                this.mAlwaysInBiggerTapRegion = false;
                                break;
                            }
                        }
                    } else {
                        handled = false | this.mDoubleTapListener.onDoubleTapEvent(motionEvent);
                        break;
                    }
                }
                break;
            case 3:
                boolean z5 = pointerUp2;
                cancel();
                break;
            case 5:
                boolean z6 = pointerUp2;
                this.mLastFocusX = focusX;
                this.mDownFocusX = focusX;
                this.mLastFocusY = focusY;
                this.mDownFocusY = focusY;
                cancelTaps();
                break;
            case 6:
                this.mLastFocusX = focusX;
                this.mDownFocusX = focusX;
                this.mLastFocusY = focusY;
                this.mDownFocusY = focusY;
                this.mVelocityTracker.computeCurrentVelocity(1000, (float) this.mMaximumFlingVelocity);
                int upIndex2 = ev.getActionIndex();
                int i9 = motionEvent.getPointerId(upIndex2);
                float x12 = this.mVelocityTracker.getXVelocity(i9);
                int i10 = action;
                float y1 = this.mVelocityTracker.getYVelocity(i9);
                int i11 = 0;
                while (true) {
                    int id1 = i9;
                    int i12 = i11;
                    if (i12 >= count) {
                        boolean z7 = pointerUp2;
                        float f = x12;
                        break;
                    } else {
                        if (i12 == upIndex2) {
                            upIndex = upIndex2;
                            pointerUp = pointerUp2;
                            x1 = x12;
                        } else {
                            upIndex = upIndex2;
                            int id2 = motionEvent.getPointerId(i12);
                            pointerUp = pointerUp2;
                            x1 = x12;
                            if ((this.mVelocityTracker.getXVelocity(id2) * x12) + (this.mVelocityTracker.getYVelocity(id2) * y1) < 0.0f) {
                                int i13 = id2;
                                this.mVelocityTracker.clear();
                                break;
                            }
                        }
                        i11 = i12 + 1;
                        i9 = id1;
                        upIndex2 = upIndex;
                        pointerUp2 = pointerUp;
                        x12 = x1;
                    }
                }
            default:
                int i14 = action;
                boolean z8 = pointerUp2;
                int i15 = skipIndex;
                boolean z9 = isGeneratedGesture;
                break;
        }
    }

    public boolean onGenericMotionEvent(MotionEvent ev) {
        if (this.mInputEventConsistencyVerifier != null) {
            this.mInputEventConsistencyVerifier.onGenericMotionEvent(ev, 0);
        }
        int actionButton = ev.getActionButton();
        switch (ev.getActionMasked()) {
            case 11:
                if (this.mContextClickListener != null && !this.mInContextClick && !this.mInLongPress && ((actionButton == 32 || actionButton == 2) && this.mContextClickListener.onContextClick(ev))) {
                    this.mInContextClick = true;
                    this.mHandler.removeMessages(2);
                    this.mHandler.removeMessages(3);
                    return true;
                }
            case 12:
                if (this.mInContextClick && (actionButton == 32 || actionButton == 2)) {
                    this.mInContextClick = false;
                    this.mIgnoreNextUpEvent = true;
                    break;
                }
        }
        return false;
    }

    private void cancel() {
        this.mHandler.removeMessages(1);
        this.mHandler.removeMessages(2);
        this.mHandler.removeMessages(3);
        if (this.mVelocityTracker != null) {
            this.mVelocityTracker.recycle();
        }
        this.mVelocityTracker = null;
        this.mIsDoubleTapping = false;
        this.mStillDown = false;
        this.mAlwaysInTapRegion = false;
        this.mAlwaysInBiggerTapRegion = false;
        this.mDeferConfirmSingleTap = false;
        this.mInLongPress = false;
        this.mInContextClick = false;
        this.mIgnoreNextUpEvent = false;
    }

    private void cancelTaps() {
        this.mHandler.removeMessages(1);
        this.mHandler.removeMessages(2);
        this.mHandler.removeMessages(3);
        this.mIsDoubleTapping = false;
        this.mAlwaysInTapRegion = false;
        this.mAlwaysInBiggerTapRegion = false;
        this.mDeferConfirmSingleTap = false;
        this.mInLongPress = false;
        this.mInContextClick = false;
        this.mIgnoreNextUpEvent = false;
    }

    private boolean isConsideredDoubleTap(MotionEvent firstDown, MotionEvent firstUp, MotionEvent secondDown) {
        boolean z = false;
        if (!this.mAlwaysInBiggerTapRegion) {
            return false;
        }
        long deltaTime = secondDown.getEventTime() - firstUp.getEventTime();
        if (deltaTime > ((long) DOUBLE_TAP_TIMEOUT) || deltaTime < ((long) DOUBLE_TAP_MIN_TIME)) {
            return false;
        }
        int deltaX = ((int) firstDown.getX()) - ((int) secondDown.getX());
        int deltaY = ((int) firstDown.getY()) - ((int) secondDown.getY());
        if ((deltaX * deltaX) + (deltaY * deltaY) < ((firstDown.getFlags() & 8) != 0 ? 0 : this.mDoubleTapSlopSquare)) {
            z = true;
        }
        return z;
    }

    /* access modifiers changed from: private */
    public void dispatchLongPress() {
        this.mHandler.removeMessages(3);
        this.mDeferConfirmSingleTap = false;
        this.mInLongPress = true;
        this.mListener.onLongPress(this.mCurrentDownEvent);
    }
}
