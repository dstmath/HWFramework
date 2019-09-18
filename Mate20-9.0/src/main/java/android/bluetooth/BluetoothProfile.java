package android.bluetooth;

import android.annotation.SystemApi;
import java.util.List;

public interface BluetoothProfile {
    public static final int A2DP = 2;
    public static final int A2DP_SINK = 11;
    public static final int AVRCP = 13;
    public static final int AVRCP_CONTROLLER = 12;
    public static final String EXTRA_PREVIOUS_STATE = "android.bluetooth.profile.extra.PREVIOUS_STATE";
    public static final String EXTRA_STATE = "android.bluetooth.profile.extra.STATE";
    public static final int GATT = 7;
    public static final int GATT_SERVER = 8;
    public static final int HEADSET = 1;
    public static final int HEADSET_CLIENT = 16;
    public static final int HEALTH = 3;
    public static final int HEARING_AID = 21;
    public static final int HID_DEVICE = 19;
    public static final int HID_HOST = 4;
    public static final int MAP = 9;
    public static final int MAP_CLIENT = 18;
    public static final int MAX_PROFILE_ID = 21;
    public static final int OPP = 20;
    public static final int PAN = 5;
    public static final int PBAP = 6;
    public static final int PBAP_CLIENT = 17;
    public static final int PRIORITY_AUTO_CONNECT = 1000;
    @SystemApi
    public static final int PRIORITY_OFF = 0;
    @SystemApi
    public static final int PRIORITY_ON = 100;
    public static final int PRIORITY_UNDEFINED = -1;
    public static final int SAP = 10;
    public static final int STATE_CONNECTED = 2;
    public static final int STATE_CONNECTING = 1;
    public static final int STATE_DISCONNECTED = 0;
    public static final int STATE_DISCONNECTING = 3;

    public interface ServiceListener {
        void onServiceConnected(int i, BluetoothProfile bluetoothProfile);

        void onServiceDisconnected(int i);
    }

    List<BluetoothDevice> getConnectedDevices();

    int getConnectionState(BluetoothDevice bluetoothDevice);

    List<BluetoothDevice> getDevicesMatchingConnectionStates(int[] iArr);

    static String getConnectionStateName(int connectionState) {
        switch (connectionState) {
            case 0:
                return "STATE_DISCONNECTED";
            case 1:
                return "STATE_CONNECTING";
            case 2:
                return "STATE_CONNECTED";
            case 3:
                return "STATE_DISCONNECTING";
            default:
                return "STATE_UNKNOWN";
        }
    }
}
