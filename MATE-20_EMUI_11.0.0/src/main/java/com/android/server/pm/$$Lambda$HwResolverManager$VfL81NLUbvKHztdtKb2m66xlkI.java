package com.android.server.pm;

import android.content.pm.ResolveInfo;
import java.util.function.Predicate;

/* renamed from: com.android.server.pm.-$$Lambda$HwResolverManager$VfL81NLUbvK-HztdtKb2m66xlkI  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$HwResolverManager$VfL81NLUbvKHztdtKb2m66xlkI implements Predicate {
    public static final /* synthetic */ $$Lambda$HwResolverManager$VfL81NLUbvKHztdtKb2m66xlkI INSTANCE = new $$Lambda$HwResolverManager$VfL81NLUbvKHztdtKb2m66xlkI();

    private /* synthetic */ $$Lambda$HwResolverManager$VfL81NLUbvKHztdtKb2m66xlkI() {
    }

    @Override // java.util.function.Predicate
    public final boolean test(Object obj) {
        return "com.huawei.pcassistant".equals(((ResolveInfo) obj).activityInfo.applicationInfo.packageName);
    }
}
