package com.android.server.mtm.dump;

import com.android.server.mtm.dump.DumpAppSrMng;
import java.util.function.Consumer;

/* renamed from: com.android.server.mtm.dump.-$$Lambda$DumpAppSrMng$aN0pZowdDz5s63BwOlmEY-Uv0nk  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$DumpAppSrMng$aN0pZowdDz5s63BwOlmEYUv0nk implements Consumer {
    public static final /* synthetic */ $$Lambda$DumpAppSrMng$aN0pZowdDz5s63BwOlmEYUv0nk INSTANCE = new $$Lambda$DumpAppSrMng$aN0pZowdDz5s63BwOlmEYUv0nk();

    private /* synthetic */ $$Lambda$DumpAppSrMng$aN0pZowdDz5s63BwOlmEYUv0nk() {
    }

    @Override // java.util.function.Consumer
    public final void accept(Object obj) {
        DumpAppSrMng.dumpDumpRules((DumpAppSrMng.Params) obj);
    }
}
