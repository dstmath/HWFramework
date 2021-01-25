package com.android.server.wm;

import android.app.ActivityManagerInternal;
import com.android.internal.util.function.QuadConsumer;

/* renamed from: com.android.server.wm.-$$Lambda$1636dquQO0UvkFayOGf_gceB4iw  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$1636dquQO0UvkFayOGf_gceB4iw implements QuadConsumer {
    public static final /* synthetic */ $$Lambda$1636dquQO0UvkFayOGf_gceB4iw INSTANCE = new $$Lambda$1636dquQO0UvkFayOGf_gceB4iw();

    private /* synthetic */ $$Lambda$1636dquQO0UvkFayOGf_gceB4iw() {
    }

    public final void accept(Object obj, Object obj2, Object obj3, Object obj4) {
        ((ActivityManagerInternal) obj).updateForegroundTimeIfOnBattery((String) obj2, ((Integer) obj3).intValue(), ((Long) obj4).longValue());
    }
}
