package android.service.autofill.augmented;

import com.android.internal.util.function.TriConsumer;

/* renamed from: android.service.autofill.augmented.-$$Lambda$AugmentedAutofillService$1$4dXh5Zwc8KxDD9bV1LFhgo3zrgk  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$AugmentedAutofillService$1$4dXh5Zwc8KxDD9bV1LFhgo3zrgk implements TriConsumer {
    public static final /* synthetic */ $$Lambda$AugmentedAutofillService$1$4dXh5Zwc8KxDD9bV1LFhgo3zrgk INSTANCE = new $$Lambda$AugmentedAutofillService$1$4dXh5Zwc8KxDD9bV1LFhgo3zrgk();

    private /* synthetic */ $$Lambda$AugmentedAutofillService$1$4dXh5Zwc8KxDD9bV1LFhgo3zrgk() {
    }

    @Override // com.android.internal.util.function.TriConsumer
    public final void accept(Object obj, Object obj2, Object obj3) {
        ((AugmentedAutofillService) obj).handleOnConnected(((Boolean) obj2).booleanValue(), ((Boolean) obj3).booleanValue());
    }
}
