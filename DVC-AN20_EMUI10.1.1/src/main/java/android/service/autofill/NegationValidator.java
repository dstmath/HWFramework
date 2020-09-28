package android.service.autofill;

import android.os.Parcel;
import android.os.Parcelable;
import android.view.autofill.Helper;
import com.android.internal.util.Preconditions;

/* access modifiers changed from: package-private */
public final class NegationValidator extends InternalValidator {
    public static final Parcelable.Creator<NegationValidator> CREATOR = new Parcelable.Creator<NegationValidator>() {
        /* class android.service.autofill.NegationValidator.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public NegationValidator createFromParcel(Parcel parcel) {
            return new NegationValidator((InternalValidator) parcel.readParcelable(null));
        }

        @Override // android.os.Parcelable.Creator
        public NegationValidator[] newArray(int size) {
            return new NegationValidator[size];
        }
    };
    private final InternalValidator mValidator;

    NegationValidator(InternalValidator validator) {
        this.mValidator = (InternalValidator) Preconditions.checkNotNull(validator);
    }

    @Override // android.service.autofill.InternalValidator
    public boolean isValid(ValueFinder finder) {
        return !this.mValidator.isValid(finder);
    }

    public String toString() {
        if (!Helper.sDebug) {
            return super.toString();
        }
        return "NegationValidator: [validator=" + this.mValidator + "]";
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.mValidator, flags);
    }
}
