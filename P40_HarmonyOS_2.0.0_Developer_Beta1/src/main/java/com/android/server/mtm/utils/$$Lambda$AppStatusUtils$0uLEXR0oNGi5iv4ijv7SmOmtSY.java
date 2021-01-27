package com.android.server.mtm.utils;

import com.android.server.mtm.iaware.appmng.AwareProcessInfo;
import com.android.server.rms.iaware.appmng.AwareIntelligentRecg;
import java.util.function.Predicate;

/* renamed from: com.android.server.mtm.utils.-$$Lambda$AppStatusUtils$0uLEXR0oNGi5iv4ij-v7SmOmtSY  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$AppStatusUtils$0uLEXR0oNGi5iv4ijv7SmOmtSY implements Predicate {
    public static final /* synthetic */ $$Lambda$AppStatusUtils$0uLEXR0oNGi5iv4ijv7SmOmtSY INSTANCE = new $$Lambda$AppStatusUtils$0uLEXR0oNGi5iv4ijv7SmOmtSY();

    private /* synthetic */ $$Lambda$AppStatusUtils$0uLEXR0oNGi5iv4ijv7SmOmtSY() {
    }

    @Override // java.util.function.Predicate
    public final boolean test(Object obj) {
        return AwareIntelligentRecg.getInstance().isBluetoothConnect(((AwareProcessInfo) obj).procProcInfo.mPid);
    }
}
