package com.android.server.inputmethod;

import com.android.server.inputmethod.MultiClientInputMethodManagerService;
import java.util.function.BiConsumer;

/* renamed from: com.android.server.inputmethod.-$$Lambda$Jz_nOUKMszfA8aOqhALo1zh2yrY  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$Jz_nOUKMszfA8aOqhALo1zh2yrY implements BiConsumer {
    public static final /* synthetic */ $$Lambda$Jz_nOUKMszfA8aOqhALo1zh2yrY INSTANCE = new $$Lambda$Jz_nOUKMszfA8aOqhALo1zh2yrY();

    private /* synthetic */ $$Lambda$Jz_nOUKMszfA8aOqhALo1zh2yrY() {
    }

    @Override // java.util.function.BiConsumer
    public final void accept(Object obj, Object obj2) {
        ((MultiClientInputMethodManagerService.OnWorkerThreadCallback) obj).onBootPhase(((Integer) obj2).intValue());
    }
}
