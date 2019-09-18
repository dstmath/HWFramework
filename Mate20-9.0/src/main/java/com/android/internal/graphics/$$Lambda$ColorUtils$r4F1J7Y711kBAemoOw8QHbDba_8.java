package com.android.internal.graphics;

import com.android.internal.graphics.ColorUtils;

/* renamed from: com.android.internal.graphics.-$$Lambda$ColorUtils$r4F1J7Y711kBAemoOw8QHbDba_8  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$ColorUtils$r4F1J7Y711kBAemoOw8QHbDba_8 implements ColorUtils.ContrastCalculator {
    public static final /* synthetic */ $$Lambda$ColorUtils$r4F1J7Y711kBAemoOw8QHbDba_8 INSTANCE = new $$Lambda$ColorUtils$r4F1J7Y711kBAemoOw8QHbDba_8();

    private /* synthetic */ $$Lambda$ColorUtils$r4F1J7Y711kBAemoOw8QHbDba_8() {
    }

    public final double calculateContrast(int i, int i2, int i3) {
        return ColorUtils.calculateContrast(ColorUtils.setAlphaComponent(i, i3), i2);
    }
}
