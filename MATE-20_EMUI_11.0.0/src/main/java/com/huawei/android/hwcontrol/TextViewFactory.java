package com.huawei.android.hwcontrol;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.hwcontrol.HwWidgetFactory;
import android.text.Layout;
import android.util.AttributeSet;
import android.widget.Editor;
import android.widget.TextView;
import huawei.com.android.internal.widget.HwEditor;

public class TextViewFactory implements HwWidgetFactory.HwTextView {
    private static final boolean IS_VIBRATE_IMPLEMENTED = false;
    private boolean mIsCursorRightMoved = false;
    private boolean mIsCustomStyleUsed = false;

    public void initialTextView(Context context, AttributeSet attrs, TextView textView) {
    }

    public void initTextViewAddtionalStyle(Context context, AttributeSet attrs, TextView view, Editor editor) {
    }

    public boolean isCustomStyle() {
        return this.mIsCustomStyleUsed;
    }

    public void reLayoutAfterMeasure(TextView textView, Layout layout) {
    }

    public void setError(TextView textView, Context context, CharSequence error) {
        Drawable drawable = context.getResources().getDrawable(33751585);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        textView.setError(error, drawable);
    }

    public Editor getEditor(TextView textView) {
        return new HwEditor(textView);
    }

    public boolean playIvtEffect(Context context, String effectName) {
        return false;
    }

    public boolean playIvtEffect(Context context, String effectName, Object what, int start, int end) {
        return false;
    }
}
