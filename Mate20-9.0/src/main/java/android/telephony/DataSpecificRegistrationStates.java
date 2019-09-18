package android.telephony;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.Objects;

public class DataSpecificRegistrationStates implements Parcelable {
    public static final Parcelable.Creator<DataSpecificRegistrationStates> CREATOR = new Parcelable.Creator<DataSpecificRegistrationStates>() {
        public DataSpecificRegistrationStates createFromParcel(Parcel source) {
            return new DataSpecificRegistrationStates(source);
        }

        public DataSpecificRegistrationStates[] newArray(int size) {
            return new DataSpecificRegistrationStates[size];
        }
    };
    public final int maxDataCalls;

    DataSpecificRegistrationStates(int maxDataCalls2) {
        this.maxDataCalls = maxDataCalls2;
    }

    private DataSpecificRegistrationStates(Parcel source) {
        this.maxDataCalls = source.readInt();
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.maxDataCalls);
    }

    public int describeContents() {
        return 0;
    }

    public String toString() {
        return "DataSpecificRegistrationStates { mMaxDataCalls=" + this.maxDataCalls + "}";
    }

    public int hashCode() {
        return Objects.hash(new Object[]{Integer.valueOf(this.maxDataCalls)});
    }

    public boolean equals(Object o) {
        boolean z = true;
        if (this == o) {
            return true;
        }
        if (o == null || !(o instanceof DataSpecificRegistrationStates)) {
            return false;
        }
        if (this.maxDataCalls != ((DataSpecificRegistrationStates) o).maxDataCalls) {
            z = false;
        }
        return z;
    }
}
