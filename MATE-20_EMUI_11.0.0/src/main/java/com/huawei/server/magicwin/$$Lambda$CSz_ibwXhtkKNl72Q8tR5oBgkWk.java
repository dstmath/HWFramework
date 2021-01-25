package com.huawei.server.magicwin;

import java.util.Map;
import java.util.function.Function;

/* renamed from: com.huawei.server.magicwin.-$$Lambda$CSz_ibwXhtkKNl72Q8tR5oBgkWk  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$CSz_ibwXhtkKNl72Q8tR5oBgkWk implements Function {
    public static final /* synthetic */ $$Lambda$CSz_ibwXhtkKNl72Q8tR5oBgkWk INSTANCE = new $$Lambda$CSz_ibwXhtkKNl72Q8tR5oBgkWk();

    private /* synthetic */ $$Lambda$CSz_ibwXhtkKNl72Q8tR5oBgkWk() {
    }

    @Override // java.util.function.Function
    public final Object apply(Object obj) {
        return (String) ((Map.Entry) obj).getKey();
    }
}
