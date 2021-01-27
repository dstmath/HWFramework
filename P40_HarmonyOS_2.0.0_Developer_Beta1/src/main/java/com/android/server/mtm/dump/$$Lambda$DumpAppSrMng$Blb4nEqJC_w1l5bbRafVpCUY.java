package com.android.server.mtm.dump;

import com.android.server.mtm.dump.DumpAppSrMng;
import java.util.function.Consumer;

/* renamed from: com.android.server.mtm.dump.-$$Lambda$DumpAppSrMng$Blb-4nEqJC_w1l5bbR--afVpCUY  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$DumpAppSrMng$Blb4nEqJC_w1l5bbRafVpCUY implements Consumer {
    public static final /* synthetic */ $$Lambda$DumpAppSrMng$Blb4nEqJC_w1l5bbRafVpCUY INSTANCE = new $$Lambda$DumpAppSrMng$Blb4nEqJC_w1l5bbRafVpCUY();

    private /* synthetic */ $$Lambda$DumpAppSrMng$Blb4nEqJC_w1l5bbRafVpCUY() {
    }

    @Override // java.util.function.Consumer
    public final void accept(Object obj) {
        DumpAppSrMng.dumpUpdateRules((DumpAppSrMng.Params) obj);
    }
}
