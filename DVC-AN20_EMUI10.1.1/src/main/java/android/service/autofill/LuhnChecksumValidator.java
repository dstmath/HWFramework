package android.service.autofill;

import android.os.Parcel;
import android.os.Parcelable;
import android.provider.Downloads;
import android.util.Log;
import android.view.autofill.AutofillId;
import android.view.autofill.Helper;
import com.android.internal.util.Preconditions;
import java.util.Arrays;

public final class LuhnChecksumValidator extends InternalValidator implements Validator, Parcelable {
    public static final Parcelable.Creator<LuhnChecksumValidator> CREATOR = new Parcelable.Creator<LuhnChecksumValidator>() {
        /* class android.service.autofill.LuhnChecksumValidator.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public LuhnChecksumValidator createFromParcel(Parcel parcel) {
            return new LuhnChecksumValidator((AutofillId[]) parcel.readParcelableArray(null, AutofillId.class));
        }

        @Override // android.os.Parcelable.Creator
        public LuhnChecksumValidator[] newArray(int size) {
            return new LuhnChecksumValidator[size];
        }
    };
    private static final String TAG = "LuhnChecksumValidator";
    private final AutofillId[] mIds;

    public LuhnChecksumValidator(AutofillId... ids) {
        this.mIds = (AutofillId[]) Preconditions.checkArrayElementsNotNull(ids, Downloads.EXTRA_IDS);
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
                    addend = digit * 2;
                    if (addend > 9) {
                        addend -= 9;
                    }
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
        return sum % 10 == 0;
    }

    @Override // android.service.autofill.InternalValidator
    public boolean isValid(ValueFinder finder) {
        AutofillId[] autofillIdArr = this.mIds;
        if (autofillIdArr == null || autofillIdArr.length == 0) {
            return false;
        }
        StringBuilder builder = new StringBuilder();
        AutofillId[] autofillIdArr2 = this.mIds;
        for (AutofillId id : autofillIdArr2) {
            String partialNumber = finder.findByAutofillId(id);
            if (partialNumber == null) {
                if (Helper.sDebug) {
                    Log.d(TAG, "No partial number for id " + id);
                }
                return false;
            }
            builder.append(partialNumber);
        }
        String number = builder.toString();
        boolean valid = isLuhnChecksumValid(number);
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

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeParcelableArray(this.mIds, flags);
    }
}
