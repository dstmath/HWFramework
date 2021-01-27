package com.android.server.connectivity.usbp2p;

import com.android.server.connectivity.usbp2p.UsbP2pManager;
import java.util.function.Consumer;

/* renamed from: com.android.server.connectivity.usbp2p.-$$Lambda$l1gfiXhlZxt682CCAsd2Ts5B__M  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$l1gfiXhlZxt682CCAsd2Ts5B__M implements Consumer {
    public static final /* synthetic */ $$Lambda$l1gfiXhlZxt682CCAsd2Ts5B__M INSTANCE = new $$Lambda$l1gfiXhlZxt682CCAsd2Ts5B__M();

    private /* synthetic */ $$Lambda$l1gfiXhlZxt682CCAsd2Ts5B__M() {
    }

    @Override // java.util.function.Consumer
    public final void accept(Object obj) {
        ((UsbP2pManager.UsbP2pRequestInfo) obj).unlinkDeathRecipient();
    }
}
