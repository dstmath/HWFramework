package com.android.server.wm;

import java.util.function.Predicate;

/* renamed from: com.android.server.wm.-$$Lambda$DisplayContent$05CtqlkxQvjLanO8D5BmaCdILKQ  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$DisplayContent$05CtqlkxQvjLanO8D5BmaCdILKQ implements Predicate {
    public static final /* synthetic */ $$Lambda$DisplayContent$05CtqlkxQvjLanO8D5BmaCdILKQ INSTANCE = new $$Lambda$DisplayContent$05CtqlkxQvjLanO8D5BmaCdILKQ();

    private /* synthetic */ $$Lambda$DisplayContent$05CtqlkxQvjLanO8D5BmaCdILKQ() {
    }

    @Override // java.util.function.Predicate
    public final boolean test(Object obj) {
        return ((WindowState) obj).mSeamlesslyRotated;
    }
}
