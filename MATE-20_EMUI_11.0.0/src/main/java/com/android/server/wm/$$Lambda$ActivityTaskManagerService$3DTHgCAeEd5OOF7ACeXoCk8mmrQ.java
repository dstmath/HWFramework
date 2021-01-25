package com.android.server.wm;

import android.os.IBinder;
import java.util.function.BiConsumer;

/* renamed from: com.android.server.wm.-$$Lambda$ActivityTaskManagerService$3DTHgCAeEd5OOF7ACeXoCk8mmrQ  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$ActivityTaskManagerService$3DTHgCAeEd5OOF7ACeXoCk8mmrQ implements BiConsumer {
    public static final /* synthetic */ $$Lambda$ActivityTaskManagerService$3DTHgCAeEd5OOF7ACeXoCk8mmrQ INSTANCE = new $$Lambda$ActivityTaskManagerService$3DTHgCAeEd5OOF7ACeXoCk8mmrQ();

    private /* synthetic */ $$Lambda$ActivityTaskManagerService$3DTHgCAeEd5OOF7ACeXoCk8mmrQ() {
    }

    @Override // java.util.function.BiConsumer
    public final void accept(Object obj, Object obj2) {
        ((ActivityTaskManagerService) obj).expireStartAsCallerTokenMsg((IBinder) obj2);
    }
}
