package android.view;

import android.animation.TypeEvaluator;
import android.graphics.Insets;

/* renamed from: android.view.-$$Lambda$InsetsController$Cj7UJrCkdHvJAZ_cYKrXuTMsjz8  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$InsetsController$Cj7UJrCkdHvJAZ_cYKrXuTMsjz8 implements TypeEvaluator {
    public static final /* synthetic */ $$Lambda$InsetsController$Cj7UJrCkdHvJAZ_cYKrXuTMsjz8 INSTANCE = new $$Lambda$InsetsController$Cj7UJrCkdHvJAZ_cYKrXuTMsjz8();

    private /* synthetic */ $$Lambda$InsetsController$Cj7UJrCkdHvJAZ_cYKrXuTMsjz8() {
    }

    @Override // android.animation.TypeEvaluator
    public final Object evaluate(float f, Object obj, Object obj2) {
        Insets insets;
        Insets insets2;
        return Insets.of(0, (int) (((float) insets.top) + (((float) (insets2.top - insets.top)) * f)), 0, (int) (((float) insets.bottom) + (((float) (((Insets) obj2).bottom - ((Insets) obj).bottom)) * f)));
    }
}
