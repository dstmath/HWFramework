package com.android.server;

import com.android.server.ConnectivityService;
import java.util.function.ToIntFunction;

/* renamed from: com.android.server.-$$Lambda$ConnectivityService$iOdlQdHoQM14teTS-EPRH-RRL3k  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$ConnectivityService$iOdlQdHoQM14teTSEPRHRRL3k implements ToIntFunction {
    public static final /* synthetic */ $$Lambda$ConnectivityService$iOdlQdHoQM14teTSEPRHRRL3k INSTANCE = new $$Lambda$ConnectivityService$iOdlQdHoQM14teTSEPRHRRL3k();

    private /* synthetic */ $$Lambda$ConnectivityService$iOdlQdHoQM14teTSEPRHRRL3k() {
    }

    @Override // java.util.function.ToIntFunction
    public final int applyAsInt(Object obj) {
        return ((ConnectivityService.NetworkRequestInfo) obj).request.requestId;
    }
}
