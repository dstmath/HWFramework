package com.huawei.android.text;

import android.text.Layout;
import android.text.SpannableStringBuilder;
import android.text.style.AlignmentSpan;

public class SpannableStringBuilderEx {
    public static void setSpanRight(SpannableStringBuilder stringBuilder, int start, int end, int flags) {
        if (stringBuilder != null) {
            stringBuilder.setSpan(new AlignmentSpan.Standard(Layout.Alignment.ALIGN_RIGHT), start, end, flags);
        }
    }

    public static void setSpanLeft(SpannableStringBuilder stringBuilder, int start, int end, int flags) {
        if (stringBuilder != null) {
            stringBuilder.setSpan(new AlignmentSpan.Standard(Layout.Alignment.ALIGN_LEFT), start, end, flags);
        }
    }
}
