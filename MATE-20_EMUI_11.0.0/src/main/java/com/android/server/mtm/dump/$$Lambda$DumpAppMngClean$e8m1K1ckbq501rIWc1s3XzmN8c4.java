package com.android.server.mtm.dump;

import com.android.server.mtm.dump.DumpAppMngClean;
import java.util.function.Consumer;

/* renamed from: com.android.server.mtm.dump.-$$Lambda$DumpAppMngClean$e8m1K1ckbq501rIWc1s3XzmN8c4  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$DumpAppMngClean$e8m1K1ckbq501rIWc1s3XzmN8c4 implements Consumer {
    public static final /* synthetic */ $$Lambda$DumpAppMngClean$e8m1K1ckbq501rIWc1s3XzmN8c4 INSTANCE = new $$Lambda$DumpAppMngClean$e8m1K1ckbq501rIWc1s3XzmN8c4();

    private /* synthetic */ $$Lambda$DumpAppMngClean$e8m1K1ckbq501rIWc1s3XzmN8c4() {
    }

    @Override // java.util.function.Consumer
    public final void accept(Object obj) {
        DumpAppMngClean.dumpSMClean((DumpAppMngClean.Params) obj);
    }
}
