package com.huawei.android.text;

import android.text.Layout.Alignment;
import android.text.StaticLayout;
import android.text.TextDirectionHeuristic;
import android.text.TextPaint;
import android.text.TextUtils.TruncateAt;

public class StaticLayoutEx {
    public static StaticLayout getStaticLayout(CharSequence source, int bufstart, int bufend, TextPaint paint, int outerwidth, Alignment align, TextDirectionHeuristic textDir, float spacingmult, float spacingadd, boolean includepad, TruncateAt ellipsize, int ellipsizedWidth, int maxLines) {
        return new StaticLayout(source, bufstart, bufend, paint, outerwidth, align, textDir, spacingmult, spacingadd, includepad, ellipsize, ellipsizedWidth, maxLines);
    }
}
