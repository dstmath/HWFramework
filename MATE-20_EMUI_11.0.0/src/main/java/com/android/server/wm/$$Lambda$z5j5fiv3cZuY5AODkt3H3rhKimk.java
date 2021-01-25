package com.android.server.wm;

import android.app.ActivityManagerInternal;
import android.content.ComponentName;
import android.content.Intent;
import com.android.internal.util.function.QuadConsumer;

/* renamed from: com.android.server.wm.-$$Lambda$z5j5fiv3cZuY5AODkt3H3rhKimk  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$z5j5fiv3cZuY5AODkt3H3rhKimk implements QuadConsumer {
    public static final /* synthetic */ $$Lambda$z5j5fiv3cZuY5AODkt3H3rhKimk INSTANCE = new $$Lambda$z5j5fiv3cZuY5AODkt3H3rhKimk();

    private /* synthetic */ $$Lambda$z5j5fiv3cZuY5AODkt3H3rhKimk() {
    }

    public final void accept(Object obj, Object obj2, Object obj3, Object obj4) {
        ((ActivityManagerInternal) obj).cleanUpServices(((Integer) obj2).intValue(), (ComponentName) obj3, (Intent) obj4);
    }
}
