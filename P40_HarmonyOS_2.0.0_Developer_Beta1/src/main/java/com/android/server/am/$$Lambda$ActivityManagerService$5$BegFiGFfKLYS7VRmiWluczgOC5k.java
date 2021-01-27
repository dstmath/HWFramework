package com.android.server.am;

import com.android.internal.os.ProcessCpuTracker;
import com.android.server.am.ActivityManagerService;

/* renamed from: com.android.server.am.-$$Lambda$ActivityManagerService$5$BegFiGFfKLYS7VRmiWluczgOC5k  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$ActivityManagerService$5$BegFiGFfKLYS7VRmiWluczgOC5k implements ProcessCpuTracker.FilterStats {
    public static final /* synthetic */ $$Lambda$ActivityManagerService$5$BegFiGFfKLYS7VRmiWluczgOC5k INSTANCE = new $$Lambda$ActivityManagerService$5$BegFiGFfKLYS7VRmiWluczgOC5k();

    private /* synthetic */ $$Lambda$ActivityManagerService$5$BegFiGFfKLYS7VRmiWluczgOC5k() {
    }

    public final boolean needed(ProcessCpuTracker.Stats stats) {
        return ActivityManagerService.AnonymousClass5.lambda$handleMessage$0(stats);
    }
}
