package android.app.admin;

import android.os.Parcel;
import android.os.ParcelFormatException;
import android.os.Parcelable;

public abstract class NetworkEvent implements Parcelable {
    public static final Parcelable.Creator<NetworkEvent> CREATOR = new Parcelable.Creator<NetworkEvent>() {
        public NetworkEvent createFromParcel(Parcel in) {
            int initialPosition = in.dataPosition();
            int parcelToken = in.readInt();
            in.setDataPosition(initialPosition);
            switch (parcelToken) {
                case 1:
                    return DnsEvent.CREATOR.createFromParcel(in);
                case 2:
                    return ConnectEvent.CREATOR.createFromParcel(in);
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
    long mId;
    String mPackageName;
    long mTimestamp;

    public abstract void writeToParcel(Parcel parcel, int i);

    NetworkEvent() {
    }

    NetworkEvent(String packageName, long timestamp) {
        this.mPackageName = packageName;
        this.mTimestamp = timestamp;
    }

    public String getPackageName() {
        return this.mPackageName;
    }

    public long getTimestamp() {
        return this.mTimestamp;
    }

    public void setId(long id) {
        this.mId = id;
    }

    public long getId() {
        return this.mId;
    }

    public int describeContents() {
        return 0;
    }
}
