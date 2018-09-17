package com.android.server.pm;

import java.io.File;

final /* synthetic */ class -$Lambda$ilXJRPJlbkTpQIIJ5M-1Mp8u2aI implements Runnable {
    private final /* synthetic */ int -$f0;
    private final /* synthetic */ Object -$f1;
    private final /* synthetic */ Object -$f2;

    private final /* synthetic */ void $m$0() {
        ((ParallelPackageParser) this.-$f1).lambda$-com_android_server_pm_ParallelPackageParser_3701((File) this.-$f2, this.-$f0);
    }

    public /* synthetic */ -$Lambda$ilXJRPJlbkTpQIIJ5M-1Mp8u2aI(int i, Object obj, Object obj2) {
        this.-$f0 = i;
        this.-$f1 = obj;
        this.-$f2 = obj2;
    }

    public final void run() {
        $m$0();
    }
}
