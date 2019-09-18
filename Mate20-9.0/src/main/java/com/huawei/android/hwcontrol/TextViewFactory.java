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
    static final boolean DEBUG_EXTRACT = false;
    static final String LOG_TAG = "TextViewFactory";
    private static final boolean isVibrateImplemented = false;
    private boolean mIsCursorRightMoved = false;
    private boolean mUseCustomStyle = false;

    public void initialTextView(Context context, AttributeSet attrs, TextView tv) {
    }

    public void initTextViewAddtionalStyle(Context context, AttributeSet attrs, TextView view, Editor editor) {
    }

    public boolean isCustomStyle() {
        return this.mUseCustomStyle;
    }

    public void reLayoutAfterMeasure(TextView textView, Layout layout) {
    }

    public void setError(TextView textView, Context context, CharSequence error) {
        Drawable dr = context.getResources().getDrawable(33751585);
        dr.setBounds(0, 0, dr.getIntrinsicWidth(), dr.getIntrinsicHeight());
        textView.setError(error, dr);
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
