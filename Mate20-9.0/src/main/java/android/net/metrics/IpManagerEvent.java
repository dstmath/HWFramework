package android.net.metrics;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.SparseArray;
import com.android.internal.util.MessageUtils;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public final class IpManagerEvent implements Parcelable {
    public static final int COMPLETE_LIFECYCLE = 3;
    public static final Parcelable.Creator<IpManagerEvent> CREATOR = new Parcelable.Creator<IpManagerEvent>() {
        public IpManagerEvent createFromParcel(Parcel in) {
            return new IpManagerEvent(in);
        }

        public IpManagerEvent[] newArray(int size) {
            return new IpManagerEvent[size];
        }
    };
    public static final int ERROR_INTERFACE_NOT_FOUND = 8;
    public static final int ERROR_INVALID_PROVISIONING = 7;
    public static final int ERROR_STARTING_IPREACHABILITYMONITOR = 6;
    public static final int ERROR_STARTING_IPV4 = 4;
    public static final int ERROR_STARTING_IPV6 = 5;
    public static final int PROVISIONING_FAIL = 2;
    public static final int PROVISIONING_OK = 1;
    public final long durationMs;
    public final int eventType;

    static final class Decoder {
        static final SparseArray<String> constants = MessageUtils.findMessageNames(new Class[]{IpManagerEvent.class}, new String[]{"PROVISIONING_", "COMPLETE_", "ERROR_"});

        Decoder() {
        }
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface EventType {
    }

    public IpManagerEvent(int eventType2, long duration) {
        this.eventType = eventType2;
        this.durationMs = duration;
    }

    private IpManagerEvent(Parcel in) {
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
        return String.format("IpManagerEvent(%s, %dms)", new Object[]{Decoder.constants.get(this.eventType), Long.valueOf(this.durationMs)});
    }
}
