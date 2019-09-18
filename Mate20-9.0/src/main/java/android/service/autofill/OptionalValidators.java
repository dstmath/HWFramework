package android.service.autofill;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.view.autofill.Helper;
import com.android.internal.util.Preconditions;

final class OptionalValidators extends InternalValidator {
    public static final Parcelable.Creator<OptionalValidators> CREATOR = new Parcelable.Creator<OptionalValidators>() {
        public OptionalValidators createFromParcel(Parcel parcel) {
            return new OptionalValidators((InternalValidator[]) parcel.readParcelableArray(null, InternalValidator.class));
        }

        public OptionalValidators[] newArray(int size) {
            return new OptionalValidators[size];
        }
    };
    private static final String TAG = "OptionalValidators";
    private final InternalValidator[] mValidators;

    OptionalValidators(InternalValidator[] validators) {
        this.mValidators = (InternalValidator[]) Preconditions.checkArrayElementsNotNull(validators, "validators");
    }

    public boolean isValid(ValueFinder finder) {
        for (InternalValidator validator : this.mValidators) {
            boolean valid = validator.isValid(finder);
            if (Helper.sDebug) {
                Log.d(TAG, "isValid(" + validator + "): " + valid);
            }
            if (valid) {
                return true;
            }
        }
        return false;
    }

    public String toString() {
        if (!Helper.sDebug) {
            return super.toString();
        }
        return "OptionalValidators: [validators=" + this.mValidators + "]";
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelableArray(this.mValidators, flags);
    }
}
