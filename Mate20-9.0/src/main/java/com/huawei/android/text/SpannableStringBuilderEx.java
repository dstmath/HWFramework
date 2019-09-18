package com.huawei.android.text;

import android.text.Layout;
import android.text.SpannableStringBuilder;
import android.text.style.AlignmentSpan;

public class SpannableStringBuilderEx {
    public static void setSpanRight(SpannableStringBuilder ssb, int start, int end, int flags) {
        ssb.setSpan(new AlignmentSpan.Standard(Layout.Alignment.ALIGN_RIGHT), start, end, flags);
    }

    public static void setSpanLeft(SpannableStringBuilder ssb, int start, int end, int flags) {
        ssb.setSpan(new AlignmentSpan.Standard(Layout.Alignment.ALIGN_LEFT), start, end, flags);
    }
}
