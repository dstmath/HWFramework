package com.android.server.wm;

import android.app.ActivityManagerInternal;
import android.content.pm.ApplicationInfo;
import java.util.function.BiConsumer;

/* renamed from: com.android.server.wm.-$$Lambda$rk4xJMbKRM1lLgjaIamkhpdvq98  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$rk4xJMbKRM1lLgjaIamkhpdvq98 implements BiConsumer {
    public static final /* synthetic */ $$Lambda$rk4xJMbKRM1lLgjaIamkhpdvq98 INSTANCE = new $$Lambda$rk4xJMbKRM1lLgjaIamkhpdvq98();

    private /* synthetic */ $$Lambda$rk4xJMbKRM1lLgjaIamkhpdvq98() {
    }

    @Override // java.util.function.BiConsumer
    public final void accept(Object obj, Object obj2) {
        ((ActivityManagerInternal) obj).preloadApp((ApplicationInfo) obj2);
    }
}
