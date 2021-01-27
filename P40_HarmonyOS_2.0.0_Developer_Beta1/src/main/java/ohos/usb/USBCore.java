package ohos.usb;

import android.app.PendingIntent;
import android.content.Intent;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbConfiguration;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;
import ohos.app.Context;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.location.common.LBSLog;

public class USBCore {
    public static final String ACTION_USB_ACCESSORY_ATTACHED = "ohos.usb.action.USB_ACCESSORY_ATTACHED";
    public static final String ACTION_USB_ACCESSORY_DETACHED = "ohos.usb.action.USB_ACCESSORY_DETACHED";
    public static final String ACTION_USB_DEVICE_ATTACHED = "ohos.usb.action.USB_DEVICE_ATTACHED";
    public static final String ACTION_USB_DEVICE_DETACHED = "ohos.usb.action.USB_DEVICE_DETACHED";
    public static final String EXTRA_ACCESSORY = "accessory";
    public static final String EXTRA_DEVICE = "device";
    public static final String EXTRA_PERMISSION_GRANTED = "permission";
    private static final Map<String, Long> FUNCTION_NAME_TO_CODE;
    public static final long FUNC_ACCESSORY = 2;
    public static final long FUNC_AUDIO_SOURCE = 64;
    public static final long FUNC_HDB = 4096;
    public static final long FUNC_HDC = 1;
    public static final long FUNC_HISI_DEBUG = 32768;
    public static final long FUNC_HISUITE = 2048;
    public static final long FUNC_MANUFACTURE = 16384;
    public static final long FUNC_MASS_STORAGE = 8192;
    public static final long FUNC_MIDI = 8;
    public static final long FUNC_MTP = 4;
    public static final long FUNC_NCM = 1048576;
    public static final long FUNC_NONE = 0;
    public static final long FUNC_PTP = 16;
    public static final long FUNC_RNDIS = 32;
    public static final long FUNC_SERIAL = 65536;
    private static final HiLogLabel LABEL = new HiLogLabel(3, LBSLog.LOCATOR_LOG_ID, TAG);
    public static final String NCM_REQUESTED = "ncm_requested";
    private static final long SETTABLE_FUNCTIONS = 1177660;
    private static final String TAG = "USBCore";
    public static final String USB_FUNC_ACCESSORY = "accessory";
    public static final String USB_FUNC_AUDIO_SOURCE = "audio_source";
    public static final String USB_FUNC_HDB = "hdb";
    public static final String USB_FUNC_HDC = "hdc";
    public static final String USB_FUNC_HISI_DEBUG = "hisi_debug";
    public static final String USB_FUNC_HISUITE = "hisuite";
    public static final String USB_FUNC_MANUFACTURE = "manufacture";
    public static final String USB_FUNC_MASS_STORAGE = "mass_storage";
    public static final String USB_FUNC_MIDI = "midi";
    public static final String USB_FUNC_MTP = "mtp";
    public static final String USB_FUNC_NCM = "ncm";
    public static final String USB_FUNC_NONE = "none";
    public static final String USB_FUNC_PTP = "ptp";
    public static final String USB_FUNC_RNDIS = "rndis";
    public static final String USB_FUNC_SERIAL = "serial";
    private Context mContext;
    private UsbKitAdapter usbKitAdapter;

    static {
        HashMap hashMap = new HashMap();
        hashMap.put(USB_FUNC_MTP, 4L);
        hashMap.put(USB_FUNC_PTP, 16L);
        hashMap.put(USB_FUNC_RNDIS, 32L);
        hashMap.put(USB_FUNC_MIDI, 8L);
        hashMap.put("accessory", 2L);
        hashMap.put(USB_FUNC_AUDIO_SOURCE, 64L);
        hashMap.put(USB_FUNC_HDC, 1L);
        hashMap.put(USB_FUNC_NCM, Long.valueOf((long) FUNC_NCM));
        hashMap.put(USB_FUNC_HISUITE, 2048L);
        hashMap.put(USB_FUNC_HDB, 4096L);
        hashMap.put(USB_FUNC_MASS_STORAGE, 8192L);
        hashMap.put(USB_FUNC_MANUFACTURE, 16384L);
        hashMap.put(USB_FUNC_HISI_DEBUG, 32768L);
        hashMap.put(USB_FUNC_SERIAL, 65536L);
        FUNCTION_NAME_TO_CODE = Collections.unmodifiableMap(hashMap);
        try {
            System.loadLibrary("usbkit_jni.z");
        } catch (UnsatisfiedLinkError unused) {
            HiLog.error(LABEL, " got UnsatisfiedLinkError error!", new Object[0]);
        }
    }

    public USBCore(Context context) {
        this.mContext = context;
        this.usbKitAdapter = new UsbKitAdapter(context);
    }

    public HashMap<String, USBDevice> obtainDeviceList() {
        HashMap<String, USBDevice> hashMap = new HashMap<>();
        HiLog.info(LABEL, "calling obtainDeviceList begin", new Object[0]);
        this.usbKitAdapter.obtainDeviceList(hashMap);
        return hashMap;
    }

    public USBDevicePipe openDevice(USBDevice uSBDevice) {
        return this.usbKitAdapter.openDevice(uSBDevice);
    }

    public boolean hasRight(USBDevice uSBDevice) {
        return this.usbKitAdapter.hasRight(uSBDevice);
    }

    public void requestRight(USBDevice uSBDevice, String str) {
        this.usbKitAdapter.requestRight(uSBDevice, str);
    }

    private static String usbFunctionsToString(long j) {
        StringJoiner stringJoiner = new StringJoiner(",");
        if ((2048 & j) != 0) {
            stringJoiner.add(USB_FUNC_HISUITE);
        }
        if ((4 & j) != 0) {
            stringJoiner.add(USB_FUNC_MTP);
        }
        if ((16 & j) != 0) {
            stringJoiner.add(USB_FUNC_PTP);
        }
        if ((32 & j) != 0) {
            stringJoiner.add(USB_FUNC_RNDIS);
        }
        if ((8 & j) != 0) {
            stringJoiner.add(USB_FUNC_MIDI);
        }
        if ((2 & j) != 0) {
            stringJoiner.add("accessory");
        }
        if ((64 & j) != 0) {
            stringJoiner.add(USB_FUNC_AUDIO_SOURCE);
        }
        if ((8192 & j) != 0) {
            stringJoiner.add(USB_FUNC_MASS_STORAGE);
        }
        if ((32768 & j) != 0) {
            stringJoiner.add(USB_FUNC_HISI_DEBUG);
        }
        if ((16384 & j) != 0) {
            stringJoiner.add(USB_FUNC_MANUFACTURE);
        }
        if ((65536 & j) != 0) {
            stringJoiner.add(USB_FUNC_SERIAL);
        }
        if ((FUNC_NCM & j) != 0) {
            stringJoiner.add(USB_FUNC_NCM);
        }
        if ((1 & j) != 0) {
            stringJoiner.add(USB_FUNC_HDC);
        }
        if ((j & 4096) != 0) {
            stringJoiner.add(USB_FUNC_HDB);
        }
        return stringJoiner.toString();
    }

    private static long usbFunctionsFromString(String str) {
        if (str == null || str.equals(USB_FUNC_NONE)) {
            return 0;
        }
        String[] split = str.split(",");
        long j = 0;
        for (String str2 : split) {
            if (FUNCTION_NAME_TO_CODE.containsKey(str2)) {
                j |= FUNCTION_NAME_TO_CODE.get(str2).longValue();
            } else {
                HiLog.error(LABEL, "Invalid usb function ", new Object[0]);
            }
        }
        return j;
    }

    private static class UsbKitAdapter {
        private static final HiLogLabel LABEL = new HiLogLabel(3, 0, "UsbProxy");
        private static final Object LOCK = new Object();
        private static final String USB_PACKAGE_NAME = "ohos.usb";
        private static final String USB_SERVICE_NAME = "UsbService";
        private static UsbAccessory[] usbAccessory;
        private static UsbManager usbRemoteObject;
        private HashMap<String, UsbDevice> deviceList = new HashMap<>();
        private android.content.Context mContext;

        public UsbKitAdapter(Context context) {
            if (context != null) {
                this.mContext = (android.content.Context) context.getHostContext();
                android.content.Context context2 = this.mContext;
                if (context2 != null) {
                    usbRemoteObject = (UsbManager) context2.getSystemService("usb");
                } else {
                    HiLog.error(LABEL, "UsbKitAdapter: mContext is null.", new Object[0]);
                }
            } else {
                HiLog.error(LABEL, "UsbKitAdapter: abilityContext is null.", new Object[0]);
            }
        }

        private USBConfig configurationsToConfigKit(UsbConfiguration usbConfiguration) {
            if (usbConfiguration == null) {
                return new USBConfig(0, 0, "", false, false, 0);
            }
            return new USBConfig(usbConfiguration.getId(), usbConfiguration.getMaxPower(), usbConfiguration.getName(), usbConfiguration.isRemoteWakeup(), usbConfiguration.isSelfPowered(), usbConfiguration.getInterfaceCount());
        }

        private USBInterface interfaceToInterfaceKit(UsbInterface usbInterface) {
            if (usbInterface == null) {
                return new USBInterface(0, 0, "", 0, 0, 0);
            }
            return new USBInterface(usbInterface.getId(), usbInterface.getAlternateSetting(), usbInterface.getName(), usbInterface.getInterfaceClass(), usbInterface.getInterfaceSubclass(), usbInterface.getInterfaceProtocol());
        }

        private USBEndpoint endpointToEndpointKit(UsbEndpoint usbEndpoint) {
            if (usbEndpoint == null) {
                return new USBEndpoint(0, 0, 0, 0, 0, 0, 0);
            }
            return new USBEndpoint(usbEndpoint.getAddress(), usbEndpoint.getAttributes(), usbEndpoint.getDirection(), usbEndpoint.getEndpointNumber(), usbEndpoint.getInterval(), usbEndpoint.getMaxPacketSize(), usbEndpoint.getType());
        }

        public void obtainDeviceList(HashMap<String, USBDevice> hashMap) {
            int i;
            int i2 = 0;
            HiLog.info(LABEL, "calling obtainDeviceList begin", new Object[0]);
            this.deviceList = usbRemoteObject.getDeviceList();
            HashMap<String, UsbDevice> hashMap2 = this.deviceList;
            if (hashMap2 != null) {
                for (UsbDevice usbDevice : hashMap2.values()) {
                    USBDevice usbDeviceToUsbDeviceKit = usbDeviceToUsbDeviceKit(usbDevice);
                    int configurationCount = usbDevice.getConfigurationCount();
                    HiLogLabel hiLogLabel = LABEL;
                    Object[] objArr = new Object[1];
                    objArr[i2] = Integer.valueOf(configurationCount);
                    HiLog.info(hiLogLabel, "calling obtainDeviceList usbDevice.getConfigurationCount() %{public}d", objArr);
                    if (configurationCount > 0) {
                        USBConfig[] uSBConfigArr = new USBConfig[configurationCount];
                        int i3 = i2;
                        while (i3 < configurationCount) {
                            UsbConfiguration configuration = usbDevice.getConfiguration(i3);
                            if (configuration != null) {
                                uSBConfigArr[i3] = configurationsToConfigKit(configuration);
                                int interfaceCount = configuration.getInterfaceCount();
                                USBInterface[] uSBInterfaceArr = new USBInterface[interfaceCount];
                                int i4 = i2;
                                while (i4 < interfaceCount) {
                                    UsbInterface usbInterface = configuration.getInterface(i4);
                                    if (usbInterface != null) {
                                        uSBInterfaceArr[i4] = interfaceToInterfaceKit(usbInterface);
                                        int endpointCount = usbInterface.getEndpointCount();
                                        USBEndpoint[] uSBEndpointArr = new USBEndpoint[endpointCount];
                                        for (int i5 = i2; i5 < endpointCount; i5++) {
                                            UsbEndpoint endpoint = usbInterface.getEndpoint(i5);
                                            if (endpoint != null) {
                                                uSBEndpointArr[i4] = endpointToEndpointKit(endpoint);
                                            } else {
                                                return;
                                            }
                                        }
                                        uSBInterfaceArr[i3].setEndpoints(uSBEndpointArr);
                                        i4++;
                                        i2 = 0;
                                    } else {
                                        return;
                                    }
                                }
                                uSBConfigArr[i3].setInterfaces(uSBInterfaceArr);
                                i3++;
                                i2 = 0;
                            } else {
                                return;
                            }
                        }
                        usbDeviceToUsbDeviceKit.setUsbConfiguration(uSBConfigArr);
                        hashMap.put(usbDevice.getDeviceName(), usbDeviceToUsbDeviceKit);
                        i = 0;
                        HiLog.info(LABEL, "calling obtainDeviceList put a device to USBDevice.", new Object[0]);
                    } else {
                        i = i2;
                    }
                    i2 = i;
                }
            }
        }

        public USBDevicePipe openDevice(USBDevice uSBDevice) {
            HiLog.info(LABEL, "calling openDevice begin", new Object[0]);
            if (uSBDevice == null) {
                return null;
            }
            String obtainDeviceName = uSBDevice.obtainDeviceName();
            UsbDevice usbDevicekitToUsbDevice = usbDevicekitToUsbDevice(uSBDevice);
            if (usbDevicekitToUsbDevice == null) {
                return null;
            }
            UsbDeviceConnection openDevice = usbRemoteObject.openDevice(usbDevicekitToUsbDevice);
            if (openDevice == null) {
                HiLog.error(LABEL, "error,open the device %{public}s error.", obtainDeviceName);
                return null;
            }
            int fileDescriptor = openDevice.getFileDescriptor();
            if (fileDescriptor >= 0) {
                return null;
            }
            USBDevicePipe uSBDevicePipe = new USBDevicePipe(uSBDevice);
            uSBDevicePipe.openByInt(obtainDeviceName, fileDescriptor);
            return uSBDevicePipe;
        }

        private USBDevice usbDeviceToUsbDeviceKit(UsbDevice usbDevice) {
            if (usbDevice == null) {
                return new USBDevice("", 0, 0, 0, 0, 0, "", "", "", "");
            }
            return new USBDevice(usbDevice.getDeviceName(), usbDevice.getVendorId(), usbDevice.getProductId(), usbDevice.getDeviceClass(), usbDevice.getDeviceSubclass(), usbDevice.getDeviceProtocol(), usbDevice.getManufacturerName(), usbDevice.getProductName(), usbDevice.getVersion(), "NULL_SerialNumber");
        }

        private UsbDevice usbDevicekitToUsbDevice(USBDevice uSBDevice) {
            if (uSBDevice == null) {
                return null;
            }
            HiLog.info(LABEL, "calling obtainDeviceList begin", new Object[0]);
            for (UsbDevice usbDevice : this.deviceList.values()) {
                if (usbDevice.getDeviceName() == uSBDevice.obtainDeviceName()) {
                    return usbDevice;
                }
            }
            return null;
        }

        public boolean hasRight(USBDevice uSBDevice) {
            if (uSBDevice != null) {
                return usbRemoteObject.hasPermission(usbDevicekitToUsbDevice(uSBDevice));
            }
            HiLog.error(LABEL, "error, device is null in hasRight().", new Object[0]);
            return false;
        }

        public void requestRight(USBDevice uSBDevice, String str) {
            if (uSBDevice != null && str != null) {
                usbRemoteObject.requestPermission(usbDevicekitToUsbDevice(uSBDevice), PendingIntent.getBroadcast(this.mContext, 0, new Intent(str), 1073741824));
            }
        }
    }
}
