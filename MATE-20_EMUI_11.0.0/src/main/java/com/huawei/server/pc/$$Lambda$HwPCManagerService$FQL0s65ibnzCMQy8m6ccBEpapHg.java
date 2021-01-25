package com.huawei.server.pc;

import android.app.ActivityManager;
import java.util.function.Predicate;

/* renamed from: com.huawei.server.pc.-$$Lambda$HwPCManagerService$FQL0s65ibnzCMQy8m6ccBEpapHg  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$HwPCManagerService$FQL0s65ibnzCMQy8m6ccBEpapHg implements Predicate {
    public static final /* synthetic */ $$Lambda$HwPCManagerService$FQL0s65ibnzCMQy8m6ccBEpapHg INSTANCE = new $$Lambda$HwPCManagerService$FQL0s65ibnzCMQy8m6ccBEpapHg();

    private /* synthetic */ $$Lambda$HwPCManagerService$FQL0s65ibnzCMQy8m6ccBEpapHg() {
    }

    @Override // java.util.function.Predicate
    public final boolean test(Object obj) {
        return HwPCManagerService.lambda$removeTaskFromRecent$9((ActivityManager.RecentTaskInfo) obj);
    }
}
