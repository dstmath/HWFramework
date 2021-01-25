package com.android.server.inputmethod;

import com.android.server.inputmethod.MultiClientInputMethodManagerService;
import java.util.function.BiConsumer;

/* renamed from: com.android.server.inputmethod.-$$Lambda$WSIWOuAiWdU2M_nAMbBdbAY3JqQ  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$WSIWOuAiWdU2M_nAMbBdbAY3JqQ implements BiConsumer {
    public static final /* synthetic */ $$Lambda$WSIWOuAiWdU2M_nAMbBdbAY3JqQ INSTANCE = new $$Lambda$WSIWOuAiWdU2M_nAMbBdbAY3JqQ();

    private /* synthetic */ $$Lambda$WSIWOuAiWdU2M_nAMbBdbAY3JqQ() {
    }

    @Override // java.util.function.BiConsumer
    public final void accept(Object obj, Object obj2) {
        ((MultiClientInputMethodManagerService.OnWorkerThreadCallback) obj).onStartUser(((Integer) obj2).intValue());
    }
}
