package android.service.autofill;

import android.view.autofill.AutofillId;
import com.android.internal.util.Preconditions;

final class AutofillServiceHelper {
    static AutofillId[] assertValid(AutofillId[] ids) {
        int i = 0;
        Preconditions.checkArgument(ids != null && ids.length > 0, "must have at least one id");
        while (i < ids.length) {
            if (ids[i] != null) {
                i++;
            } else {
                throw new IllegalArgumentException("ids[" + i + "] must not be null");
            }
        }
        return ids;
    }

    private AutofillServiceHelper() {
        throw new UnsupportedOperationException("contains static members only");
    }
}
