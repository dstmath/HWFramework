package com.android.server.wm;

import android.app.ActivityManagerInternal;
import android.app.ProfilerInfo;
import android.content.pm.ActivityInfo;
import com.android.internal.util.function.QuintConsumer;

/* renamed from: com.android.server.wm.-$$Lambda$8ew6SY_v_7ex9pwFGDswbkGWuXc  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$8ew6SY_v_7ex9pwFGDswbkGWuXc implements QuintConsumer {
    public static final /* synthetic */ $$Lambda$8ew6SY_v_7ex9pwFGDswbkGWuXc INSTANCE = new $$Lambda$8ew6SY_v_7ex9pwFGDswbkGWuXc();

    private /* synthetic */ $$Lambda$8ew6SY_v_7ex9pwFGDswbkGWuXc() {
    }

    public final void accept(Object obj, Object obj2, Object obj3, Object obj4, Object obj5) {
        ((ActivityManagerInternal) obj).setDebugFlagsForStartingActivity((ActivityInfo) obj2, ((Integer) obj3).intValue(), (ProfilerInfo) obj4, (WindowManagerGlobalLock) obj5);
    }
}
