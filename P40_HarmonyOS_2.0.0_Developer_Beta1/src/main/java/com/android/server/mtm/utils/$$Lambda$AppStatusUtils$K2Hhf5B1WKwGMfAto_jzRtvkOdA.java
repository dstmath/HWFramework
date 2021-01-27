package com.android.server.mtm.utils;

import com.android.server.mtm.iaware.appmng.AwareProcessInfo;
import java.util.function.Predicate;

/* renamed from: com.android.server.mtm.utils.-$$Lambda$AppStatusUtils$K2Hhf5B1WKwGMfAto_jzRtvkOdA  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$AppStatusUtils$K2Hhf5B1WKwGMfAto_jzRtvkOdA implements Predicate {
    public static final /* synthetic */ $$Lambda$AppStatusUtils$K2Hhf5B1WKwGMfAto_jzRtvkOdA INSTANCE = new $$Lambda$AppStatusUtils$K2Hhf5B1WKwGMfAto_jzRtvkOdA();

    private /* synthetic */ $$Lambda$AppStatusUtils$K2Hhf5B1WKwGMfAto_jzRtvkOdA() {
    }

    @Override // java.util.function.Predicate
    public final boolean test(Object obj) {
        return AppStatusUtils.lambda$initPredicates$8((AwareProcessInfo) obj);
    }
}
