package android.telephony;

import java.util.function.Function;

/* renamed from: android.telephony.-$$Lambda$MLKtmRGKP3e0WU7x_KyS5-Vg8q4  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$MLKtmRGKP3e0WU7x_KyS5Vg8q4 implements Function {
    public static final /* synthetic */ $$Lambda$MLKtmRGKP3e0WU7x_KyS5Vg8q4 INSTANCE = new $$Lambda$MLKtmRGKP3e0WU7x_KyS5Vg8q4();

    private /* synthetic */ $$Lambda$MLKtmRGKP3e0WU7x_KyS5Vg8q4() {
    }

    @Override // java.util.function.Function
    public final Object apply(Object obj) {
        return ((NetworkRegistrationInfo) obj).sanitizeLocationInfo();
    }
}
