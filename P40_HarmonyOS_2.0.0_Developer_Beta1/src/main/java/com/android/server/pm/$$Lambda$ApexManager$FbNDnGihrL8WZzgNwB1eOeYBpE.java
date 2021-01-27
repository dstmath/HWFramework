package com.android.server.pm;

import android.content.pm.PackageInfo;
import java.util.function.Predicate;

/* renamed from: com.android.server.pm.-$$Lambda$ApexManager$FbNDnGihrL8WZzgNwB1-eOeYBpE  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$ApexManager$FbNDnGihrL8WZzgNwB1eOeYBpE implements Predicate {
    public static final /* synthetic */ $$Lambda$ApexManager$FbNDnGihrL8WZzgNwB1eOeYBpE INSTANCE = new $$Lambda$ApexManager$FbNDnGihrL8WZzgNwB1eOeYBpE();

    private /* synthetic */ $$Lambda$ApexManager$FbNDnGihrL8WZzgNwB1eOeYBpE() {
    }

    @Override // java.util.function.Predicate
    public final boolean test(Object obj) {
        return ApexManager.lambda$getInactivePackages$2((PackageInfo) obj);
    }
}
