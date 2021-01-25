package com.android.server.mtm.dump;

import com.android.server.mtm.dump.DumpAppMngClean;
import java.util.function.Consumer;

/* renamed from: com.android.server.mtm.dump.-$$Lambda$DumpAppMngClean$cbwwaUEoxPYfIb5JYcWH9zk8MaE  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$DumpAppMngClean$cbwwaUEoxPYfIb5JYcWH9zk8MaE implements Consumer {
    public static final /* synthetic */ $$Lambda$DumpAppMngClean$cbwwaUEoxPYfIb5JYcWH9zk8MaE INSTANCE = new $$Lambda$DumpAppMngClean$cbwwaUEoxPYfIb5JYcWH9zk8MaE();

    private /* synthetic */ $$Lambda$DumpAppMngClean$cbwwaUEoxPYfIb5JYcWH9zk8MaE() {
    }

    @Override // java.util.function.Consumer
    public final void accept(Object obj) {
        DumpAppMngClean.dumpPackageList((DumpAppMngClean.Params) obj);
    }
}
