package com.android.server.print;

import android.content.ComponentName;
import java.util.function.BiConsumer;

/* renamed from: com.android.server.print.-$$Lambda$UserState$HoM_sy_T_4RiQGYcbixewHZ2IMA  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$UserState$HoM_sy_T_4RiQGYcbixewHZ2IMA implements BiConsumer {
    public static final /* synthetic */ $$Lambda$UserState$HoM_sy_T_4RiQGYcbixewHZ2IMA INSTANCE = new $$Lambda$UserState$HoM_sy_T_4RiQGYcbixewHZ2IMA();

    private /* synthetic */ $$Lambda$UserState$HoM_sy_T_4RiQGYcbixewHZ2IMA() {
    }

    @Override // java.util.function.BiConsumer
    public final void accept(Object obj, Object obj2) {
        ((UserState) obj).failScheduledPrintJobsForServiceInternal((ComponentName) obj2);
    }
}
