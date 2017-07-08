package android.view;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.WindowManager.LayoutParams;
import com.android.internal.logging.MetricsProto.MetricsEvent;
import com.android.internal.telephony.RILConstants;
import com.huawei.pgmng.log.LogPower;
import com.huawei.pgmng.plug.PGSdk;
import huawei.cust.HwCfgFilePolicy;

public class GestureDetector {
    private static final int DOUBLE_TAP_MIN_TIME = 0;
    private static final int DOUBLE_TAP_TIMEOUT = 0;
    private static final int LONGPRESS_TIMEOUT = 0;
    private static final int LONG_PRESS = 2;
    private static final int SHOW_PRESS = 1;
    private static final int TAP = 3;
    private static final int TAP_TIMEOUT = 0;
    private boolean mAlwaysInBiggerTapRegion;
    private boolean mAlwaysInTapRegion;
    private OnContextClickListener mContextClickListener;
    private MotionEvent mCurrentDownEvent;
    private boolean mDeferConfirmSingleTap;
    private OnDoubleTapListener mDoubleTapListener;
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
                case GestureDetector.SHOW_PRESS /*1*/:
                    GestureDetector.this.mListener.onShowPress(GestureDetector.this.mCurrentDownEvent);
                case GestureDetector.LONG_PRESS /*2*/:
                    GestureDetector.this.dispatchLongPress();
                case GestureDetector.TAP /*3*/:
                    if (GestureDetector.this.mDoubleTapListener == null) {
                        return;
                    }
                    if (GestureDetector.this.mStillDown) {
                        GestureDetector.this.mDeferConfirmSingleTap = true;
                    } else {
                        GestureDetector.this.mDoubleTapListener.onSingleTapConfirmed(GestureDetector.this.mCurrentDownEvent);
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

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.view.GestureDetector.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.view.GestureDetector.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.view.GestureDetector.<clinit>():void");
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
        InputEventConsistencyVerifier inputEventConsistencyVerifier = null;
        if (InputEventConsistencyVerifier.isInstrumentationEnabled()) {
            inputEventConsistencyVerifier = new InputEventConsistencyVerifier(this, LONGPRESS_TIMEOUT);
        }
        this.mInputEventConsistencyVerifier = inputEventConsistencyVerifier;
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

    public void setContextClickListener(OnContextClickListener onContextClickListener) {
        this.mContextClickListener = onContextClickListener;
    }

    public void setIsLongpressEnabled(boolean isLongpressEnabled) {
        this.mIsLongpressEnabled = isLongpressEnabled;
    }

    public boolean isLongpressEnabled() {
        return this.mIsLongpressEnabled;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean onTouchEvent(MotionEvent ev) {
        int i;
        int div;
        if (this.mInputEventConsistencyVerifier != null) {
            this.mInputEventConsistencyVerifier.onTouchEvent(ev, LONGPRESS_TIMEOUT);
        }
        int action = ev.getAction();
        if (this.mVelocityTracker == null) {
            this.mVelocityTracker = VelocityTracker.obtain();
        }
        this.mVelocityTracker.addMovement(ev);
        int i2 = action & MetricsEvent.ACTION_DOUBLE_TAP_POWER_CAMERA_GESTURE;
        boolean pointerUp = r0 == 6;
        int skipIndex = pointerUp ? ev.getActionIndex() : -1;
        float sumX = 0.0f;
        float sumY = 0.0f;
        int count = ev.getPointerCount();
        for (i = LONGPRESS_TIMEOUT; i < count; i += SHOW_PRESS) {
            if (skipIndex != i) {
                sumX += ev.getX(i);
                sumY += ev.getY(i);
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
        switch (action & MetricsEvent.ACTION_DOUBLE_TAP_POWER_CAMERA_GESTURE) {
            case LONGPRESS_TIMEOUT /*0*/:
                if (this.mDoubleTapListener != null) {
                    boolean hadTapMessage = this.mHandler.hasMessages(TAP);
                    if (hadTapMessage) {
                        this.mHandler.removeMessages(TAP);
                    }
                    if (!(this.mCurrentDownEvent == null || this.mPreviousUpEvent == null || !hadTapMessage)) {
                        if (isConsideredDoubleTap(this.mCurrentDownEvent, this.mPreviousUpEvent, ev)) {
                            this.mIsDoubleTapping = true;
                            handled = this.mDoubleTapListener.onDoubleTap(this.mCurrentDownEvent) | this.mDoubleTapListener.onDoubleTapEvent(ev);
                        }
                    }
                    this.mHandler.sendEmptyMessageDelayed(TAP, (long) DOUBLE_TAP_TIMEOUT);
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
                    this.mHandler.removeMessages(LONG_PRESS);
                    this.mHandler.sendEmptyMessageAtTime(LONG_PRESS, (this.mCurrentDownEvent.getDownTime() + ((long) TAP_TIMEOUT)) + ((long) LONGPRESS_TIMEOUT));
                }
                this.mHandler.sendEmptyMessageAtTime(SHOW_PRESS, this.mCurrentDownEvent.getDownTime() + ((long) TAP_TIMEOUT));
                handled |= this.mListener.onDown(ev);
                break;
            case SHOW_PRESS /*1*/:
                this.mStillDown = false;
                MotionEvent currentUpEvent = MotionEvent.obtain(ev);
                if (this.mIsDoubleTapping) {
                    handled = this.mDoubleTapListener.onDoubleTapEvent(ev);
                } else if (this.mInLongPress) {
                    this.mHandler.removeMessages(TAP);
                    this.mInLongPress = false;
                } else if (this.mAlwaysInTapRegion && !this.mIgnoreNextUpEvent) {
                    handled = this.mListener.onSingleTapUp(ev);
                    if (this.mDeferConfirmSingleTap && this.mDoubleTapListener != null) {
                        this.mDoubleTapListener.onSingleTapConfirmed(ev);
                    }
                } else if (!this.mIgnoreNextUpEvent) {
                    VelocityTracker velocityTracker = this.mVelocityTracker;
                    int pointerId = ev.getPointerId(LONGPRESS_TIMEOUT);
                    velocityTracker.computeCurrentVelocity(RILConstants.RIL_UNSOL_RESPONSE_RADIO_STATE_CHANGED, (float) this.mMaximumFlingVelocity);
                    float velocityY = velocityTracker.getYVelocity(pointerId);
                    float velocityX = velocityTracker.getXVelocity(pointerId);
                    if (Math.abs(velocityY) <= ((float) this.mMinimumFlingVelocity)) {
                        break;
                    }
                    LogPower.push(LogPower.FLING_START);
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
                this.mDeferConfirmSingleTap = false;
                this.mIgnoreNextUpEvent = false;
                this.mHandler.removeMessages(SHOW_PRESS);
                this.mHandler.removeMessages(LONG_PRESS);
                break;
            case LONG_PRESS /*2*/:
                if (!(this.mInLongPress || this.mInContextClick)) {
                    float scrollX = this.mLastFocusX - focusX;
                    float scrollY = this.mLastFocusY - focusY;
                    if (!this.mIsDoubleTapping) {
                        if (!this.mAlwaysInTapRegion) {
                            if (Math.abs(scrollX) >= LayoutParams.BRIGHTNESS_OVERRIDE_FULL || Math.abs(scrollY) >= LayoutParams.BRIGHTNESS_OVERRIDE_FULL) {
                                handled = this.mListener.onScroll(this.mCurrentDownEvent, ev, scrollX, scrollY);
                                this.mLastFocusX = focusX;
                                this.mLastFocusY = focusY;
                                break;
                            }
                        }
                        int deltaX = (int) (focusX - this.mDownFocusX);
                        int deltaY = (int) (focusY - this.mDownFocusY);
                        int distance = (deltaX * deltaX) + (deltaY * deltaY);
                        i2 = this.mTouchSlopSquare;
                        if (distance > r0) {
                            handled = this.mListener.onScroll(this.mCurrentDownEvent, ev, scrollX, scrollY);
                            this.mLastFocusX = focusX;
                            this.mLastFocusY = focusY;
                            this.mAlwaysInTapRegion = false;
                            this.mHandler.removeMessages(TAP);
                            this.mHandler.removeMessages(SHOW_PRESS);
                            this.mHandler.removeMessages(LONG_PRESS);
                        }
                        i2 = this.mDoubleTapTouchSlopSquare;
                        if (distance > r0) {
                            this.mAlwaysInBiggerTapRegion = false;
                            break;
                        }
                    }
                    handled = this.mDoubleTapListener.onDoubleTapEvent(ev);
                    break;
                }
                break;
            case TAP /*3*/:
                cancel();
                break;
            case HwCfgFilePolicy.CLOUD_MCC /*5*/:
                this.mLastFocusX = focusX;
                this.mDownFocusX = focusX;
                this.mLastFocusY = focusY;
                this.mDownFocusY = focusY;
                cancelTaps();
                break;
            case HwCfgFilePolicy.CLOUD_DPLMN /*6*/:
                this.mLastFocusX = focusX;
                this.mDownFocusX = focusX;
                this.mLastFocusY = focusY;
                this.mDownFocusY = focusY;
                this.mVelocityTracker.computeCurrentVelocity(RILConstants.RIL_UNSOL_RESPONSE_RADIO_STATE_CHANGED, (float) this.mMaximumFlingVelocity);
                int upIndex = ev.getActionIndex();
                int id1 = ev.getPointerId(upIndex);
                float x1 = this.mVelocityTracker.getXVelocity(id1);
                float y1 = this.mVelocityTracker.getYVelocity(id1);
                for (i = LONGPRESS_TIMEOUT; i < count; i += SHOW_PRESS) {
                    if (i != upIndex) {
                        int id2 = ev.getPointerId(i);
                        if ((x1 * this.mVelocityTracker.getXVelocity(id2)) + (y1 * this.mVelocityTracker.getYVelocity(id2)) < 0.0f) {
                            this.mVelocityTracker.clear();
                            break;
                        }
                    }
                }
                break;
        }
        if (!(handled || this.mInputEventConsistencyVerifier == null)) {
            this.mInputEventConsistencyVerifier.onUnhandledEvent(ev, LONGPRESS_TIMEOUT);
        }
        return handled;
    }

    public boolean onGenericMotionEvent(MotionEvent ev) {
        if (this.mInputEventConsistencyVerifier != null) {
            this.mInputEventConsistencyVerifier.onGenericMotionEvent(ev, LONGPRESS_TIMEOUT);
        }
        int actionButton = ev.getActionButton();
        switch (ev.getActionMasked()) {
            case PGSdk.TYPE_IM /*11*/:
                if (!(this.mContextClickListener == null || this.mInContextClick || this.mInLongPress || ((actionButton != 32 && actionButton != LONG_PRESS) || !this.mContextClickListener.onContextClick(ev)))) {
                    this.mInContextClick = true;
                    this.mHandler.removeMessages(LONG_PRESS);
                    this.mHandler.removeMessages(TAP);
                    return true;
                }
            case PGSdk.TYPE_MUSIC /*12*/:
                if (this.mInContextClick && (actionButton == 32 || actionButton == LONG_PRESS)) {
                    this.mInContextClick = false;
                    this.mIgnoreNextUpEvent = true;
                    break;
                }
        }
        return false;
    }

    private void cancel() {
        this.mHandler.removeMessages(SHOW_PRESS);
        this.mHandler.removeMessages(LONG_PRESS);
        this.mHandler.removeMessages(TAP);
        this.mVelocityTracker.recycle();
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
        this.mHandler.removeMessages(SHOW_PRESS);
        this.mHandler.removeMessages(LONG_PRESS);
        this.mHandler.removeMessages(TAP);
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
        if ((deltaX * deltaX) + (deltaY * deltaY) < this.mDoubleTapSlopSquare) {
            z = true;
        }
        return z;
    }

    private void dispatchLongPress() {
        this.mHandler.removeMessages(TAP);
        this.mDeferConfirmSingleTap = false;
        this.mInLongPress = true;
        this.mListener.onLongPress(this.mCurrentDownEvent);
    }
}
