package com.android.server;

import com.android.internal.util.function.TriConsumer;

/* renamed from: com.android.server.-$$Lambda$AppOpsService$UKMH8n9xZqCOX59uFPylskhjBgo  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$AppOpsService$UKMH8n9xZqCOX59uFPylskhjBgo implements TriConsumer {
    public static final /* synthetic */ $$Lambda$AppOpsService$UKMH8n9xZqCOX59uFPylskhjBgo INSTANCE = new $$Lambda$AppOpsService$UKMH8n9xZqCOX59uFPylskhjBgo();

    private /* synthetic */ $$Lambda$AppOpsService$UKMH8n9xZqCOX59uFPylskhjBgo() {
    }

    public final void accept(Object obj, Object obj2, Object obj3) {
        ((AppOpsService) obj).notifyWatchersOfChange(((Integer) obj2).intValue(), ((Integer) obj3).intValue());
    }
}
