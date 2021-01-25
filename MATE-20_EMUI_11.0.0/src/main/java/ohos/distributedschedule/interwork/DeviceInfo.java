package ohos.distributedschedule.interwork;

import ohos.utils.Parcel;
import ohos.utils.Sequenceable;

public class DeviceInfo implements Sequenceable {
    public static final int FLAG_GET_ALL_DEVICE = 0;
    public static final int FLAG_GET_OFFLINE_DEVICE = 2;
    public static final int FLAG_GET_ONLINE_DEVICE = 1;
    private String mDeviceId;
    private String mDeviceName;
    private DeviceState mDeviceState = DeviceState.OFFLINE;
    private DeviceType mDeviceType = DeviceType.UNKNOWN_TYPE;

    public enum DeviceState {
        UNKNOWN,
        ONLINE,
        OFFLINE
    }

    public void setDeviceInfo(String str, String str2) {
        this.mDeviceId = str;
        this.mDeviceName = str2;
    }

    public void setDeviceType(DeviceType deviceType) {
        this.mDeviceType = deviceType;
    }

    public void setDeviceState(DeviceState deviceState) {
        this.mDeviceState = deviceState;
    }

    public String getDeviceId() {
        return this.mDeviceId;
    }

    public String getDeviceName() {
        return this.mDeviceName;
    }

    public DeviceType getDeviceType() {
        return this.mDeviceType;
    }

    public DeviceState getDeviceState() {
        return this.mDeviceState;
    }

    public boolean isDeviceOnline() {
        return this.mDeviceState == DeviceState.ONLINE;
    }

    public boolean marshalling(Parcel parcel) {
        String str;
        if (parcel == null || this.mDeviceType == null || (str = this.mDeviceId) == null || this.mDeviceName == null || this.mDeviceState == null || !parcel.writeString(str) || !parcel.writeString(this.mDeviceName) || !parcel.writeInt(this.mDeviceType.mVal) || !parcel.writeInt(this.mDeviceState.ordinal())) {
            return false;
        }
        return true;
    }

    public boolean unmarshalling(Parcel parcel) {
        if (parcel == null) {
            return false;
        }
        this.mDeviceId = parcel.readString();
        if (this.mDeviceId == null) {
            return false;
        }
        this.mDeviceName = parcel.readString();
        if (this.mDeviceName == null) {
            return false;
        }
        int readInt = parcel.readInt();
        this.mDeviceType = DeviceType.UNKNOWN_TYPE;
        DeviceType[] values = DeviceType.values();
        int length = values.length;
        int i = 0;
        while (true) {
            if (i >= length) {
                break;
            }
            DeviceType deviceType = values[i];
            if (deviceType.mVal == readInt) {
                this.mDeviceType = deviceType;
                break;
            }
            i++;
        }
        int readInt2 = parcel.readInt();
        if (readInt2 < 0 || readInt2 >= DeviceState.values().length) {
            return false;
        }
        this.mDeviceState = DeviceState.values()[readInt2];
        return true;
    }

    public enum DeviceType {
        UNKNOWN_TYPE(0),
        LAPTOP(12),
        SMART_PHONE(14),
        SMART_PAD(17),
        SMART_WATCH(109),
        SMART_CAR(131),
        SMART_TV(156);
        
        private final int mVal;

        private DeviceType(int i) {
            this.mVal = i;
        }

        public int getVal() {
            return this.mVal;
        }
    }
}
