package android.telecom;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import com.android.internal.telephony.IccCardConstants;
import java.util.Locale;

public final class CallAudioState implements Parcelable {
    public static final Creator<CallAudioState> CREATOR = new Creator<CallAudioState>() {
        public CallAudioState createFromParcel(Parcel source) {
            return new CallAudioState(source.readByte() != (byte) 0, source.readInt(), source.readInt());
        }

        public CallAudioState[] newArray(int size) {
            return new CallAudioState[size];
        }
    };
    public static final int ROUTE_ALL = 15;
    public static final int ROUTE_BLUETOOTH = 2;
    public static final int ROUTE_EARPIECE = 1;
    public static final int ROUTE_SPEAKER = 8;
    public static final int ROUTE_WIRED_HEADSET = 4;
    public static final int ROUTE_WIRED_OR_EARPIECE = 5;
    private final boolean isMuted;
    private final int route;
    private final int supportedRouteMask;

    public CallAudioState(boolean muted, int route, int supportedRouteMask) {
        this.isMuted = muted;
        this.route = route;
        this.supportedRouteMask = supportedRouteMask;
    }

    public CallAudioState(CallAudioState state) {
        this.isMuted = state.isMuted();
        this.route = state.getRoute();
        this.supportedRouteMask = state.getSupportedRouteMask();
    }

    public CallAudioState(AudioState state) {
        this.isMuted = state.isMuted();
        this.route = state.getRoute();
        this.supportedRouteMask = state.getSupportedRouteMask();
    }

    public boolean equals(Object obj) {
        boolean z = false;
        if (obj == null || !(obj instanceof CallAudioState)) {
            return false;
        }
        CallAudioState state = (CallAudioState) obj;
        if (isMuted() == state.isMuted() && getRoute() == state.getRoute() && getSupportedRouteMask() == state.getSupportedRouteMask()) {
            z = true;
        }
        return z;
    }

    public String toString() {
        return String.format(Locale.US, "[AudioState isMuted: %b, route: %s, supportedRouteMask: %s]", new Object[]{Boolean.valueOf(this.isMuted), audioRouteToString(this.route), audioRouteToString(this.supportedRouteMask)});
    }

    public boolean isMuted() {
        return this.isMuted;
    }

    public int getRoute() {
        return this.route;
    }

    public int getSupportedRouteMask() {
        return this.supportedRouteMask;
    }

    public static String audioRouteToString(int route) {
        if (route == 0 || (route & -16) != 0) {
            return IccCardConstants.INTENT_VALUE_ICC_UNKNOWN;
        }
        StringBuffer buffer = new StringBuffer();
        if ((route & 1) == 1) {
            listAppend(buffer, "EARPIECE");
        }
        if ((route & 2) == 2) {
            listAppend(buffer, "BLUETOOTH");
        }
        if ((route & 4) == 4) {
            listAppend(buffer, "WIRED_HEADSET");
        }
        if ((route & 8) == 8) {
            listAppend(buffer, "SPEAKER");
        }
        return buffer.toString();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel destination, int flags) {
        destination.writeByte((byte) (this.isMuted ? 1 : 0));
        destination.writeInt(this.route);
        destination.writeInt(this.supportedRouteMask);
    }

    private static void listAppend(StringBuffer buffer, String str) {
        if (buffer.length() > 0) {
            buffer.append(", ");
        }
        buffer.append(str);
    }
}
