package com.android.server.role;

import com.android.internal.util.function.QuintConsumer;

/* renamed from: com.android.server.role.-$$Lambda$RoleManagerService$TCTA4I2bhEypguZihxs4ezif6t0  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$RoleManagerService$TCTA4I2bhEypguZihxs4ezif6t0 implements QuintConsumer {
    public static final /* synthetic */ $$Lambda$RoleManagerService$TCTA4I2bhEypguZihxs4ezif6t0 INSTANCE = new $$Lambda$RoleManagerService$TCTA4I2bhEypguZihxs4ezif6t0();

    private /* synthetic */ $$Lambda$RoleManagerService$TCTA4I2bhEypguZihxs4ezif6t0() {
    }

    public final void accept(Object obj, Object obj2, Object obj3, Object obj4, Object obj5) {
        ((RoleManagerService) obj).notifyRoleHoldersChanged((String) obj2, ((Integer) obj3).intValue(), (String) obj4, (String) obj5);
    }
}
