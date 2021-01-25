package com.android.server.wm;

import java.util.function.Function;

/* renamed from: com.android.server.wm.-$$Lambda$HwMagicWinWmsPolicy$P2Lmov7G9GCOwVXZX31aW8xmw0I  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$HwMagicWinWmsPolicy$P2Lmov7G9GCOwVXZX31aW8xmw0I implements Function {
    public static final /* synthetic */ $$Lambda$HwMagicWinWmsPolicy$P2Lmov7G9GCOwVXZX31aW8xmw0I INSTANCE = new $$Lambda$HwMagicWinWmsPolicy$P2Lmov7G9GCOwVXZX31aW8xmw0I();

    private /* synthetic */ $$Lambda$HwMagicWinWmsPolicy$P2Lmov7G9GCOwVXZX31aW8xmw0I() {
    }

    @Override // java.util.function.Function
    public final Object apply(Object obj) {
        return ((WindowStateEx) obj).getAppWindowTokenEx();
    }
}
