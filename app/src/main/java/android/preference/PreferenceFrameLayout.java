package android.preference;

import android.content.Context;
import android.content.res.TypedArray;
import android.net.wifi.wifipro.NetworkHistoryUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import com.android.internal.R;

public class PreferenceFrameLayout extends FrameLayout {
    private static final int DEFAULT_BORDER_BOTTOM = 0;
    private static final int DEFAULT_BORDER_LEFT = 0;
    private static final int DEFAULT_BORDER_RIGHT = 0;
    private static final int DEFAULT_BORDER_TOP = 0;
    private final int mBorderBottom;
    private final int mBorderLeft;
    private final int mBorderRight;
    private final int mBorderTop;
    private boolean mPaddingApplied;

    public static class LayoutParams extends android.widget.FrameLayout.LayoutParams {
        public boolean removeBorders;

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
            this.removeBorders = false;
            TypedArray a = c.obtainStyledAttributes(attrs, R.styleable.PreferenceFrameLayout_Layout);
            this.removeBorders = a.getBoolean(0, false);
            a.recycle();
        }

        public LayoutParams(int width, int height) {
            super(width, height);
            this.removeBorders = false;
        }
    }

    public PreferenceFrameLayout(Context context) {
        this(context, null);
    }

    public PreferenceFrameLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 18219055);
    }

    public PreferenceFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public PreferenceFrameLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.PreferenceFrameLayout, defStyleAttr, defStyleRes);
        float density = context.getResources().getDisplayMetrics().density;
        int defaultBottomPadding = (int) ((density * 0.0f) + NetworkHistoryUtils.RECOVERY_PERCENTAGE);
        int defaultLeftPadding = (int) ((density * 0.0f) + NetworkHistoryUtils.RECOVERY_PERCENTAGE);
        int defaultRightPadding = (int) ((density * 0.0f) + NetworkHistoryUtils.RECOVERY_PERCENTAGE);
        this.mBorderTop = a.getDimensionPixelSize(0, (int) ((density * 0.0f) + NetworkHistoryUtils.RECOVERY_PERCENTAGE));
        this.mBorderBottom = a.getDimensionPixelSize(1, defaultBottomPadding);
        this.mBorderLeft = a.getDimensionPixelSize(2, defaultLeftPadding);
        this.mBorderRight = a.getDimensionPixelSize(3, defaultRightPadding);
        a.recycle();
    }

    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    public void addView(View child) {
        LayoutParams layoutParams = null;
        int borderTop = getPaddingTop();
        int borderBottom = getPaddingBottom();
        int borderLeft = getPaddingLeft();
        int borderRight = getPaddingRight();
        if (child.getLayoutParams() instanceof LayoutParams) {
            layoutParams = (LayoutParams) child.getLayoutParams();
        }
        if (layoutParams == null || !layoutParams.removeBorders) {
            if (!this.mPaddingApplied) {
                borderTop += this.mBorderTop;
                borderBottom += this.mBorderBottom;
                borderLeft += this.mBorderLeft;
                borderRight += this.mBorderRight;
                this.mPaddingApplied = true;
            }
        } else if (this.mPaddingApplied) {
            borderTop -= this.mBorderTop;
            borderBottom -= this.mBorderBottom;
            borderLeft -= this.mBorderLeft;
            borderRight -= this.mBorderRight;
            this.mPaddingApplied = false;
        }
        int previousTop = getPaddingTop();
        int previousBottom = getPaddingBottom();
        int previousLeft = getPaddingLeft();
        int previousRight = getPaddingRight();
        if (previousTop == borderTop && previousBottom == borderBottom && previousLeft == borderLeft) {
            if (previousRight != borderRight) {
            }
            super.addView(child);
        }
        setPadding(borderLeft, borderTop, borderRight, borderBottom);
        super.addView(child);
    }
}
