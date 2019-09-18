package android.service.autofill;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Slog;
import android.view.autofill.AutofillValue;
import android.view.autofill.Helper;
import com.android.internal.util.Preconditions;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class TextValueSanitizer extends InternalSanitizer implements Sanitizer, Parcelable {
    public static final Parcelable.Creator<TextValueSanitizer> CREATOR = new Parcelable.Creator<TextValueSanitizer>() {
        public TextValueSanitizer createFromParcel(Parcel parcel) {
            return new TextValueSanitizer((Pattern) parcel.readSerializable(), parcel.readString());
        }

        public TextValueSanitizer[] newArray(int size) {
            return new TextValueSanitizer[size];
        }
    };
    private static final String TAG = "TextValueSanitizer";
    private final Pattern mRegex;
    private final String mSubst;

    public TextValueSanitizer(Pattern regex, String subst) {
        this.mRegex = (Pattern) Preconditions.checkNotNull(regex);
        this.mSubst = (String) Preconditions.checkNotNull(subst);
    }

    public AutofillValue sanitize(AutofillValue value) {
        if (value == null) {
            Slog.w(TAG, "sanitize() called with null value");
            return null;
        } else if (!value.isText()) {
            if (Helper.sDebug) {
                Slog.d(TAG, "sanitize() called with non-text value: " + value);
            }
            return null;
        } else {
            try {
                Matcher matcher = this.mRegex.matcher(value.getTextValue());
                if (matcher.matches()) {
                    return AutofillValue.forText(matcher.replaceAll(this.mSubst));
                }
                if (Helper.sDebug) {
                    Slog.d(TAG, "sanitize(): " + this.mRegex + " failed for " + value);
                }
                return null;
            } catch (Exception e) {
                Slog.w(TAG, "Exception evaluating " + this.mRegex + "/" + this.mSubst + ": " + e);
                return null;
            }
        }
    }

    public String toString() {
        if (!Helper.sDebug) {
            return super.toString();
        }
        return "TextValueSanitizer: [regex=" + this.mRegex + ", subst=" + this.mSubst + "]";
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeSerializable(this.mRegex);
        parcel.writeString(this.mSubst);
    }
}
