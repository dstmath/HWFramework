package com.android.server.accessibility;

import com.android.internal.util.function.QuintConsumer;
import com.android.server.accessibility.MagnificationController;

/* renamed from: com.android.server.accessibility.-$$Lambda$iE9JplYHP8mrOjjadf_Oh8XKSE4  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$iE9JplYHP8mrOjjadf_Oh8XKSE4 implements QuintConsumer {
    public static final /* synthetic */ $$Lambda$iE9JplYHP8mrOjjadf_Oh8XKSE4 INSTANCE = new $$Lambda$iE9JplYHP8mrOjjadf_Oh8XKSE4();

    private /* synthetic */ $$Lambda$iE9JplYHP8mrOjjadf_Oh8XKSE4() {
    }

    public final void accept(Object obj, Object obj2, Object obj3, Object obj4, Object obj5) {
        ((MagnificationController.DisplayMagnification) obj).requestRectangleOnScreen(((Integer) obj2).intValue(), ((Integer) obj3).intValue(), ((Integer) obj4).intValue(), ((Integer) obj5).intValue());
    }
}
