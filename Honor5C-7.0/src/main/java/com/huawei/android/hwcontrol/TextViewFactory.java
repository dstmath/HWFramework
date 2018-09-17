package com.huawei.android.hwcontrol;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.hwcontrol.HwWidgetFactory.HwTextView;
import android.os.SystemProperties;
import android.provider.Settings.System;
import android.text.Layout;
import android.text.Selection;
import android.util.AttributeSet;
import android.widget.Editor;
import android.widget.TextView;
import huawei.android.os.HwGeneralManager;
import huawei.com.android.internal.widget.HwEditor;

public class TextViewFactory implements HwTextView {
    static final boolean DEBUG_EXTRACT = false;
    static final String LOG_TAG = "TextViewFactory";
    private boolean isVibrateImplemented;
    private boolean mIsCursorRightMoved;
    private boolean mUseCustomStyle;

    public TextViewFactory() {
        this.isVibrateImplemented = SystemProperties.getBoolean("ro.config.touch_vibrate", DEBUG_EXTRACT);
        this.mIsCursorRightMoved = DEBUG_EXTRACT;
        this.mUseCustomStyle = DEBUG_EXTRACT;
    }

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
        Drawable dr = context.getResources().getDrawable(33751353);
        dr.setBounds(0, 0, dr.getIntrinsicWidth(), dr.getIntrinsicHeight());
        textView.setError(error, dr);
    }

    public Editor getEditor(TextView textView) {
        return new HwEditor(textView);
    }

    public boolean playIvtEffect(Context context, String effectName) {
        if (!this.isVibrateImplemented || 1 != System.getInt(context.getContentResolver(), "touch_vibrate_mode", 1)) {
            return DEBUG_EXTRACT;
        }
        HwGeneralManager.getInstance().playIvtEffect(effectName);
        return true;
    }

    public boolean playIvtEffect(Context context, String effectName, Object what, int start, int end) {
        if (this.isVibrateImplemented) {
            if (what == Selection.SELECTION_START) {
                boolean z;
                if (start == end) {
                    z = true;
                } else {
                    z = DEBUG_EXTRACT;
                }
                this.mIsCursorRightMoved = z;
            }
            if (!this.mIsCursorRightMoved && what == Selection.SELECTION_END && Math.abs(start - end) == 1) {
                return playIvtEffect(context, effectName);
            }
        }
        return DEBUG_EXTRACT;
    }
}
