package com.android.server.connectivity.usbp2p;

import com.android.server.connectivity.usbp2p.UsbP2pManager;
import java.util.function.Predicate;

/* renamed from: com.android.server.connectivity.usbp2p.-$$Lambda$9y4rklj21DjMtYk4DIqT2aD83rE  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$9y4rklj21DjMtYk4DIqT2aD83rE implements Predicate {
    public static final /* synthetic */ $$Lambda$9y4rklj21DjMtYk4DIqT2aD83rE INSTANCE = new $$Lambda$9y4rklj21DjMtYk4DIqT2aD83rE();

    private /* synthetic */ $$Lambda$9y4rklj21DjMtYk4DIqT2aD83rE() {
    }

    @Override // java.util.function.Predicate
    public final boolean test(Object obj) {
        return ((UsbP2pManager.UsbP2pRequestInfo) obj).isRequest();
    }
}
