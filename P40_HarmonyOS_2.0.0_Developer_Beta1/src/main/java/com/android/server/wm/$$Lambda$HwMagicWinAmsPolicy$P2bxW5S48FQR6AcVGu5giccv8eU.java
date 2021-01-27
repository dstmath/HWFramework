package com.android.server.wm;

import java.util.function.Function;

/* renamed from: com.android.server.wm.-$$Lambda$HwMagicWinAmsPolicy$P2bxW5S48FQR6AcVGu5giccv8eU  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$HwMagicWinAmsPolicy$P2bxW5S48FQR6AcVGu5giccv8eU implements Function {
    public static final /* synthetic */ $$Lambda$HwMagicWinAmsPolicy$P2bxW5S48FQR6AcVGu5giccv8eU INSTANCE = new $$Lambda$HwMagicWinAmsPolicy$P2bxW5S48FQR6AcVGu5giccv8eU();

    private /* synthetic */ $$Lambda$HwMagicWinAmsPolicy$P2bxW5S48FQR6AcVGu5giccv8eU() {
    }

    @Override // java.util.function.Function
    public final Object apply(Object obj) {
        return Integer.valueOf(((ActivityStackEx) obj).getDisplayId());
    }
}
