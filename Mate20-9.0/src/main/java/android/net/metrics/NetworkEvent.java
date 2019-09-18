package android.net.metrics;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.SparseArray;
import com.android.internal.util.MessageUtils;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public final class NetworkEvent implements Parcelable {
    public static final Parcelable.Creator<NetworkEvent> CREATOR = new Parcelable.Creator<NetworkEvent>() {
        public NetworkEvent createFromParcel(Parcel in) {
            return new NetworkEvent(in);
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

    static final class Decoder {
        static final SparseArray<String> constants = MessageUtils.findMessageNames(new Class[]{NetworkEvent.class}, new String[]{"NETWORK_"});

        Decoder() {
        }
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface EventType {
    }

    public NetworkEvent(int eventType2, long durationMs2) {
        this.eventType = eventType2;
        this.durationMs = durationMs2;
    }

    public NetworkEvent(int eventType2) {
        this(eventType2, 0);
    }

    private NetworkEvent(Parcel in) {
        this.eventType = in.readInt();
        this.durationMs = in.readLong();
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(this.eventType);
        out.writeLong(this.durationMs);
    }

    public int describeContents() {
        return 0;
    }

    public String toString() {
        return String.format("NetworkEvent(%s, %dms)", new Object[]{Decoder.constants.get(this.eventType), Long.valueOf(this.durationMs)});
    }
}
