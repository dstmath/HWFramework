package android.view;

import android.content.Context;
import android.os.Handler;
import android.view.GestureDetector.SimpleOnGestureListener;
import com.android.internal.R;

public class ScaleGestureDetector {
    private static final int ANCHORED_SCALE_MODE_DOUBLE_TAP = 1;
    private static final int ANCHORED_SCALE_MODE_NONE = 0;
    private static final int ANCHORED_SCALE_MODE_STYLUS = 2;
    private static final float SCALE_FACTOR = 0.5f;
    private static final String TAG = "ScaleGestureDetector";
    private static final long TOUCH_STABILIZE_TIME = 128;
    private int mAnchoredScaleMode;
    private float mAnchoredScaleStartX;
    private float mAnchoredScaleStartY;
    private final Context mContext;
    private float mCurrSpan;
    private float mCurrSpanX;
    private float mCurrSpanY;
    private long mCurrTime;
    private boolean mEventBeforeOrAboveStartingGestureEvent;
    private float mFocusX;
    private float mFocusY;
    private GestureDetector mGestureDetector;
    private final Handler mHandler;
    private boolean mInProgress;
    private float mInitialSpan;
    private final InputEventConsistencyVerifier mInputEventConsistencyVerifier;
    private final OnScaleGestureListener mListener;
    private int mMinSpan;
    private float mPrevSpan;
    private float mPrevSpanX;
    private float mPrevSpanY;
    private long mPrevTime;
    private boolean mQuickScaleEnabled;
    private int mSpanSlop;
    private boolean mStylusScaleEnabled;

    public interface OnScaleGestureListener {
        boolean onScale(ScaleGestureDetector scaleGestureDetector);

        boolean onScaleBegin(ScaleGestureDetector scaleGestureDetector);

        void onScaleEnd(ScaleGestureDetector scaleGestureDetector);
    }

    public static class SimpleOnScaleGestureListener implements OnScaleGestureListener {
        public boolean onScale(ScaleGestureDetector detector) {
            return false;
        }

        public boolean onScaleBegin(ScaleGestureDetector detector) {
            return true;
        }

        public void onScaleEnd(ScaleGestureDetector detector) {
        }
    }

    public ScaleGestureDetector(Context context, OnScaleGestureListener listener) {
        this(context, listener, null);
    }

    public ScaleGestureDetector(Context context, OnScaleGestureListener listener, Handler handler) {
        this.mAnchoredScaleMode = 0;
        this.mInputEventConsistencyVerifier = InputEventConsistencyVerifier.isInstrumentationEnabled() ? new InputEventConsistencyVerifier(this, 0) : null;
        this.mContext = context;
        this.mListener = listener;
        this.mSpanSlop = ViewConfiguration.get(context).getScaledTouchSlop() * 2;
        this.mMinSpan = context.getResources().getDimensionPixelSize(R.dimen.config_minScalingSpan);
        this.mHandler = handler;
        int targetSdkVersion = context.getApplicationInfo().targetSdkVersion;
        if (targetSdkVersion > 18) {
            setQuickScaleEnabled(true);
        }
        if (targetSdkVersion > 22) {
            setStylusScaleEnabled(true);
        }
    }

    public boolean onTouchEvent(MotionEvent event) {
        int streamComplete;
        boolean configChanged;
        float focusX;
        float focusY;
        int i;
        float span;
        if (this.mInputEventConsistencyVerifier != null) {
            this.mInputEventConsistencyVerifier.onTouchEvent(event, 0);
        }
        this.mCurrTime = event.getEventTime();
        int action = event.getActionMasked();
        if (this.mQuickScaleEnabled) {
            this.mGestureDetector.onTouchEvent(event);
        }
        int count = event.getPointerCount();
        boolean isStylusButtonDown = (event.getButtonState() & 32) != 0;
        boolean anchoredScaleCancelled = this.mAnchoredScaleMode == 2 ? isStylusButtonDown ^ 1 : false;
        if (action == 1 || action == 3) {
            streamComplete = 1;
        } else {
            boolean streamComplete2 = anchoredScaleCancelled;
        }
        if (action == 0 || streamComplete2 != 0) {
            if (this.mInProgress) {
                this.mListener.onScaleEnd(this);
                this.mInProgress = false;
                this.mInitialSpan = 0.0f;
                this.mAnchoredScaleMode = 0;
            } else if (inAnchoredScaleMode() && streamComplete2 != 0) {
                this.mInProgress = false;
                this.mInitialSpan = 0.0f;
                this.mAnchoredScaleMode = 0;
            }
            if (streamComplete2 != 0) {
                return true;
            }
        }
        if (!(this.mInProgress || !this.mStylusScaleEnabled || (inAnchoredScaleMode() ^ 1) == 0 || (streamComplete2 ^ 1) == 0 || !isStylusButtonDown)) {
            this.mAnchoredScaleStartX = event.getX();
            this.mAnchoredScaleStartY = event.getY();
            this.mAnchoredScaleMode = 2;
            this.mInitialSpan = 0.0f;
        }
        if (action == 0 || action == 6 || action == 5) {
            configChanged = true;
        } else {
            configChanged = anchoredScaleCancelled;
        }
        boolean pointerUp = action == 6;
        int skipIndex = pointerUp ? event.getActionIndex() : -1;
        float sumX = 0.0f;
        float sumY = 0.0f;
        int div = pointerUp ? count - 1 : count;
        if (inAnchoredScaleMode()) {
            focusX = this.mAnchoredScaleStartX;
            focusY = this.mAnchoredScaleStartY;
            if (event.getY() < focusY) {
                this.mEventBeforeOrAboveStartingGestureEvent = true;
            } else {
                this.mEventBeforeOrAboveStartingGestureEvent = false;
            }
        } else {
            for (i = 0; i < count; i++) {
                if (skipIndex != i) {
                    sumX += event.getX(i);
                    sumY += event.getY(i);
                }
            }
            focusX = sumX / ((float) div);
            focusY = sumY / ((float) div);
        }
        float devSumX = 0.0f;
        float devSumY = 0.0f;
        for (i = 0; i < count; i++) {
            if (skipIndex != i) {
                devSumX += Math.abs(event.getX(i) - focusX);
                devSumY += Math.abs(event.getY(i) - focusY);
            }
        }
        float spanX = (devSumX / ((float) div)) * 2.0f;
        float spanY = (devSumY / ((float) div)) * 2.0f;
        if (inAnchoredScaleMode()) {
            span = spanY;
        } else {
            span = (float) Math.hypot((double) spanX, (double) spanY);
        }
        boolean wasInProgress = this.mInProgress;
        this.mFocusX = focusX;
        this.mFocusY = focusY;
        if (!inAnchoredScaleMode() && this.mInProgress && (span < ((float) this.mMinSpan) || configChanged)) {
            this.mListener.onScaleEnd(this);
            this.mInProgress = false;
            this.mInitialSpan = span;
        }
        if (configChanged) {
            this.mCurrSpanX = spanX;
            this.mPrevSpanX = spanX;
            this.mCurrSpanY = spanY;
            this.mPrevSpanY = spanY;
            this.mCurrSpan = span;
            this.mPrevSpan = span;
            this.mInitialSpan = span;
        }
        int minSpan = inAnchoredScaleMode() ? this.mSpanSlop : this.mMinSpan;
        if (!this.mInProgress && span >= ((float) minSpan) && (wasInProgress || Math.abs(span - this.mInitialSpan) > ((float) this.mSpanSlop))) {
            this.mCurrSpanX = spanX;
            this.mPrevSpanX = spanX;
            this.mCurrSpanY = spanY;
            this.mPrevSpanY = spanY;
            this.mCurrSpan = span;
            this.mPrevSpan = span;
            this.mPrevTime = this.mCurrTime;
            this.mInProgress = this.mListener.onScaleBegin(this);
        }
        if (action == 2) {
            this.mCurrSpanX = spanX;
            this.mCurrSpanY = spanY;
            this.mCurrSpan = span;
            boolean updatePrev = true;
            if (this.mInProgress) {
                updatePrev = this.mListener.onScale(this);
            }
            if (updatePrev) {
                this.mPrevSpanX = this.mCurrSpanX;
                this.mPrevSpanY = this.mCurrSpanY;
                this.mPrevSpan = this.mCurrSpan;
                this.mPrevTime = this.mCurrTime;
            }
        }
        return true;
    }

    private boolean inAnchoredScaleMode() {
        return this.mAnchoredScaleMode != 0;
    }

    public void setQuickScaleEnabled(boolean scales) {
        this.mQuickScaleEnabled = scales;
        if (this.mQuickScaleEnabled && this.mGestureDetector == null) {
            this.mGestureDetector = new GestureDetector(this.mContext, new SimpleOnGestureListener() {
                public boolean onDoubleTap(MotionEvent e) {
                    ScaleGestureDetector.this.mAnchoredScaleStartX = e.getX();
                    ScaleGestureDetector.this.mAnchoredScaleStartY = e.getY();
                    ScaleGestureDetector.this.mAnchoredScaleMode = 1;
                    return true;
                }
            }, this.mHandler);
        }
    }

    public boolean isQuickScaleEnabled() {
        return this.mQuickScaleEnabled;
    }

    public void setStylusScaleEnabled(boolean scales) {
        this.mStylusScaleEnabled = scales;
    }

    public boolean isStylusScaleEnabled() {
        return this.mStylusScaleEnabled;
    }

    public boolean isInProgress() {
        return this.mInProgress;
    }

    public float getFocusX() {
        return this.mFocusX;
    }

    public float getFocusY() {
        return this.mFocusY;
    }

    public float getCurrentSpan() {
        return this.mCurrSpan;
    }

    public float getCurrentSpanX() {
        return this.mCurrSpanX;
    }

    public float getCurrentSpanY() {
        return this.mCurrSpanY;
    }

    public float getPreviousSpan() {
        return this.mPrevSpan;
    }

    public float getPreviousSpanX() {
        return this.mPrevSpanX;
    }

    public float getPreviousSpanY() {
        return this.mPrevSpanY;
    }

    public float getScaleFactor() {
        float f = 1.0f;
        if (inAnchoredScaleMode()) {
            boolean scaleUp = (!this.mEventBeforeOrAboveStartingGestureEvent || this.mCurrSpan >= this.mPrevSpan) ? !this.mEventBeforeOrAboveStartingGestureEvent && this.mCurrSpan > this.mPrevSpan : true;
            float spanDiff = Math.abs(1.0f - (this.mCurrSpan / this.mPrevSpan)) * SCALE_FACTOR;
            if (this.mPrevSpan > 0.0f) {
                f = scaleUp ? 1.0f + spanDiff : 1.0f - spanDiff;
            }
            return f;
        }
        if (this.mPrevSpan > 0.0f) {
            f = this.mCurrSpan / this.mPrevSpan;
        }
        return f;
    }

    public long getTimeDelta() {
        return this.mCurrTime - this.mPrevTime;
    }

    public long getEventTime() {
        return this.mCurrTime;
    }
}
