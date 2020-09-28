package com.android.internal.util;

import android.content.ComponentName;
import java.util.Objects;
import java.util.function.Predicate;

/* renamed from: com.android.internal.util.-$$Lambda$eRa1rlfDk6Og2yFeXGHqUGPzRF0  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$eRa1rlfDk6Og2yFeXGHqUGPzRF0 implements Predicate {
    public static final /* synthetic */ $$Lambda$eRa1rlfDk6Og2yFeXGHqUGPzRF0 INSTANCE = new $$Lambda$eRa1rlfDk6Og2yFeXGHqUGPzRF0();

    private /* synthetic */ $$Lambda$eRa1rlfDk6Og2yFeXGHqUGPzRF0() {
    }

    @Override // java.util.function.Predicate
    public final boolean test(Object obj) {
        return Objects.nonNull((ComponentName.WithComponentName) obj);
    }
}
