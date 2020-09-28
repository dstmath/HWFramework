package android.service.autofill;

import android.os.Bundle;
import android.os.RemoteCallback;
import com.android.internal.util.function.NonaConsumer;
import java.util.List;
import java.util.Map;

/* renamed from: android.service.autofill.-$$Lambda$AutofillFieldClassificationService$AutofillFieldClassificationServiceWrapper$mUalgFt87R5lup2LhB9vW49Xixs  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$AutofillFieldClassificationService$AutofillFieldClassificationServiceWrapper$mUalgFt87R5lup2LhB9vW49Xixs implements NonaConsumer {
    public static final /* synthetic */ $$Lambda$AutofillFieldClassificationService$AutofillFieldClassificationServiceWrapper$mUalgFt87R5lup2LhB9vW49Xixs INSTANCE = new $$Lambda$AutofillFieldClassificationService$AutofillFieldClassificationServiceWrapper$mUalgFt87R5lup2LhB9vW49Xixs();

    private /* synthetic */ $$Lambda$AutofillFieldClassificationService$AutofillFieldClassificationServiceWrapper$mUalgFt87R5lup2LhB9vW49Xixs() {
    }

    @Override // com.android.internal.util.function.NonaConsumer
    public final void accept(Object obj, Object obj2, Object obj3, Object obj4, Object obj5, Object obj6, Object obj7, Object obj8, Object obj9) {
        ((AutofillFieldClassificationService) obj).calculateScores((RemoteCallback) obj2, (List) obj3, (String[]) obj4, (String[]) obj5, (String) obj6, (Bundle) obj7, (Map) obj8, (Map) obj9);
    }
}
