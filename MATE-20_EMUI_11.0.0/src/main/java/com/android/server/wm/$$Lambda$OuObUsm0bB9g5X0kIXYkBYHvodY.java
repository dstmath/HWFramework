package com.android.server.wm;

import android.os.Environment;
import java.util.function.IntFunction;

/* renamed from: com.android.server.wm.-$$Lambda$OuObUsm0bB9g5X0kIXYkBYHvodY  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$OuObUsm0bB9g5X0kIXYkBYHvodY implements IntFunction {
    public static final /* synthetic */ $$Lambda$OuObUsm0bB9g5X0kIXYkBYHvodY INSTANCE = new $$Lambda$OuObUsm0bB9g5X0kIXYkBYHvodY();

    private /* synthetic */ $$Lambda$OuObUsm0bB9g5X0kIXYkBYHvodY() {
    }

    @Override // java.util.function.IntFunction
    public final Object apply(int i) {
        return Environment.getDataSystemCeDirectory(i);
    }
}
