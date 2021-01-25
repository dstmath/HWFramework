package android.service.autofill.augmented;

import android.content.ComponentName;
import android.os.IBinder;
import android.view.autofill.AutofillId;
import android.view.autofill.AutofillValue;
import com.android.internal.util.function.NonaConsumer;

/* renamed from: android.service.autofill.augmented.-$$Lambda$AugmentedAutofillService$1$mgzh8N5GuvmPXfqMBgjw-Q27Ij0  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$AugmentedAutofillService$1$mgzh8N5GuvmPXfqMBgjwQ27Ij0 implements NonaConsumer {
    public static final /* synthetic */ $$Lambda$AugmentedAutofillService$1$mgzh8N5GuvmPXfqMBgjwQ27Ij0 INSTANCE = new $$Lambda$AugmentedAutofillService$1$mgzh8N5GuvmPXfqMBgjwQ27Ij0();

    private /* synthetic */ $$Lambda$AugmentedAutofillService$1$mgzh8N5GuvmPXfqMBgjwQ27Ij0() {
    }

    @Override // com.android.internal.util.function.NonaConsumer
    public final void accept(Object obj, Object obj2, Object obj3, Object obj4, Object obj5, Object obj6, Object obj7, Object obj8, Object obj9) {
        ((AugmentedAutofillService) obj).handleOnFillRequest(((Integer) obj2).intValue(), (IBinder) obj3, ((Integer) obj4).intValue(), (ComponentName) obj5, (AutofillId) obj6, (AutofillValue) obj7, ((Long) obj8).longValue(), (IFillCallback) obj9);
    }
}
