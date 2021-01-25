package ohos.usb;

public class USBConfig {
    private int localId;
    private int localInterfaceCount;
    private boolean localIsRemoteWakeup;
    private boolean localIsSelfPowered;
    private int localMaxPower;
    private String localName;
    private USBInterface[] mInterfaces;

    public USBConfig(int i, int i2, String str, boolean z, boolean z2, int i3) {
        this.localId = i;
        this.localMaxPower = i2;
        this.localName = str;
        this.localIsRemoteWakeup = z;
        this.localIsSelfPowered = z2;
        this.localInterfaceCount = i3;
    }

    public USBInterface getInterface(int i) {
        if (i < 0 || i >= getInterfaceCount()) {
            return new USBInterface(0, 0, "", 0, 0, 0);
        }
        return this.mInterfaces[i];
    }

    public void setInterfaces(USBInterface[] uSBInterfaceArr) {
        this.mInterfaces = uSBInterfaceArr;
    }

    public int getId() {
        return this.localId;
    }

    public int getMaxPower() {
        return this.localMaxPower;
    }

    public String getName() {
        return this.localName;
    }

    public boolean isRemoteWakeup() {
        return this.localIsRemoteWakeup;
    }

    public boolean isSelfPowered() {
        return this.localIsSelfPowered;
    }

    public int getInterfaceCount() {
        USBInterface[] uSBInterfaceArr = this.mInterfaces;
        if (uSBInterfaceArr == null) {
            return -1;
        }
        return uSBInterfaceArr.length;
    }
}
