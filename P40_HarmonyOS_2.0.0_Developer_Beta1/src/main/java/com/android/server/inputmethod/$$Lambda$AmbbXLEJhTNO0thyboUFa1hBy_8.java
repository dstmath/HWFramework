package com.android.server.inputmethod;

import android.os.Binder;
import com.android.internal.util.function.QuadConsumer;
import com.android.server.wm.WindowManagerInternal;

/* renamed from: com.android.server.inputmethod.-$$Lambda$AmbbXLEJhTNO0thyboUFa1hBy_8  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$AmbbXLEJhTNO0thyboUFa1hBy_8 implements QuadConsumer {
    public static final /* synthetic */ $$Lambda$AmbbXLEJhTNO0thyboUFa1hBy_8 INSTANCE = new $$Lambda$AmbbXLEJhTNO0thyboUFa1hBy_8();

    private /* synthetic */ $$Lambda$AmbbXLEJhTNO0thyboUFa1hBy_8() {
    }

    public final void accept(Object obj, Object obj2, Object obj3, Object obj4) {
        ((WindowManagerInternal) obj).addWindowToken((Binder) obj2, ((Integer) obj3).intValue(), ((Integer) obj4).intValue());
    }
}
