package android.net.metrics;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.util.SparseArray;
import com.android.internal.util.MessageUtils;

public final class NetworkEvent implements Parcelable {
    public static final Creator<NetworkEvent> CREATOR = new Creator<NetworkEvent>() {
        public NetworkEvent createFromParcel(Parcel in) {
            return new NetworkEvent(in, null);
        }

        public NetworkEvent[] newArray(int size) {
            return new NetworkEvent[size];
        }
    };
    public static final int NETWORK_CAPTIVE_PORTAL_FOUND = 4;
    public static final int NETWORK_CONNECTED = 1;
    public static final int NETWORK_DISCONNECTED = 7;
    public static final int NETWORK_FIRST_VALIDATION_PORTAL_FOUND = 10;
    public static final int NETWORK_FIRST_VALIDATION_SUCCESS = 8;
    public static final int NETWORK_LINGER = 5;
    public static final int NETWORK_REVALIDATION_PORTAL_FOUND = 11;
    public static final int NETWORK_REVALIDATION_SUCCESS = 9;
    public static final int NETWORK_UNLINGER = 6;
    public static final int NETWORK_VALIDATED = 2;
    public static final int NETWORK_VALIDATION_FAILED = 3;
    public final long durationMs;
    public final int eventType;
    public final int netId;

    static final class Decoder {
        static final SparseArray<String> constants = MessageUtils.findMessageNames(new Class[]{NetworkEvent.class}, new String[]{"NETWORK_"});

        Decoder() {
        }
    }

    public NetworkEvent(int netId, int eventType, long durationMs) {
        this.netId = netId;
        this.eventType = eventType;
        this.durationMs = durationMs;
    }

    public NetworkEvent(int netId, int eventType) {
        this(netId, eventType, 0);
    }

    private NetworkEvent(Parcel in) {
        this.netId = in.readInt();
        this.eventType = in.readInt();
        this.durationMs = in.readLong();
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(this.netId);
        out.writeInt(this.eventType);
        out.writeLong(this.durationMs);
    }

    public int describeContents() {
        return 0;
    }

    public String toString() {
        return String.format("NetworkEvent(%d, %s, %dms)", new Object[]{Integer.valueOf(this.netId), Decoder.constants.get(this.eventType), Long.valueOf(this.durationMs)});
    }
}
