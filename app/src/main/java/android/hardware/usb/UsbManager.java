package android.hardware.usb;

import android.app.PendingIntent;
import android.content.Context;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.os.Process;
import android.os.RemoteException;
import android.util.Log;
import com.android.internal.util.Preconditions;
import java.util.HashMap;

public class UsbManager {
    public static final String ACTION_USB_ACCESSORY_ATTACHED = "android.hardware.usb.action.USB_ACCESSORY_ATTACHED";
    public static final String ACTION_USB_ACCESSORY_DETACHED = "android.hardware.usb.action.USB_ACCESSORY_DETACHED";
    public static final String ACTION_USB_DEVICE_ATTACHED = "android.hardware.usb.action.USB_DEVICE_ATTACHED";
    public static final String ACTION_USB_DEVICE_DETACHED = "android.hardware.usb.action.USB_DEVICE_DETACHED";
    public static final String ACTION_USB_PORT_CHANGED = "android.hardware.usb.action.USB_PORT_CHANGED";
    public static final String ACTION_USB_STATE = "android.hardware.usb.action.USB_STATE";
    public static final String EXTRA_ACCESSORY = "accessory";
    public static final String EXTRA_DEVICE = "device";
    public static final String EXTRA_PERMISSION_GRANTED = "permission";
    public static final String EXTRA_PORT = "port";
    public static final String EXTRA_PORT_STATUS = "portStatus";
    public static final String NCM_REQUESTED = "ncm_requested";
    public static final String ONLY_CHARGING = "only_charging";
    private static final String TAG = "UsbManager";
    public static final String USB_CONFIGURED = "configured";
    public static final String USB_CONNECTED = "connected";
    public static final String USB_DATA_UNLOCKED = "unlocked";
    public static final String USB_FUNCTION_ACCESSORY = "accessory";
    public static final String USB_FUNCTION_ADB = "adb";
    public static final String USB_FUNCTION_AUDIO_SOURCE = "audio_source";
    public static final String USB_FUNCTION_HISUITE = "hisuite";
    public static final String USB_FUNCTION_MIDI = "midi";
    public static final String USB_FUNCTION_MTP = "mtp";
    public static final String USB_FUNCTION_NCM = "ncm";
    public static final String USB_FUNCTION_NONE = "none";
    public static final String USB_FUNCTION_PTP = "ptp";
    public static final String USB_FUNCTION_RNDIS = "rndis";
    public static final String USB_HOST_CONNECTED = "host_connected";
    private final Context mContext;
    private final IUsbManager mService;

    public UsbManager(Context context, IUsbManager service) {
        this.mContext = context;
        this.mService = service;
    }

    public HashMap<String, UsbDevice> getDeviceList() {
        Bundle bundle = new Bundle();
        try {
            this.mService.getDeviceList(bundle);
            HashMap<String, UsbDevice> result = new HashMap();
            for (String name : bundle.keySet()) {
                result.put(name, (UsbDevice) bundle.get(name));
            }
            return result;
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public UsbDeviceConnection openDevice(UsbDevice device) {
        try {
            String deviceName = device.getDeviceName();
            ParcelFileDescriptor pfd = this.mService.openDevice(deviceName);
            if (pfd != null) {
                UsbDeviceConnection connection = new UsbDeviceConnection(device);
                boolean result = connection.open(deviceName, pfd);
                pfd.close();
                if (result) {
                    return connection;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "exception in UsbManager.openDevice", e);
        }
        return null;
    }

    public UsbAccessory[] getAccessoryList() {
        try {
            if (this.mService.getCurrentAccessory() == null) {
                return null;
            }
            return new UsbAccessory[]{this.mService.getCurrentAccessory()};
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public ParcelFileDescriptor openAccessory(UsbAccessory accessory) {
        try {
            return this.mService.openAccessory(accessory);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean hasPermission(UsbDevice device) {
        try {
            return this.mService.hasDevicePermission(device);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean hasPermission(UsbAccessory accessory) {
        try {
            return this.mService.hasAccessoryPermission(accessory);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void requestPermission(UsbDevice device, PendingIntent pi) {
        try {
            this.mService.requestDevicePermission(device, this.mContext.getPackageName(), pi);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void requestPermission(UsbAccessory accessory, PendingIntent pi) {
        try {
            this.mService.requestAccessoryPermission(accessory, this.mContext.getPackageName(), pi);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void grantPermission(UsbDevice device) {
        try {
            this.mService.grantDevicePermission(device, Process.myUid());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean isFunctionEnabled(String function) {
        try {
            return this.mService.isFunctionEnabled(function);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setCurrentFunction(String function) {
        try {
            this.mService.setCurrentFunction(function);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setUsbDataUnlocked(boolean unlocked) {
        try {
            this.mService.setUsbDataUnlocked(unlocked);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public UsbPort[] getPorts() {
        try {
            return this.mService.getPorts();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public UsbPortStatus getPortStatus(UsbPort port) {
        Preconditions.checkNotNull(port, "port must not be null");
        try {
            return this.mService.getPortStatus(port.getId());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setPortRoles(UsbPort port, int powerRole, int dataRole) {
        Preconditions.checkNotNull(port, "port must not be null");
        UsbPort.checkRoles(powerRole, dataRole);
        try {
            this.mService.setPortRoles(port.getId(), powerRole, dataRole);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public static String addFunction(String functions, String function) {
        if (USB_FUNCTION_NONE.equals(functions)) {
            return function;
        }
        if (!containsFunction(functions, function)) {
            if (functions.length() > 0) {
                functions = functions + ",";
            }
            functions = functions + function;
        }
        return functions;
    }

    public static String removeFunction(String functions, String function) {
        String[] split = functions.split(",");
        for (int i = 0; i < split.length; i++) {
            if (function.equals(split[i])) {
                split[i] = null;
            }
        }
        if (split.length == 1 && split[0] == null) {
            return USB_FUNCTION_NONE;
        }
        StringBuilder builder = new StringBuilder();
        for (String s : split) {
            if (s != null) {
                if (builder.length() > 0) {
                    builder.append(",");
                }
                builder.append(s);
            }
        }
        return builder.toString();
    }

    public static boolean containsFunction(String functions, String function) {
        int index = functions.indexOf(function);
        if (index < 0) {
            return false;
        }
        if (index > 0 && functions.charAt(index - 1) != ',') {
            return false;
        }
        int charAfter = index + function.length();
        if (charAfter >= functions.length() || functions.charAt(charAfter) == ',') {
            return true;
        }
        return false;
    }
}
