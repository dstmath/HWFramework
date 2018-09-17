package com.android.internal.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View.MeasureSpec;
import android.widget.ImageView;
import com.android.internal.os.HwBootFail;

public class PreferenceImageView extends ImageView {
    public PreferenceImageView(Context context) {
        this(context, null);
    }

    public PreferenceImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PreferenceImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public PreferenceImageView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        if (widthMode == RtlSpacingHelper.UNDEFINED || widthMode == 0) {
            int widthSize = MeasureSpec.getSize(widthMeasureSpec);
            int maxWidth = getMaxWidth();
            if (maxWidth != HwBootFail.STAGE_BOOT_SUCCESS && (maxWidth < widthSize || widthMode == 0)) {
                widthMeasureSpec = MeasureSpec.makeMeasureSpec(maxWidth, RtlSpacingHelper.UNDEFINED);
            }
        }
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        if (heightMode == RtlSpacingHelper.UNDEFINED || heightMode == 0) {
            int heightSize = MeasureSpec.getSize(heightMeasureSpec);
            int maxHeight = getMaxHeight();
            if (maxHeight != HwBootFail.STAGE_BOOT_SUCCESS && (maxHeight < heightSize || heightMode == 0)) {
                heightMeasureSpec = MeasureSpec.makeMeasureSpec(maxHeight, RtlSpacingHelper.UNDEFINED);
            }
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
