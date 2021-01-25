package com.android.server.content;

import java.util.function.Predicate;

/* renamed from: com.android.server.content.-$$Lambda$SyncManager$ag0YGuZ1oL06fytmNlyErbNyYcw  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$SyncManager$ag0YGuZ1oL06fytmNlyErbNyYcw implements Predicate {
    public static final /* synthetic */ $$Lambda$SyncManager$ag0YGuZ1oL06fytmNlyErbNyYcw INSTANCE = new $$Lambda$SyncManager$ag0YGuZ1oL06fytmNlyErbNyYcw();

    private /* synthetic */ $$Lambda$SyncManager$ag0YGuZ1oL06fytmNlyErbNyYcw() {
    }

    @Override // java.util.function.Predicate
    public final boolean test(Object obj) {
        return ((SyncOperation) obj).isPeriodic;
    }
}
