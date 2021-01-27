package com.android.server.mtm.dump;

import com.android.server.mtm.dump.DumpAppMngClean;
import java.util.function.Consumer;

/* renamed from: com.android.server.mtm.dump.-$$Lambda$DumpAppMngClean$tOb1n4OVQLZKFVyT1G2msFkLPFk  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$DumpAppMngClean$tOb1n4OVQLZKFVyT1G2msFkLPFk implements Consumer {
    public static final /* synthetic */ $$Lambda$DumpAppMngClean$tOb1n4OVQLZKFVyT1G2msFkLPFk INSTANCE = new $$Lambda$DumpAppMngClean$tOb1n4OVQLZKFVyT1G2msFkLPFk();

    private /* synthetic */ $$Lambda$DumpAppMngClean$tOb1n4OVQLZKFVyT1G2msFkLPFk() {
    }

    @Override // java.util.function.Consumer
    public final void accept(Object obj) {
        DumpAppMngClean.dumpAppStatus((DumpAppMngClean.Params) obj);
    }
}
