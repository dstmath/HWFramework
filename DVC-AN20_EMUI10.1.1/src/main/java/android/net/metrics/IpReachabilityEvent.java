package android.net.metrics;

import android.annotation.SystemApi;
import android.net.metrics.IpConnectivityLog;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.SparseArray;
import com.android.internal.util.MessageUtils;

@SystemApi
public final class IpReachabilityEvent implements IpConnectivityLog.Event {
    public static final Parcelable.Creator<IpReachabilityEvent> CREATOR = new Parcelable.Creator<IpReachabilityEvent>() {
        /* class android.net.metrics.IpReachabilityEvent.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public IpReachabilityEvent createFromParcel(Parcel in) {
            return new IpReachabilityEvent(in);
        }

        @Override // android.os.Parcelable.Creator
        public IpReachabilityEvent[] newArray(int size) {
            return new IpReachabilityEvent[size];
        }
    };
    public static final int NUD_FAILED = 512;
    public static final int NUD_FAILED_ORGANIC = 1024;
    public static final int PROBE = 256;
    public static final int PROVISIONING_LOST = 768;
    public static final int PROVISIONING_LOST_ORGANIC = 1280;
    public final int eventType;

    public IpReachabilityEvent(int eventType2) {
        this.eventType = eventType2;
    }

    private IpReachabilityEvent(Parcel in) {
        this.eventType = in.readInt();
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(this.eventType);
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    public String toString() {
        int i = this.eventType;
        return String.format("IpReachabilityEvent(%s:%02x)", Decoder.constants.get(65280 & i), Integer.valueOf(i & 255));
    }

    public boolean equals(Object obj) {
        if (obj == null || !obj.getClass().equals(IpReachabilityEvent.class) || this.eventType != ((IpReachabilityEvent) obj).eventType) {
            return false;
        }
        return true;
    }

    static final class Decoder {
        static final SparseArray<String> constants = MessageUtils.findMessageNames(new Class[]{IpReachabilityEvent.class}, new String[]{"PROBE", "PROVISIONING_", "NUD_"});

        Decoder() {
        }
    }
}
