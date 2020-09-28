package android.bluetooth;

import android.annotation.SystemApi;
import android.annotation.UnsupportedAppUsage;
import android.bluetooth.IBluetoothManagerCallback;
import android.content.Context;
import android.net.wifi.WifiEnterpriseConfig;
import android.os.Handler;
import android.os.Parcel;
import android.os.ParcelUuid;
import android.os.Parcelable;
import android.os.Process;
import android.os.RemoteException;
import android.util.Log;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.UUID;

public final class BluetoothDevice implements Parcelable {
    @SystemApi
    public static final int ACCESS_ALLOWED = 1;
    @SystemApi
    public static final int ACCESS_REJECTED = 2;
    @SystemApi
    public static final int ACCESS_UNKNOWN = 0;
    public static final String ACTION_ACL_CONNECTED = "android.bluetooth.device.action.ACL_CONNECTED";
    public static final String ACTION_ACL_DISCONNECTED = "android.bluetooth.device.action.ACL_DISCONNECTED";
    public static final String ACTION_ACL_DISCONNECT_REQUESTED = "android.bluetooth.device.action.ACL_DISCONNECT_REQUESTED";
    @UnsupportedAppUsage
    public static final String ACTION_ALIAS_CHANGED = "android.bluetooth.device.action.ALIAS_CHANGED";
    public static final String ACTION_BATTERY_LEVEL_CHANGED = "android.bluetooth.device.action.BATTERY_LEVEL_CHANGED";
    public static final String ACTION_BOND_STATE_CHANGED = "android.bluetooth.device.action.BOND_STATE_CHANGED";
    public static final String ACTION_CLASS_CHANGED = "android.bluetooth.device.action.CLASS_CHANGED";
    public static final String ACTION_CONNECTION_ACCESS_CANCEL = "android.bluetooth.device.action.CONNECTION_ACCESS_CANCEL";
    public static final String ACTION_CONNECTION_ACCESS_REPLY = "android.bluetooth.device.action.CONNECTION_ACCESS_REPLY";
    public static final String ACTION_CONNECTION_ACCESS_REQUEST = "android.bluetooth.device.action.CONNECTION_ACCESS_REQUEST";
    public static final String ACTION_FOUND = "android.bluetooth.device.action.FOUND";
    public static final String ACTION_MAS_INSTANCE = "android.bluetooth.device.action.MAS_INSTANCE";
    public static final String ACTION_NAME_CHANGED = "android.bluetooth.device.action.NAME_CHANGED";
    public static final String ACTION_NAME_FAILED = "android.bluetooth.device.action.NAME_FAILED";
    @UnsupportedAppUsage
    public static final String ACTION_PAIRING_CANCEL = "android.bluetooth.device.action.PAIRING_CANCEL";
    public static final String ACTION_PAIRING_REQUEST = "android.bluetooth.device.action.PAIRING_REQUEST";
    @UnsupportedAppUsage
    public static final String ACTION_SDP_RECORD = "android.bluetooth.device.action.SDP_RECORD";
    @SystemApi
    public static final String ACTION_SILENCE_MODE_CHANGED = "android.bluetooth.device.action.SILENCE_MODE_CHANGED";
    public static final String ACTION_UUID = "android.bluetooth.device.action.UUID";
    public static final int BATTERY_LEVEL_UNKNOWN = -1;
    public static final int BOND_BONDED = 12;
    public static final int BOND_BONDING = 11;
    public static final int BOND_NONE = 10;
    public static final int BOND_SUCCESS = 0;
    private static final int CODE_CLOUD_DEVICE = 1008;
    public static final int CONNECTION_ACCESS_NO = 2;
    public static final int CONNECTION_ACCESS_YES = 1;
    private static final int CONNECTION_STATE_CONNECTED = 1;
    private static final int CONNECTION_STATE_DISCONNECTED = 0;
    private static final int CONNECTION_STATE_ENCRYPTED_BREDR = 2;
    private static final int CONNECTION_STATE_ENCRYPTED_LE = 4;
    public static final Parcelable.Creator<BluetoothDevice> CREATOR = new Parcelable.Creator<BluetoothDevice>() {
        /* class android.bluetooth.BluetoothDevice.AnonymousClass2 */

        @Override // android.os.Parcelable.Creator
        public BluetoothDevice createFromParcel(Parcel in) {
            return new BluetoothDevice(in.readString());
        }

        @Override // android.os.Parcelable.Creator
        public BluetoothDevice[] newArray(int size) {
            return new BluetoothDevice[size];
        }
    };
    private static final boolean DBG = false;
    private static final String DESCRIPTOR = "android.bluetooth.IBluetooth";
    public static final int DEVICE_TYPE_CLASSIC = 1;
    public static final int DEVICE_TYPE_DUAL = 3;
    public static final int DEVICE_TYPE_LE = 2;
    public static final int DEVICE_TYPE_UNKNOWN = 0;
    public static final int ERROR = Integer.MIN_VALUE;
    public static final String EXTRA_ACCESS_REQUEST_TYPE = "android.bluetooth.device.extra.ACCESS_REQUEST_TYPE";
    public static final String EXTRA_ALWAYS_ALLOWED = "android.bluetooth.device.extra.ALWAYS_ALLOWED";
    public static final String EXTRA_BATTERY_LEVEL = "android.bluetooth.device.extra.BATTERY_LEVEL";
    public static final String EXTRA_BOND_STATE = "android.bluetooth.device.extra.BOND_STATE";
    public static final String EXTRA_CLASS = "android.bluetooth.device.extra.CLASS";
    public static final String EXTRA_CLASS_NAME = "android.bluetooth.device.extra.CLASS_NAME";
    public static final String EXTRA_CONNECTION_ACCESS_RESULT = "android.bluetooth.device.extra.CONNECTION_ACCESS_RESULT";
    public static final String EXTRA_DEVICE = "android.bluetooth.device.extra.DEVICE";
    public static final String EXTRA_MAS_INSTANCE = "android.bluetooth.device.extra.MAS_INSTANCE";
    public static final String EXTRA_NAME = "android.bluetooth.device.extra.NAME";
    public static final String EXTRA_PACKAGE_NAME = "android.bluetooth.device.extra.PACKAGE_NAME";
    public static final String EXTRA_PAIRING_KEY = "android.bluetooth.device.extra.PAIRING_KEY";
    public static final String EXTRA_PAIRING_VARIANT = "android.bluetooth.device.extra.PAIRING_VARIANT";
    public static final String EXTRA_PREVIOUS_BOND_STATE = "android.bluetooth.device.extra.PREVIOUS_BOND_STATE";
    @UnsupportedAppUsage
    public static final String EXTRA_REASON = "android.bluetooth.device.extra.REASON";
    public static final String EXTRA_RSSI = "android.bluetooth.device.extra.RSSI";
    public static final String EXTRA_SDP_RECORD = "android.bluetooth.device.extra.SDP_RECORD";
    @UnsupportedAppUsage
    public static final String EXTRA_SDP_SEARCH_STATUS = "android.bluetooth.device.extra.SDP_SEARCH_STATUS";
    public static final String EXTRA_UUID = "android.bluetooth.device.extra.UUID";
    @SystemApi
    public static final int METADATA_COMPANION_APP = 4;
    @SystemApi
    public static final int METADATA_ENHANCED_SETTINGS_UI_URI = 16;
    @SystemApi
    public static final int METADATA_HARDWARE_VERSION = 3;
    @SystemApi
    public static final int METADATA_IS_UNTETHERED_HEADSET = 6;
    @SystemApi
    public static final int METADATA_MAIN_ICON = 5;
    @SystemApi
    public static final int METADATA_MANUFACTURER_NAME = 0;
    @SystemApi
    public static final int METADATA_MAX_LENGTH = 2048;
    @SystemApi
    public static final int METADATA_MODEL_NAME = 1;
    @SystemApi
    public static final int METADATA_SOFTWARE_VERSION = 2;
    @SystemApi
    public static final int METADATA_UNTETHERED_CASE_BATTERY = 12;
    @SystemApi
    public static final int METADATA_UNTETHERED_CASE_CHARGING = 15;
    @SystemApi
    public static final int METADATA_UNTETHERED_CASE_ICON = 9;
    @SystemApi
    public static final int METADATA_UNTETHERED_LEFT_BATTERY = 10;
    @SystemApi
    public static final int METADATA_UNTETHERED_LEFT_CHARGING = 13;
    @SystemApi
    public static final int METADATA_UNTETHERED_LEFT_ICON = 7;
    @SystemApi
    public static final int METADATA_UNTETHERED_RIGHT_BATTERY = 11;
    @SystemApi
    public static final int METADATA_UNTETHERED_RIGHT_CHARGING = 14;
    @SystemApi
    public static final int METADATA_UNTETHERED_RIGHT_ICON = 8;
    public static final int PAIRING_VARIANT_CONSENT = 3;
    public static final int PAIRING_VARIANT_DISPLAY_PASSKEY = 4;
    public static final int PAIRING_VARIANT_DISPLAY_PIN = 5;
    public static final int PAIRING_VARIANT_OOB_CONSENT = 6;
    public static final int PAIRING_VARIANT_PASSKEY = 1;
    public static final int PAIRING_VARIANT_PASSKEY_CONFIRMATION = 2;
    public static final int PAIRING_VARIANT_PIN = 0;
    public static final int PAIRING_VARIANT_PIN_16_DIGITS = 7;
    public static final int PHY_LE_1M = 1;
    public static final int PHY_LE_1M_MASK = 1;
    public static final int PHY_LE_2M = 2;
    public static final int PHY_LE_2M_MASK = 2;
    public static final int PHY_LE_CODED = 3;
    public static final int PHY_LE_CODED_MASK = 4;
    public static final int PHY_OPTION_NO_PREFERRED = 0;
    public static final int PHY_OPTION_S2 = 1;
    public static final int PHY_OPTION_S8 = 2;
    public static final int REQUEST_TYPE_MESSAGE_ACCESS = 3;
    public static final int REQUEST_TYPE_PHONEBOOK_ACCESS = 2;
    public static final int REQUEST_TYPE_PROFILE_CONNECTION = 1;
    public static final int REQUEST_TYPE_SIM_ACCESS = 4;
    private static final String TAG = "BluetoothDevice";
    public static final int TRANSPORT_AUTO = 0;
    public static final int TRANSPORT_BREDR = 1;
    public static final int TRANSPORT_LE = 2;
    public static final int TRANSPORT_NEARBY_FASTCONNECT = 12;
    public static final int UNBOND_REASON_AUTH_CANCELED = 3;
    @UnsupportedAppUsage
    public static final int UNBOND_REASON_AUTH_FAILED = 1;
    @UnsupportedAppUsage
    public static final int UNBOND_REASON_AUTH_REJECTED = 2;
    @UnsupportedAppUsage
    public static final int UNBOND_REASON_AUTH_TIMEOUT = 6;
    @UnsupportedAppUsage
    public static final int UNBOND_REASON_DISCOVERY_IN_PROGRESS = 5;
    @UnsupportedAppUsage
    public static final int UNBOND_REASON_REMOTE_AUTH_CANCELED = 8;
    @UnsupportedAppUsage
    public static final int UNBOND_REASON_REMOTE_DEVICE_DOWN = 4;
    public static final int UNBOND_REASON_REMOVED = 9;
    @UnsupportedAppUsage
    public static final int UNBOND_REASON_REPEATED_ATTEMPTS = 7;
    private static volatile IBluetooth sService;
    static IBluetoothManagerCallback sStateChangeCallback = new IBluetoothManagerCallback.Stub() {
        /* class android.bluetooth.BluetoothDevice.AnonymousClass1 */

        @Override // android.bluetooth.IBluetoothManagerCallback
        public void onBluetoothServiceUp(IBluetooth bluetoothService) throws RemoteException {
            synchronized (BluetoothDevice.class) {
                if (BluetoothDevice.sService == null) {
                    IBluetooth unused = BluetoothDevice.sService = bluetoothService;
                }
            }
        }

        @Override // android.bluetooth.IBluetoothManagerCallback
        public void onBluetoothServiceDown() throws RemoteException {
            synchronized (BluetoothDevice.class) {
                IBluetooth unused = BluetoothDevice.sService = null;
            }
        }

        @Override // android.bluetooth.IBluetoothManagerCallback
        public void onBrEdrDown() {
        }
    };
    private final String mAddress;

    @UnsupportedAppUsage
    static IBluetooth getService() {
        synchronized (BluetoothDevice.class) {
            if (sService == null) {
                sService = BluetoothAdapter.getDefaultAdapter().getBluetoothService(sStateChangeCallback);
            }
        }
        return sService;
    }

    @UnsupportedAppUsage
    BluetoothDevice(String address) {
        getService();
        if (BluetoothAdapter.checkBluetoothAddress(address)) {
            this.mAddress = address;
            return;
        }
        throw new IllegalArgumentException(address + " is not a valid Bluetooth address");
    }

    public boolean equals(Object o) {
        if (o instanceof BluetoothDevice) {
            return this.mAddress.equals(((BluetoothDevice) o).getAddress());
        }
        return false;
    }

    public int hashCode() {
        return this.mAddress.hashCode();
    }

    public String toString() {
        return this.mAddress;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(this.mAddress);
    }

    public String getAddress() {
        return this.mAddress;
    }

    private String getPartAddress() {
        return getAddress().substring(0, getAddress().length() / 2) + ":**:**:**";
    }

    public String getName() {
        IBluetooth service = sService;
        if (service == null) {
            Log.e(TAG, "BT not enabled. Cannot get Remote Device name");
            return null;
        }
        try {
            String name = service.getRemoteName(this);
            if (name != null) {
                return name.replaceAll("[\\t\\n\\r]+", WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
            }
            return null;
        } catch (RemoteException e) {
            Log.e(TAG, "", e);
            return null;
        }
    }

    public int getType() {
        IBluetooth service = sService;
        if (service == null) {
            Log.e(TAG, "BT not enabled. Cannot get Remote Device type");
            return 0;
        }
        try {
            return service.getRemoteType(this);
        } catch (RemoteException e) {
            Log.e(TAG, "", e);
            return 0;
        }
    }

    @UnsupportedAppUsage
    public String getAlias() {
        IBluetooth service = sService;
        if (service == null) {
            Log.e(TAG, "BT not enabled. Cannot get Remote Device Alias");
            return null;
        }
        try {
            return service.getRemoteAlias(this);
        } catch (RemoteException e) {
            Log.e(TAG, "", e);
            return null;
        }
    }

    @UnsupportedAppUsage
    public boolean setAlias(String alias) {
        IBluetooth service = sService;
        if (service == null) {
            Log.e(TAG, "BT not enabled. Cannot set Remote Device name");
            return false;
        }
        try {
            return service.setRemoteAlias(this, alias);
        } catch (RemoteException e) {
            Log.e(TAG, "", e);
            return false;
        }
    }

    @UnsupportedAppUsage
    public String getAliasName() {
        String name = getAlias();
        if (name == null) {
            return getName();
        }
        return name;
    }

    @UnsupportedAppUsage
    public int getBatteryLevel() {
        IBluetooth service = sService;
        if (service == null) {
            Log.e(TAG, "Bluetooth disabled. Cannot get remote device battery level");
            return -1;
        }
        try {
            return service.getBatteryLevel(this);
        } catch (RemoteException e) {
            Log.e(TAG, "", e);
            return -1;
        }
    }

    public boolean createBond() {
        IBluetooth service = sService;
        if (service == null) {
            Log.e(TAG, "BT not enabled. Cannot create bond to Remote Device");
            return false;
        }
        try {
            Log.i(TAG, "createBond() for device " + getPartAddress() + " called by pid: " + Process.myPid() + " tid: " + Process.myTid());
            return service.createBond(this, 0);
        } catch (RemoteException e) {
            Log.e(TAG, "", e);
            return false;
        }
    }

    @UnsupportedAppUsage
    public boolean createBond(int transport) {
        IBluetooth service = sService;
        if (service == null) {
            Log.e(TAG, "BT not enabled. Cannot create bond to Remote Device");
            return false;
        } else if (transport < 0 || transport > 2) {
            throw new IllegalArgumentException(transport + " is not a valid Bluetooth transport");
        } else {
            try {
                Log.i(TAG, "createBond() for device " + getPartAddress() + " called by pid: " + Process.myPid() + " tid: " + Process.myTid());
                return service.createBond(this, transport);
            } catch (RemoteException e) {
                Log.e(TAG, "", e);
                return false;
            }
        }
    }

    public boolean createBondOutOfBand(int transport, OobData oobData) {
        IBluetooth service = sService;
        if (service == null) {
            Log.w(TAG, "BT not enabled, createBondOutOfBand failed");
            return false;
        }
        try {
            return service.createBondOutOfBand(this, transport, oobData);
        } catch (RemoteException e) {
            Log.e(TAG, "", e);
            return false;
        }
    }

    @UnsupportedAppUsage
    public boolean isBondingInitiatedLocally() {
        IBluetooth service = sService;
        if (service == null) {
            Log.w(TAG, "BT not enabled, isBondingInitiatedLocally failed");
            return false;
        }
        try {
            return service.isBondingInitiatedLocally(this);
        } catch (RemoteException e) {
            Log.e(TAG, "", e);
            return false;
        }
    }

    public boolean setDeviceOutOfBandData(byte[] hash, byte[] randomizer) {
        return false;
    }

    @SystemApi
    public boolean cancelBondProcess() {
        IBluetooth service = sService;
        if (service == null) {
            Log.e(TAG, "BT not enabled. Cannot cancel Remote Device bond");
            return false;
        }
        try {
            Log.i(TAG, "cancelBondProcess() for device " + getPartAddress() + " called by pid: " + Process.myPid() + " tid: " + Process.myTid());
            return service.cancelBondProcess(this);
        } catch (RemoteException e) {
            Log.e(TAG, "", e);
            return false;
        }
    }

    @SystemApi
    public boolean removeBond() {
        IBluetooth service = sService;
        if (service == null) {
            Log.e(TAG, "BT not enabled. Cannot remove Remote Device bond");
            return false;
        }
        try {
            Log.i(TAG, "removeBond() for device " + getPartAddress() + " called by pid: " + Process.myPid() + " tid: " + Process.myTid());
            return service.removeBond(this);
        } catch (RemoteException e) {
            Log.e(TAG, "", e);
            return false;
        }
    }

    public int getBondState() {
        IBluetooth service = sService;
        if (service == null) {
            Log.e(TAG, "BT not enabled. Cannot get bond state");
            return 10;
        }
        try {
            return service.getBondState(this);
        } catch (RemoteException e) {
            Log.e(TAG, "", e);
            return 10;
        }
    }

    @SystemApi
    public boolean isConnected() {
        IBluetooth service = sService;
        if (service == null) {
            return false;
        }
        try {
            if (service.getConnectionState(this) != 0) {
                return true;
            }
            return false;
        } catch (RemoteException e) {
            Log.e(TAG, "", e);
            return false;
        }
    }

    @SystemApi
    public boolean isEncrypted() {
        IBluetooth service = sService;
        if (service == null) {
            return false;
        }
        try {
            if (service.getConnectionState(this) > 1) {
                return true;
            }
            return false;
        } catch (RemoteException e) {
            Log.e(TAG, "", e);
            return false;
        }
    }

    public BluetoothClass getBluetoothClass() {
        IBluetooth service = sService;
        if (service == null) {
            Log.e(TAG, "BT not enabled. Cannot get Bluetooth Class");
            return null;
        }
        try {
            int classInt = service.getRemoteClass(this);
            if (classInt == -16777216) {
                return null;
            }
            return new BluetoothClass(classInt);
        } catch (RemoteException e) {
            Log.e(TAG, "", e);
            return null;
        }
    }

    public ParcelUuid[] getUuids() {
        IBluetooth service = sService;
        if (service == null || !isBluetoothEnabled()) {
            Log.e(TAG, "BT not enabled. Cannot get remote device Uuids");
            return null;
        }
        try {
            return service.getRemoteUuids(this);
        } catch (RemoteException e) {
            Log.e(TAG, "", e);
            return null;
        }
    }

    public boolean fetchUuidsWithSdp() {
        IBluetooth service = sService;
        if (service == null || !isBluetoothEnabled()) {
            Log.e(TAG, "BT not enabled. Cannot fetchUuidsWithSdp");
            return false;
        }
        try {
            return service.fetchRemoteUuids(this);
        } catch (RemoteException e) {
            Log.e(TAG, "", e);
            return false;
        }
    }

    public boolean sdpSearch(ParcelUuid uuid) {
        IBluetooth service = sService;
        if (service == null) {
            Log.e(TAG, "BT not enabled. Cannot query remote device sdp records");
            return false;
        }
        try {
            return service.sdpSearch(this, uuid);
        } catch (RemoteException e) {
            Log.e(TAG, "", e);
            return false;
        }
    }

    public boolean setPin(byte[] pin) {
        IBluetooth service = sService;
        if (service == null) {
            Log.e(TAG, "BT not enabled. Cannot set Remote Device pin");
            return false;
        }
        try {
            return service.setPin(this, true, pin.length, pin);
        } catch (RemoteException e) {
            Log.e(TAG, "", e);
            return false;
        }
    }

    @UnsupportedAppUsage
    public boolean setPasskey(int passkey) {
        return false;
    }

    public boolean setPairingConfirmation(boolean confirm) {
        IBluetooth service = sService;
        if (service == null) {
            Log.e(TAG, "BT not enabled. Cannot set pairing confirmation");
            return false;
        }
        try {
            return service.setPairingConfirmation(this, confirm);
        } catch (RemoteException e) {
            Log.e(TAG, "", e);
            return false;
        }
    }

    public boolean setRemoteOutOfBandData() {
        return false;
    }

    @UnsupportedAppUsage
    public boolean cancelPairingUserInput() {
        IBluetooth service = sService;
        if (service == null) {
            Log.e(TAG, "BT not enabled. Cannot create pairing user input");
            return false;
        }
        try {
            return service.cancelBondProcess(this);
        } catch (RemoteException e) {
            Log.e(TAG, "", e);
            return false;
        }
    }

    @UnsupportedAppUsage
    public boolean isBluetoothDock() {
        return false;
    }

    /* access modifiers changed from: package-private */
    public boolean isBluetoothEnabled() {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter == null || !adapter.isEnabled()) {
            return false;
        }
        return true;
    }

    @UnsupportedAppUsage
    public int getPhonebookAccessPermission() {
        IBluetooth service = sService;
        if (service == null) {
            return 0;
        }
        try {
            return service.getPhonebookAccessPermission(this);
        } catch (RemoteException e) {
            Log.e(TAG, "", e);
            return 0;
        }
    }

    @SystemApi
    public boolean setSilenceMode(boolean silence) {
        IBluetooth service = sService;
        if (service != null) {
            try {
                return service.setSilenceMode(this, silence);
            } catch (RemoteException e) {
                Log.e(TAG, "setSilenceMode fail", e);
                return false;
            }
        } else {
            throw new IllegalStateException("Bluetooth is not turned ON");
        }
    }

    @SystemApi
    public boolean isInSilenceMode() {
        IBluetooth service = sService;
        if (service != null) {
            try {
                return service.getSilenceMode(this);
            } catch (RemoteException e) {
                Log.e(TAG, "isInSilenceMode fail", e);
                return false;
            }
        } else {
            throw new IllegalStateException("Bluetooth is not turned ON");
        }
    }

    @SystemApi
    public boolean setPhonebookAccessPermission(int value) {
        IBluetooth service = sService;
        if (service == null) {
            return false;
        }
        try {
            return service.setPhonebookAccessPermission(this, value);
        } catch (RemoteException e) {
            Log.e(TAG, "", e);
            return false;
        }
    }

    @UnsupportedAppUsage
    public int getMessageAccessPermission() {
        IBluetooth service = sService;
        if (service == null) {
            return 0;
        }
        try {
            return service.getMessageAccessPermission(this);
        } catch (RemoteException e) {
            Log.e(TAG, "", e);
            return 0;
        }
    }

    @UnsupportedAppUsage
    public boolean setMessageAccessPermission(int value) {
        IBluetooth service = sService;
        if (service == null) {
            return false;
        }
        try {
            return service.setMessageAccessPermission(this, value);
        } catch (RemoteException e) {
            Log.e(TAG, "", e);
            return false;
        }
    }

    public int getSimAccessPermission() {
        IBluetooth service = sService;
        if (service == null) {
            return 0;
        }
        try {
            return service.getSimAccessPermission(this);
        } catch (RemoteException e) {
            Log.e(TAG, "", e);
            return 0;
        }
    }

    @UnsupportedAppUsage
    public boolean setSimAccessPermission(int value) {
        IBluetooth service = sService;
        if (service == null) {
            return false;
        }
        try {
            return service.setSimAccessPermission(this, value);
        } catch (RemoteException e) {
            Log.e(TAG, "", e);
            return false;
        }
    }

    @UnsupportedAppUsage
    public BluetoothSocket createRfcommSocket(int channel) throws IOException {
        if (isBluetoothEnabled()) {
            return new BluetoothSocket(1, -1, true, true, this, channel, null);
        }
        Log.e(TAG, "Bluetooth is not enabled");
        throw new IOException();
    }

    public BluetoothSocket createL2capSocket(int channel) throws IOException {
        return new BluetoothSocket(3, -1, true, true, this, channel, null);
    }

    public BluetoothSocket createInsecureL2capSocket(int channel) throws IOException {
        return new BluetoothSocket(3, -1, false, false, this, channel, null);
    }

    public BluetoothSocket createRfcommSocketToServiceRecord(UUID uuid) throws IOException {
        if (isBluetoothEnabled()) {
            return new BluetoothSocket(1, -1, true, true, this, -1, new ParcelUuid(uuid));
        }
        Log.e(TAG, "Bluetooth is not enabled");
        throw new IOException();
    }

    public BluetoothSocket createInsecureRfcommSocketToServiceRecord(UUID uuid) throws IOException {
        if (isBluetoothEnabled()) {
            return new BluetoothSocket(1, -1, false, false, this, -1, new ParcelUuid(uuid));
        }
        Log.e(TAG, "Bluetooth is not enabled");
        throw new IOException();
    }

    @UnsupportedAppUsage
    public BluetoothSocket createInsecureRfcommSocket(int port) throws IOException {
        if (isBluetoothEnabled()) {
            return new BluetoothSocket(1, -1, false, false, this, port, null);
        }
        Log.e(TAG, "Bluetooth is not enabled");
        throw new IOException();
    }

    @UnsupportedAppUsage
    public BluetoothSocket createScoSocket() throws IOException {
        if (isBluetoothEnabled()) {
            return new BluetoothSocket(2, -1, true, true, this, -1, null);
        }
        Log.e(TAG, "Bluetooth is not enabled");
        throw new IOException();
    }

    @UnsupportedAppUsage
    public static byte[] convertPinToBytes(String pin) {
        if (pin == null) {
            return null;
        }
        try {
            byte[] pinBytes = pin.getBytes("UTF-8");
            if (pinBytes.length <= 0 || pinBytes.length > 16) {
                return null;
            }
            return pinBytes;
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "UTF-8 not supported?!?");
            return null;
        }
    }

    public BluetoothGatt connectGatt(Context context, boolean autoConnect, BluetoothGattCallback callback) {
        Log.i(TAG, "connectGatt");
        return connectGatt(context, autoConnect, callback, 0);
    }

    public BluetoothGatt connectGatt(Context context, boolean autoConnect, BluetoothGattCallback callback, int transport) {
        return connectGatt(context, autoConnect, callback, transport, 1);
    }

    public BluetoothGatt connectGatt(Context context, boolean autoConnect, BluetoothGattCallback callback, int transport, int phy) {
        return connectGatt(context, autoConnect, callback, transport, phy, null);
    }

    public BluetoothGatt connectGatt(Context context, boolean autoConnect, BluetoothGattCallback callback, int transport, int phy, Handler handler) {
        return connectGatt(context, autoConnect, callback, transport, false, phy, handler);
    }

    @UnsupportedAppUsage
    public BluetoothGatt connectGatt(Context context, boolean autoConnect, BluetoothGattCallback callback, int transport, boolean opportunistic, int phy, Handler handler) {
        if (callback != null) {
            try {
                IBluetoothGatt iGatt = BluetoothAdapter.getDefaultAdapter().getBluetoothManager().getBluetoothGatt();
                if (iGatt == null) {
                    return null;
                }
                BluetoothGatt gatt = new BluetoothGatt(iGatt, this, transport, opportunistic, phy);
                try {
                    gatt.connect(Boolean.valueOf(autoConnect), callback, handler);
                    return gatt;
                } catch (RemoteException e) {
                    e = e;
                    Log.e(TAG, "", e);
                    return null;
                }
            } catch (RemoteException e2) {
                e = e2;
                Log.e(TAG, "", e);
                return null;
            }
        } else {
            throw new NullPointerException("callback is null");
        }
    }

    public BluetoothSocket createL2capChannel(int psm) throws IOException {
        if (isBluetoothEnabled()) {
            return new BluetoothSocket(4, -1, true, true, this, psm, null);
        }
        Log.e(TAG, "createL2capChannel: Bluetooth is not enabled");
        throw new IOException();
    }

    public BluetoothSocket createL2capCocSocket(int transport, int psm) throws IOException {
        Log.e(TAG, "createL2capCocSocket: PLEASE USE THE OFFICIAL API, createL2capChannel");
        return createL2capChannel(psm);
    }

    public BluetoothSocket createInsecureL2capChannel(int psm) throws IOException {
        if (isBluetoothEnabled()) {
            return new BluetoothSocket(4, -1, false, false, this, psm, null);
        }
        Log.e(TAG, "createInsecureL2capChannel: Bluetooth is not enabled");
        throw new IOException();
    }

    public BluetoothSocket createInsecureL2capCocSocket(int transport, int psm) throws IOException {
        Log.e(TAG, "createL2capCocSocket: PLEASE USE THE OFFICIAL API, createInsecureL2capChannel");
        return createInsecureL2capChannel(psm);
    }

    public BluetoothGatt fastConnectGatt(Context context, boolean autoConnect, BluetoothGattCallback callback, int transport) {
        Log.i(TAG, "fastConnectGatt: " + transport);
        return connectGatt(context, autoConnect, callback, transport | 12, 1);
    }

    @SystemApi
    public boolean setMetadata(int key, byte[] value) {
        IBluetooth service = sService;
        if (service == null) {
            Log.e(TAG, "Bluetooth is not enabled. Cannot set metadata");
            return false;
        } else if (value.length <= 2048) {
            try {
                return service.setMetadata(this, key, value);
            } catch (RemoteException e) {
                Log.e(TAG, "setMetadata fail", e);
                return false;
            }
        } else {
            throw new IllegalArgumentException("value length is " + value.length + ", should not over " + 2048);
        }
    }

    @SystemApi
    public byte[] getMetadata(int key) {
        IBluetooth service = sService;
        if (service == null) {
            Log.e(TAG, "Bluetooth is not enabled. Cannot get metadata");
            return null;
        }
        try {
            return service.getMetadata(this, key);
        } catch (RemoteException e) {
            Log.e(TAG, "getMetadata fail", e);
            return null;
        }
    }

    private int transact(int code, Parcel data, Parcel reply) {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter == null) {
            Log.e(TAG, "tanasact null");
            return 0;
        }
        int ret = 0;
        try {
            IBluetooth service = adapter.getBluetoothService(null);
            if (service != null) {
                service.asBinder().transact(code, data, reply, 0);
                reply.readException();
                ret = reply.readInt();
            } else {
                Log.e(TAG, "Cannot tanasact!");
            }
        } catch (RemoteException e) {
            Log.e(TAG, "tanasact exception");
        } catch (Throwable th) {
            reply.recycle();
            data.recycle();
            throw th;
        }
        reply.recycle();
        data.recycle();
        return ret;
    }

    @UnsupportedAppUsage
    public boolean isCloudDevice() {
        Log.d(TAG, "isCloudDevice");
        if (this.mAddress == null) {
            Log.d(TAG, "isCloudDevice addr is null");
            return false;
        }
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(DESCRIPTOR);
        writeToParcel(data, 0);
        int cloudDev = transact(1008, data, reply);
        Log.d(TAG, "cloudDevice devie type " + cloudDev);
        if (cloudDev == 1) {
            return true;
        }
        return false;
    }

    @UnsupportedAppUsage
    public boolean unpairNotify(int action) {
        Log.d(TAG, "unpairNotify action " + action);
        IBluetooth service = sService;
        if (service == null) {
            Log.e(TAG, "BT not enabled. Cannot unpairNotify");
            return false;
        }
        try {
            return service.unpairNotify(this, action);
        } catch (RemoteException e) {
            Log.e(TAG, "unpairNotify execute failed");
            return false;
        }
    }
}
