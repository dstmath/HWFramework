package com.android.server.accessibility;

import android.os.RemoteCallbackList;
import com.android.internal.util.function.TriConsumer;

/* renamed from: com.android.server.accessibility.-$$Lambda$AccessibilityManagerService$heq1MRdQjg8BGWFbpV3PEpnDVcg  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$AccessibilityManagerService$heq1MRdQjg8BGWFbpV3PEpnDVcg implements TriConsumer {
    public static final /* synthetic */ $$Lambda$AccessibilityManagerService$heq1MRdQjg8BGWFbpV3PEpnDVcg INSTANCE = new $$Lambda$AccessibilityManagerService$heq1MRdQjg8BGWFbpV3PEpnDVcg();

    private /* synthetic */ $$Lambda$AccessibilityManagerService$heq1MRdQjg8BGWFbpV3PEpnDVcg() {
    }

    public final void accept(Object obj, Object obj2, Object obj3) {
        ((AccessibilityManagerService) obj).sendServicesStateChanged((RemoteCallbackList) obj2, ((Long) obj3).longValue());
    }
}
