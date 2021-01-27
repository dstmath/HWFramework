package android.service.autofill.augmented;

import java.util.function.Consumer;

/* renamed from: android.service.autofill.augmented.-$$Lambda$AugmentedAutofillService$1$LSvI4QN2NxJLegcZI0BFIvKwp6o  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$AugmentedAutofillService$1$LSvI4QN2NxJLegcZI0BFIvKwp6o implements Consumer {
    public static final /* synthetic */ $$Lambda$AugmentedAutofillService$1$LSvI4QN2NxJLegcZI0BFIvKwp6o INSTANCE = new $$Lambda$AugmentedAutofillService$1$LSvI4QN2NxJLegcZI0BFIvKwp6o();

    private /* synthetic */ $$Lambda$AugmentedAutofillService$1$LSvI4QN2NxJLegcZI0BFIvKwp6o() {
    }

    @Override // java.util.function.Consumer
    public final void accept(Object obj) {
        ((AugmentedAutofillService) obj).handleOnDestroyAllFillWindowsRequest();
    }
}
