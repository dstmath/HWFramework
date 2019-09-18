package android.telecom;

import android.annotation.SystemApi;
import android.os.Parcel;
import android.os.Parcelable;
import java.util.Locale;

@SystemApi
@Deprecated
public class AudioState implements Parcelable {
    public static final Parcelable.Creator<AudioState> CREATOR = new Parcelable.Creator<AudioState>() {
        public AudioState createFromParcel(Parcel source) {
            return new AudioState(source.readByte() != 0, source.readInt(), source.readInt());
        }

        public AudioState[] newArray(int size) {
            return new AudioState[size];
        }
    };
    private static final int ROUTE_ALL = 15;
    public static final int ROUTE_BLUETOOTH = 2;
    public static final int ROUTE_EARPIECE = 1;
    public static final int ROUTE_SPEAKER = 8;
    public static final int ROUTE_WIRED_HEADSET = 4;
    public static final int ROUTE_WIRED_OR_EARPIECE = 5;
    private final boolean isMuted;
    private final int route;
    private final int supportedRouteMask;

    public AudioState(boolean muted, int route2, int supportedRouteMask2) {
        this.isMuted = muted;
        this.route = route2;
        this.supportedRouteMask = supportedRouteMask2;
    }

    public AudioState(AudioState state) {
        this.isMuted = state.isMuted();
        this.route = state.getRoute();
        this.supportedRouteMask = state.getSupportedRouteMask();
    }

    public AudioState(CallAudioState state) {
        this.isMuted = state.isMuted();
        this.route = state.getRoute();
        this.supportedRouteMask = state.getSupportedRouteMask();
    }

    public boolean equals(Object obj) {
        boolean z = false;
        if (obj == null || !(obj instanceof AudioState)) {
            return false;
        }
        AudioState state = (AudioState) obj;
        if (isMuted() == state.isMuted() && getRoute() == state.getRoute() && getSupportedRouteMask() == state.getSupportedRouteMask()) {
            z = true;
        }
        return z;
    }

    public String toString() {
        return String.format(Locale.US, "[AudioState isMuted: %b, route: %s, supportedRouteMask: %s]", new Object[]{Boolean.valueOf(this.isMuted), audioRouteToString(this.route), audioRouteToString(this.supportedRouteMask)});
    }

    public static String audioRouteToString(int route2) {
        if (route2 == 0 || (route2 & -16) != 0) {
            return "UNKNOWN";
        }
        StringBuffer buffer = new StringBuffer();
        if ((route2 & 1) == 1) {
            listAppend(buffer, "EARPIECE");
        }
        if ((route2 & 2) == 2) {
            listAppend(buffer, "BLUETOOTH");
        }
        if ((route2 & 4) == 4) {
            listAppend(buffer, "WIRED_HEADSET");
        }
        if ((route2 & 8) == 8) {
            listAppend(buffer, "SPEAKER");
        }
        return buffer.toString();
    }

    private static void listAppend(StringBuffer buffer, String str) {
        if (buffer.length() > 0) {
            buffer.append(", ");
        }
        buffer.append(str);
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel destination, int flags) {
        destination.writeByte(this.isMuted ? (byte) 1 : 0);
        destination.writeInt(this.route);
        destination.writeInt(this.supportedRouteMask);
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
}
