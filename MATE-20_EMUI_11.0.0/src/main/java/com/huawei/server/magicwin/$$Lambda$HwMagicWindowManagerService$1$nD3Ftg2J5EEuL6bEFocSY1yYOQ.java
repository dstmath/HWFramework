package com.huawei.server.magicwin;

import com.android.server.wm.HwMagicContainer;
import java.util.function.Consumer;

/* renamed from: com.huawei.server.magicwin.-$$Lambda$HwMagicWindowManagerService$1$nD3Ftg2J5EEuL-6bEFocSY1yYOQ  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$HwMagicWindowManagerService$1$nD3Ftg2J5EEuL6bEFocSY1yYOQ implements Consumer {
    public static final /* synthetic */ $$Lambda$HwMagicWindowManagerService$1$nD3Ftg2J5EEuL6bEFocSY1yYOQ INSTANCE = new $$Lambda$HwMagicWindowManagerService$1$nD3Ftg2J5EEuL6bEFocSY1yYOQ();

    private /* synthetic */ $$Lambda$HwMagicWindowManagerService$1$nD3Ftg2J5EEuL6bEFocSY1yYOQ() {
    }

    @Override // java.util.function.Consumer
    public final void accept(Object obj) {
        HwMagicWinStatistics.getInstance(((HwMagicContainer) obj).getType()).stopTick("screen_off");
    }
}
