package android.companion;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanFilter;
import android.net.wifi.ScanResult;
import android.os.ParcelUuid;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class BluetoothDeviceFilterUtils {
    private static final boolean DEBUG = false;
    private static final String LOG_TAG = "BluetoothDeviceFilterUtils";

    private BluetoothDeviceFilterUtils() {
    }

    static String patternToString(Pattern p) {
        return p == null ? null : p.pattern();
    }

    static Pattern patternFromString(String s) {
        return s == null ? null : Pattern.compile(s);
    }

    static boolean matches(ScanFilter filter, BluetoothDevice device) {
        if (matchesAddress(filter.getDeviceAddress(), device)) {
            return matchesServiceUuid(filter.getServiceUuid(), filter.getServiceUuidMask(), device);
        }
        return false;
    }

    static boolean matchesAddress(String deviceAddress, BluetoothDevice device) {
        if (deviceAddress != null) {
            return device != null ? deviceAddress.equals(device.getAddress()) : false;
        } else {
            return true;
        }
    }

    static boolean matchesServiceUuids(List<ParcelUuid> serviceUuids, List<ParcelUuid> serviceUuidMasks, BluetoothDevice device) {
        for (int i = 0; i < serviceUuids.size(); i++) {
            if (!matchesServiceUuid((ParcelUuid) serviceUuids.get(i), (ParcelUuid) serviceUuidMasks.get(i), device)) {
                return false;
            }
        }
        return true;
    }

    static boolean matchesServiceUuid(ParcelUuid serviceUuid, ParcelUuid serviceUuidMask, BluetoothDevice device) {
        if (serviceUuid != null) {
            return ScanFilter.matchesServiceUuids(serviceUuid, serviceUuidMask, Arrays.asList(device.getUuids()));
        }
        return true;
    }

    static boolean matchesName(Pattern namePattern, BluetoothDevice device) {
        if (namePattern == null) {
            return true;
        }
        if (device == null) {
            return false;
        }
        String name = device.getName();
        return name != null ? namePattern.matcher(name).find() : false;
    }

    static boolean matchesName(Pattern namePattern, ScanResult device) {
        if (namePattern == null) {
            return true;
        }
        if (device == null) {
            return false;
        }
        String name = device.SSID;
        return name != null ? namePattern.matcher(name).find() : false;
    }

    private static void debugLogMatchResult(boolean result, BluetoothDevice device, Object criteria) {
        Log.i(LOG_TAG, getDeviceDisplayNameInternal(device) + (result ? " ~ " : " !~ ") + criteria);
    }

    private static void debugLogMatchResult(boolean result, ScanResult device, Object criteria) {
        Log.i(LOG_TAG, getDeviceDisplayNameInternal(device) + (result ? " ~ " : " !~ ") + criteria);
    }

    public static String getDeviceDisplayNameInternal(BluetoothDevice device) {
        return TextUtils.firstNotEmpty(device.getAliasName(), device.getAddress());
    }

    public static String getDeviceDisplayNameInternal(ScanResult device) {
        return TextUtils.firstNotEmpty(device.SSID, device.BSSID);
    }

    public static String getDeviceMacAddress(Parcelable device) {
        if (device instanceof BluetoothDevice) {
            return ((BluetoothDevice) device).getAddress();
        }
        if (device instanceof ScanResult) {
            return ((ScanResult) device).BSSID;
        }
        if (device instanceof android.bluetooth.le.ScanResult) {
            return getDeviceMacAddress(((android.bluetooth.le.ScanResult) device).getDevice());
        }
        throw new IllegalArgumentException("Unknown device type: " + device);
    }
}
