package com.android.server.mtm.dump;

import com.android.server.mtm.dump.DumpAppMngClean;
import java.util.function.Consumer;

/* renamed from: com.android.server.mtm.dump.-$$Lambda$DumpAppMngClean$Si5_TCKxoA2HllMjMY9I3RiRr8c  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$DumpAppMngClean$Si5_TCKxoA2HllMjMY9I3RiRr8c implements Consumer {
    public static final /* synthetic */ $$Lambda$DumpAppMngClean$Si5_TCKxoA2HllMjMY9I3RiRr8c INSTANCE = new $$Lambda$DumpAppMngClean$Si5_TCKxoA2HllMjMY9I3RiRr8c();

    private /* synthetic */ $$Lambda$DumpAppMngClean$Si5_TCKxoA2HllMjMY9I3RiRr8c() {
    }

    @Override // java.util.function.Consumer
    public final void accept(Object obj) {
        DumpAppMngClean.dumpProcStatus((DumpAppMngClean.Params) obj);
    }
}
