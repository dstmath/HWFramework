package com.android.server.hidata.wavemapping.entity;

import java.util.function.BiFunction;

/* renamed from: com.android.server.hidata.wavemapping.entity.-$$Lambda$UNDzBq9YY5vB0tVYGv7BxBnAJ8Y  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$UNDzBq9YY5vB0tVYGv7BxBnAJ8Y implements BiFunction {
    public static final /* synthetic */ $$Lambda$UNDzBq9YY5vB0tVYGv7BxBnAJ8Y INSTANCE = new $$Lambda$UNDzBq9YY5vB0tVYGv7BxBnAJ8Y();

    private /* synthetic */ $$Lambda$UNDzBq9YY5vB0tVYGv7BxBnAJ8Y() {
    }

    @Override // java.util.function.BiFunction
    public final Object apply(Object obj, Object obj2) {
        return Long.valueOf(Long.sum(((Long) obj).longValue(), ((Long) obj2).longValue()));
    }
}
