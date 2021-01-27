package com.android.server.wm;

import com.android.server.am.ActivityManagerServiceEx;
import java.util.function.Function;

/* renamed from: com.android.server.wm.-$$Lambda$HwMagicWinManager$dtmjV-mrQAAUiq5Bz2DnD-51N9Q  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$HwMagicWinManager$dtmjVmrQAAUiq5Bz2DnD51N9Q implements Function {
    public static final /* synthetic */ $$Lambda$HwMagicWinManager$dtmjVmrQAAUiq5Bz2DnD51N9Q INSTANCE = new $$Lambda$HwMagicWinManager$dtmjVmrQAAUiq5Bz2DnD51N9Q();

    private /* synthetic */ $$Lambda$HwMagicWinManager$dtmjVmrQAAUiq5Bz2DnD51N9Q() {
    }

    @Override // java.util.function.Function
    public final Object apply(Object obj) {
        return ((ActivityManagerServiceEx) obj).getActivityTaskManagerEx();
    }
}
