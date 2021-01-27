package com.android.server.wm;

import android.app.ActivityManagerInternal;
import java.util.ArrayList;
import java.util.function.BiConsumer;

/* renamed from: com.android.server.wm.-$$Lambda$j9nJq2XXOKyN4f0dfDaTjqmQRvg  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$j9nJq2XXOKyN4f0dfDaTjqmQRvg implements BiConsumer {
    public static final /* synthetic */ $$Lambda$j9nJq2XXOKyN4f0dfDaTjqmQRvg INSTANCE = new $$Lambda$j9nJq2XXOKyN4f0dfDaTjqmQRvg();

    private /* synthetic */ $$Lambda$j9nJq2XXOKyN4f0dfDaTjqmQRvg() {
    }

    @Override // java.util.function.BiConsumer
    public final void accept(Object obj, Object obj2) {
        ((ActivityManagerInternal) obj).killProcessesForRemovedTask((ArrayList) obj2);
    }
}
