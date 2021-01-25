package com.android.server.am;

import java.io.File;
import java.util.function.ToLongFunction;

/* renamed from: com.android.server.am.-$$Lambda$yk1Ms9fVlF6PvprMwF2rru-dw4Q  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$yk1Ms9fVlF6PvprMwF2rrudw4Q implements ToLongFunction {
    public static final /* synthetic */ $$Lambda$yk1Ms9fVlF6PvprMwF2rrudw4Q INSTANCE = new $$Lambda$yk1Ms9fVlF6PvprMwF2rrudw4Q();

    private /* synthetic */ $$Lambda$yk1Ms9fVlF6PvprMwF2rrudw4Q() {
    }

    @Override // java.util.function.ToLongFunction
    public final long applyAsLong(Object obj) {
        return ((File) obj).lastModified();
    }
}
