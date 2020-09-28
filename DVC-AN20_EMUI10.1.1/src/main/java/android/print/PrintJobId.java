package android.print;

import android.os.Parcel;
import android.os.Parcelable;
import com.android.internal.util.Preconditions;
import java.util.UUID;

public final class PrintJobId implements Parcelable {
    public static final Parcelable.Creator<PrintJobId> CREATOR = new Parcelable.Creator<PrintJobId>() {
        /* class android.print.PrintJobId.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public PrintJobId createFromParcel(Parcel parcel) {
            return new PrintJobId((String) Preconditions.checkNotNull(parcel.readString()));
        }

        @Override // android.os.Parcelable.Creator
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
        return (1 * 31) + this.mValue.hashCode();
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj != null && getClass() == obj.getClass() && this.mValue.equals(((PrintJobId) obj).mValue)) {
            return true;
        }
        return false;
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

    public static PrintJobId unflattenFromString(String string) {
        return new PrintJobId(string);
    }
}
