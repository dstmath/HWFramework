package com.android.internal.os;

import android.os.SystemProperties;
import java.util.function.Supplier;

/* renamed from: com.android.internal.os.-$$Lambda$RuntimeInit$ep4ioD9YINkHI5Q1wZ0N_7VFAOg  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$RuntimeInit$ep4ioD9YINkHI5Q1wZ0N_7VFAOg implements Supplier {
    public static final /* synthetic */ $$Lambda$RuntimeInit$ep4ioD9YINkHI5Q1wZ0N_7VFAOg INSTANCE = new $$Lambda$RuntimeInit$ep4ioD9YINkHI5Q1wZ0N_7VFAOg();

    private /* synthetic */ $$Lambda$RuntimeInit$ep4ioD9YINkHI5Q1wZ0N_7VFAOg() {
    }

    @Override // java.util.function.Supplier
    public final Object get() {
        return SystemProperties.get("persist.sys.timezone");
    }
}
