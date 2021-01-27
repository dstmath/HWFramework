package com.android.server.wm;

import android.app.ActivityManagerInternal;
import android.content.ComponentName;
import android.view.IApplicationToken;
import com.android.internal.util.function.HexConsumer;

/* renamed from: com.android.server.wm.-$$Lambda$UB90fpYUkajpKCLGR93ZDlgDhyw  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$UB90fpYUkajpKCLGR93ZDlgDhyw implements HexConsumer {
    public static final /* synthetic */ $$Lambda$UB90fpYUkajpKCLGR93ZDlgDhyw INSTANCE = new $$Lambda$UB90fpYUkajpKCLGR93ZDlgDhyw();

    private /* synthetic */ $$Lambda$UB90fpYUkajpKCLGR93ZDlgDhyw() {
    }

    public final void accept(Object obj, Object obj2, Object obj3, Object obj4, Object obj5, Object obj6) {
        ((ActivityManagerInternal) obj).updateActivityUsageStats((ComponentName) obj2, ((Integer) obj3).intValue(), ((Integer) obj4).intValue(), (IApplicationToken.Stub) obj5, (ComponentName) obj6);
    }
}
