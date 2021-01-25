package com.android.server.inputmethod;

import com.android.server.inputmethod.MultiClientInputMethodManagerService;
import java.util.function.BiConsumer;

/* renamed from: com.android.server.inputmethod.-$$Lambda$DAfcEi3wsfG3_Hj9sOIZhg6DC1c  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$DAfcEi3wsfG3_Hj9sOIZhg6DC1c implements BiConsumer {
    public static final /* synthetic */ $$Lambda$DAfcEi3wsfG3_Hj9sOIZhg6DC1c INSTANCE = new $$Lambda$DAfcEi3wsfG3_Hj9sOIZhg6DC1c();

    private /* synthetic */ $$Lambda$DAfcEi3wsfG3_Hj9sOIZhg6DC1c() {
    }

    @Override // java.util.function.BiConsumer
    public final void accept(Object obj, Object obj2) {
        ((MultiClientInputMethodManagerService.OnWorkerThreadCallback) obj).onUnlockUser(((Integer) obj2).intValue());
    }
}
