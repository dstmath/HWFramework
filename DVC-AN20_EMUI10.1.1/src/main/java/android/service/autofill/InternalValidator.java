package android.service.autofill;

import android.os.Parcelable;

public abstract class InternalValidator implements Validator, Parcelable {
    public abstract boolean isValid(ValueFinder valueFinder);
}
