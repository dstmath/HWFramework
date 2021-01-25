package android.service.autofill.augmented;

import android.annotation.SystemApi;
import android.os.RemoteException;
import android.service.autofill.augmented.AugmentedAutofillService;
import android.util.Log;
import android.util.Pair;
import android.view.autofill.AutofillId;
import android.view.autofill.AutofillValue;
import com.android.internal.util.Preconditions;
import java.util.List;

@SystemApi
public final class FillController {
    private static final String TAG = FillController.class.getSimpleName();
    private final AugmentedAutofillService.AutofillProxy mProxy;

    FillController(AugmentedAutofillService.AutofillProxy proxy) {
        this.mProxy = proxy;
    }

    public void autofill(List<Pair<AutofillId, AutofillValue>> values) {
        Preconditions.checkNotNull(values);
        if (AugmentedAutofillService.sDebug) {
            String str = TAG;
            Log.d(str, "autofill() with " + values.size() + " values");
        }
        try {
            this.mProxy.autofill(values);
            FillWindow fillWindow = this.mProxy.getFillWindow();
            if (fillWindow != null) {
                fillWindow.destroy();
            }
        } catch (RemoteException e) {
            e.rethrowAsRuntimeException();
        }
    }
}
