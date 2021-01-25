package com.android.server.broadcastradio.hal2;

import android.hardware.broadcastradio.V2_0.ProgramInfo;
import java.util.function.Function;

/* renamed from: com.android.server.broadcastradio.hal2.-$$Lambda$Convert$P20z6nVni7Z0919gQ-M-2S9sxbM  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$Convert$P20z6nVni7Z0919gQM2S9sxbM implements Function {
    public static final /* synthetic */ $$Lambda$Convert$P20z6nVni7Z0919gQM2S9sxbM INSTANCE = new $$Lambda$Convert$P20z6nVni7Z0919gQM2S9sxbM();

    private /* synthetic */ $$Lambda$Convert$P20z6nVni7Z0919gQM2S9sxbM() {
    }

    @Override // java.util.function.Function
    public final Object apply(Object obj) {
        return Convert.programInfoFromHal((ProgramInfo) obj);
    }
}
