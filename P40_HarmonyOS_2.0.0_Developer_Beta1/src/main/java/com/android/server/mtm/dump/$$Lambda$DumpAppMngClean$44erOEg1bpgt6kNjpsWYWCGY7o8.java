package com.android.server.mtm.dump;

import com.android.server.mtm.dump.DumpAppMngClean;
import java.util.function.Consumer;

/* renamed from: com.android.server.mtm.dump.-$$Lambda$DumpAppMngClean$44erOEg1bpgt6kNjpsWYWCGY7o8  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$DumpAppMngClean$44erOEg1bpgt6kNjpsWYWCGY7o8 implements Consumer {
    public static final /* synthetic */ $$Lambda$DumpAppMngClean$44erOEg1bpgt6kNjpsWYWCGY7o8 INSTANCE = new $$Lambda$DumpAppMngClean$44erOEg1bpgt6kNjpsWYWCGY7o8();

    private /* synthetic */ $$Lambda$DumpAppMngClean$44erOEg1bpgt6kNjpsWYWCGY7o8() {
    }

    @Override // java.util.function.Consumer
    public final void accept(Object obj) {
        DumpAppMngClean.dumpCrashClean((DumpAppMngClean.Params) obj);
    }
}
