package com.android.server.autofill;

import android.service.autofill.Dataset;
import com.android.internal.util.function.QuintConsumer;

/* renamed from: com.android.server.autofill.-$$Lambda$knR7oLyPSG_CoFAxBA_nqSw3JBo  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$knR7oLyPSG_CoFAxBA_nqSw3JBo implements QuintConsumer {
    public static final /* synthetic */ $$Lambda$knR7oLyPSG_CoFAxBA_nqSw3JBo INSTANCE = new $$Lambda$knR7oLyPSG_CoFAxBA_nqSw3JBo();

    private /* synthetic */ $$Lambda$knR7oLyPSG_CoFAxBA_nqSw3JBo() {
    }

    public final void accept(Object obj, Object obj2, Object obj3, Object obj4, Object obj5) {
        ((Session) obj).autoFill(((Integer) obj2).intValue(), ((Integer) obj3).intValue(), (Dataset) obj4, ((Boolean) obj5).booleanValue());
    }
}
