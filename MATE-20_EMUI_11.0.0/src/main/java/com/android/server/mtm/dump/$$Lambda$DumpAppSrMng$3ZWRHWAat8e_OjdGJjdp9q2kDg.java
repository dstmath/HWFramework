package com.android.server.mtm.dump;

import com.android.server.mtm.dump.DumpAppSrMng;
import java.util.function.Consumer;

/* renamed from: com.android.server.mtm.dump.-$$Lambda$DumpAppSrMng$3ZWRHWAat-8e_OjdGJjdp9q2kDg  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$DumpAppSrMng$3ZWRHWAat8e_OjdGJjdp9q2kDg implements Consumer {
    public static final /* synthetic */ $$Lambda$DumpAppSrMng$3ZWRHWAat8e_OjdGJjdp9q2kDg INSTANCE = new $$Lambda$DumpAppSrMng$3ZWRHWAat8e_OjdGJjdp9q2kDg();

    private /* synthetic */ $$Lambda$DumpAppSrMng$3ZWRHWAat8e_OjdGJjdp9q2kDg() {
    }

    @Override // java.util.function.Consumer
    public final void accept(Object obj) {
        DumpAppSrMng.dumpAppStart((DumpAppSrMng.Params) obj);
    }
}
