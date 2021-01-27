package com.android.server.wm;

import android.app.ActivityManagerInternal;
import android.content.ComponentName;
import android.content.pm.ApplicationInfo;
import com.android.internal.util.function.HexConsumer;

/* renamed from: com.android.server.wm.-$$Lambda$3W4Y_XVQUddVKzLjibuHW7h0R1g  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$3W4Y_XVQUddVKzLjibuHW7h0R1g implements HexConsumer {
    public static final /* synthetic */ $$Lambda$3W4Y_XVQUddVKzLjibuHW7h0R1g INSTANCE = new $$Lambda$3W4Y_XVQUddVKzLjibuHW7h0R1g();

    private /* synthetic */ $$Lambda$3W4Y_XVQUddVKzLjibuHW7h0R1g() {
    }

    public final void accept(Object obj, Object obj2, Object obj3, Object obj4, Object obj5, Object obj6) {
        ((ActivityManagerInternal) obj).startProcess((String) obj2, (ApplicationInfo) obj3, ((Boolean) obj4).booleanValue(), (String) obj5, (ComponentName) obj6);
    }
}
