package android.net.metrics;

import android.annotation.SystemApi;
import android.net.metrics.IpConnectivityLog;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.SparseArray;
import com.android.internal.util.MessageUtils;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@SystemApi
public final class IpManagerEvent implements IpConnectivityLog.Event {
    public static final int COMPLETE_LIFECYCLE = 3;
    public static final Parcelable.Creator<IpManagerEvent> CREATOR = new Parcelable.Creator<IpManagerEvent>() {
        /* class android.net.metrics.IpManagerEvent.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public IpManagerEvent createFromParcel(Parcel in) {
            return new IpManagerEvent(in);
        }

        @Override // android.os.Parcelable.Creator
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

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(this.eventType);
        out.writeLong(this.durationMs);
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    public String toString() {
        return String.format("IpManagerEvent(%s, %dms)", Decoder.constants.get(this.eventType), Long.valueOf(this.durationMs));
    }

    public boolean equals(Object obj) {
        if (obj == null || !obj.getClass().equals(IpManagerEvent.class)) {
            return false;
        }
        IpManagerEvent other = (IpManagerEvent) obj;
        if (this.eventType == other.eventType && this.durationMs == other.durationMs) {
            return true;
        }
        return false;
    }

    static final class Decoder {
        static final SparseArray<String> constants = MessageUtils.findMessageNames(new Class[]{IpManagerEvent.class}, new String[]{"PROVISIONING_", "COMPLETE_", "ERROR_"});

        Decoder() {
        }
    }
}
