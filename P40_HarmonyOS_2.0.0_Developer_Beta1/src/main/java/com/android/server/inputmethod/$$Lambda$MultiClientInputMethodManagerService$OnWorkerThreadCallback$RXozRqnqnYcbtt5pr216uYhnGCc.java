package com.android.server.inputmethod;

import com.android.server.inputmethod.MultiClientInputMethodManagerService;
import java.util.function.BiConsumer;

/* renamed from: com.android.server.inputmethod.-$$Lambda$MultiClientInputMethodManagerService$OnWorkerThreadCallback$RXozRqnqnYcbtt5pr216uYhnGCc  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$MultiClientInputMethodManagerService$OnWorkerThreadCallback$RXozRqnqnYcbtt5pr216uYhnGCc implements BiConsumer {
    public static final /* synthetic */ $$Lambda$MultiClientInputMethodManagerService$OnWorkerThreadCallback$RXozRqnqnYcbtt5pr216uYhnGCc INSTANCE = new $$Lambda$MultiClientInputMethodManagerService$OnWorkerThreadCallback$RXozRqnqnYcbtt5pr216uYhnGCc();

    private /* synthetic */ $$Lambda$MultiClientInputMethodManagerService$OnWorkerThreadCallback$RXozRqnqnYcbtt5pr216uYhnGCc() {
    }

    @Override // java.util.function.BiConsumer
    public final void accept(Object obj, Object obj2) {
        ((MultiClientInputMethodManagerService.OnWorkerThreadCallback) obj).tryBindInputMethodService(((Integer) obj2).intValue());
    }
}
