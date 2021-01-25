package android.net.metrics;

import android.annotation.SystemApi;
import android.annotation.UnsupportedAppUsage;
import android.net.metrics.IpConnectivityLog;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

@SystemApi
public final class DhcpClientEvent implements IpConnectivityLog.Event {
    public static final Parcelable.Creator<DhcpClientEvent> CREATOR = new Parcelable.Creator<DhcpClientEvent>() {
        /* class android.net.metrics.DhcpClientEvent.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public DhcpClientEvent createFromParcel(Parcel in) {
            return new DhcpClientEvent(in);
        }

        @Override // android.os.Parcelable.Creator
        public DhcpClientEvent[] newArray(int size) {
            return new DhcpClientEvent[size];
        }
    };
    public final int durationMs;
    public final String msg;

    @UnsupportedAppUsage
    private DhcpClientEvent(String msg2, int durationMs2) {
        this.msg = msg2;
        this.durationMs = durationMs2;
    }

    private DhcpClientEvent(Parcel in) {
        this.msg = in.readString();
        this.durationMs = in.readInt();
    }

    public static final class Builder {
        private int mDurationMs;
        private String mMsg;

        public Builder setMsg(String msg) {
            this.mMsg = msg;
            return this;
        }

        public Builder setDurationMs(int durationMs) {
            this.mDurationMs = durationMs;
            return this;
        }

        public DhcpClientEvent build() {
            return new DhcpClientEvent(this.mMsg, this.mDurationMs);
        }
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(this.msg);
        out.writeInt(this.durationMs);
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    public String toString() {
        return String.format("DhcpClientEvent(%s, %dms)", this.msg, Integer.valueOf(this.durationMs));
    }

    public boolean equals(Object obj) {
        if (obj == null || !obj.getClass().equals(DhcpClientEvent.class)) {
            return false;
        }
        DhcpClientEvent other = (DhcpClientEvent) obj;
        if (!TextUtils.equals(this.msg, other.msg) || this.durationMs != other.durationMs) {
            return false;
        }
        return true;
    }
}
