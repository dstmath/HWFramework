package android.net;

import android.os.Parcel;
import android.os.Parcelable;

public final class IpSecTransformResponse implements Parcelable {
    public static final Parcelable.Creator<IpSecTransformResponse> CREATOR = new Parcelable.Creator<IpSecTransformResponse>() {
        /* class android.net.IpSecTransformResponse.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public IpSecTransformResponse createFromParcel(Parcel in) {
            return new IpSecTransformResponse(in);
        }

        @Override // android.os.Parcelable.Creator
        public IpSecTransformResponse[] newArray(int size) {
            return new IpSecTransformResponse[size];
        }
    };
    private static final String TAG = "IpSecTransformResponse";
    public final int resourceId;
    public final int status;

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(this.status);
        out.writeInt(this.resourceId);
    }

    public IpSecTransformResponse(int inStatus) {
        if (inStatus != 0) {
            this.status = inStatus;
            this.resourceId = -1;
            return;
        }
        throw new IllegalArgumentException("Valid status implies other args must be provided");
    }

    public IpSecTransformResponse(int inStatus, int inResourceId) {
        this.status = inStatus;
        this.resourceId = inResourceId;
    }

    private IpSecTransformResponse(Parcel in) {
        this.status = in.readInt();
        this.resourceId = in.readInt();
    }
}
