package com.huawei.android.text;

import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextDirectionHeuristic;
import android.text.TextPaint;
import android.text.TextUtils;

public class StaticLayoutEx {
    public static StaticLayout getStaticLayout(CharSequence source, int bufstart, int bufend, TextPaint paint, int outerwidth, Layout.Alignment align, TextDirectionHeuristic textDir, float spacingmult, float spacingadd, boolean isIncludepad, TextUtils.TruncateAt ellipsize, int ellipsizedWidth, int maxLines) {
        return new StaticLayout(source, bufstart, bufend, paint, outerwidth, align, textDir, spacingmult, spacingadd, isIncludepad, ellipsize, ellipsizedWidth, maxLines);
    }
}
