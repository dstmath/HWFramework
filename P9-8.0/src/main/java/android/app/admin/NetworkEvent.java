package android.app.admin;

import android.os.Parcel;
import android.os.ParcelFormatException;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public abstract class NetworkEvent implements Parcelable {
    public static final Creator<NetworkEvent> CREATOR = new Creator<NetworkEvent>() {
        public NetworkEvent createFromParcel(Parcel in) {
            int initialPosition = in.dataPosition();
            int parcelToken = in.readInt();
            in.setDataPosition(initialPosition);
            switch (parcelToken) {
                case 1:
                    return (NetworkEvent) DnsEvent.CREATOR.createFromParcel(in);
                case 2:
                    return (NetworkEvent) ConnectEvent.CREATOR.createFromParcel(in);
                default:
                    throw new ParcelFormatException("Unexpected NetworkEvent token in parcel: " + parcelToken);
            }
        }

        public NetworkEvent[] newArray(int size) {
            return new NetworkEvent[size];
        }
    };
    static final int PARCEL_TOKEN_CONNECT_EVENT = 2;
    static final int PARCEL_TOKEN_DNS_EVENT = 1;
    String packageName;
    long timestamp;

    public abstract void writeToParcel(Parcel parcel, int i);

    NetworkEvent() {
    }

    NetworkEvent(String packageName, long timestamp) {
        this.packageName = packageName;
        this.timestamp = timestamp;
    }

    public String getPackageName() {
        return this.packageName;
    }

    public long getTimestamp() {
        return this.timestamp;
    }

    public int describeContents() {
        return 0;
    }
}
