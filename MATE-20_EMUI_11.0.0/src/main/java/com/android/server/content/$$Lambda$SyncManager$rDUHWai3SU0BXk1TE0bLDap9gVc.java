package com.android.server.content;

import java.util.function.Predicate;

/* renamed from: com.android.server.content.-$$Lambda$SyncManager$rDUHWai3SU0BXk1TE0bLDap9gVc  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$SyncManager$rDUHWai3SU0BXk1TE0bLDap9gVc implements Predicate {
    public static final /* synthetic */ $$Lambda$SyncManager$rDUHWai3SU0BXk1TE0bLDap9gVc INSTANCE = new $$Lambda$SyncManager$rDUHWai3SU0BXk1TE0bLDap9gVc();

    private /* synthetic */ $$Lambda$SyncManager$rDUHWai3SU0BXk1TE0bLDap9gVc() {
    }

    @Override // java.util.function.Predicate
    public final boolean test(Object obj) {
        return SyncManager.lambda$dumpPendingSyncs$8((SyncOperation) obj);
    }
}
