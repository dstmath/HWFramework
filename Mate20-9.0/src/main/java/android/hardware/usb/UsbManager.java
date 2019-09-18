package android.hardware.usb;

import android.annotation.SystemApi;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.os.Process;
import android.os.RemoteException;
import android.util.Log;
import com.android.internal.util.Preconditions;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

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
    public static final long FUNCTION_ACCESSORY = 2;
    public static final long FUNCTION_ADB = 1;
    public static final long FUNCTION_AUDIO_SOURCE = 64;
    public static final long FUNCTION_HDB = 4096;
    public static final long FUNCTION_HISI_DEBUG = 32768;
    public static final long FUNCTION_HISUITE = 2048;
    public static final long FUNCTION_MANUFACTURE = 16384;
    public static final long FUNCTION_MASS_STORAGE = 8192;
    public static final long FUNCTION_MIDI = 8;
    public static final long FUNCTION_MTP = 4;
    private static final Map<String, Long> FUNCTION_NAME_TO_CODE = new HashMap();
    public static final long FUNCTION_NCM = 1048576;
    public static final long FUNCTION_NONE = 0;
    public static final long FUNCTION_PTP = 16;
    public static final long FUNCTION_RNDIS = 32;
    public static final long FUNCTION_SERIAL = 65536;
    public static final String NCM_REQUESTED = "ncm_requested";
    public static final String ONLY_CHARGING = "only_charging";
    private static final long SETTABLE_FUNCTIONS = 1177725;
    private static final String TAG = "UsbManager";
    public static final String USB_CONFIGURED = "configured";
    public static final String USB_CONNECTED = "connected";
    public static final String USB_DATA_UNLOCKED = "unlocked";
    public static final String USB_FUNCTION_ACCESSORY = "accessory";
    public static final String USB_FUNCTION_ADB = "adb";
    public static final String USB_FUNCTION_AUDIO_SOURCE = "audio_source";
    public static final String USB_FUNCTION_HDB = "hdb";
    public static final String USB_FUNCTION_HISI_DEBUG = "hisi_debug";
    public static final String USB_FUNCTION_HISUITE = "hisuite";
    public static final String USB_FUNCTION_MANUFACTURE = "manufacture";
    public static final String USB_FUNCTION_MASS_STORAGE = "mass_storage";
    public static final String USB_FUNCTION_MIDI = "midi";
    public static final String USB_FUNCTION_MTP = "mtp";
    public static final String USB_FUNCTION_NCM = "ncm";
    public static final String USB_FUNCTION_NONE = "none";
    public static final String USB_FUNCTION_PTP = "ptp";
    public static final String USB_FUNCTION_RNDIS = "rndis";
    public static final String USB_FUNCTION_SERIAL = "serial";
    public static final String USB_HOST_CONNECTED = "host_connected";
    private final Context mContext;
    private final IUsbManager mService;

    static {
        FUNCTION_NAME_TO_CODE.put(USB_FUNCTION_MTP, 4L);
        FUNCTION_NAME_TO_CODE.put(USB_FUNCTION_PTP, 16L);
        FUNCTION_NAME_TO_CODE.put(USB_FUNCTION_RNDIS, 32L);
        FUNCTION_NAME_TO_CODE.put("midi", 8L);
        FUNCTION_NAME_TO_CODE.put("accessory", 2L);
        FUNCTION_NAME_TO_CODE.put(USB_FUNCTION_AUDIO_SOURCE, 64L);
        FUNCTION_NAME_TO_CODE.put(USB_FUNCTION_ADB, 1L);
        FUNCTION_NAME_TO_CODE.put(USB_FUNCTION_NCM, 1048576L);
        FUNCTION_NAME_TO_CODE.put(USB_FUNCTION_HISUITE, 2048L);
        FUNCTION_NAME_TO_CODE.put(USB_FUNCTION_HDB, 4096L);
        FUNCTION_NAME_TO_CODE.put(USB_FUNCTION_MASS_STORAGE, 8192L);
        FUNCTION_NAME_TO_CODE.put(USB_FUNCTION_MANUFACTURE, 16384L);
        FUNCTION_NAME_TO_CODE.put(USB_FUNCTION_HISI_DEBUG, 32768L);
        FUNCTION_NAME_TO_CODE.put("serial", 65536L);
    }

    public UsbManager(Context context, IUsbManager service) {
        this.mContext = context;
        this.mService = service;
    }

    public HashMap<String, UsbDevice> getDeviceList() {
        HashMap<String, UsbDevice> result = new HashMap<>();
        if (this.mService == null) {
            return result;
        }
        Bundle bundle = new Bundle();
        try {
            this.mService.getDeviceList(bundle);
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
            ParcelFileDescriptor pfd = this.mService.openDevice(deviceName, this.mContext.getPackageName());
            if (pfd != null) {
                UsbDeviceConnection connection = new UsbDeviceConnection(device);
                boolean result = connection.open(deviceName, pfd, this.mContext);
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
        if (this.mService == null) {
            return null;
        }
        try {
            UsbAccessory accessory = this.mService.getCurrentAccessory();
            if (accessory == null) {
                return null;
            }
            return new UsbAccessory[]{accessory};
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

    public ParcelFileDescriptor getControlFd(long function) {
        try {
            return this.mService.getControlFd(function);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean hasPermission(UsbDevice device) {
        if (this.mService == null) {
            return false;
        }
        try {
            return this.mService.hasDevicePermission(device, this.mContext.getPackageName());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean hasPermission(UsbAccessory accessory) {
        if (this.mService == null) {
            return false;
        }
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
        grantPermission(device, Process.myUid());
    }

    public void grantPermission(UsbDevice device, int uid) {
        try {
            this.mService.grantDevicePermission(device, uid);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @SystemApi
    public void grantPermission(UsbDevice device, String packageName) {
        try {
            grantPermission(device, this.mContext.getPackageManager().getPackageUidAsUser(packageName, this.mContext.getUserId()));
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Package " + packageName + " not found.", e);
        }
    }

    @Deprecated
    public boolean isFunctionEnabled(String function) {
        try {
            return this.mService.isFunctionEnabled(function);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setCurrentFunctions(long functions) {
        try {
            this.mService.setCurrentFunctions(functions);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Deprecated
    public void setCurrentFunction(String functions, boolean usbDataUnlocked) {
        Log.d(TAG, "setCurrentFunction: " + functions);
        try {
            this.mService.setCurrentFunction(functions, usbDataUnlocked);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public long getCurrentFunctions() {
        try {
            return this.mService.getCurrentFunctions();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setScreenUnlockedFunctions(long functions) {
        try {
            this.mService.setScreenUnlockedFunctions(functions);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public long getScreenUnlockedFunctions() {
        try {
            return this.mService.getScreenUnlockedFunctions();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public UsbPort[] getPorts() {
        if (this.mService == null) {
            return null;
        }
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
        Log.d(TAG, "setPortRoles Package:" + this.mContext.getPackageName());
        try {
            this.mService.setPortRoles(port.getId(), powerRole, dataRole);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setUsbDeviceConnectionHandler(ComponentName usbDeviceConnectionHandler) {
        try {
            this.mService.setUsbDeviceConnectionHandler(usbDeviceConnectionHandler);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public static boolean areSettableFunctions(long functions) {
        return functions == 0 || (-1177726 & functions) == 0 || (16384 & functions) != 0;
    }

    public static String usbFunctionsToString(long functions) {
        StringJoiner joiner = new StringJoiner(",");
        if ((2048 & functions) != 0) {
            joiner.add(USB_FUNCTION_HISUITE);
        }
        if ((4 & functions) != 0) {
            joiner.add(USB_FUNCTION_MTP);
        }
        if ((16 & functions) != 0) {
            joiner.add(USB_FUNCTION_PTP);
        }
        if ((32 & functions) != 0) {
            joiner.add(USB_FUNCTION_RNDIS);
        }
        if ((8 & functions) != 0) {
            joiner.add("midi");
        }
        if ((2 & functions) != 0) {
            joiner.add("accessory");
        }
        if ((64 & functions) != 0) {
            joiner.add(USB_FUNCTION_AUDIO_SOURCE);
        }
        if ((8192 & functions) != 0) {
            joiner.add(USB_FUNCTION_MASS_STORAGE);
        }
        if ((32768 & functions) != 0) {
            joiner.add(USB_FUNCTION_HISI_DEBUG);
        }
        if ((16384 & functions) != 0) {
            joiner.add(USB_FUNCTION_MANUFACTURE);
        }
        if ((65536 & functions) != 0) {
            joiner.add("serial");
        }
        if ((1048576 & functions) != 0) {
            joiner.add(USB_FUNCTION_NCM);
        }
        if ((1 & functions) != 0) {
            joiner.add(USB_FUNCTION_ADB);
        }
        if ((4096 & functions) != 0) {
            joiner.add(USB_FUNCTION_HDB);
        }
        return joiner.toString();
    }

    public static long usbFunctionsFromString(String functions) {
        if (functions == null || functions.equals("none")) {
            return 0;
        }
        long ret = 0;
        for (String function : functions.split(",")) {
            if (FUNCTION_NAME_TO_CODE.containsKey(function)) {
                ret |= FUNCTION_NAME_TO_CODE.get(function).longValue();
            } else if (function.length() > 0) {
                throw new IllegalArgumentException("Invalid usb function " + functions);
            }
        }
        return ret;
    }
}
