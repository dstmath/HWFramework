package com.android.server.mtm.dump;

import com.android.server.mtm.dump.DumpAppMngClean;
import java.util.function.Consumer;

/* renamed from: com.android.server.mtm.dump.-$$Lambda$DumpAppMngClean$0BSnoGLDii96rEbhd748D3onels  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$DumpAppMngClean$0BSnoGLDii96rEbhd748D3onels implements Consumer {
    public static final /* synthetic */ $$Lambda$DumpAppMngClean$0BSnoGLDii96rEbhd748D3onels INSTANCE = new $$Lambda$DumpAppMngClean$0BSnoGLDii96rEbhd748D3onels();

    private /* synthetic */ $$Lambda$DumpAppMngClean$0BSnoGLDii96rEbhd748D3onels() {
    }

    public final void accept(Object obj) {
        DumpAppMngClean.dumpThermalClean((DumpAppMngClean.Params) obj);
    }
}
