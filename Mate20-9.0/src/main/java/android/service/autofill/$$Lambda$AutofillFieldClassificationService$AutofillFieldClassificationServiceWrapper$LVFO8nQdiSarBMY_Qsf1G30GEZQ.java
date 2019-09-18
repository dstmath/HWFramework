package android.service.autofill;

import android.os.Bundle;
import android.os.RemoteCallback;
import com.android.internal.util.function.HexConsumer;
import java.util.List;

/* renamed from: android.service.autofill.-$$Lambda$AutofillFieldClassificationService$AutofillFieldClassificationServiceWrapper$LVFO8nQdiSarBMY_Qsf1G30GEZQ  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$AutofillFieldClassificationService$AutofillFieldClassificationServiceWrapper$LVFO8nQdiSarBMY_Qsf1G30GEZQ implements HexConsumer {
    public static final /* synthetic */ $$Lambda$AutofillFieldClassificationService$AutofillFieldClassificationServiceWrapper$LVFO8nQdiSarBMY_Qsf1G30GEZQ INSTANCE = new $$Lambda$AutofillFieldClassificationService$AutofillFieldClassificationServiceWrapper$LVFO8nQdiSarBMY_Qsf1G30GEZQ();

    private /* synthetic */ $$Lambda$AutofillFieldClassificationService$AutofillFieldClassificationServiceWrapper$LVFO8nQdiSarBMY_Qsf1G30GEZQ() {
    }

    public final void accept(Object obj, Object obj2, Object obj3, Object obj4, Object obj5, Object obj6) {
        ((AutofillFieldClassificationService) ((AutofillFieldClassificationService) obj)).getScores((RemoteCallback) obj2, (String) obj3, (Bundle) obj4, (List) obj5, (String[]) obj6);
    }
}
