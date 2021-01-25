package com.android.server.mtm.dump;

import com.android.server.mtm.dump.DumpAppMngClean;
import java.util.function.Consumer;

/* renamed from: com.android.server.mtm.dump.-$$Lambda$DumpAppMngClean$hDNAtW__EH7um6U6VCOq30-MXPM  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$DumpAppMngClean$hDNAtW__EH7um6U6VCOq30MXPM implements Consumer {
    public static final /* synthetic */ $$Lambda$DumpAppMngClean$hDNAtW__EH7um6U6VCOq30MXPM INSTANCE = new $$Lambda$DumpAppMngClean$hDNAtW__EH7um6U6VCOq30MXPM();

    private /* synthetic */ $$Lambda$DumpAppMngClean$hDNAtW__EH7um6U6VCOq30MXPM() {
    }

    @Override // java.util.function.Consumer
    public final void accept(Object obj) {
        DumpAppMngClean.dumpPackage((DumpAppMngClean.Params) obj);
    }
}
