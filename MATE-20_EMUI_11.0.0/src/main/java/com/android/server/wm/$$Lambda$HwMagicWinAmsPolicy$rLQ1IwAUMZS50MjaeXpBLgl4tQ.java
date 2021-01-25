package com.android.server.wm;

import java.util.function.Function;

/* renamed from: com.android.server.wm.-$$Lambda$HwMagicWinAmsPolicy$rLQ1I-wAUMZS50MjaeXpBLgl4tQ  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$HwMagicWinAmsPolicy$rLQ1IwAUMZS50MjaeXpBLgl4tQ implements Function {
    public static final /* synthetic */ $$Lambda$HwMagicWinAmsPolicy$rLQ1IwAUMZS50MjaeXpBLgl4tQ INSTANCE = new $$Lambda$HwMagicWinAmsPolicy$rLQ1IwAUMZS50MjaeXpBLgl4tQ();

    private /* synthetic */ $$Lambda$HwMagicWinAmsPolicy$rLQ1IwAUMZS50MjaeXpBLgl4tQ() {
    }

    @Override // java.util.function.Function
    public final Object apply(Object obj) {
        return Integer.valueOf(((ActivityStackEx) obj).getDisplayId());
    }
}
