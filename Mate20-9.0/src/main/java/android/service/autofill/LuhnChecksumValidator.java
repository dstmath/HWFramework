package android.service.autofill;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.view.autofill.AutofillId;
import android.view.autofill.Helper;
import com.android.internal.util.Preconditions;
import java.util.Arrays;

public final class LuhnChecksumValidator extends InternalValidator implements Validator, Parcelable {
    public static final Parcelable.Creator<LuhnChecksumValidator> CREATOR = new Parcelable.Creator<LuhnChecksumValidator>() {
        public LuhnChecksumValidator createFromParcel(Parcel parcel) {
            return new LuhnChecksumValidator((AutofillId[]) parcel.readParcelableArray(null, AutofillId.class));
        }

        public LuhnChecksumValidator[] newArray(int size) {
            return new LuhnChecksumValidator[size];
        }
    };
    private static final String TAG = "LuhnChecksumValidator";
    private final AutofillId[] mIds;

    public LuhnChecksumValidator(AutofillId... ids) {
        this.mIds = (AutofillId[]) Preconditions.checkArrayElementsNotNull(ids, "ids");
    }

    private static boolean isLuhnChecksumValid(String number) {
        int addend;
        int sum = 0;
        boolean isDoubled = false;
        int i = number.length() - 1;
        while (true) {
            boolean z = false;
            if (i < 0) {
                break;
            }
            int digit = number.charAt(i) - '0';
            if (digit >= 0 && digit <= 9) {
                if (isDoubled) {
                    int addend2 = digit * 2;
                    addend = addend2 > 9 ? addend2 - 9 : addend2;
                } else {
                    addend = digit;
                }
                sum += addend;
                if (!isDoubled) {
                    z = true;
                }
                isDoubled = z;
            }
            i--;
        }
        if (sum % 10 == 0) {
            return true;
        }
        return false;
    }

    public boolean isValid(ValueFinder finder) {
        if (this.mIds == null || this.mIds.length == 0) {
            return false;
        }
        StringBuilder builder = new StringBuilder();
        for (AutofillId id : this.mIds) {
            String partialNumber = finder.findByAutofillId(id);
            if (partialNumber == null) {
                if (Helper.sDebug) {
                    Log.d(TAG, "No partial number for id " + id);
                }
                return false;
            }
            builder.append(partialNumber);
        }
        boolean valid = isLuhnChecksumValid(builder.toString());
        if (Helper.sDebug) {
            Log.d(TAG, "isValid(" + number.length() + " chars): " + valid);
        }
        return valid;
    }

    public String toString() {
        if (!Helper.sDebug) {
            return super.toString();
        }
        return "LuhnChecksumValidator: [ids=" + Arrays.toString(this.mIds) + "]";
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeParcelableArray(this.mIds, flags);
    }
}
