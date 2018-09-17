package com.huawei.android.text;

import android.text.Layout.Alignment;
import android.text.SpannableStringBuilder;
import android.text.style.AlignmentSpan.Standard;

public class SpannableStringBuilderEx {
    public static void setSpanRight(SpannableStringBuilder ssb, int start, int end, int flags) {
        ssb.setSpan(new Standard(Alignment.ALIGN_RIGHT), start, end, flags);
    }

    public static void setSpanLeft(SpannableStringBuilder ssb, int start, int end, int flags) {
        ssb.setSpan(new Standard(Alignment.ALIGN_LEFT), start, end, flags);
    }
}
