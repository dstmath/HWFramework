package com.android.server.pm;

import android.content.pm.ResolveInfo;
import java.util.function.Predicate;

/* renamed from: com.android.server.pm.-$$Lambda$HwResolverManager$0LP_VYpi5QajDx78ImD-BrBTqp8  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$HwResolverManager$0LP_VYpi5QajDx78ImDBrBTqp8 implements Predicate {
    public static final /* synthetic */ $$Lambda$HwResolverManager$0LP_VYpi5QajDx78ImDBrBTqp8 INSTANCE = new $$Lambda$HwResolverManager$0LP_VYpi5QajDx78ImDBrBTqp8();

    private /* synthetic */ $$Lambda$HwResolverManager$0LP_VYpi5QajDx78ImDBrBTqp8() {
    }

    @Override // java.util.function.Predicate
    public final boolean test(Object obj) {
        return "com.huawei.pcassistant".equals(((ResolveInfo) obj).activityInfo.applicationInfo.packageName);
    }
}
