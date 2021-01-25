package com.android.server.mtm.dump;

import com.android.server.mtm.dump.DumpAppMngClean;
import java.util.function.Consumer;

/* renamed from: com.android.server.mtm.dump.-$$Lambda$DumpAppMngClean$r5dyE1sFOC6cAkYBgSOTkjHQvMw  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$DumpAppMngClean$r5dyE1sFOC6cAkYBgSOTkjHQvMw implements Consumer {
    public static final /* synthetic */ $$Lambda$DumpAppMngClean$r5dyE1sFOC6cAkYBgSOTkjHQvMw INSTANCE = new $$Lambda$DumpAppMngClean$r5dyE1sFOC6cAkYBgSOTkjHQvMw();

    private /* synthetic */ $$Lambda$DumpAppMngClean$r5dyE1sFOC6cAkYBgSOTkjHQvMw() {
    }

    @Override // java.util.function.Consumer
    public final void accept(Object obj) {
        DumpAppMngClean.getSMCleanList((DumpAppMngClean.Params) obj);
    }
}
