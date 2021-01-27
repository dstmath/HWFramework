package com.android.server.wm;

import java.util.function.Function;

/* renamed from: com.android.server.wm.-$$Lambda$HwMagicWinWmsPolicy$lWW4ckzJKmBf4QkxBnZVeaUIWOg  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$HwMagicWinWmsPolicy$lWW4ckzJKmBf4QkxBnZVeaUIWOg implements Function {
    public static final /* synthetic */ $$Lambda$HwMagicWinWmsPolicy$lWW4ckzJKmBf4QkxBnZVeaUIWOg INSTANCE = new $$Lambda$HwMagicWinWmsPolicy$lWW4ckzJKmBf4QkxBnZVeaUIWOg();

    private /* synthetic */ $$Lambda$HwMagicWinWmsPolicy$lWW4ckzJKmBf4QkxBnZVeaUIWOg() {
    }

    @Override // java.util.function.Function
    public final Object apply(Object obj) {
        return ((WindowStateEx) obj).getAppWindowTokenEx();
    }
}
