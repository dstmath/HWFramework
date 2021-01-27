package android.telecom;

import android.bluetooth.BluetoothDevice;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import com.android.internal.telephony.IccCardConstants;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class CallAudioState implements Parcelable {
    public static final Parcelable.Creator<CallAudioState> CREATOR = new Parcelable.Creator<CallAudioState>() {
        /* class android.telecom.CallAudioState.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public CallAudioState createFromParcel(Parcel source) {
            boolean isMuted = source.readByte() != 0;
            int route = source.readInt();
            int supportedRouteMask = source.readInt();
            BluetoothDevice activeBluetoothDevice = (BluetoothDevice) source.readParcelable(ClassLoader.getSystemClassLoader());
            ArrayList arrayList = new ArrayList();
            source.readParcelableList(arrayList, ClassLoader.getSystemClassLoader());
            return new CallAudioState(isMuted, route, supportedRouteMask, activeBluetoothDevice, arrayList);
        }

        @Override // android.os.Parcelable.Creator
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
            return true;
        }
        return false;
    }

    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x0051: APUT  
      (r2v1 java.lang.Object[])
      (3 ??[int, float, short, byte, char])
      (wrap: java.lang.String : 0x004c: INVOKE  (r3v8 java.lang.String) = (r5v0 'this' android.telecom.CallAudioState A[IMMUTABLE_TYPE, THIS]), (r3v7 java.lang.String) type: DIRECT call: android.telecom.CallAudioState.getPartAddress(java.lang.String):java.lang.String)
     */
    public String toString() {
        String bluetoothDeviceList = (String) this.supportedBluetoothDevices.stream().map($$Lambda$cyYWqCYT05eM23eLVm4oQ5DrYjw.INSTANCE).map(new Function() {
            /* class android.telecom.$$Lambda$CallAudioState$UVwAP4duMWl7CzxiFaUgAscTPk */

            @Override // java.util.function.Function
            public final Object apply(Object obj) {
                return CallAudioState.this.lambda$toString$0$CallAudioState((String) obj);
            }
        }).collect(Collectors.joining(", "));
        Locale locale = Locale.US;
        Object[] objArr = new Object[5];
        objArr[0] = Boolean.valueOf(this.isMuted);
        objArr[1] = audioRouteToString(this.route);
        objArr[2] = audioRouteToString(this.supportedRouteMask);
        BluetoothDevice bluetoothDevice = this.activeBluetoothDevice;
        objArr[3] = lambda$toString$0$CallAudioState(bluetoothDevice == null ? "" : bluetoothDevice.getAddress());
        objArr[4] = bluetoothDeviceList;
        return String.format(locale, "[AudioState isMuted: %b, route: %s, supportedRouteMask: %s, activeBluetoothDevice: [%s], supportedBluetoothDevices: [%s]]", objArr);
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
            return IccCardConstants.INTENT_VALUE_ICC_UNKNOWN;
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

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
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

    /* access modifiers changed from: private */
    /* renamed from: getPartAddress */
    public String lambda$toString$0$CallAudioState(String address) {
        if (TextUtils.isEmpty(address)) {
            return "";
        }
        return address.substring(0, address.length() / 2) + ":**:**:**";
    }
}
