package com.android.server.print;

import java.util.function.Consumer;

/* renamed from: com.android.server.print.-$$Lambda$nSUd_Gl040MrfHGSQHSjunnnXaY  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$nSUd_Gl040MrfHGSQHSjunnnXaY implements Consumer {
    public static final /* synthetic */ $$Lambda$nSUd_Gl040MrfHGSQHSjunnnXaY INSTANCE = new $$Lambda$nSUd_Gl040MrfHGSQHSjunnnXaY();

    private /* synthetic */ $$Lambda$nSUd_Gl040MrfHGSQHSjunnnXaY() {
    }

    @Override // java.util.function.Consumer
    public final void accept(Object obj) {
        ((RemotePrintService) obj).createPrinterDiscoverySession();
    }
}
