package com.android.server.mtm.utils;

import com.android.server.mtm.iaware.appmng.AwareProcessInfo;
import com.android.server.rms.iaware.appmng.AwareIntelligentRecg;
import java.util.function.Predicate;

/* renamed from: com.android.server.mtm.utils.-$$Lambda$AppStatusUtils$Z6qTDdZKkG5zKP5Oku2ObkIlCiA  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$AppStatusUtils$Z6qTDdZKkG5zKP5Oku2ObkIlCiA implements Predicate {
    public static final /* synthetic */ $$Lambda$AppStatusUtils$Z6qTDdZKkG5zKP5Oku2ObkIlCiA INSTANCE = new $$Lambda$AppStatusUtils$Z6qTDdZKkG5zKP5Oku2ObkIlCiA();

    private /* synthetic */ $$Lambda$AppStatusUtils$Z6qTDdZKkG5zKP5Oku2ObkIlCiA() {
    }

    @Override // java.util.function.Predicate
    public final boolean test(Object obj) {
        return AwareIntelligentRecg.getInstance().isBluetoothLast(((AwareProcessInfo) obj).procProcInfo.mPid, AppStatusUtils.BLUETOOTH_PROTECT_TIME);
    }
}
