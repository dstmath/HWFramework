package android.service.autofill;

import android.view.autofill.AutofillId;
import android.view.autofill.AutofillValue;

public interface ValueFinder {
    AutofillValue findRawValueByAutofillId(AutofillId autofillId);

    default String findByAutofillId(AutofillId id) {
        AutofillValue value = findRawValueByAutofillId(id);
        if (value == null || !value.isText()) {
            return null;
        }
        return value.getTextValue().toString();
    }
}
