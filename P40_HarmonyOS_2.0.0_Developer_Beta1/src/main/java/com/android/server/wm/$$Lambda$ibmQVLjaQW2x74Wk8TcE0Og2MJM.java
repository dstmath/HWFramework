package com.android.server.wm;

import android.app.ActivityManagerInternal;
import com.android.internal.util.function.TriConsumer;

/* renamed from: com.android.server.wm.-$$Lambda$ibmQVLjaQW2x74Wk8TcE0Og2MJM  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$ibmQVLjaQW2x74Wk8TcE0Og2MJM implements TriConsumer {
    public static final /* synthetic */ $$Lambda$ibmQVLjaQW2x74Wk8TcE0Og2MJM INSTANCE = new $$Lambda$ibmQVLjaQW2x74Wk8TcE0Og2MJM();

    private /* synthetic */ $$Lambda$ibmQVLjaQW2x74Wk8TcE0Og2MJM() {
    }

    public final void accept(Object obj, Object obj2, Object obj3) {
        ((ActivityManagerInternal) obj).killAllBackgroundProcessesExcept(((Integer) obj2).intValue(), ((Integer) obj3).intValue());
    }
}
