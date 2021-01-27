package com.android.server.mtm.dump;

import com.android.server.mtm.dump.DumpAppMngClean;
import java.util.function.Consumer;

/* renamed from: com.android.server.mtm.dump.-$$Lambda$DumpAppMngClean$y9Uikx6p17KgNI5ky-DqDdmcPo4  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$DumpAppMngClean$y9Uikx6p17KgNI5kyDqDdmcPo4 implements Consumer {
    public static final /* synthetic */ $$Lambda$DumpAppMngClean$y9Uikx6p17KgNI5kyDqDdmcPo4 INSTANCE = new $$Lambda$DumpAppMngClean$y9Uikx6p17KgNI5kyDqDdmcPo4();

    private /* synthetic */ $$Lambda$DumpAppMngClean$y9Uikx6p17KgNI5kyDqDdmcPo4() {
    }

    @Override // java.util.function.Consumer
    public final void accept(Object obj) {
        DumpAppMngClean.dumpPGClean((DumpAppMngClean.Params) obj);
    }
}
