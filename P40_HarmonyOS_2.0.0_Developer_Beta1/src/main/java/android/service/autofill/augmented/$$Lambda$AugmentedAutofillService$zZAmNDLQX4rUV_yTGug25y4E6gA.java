package android.service.autofill.augmented;

import java.util.function.Consumer;

/* renamed from: android.service.autofill.augmented.-$$Lambda$AugmentedAutofillService$zZAmNDLQX4rUV_yTGug25y4E6gA  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$AugmentedAutofillService$zZAmNDLQX4rUV_yTGug25y4E6gA implements Consumer {
    public static final /* synthetic */ $$Lambda$AugmentedAutofillService$zZAmNDLQX4rUV_yTGug25y4E6gA INSTANCE = new $$Lambda$AugmentedAutofillService$zZAmNDLQX4rUV_yTGug25y4E6gA();

    private /* synthetic */ $$Lambda$AugmentedAutofillService$zZAmNDLQX4rUV_yTGug25y4E6gA() {
    }

    @Override // java.util.function.Consumer
    public final void accept(Object obj) {
        ((AugmentedAutofillService) obj).handleOnUnbind();
    }
}
