package com.android.server.inputmethod;

import com.android.server.inputmethod.MultiClientInputMethodManagerService;
import java.util.function.BiConsumer;

/* renamed from: com.android.server.inputmethod.-$$Lambda$RXzNxmh28D9UoOHbnsbdOPsUo9Y  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$RXzNxmh28D9UoOHbnsbdOPsUo9Y implements BiConsumer {
    public static final /* synthetic */ $$Lambda$RXzNxmh28D9UoOHbnsbdOPsUo9Y INSTANCE = new $$Lambda$RXzNxmh28D9UoOHbnsbdOPsUo9Y();

    private /* synthetic */ $$Lambda$RXzNxmh28D9UoOHbnsbdOPsUo9Y() {
    }

    @Override // java.util.function.BiConsumer
    public final void accept(Object obj, Object obj2) {
        ((MultiClientInputMethodManagerService.OnWorkerThreadCallback) obj).onStopUser(((Integer) obj2).intValue());
    }
}
