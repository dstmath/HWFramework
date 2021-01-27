package android.view.autofill;

import android.content.Context;
import android.os.Bundle;
import android.util.ArrayMap;
import android.view.View;
import android.view.WindowManager;
import java.util.List;

public interface IHwAutofillHelper {
    void cacheCurrentData(Bundle bundle, String str, AutofillId[] autofillIdArr, ArrayMap<AutofillId, AutofillValue> arrayMap);

    boolean isHwAutofillService(Context context);

    void recordCurrentInfo(Context context, View view);

    void recordSavedState(Bundle bundle, String str);

    void resizeLayoutForLowResolution(View view, WindowManager.LayoutParams layoutParams);

    boolean shouldForbidFillRequest(Bundle bundle, String str);

    void updateAutoFillManagerClient(Bundle bundle, String str, IAutoFillManagerClient iAutoFillManagerClient, int i, List<AutofillId> list, List<AutofillValue> list2);

    boolean updateInitialFlag(Bundle bundle, String str);
}
