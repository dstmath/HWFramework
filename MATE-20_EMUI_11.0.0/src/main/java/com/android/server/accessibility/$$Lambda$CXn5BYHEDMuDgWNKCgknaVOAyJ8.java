package com.android.server.accessibility;

import android.view.MagnificationSpec;
import com.android.internal.util.function.TriConsumer;
import com.android.server.accessibility.MagnificationController;

/* renamed from: com.android.server.accessibility.-$$Lambda$CXn5BYHEDMuDgWNKCgknaVOAyJ8  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$CXn5BYHEDMuDgWNKCgknaVOAyJ8 implements TriConsumer {
    public static final /* synthetic */ $$Lambda$CXn5BYHEDMuDgWNKCgknaVOAyJ8 INSTANCE = new $$Lambda$CXn5BYHEDMuDgWNKCgknaVOAyJ8();

    private /* synthetic */ $$Lambda$CXn5BYHEDMuDgWNKCgknaVOAyJ8() {
    }

    public final void accept(Object obj, Object obj2, Object obj3) {
        ((MagnificationController.SpecAnimationBridge) obj).updateSentSpecMainThread((MagnificationSpec) obj2, ((Boolean) obj3).booleanValue());
    }
}
