package com.android.server.wm;

import java.util.function.Function;

/* renamed from: com.android.server.wm.-$$Lambda$HwMagicWinAmsPolicy$tW-vAgLOO1B-sNQbAloKRutz-58  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$HwMagicWinAmsPolicy$tWvAgLOO1BsNQbAloKRutz58 implements Function {
    public static final /* synthetic */ $$Lambda$HwMagicWinAmsPolicy$tWvAgLOO1BsNQbAloKRutz58 INSTANCE = new $$Lambda$HwMagicWinAmsPolicy$tWvAgLOO1BsNQbAloKRutz58();

    private /* synthetic */ $$Lambda$HwMagicWinAmsPolicy$tWvAgLOO1BsNQbAloKRutz58() {
    }

    @Override // java.util.function.Function
    public final Object apply(Object obj) {
        return ((ActivityTaskManagerServiceEx) obj).getRootActivityContainer();
    }
}
