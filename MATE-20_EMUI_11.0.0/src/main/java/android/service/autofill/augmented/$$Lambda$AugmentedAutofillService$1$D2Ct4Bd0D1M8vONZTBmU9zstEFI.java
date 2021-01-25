package android.service.autofill.augmented;

import java.util.function.Consumer;

/* renamed from: android.service.autofill.augmented.-$$Lambda$AugmentedAutofillService$1$D2Ct4Bd0D1M8vONZTBmU9zstEFI  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$AugmentedAutofillService$1$D2Ct4Bd0D1M8vONZTBmU9zstEFI implements Consumer {
    public static final /* synthetic */ $$Lambda$AugmentedAutofillService$1$D2Ct4Bd0D1M8vONZTBmU9zstEFI INSTANCE = new $$Lambda$AugmentedAutofillService$1$D2Ct4Bd0D1M8vONZTBmU9zstEFI();

    private /* synthetic */ $$Lambda$AugmentedAutofillService$1$D2Ct4Bd0D1M8vONZTBmU9zstEFI() {
    }

    @Override // java.util.function.Consumer
    public final void accept(Object obj) {
        ((AugmentedAutofillService) obj).handleOnDisconnected();
    }
}
