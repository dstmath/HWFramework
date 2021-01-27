package com.android.server.mtm.utils;

import com.android.server.mtm.iaware.appmng.AwareProcessInfo;
import java.util.function.Predicate;

/* renamed from: com.android.server.mtm.utils.-$$Lambda$AppStatusUtils$lb8SJxzeyQhggDjfulZ2u7QZmyc  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$AppStatusUtils$lb8SJxzeyQhggDjfulZ2u7QZmyc implements Predicate {
    public static final /* synthetic */ $$Lambda$AppStatusUtils$lb8SJxzeyQhggDjfulZ2u7QZmyc INSTANCE = new $$Lambda$AppStatusUtils$lb8SJxzeyQhggDjfulZ2u7QZmyc();

    private /* synthetic */ $$Lambda$AppStatusUtils$lb8SJxzeyQhggDjfulZ2u7QZmyc() {
    }

    @Override // java.util.function.Predicate
    public final boolean test(Object obj) {
        return AppStatusUtils.lambda$initPredicates$0((AwareProcessInfo) obj);
    }
}
