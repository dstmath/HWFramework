package android.net;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.Objects;

public final class DataUsageRequest implements Parcelable {
    public static final Parcelable.Creator<DataUsageRequest> CREATOR = new Parcelable.Creator<DataUsageRequest>() {
        /* class android.net.DataUsageRequest.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public DataUsageRequest createFromParcel(Parcel in) {
            return new DataUsageRequest(in.readInt(), (NetworkTemplate) in.readParcelable(null), in.readLong());
        }

        @Override // android.os.Parcelable.Creator
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

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.requestId);
        dest.writeParcelable(this.template, flags);
        dest.writeLong(this.thresholdInBytes);
    }

    public String toString() {
        return "DataUsageRequest [ requestId=" + this.requestId + ", networkTemplate=" + this.template + ", thresholdInBytes=" + this.thresholdInBytes + " ]";
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof DataUsageRequest)) {
            return false;
        }
        DataUsageRequest that = (DataUsageRequest) obj;
        if (that.requestId == this.requestId && Objects.equals(that.template, this.template) && that.thresholdInBytes == this.thresholdInBytes) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        return Objects.hash(Integer.valueOf(this.requestId), this.template, Long.valueOf(this.thresholdInBytes));
    }
}
