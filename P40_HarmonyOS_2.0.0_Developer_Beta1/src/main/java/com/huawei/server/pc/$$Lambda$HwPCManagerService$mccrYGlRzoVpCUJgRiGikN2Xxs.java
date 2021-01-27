package com.huawei.server.pc;

import android.app.ActivityManager;
import java.util.function.Function;

/* renamed from: com.huawei.server.pc.-$$Lambda$HwPCManagerService$mccrYGlRzoVpCUJgRiGik-N2Xxs  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$HwPCManagerService$mccrYGlRzoVpCUJgRiGikN2Xxs implements Function {
    public static final /* synthetic */ $$Lambda$HwPCManagerService$mccrYGlRzoVpCUJgRiGikN2Xxs INSTANCE = new $$Lambda$HwPCManagerService$mccrYGlRzoVpCUJgRiGikN2Xxs();

    private /* synthetic */ $$Lambda$HwPCManagerService$mccrYGlRzoVpCUJgRiGikN2Xxs() {
    }

    @Override // java.util.function.Function
    public final Object apply(Object obj) {
        return Integer.valueOf(((ActivityManager.RecentTaskInfo) obj).id);
    }
}
