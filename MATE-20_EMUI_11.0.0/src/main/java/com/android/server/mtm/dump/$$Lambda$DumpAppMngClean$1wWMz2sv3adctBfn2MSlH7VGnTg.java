package com.android.server.mtm.dump;

import com.android.server.mtm.dump.DumpAppMngClean;
import java.util.function.Consumer;

/* renamed from: com.android.server.mtm.dump.-$$Lambda$DumpAppMngClean$1wWMz2sv3adctBfn2MSlH7VGnTg  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$DumpAppMngClean$1wWMz2sv3adctBfn2MSlH7VGnTg implements Consumer {
    public static final /* synthetic */ $$Lambda$DumpAppMngClean$1wWMz2sv3adctBfn2MSlH7VGnTg INSTANCE = new $$Lambda$DumpAppMngClean$1wWMz2sv3adctBfn2MSlH7VGnTg();

    private /* synthetic */ $$Lambda$DumpAppMngClean$1wWMz2sv3adctBfn2MSlH7VGnTg() {
    }

    @Override // java.util.function.Consumer
    public final void accept(Object obj) {
        DumpAppMngClean.dumpThermalClean((DumpAppMngClean.Params) obj);
    }
}
