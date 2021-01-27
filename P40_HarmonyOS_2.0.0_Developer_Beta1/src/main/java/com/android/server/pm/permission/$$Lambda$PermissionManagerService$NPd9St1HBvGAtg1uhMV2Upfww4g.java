package com.android.server.pm.permission;

import com.android.internal.util.function.TriConsumer;

/* renamed from: com.android.server.pm.permission.-$$Lambda$PermissionManagerService$NPd9St1HBvGAtg1uhMV2Upfww4g  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$PermissionManagerService$NPd9St1HBvGAtg1uhMV2Upfww4g implements TriConsumer {
    public static final /* synthetic */ $$Lambda$PermissionManagerService$NPd9St1HBvGAtg1uhMV2Upfww4g INSTANCE = new $$Lambda$PermissionManagerService$NPd9St1HBvGAtg1uhMV2Upfww4g();

    private /* synthetic */ $$Lambda$PermissionManagerService$NPd9St1HBvGAtg1uhMV2Upfww4g() {
    }

    public final void accept(Object obj, Object obj2, Object obj3) {
        ((PermissionManagerService) obj).doNotifyRuntimePermissionStateChanged((String) obj2, ((Integer) obj3).intValue());
    }
}
