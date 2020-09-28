package android.view;

import android.annotation.UnsupportedAppUsage;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.util.StatsLog;

public class GestureDetector {
    private static final int DOUBLE_TAP_MIN_TIME = ViewConfiguration.getDoubleTapMinTime();
    private static final int DOUBLE_TAP_TIMEOUT = ViewConfiguration.getDoubleTapTimeout();
    @UnsupportedAppUsage(maxTargetSdk = 28)
    private static final int LONGPRESS_TIMEOUT = ViewConfiguration.getLongPressTimeout();
    private static final int LONG_PRESS = 2;
    private static final int SHOW_PRESS = 1;
    private static final int TAP = 3;
    private static final int TAP_TIMEOUT = ViewConfiguration.getTapTimeout();
    private boolean mAlwaysInBiggerTapRegion;
    @UnsupportedAppUsage
    private boolean mAlwaysInTapRegion;
    private OnContextClickListener mContextClickListener;
    private MotionEvent mCurrentDownEvent;
    private MotionEvent mCurrentMotionEvent;
    private int mCustomLongpressTimeout;
    private boolean mDeferConfirmSingleTap;
    private OnDoubleTapListener mDoubleTapListener;
    private int mDoubleTapSlopSquare;
    private int mDoubleTapTouchSlopSquare;
    private float mDownFocusX;
    private float mDownFocusY;
    private final Handler mHandler;
    private boolean mHasRecordedClassification;
    private boolean mIgnoreNextUpEvent;
    private boolean mInContextClick;
    private boolean mInLongPress;
    private final InputEventConsistencyVerifier mInputEventConsistencyVerifier;
    private boolean mIsDoubleTapping;
    private boolean mIsLongpressEnabled;
    private float mLastFocusX;
    private float mLastFocusY;
    @UnsupportedAppUsage
    private final OnGestureListener mListener;
    private int mMaximumFlingVelocity;
    @UnsupportedAppUsage
    private int mMinimumFlingVelocity;
    private MotionEvent mPreviousUpEvent;
    private boolean mStillDown;
    @UnsupportedAppUsage
    private int mTouchSlopSquare;
    private VelocityTracker mVelocityTracker;

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
        @Override // android.view.GestureDetector.OnGestureListener
        public boolean onSingleTapUp(MotionEvent e) {
            return false;
        }

        @Override // android.view.GestureDetector.OnGestureListener
        public void onLongPress(MotionEvent e) {
        }

        @Override // android.view.GestureDetector.OnGestureListener
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            return false;
        }

        @Override // android.view.GestureDetector.OnGestureListener
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            return false;
        }

        @Override // android.view.GestureDetector.OnGestureListener
        public void onShowPress(MotionEvent e) {
        }

        @Override // android.view.GestureDetector.OnGestureListener
        public boolean onDown(MotionEvent e) {
            return false;
        }

        @Override // android.view.GestureDetector.OnDoubleTapListener
        public boolean onDoubleTap(MotionEvent e) {
            return false;
        }

        @Override // android.view.GestureDetector.OnDoubleTapListener
        public boolean onDoubleTapEvent(MotionEvent e) {
            return false;
        }

        @Override // android.view.GestureDetector.OnDoubleTapListener
        public boolean onSingleTapConfirmed(MotionEvent e) {
            return false;
        }

        @Override // android.view.GestureDetector.OnContextClickListener
        public boolean onContextClick(MotionEvent e) {
            return false;
        }
    }

    private class GestureHandler extends Handler {
        GestureHandler() {
        }

        GestureHandler(Handler handler) {
            super(handler.getLooper());
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            int i = msg.what;
            if (i == 1) {
                GestureDetector.this.mListener.onShowPress(GestureDetector.this.mCurrentDownEvent);
            } else if (i == 2) {
                GestureDetector.this.recordGestureClassification(msg.arg1);
                GestureDetector.this.dispatchLongPress();
            } else if (i != 3) {
                throw new RuntimeException("Unknown message " + msg);
            } else if (GestureDetector.this.mDoubleTapListener == null) {
            } else {
                if (!GestureDetector.this.mStillDown) {
                    GestureDetector.this.recordGestureClassification(1);
                    GestureDetector.this.mDoubleTapListener.onSingleTapConfirmed(GestureDetector.this.mCurrentDownEvent);
                    return;
                }
                GestureDetector.this.mDeferConfirmSingleTap = true;
            }
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

    /* JADX INFO: Multiple debug info for r4v20 int: [D('deltaX' int), D('distance' int)] */
    public boolean onTouchEvent(MotionEvent ev) {
        InputEventConsistencyVerifier inputEventConsistencyVerifier;
        MotionEvent motionEvent;
        OnDoubleTapListener onDoubleTapListener;
        int motionClassification;
        boolean hasPendingLongPress;
        int action;
        float x1;
        int upIndex;
        InputEventConsistencyVerifier inputEventConsistencyVerifier2 = this.mInputEventConsistencyVerifier;
        if (inputEventConsistencyVerifier2 != null) {
            inputEventConsistencyVerifier2.onTouchEvent(ev, 0);
        }
        int action2 = ev.getAction();
        MotionEvent motionEvent2 = this.mCurrentMotionEvent;
        if (motionEvent2 != null) {
            motionEvent2.recycle();
        }
        this.mCurrentMotionEvent = MotionEvent.obtain(ev);
        if (this.mVelocityTracker == null) {
            this.mVelocityTracker = VelocityTracker.obtain();
        }
        this.mVelocityTracker.addMovement(ev);
        boolean pointerUp = (action2 & 255) == 6;
        int skipIndex = pointerUp ? ev.getActionIndex() : -1;
        boolean isGeneratedGesture = (ev.getFlags() & 8) != 0;
        int count = ev.getPointerCount();
        float sumY = 0.0f;
        float sumX = 0.0f;
        for (int i = 0; i < count; i++) {
            if (skipIndex != i) {
                sumX += ev.getX(i);
                sumY += ev.getY(i);
            }
        }
        int div = pointerUp ? count - 1 : count;
        float focusX = sumX / ((float) div);
        float focusY = sumY / ((float) div);
        boolean handled = false;
        int i2 = action2 & 255;
        if (i2 == 0) {
            if (this.mDoubleTapListener != null) {
                boolean hadTapMessage = this.mHandler.hasMessages(3);
                if (hadTapMessage) {
                    this.mHandler.removeMessages(3);
                }
                MotionEvent motionEvent3 = this.mCurrentDownEvent;
                if (motionEvent3 == null || (motionEvent = this.mPreviousUpEvent) == null || !hadTapMessage || !isConsideredDoubleTap(motionEvent3, motionEvent, ev)) {
                    this.mHandler.sendEmptyMessageDelayed(3, (long) DOUBLE_TAP_TIMEOUT);
                } else {
                    this.mIsDoubleTapping = true;
                    recordGestureClassification(2);
                    handled = false | this.mDoubleTapListener.onDoubleTap(this.mCurrentDownEvent) | this.mDoubleTapListener.onDoubleTapEvent(ev);
                }
            }
            this.mLastFocusX = focusX;
            this.mDownFocusX = focusX;
            this.mLastFocusY = focusY;
            this.mDownFocusY = focusY;
            MotionEvent motionEvent4 = this.mCurrentDownEvent;
            if (motionEvent4 != null) {
                motionEvent4.recycle();
            }
            this.mCurrentDownEvent = MotionEvent.obtain(ev);
            this.mAlwaysInTapRegion = true;
            this.mAlwaysInBiggerTapRegion = true;
            this.mStillDown = true;
            this.mInLongPress = false;
            this.mDeferConfirmSingleTap = false;
            this.mHasRecordedClassification = false;
            if (this.mIsLongpressEnabled) {
                int longPressTimeout = this.mCustomLongpressTimeout;
                if (longPressTimeout <= 0) {
                    longPressTimeout = LONGPRESS_TIMEOUT;
                }
                this.mHandler.removeMessages(2);
                Handler handler = this.mHandler;
                handler.sendMessageAtTime(handler.obtainMessage(2, 3, 0), this.mCurrentDownEvent.getDownTime() + ((long) longPressTimeout));
            }
            this.mHandler.sendEmptyMessageAtTime(1, this.mCurrentDownEvent.getDownTime() + ((long) TAP_TIMEOUT));
            handled |= this.mListener.onDown(ev);
        } else if (i2 == 1) {
            this.mStillDown = false;
            MotionEvent currentUpEvent = MotionEvent.obtain(ev);
            if (this.mIsDoubleTapping) {
                recordGestureClassification(2);
                handled = false | this.mDoubleTapListener.onDoubleTapEvent(ev);
            } else if (this.mInLongPress) {
                this.mHandler.removeMessages(3);
                this.mInLongPress = false;
            } else if (this.mAlwaysInTapRegion && !this.mIgnoreNextUpEvent) {
                recordGestureClassification(1);
                handled = this.mListener.onSingleTapUp(ev);
                if (this.mDeferConfirmSingleTap && (onDoubleTapListener = this.mDoubleTapListener) != null) {
                    onDoubleTapListener.onSingleTapConfirmed(ev);
                }
            } else if (!this.mIgnoreNextUpEvent) {
                VelocityTracker velocityTracker = this.mVelocityTracker;
                int pointerId = ev.getPointerId(0);
                velocityTracker.computeCurrentVelocity(1000, (float) this.mMaximumFlingVelocity);
                float velocityY = velocityTracker.getYVelocity(pointerId);
                float velocityX = velocityTracker.getXVelocity(pointerId);
                if (Math.abs(velocityY) > ((float) this.mMinimumFlingVelocity) || Math.abs(velocityX) > ((float) this.mMinimumFlingVelocity)) {
                    try {
                        handled = this.mListener.onFling(this.mCurrentDownEvent, ev, velocityX, velocityY);
                    } catch (Exception e) {
                        Log.e("GestureDetector", "onFling Exception");
                    }
                }
            }
            MotionEvent motionEvent5 = this.mPreviousUpEvent;
            if (motionEvent5 != null) {
                motionEvent5.recycle();
            }
            this.mPreviousUpEvent = currentUpEvent;
            VelocityTracker velocityTracker2 = this.mVelocityTracker;
            if (velocityTracker2 != null) {
                velocityTracker2.recycle();
                this.mVelocityTracker = null;
            }
            this.mIsDoubleTapping = false;
            this.mDeferConfirmSingleTap = false;
            this.mIgnoreNextUpEvent = false;
            this.mHandler.removeMessages(1);
            this.mHandler.removeMessages(2);
        } else if (i2 != 2) {
            if (i2 == 3) {
                cancel();
            } else if (i2 == 5) {
                this.mLastFocusX = focusX;
                this.mDownFocusX = focusX;
                this.mLastFocusY = focusY;
                this.mDownFocusY = focusY;
                cancelTaps();
            } else if (i2 == 6) {
                this.mLastFocusX = focusX;
                this.mDownFocusX = focusX;
                this.mLastFocusY = focusY;
                this.mDownFocusY = focusY;
                this.mVelocityTracker.computeCurrentVelocity(1000, (float) this.mMaximumFlingVelocity);
                int upIndex2 = ev.getActionIndex();
                int id1 = ev.getPointerId(upIndex2);
                float x12 = this.mVelocityTracker.getXVelocity(id1);
                float y1 = this.mVelocityTracker.getYVelocity(id1);
                int i3 = 0;
                while (true) {
                    if (i3 >= count) {
                        break;
                    }
                    if (i3 != upIndex2) {
                        upIndex = upIndex2;
                        int id2 = ev.getPointerId(i3);
                        action = action2;
                        x1 = x12;
                        if ((this.mVelocityTracker.getXVelocity(id2) * x12) + (this.mVelocityTracker.getYVelocity(id2) * y1) < 0.0f) {
                            this.mVelocityTracker.clear();
                            break;
                        }
                    } else {
                        upIndex = upIndex2;
                        action = action2;
                        x1 = x12;
                    }
                    i3++;
                    upIndex2 = upIndex;
                    x12 = x1;
                    action2 = action;
                }
            }
        } else if (!this.mInLongPress) {
            if (!this.mInContextClick) {
                int motionClassification2 = ev.getClassification();
                boolean hasPendingLongPress2 = this.mHandler.hasMessages(2);
                float scrollX = this.mLastFocusX - focusX;
                float scrollY = this.mLastFocusY - focusY;
                if (this.mIsDoubleTapping) {
                    recordGestureClassification(2);
                    motionClassification = motionClassification2;
                    hasPendingLongPress = hasPendingLongPress2;
                    handled = false | this.mDoubleTapListener.onDoubleTapEvent(ev);
                } else if (this.mAlwaysInTapRegion) {
                    int deltaX = (int) (focusX - this.mDownFocusX);
                    int deltaY = (int) (focusY - this.mDownFocusY);
                    int distance = (deltaX * deltaX) + (deltaY * deltaY);
                    int deltaY2 = isGeneratedGesture ? 0 : this.mTouchSlopSquare;
                    if (hasPendingLongPress2 && (motionClassification2 == 1)) {
                        float multiplier = ViewConfiguration.getAmbiguousGestureMultiplier();
                        if (distance > deltaY2) {
                            this.mHandler.removeMessages(2);
                            long longPressTimeout2 = (long) ViewConfiguration.getLongPressTimeout();
                            Handler handler2 = this.mHandler;
                            motionClassification = motionClassification2;
                            hasPendingLongPress = hasPendingLongPress2;
                            handler2.sendMessageAtTime(handler2.obtainMessage(2, 3, 0), ev.getDownTime() + ((long) (((float) longPressTimeout2) * multiplier)));
                        } else {
                            motionClassification = motionClassification2;
                            hasPendingLongPress = hasPendingLongPress2;
                        }
                        deltaY2 = (int) (((float) deltaY2) * multiplier * multiplier);
                    } else {
                        motionClassification = motionClassification2;
                        hasPendingLongPress = hasPendingLongPress2;
                    }
                    if (distance > deltaY2) {
                        recordGestureClassification(5);
                        handled = this.mListener.onScroll(this.mCurrentDownEvent, ev, scrollX, scrollY);
                        this.mLastFocusX = focusX;
                        this.mLastFocusY = focusY;
                        this.mAlwaysInTapRegion = false;
                        this.mHandler.removeMessages(3);
                        this.mHandler.removeMessages(1);
                        this.mHandler.removeMessages(2);
                    }
                    if (distance > (isGeneratedGesture ? 0 : this.mDoubleTapTouchSlopSquare)) {
                        this.mAlwaysInBiggerTapRegion = false;
                    }
                } else {
                    motionClassification = motionClassification2;
                    hasPendingLongPress = hasPendingLongPress2;
                    if (Math.abs(scrollX) >= 1.0f || Math.abs(scrollY) >= 1.0f) {
                        recordGestureClassification(5);
                        boolean handled2 = this.mListener.onScroll(this.mCurrentDownEvent, ev, scrollX, scrollY);
                        this.mLastFocusX = focusX;
                        this.mLastFocusY = focusY;
                        handled = handled2;
                    }
                }
                if ((motionClassification == 2) && hasPendingLongPress) {
                    this.mHandler.removeMessages(2);
                    Handler handler3 = this.mHandler;
                    handler3.sendMessage(handler3.obtainMessage(2, 4, 0));
                }
            }
        }
        if (!handled && (inputEventConsistencyVerifier = this.mInputEventConsistencyVerifier) != null) {
            inputEventConsistencyVerifier.onUnhandledEvent(ev, 0);
        }
        return handled;
    }

    public boolean onGenericMotionEvent(MotionEvent ev) {
        InputEventConsistencyVerifier inputEventConsistencyVerifier = this.mInputEventConsistencyVerifier;
        if (inputEventConsistencyVerifier != null) {
            inputEventConsistencyVerifier.onGenericMotionEvent(ev, 0);
        }
        int actionButton = ev.getActionButton();
        int actionMasked = ev.getActionMasked();
        if (actionMasked != 11) {
            if (actionMasked == 12 && this.mInContextClick && (actionButton == 32 || actionButton == 2)) {
                this.mInContextClick = false;
                this.mIgnoreNextUpEvent = true;
            }
        } else if (this.mContextClickListener != null && !this.mInContextClick && !this.mInLongPress && ((actionButton == 32 || actionButton == 2) && this.mContextClickListener.onContextClick(ev))) {
            this.mInContextClick = true;
            this.mHandler.removeMessages(2);
            this.mHandler.removeMessages(3);
            return true;
        }
        return false;
    }

    private void cancel() {
        this.mHandler.removeMessages(1);
        this.mHandler.removeMessages(2);
        this.mHandler.removeMessages(3);
        VelocityTracker velocityTracker = this.mVelocityTracker;
        if (velocityTracker != null) {
            velocityTracker.recycle();
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
            return true;
        }
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void dispatchLongPress() {
        this.mHandler.removeMessages(3);
        this.mDeferConfirmSingleTap = false;
        this.mInLongPress = true;
        this.mListener.onLongPress(this.mCurrentDownEvent);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void recordGestureClassification(int classification) {
        if (!this.mHasRecordedClassification && classification != 0) {
            if (this.mCurrentDownEvent == null || this.mCurrentMotionEvent == null) {
                this.mHasRecordedClassification = true;
                return;
            }
            StatsLog.write(177, getClass().getName(), classification, (int) (SystemClock.uptimeMillis() - this.mCurrentMotionEvent.getDownTime()), (float) Math.hypot((double) (this.mCurrentMotionEvent.getRawX() - this.mCurrentDownEvent.getRawX()), (double) (this.mCurrentMotionEvent.getRawY() - this.mCurrentDownEvent.getRawY())));
            this.mHasRecordedClassification = true;
        }
    }
}
