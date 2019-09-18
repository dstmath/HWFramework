package android.net;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.Objects;

public final class DataUsageRequest implements Parcelable {
    public static final Parcelable.Creator<DataUsageRequest> CREATOR = new Parcelable.Creator<DataUsageRequest>() {
        public DataUsageRequest createFromParcel(Parcel in) {
            return new DataUsageRequest(in.readInt(), (NetworkTemplate) in.readParcelable(null), in.readLong());
        }

        public DataUsageRequest[] newArray(int size) {
            return new DataUsageRequest[size];
        }
    };
    public static final String PARCELABLE_KEY = "DataUsageRequest";
    public static final int REQUEST_ID_UNSET = 0;
    public final int requestId;
    public final NetworkTemplate template;
    public final long thresholdInBytes;

    public DataUsageRequest(int requestId2, NetworkTemplate template2, long thresholdInBytes2) {
        this.requestId = requestId2;
        this.template = template2;
        this.thresholdInBytes = thresholdInBytes2;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.requestId);
        dest.writeParcelable(this.template, flags);
        dest.writeLong(this.thresholdInBytes);
    }

    public String toString() {
        return "DataUsageRequest [ requestId=" + this.requestId + ", networkTemplate=" + this.template + ", thresholdInBytes=" + this.thresholdInBytes + " ]";
    }

    public boolean equals(Object obj) {
        boolean z = false;
        if (!(obj instanceof DataUsageRequest)) {
            return false;
        }
        DataUsageRequest that = (DataUsageRequest) obj;
        if (that.requestId == this.requestId && Objects.equals(that.template, this.template) && that.thresholdInBytes == this.thresholdInBytes) {
            z = true;
        }
        return z;
    }

    public int hashCode() {
        return Objects.hash(new Object[]{Integer.valueOf(this.requestId), this.template, Long.valueOf(this.thresholdInBytes)});
    }
}
