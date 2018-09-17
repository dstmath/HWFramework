package android.net;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.util.Objects;

public final class DataUsageRequest implements Parcelable {
    public static final Creator<DataUsageRequest> CREATOR = new Creator<DataUsageRequest>() {
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

    public DataUsageRequest(int requestId, NetworkTemplate template, long thresholdInBytes) {
        this.requestId = requestId;
        this.template = template;
        this.thresholdInBytes = thresholdInBytes;
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
