package com.android.server.mtm.utils;

import com.android.server.mtm.iaware.appmng.AwareProcessInfo;
import java.util.function.Predicate;

/* renamed from: com.android.server.mtm.utils.-$$Lambda$AppStatusUtils$T41BqTBsszutBC7t2M7WZrTBG7Q  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$AppStatusUtils$T41BqTBsszutBC7t2M7WZrTBG7Q implements Predicate {
    public static final /* synthetic */ $$Lambda$AppStatusUtils$T41BqTBsszutBC7t2M7WZrTBG7Q INSTANCE = new $$Lambda$AppStatusUtils$T41BqTBsszutBC7t2M7WZrTBG7Q();

    private /* synthetic */ $$Lambda$AppStatusUtils$T41BqTBsszutBC7t2M7WZrTBG7Q() {
    }

    @Override // java.util.function.Predicate
    public final boolean test(Object obj) {
        return AppStatusUtils.lambda$initPredicates$1((AwareProcessInfo) obj);
    }
}
