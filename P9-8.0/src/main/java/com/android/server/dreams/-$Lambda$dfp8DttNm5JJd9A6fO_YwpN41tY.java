package com.android.server.dreams;

import android.content.ComponentName;
import android.os.Binder;
import android.os.PowerManager.WakeLock;

final /* synthetic */ class -$Lambda$dfp8DttNm5JJd9A6fO_YwpN41tY implements Runnable {
    private final /* synthetic */ boolean -$f0;
    private final /* synthetic */ boolean -$f1;
    private final /* synthetic */ int -$f2;
    private final /* synthetic */ Object -$f3;
    private final /* synthetic */ Object -$f4;
    private final /* synthetic */ Object -$f5;
    private final /* synthetic */ Object -$f6;

    private final /* synthetic */ void $m$0() {
        ((DreamManagerService) this.-$f3).lambda$-com_android_server_dreams_DreamManagerService_15775((Binder) this.-$f4, (ComponentName) this.-$f5, this.-$f0, this.-$f2, this.-$f1, (WakeLock) this.-$f6);
    }

    public /* synthetic */ -$Lambda$dfp8DttNm5JJd9A6fO_YwpN41tY(boolean z, boolean z2, int i, Object obj, Object obj2, Object obj3, Object obj4) {
        this.-$f0 = z;
        this.-$f1 = z2;
        this.-$f2 = i;
        this.-$f3 = obj;
        this.-$f4 = obj2;
        this.-$f5 = obj3;
        this.-$f6 = obj4;
    }

    public final void run() {
        $m$0();
    }
}
