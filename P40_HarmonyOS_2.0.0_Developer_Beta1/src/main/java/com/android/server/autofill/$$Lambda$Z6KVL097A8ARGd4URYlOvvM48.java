package com.android.server.autofill;

import java.util.function.BiConsumer;

/* renamed from: com.android.server.autofill.-$$Lambda$Z6K-VL097A8ARGd4URY-lOvvM48  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$Z6KVL097A8ARGd4URYlOvvM48 implements BiConsumer {
    public static final /* synthetic */ $$Lambda$Z6KVL097A8ARGd4URYlOvvM48 INSTANCE = new $$Lambda$Z6KVL097A8ARGd4URYlOvvM48();

    private /* synthetic */ $$Lambda$Z6KVL097A8ARGd4URYlOvvM48() {
    }

    @Override // java.util.function.BiConsumer
    public final void accept(Object obj, Object obj2) {
        ((AutofillManagerServiceImpl) obj).handleSessionSave((Session) obj2);
    }
}
