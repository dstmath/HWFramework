package android.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.shapes.RectShape;
import android.graphics.drawable.shapes.Shape;
import android.util.AttributeSet;
import android.view.View;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityNodeInfo.AccessibilityAction;
import com.android.internal.R;

public class RatingBar extends AbsSeekBar {
    private int mNumStars;
    private OnRatingBarChangeListener mOnRatingBarChangeListener;
    private int mProgressOnStartTracking;

    public interface OnRatingBarChangeListener {
        void onRatingChanged(RatingBar ratingBar, float f, boolean z);
    }

    public RatingBar(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public RatingBar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mNumStars = 5;
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RatingBar, defStyleAttr, defStyleRes);
        int numStars = a.getInt(0, this.mNumStars);
        setIsIndicator(a.getBoolean(3, this.mIsUserSeekable ^ 1));
        float rating = a.getFloat(1, -1.0f);
        float stepSize = a.getFloat(2, -1.0f);
        a.recycle();
        if (numStars > 0 && numStars != this.mNumStars) {
            setNumStars(numStars);
        }
        if (stepSize >= 0.0f) {
            setStepSize(stepSize);
        } else {
            setStepSize(0.5f);
        }
        if (rating >= 0.0f) {
            setRating(rating);
        }
        this.mTouchProgressOffset = 0.6f;
    }

    public RatingBar(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.ratingBarStyle);
    }

    public RatingBar(Context context) {
        this(context, null);
    }

    public void setOnRatingBarChangeListener(OnRatingBarChangeListener listener) {
        this.mOnRatingBarChangeListener = listener;
    }

    public OnRatingBarChangeListener getOnRatingBarChangeListener() {
        return this.mOnRatingBarChangeListener;
    }

    public void setIsIndicator(boolean isIndicator) {
        this.mIsUserSeekable = isIndicator ^ 1;
        if (isIndicator) {
            setFocusable(16);
        } else {
            setFocusable(1);
        }
    }

    public boolean isIndicator() {
        return this.mIsUserSeekable ^ 1;
    }

    public void setNumStars(int numStars) {
        if (numStars > 0) {
            this.mNumStars = numStars;
            requestLayout();
        }
    }

    public int getNumStars() {
        return this.mNumStars;
    }

    public void setRating(float rating) {
        setProgress(Math.round(getProgressPerStar() * rating));
    }

    public float getRating() {
        return ((float) getProgress()) / getProgressPerStar();
    }

    public void setStepSize(float stepSize) {
        if (stepSize > 0.0f) {
            float newMax = ((float) this.mNumStars) / stepSize;
            int newProgress = (int) ((newMax / ((float) getMax())) * ((float) getProgress()));
            setMax((int) newMax);
            setProgress(newProgress);
        }
    }

    public float getStepSize() {
        return ((float) getNumStars()) / ((float) getMax());
    }

    private float getProgressPerStar() {
        if (this.mNumStars > 0) {
            return (((float) getMax()) * 1.0f) / ((float) this.mNumStars);
        }
        return 1.0f;
    }

    Shape getDrawableShape() {
        return new RectShape();
    }

    void onProgressRefresh(float scale, boolean fromUser, int progress) {
        super.onProgressRefresh(scale, fromUser, progress);
        updateSecondaryProgress(progress);
        if (!fromUser) {
            dispatchRatingChange(false);
        }
    }

    private void updateSecondaryProgress(int progress) {
        float ratio = getProgressPerStar();
        if (ratio > 0.0f) {
            setSecondaryProgress((int) (Math.ceil((double) (((float) progress) / ratio)) * ((double) ratio)));
        }
    }

    protected synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (this.mSampleWidth > 0) {
            -wrap6(View.resolveSizeAndState(this.mSampleWidth * this.mNumStars, widthMeasureSpec, 0), getMeasuredHeight());
        }
    }

    void onStartTrackingTouch() {
        this.mProgressOnStartTracking = getProgress();
        super.onStartTrackingTouch();
    }

    void onStopTrackingTouch() {
        super.onStopTrackingTouch();
        if (getProgress() != this.mProgressOnStartTracking) {
            dispatchRatingChange(true);
        }
    }

    void onKeyChange() {
        super.onKeyChange();
        dispatchRatingChange(true);
    }

    void dispatchRatingChange(boolean fromUser) {
        if (this.mOnRatingBarChangeListener != null) {
            this.mOnRatingBarChangeListener.onRatingChanged(this, getRating(), fromUser);
        }
    }

    public synchronized void setMax(int max) {
        if (max > 0) {
            super.setMax(max);
        }
    }

    public CharSequence getAccessibilityClassName() {
        return RatingBar.class.getName();
    }

    public void onInitializeAccessibilityNodeInfoInternal(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfoInternal(info);
        if (canUserSetProgress()) {
            info.addAction(AccessibilityAction.ACTION_SET_PROGRESS);
        }
    }

    boolean canUserSetProgress() {
        return super.canUserSetProgress() ? isIndicator() ^ 1 : false;
    }
}
