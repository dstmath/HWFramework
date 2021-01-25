package ohos.global.configuration;

public class DeviceCapability {
    public static final int DEVICE_TYPE_CAR = 2;
    public static final int DEVICE_TYPE_GLASSES = 7;
    public static final int DEVICE_TYPE_HEADSET = 8;
    public static final int DEVICE_TYPE_PC = 3;
    public static final int DEVICE_TYPE_PHONE = 0;
    public static final int DEVICE_TYPE_SPEAKER = 5;
    public static final int DEVICE_TYPE_TABLET = 1;
    public static final int DEVICE_TYPE_TV = 4;
    public static final int DEVICE_TYPE_UNDEFINED = -1;
    public static final int DEVICE_TYPE_WATCH = 6;
    public static final int FLOAT_UNDEFINED = -1;
    public static final int SCREEN_ANY = -1;
    public static final int SCREEN_LDPI = 240;
    public static final int SCREEN_MDPI = 160;
    public static final int SCREEN_NODPI = -2;
    public static final int SCREEN_SDPI = 120;
    public static final int SCREEN_TVDPI = 213;
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
}
