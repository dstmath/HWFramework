package com.android.server.broadcastradio.hal2;

import android.hardware.broadcastradio.V2_0.Announcement;
import java.util.function.Function;

/* renamed from: com.android.server.broadcastradio.hal2.-$$Lambda$RadioModule$2$06udTLOtrtIC_bWC-WpXUXkuLVM  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$RadioModule$2$06udTLOtrtIC_bWCWpXUXkuLVM implements Function {
    public static final /* synthetic */ $$Lambda$RadioModule$2$06udTLOtrtIC_bWCWpXUXkuLVM INSTANCE = new $$Lambda$RadioModule$2$06udTLOtrtIC_bWCWpXUXkuLVM();

    private /* synthetic */ $$Lambda$RadioModule$2$06udTLOtrtIC_bWCWpXUXkuLVM() {
    }

    @Override // java.util.function.Function
    public final Object apply(Object obj) {
        return Convert.announcementFromHal((Announcement) obj);
    }
}
