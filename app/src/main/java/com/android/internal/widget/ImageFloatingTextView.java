package com.android.internal.widget;

import android.content.Context;
import android.text.BoringLayout.Metrics;
import android.text.Layout;
import android.text.Layout.Alignment;
import android.text.StaticLayout.Builder;
import android.text.TextUtils.TruncateAt;
import android.util.AttributeSet;
import android.view.RemotableViewMethod;
import android.widget.RemoteViews.RemoteView;
import android.widget.TextView;
import com.android.internal.R;

@RemoteView
public class ImageFloatingTextView extends TextView {
    private int mIndentLines;

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
    }

    protected Layout makeSingleLayout(int wantWidth, Metrics boring, int ellipsisWidth, Alignment alignment, boolean shouldEllipsize, TruncateAt effectiveEllipsize, boolean useSaved) {
        CharSequence text = getText() == null ? "" : getText();
        Builder includePad = Builder.obtain(text, 0, text.length(), getPaint(), wantWidth).setAlignment(alignment).setTextDirection(getTextDirectionHeuristic()).setLineSpacing(getLineSpacingExtra(), getLineSpacingMultiplier()).setIncludePad(getIncludeFontPadding());
        if (!shouldEllipsize) {
            effectiveEllipsize = null;
        }
        Builder builder = includePad.setEllipsize(effectiveEllipsize).setEllipsizedWidth(ellipsisWidth).setBreakStrategy(1).setHyphenationFrequency(2);
        int endMargin = getContext().getResources().getDimensionPixelSize(R.dimen.notification_content_picture_margin);
        int[] iArr = null;
        if (this.mIndentLines > 0) {
            iArr = new int[(this.mIndentLines + 1)];
            for (int i = 0; i < this.mIndentLines; i++) {
                iArr[i] = endMargin;
            }
        }
        if (getLayoutDirection() == 1) {
            builder.setIndents(iArr, null);
        } else {
            builder.setIndents(null, iArr);
        }
        return builder.build();
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
}
