package ohos.aafwk.ability;

import ohos.utils.Parcel;
import ohos.utils.Sequenceable;

public class DeviceConfigInfo implements Sequenceable {
    public static final int GLES_VERSION_DEFAULT = 0;
    public static final int INPUT_FLAG_FIVE_WAY_NAV = 2;
    public static final int INPUT_FLAG_HARD_KEYBOARD = 1;
    public static final int KEY_BOARD_12KEY = 3;
    public static final int KEY_BOARD_COMMON = 2;
    public static final int KEY_BOARD_DEFAULT = 0;
    public static final int KEY_BOARD_NOKEYS = 1;
    public static final int NAVIGATION_DEFAULT = 0;
    public static final int NAVIGATION_DPAD = 2;
    public static final int NAVIGATION_NONAV = 1;
    public static final int NAVIGATION_TRACKBALL = 3;
    public static final int NAVIGATION_WHEEL = 4;
    public static final Sequenceable.Producer<DeviceConfigInfo> PRODUCER = $$Lambda$DeviceConfigInfo$egF5mXP1ErP2x8bul60UmMdboYQ.INSTANCE;
    public static final int TOUCH_SCREEN_DEFAULT = 0;
    public static final int TOUCH_SCREEN_FINGER = 3;
    public static final int TOUCH_SCREEN_NOTOUCH = 1;
    private int deviceGLESVersion;
    private int externalInputDevices = 0;
    private int keyBoardType;
    private int navigationType;
    private int touchScreenType;

    public DeviceConfigInfo() {
    }

    static /* synthetic */ DeviceConfigInfo lambda$static$0(Parcel parcel) {
        DeviceConfigInfo deviceConfigInfo = new DeviceConfigInfo();
        deviceConfigInfo.unmarshalling(parcel);
        return deviceConfigInfo;
    }

    public void setTouchScreenType(int i) {
        this.touchScreenType = i;
    }

    public void setKeyBoardType(int i) {
        this.keyBoardType = i;
    }

    public void setNavigationType(int i) {
        this.navigationType = i;
    }

    public void setExternalInputDevices(int i) {
        this.externalInputDevices = i;
    }

    public void setDeviceGLESVersion(int i) {
        this.deviceGLESVersion = i;
    }

    public int getTouchScreenType() {
        return this.touchScreenType;
    }

    public int getKeyBoardType() {
        return this.keyBoardType;
    }

    public int getNavigationType() {
        return this.navigationType;
    }

    public int getExternalInputDevices() {
        return this.externalInputDevices;
    }

    public int getDeviceGLESVersion() {
        return this.deviceGLESVersion;
    }

    public boolean marshalling(Parcel parcel) {
        if (parcel.writeInt(this.touchScreenType) && parcel.writeInt(this.keyBoardType) && parcel.writeInt(this.navigationType) && parcel.writeInt(this.externalInputDevices) && parcel.writeInt(this.deviceGLESVersion)) {
            return true;
        }
        return false;
    }

    public boolean unmarshalling(Parcel parcel) {
        this.touchScreenType = parcel.readInt();
        this.keyBoardType = parcel.readInt();
        this.navigationType = parcel.readInt();
        this.externalInputDevices = parcel.readInt();
        this.deviceGLESVersion = parcel.readInt();
        return true;
    }

    private DeviceConfigInfo(Parcel parcel) {
        unmarshalling(parcel);
    }
}
