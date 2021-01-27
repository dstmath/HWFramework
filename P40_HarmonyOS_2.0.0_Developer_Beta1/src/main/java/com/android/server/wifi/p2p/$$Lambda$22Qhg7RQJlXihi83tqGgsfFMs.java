package com.android.server.wifi.p2p;

import com.android.server.wifi.util.NativeUtil;
import java.util.function.Function;

/* renamed from: com.android.server.wifi.p2p.-$$Lambda$22Qhg7RQJlX-ihi83tqGgsfF-Ms  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$22Qhg7RQJlXihi83tqGgsfFMs implements Function {
    public static final /* synthetic */ $$Lambda$22Qhg7RQJlXihi83tqGgsfFMs INSTANCE = new $$Lambda$22Qhg7RQJlXihi83tqGgsfFMs();

    private /* synthetic */ $$Lambda$22Qhg7RQJlXihi83tqGgsfFMs() {
    }

    @Override // java.util.function.Function
    public final Object apply(Object obj) {
        return NativeUtil.macAddressFromByteArray((byte[]) obj);
    }
}
