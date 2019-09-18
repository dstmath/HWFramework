package android.net.metrics;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.SparseArray;
import com.android.internal.util.MessageUtils;

public final class IpReachabilityEvent implements Parcelable {
    public static final Parcelable.Creator<IpReachabilityEvent> CREATOR = new Parcelable.Creator<IpReachabilityEvent>() {
        public IpReachabilityEvent createFromParcel(Parcel in) {
            return new IpReachabilityEvent(in);
        }

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

    static final class Decoder {
        static final SparseArray<String> constants = MessageUtils.findMessageNames(new Class[]{IpReachabilityEvent.class}, new String[]{"PROBE", "PROVISIONING_", "NUD_"});

        Decoder() {
        }
    }

    public IpReachabilityEvent(int eventType2) {
        this.eventType = eventType2;
    }

    private IpReachabilityEvent(Parcel in) {
        this.eventType = in.readInt();
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(this.eventType);
    }

    public int describeContents() {
        return 0;
    }

    public static int nudFailureEventType(boolean isFromProbe, boolean isProvisioningLost) {
        if (isFromProbe) {
            return isProvisioningLost ? 768 : 512;
        }
        return isProvisioningLost ? 1280 : 1024;
    }

    public String toString() {
        return String.format("IpReachabilityEvent(%s:%02x)", new Object[]{Decoder.constants.get(this.eventType & 65280), Integer.valueOf(this.eventType & 255)});
    }
}
