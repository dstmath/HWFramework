package com.android.server.mtm.utils;

import com.android.server.mtm.iaware.appmng.AwareAppMngSort;
import com.android.server.mtm.iaware.appmng.AwareProcessInfo;
import java.util.function.Predicate;

/* renamed from: com.android.server.mtm.utils.-$$Lambda$AppStatusUtils$AMRUNX7hhUrxr_vj2FcJCaX1hXQ  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$AppStatusUtils$AMRUNX7hhUrxr_vj2FcJCaX1hXQ implements Predicate {
    public static final /* synthetic */ $$Lambda$AppStatusUtils$AMRUNX7hhUrxr_vj2FcJCaX1hXQ INSTANCE = new $$Lambda$AppStatusUtils$AMRUNX7hhUrxr_vj2FcJCaX1hXQ();

    private /* synthetic */ $$Lambda$AppStatusUtils$AMRUNX7hhUrxr_vj2FcJCaX1hXQ() {
    }

    @Override // java.util.function.Predicate
    public final boolean test(Object obj) {
        return AwareAppMngSort.getInstance().checkNonSystemUser((AwareProcessInfo) obj);
    }
}
