package com.android.server.slice;

import android.app.slice.SliceSpec;
import java.util.function.Predicate;

/* renamed from: com.android.server.slice.-$$Lambda$PinnedSliceState$2PaYhOaggf1E5xg82LTTEwxmLE4  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$PinnedSliceState$2PaYhOaggf1E5xg82LTTEwxmLE4 implements Predicate {
    public static final /* synthetic */ $$Lambda$PinnedSliceState$2PaYhOaggf1E5xg82LTTEwxmLE4 INSTANCE = new $$Lambda$PinnedSliceState$2PaYhOaggf1E5xg82LTTEwxmLE4();

    private /* synthetic */ $$Lambda$PinnedSliceState$2PaYhOaggf1E5xg82LTTEwxmLE4() {
    }

    @Override // java.util.function.Predicate
    public final boolean test(Object obj) {
        return PinnedSliceState.lambda$mergeSpecs$1((SliceSpec) obj);
    }
}
