package android.telecom;

import android.bluetooth.BluetoothDevice;
import android.os.Parcel;
import android.os.Parcelable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

public final class CallAudioState implements Parcelable {
    public static final Parcelable.Creator<CallAudioState> CREATOR = new Parcelable.Creator<CallAudioState>() {
        public CallAudioState createFromParcel(Parcel source) {
            boolean isMuted = source.readByte() != 0;
            int route = source.readInt();
            int supportedRouteMask = source.readInt();
            List<BluetoothDevice> supportedBluetoothDevices = new ArrayList<>();
            source.readParcelableList(supportedBluetoothDevices, ClassLoader.getSystemClassLoader());
            CallAudioState callAudioState = new CallAudioState(isMuted, route, supportedRouteMask, (BluetoothDevice) source.readParcelable(ClassLoader.getSystemClassLoader()), supportedBluetoothDevices);
            return callAudioState;
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
    private final BluetoothDevice activeBluetoothDevice;
    private final boolean isMuted;
    private final int route;
    private final Collection<BluetoothDevice> supportedBluetoothDevices;
    private final int supportedRouteMask;

    @Retention(RetentionPolicy.SOURCE)
    public @interface CallAudioRoute {
    }

    public CallAudioState(boolean muted, int route2, int supportedRouteMask2) {
        this(muted, route2, supportedRouteMask2, null, Collections.emptyList());
    }

    public CallAudioState(boolean isMuted2, int route2, int supportedRouteMask2, BluetoothDevice activeBluetoothDevice2, Collection<BluetoothDevice> supportedBluetoothDevices2) {
        this.isMuted = isMuted2;
        this.route = route2;
        this.supportedRouteMask = supportedRouteMask2;
        this.activeBluetoothDevice = activeBluetoothDevice2;
        this.supportedBluetoothDevices = supportedBluetoothDevices2;
    }

    public CallAudioState(CallAudioState state) {
        this.isMuted = state.isMuted();
        this.route = state.getRoute();
        this.supportedRouteMask = state.getSupportedRouteMask();
        this.activeBluetoothDevice = state.activeBluetoothDevice;
        this.supportedBluetoothDevices = state.getSupportedBluetoothDevices();
    }

    public CallAudioState(AudioState state) {
        this.isMuted = state.isMuted();
        this.route = state.getRoute();
        this.supportedRouteMask = state.getSupportedRouteMask();
        this.activeBluetoothDevice = null;
        this.supportedBluetoothDevices = Collections.emptyList();
    }

    public boolean equals(Object obj) {
        boolean z = false;
        if (obj == null || !(obj instanceof CallAudioState)) {
            return false;
        }
        CallAudioState state = (CallAudioState) obj;
        if (this.supportedBluetoothDevices.size() != state.supportedBluetoothDevices.size()) {
            return false;
        }
        for (BluetoothDevice device : this.supportedBluetoothDevices) {
            if (!state.supportedBluetoothDevices.contains(device)) {
                return false;
            }
        }
        if (Objects.equals(this.activeBluetoothDevice, state.activeBluetoothDevice) && isMuted() == state.isMuted() && getRoute() == state.getRoute() && getSupportedRouteMask() == state.getSupportedRouteMask()) {
            z = true;
        }
        return z;
    }

    public String toString() {
        return String.format(Locale.US, "[AudioState isMuted: %b, route: %s, supportedRouteMask: %s, activeBluetoothDevice: [%s], supportedBluetoothDevices: [%s]]", new Object[]{Boolean.valueOf(this.isMuted), audioRouteToString(this.route), audioRouteToString(this.supportedRouteMask), this.activeBluetoothDevice, (String) this.supportedBluetoothDevices.stream().map($$Lambda$cyYWqCYT05eM23eLVm4oQ5DrYjw.INSTANCE).collect(Collectors.joining(", "))});
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

    public BluetoothDevice getActiveBluetoothDevice() {
        return this.activeBluetoothDevice;
    }

    public Collection<BluetoothDevice> getSupportedBluetoothDevices() {
        return this.supportedBluetoothDevices;
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

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel destination, int flags) {
        destination.writeByte(this.isMuted ? (byte) 1 : 0);
        destination.writeInt(this.route);
        destination.writeInt(this.supportedRouteMask);
        destination.writeParcelable(this.activeBluetoothDevice, 0);
        destination.writeParcelableList(new ArrayList(this.supportedBluetoothDevices), 0);
    }

    private static void listAppend(StringBuffer buffer, String str) {
        if (buffer.length() > 0) {
            buffer.append(", ");
        }
        buffer.append(str);
    }
}
