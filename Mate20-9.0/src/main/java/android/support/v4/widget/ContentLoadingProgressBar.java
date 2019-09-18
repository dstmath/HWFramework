package android.support.v4.widget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.ProgressBar;

public class ContentLoadingProgressBar extends ProgressBar {
    private static final int MIN_DELAY = 500;
    private static final int MIN_SHOW_TIME = 500;
    private final Runnable mDelayedHide;
    private final Runnable mDelayedShow;
    boolean mDismissed;
    boolean mPostedHide;
    boolean mPostedShow;
    long mStartTime;

    public ContentLoadingProgressBar(@NonNull Context context) {
        this(context, null);
    }

    public ContentLoadingProgressBar(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs, 0);
        this.mStartTime = -1;
        this.mPostedHide = false;
        this.mPostedShow = false;
        this.mDismissed = false;
        this.mDelayedHide = new Runnable() {
            public void run() {
                ContentLoadingProgressBar.this.mPostedHide = false;
                ContentLoadingProgressBar.this.mStartTime = -1;
                ContentLoadingProgressBar.this.setVisibility(8);
            }
        };
        this.mDelayedShow = new Runnable() {
            public void run() {
                ContentLoadingProgressBar.this.mPostedShow = false;
                if (!ContentLoadingProgressBar.this.mDismissed) {
                    ContentLoadingProgressBar.this.mStartTime = System.currentTimeMillis();
                    ContentLoadingProgressBar.this.setVisibility(0);
                }
            }
        };
    }

    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        removeCallbacks();
    }

    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        removeCallbacks();
    }

    private void removeCallbacks() {
        removeCallbacks(this.mDelayedHide);
        removeCallbacks(this.mDelayedShow);
    }

    public synchronized void hide() {
        this.mDismissed = true;
        removeCallbacks(this.mDelayedShow);
        this.mPostedShow = false;
        long diff = System.currentTimeMillis() - this.mStartTime;
        if (diff < 500) {
            if (this.mStartTime != -1) {
                if (!this.mPostedHide) {
                    postDelayed(this.mDelayedHide, 500 - diff);
                    this.mPostedHide = true;
                }
            }
        }
        setVisibility(8);
    }

    public synchronized void show() {
        this.mStartTime = -1;
        this.mDismissed = false;
        removeCallbacks(this.mDelayedHide);
        this.mPostedHide = false;
        if (!this.mPostedShow) {
            postDelayed(this.mDelayedShow, 500);
            this.mPostedShow = true;
        }
    }
}
