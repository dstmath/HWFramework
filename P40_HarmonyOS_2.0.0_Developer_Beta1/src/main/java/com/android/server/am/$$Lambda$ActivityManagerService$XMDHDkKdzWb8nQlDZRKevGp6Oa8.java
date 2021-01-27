package com.android.server.am;

import com.android.internal.os.ProcessCpuTracker;

/* renamed from: com.android.server.am.-$$Lambda$ActivityManagerService$XMDHDkKdzWb8nQlDZRKevGp6Oa8  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$ActivityManagerService$XMDHDkKdzWb8nQlDZRKevGp6Oa8 implements ProcessCpuTracker.FilterStats {
    public static final /* synthetic */ $$Lambda$ActivityManagerService$XMDHDkKdzWb8nQlDZRKevGp6Oa8 INSTANCE = new $$Lambda$ActivityManagerService$XMDHDkKdzWb8nQlDZRKevGp6Oa8();

    private /* synthetic */ $$Lambda$ActivityManagerService$XMDHDkKdzWb8nQlDZRKevGp6Oa8() {
    }

    public final boolean needed(ProcessCpuTracker.Stats stats) {
        return ActivityManagerService.lambda$reportMemUsage$3(stats);
    }
}
