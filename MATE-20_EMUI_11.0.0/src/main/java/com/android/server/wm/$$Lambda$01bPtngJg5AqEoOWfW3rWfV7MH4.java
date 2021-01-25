package com.android.server.wm;

import java.util.function.Consumer;

/* renamed from: com.android.server.wm.-$$Lambda$01bPtngJg5AqEoOWfW3rWfV7MH4  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$01bPtngJg5AqEoOWfW3rWfV7MH4 implements Consumer {
    public static final /* synthetic */ $$Lambda$01bPtngJg5AqEoOWfW3rWfV7MH4 INSTANCE = new $$Lambda$01bPtngJg5AqEoOWfW3rWfV7MH4();

    private /* synthetic */ $$Lambda$01bPtngJg5AqEoOWfW3rWfV7MH4() {
    }

    @Override // java.util.function.Consumer
    public final void accept(Object obj) {
        ((WindowState) obj).onExitAnimationDone();
    }
}
