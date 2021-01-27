package com.android.server.mtm.dump;

import com.android.server.mtm.dump.DumpAppMngClean;
import java.util.function.Consumer;

/* renamed from: com.android.server.mtm.dump.-$$Lambda$DumpAppMngClean$f1_NYVqBdmS4PdHLMZlxpIsrCX8  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$DumpAppMngClean$f1_NYVqBdmS4PdHLMZlxpIsrCX8 implements Consumer {
    public static final /* synthetic */ $$Lambda$DumpAppMngClean$f1_NYVqBdmS4PdHLMZlxpIsrCX8 INSTANCE = new $$Lambda$DumpAppMngClean$f1_NYVqBdmS4PdHLMZlxpIsrCX8();

    private /* synthetic */ $$Lambda$DumpAppMngClean$f1_NYVqBdmS4PdHLMZlxpIsrCX8() {
    }

    @Override // java.util.function.Consumer
    public final void accept(Object obj) {
        DumpAppMngClean.dumpHistory((DumpAppMngClean.Params) obj);
    }
}
