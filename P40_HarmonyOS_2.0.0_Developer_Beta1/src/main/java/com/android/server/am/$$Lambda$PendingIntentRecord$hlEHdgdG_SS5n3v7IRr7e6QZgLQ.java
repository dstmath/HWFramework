package com.android.server.am;

import java.util.function.Consumer;

/* renamed from: com.android.server.am.-$$Lambda$PendingIntentRecord$hlEHdgdG_SS5n3v7IRr7e6QZgLQ  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$PendingIntentRecord$hlEHdgdG_SS5n3v7IRr7e6QZgLQ implements Consumer {
    public static final /* synthetic */ $$Lambda$PendingIntentRecord$hlEHdgdG_SS5n3v7IRr7e6QZgLQ INSTANCE = new $$Lambda$PendingIntentRecord$hlEHdgdG_SS5n3v7IRr7e6QZgLQ();

    private /* synthetic */ $$Lambda$PendingIntentRecord$hlEHdgdG_SS5n3v7IRr7e6QZgLQ() {
    }

    @Override // java.util.function.Consumer
    public final void accept(Object obj) {
        ((PendingIntentRecord) obj).completeFinalize();
    }
}
