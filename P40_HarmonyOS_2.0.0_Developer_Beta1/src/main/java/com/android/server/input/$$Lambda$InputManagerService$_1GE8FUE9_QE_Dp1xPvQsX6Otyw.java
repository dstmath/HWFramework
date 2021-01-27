package com.android.server.input;

import android.hardware.display.DisplayViewport;
import java.util.function.Consumer;

/* renamed from: com.android.server.input.-$$Lambda$InputManagerService$_1GE8FUE9_QE_Dp1xPvQsX6Otyw  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$InputManagerService$_1GE8FUE9_QE_Dp1xPvQsX6Otyw implements Consumer {
    public static final /* synthetic */ $$Lambda$InputManagerService$_1GE8FUE9_QE_Dp1xPvQsX6Otyw INSTANCE = new $$Lambda$InputManagerService$_1GE8FUE9_QE_Dp1xPvQsX6Otyw();

    private /* synthetic */ $$Lambda$InputManagerService$_1GE8FUE9_QE_Dp1xPvQsX6Otyw() {
    }

    @Override // java.util.function.Consumer
    public final void accept(Object obj) {
        InputManagerService.lambda$setDisplayViewportsInternal$1((DisplayViewport) obj);
    }
}
