package com.android.server.print;

import java.util.List;
import java.util.function.BiConsumer;

/* renamed from: com.android.server.print.-$$Lambda$UserState$f3loorfBpq9Tu3Vl5vt4Ul321ok  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$UserState$f3loorfBpq9Tu3Vl5vt4Ul321ok implements BiConsumer {
    public static final /* synthetic */ $$Lambda$UserState$f3loorfBpq9Tu3Vl5vt4Ul321ok INSTANCE = new $$Lambda$UserState$f3loorfBpq9Tu3Vl5vt4Ul321ok();

    private /* synthetic */ $$Lambda$UserState$f3loorfBpq9Tu3Vl5vt4Ul321ok() {
    }

    @Override // java.util.function.BiConsumer
    public final void accept(Object obj, Object obj2) {
        ((UserState) obj).handleDispatchPrintServiceRecommendationsUpdated((List) obj2);
    }
}
