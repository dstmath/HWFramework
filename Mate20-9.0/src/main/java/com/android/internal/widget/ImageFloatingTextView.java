package com.android.internal.widget;

import android.content.Context;
import android.text.BoringLayout;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextUtils;
import android.text.method.TransformationMethod;
import android.util.AttributeSet;
import android.view.RemotableViewMethod;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.TextView;

@RemoteViews.RemoteView
public class ImageFloatingTextView extends TextView {
    private int mImageEndMargin;
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
        this.mLayoutMaxLines = -1;
    }

    /* access modifiers changed from: protected */
    public Layout makeSingleLayout(int wantWidth, BoringLayout.Metrics boring, int ellipsisWidth, Layout.Alignment alignment, boolean shouldEllipsize, TextUtils.TruncateAt effectiveEllipsize, boolean useSaved) {
        int maxLines;
        TransformationMethod transformationMethod = getTransformationMethod();
        CharSequence text = getText();
        if (transformationMethod != null) {
            text = transformationMethod.getTransformation(text, this);
        }
        CharSequence text2 = text == null ? "" : text;
        StaticLayout.Builder builder = StaticLayout.Builder.obtain(text2, 0, text2.length(), getPaint(), wantWidth).setAlignment(alignment).setTextDirection(getTextDirectionHeuristic()).setLineSpacing(getLineSpacingExtra(), getLineSpacingMultiplier()).setIncludePad(getIncludeFontPadding()).setUseLineSpacingFromFallbacks(true).setBreakStrategy(1).setHyphenationFrequency(2);
        if (this.mMaxLinesForHeight > 0) {
            maxLines = this.mMaxLinesForHeight;
        } else {
            maxLines = getMaxLines() >= 0 ? getMaxLines() : Integer.MAX_VALUE;
        }
        builder.setMaxLines(maxLines);
        this.mLayoutMaxLines = maxLines;
        if (shouldEllipsize) {
            builder.setEllipsize(effectiveEllipsize).setEllipsizedWidth(ellipsisWidth);
        }
        int[] margins = null;
        if (this.mIndentLines > 0) {
            margins = new int[(this.mIndentLines + 1)];
            for (int i = 0; i < this.mIndentLines; i++) {
                margins[i] = this.mImageEndMargin;
            }
        }
        if (this.mResolvedDirection == 1) {
            builder.setIndents(margins, null);
        } else {
            builder.setIndents(null, margins);
        }
        return builder.build();
    }

    @RemotableViewMethod
    public void setImageEndMargin(int imageEndMargin) {
        this.mImageEndMargin = imageEndMargin;
    }

    /* access modifiers changed from: protected */
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int availableHeight = (View.MeasureSpec.getSize(heightMeasureSpec) - this.mPaddingTop) - this.mPaddingBottom;
        if (!(getLayout() == null || getLayout().getHeight() == availableHeight)) {
            this.mMaxLinesForHeight = -1;
            nullLayouts();
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        Layout layout = getLayout();
        if (layout.getHeight() > availableHeight) {
            int maxLines = layout.getLineCount() - 1;
            while (maxLines > 1 && layout.getLineBottom(maxLines - 1) > availableHeight) {
                maxLines--;
            }
            if (getMaxLines() > 0) {
                maxLines = Math.min(getMaxLines(), maxLines);
            }
            if (maxLines != this.mLayoutMaxLines) {
                this.mMaxLinesForHeight = maxLines;
                nullLayouts();
                super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            }
        }
    }

    public void onRtlPropertiesChanged(int layoutDirection) {
        super.onRtlPropertiesChanged(layoutDirection);
        if (layoutDirection != this.mResolvedDirection && isLayoutDirectionResolved()) {
            this.mResolvedDirection = layoutDirection;
            if (this.mIndentLines > 0) {
                nullLayouts();
                requestLayout();
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
        nullLayouts();
        requestLayout();
        return true;
    }
}
