package android.print;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import com.android.internal.util.Preconditions;
import java.util.UUID;

public final class PrintJobId implements Parcelable {
    public static final Creator<PrintJobId> CREATOR = new Creator<PrintJobId>() {
        public PrintJobId createFromParcel(Parcel parcel) {
            return new PrintJobId((String) Preconditions.checkNotNull(parcel.readString()));
        }

        public PrintJobId[] newArray(int size) {
            return new PrintJobId[size];
        }
    };
    private final String mValue;

    public PrintJobId() {
        this(UUID.randomUUID().toString());
    }

    public PrintJobId(String value) {
        this.mValue = value;
    }

    public int hashCode() {
        return this.mValue.hashCode() + 31;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        return this.mValue.equals(((PrintJobId) obj).mValue);
    }

    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(this.mValue);
    }

    public int describeContents() {
        return 0;
    }

    public String flattenToString() {
        return this.mValue;
    }

    public static PrintJobId unflattenFromString(String string) {
        return new PrintJobId(string);
    }
}
