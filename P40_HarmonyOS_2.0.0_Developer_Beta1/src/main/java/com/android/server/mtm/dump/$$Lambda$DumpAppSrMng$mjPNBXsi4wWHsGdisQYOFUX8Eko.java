package com.android.server.mtm.dump;

import com.android.server.mtm.dump.DumpAppSrMng;
import java.util.function.Consumer;

/* renamed from: com.android.server.mtm.dump.-$$Lambda$DumpAppSrMng$mjPNBXsi4wWHsGdisQYOFUX8Eko  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$DumpAppSrMng$mjPNBXsi4wWHsGdisQYOFUX8Eko implements Consumer {
    public static final /* synthetic */ $$Lambda$DumpAppSrMng$mjPNBXsi4wWHsGdisQYOFUX8Eko INSTANCE = new $$Lambda$DumpAppSrMng$mjPNBXsi4wWHsGdisQYOFUX8Eko();

    private /* synthetic */ $$Lambda$DumpAppSrMng$mjPNBXsi4wWHsGdisQYOFUX8Eko() {
    }

    @Override // java.util.function.Consumer
    public final void accept(Object obj) {
        DumpAppSrMng.dumpList((DumpAppSrMng.Params) obj);
    }
}
