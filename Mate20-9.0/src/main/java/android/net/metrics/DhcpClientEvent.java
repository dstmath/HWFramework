package android.net.metrics;

import android.os.Parcel;
import android.os.Parcelable;

public final class DhcpClientEvent implements Parcelable {
    public static final Parcelable.Creator<DhcpClientEvent> CREATOR = new Parcelable.Creator<DhcpClientEvent>() {
        public DhcpClientEvent createFromParcel(Parcel in) {
            return new DhcpClientEvent(in);
        }

        public DhcpClientEvent[] newArray(int size) {
            return new DhcpClientEvent[size];
        }
    };
    public static final String INITIAL_BOUND = "InitialBoundState";
    public static final String RENEWING_BOUND = "RenewingBoundState";
    public final int durationMs;
    public final String msg;

    public DhcpClientEvent(String msg2, int durationMs2) {
        this.msg = msg2;
        this.durationMs = durationMs2;
    }

    private DhcpClientEvent(Parcel in) {
        this.msg = in.readString();
        this.durationMs = in.readInt();
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(this.msg);
        out.writeInt(this.durationMs);
    }

    public int describeContents() {
        return 0;
    }

    public String toString() {
        return String.format("DhcpClientEvent(%s, %dms)", new Object[]{this.msg, Integer.valueOf(this.durationMs)});
    }
}
