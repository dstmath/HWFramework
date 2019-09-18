package com.android.server.mtm.dump;

import com.android.server.mtm.dump.DumpAppMngClean;
import java.util.function.Consumer;

/* renamed from: com.android.server.mtm.dump.-$$Lambda$DumpAppMngClean$KfhossEyWguRgocnpArCBogtq84  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$DumpAppMngClean$KfhossEyWguRgocnpArCBogtq84 implements Consumer {
    public static final /* synthetic */ $$Lambda$DumpAppMngClean$KfhossEyWguRgocnpArCBogtq84 INSTANCE = new $$Lambda$DumpAppMngClean$KfhossEyWguRgocnpArCBogtq84();

    private /* synthetic */ $$Lambda$DumpAppMngClean$KfhossEyWguRgocnpArCBogtq84() {
    }

    public final void accept(Object obj) {
        DumpAppMngClean.clean(((DumpAppMngClean.Params) obj).context, ((DumpAppMngClean.Params) obj).pw, ((DumpAppMngClean.Params) obj).args);
    }
}
