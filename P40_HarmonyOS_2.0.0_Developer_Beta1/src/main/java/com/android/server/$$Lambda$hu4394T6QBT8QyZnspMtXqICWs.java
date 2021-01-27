package com.android.server;

import android.location.IGnssStatusListener;
import android.os.IBinder;
import java.util.function.Function;

/* renamed from: com.android.server.-$$Lambda$hu439-4T6QBT8QyZnspMtXqICWs  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$hu4394T6QBT8QyZnspMtXqICWs implements Function {
    public static final /* synthetic */ $$Lambda$hu4394T6QBT8QyZnspMtXqICWs INSTANCE = new $$Lambda$hu4394T6QBT8QyZnspMtXqICWs();

    private /* synthetic */ $$Lambda$hu4394T6QBT8QyZnspMtXqICWs() {
    }

    @Override // java.util.function.Function
    public final Object apply(Object obj) {
        return IGnssStatusListener.Stub.asInterface((IBinder) obj);
    }
}
