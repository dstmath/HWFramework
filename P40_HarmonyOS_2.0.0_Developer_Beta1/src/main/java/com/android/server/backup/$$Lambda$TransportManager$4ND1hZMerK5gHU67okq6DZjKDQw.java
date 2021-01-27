package com.android.server.backup;

import android.content.ComponentName;
import java.util.function.Predicate;

/* renamed from: com.android.server.backup.-$$Lambda$TransportManager$4ND1hZMerK5gHU67okq6DZjKDQw  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$TransportManager$4ND1hZMerK5gHU67okq6DZjKDQw implements Predicate {
    public static final /* synthetic */ $$Lambda$TransportManager$4ND1hZMerK5gHU67okq6DZjKDQw INSTANCE = new $$Lambda$TransportManager$4ND1hZMerK5gHU67okq6DZjKDQw();

    private /* synthetic */ $$Lambda$TransportManager$4ND1hZMerK5gHU67okq6DZjKDQw() {
    }

    @Override // java.util.function.Predicate
    public final boolean test(Object obj) {
        return TransportManager.lambda$onPackageAdded$1((ComponentName) obj);
    }
}
