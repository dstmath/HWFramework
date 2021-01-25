package com.android.server.wm;

import com.huawei.server.magicwin.HwMagicWinStatistics;
import java.util.function.Consumer;

/* renamed from: com.android.server.wm.-$$Lambda$HwMagicModeSwitcher$04CxX6rBzYUWBOIr4yFVy3KoCn8  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$HwMagicModeSwitcher$04CxX6rBzYUWBOIr4yFVy3KoCn8 implements Consumer {
    public static final /* synthetic */ $$Lambda$HwMagicModeSwitcher$04CxX6rBzYUWBOIr4yFVy3KoCn8 INSTANCE = new $$Lambda$HwMagicModeSwitcher$04CxX6rBzYUWBOIr4yFVy3KoCn8();

    private /* synthetic */ $$Lambda$HwMagicModeSwitcher$04CxX6rBzYUWBOIr4yFVy3KoCn8() {
    }

    @Override // java.util.function.Consumer
    public final void accept(Object obj) {
        HwMagicWinStatistics.getInstance(((HwMagicContainer) obj).getType()).stopTick("move_to_fullscreen");
    }
}
