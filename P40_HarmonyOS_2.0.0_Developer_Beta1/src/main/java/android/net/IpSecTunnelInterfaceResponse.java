package android.net;

import android.os.Parcel;
import android.os.Parcelable;

public final class IpSecTunnelInterfaceResponse implements Parcelable {
    public static final Parcelable.Creator<IpSecTunnelInterfaceResponse> CREATOR = new Parcelable.Creator<IpSecTunnelInterfaceResponse>() {
        /* class android.net.IpSecTunnelInterfaceResponse.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public IpSecTunnelInterfaceResponse createFromParcel(Parcel in) {
            return new IpSecTunnelInterfaceResponse(in);
        }

        @Override // android.os.Parcelable.Creator
        public IpSecTunnelInterfaceResponse[] newArray(int size) {
            return new IpSecTunnelInterfaceResponse[size];
        }
    };
    private static final String TAG = "IpSecTunnelInterfaceResponse";
    public final String interfaceName;
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
        out.writeString(this.interfaceName);
    }

    public IpSecTunnelInterfaceResponse(int inStatus) {
        if (inStatus != 0) {
            this.status = inStatus;
            this.resourceId = -1;
            this.interfaceName = "";
            return;
        }
        throw new IllegalArgumentException("Valid status implies other args must be provided");
    }

    public IpSecTunnelInterfaceResponse(int inStatus, int inResourceId, String inInterfaceName) {
        this.status = inStatus;
        this.resourceId = inResourceId;
        this.interfaceName = inInterfaceName;
    }

    private IpSecTunnelInterfaceResponse(Parcel in) {
        this.status = in.readInt();
        this.resourceId = in.readInt();
        this.interfaceName = in.readString();
    }
}
