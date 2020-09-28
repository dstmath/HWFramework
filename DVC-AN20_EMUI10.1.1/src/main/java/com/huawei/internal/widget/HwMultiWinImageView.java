package com.huawei.internal.widget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

public class HwMultiWinImageView extends ImageView {
    private static final String TAG = "HwMultiWinImageView";

    public HwMultiWinImageView(Context context) {
        super(context);
    }

    public HwMultiWinImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public HwMultiWinImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public HwMultiWinImageView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void setImageDrawable(Drawable drawable) {
        super.setImageDrawable(drawable);
        if (drawable == null) {
            Log.w(TAG, "MultiWindow ImageView drawable set to null: " + this);
            Log.d(TAG, Log.getStackTraceString(new Throwable()));
        }
    }
}
