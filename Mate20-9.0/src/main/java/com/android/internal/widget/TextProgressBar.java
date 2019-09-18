package com.android.internal.widget;

import android.content.Context;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.RemotableViewMethod;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.RemoteViews;

@RemoteViews.RemoteView
public class TextProgressBar extends RelativeLayout implements Chronometer.OnChronometerTickListener {
    static final int CHRONOMETER_ID = 16908308;
    static final int PROGRESSBAR_ID = 16908301;
    public static final String TAG = "TextProgressBar";
    Chronometer mChronometer = null;
    boolean mChronometerFollow = false;
    int mChronometerGravity = 0;
    int mDuration = -1;
    long mDurationBase = -1;
    ProgressBar mProgressBar = null;

    public TextProgressBar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public TextProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public TextProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TextProgressBar(Context context) {
        super(context);
    }

    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        super.addView(child, index, params);
        int childId = child.getId();
        if (childId == CHRONOMETER_ID && (child instanceof Chronometer)) {
            this.mChronometer = (Chronometer) child;
            this.mChronometer.setOnChronometerTickListener(this);
            this.mChronometerFollow = params.width == -2;
            this.mChronometerGravity = this.mChronometer.getGravity() & 8388615;
        } else if (childId == PROGRESSBAR_ID && (child instanceof ProgressBar)) {
            this.mProgressBar = (ProgressBar) child;
        }
    }

    @RemotableViewMethod
    public void setDurationBase(long durationBase) {
        this.mDurationBase = durationBase;
        if (this.mProgressBar == null || this.mChronometer == null) {
            throw new RuntimeException("Expecting child ProgressBar with id 'android.R.id.progress' and Chronometer id 'android.R.id.text1'");
        }
        this.mDuration = (int) (durationBase - this.mChronometer.getBase());
        if (this.mDuration <= 0) {
            this.mDuration = 1;
        }
        this.mProgressBar.setMax(this.mDuration);
    }

    public void onChronometerTick(Chronometer chronometer) {
        if (this.mProgressBar != null) {
            long now = SystemClock.elapsedRealtime();
            if (now >= this.mDurationBase) {
                this.mChronometer.stop();
            }
            this.mProgressBar.setProgress(this.mDuration - ((int) (this.mDurationBase - now)));
            if (this.mChronometerFollow) {
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) this.mProgressBar.getLayoutParams();
                int contentWidth = this.mProgressBar.getWidth() - (params.leftMargin + params.rightMargin);
                int leadingEdge = ((this.mProgressBar.getProgress() * contentWidth) / this.mProgressBar.getMax()) + params.leftMargin;
                int adjustLeft = 0;
                int textWidth = this.mChronometer.getWidth();
                if (this.mChronometerGravity == 8388613) {
                    adjustLeft = -textWidth;
                } else if (this.mChronometerGravity == 1) {
                    adjustLeft = -(textWidth / 2);
                }
                int leadingEdge2 = leadingEdge + adjustLeft;
                int rightLimit = (contentWidth - params.rightMargin) - textWidth;
                if (leadingEdge2 < params.leftMargin) {
                    leadingEdge2 = params.leftMargin;
                } else if (leadingEdge2 > rightLimit) {
                    leadingEdge2 = rightLimit;
                }
                ((RelativeLayout.LayoutParams) this.mChronometer.getLayoutParams()).leftMargin = leadingEdge2;
                this.mChronometer.requestLayout();
                return;
            }
            return;
        }
        throw new RuntimeException("Expecting child ProgressBar with id 'android.R.id.progress'");
    }
}
