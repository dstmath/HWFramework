package ohos.usb;

import java.util.Optional;

public class USBInterface {
    private final int localAlternateSetting;
    private final int localClass;
    private final int localId;
    private final String localName;
    private final int localProtocol;
    private final int localSubclass;
    private UsbInterfaceKitAdapter usbInterfaceKitAdapter = UsbInterfaceKitAdapter.getInstance();

    public int describeContents() {
        return 0;
    }

    public int describeContents(int i) {
        return i;
    }

    public String toString() {
        return "USBInterface []";
    }

    public USBInterface(int i, int i2, String str, int i3, int i4, int i5) {
        this.localId = i;
        this.localAlternateSetting = i2;
        this.localName = str;
        this.localClass = i3;
        this.localSubclass = i4;
        this.localProtocol = i5;
    }

    public int obtainAlternateSetting() {
        return this.localAlternateSetting;
    }

    public int obtainInterfaceId() {
        return this.localId;
    }

    public int obtainInterfaceClass() {
        return this.localClass;
    }

    public int obtainInterfaceProtocol() {
        return this.localProtocol;
    }

    public int obtainInterfaceSubclass() {
        return this.localSubclass;
    }

    public String obtainInterfaceName() {
        return this.localName;
    }

    public int obtainEndpointAddress(int i) {
        return this.usbInterfaceKitAdapter.obtainEndpointAddress(i);
    }

    public int obtainEndpointCount() {
        return this.usbInterfaceKitAdapter.obtainEndpointCount();
    }

    public int obtainEndpointAttributes(int i) {
        return this.usbInterfaceKitAdapter.obtainEndpointAttributes(i);
    }

    public int obtainEndpointDirection(int i) {
        return this.usbInterfaceKitAdapter.obtainEndpointDirection(i);
    }

    public int obtainEndpointNumber(int i) {
        return this.usbInterfaceKitAdapter.obtainEndpointNumber(i);
    }

    public int obtainEndpointInterval(int i) {
        return this.usbInterfaceKitAdapter.obtainEndpointInterval(i);
    }

    public int obtainEndpointMaxPacketSize(int i) {
        return this.usbInterfaceKitAdapter.obtainEndpointMaxPacketSize(i);
    }

    public int obtainEndpointType(int i) {
        return this.usbInterfaceKitAdapter.obtainEndpointType(i);
    }

    public Optional<String> toString(int i) {
        return Optional.of("USBInterface[localId=" + this.localId + ", localAlternateSetting=" + this.localAlternateSetting + ", localName=" + this.localName + ", localClass=" + this.localClass + ", localSubclass=" + this.localSubclass + ", endpointIndex=" + i + ", localProtocol=" + this.localProtocol + "]");
    }

    public void setEndpoints(USBEndpoint[] uSBEndpointArr) {
        this.usbInterfaceKitAdapter.setEndpoints(uSBEndpointArr);
    }

    private static class UsbInterfaceKitAdapter {
        private static final Object LOCK = new Object();
        private static volatile UsbInterfaceKitAdapter instance;
        private USBEndpoint[] mEndpoints;

        private UsbInterfaceKitAdapter() {
        }

        public static UsbInterfaceKitAdapter getInstance() {
            UsbInterfaceKitAdapter usbInterfaceKitAdapter;
            synchronized (LOCK) {
                if (instance == null) {
                    instance = new UsbInterfaceKitAdapter();
                }
                usbInterfaceKitAdapter = instance;
            }
            return usbInterfaceKitAdapter;
        }

        private boolean checkBounds(int i) {
            if (this.mEndpoints != null && i >= 0 && i < obtainEndpointCount()) {
                return true;
            }
            return false;
        }

        public void setEndpoints(USBEndpoint[] uSBEndpointArr) {
            this.mEndpoints = uSBEndpointArr;
        }

        public int obtainEndpointAddress(int i) {
            if (!checkBounds(i)) {
                return 0;
            }
            return this.mEndpoints[i].getAddress();
        }

        public int obtainEndpointCount() {
            USBEndpoint[] uSBEndpointArr = this.mEndpoints;
            if (uSBEndpointArr == null) {
                return 0;
            }
            return uSBEndpointArr.length;
        }

        public int obtainEndpointAttributes(int i) {
            if (!checkBounds(i)) {
                return 0;
            }
            return this.mEndpoints[i].getAttributes();
        }

        public int obtainEndpointDirection(int i) {
            if (!checkBounds(i)) {
                return 0;
            }
            return this.mEndpoints[i].getDirection();
        }

        public int obtainEndpointNumber(int i) {
            if (!checkBounds(i)) {
                return 0;
            }
            return this.mEndpoints[i].getEndpointNumber();
        }

        public int obtainEndpointInterval(int i) {
            if (!checkBounds(i)) {
                return 0;
            }
            return this.mEndpoints[i].getInterval();
        }

        public int obtainEndpointMaxPacketSize(int i) {
            if (!checkBounds(i)) {
                return 0;
            }
            return this.mEndpoints[i].getMaxPacketSize();
        }

        public int obtainEndpointType(int i) {
            if (!checkBounds(i)) {
                return 0;
            }
            return this.mEndpoints[i].getType();
        }
    }
}
