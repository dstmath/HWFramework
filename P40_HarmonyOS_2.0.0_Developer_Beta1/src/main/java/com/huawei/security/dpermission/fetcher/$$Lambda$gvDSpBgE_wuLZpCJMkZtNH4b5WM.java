package com.huawei.security.dpermission.fetcher;

import com.huawei.security.dpermission.model.PackageBo;
import java.util.function.Function;

/* renamed from: com.huawei.security.dpermission.fetcher.-$$Lambda$gvDSpBgE_wuLZpCJMkZtNH4b5WM  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$gvDSpBgE_wuLZpCJMkZtNH4b5WM implements Function {
    public static final /* synthetic */ $$Lambda$gvDSpBgE_wuLZpCJMkZtNH4b5WM INSTANCE = new $$Lambda$gvDSpBgE_wuLZpCJMkZtNH4b5WM();

    private /* synthetic */ $$Lambda$gvDSpBgE_wuLZpCJMkZtNH4b5WM() {
    }

    @Override // java.util.function.Function
    public final Object apply(Object obj) {
        return ((PackageBo) obj).getPermissions();
    }
}
