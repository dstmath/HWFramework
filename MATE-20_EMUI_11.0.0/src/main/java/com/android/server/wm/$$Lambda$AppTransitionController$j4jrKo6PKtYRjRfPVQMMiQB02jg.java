package com.android.server.wm;

import java.util.function.Predicate;

/* renamed from: com.android.server.wm.-$$Lambda$AppTransitionController$j4jrKo6PKtYRjRfPVQMMiQB02jg  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$AppTransitionController$j4jrKo6PKtYRjRfPVQMMiQB02jg implements Predicate {
    public static final /* synthetic */ $$Lambda$AppTransitionController$j4jrKo6PKtYRjRfPVQMMiQB02jg INSTANCE = new $$Lambda$AppTransitionController$j4jrKo6PKtYRjRfPVQMMiQB02jg();

    private /* synthetic */ $$Lambda$AppTransitionController$j4jrKo6PKtYRjRfPVQMMiQB02jg() {
    }

    @Override // java.util.function.Predicate
    public final boolean test(Object obj) {
        return AppTransitionController.lambda$findAnimLayoutParamsToken$2((AppWindowToken) obj);
    }
}
