package android.graphics.drawable;

import android.animation.Animator;
import java.util.function.Consumer;

/* renamed from: android.graphics.drawable.-$$Lambda$GH0R7RFIaNRo5vIhb-BgtisLMbI  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$GH0R7RFIaNRo5vIhbBgtisLMbI implements Consumer {
    public static final /* synthetic */ $$Lambda$GH0R7RFIaNRo5vIhbBgtisLMbI INSTANCE = new $$Lambda$GH0R7RFIaNRo5vIhbBgtisLMbI();

    private /* synthetic */ $$Lambda$GH0R7RFIaNRo5vIhbBgtisLMbI() {
    }

    @Override // java.util.function.Consumer
    public final void accept(Object obj) {
        ((Animator) obj).cancel();
    }
}
