package com.android.internal.widget;

import android.content.Context;
import android.text.BoringLayout.Metrics;
import android.text.Layout;
import android.text.Layout.Alignment;
import android.text.StaticLayout.Builder;
import android.text.TextUtils.TruncateAt;
import android.util.AttributeSet;
import android.util.LogException;
import android.view.RemotableViewMethod;
import android.view.View.MeasureSpec;
import android.widget.RemoteViews.RemoteView;
import android.widget.TextView;
import com.android.internal.R;

@RemoteView
public class ImageFloatingTextView extends TextView {
    private boolean mBlockLayouts;
    private boolean mFirstMeasure;
    private int mIndentLines;
    private int mLayoutMaxLines;
    private int mMaxLinesForHeight;
    private int mResolvedDirection;

    public ImageFloatingTextView(Context context) {
        this(context, null);
    }

    public ImageFloatingTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ImageFloatingTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public ImageFloatingTextView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mResolvedDirection = -1;
        this.mMaxLinesForHeight = -1;
        this.mFirstMeasure = true;
        this.mLayoutMaxLines = -1;
    }

    protected Layout makeSingleLayout(int wantWidth, Metrics boring, int ellipsisWidth, Alignment alignment, boolean shouldEllipsize, TruncateAt effectiveEllipsize, boolean useSaved) {
        CharSequence text = getText() == null ? LogException.NO_VALUE : getText();
        Builder builder = Builder.obtain(text, 0, text.length(), getPaint(), wantWidth).setAlignment(alignment).setTextDirection(getTextDirectionHeuristic()).setLineSpacing(getLineSpacingExtra(), getLineSpacingMultiplier()).setIncludePad(getIncludeFontPadding()).setBreakStrategy(1).setHyphenationFrequency(2);
        int maxLines = this.mMaxLinesForHeight > 0 ? this.mMaxLinesForHeight : getMaxLines() >= 0 ? getMaxLines() : Integer.MAX_VALUE;
        builder.setMaxLines(maxLines);
        this.mLayoutMaxLines = maxLines;
        if (shouldEllipsize) {
            builder.setEllipsize(effectiveEllipsize).setEllipsizedWidth(ellipsisWidth);
        }
        int endMargin = getContext().getResources().getDimensionPixelSize(R.dimen.notification_content_picture_margin);
        int[] iArr = null;
        if (this.mIndentLines > 0) {
            iArr = new int[(this.mIndentLines + 1)];
            for (int i = 0; i < this.mIndentLines; i++) {
                iArr[i] = endMargin;
            }
        }
        if (this.mResolvedDirection == 1) {
            builder.setIndents(iArr, null);
        } else {
            builder.setIndents(null, iArr);
        }
        return builder.build();
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int maxLines = Math.max(1, ((MeasureSpec.getSize(heightMeasureSpec) - this.mPaddingTop) - this.mPaddingBottom) / getLineHeight());
        if (getMaxLines() > 0) {
            maxLines = Math.min(getMaxLines(), maxLines);
        }
        if (maxLines != this.mMaxLinesForHeight) {
            this.mMaxLinesForHeight = maxLines;
            if (!(getLayout() == null || this.mMaxLinesForHeight == this.mLayoutMaxLines)) {
                this.mBlockLayouts = true;
                setHint(getHint());
                this.mBlockLayouts = false;
            }
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public void requestLayout() {
        if (!this.mBlockLayouts) {
            super.requestLayout();
        }
    }

    public void onRtlPropertiesChanged(int layoutDirection) {
        super.onRtlPropertiesChanged(layoutDirection);
        if (layoutDirection != this.mResolvedDirection && isLayoutDirectionResolved()) {
            this.mResolvedDirection = layoutDirection;
            if (this.mIndentLines > 0) {
                setHint(getHint());
            }
        }
    }

    @RemotableViewMethod
    public void setHasImage(boolean hasImage) {
        setNumIndentLines(hasImage ? 2 : 0);
    }

    public boolean setNumIndentLines(int lines) {
        if (this.mIndentLines == lines) {
            return false;
        }
        this.mIndentLines = lines;
        setHint(getHint());
        return true;
    }

    public int getLayoutHeight() {
        return getLayout().getHeight();
    }
}
