package android.view.textclassifier;

import android.os.Parcel;
import android.os.Parcelable;
import com.android.internal.util.Preconditions;
import java.util.Locale;
import java.util.UUID;

public final class TextClassificationSessionId implements Parcelable {
    public static final Parcelable.Creator<TextClassificationSessionId> CREATOR = new Parcelable.Creator<TextClassificationSessionId>() {
        /* class android.view.textclassifier.TextClassificationSessionId.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public TextClassificationSessionId createFromParcel(Parcel parcel) {
            return new TextClassificationSessionId((String) Preconditions.checkNotNull(parcel.readString()));
        }

        @Override // android.os.Parcelable.Creator
        public TextClassificationSessionId[] newArray(int size) {
            return new TextClassificationSessionId[size];
        }
    };
    private final String mValue;

    public TextClassificationSessionId() {
        this(UUID.randomUUID().toString());
    }

    public TextClassificationSessionId(String value) {
        this.mValue = value;
    }

    public int hashCode() {
        return (1 * 31) + this.mValue.hashCode();
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj != null && getClass() == obj.getClass() && this.mValue.equals(((TextClassificationSessionId) obj).mValue)) {
            return true;
        }
        return false;
    }

    public String toString() {
        return String.format(Locale.US, "TextClassificationSessionId {%s}", this.mValue);
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(this.mValue);
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    public String flattenToString() {
        return this.mValue;
    }

    public static TextClassificationSessionId unflattenFromString(String string) {
        return new TextClassificationSessionId(string);
    }
}
