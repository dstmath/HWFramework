package ohos.global.configuration;

public class DeviceCapability {
    public static final int DEVICE_TYPE_CAR = 2;
    public static final int DEVICE_TYPE_PC = 3;
    public static final int DEVICE_TYPE_PHONE = 0;
    public static final int DEVICE_TYPE_TABLET = 1;
    public static final int DEVICE_TYPE_TV = 4;
    public static final int DEVICE_TYPE_UNDEFINED = -1;
    public static final int DEVICE_TYPE_WEARABLE = 6;
    public static final int FLOAT_UNDEFINED = -1;
    public static final int SCREEN_DEFAULT = -2;
    public static final int SCREEN_LDPI = 240;
    public static final int SCREEN_MDPI = 160;
    public static final int SCREEN_SDPI = 120;
    public static final int SCREEN_XLDPI = 320;
    public static final int SCREEN_XXLDPI = 480;
    public static final int SCREEN_XXXLDPI = 640;
    public int deviceType;
    public int height;
    public boolean isRound;
    public int screenDensity;
    public int width;

    public DeviceCapability() {
        this(null);
    }

    public DeviceCapability(DeviceCapability deviceCapability) {
        if (deviceCapability == null) {
            this.screenDensity = -2;
            this.deviceType = 0;
            this.isRound = false;
            this.width = -1;
            this.height = -1;
            return;
        }
        this.screenDensity = deviceCapability.screenDensity;
        this.deviceType = deviceCapability.deviceType;
        this.isRound = deviceCapability.isRound;
        this.width = deviceCapability.width;
        this.height = deviceCapability.height;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof DeviceCapability)) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        DeviceCapability deviceCapability = (DeviceCapability) obj;
        return deviceCapability.screenDensity == this.screenDensity && deviceCapability.deviceType == this.deviceType && deviceCapability.isRound == this.isRound && deviceCapability.width == this.width && deviceCapability.height == this.height;
    }

    public int hashCode() {
        return ((((((((527 + this.screenDensity) * 31) + this.deviceType) * 31) + (this.isRound ? 1 : 0)) * 31) + this.width) * 31) + this.height;
    }
}
