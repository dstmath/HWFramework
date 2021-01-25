package com.android.server.wm;

import java.util.function.Function;

/* renamed from: com.android.server.wm.-$$Lambda$HwActivityStarterEx$eDBLZR9SLmQ0NA7ufbP9KcllCPY  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$HwActivityStarterEx$eDBLZR9SLmQ0NA7ufbP9KcllCPY implements Function {
    public static final /* synthetic */ $$Lambda$HwActivityStarterEx$eDBLZR9SLmQ0NA7ufbP9KcllCPY INSTANCE = new $$Lambda$HwActivityStarterEx$eDBLZR9SLmQ0NA7ufbP9KcllCPY();

    private /* synthetic */ $$Lambda$HwActivityStarterEx$eDBLZR9SLmQ0NA7ufbP9KcllCPY() {
    }

    @Override // java.util.function.Function
    public final Object apply(Object obj) {
        return ((ActivityStack) obj).getTopActivity();
    }
}
