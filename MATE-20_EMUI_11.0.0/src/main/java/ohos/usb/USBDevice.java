package ohos.usb;

public class USBDevice {
    private final int localClass;
    private final String localManufacturerName;
    private final String localName;
    private final int localProductId;
    private final String localProductName;
    private final int localProtocol;
    private final String localSerialNumber;
    private final int localSubclass;
    private final int localVendorId;
    private final String localVersion;
    private UsbDeviceKitAdapter usbDeviceKitAdapter = UsbDeviceKitAdapter.getInstance();

    public USBDevice(String str, int i, int i2, int i3, int i4, int i5, String str2, String str3, String str4, String str5) {
        this.localName = str;
        this.localVendorId = i;
        this.localProductId = i2;
        this.localClass = i3;
        this.localSubclass = i4;
        this.localProtocol = i5;
        this.localManufacturerName = str2;
        this.localProductName = str3;
        this.localVersion = str4;
        this.localSerialNumber = str5;
    }

    public int obtainDeviceClass() {
        return this.localClass;
    }

    public int obtainDeviceId() {
        return this.usbDeviceKitAdapter.obtainDeviceId(this.localName);
    }

    public String obtainDeviceName() {
        return this.localName;
    }

    public int obtainDeviceProtocol() {
        return this.localProtocol;
    }

    public int obtainDeviceSubclass() {
        return this.localSubclass;
    }

    public USBInterface obtainInterface(int i) {
        return this.usbDeviceKitAdapter.obtainInterface(i);
    }

    public int obtainInterfaceCount() {
        return this.usbDeviceKitAdapter.obtainInterfaceCount();
    }

    public String obtainManufacturer() {
        return this.localManufacturerName;
    }

    public int obtainProductId() {
        return this.localProductId;
    }

    public String obtainProductName() {
        return this.localProductName;
    }

    public String obtainSerialNumber() {
        return this.localSerialNumber;
    }

    public int obtainVendorId() {
        return this.localVendorId;
    }

    public String obtainVersion() {
        return this.localVersion;
    }

    public int obtainId(int i) {
        return this.usbDeviceKitAdapter.obtainId(i);
    }

    public USBInterface obtainInterface(int i, int i2) {
        return this.usbDeviceKitAdapter.obtainInterface(i, i2);
    }

    public int obtainInterfaceCount(int i) {
        return this.usbDeviceKitAdapter.obtainInterfaceCount(i);
    }

    public int obtainMaxPower(int i) {
        return this.usbDeviceKitAdapter.obtainMaxPower(i);
    }

    public String obtainName(int i) {
        return this.usbDeviceKitAdapter.obtainName(i);
    }

    public boolean isRemoteWakeup(int i) {
        return this.usbDeviceKitAdapter.isRemoteWakeup(i);
    }

    public boolean isSelfPowered(int i) {
        return this.usbDeviceKitAdapter.isSelfPowered(i);
    }

    public void setUsbConfiguration(USBConfig[] uSBConfigArr) {
        this.usbDeviceKitAdapter.setUsbConfiguration(uSBConfigArr);
    }

    private static class UsbDeviceKitAdapter {
        private static final Object LOCK = new Object();
        private static volatile UsbDeviceKitAdapter instance;
        private USBConfig[] localConfigurations;
        private USBInterface[] mInterfaceKits;

        public static int getDeviceId(String str) {
            return str == null ? -1 : 0;
        }

        public static UsbDeviceKitAdapter getInstance() {
            UsbDeviceKitAdapter usbDeviceKitAdapter;
            synchronized (LOCK) {
                if (instance == null) {
                    instance = new UsbDeviceKitAdapter();
                }
                usbDeviceKitAdapter = instance;
            }
            return usbDeviceKitAdapter;
        }

        public int obtainDeviceId(String str) {
            return getDeviceId(str);
        }

        private USBInterface[] getInterfaceKitArray() {
            if (this.mInterfaceKits == null) {
                USBConfig[] uSBConfigArr = this.localConfigurations;
                if (uSBConfigArr == null) {
                    return new USBInterface[0];
                }
                int length = uSBConfigArr.length;
                int i = 0;
                for (int i2 = 0; i2 < length; i2++) {
                    i += this.localConfigurations[i2].getInterfaceCount();
                }
                this.mInterfaceKits = new USBInterface[i];
                int i3 = 0;
                int i4 = 0;
                while (i3 < length) {
                    USBConfig uSBConfig = this.localConfigurations[i3];
                    int interfaceCount = uSBConfig.getInterfaceCount();
                    int i5 = i4;
                    int i6 = 0;
                    while (i6 < interfaceCount) {
                        this.mInterfaceKits[i5] = uSBConfig.getInterface(i6);
                        i6++;
                        i5++;
                    }
                    i3++;
                    i4 = i5;
                }
            }
            return this.mInterfaceKits;
        }

        public int obtainConfigurationCount() {
            USBConfig[] uSBConfigArr = this.localConfigurations;
            if (uSBConfigArr == null) {
                return 0;
            }
            return uSBConfigArr.length;
        }

        public USBInterface obtainInterface(int i) {
            if (i >= obtainInterfaceCount() || i < 0) {
                return new USBInterface(0, 0, "", 0, 0, 0);
            }
            this.mInterfaceKits = getInterfaceKitArray();
            return this.mInterfaceKits[i];
        }

        public int obtainInterfaceCount() {
            return getInterfaceKitArray().length;
        }

        private boolean checkConfigIndexValid(int i) {
            int obtainConfigurationCount;
            if (this.localConfigurations != null && (obtainConfigurationCount = obtainConfigurationCount()) != 0 && i < obtainConfigurationCount && i >= 0) {
                return true;
            }
            return false;
        }

        private boolean checkInterfaceIndexValid(int i) {
            int obtainInterfaceCount;
            if (this.mInterfaceKits != null && (obtainInterfaceCount = obtainInterfaceCount()) != 0 && i < obtainInterfaceCount && i >= 0) {
                return true;
            }
            return false;
        }

        public USBInterface obtainInterface(int i, int i2) {
            if (!checkConfigIndexValid(i)) {
                return new USBInterface(0, 0, "", 0, 0, 0);
            }
            if (!checkInterfaceIndexValid(i2)) {
                return new USBInterface(0, 0, "", 0, 0, 0);
            }
            return this.localConfigurations[i].getInterface(i2);
        }

        public int obtainInterfaceCount(int i) {
            if (!checkConfigIndexValid(i)) {
                return 0;
            }
            return this.localConfigurations[i].getInterfaceCount();
        }

        public int obtainId(int i) {
            if (!checkConfigIndexValid(i)) {
                return 0;
            }
            return this.localConfigurations[i].getId();
        }

        public int obtainMaxPower(int i) {
            if (!checkConfigIndexValid(i)) {
                return 0;
            }
            return this.localConfigurations[i].getMaxPower();
        }

        public String obtainName(int i) {
            if (!checkConfigIndexValid(i)) {
                return new String();
            }
            return this.localConfigurations[i].getName();
        }

        public boolean isRemoteWakeup(int i) {
            if (!checkConfigIndexValid(i)) {
                return false;
            }
            return this.localConfigurations[i].isRemoteWakeup();
        }

        public boolean isSelfPowered(int i) {
            if (!checkConfigIndexValid(i)) {
                return false;
            }
            return this.localConfigurations[i].isSelfPowered();
        }

        public void setUsbConfiguration(USBConfig[] uSBConfigArr) {
            this.localConfigurations = uSBConfigArr;
        }
    }
}
