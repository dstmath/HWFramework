package android.net;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class NetworkMisc implements Parcelable {
    public static final Creator<NetworkMisc> CREATOR = new Creator<NetworkMisc>() {
        public NetworkMisc createFromParcel(Parcel in) {
            boolean z;
            boolean z2 = true;
            NetworkMisc networkMisc = new NetworkMisc();
            networkMisc.allowBypass = in.readInt() != 0;
            if (in.readInt() != 0) {
                z = true;
            } else {
                z = false;
            }
            networkMisc.explicitlySelected = z;
            if (in.readInt() != 0) {
                z = true;
            } else {
                z = false;
            }
            networkMisc.acceptUnvalidated = z;
            networkMisc.subscriberId = in.readString();
            if (in.readInt() == 0) {
                z2 = false;
            }
            networkMisc.provisioningNotificationDisabled = z2;
            return networkMisc;
        }

        public NetworkMisc[] newArray(int size) {
            return new NetworkMisc[size];
        }
    };
    public boolean acceptUnvalidated;
    public boolean allowBypass;
    public boolean explicitlySelected;
    public boolean provisioningNotificationDisabled;
    public String subscriberId;

    public NetworkMisc(NetworkMisc nm) {
        if (nm != null) {
            this.allowBypass = nm.allowBypass;
            this.explicitlySelected = nm.explicitlySelected;
            this.acceptUnvalidated = nm.acceptUnvalidated;
            this.subscriberId = nm.subscriberId;
            this.provisioningNotificationDisabled = nm.provisioningNotificationDisabled;
        }
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        int i;
        int i2 = 1;
        out.writeInt(this.allowBypass ? 1 : 0);
        if (this.explicitlySelected) {
            i = 1;
        } else {
            i = 0;
        }
        out.writeInt(i);
        if (this.acceptUnvalidated) {
            i = 1;
        } else {
            i = 0;
        }
        out.writeInt(i);
        out.writeString(this.subscriberId);
        if (!this.provisioningNotificationDisabled) {
            i2 = 0;
        }
        out.writeInt(i2);
    }
}
