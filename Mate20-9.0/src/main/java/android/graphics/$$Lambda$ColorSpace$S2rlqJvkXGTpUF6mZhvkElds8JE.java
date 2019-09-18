package android.graphics;

import java.util.function.DoubleUnaryOperator;

/* renamed from: android.graphics.-$$Lambda$ColorSpace$S2rlqJvkXGTpUF6mZhvkElds8JE  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$ColorSpace$S2rlqJvkXGTpUF6mZhvkElds8JE implements DoubleUnaryOperator {
    public static final /* synthetic */ $$Lambda$ColorSpace$S2rlqJvkXGTpUF6mZhvkElds8JE INSTANCE = new $$Lambda$ColorSpace$S2rlqJvkXGTpUF6mZhvkElds8JE();

    private /* synthetic */ $$Lambda$ColorSpace$S2rlqJvkXGTpUF6mZhvkElds8JE() {
    }

    public final double applyAsDouble(double d) {
        return ColorSpace.absResponse(d, 0.9478672985781991d, 0.05213270142180095d, 0.07739938080495357d, 0.04045d, 2.4d);
    }
}
